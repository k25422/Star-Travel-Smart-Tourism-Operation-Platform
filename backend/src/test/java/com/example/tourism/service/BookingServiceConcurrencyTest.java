package com.example.tourism.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.example.tourism.domain.Booking;
import com.example.tourism.domain.BookingStatus;
import com.example.tourism.domain.Destination;
import com.example.tourism.domain.TravelRoute;
import com.example.tourism.dto.BookingRequest;
import com.example.tourism.repository.BookingRepository;
import com.example.tourism.repository.DestinationRepository;
import com.example.tourism.repository.OutboxEventRepository;
import com.example.tourism.repository.TravelRouteRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:tourism-test;MODE=MySQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE;LOCK_TIMEOUT=10000",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "app.kafka.enabled=false"
})
class BookingServiceConcurrencyTest {

    @Autowired
    private BookingService bookingService;

    @Autowired
    private DestinationRepository destinationRepository;

    @Autowired
    private TravelRouteRepository routeRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private OutboxEventRepository outboxEventRepository;

    @Test
    void shouldPreventOversellingUnderConcurrentRequests() throws Exception {
        TravelRoute route = createRouteWithSeats(5);
        long outboxCountBefore = outboxEventRepository.count();

        int requestCount = 12;
        CountDownLatch ready = new CountDownLatch(requestCount);
        CountDownLatch start = new CountDownLatch(1);
        ExecutorService executor = Executors.newFixedThreadPool(requestCount);
        List<Future<Boolean>> futures = new ArrayList<Future<Boolean>>();

        for (int i = 0; i < requestCount; i++) {
            final int index = i;
            futures.add(executor.submit(new Callable<Boolean>() {
                @Override
                public Boolean call() throws Exception {
                    ready.countDown();
                    start.await(5, TimeUnit.SECONDS);
                    BookingRequest request = new BookingRequest();
                    request.setRouteId(route.getId());
                    request.setTravelerName("并发游客" + index);
                    request.setPhone("1380000" + (1000 + index));
                    request.setTravelers(1);
                    request.setRequestId("load-" + index);
                    try {
                        bookingService.createBooking(request, "traveler");
                        return true;
                    } catch (IllegalArgumentException ex) {
                        return false;
                    }
                }
            }));
        }

        ready.await(5, TimeUnit.SECONDS);
        start.countDown();

        int successCount = 0;
        for (Future<Boolean> future : futures) {
            if (future.get(10, TimeUnit.SECONDS)) {
                successCount++;
            }
        }
        executor.shutdownNow();

        TravelRoute latestRoute = routeRepository.findById(route.getId()).orElseThrow(IllegalStateException::new);
        assertEquals(5, successCount, "只应该成功卖出库存数量对应的订单");
        assertEquals(0, latestRoute.getAvailableSeats().intValue(), "并发下单后库存不能变成负数");
        assertEquals(5L, bookingRepository.countByRouteId(route.getId()), "订单数量应与实际卖出的库存一致");
        assertEquals(outboxCountBefore + 5L, outboxEventRepository.count(), "每个成功订单都应该生成一条 outbox 事件");
    }

    @Test
    @Transactional
    void shouldTreatSameRequestIdAsIdempotent() {
        TravelRoute route = createRouteWithSeats(3);
        BookingRequest request = new BookingRequest();
        request.setRouteId(route.getId());
        request.setTravelerName("幂等游客");
        request.setPhone("13800138099");
        request.setTravelers(1);
        request.setRequestId("idem-001");

        Booking first = bookingService.createBooking(request, "traveler");
        Booking second = bookingService.createBooking(request, "traveler");

        TravelRoute latestRoute = routeRepository.findById(route.getId()).orElseThrow(IllegalStateException::new);
        assertNotNull(first.getId());
        assertEquals(first.getId(), second.getId(), "相同 requestId 应返回同一笔订单");
        assertEquals(2, latestRoute.getAvailableSeats().intValue(), "幂等重复请求不能重复扣库存");
        assertEquals(1L, bookingRepository.countByRouteId(route.getId()), "相同 requestId 只能落一笔订单");
    }

    @Test
    void shouldAllowTravelerToCancelOwnBookingAndReleaseSeats() {
        TravelRoute route = createRouteWithSeats(3);
        BookingRequest request = new BookingRequest();
        request.setRouteId(route.getId());
        request.setTravelerName("Cancel Test Traveler");
        request.setPhone("13800138100");
        request.setTravelers(2);
        request.setRequestId("cancel-own-001");

        Booking booking = bookingService.createBooking(request, "traveler");
        Booking cancelled = bookingService.cancelOwnBooking(booking.getId(), "traveler");
        TravelRoute latestRoute = routeRepository.findById(route.getId()).orElseThrow(IllegalStateException::new);

        assertEquals(BookingStatus.CANCELLED, cancelled.getStatus(), "Traveler should be able to cancel their own booking");
        assertEquals(3, latestRoute.getAvailableSeats().intValue(), "Canceling a booking should release occupied seats");
    }

    @Test
    void shouldRejectCancelingOtherTravelersBooking() {
        TravelRoute route = createRouteWithSeats(3);
        BookingRequest request = new BookingRequest();
        request.setRouteId(route.getId());
        request.setTravelerName("Owner Test Traveler");
        request.setPhone("13800138101");
        request.setTravelers(1);
        request.setRequestId("cancel-other-001");

        Booking booking = bookingService.createBooking(request, "traveler");

        assertThrows(IllegalArgumentException.class, new org.junit.jupiter.api.function.Executable() {
            @Override
            public void execute() {
                bookingService.cancelOwnBooking(booking.getId(), "another-user");
            }
        }, "Traveler should not be able to cancel another user's booking");
    }

    private TravelRoute createRouteWithSeats(int seats) {
        Destination destination = new Destination();
        destination.setName("压测目的地" + System.nanoTime());
        destination.setProvince("测试省");
        destination.setTheme("并发");
        destination.setDescription("并发测试专用目的地");
        destination.setCoverImage("https://example.com/test.jpg");
        destination.setRating(4.9);
        destination.setPopularityScore(99);
        destinationRepository.save(destination);

        TravelRoute route = new TravelRoute();
        route.setDestination(destination);
        route.setTitle("高并发压测路线" + System.nanoTime());
        route.setDurationDays(3);
        route.setPrice(new BigDecimal("1999.00"));
        route.setAvailableSeats(seats);
        route.setDepartureCity("上海");
        route.setDepartureDate(LocalDate.now().plusDays(10));
        route.setGuideName("测试导游");
        route.setHighlight("用于并发防超卖验证");
        return routeRepository.save(route);
    }
}
