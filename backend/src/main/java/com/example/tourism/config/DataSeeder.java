package com.example.tourism.config;

import com.example.tourism.domain.AccountUser;
import com.example.tourism.domain.Booking;
import com.example.tourism.domain.BookingStatus;
import com.example.tourism.domain.Destination;
import com.example.tourism.domain.TravelRoute;
import com.example.tourism.domain.UserRole;
import com.example.tourism.repository.AccountUserRepository;
import com.example.tourism.repository.BookingRepository;
import com.example.tourism.repository.DestinationRepository;
import com.example.tourism.repository.TravelRouteRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import javax.transaction.Transactional;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataSeeder implements CommandLineRunner {

    private final AccountUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final DestinationRepository destinationRepository;
    private final TravelRouteRepository routeRepository;
    private final BookingRepository bookingRepository;

    public DataSeeder(
            AccountUserRepository userRepository,
            PasswordEncoder passwordEncoder,
            DestinationRepository destinationRepository,
            TravelRouteRepository routeRepository,
            BookingRepository bookingRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.destinationRepository = destinationRepository;
        this.routeRepository = routeRepository;
        this.bookingRepository = bookingRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        createUser("admin", "admin123", "平台管理员", "admin@tourism.local", UserRole.ADMIN);
        createUser("traveler", "user123", "体验游客", "traveler@tourism.local", UserRole.USER);

        if (destinationRepository.count() == 0 && routeRepository.count() == 0) {
            seedTourismData();
        }

        assignLegacyBookingsToTraveler();
    }

    private void seedTourismData() {
        Destination zhangjiajie = createDestination("张家界天空森林", "湖南", "山岳", "奇峰石柱、玻璃桥和云雾峡谷构成沉浸式山地旅行体验。", "https://images.unsplash.com/photo-1500530855697-b586d89ba3ee?auto=format&fit=crop&w=1200&q=80", 4.8, 97);
        Destination sanya = createDestination("三亚珊瑚海岸", "海南", "海岛", "温暖海滩、潜水线路和适合亲子度假的海岛旅程。", "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?auto=format&fit=crop&w=1200&q=80", 4.7, 92);
        Destination dunhuang = createDestination("敦煌丝路古境", "甘肃", "文化", "沙漠落日、石窟艺术和夜市风情串起丝路文化体验。", "https://images.unsplash.com/photo-1500534314209-a25ddb2bd429?auto=format&fit=crop&w=1200&q=80", 4.9, 88);
        Destination hangzhou = createDestination("杭州西湖慢旅", "浙江", "休闲", "湖畔骑行、茶村漫步和周末轻度假的慢节奏旅程。", "https://images.unsplash.com/photo-1500382017468-9049fed747ef?auto=format&fit=crop&w=1200&q=80", 4.6, 85);
        Destination beijing = createDestination("北京长城古道", "北京", "文化", "长城徒步、胡同夜游和非遗手作结合的历史文化路线。", "https://images.unsplash.com/photo-1508804185872-d7badad00f7d?auto=format&fit=crop&w=1200&q=80", 4.8, 94);
        Destination chengdu = createDestination("成都熊猫慢城", "四川", "亲子", "熊猫基地、川菜体验和城市茶馆串联成轻松亲子假期。", "https://images.unsplash.com/photo-1528360983277-13d401cdc186?auto=format&fit=crop&w=1200&q=80", 4.7, 90);
        Destination lijiang = createDestination("丽江雪山古城", "云南", "度假", "雪山观景、古城漫游和纳西文化体验适合深度慢游。", "https://images.unsplash.com/photo-1513415564515-763d91423bdd?auto=format&fit=crop&w=1200&q=80", 4.8, 91);
        Destination guilin = createDestination("桂林漓江山水", "广西", "山水", "漓江游船、阳朔骑行和喀斯特峰林构成经典山水旅行。", "https://images.unsplash.com/photo-1500534314209-a25ddb2bd429?auto=format&fit=crop&w=1200&q=80", 4.7, 89);
        Destination xiamen = createDestination("厦门鼓浪屿海风", "福建", "海岛", "海边步道、音乐街区和闽南小吃适合情侣与轻旅行人群。", "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?auto=format&fit=crop&w=1200&q=80", 4.5, 83);
        Destination harbin = createDestination("哈尔滨冰雪童话", "黑龙江", "冰雪", "冰雪大世界、中央大街和俄式建筑打造冬季主题旅行。", "https://images.unsplash.com/photo-1483921020237-2ff51e8e4b22?auto=format&fit=crop&w=1200&q=80", 4.6, 86);
        Destination qingdao = createDestination("青岛海岸啤酒城", "山东", "海滨", "红瓦绿树、海岸骑行和啤酒文化组成轻松海滨路线。", "https://images.unsplash.com/photo-1500530855697-b586d89ba3ee?auto=format&fit=crop&w=1200&q=80", 4.5, 82);
        Destination suzhou = createDestination("苏州园林水巷", "江苏", "文化", "古典园林、评弹茶馆和水巷夜游适合江南文化体验。", "https://images.unsplash.com/photo-1500382017468-9049fed747ef?auto=format&fit=crop&w=1200&q=80", 4.7, 87);
        Destination huangshan = createDestination("黄山云海松石", "安徽", "山岳", "云海、奇松、温泉和徽州古村落组合成高品质山地旅程。", "https://images.unsplash.com/photo-1500530855697-b586d89ba3ee?auto=format&fit=crop&w=1200&q=80", 4.9, 95);
        Destination xian = createDestination("西安盛唐古都", "陕西", "文化", "兵马俑、古城墙和大唐不夜城构成沉浸式古都旅行。", "https://images.unsplash.com/photo-1508804185872-d7badad00f7d?auto=format&fit=crop&w=1200&q=80", 4.8, 93);
        Destination chongqing = createDestination("重庆山城夜景", "重庆", "城市", "洪崖洞夜景、轻轨穿楼和火锅文化适合年轻客群。", "https://images.unsplash.com/photo-1500534314209-a25ddb2bd429?auto=format&fit=crop&w=1200&q=80", 4.6, 90);
        Destination shanghai = createDestination("上海都市微度假", "上海", "城市", "外滩夜景、艺术展馆和精品酒店适合都市短途微度假。", "https://images.unsplash.com/photo-1491553895911-0055eca6402d?auto=format&fit=crop&w=1200&q=80", 4.5, 84);
        Destination guizhou = createDestination("贵州苗寨瀑布群", "贵州", "民俗", "苗寨非遗、黄果树瀑布和峡谷徒步适合自然民俗结合游。", "https://images.unsplash.com/photo-1500530855697-b586d89ba3ee?auto=format&fit=crop&w=1200&q=80", 4.7, 88);
        Destination tibet = createDestination("拉萨圣城高原", "西藏", "高原", "布达拉宫、八廓街和高原湖泊组成慢节奏朝圣旅行。", "https://images.unsplash.com/photo-1500534314209-a25ddb2bd429?auto=format&fit=crop&w=1200&q=80", 4.9, 96);
        Destination xining = createDestination("青海湖高原花海", "青海", "高原", "青海湖、茶卡盐湖和环湖骑行适合夏季避暑旅程。", "https://images.unsplash.com/photo-1500382017468-9049fed747ef?auto=format&fit=crop&w=1200&q=80", 4.8, 92);
        Destination zhengzhou = createDestination("洛阳牡丹龙门", "河南", "文化", "龙门石窟、牡丹花季和古都夜游适合春季文化路线。", "https://images.unsplash.com/photo-1508804185872-d7badad00f7d?auto=format&fit=crop&w=1200&q=80", 4.6, 81);

        TravelRoute r1 = createRoute(zhangjiajie, "张家界天门奇峰深度游", 5, "3699.00", 18, "上海", "2026-07-12", "陈宇", "玻璃桥、峡谷徒步与日出摄影一次体验");
        TravelRoute r2 = createRoute(sanya, "三亚珊瑚湾海岛假期", 4, "4299.00", 27, "广州", "2026-07-18", "林楠", "潜水课程、游艇日落和海鲜市场打卡");
        TravelRoute r3 = createRoute(dunhuang, "敦煌丝路文化探索营", 6, "5199.00", 16, "西安", "2026-08-03", "马睿", "莫高窟研学、沙漠露营与驼队古道体验");
        TravelRoute r4 = createRoute(hangzhou, "西湖茶村周末轻旅行", 3, "1899.00", 32, "南京", "2026-06-27", "赵青", "茶村午餐、湖畔骑行和精品民宿体验");
        TravelRoute r5 = createRoute(beijing, "长城日落与胡同夜游", 4, "3299.00", 22, "天津", "2026-07-05", "周航", "八达岭长城轻徒步、胡同夜游和京味餐桌");
        TravelRoute r6 = createRoute(chengdu, "成都熊猫亲子美食营", 4, "2999.00", 28, "重庆", "2026-07-20", "唐佳", "熊猫基地、川菜课堂、宽窄巷子慢游");
        TravelRoute r7 = createRoute(lijiang, "丽江雪山古城度假线", 5, "4599.00", 14, "昆明", "2026-08-12", "和云", "玉龙雪山观景、古城旅拍和纳西文化体验");
        TravelRoute r8 = createRoute(guilin, "桂林漓江山水摄影游", 4, "2699.00", 20, "长沙", "2026-07-30", "陆川", "漓江游船、阳朔骑行和山水摄影指导");
        TravelRoute r9 = createRoute(xiamen, "厦门鼓浪屿海风周末", 3, "2199.00", 26, "福州", "2026-08-08", "许晴", "鼓浪屿漫步、沙坡尾市集和海边落日");
        TravelRoute r10 = createRoute(harbin, "哈尔滨冰雪童话之旅", 5, "3999.00", 12, "沈阳", "2026-12-18", "郭冬", "冰雪大世界、中央大街和俄式建筑打卡");
        TravelRoute r11 = createRoute(qingdao, "青岛海岸啤酒节路线", 3, "2399.00", 34, "济南", "2026-08-16", "宋海", "海岸骑行、啤酒博物馆和老城街区漫游");
        TravelRoute r12 = createRoute(suzhou, "苏州园林水巷雅集", 3, "2099.00", 30, "上海", "2026-09-06", "沈澜", "拙政园、平江路夜游和评弹茶馆体验");
        TravelRoute r13 = createRoute(huangshan, "黄山云海徽州古村游", 5, "4399.00", 15, "杭州", "2026-09-18", "程峰", "黄山日出、宏村写生和徽州家宴");
        TravelRoute r14 = createRoute(xian, "西安盛唐古都研学线", 5, "3899.00", 24, "郑州", "2026-07-26", "秦岚", "兵马俑、古城墙骑行和唐风夜游");
        TravelRoute r15 = createRoute(chongqing, "重庆山城夜景火锅游", 3, "1999.00", 36, "成都", "2026-08-01", "罗川", "洪崖洞夜景、轻轨穿楼和火锅体验");
        TravelRoute r16 = createRoute(shanghai, "上海艺术酒店微度假", 2, "1599.00", 40, "苏州", "2026-07-09", "顾然", "外滩夜景、艺术展馆和精品酒店下午茶");
        TravelRoute r17 = createRoute(guizhou, "贵州苗寨瀑布民俗游", 5, "3499.00", 18, "贵阳", "2026-08-19", "苗青", "黄果树瀑布、西江苗寨和非遗银饰体验");
        TravelRoute r18 = createRoute(tibet, "拉萨圣城高原慢游", 7, "6999.00", 10, "成都", "2026-09-10", "旦增", "布达拉宫、八廓街和羊湖高原风光");
        TravelRoute r19 = createRoute(xining, "青海湖盐湖避暑线", 6, "4899.00", 19, "兰州", "2026-07-14", "马青", "青海湖环线、茶卡盐湖和高原花海");
        TravelRoute r20 = createRoute(zhengzhou, "洛阳牡丹龙门文化游", 4, "2599.00", 27, "郑州", "2026-04-12", "杜若", "龙门石窟、牡丹花季和洛邑古城夜游");
        createRoute(sanya, "三亚家庭海岛轻奢游", 5, "5599.00", 18, "深圳", "2026-08-22", "林楠", "亲子酒店、沙滩营地、免税店和海上项目");
        createRoute(beijing, "北京研学文化精品线", 5, "3899.00", 30, "济南", "2026-09-03", "周航", "故宫主题讲解、长城研学和非遗手作课堂");
        createRoute(dunhuang, "敦煌星空沙漠露营线", 4, "3799.00", 21, "兰州", "2026-08-28", "马睿", "鸣沙山星空、沙漠露营和丝路主题讲解");
        createRoute(hangzhou, "杭州宋韵茶文化轻奢游", 2, "1699.00", 38, "上海", "2026-10-02", "赵青", "龙井问茶、宋韵夜游和湖畔精品民宿");

        if (bookingRepository.count() == 0) {
            createBooking(r1, "王明", "13800138001", 2, BookingStatus.CONFIRMED);
            createBooking(r2, "刘妍", "13800138002", 3, BookingStatus.PENDING);
            createBooking(r4, "孙伟", "13800138003", 1, BookingStatus.CONFIRMED);
            createBooking(r5, "李娜", "13800138004", 2, BookingStatus.CONFIRMED);
            createBooking(r7, "陈晨", "13800138005", 2, BookingStatus.PENDING);
            createBooking(r8, "赵阳", "13800138006", 4, BookingStatus.CONFIRMED);
            createBooking(r10, "何雪", "13800138007", 2, BookingStatus.CANCELLED);
            createBooking(r12, "吴迪", "13800138008", 3, BookingStatus.CONFIRMED);
            createBooking(r14, "林可", "13800138009", 2, BookingStatus.CONFIRMED);
            createBooking(r17, "周宁", "13800138010", 2, BookingStatus.PENDING);
            createBooking(r18, "白宇", "13800138011", 1, BookingStatus.CONFIRMED);
            createBooking(r20, "郑欣", "13800138012", 3, BookingStatus.CONFIRMED);
        }
    }

    private Destination createDestination(String name, String province, String theme, String description, String coverImage, double rating, int popularityScore) {
        Destination destination = new Destination();
        destination.setName(name);
        destination.setProvince(province);
        destination.setTheme(theme);
        destination.setDescription(description);
        destination.setCoverImage(coverImage);
        destination.setRating(rating);
        destination.setPopularityScore(popularityScore);
        return destinationRepository.save(destination);
    }

    private TravelRoute createRoute(Destination destination, String title, int days, String price, int seats, String city, String date, String guideName, String highlight) {
        TravelRoute route = new TravelRoute();
        route.setDestination(destination);
        route.setTitle(title);
        route.setDurationDays(days);
        route.setPrice(new BigDecimal(price));
        route.setAvailableSeats(seats);
        route.setDepartureCity(city);
        route.setDepartureDate(LocalDate.parse(date));
        route.setGuideName(guideName);
        route.setHighlight(highlight);
        return routeRepository.save(route);
    }

    private void createBooking(TravelRoute route, String travelerName, String phone, int travelers, BookingStatus status) {
        Booking booking = new Booking();
        booking.setRoute(route);
        booking.setUser(userRepository.findByUsername("traveler").orElse(null));
        booking.setTravelerName(travelerName);
        booking.setPhone(phone);
        booking.setTravelers(travelers);
        booking.setTotalAmount(route.getPrice().multiply(BigDecimal.valueOf(travelers)));
        booking.setStatus(status);
        booking.setCreatedAt(LocalDateTime.now());
        bookingRepository.save(booking);
    }

    private void assignLegacyBookingsToTraveler() {
        AccountUser traveler = userRepository.findByUsername("traveler").orElse(null);
        if (traveler == null) {
            return;
        }

        List<Booking> legacyBookings = bookingRepository.findByUserIsNull();
        for (Booking booking : legacyBookings) {
            booking.setUser(traveler);
        }
        bookingRepository.saveAll(legacyBookings);
    }

    private void createUser(String username, String password, String nickname, String email, UserRole role) {
        if (userRepository.existsByUsername(username)) {
            return;
        }
        AccountUser user = new AccountUser();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setNickname(nickname);
        user.setEmail(email);
        user.setRole(role);
        userRepository.save(user);
    }
}
