<script setup lang="ts">
import { computed, reactive, ref } from "vue";
import { ElMessage, ElMessageBox } from "element-plus";
import { Calendar, CollectionTag, Search, Star, SuitcaseLine, Tickets } from "@element-plus/icons-vue";
import AdminLayout from "../components/AdminLayout.vue";
import DestinationHoverScene from "../components/DestinationHoverScene.vue";
import SharedGlobeScene from "../components/SharedGlobeScene.vue";
import { cancelMyBooking, createBooking, getDestinations, getMyBookings, getRoutes, type Booking, type Destination, type TravelRoute } from "../api/http";
import { displayBooking, displayDestination, displayRoute } from "../utils/display";

const loading = ref(false);
const bookingVisible = ref(false);
const selectedRoute = ref<TravelRoute | null>(null);
const keyword = ref("");
const themeFilter = ref("");
const maxPrice = ref(8000);
const favoriteIds = ref<number[]>(JSON.parse(localStorage.getItem("tourism_favorites") || "[]"));
const compareIds = ref<number[]>([]);
const compareBarVisible = ref(false);
const destinations = ref<Destination[]>([]);
const routes = ref<TravelRoute[]>([]);
const bookings = ref<Booking[]>([]);
const hoveredRoute = ref<TravelRoute | null>(null);
const previewVisible = ref(false);
const previewX = ref(0);
const previewY = ref(0);
const cancelingIds = ref<number[]>([]);

const bookingForm = reactive({ travelerName: "张同学", phone: "13800000000", travelers: 2 });

const displayDestinations = computed(() => destinations.value.map(displayDestination));
const displayRoutes = computed(() => routes.value.map(displayRoute));
const themes = computed(() => Array.from(new Set(displayDestinations.value.map((item) => item.theme))));
const heroRoute = computed(() => displayRoutes.value[0]);
const filteredRoutes = computed(() => {
  const word = keyword.value.trim().toLowerCase();
  return displayRoutes.value.filter((item) => {
    const matchesWord = !word || [item.title, item.destination?.name, item.destination?.province, item.highlight]
      .filter(Boolean)
      .some((text) => String(text).toLowerCase().includes(word));
    const matchesTheme = !themeFilter.value || item.destination?.theme === themeFilter.value;
    const matchesPrice = Number(item.price) <= maxPrice.value;
    return matchesWord && matchesTheme && matchesPrice;
  });
});
const recommendedRoutes = computed(() => filteredRoutes.value);
const travelInfoCards = [
  { title: "暑期亲子精选", tag: "亲子", text: "三亚、成都、北京研学路线适合家庭出行，优先关注余位和酒店资源。" },
  { title: "摄影玩家推荐", tag: "摄影", text: "张家界、桂林、敦煌适合日出日落拍摄，建议选择 4 天以上行程。" },
  { title: "城市周末轻旅行", tag: "周末", text: "杭州、厦门适合短途放松，预算低、节奏慢，适合上班族快速出发。" },
  { title: "冬季冰雪计划", tag: "冰雪", text: "哈尔滨路线适合 12 月后出发，建议提前锁定房源和冰雪项目票。" }
];
const compareRoutes = computed(() => displayRoutes.value.filter((item) => item.id && compareIds.value.includes(item.id)));
const myBookings = computed(() => bookings.value.map(displayBooking));
const bookingSummary = computed(() => ({
  total: myBookings.value.length,
  active: myBookings.value.filter((item) => item.status !== "CANCELLED").length,
  revenue: myBookings.value.reduce((sum, item) => item.status === "CANCELLED" ? sum : sum + Number(item.totalAmount), 0)
}));
const statusMap = {
  PENDING: { label: "待确认", type: "warning" },
  CONFIRMED: { label: "已确认", type: "success" },
  CANCELLED: { label: "已取消", type: "danger" }
} as const;

async function loadData() {
  loading.value = true;
  try {
    const [routeData, destinationData, bookingData] = await Promise.all([getRoutes(), getDestinations(), getMyBookings()]);
    routes.value = routeData;
    destinations.value = destinationData;
    bookings.value = bookingData;
  } finally {
    loading.value = false;
  }
}

function toggleFavorite(route: TravelRoute) {
  if (!route.id) return;
  if (favoriteIds.value.includes(route.id)) {
    favoriteIds.value = favoriteIds.value.filter((id) => id !== route.id);
    ElMessage.info("已取消收藏");
  } else {
    favoriteIds.value.push(route.id);
    ElMessage.success("已加入收藏");
  }
  localStorage.setItem("tourism_favorites", JSON.stringify(favoriteIds.value));
}

function toggleCompare(route: TravelRoute) {
  if (!route.id) return;
  if (compareIds.value.includes(route.id)) {
    compareIds.value = compareIds.value.filter((id) => id !== route.id);
    compareBarVisible.value = compareIds.value.length > 0;
    ElMessage.info("已移出对比");
    return;
  }
  if (compareIds.value.length >= 3) {
    ElMessage.warning("最多同时对比 3 条路线");
    return;
  }
  compareIds.value.push(route.id);
  compareBarVisible.value = true;
  ElMessage.success("已加入对比，右侧对比中心已更新");
}

function openBooking(route: TravelRoute) {
  selectedRoute.value = route;
  bookingVisible.value = true;
}

function showPreview(route: TravelRoute, event: MouseEvent) {
  hoveredRoute.value = route;
  previewVisible.value = true;
  movePreview(event);
}

function movePreview(event: MouseEvent) {
  previewX.value = Math.min(window.innerWidth - 380, event.clientX + 20);
  previewY.value = Math.min(window.innerHeight - 340, event.clientY - 20);
}

function hidePreview() {
  previewVisible.value = false;
}

function createBookingRequestId(routeId: number) {
  if (typeof crypto !== "undefined" && typeof crypto.randomUUID === "function") {
    return crypto.randomUUID();
  }
  return `booking-${routeId}-${Date.now()}-${Math.random().toString(16).slice(2)}`;
}

async function submitBooking() {
  if (!selectedRoute.value?.id) return;
  if (!bookingForm.travelerName || !bookingForm.phone || bookingForm.travelers < 1) {
    ElMessage.warning("请补全游客姓名、手机号和出行人数");
    return;
  }
  await createBooking({
    routeId: selectedRoute.value.id,
    travelerName: bookingForm.travelerName,
    phone: bookingForm.phone,
    travelers: bookingForm.travelers,
    requestId: createBookingRequestId(selectedRoute.value.id)
  });
  ElMessage.success("预订成功，可在我的订单中查看");
  bookingVisible.value = false;
  await loadData();
}

async function cancelBooking(item: Booking) {
  if (item.status === "CANCELLED" || cancelingIds.value.includes(item.id)) {
    return;
  }

  try {
    await ElMessageBox.confirm(
      "取消后会释放该路线座位，订单状态会变为已取消。确定继续吗？",
      "取消订单",
      { confirmButtonText: "确定取消", cancelButtonText: "再想想", type: "warning" }
    );
  } catch {
    return;
  }

  cancelingIds.value.push(item.id);
  try {
    await cancelMyBooking(item.id);
    ElMessage.success("订单已取消，路线余位已恢复");
    await loadData();
  } finally {
    cancelingIds.value = cancelingIds.value.filter((id) => id !== item.id);
  }
}

function statusInfo(status: Booking["status"]) {
  return statusMap[status];
}

function formatDate(value: string) {
  return value ? value.replace("T", " ").slice(0, 16) : "-";
}

loadData();
</script>

<template>
  <AdminLayout>
    <section class="traveler-hero" v-if="heroRoute">
      <img :src="heroRoute.destination.coverImage" alt="travel-cover" />
      <div class="traveler-hero-content">
        <p class="eyebrow">旅行门户</p>
        <h1>{{ heroRoute.destination.name }}</h1>
        <p>{{ heroRoute.destination.description }}</p>
        <div class="hero-actions">
          <el-button type="primary" size="large" :icon="Tickets" @click="openBooking(heroRoute)">立即预订</el-button>
          <el-button size="large" :icon="Star" @click="toggleFavorite(heroRoute)">收藏路线</el-button>
        </div>
      </div>
      <div class="hero-float-card">
        <span>{{ heroRoute.title }}</span>
        <strong>¥{{ Number(heroRoute.price).toLocaleString() }}</strong>
        <small>{{ heroRoute.durationDays }} 天 / {{ heroRoute.departureCity }} 出发 / 剩余 {{ heroRoute.availableSeats }} 位</small>
      </div>
    </section>

    <section class="traveler-globe-shell">
      <article class="panel globe-panel pink-globe-panel">
        <div class="panel-title"><h2>3D 航线星图</h2><span>游客也能用沉浸式视角浏览路线</span></div>
        <SharedGlobeScene :orbit-speed="1.4" :auto-rotate="true" accent="#ff8eb8" glow="#ffe2ee" />
      </article>
      <article class="panel traveler-globe-side">
        <div class="panel-title"><h2>路线对比中心</h2><span>加入后立即列出价格、天数、余位和出发城市</span></div>
        <div class="compare-count-badge">当前已选择 {{ compareRoutes.length }} / 3 条路线</div>
        <div v-if="compareRoutes.length" class="compare-cards">
          <div v-for="item in compareRoutes" :key="item.id" class="compare-card">
            <img :src="item.destination.coverImage" alt="compare-cover" />
            <div>
              <strong>{{ item.title }}</strong>
              <span>{{ item.destination.name }} / {{ item.departureCity }} 出发</span>
              <span>¥{{ Number(item.price).toLocaleString() }} / {{ item.durationDays }} 天 / 剩余 {{ item.availableSeats }} 位</span>
            </div>
            <el-button size="small" @click="toggleCompare(item)">移出</el-button>
          </div>
        </div>
        <el-empty v-else description="还没有加入对比的路线" />
      </article>
    </section>

    <section class="traveler-search">
      <el-input v-model="keyword" :prefix-icon="Search" placeholder="搜索目的地、路线、省份或亮点" clearable />
      <el-select v-model="themeFilter" placeholder="旅行主题" clearable>
        <el-option v-for="item in themes" :key="item" :label="item" :value="item" />
      </el-select>
      <div class="price-filter"><span>最高预算：¥{{ maxPrice }}</span><el-slider v-model="maxPrice" :min="1000" :max="10000" :step="500" /></div>
    </section>

    <section class="traveler-bookings-panel panel">
      <div class="panel-title">
        <div><h2>我的订单</h2><span>只展示当前登录账号提交的预订记录</span></div>
        <div class="booking-mini-stats">
          <span>全部 {{ bookingSummary.total }}</span>
          <span>有效 {{ bookingSummary.active }}</span>
          <span>金额 ¥{{ bookingSummary.revenue.toLocaleString() }}</span>
        </div>
      </div>
      <div v-if="myBookings.length" class="traveler-booking-grid">
        <article v-for="item in myBookings" :key="item.id" class="traveler-booking-card">
          <div>
            <strong>#{{ item.id }} {{ item.route.title }}</strong>
            <span>{{ item.travelerName }} / {{ item.phone }} / {{ item.travelers }} 人</span>
            <small>{{ item.route.departureCity }} 出发 / {{ item.route.departureDate }} / {{ formatDate(item.createdAt) }}</small>
          </div>
          <div class="booking-card-side">
            <el-tag :type="statusInfo(item.status).type">{{ statusInfo(item.status).label }}</el-tag>
            <strong>¥{{ Number(item.totalAmount).toLocaleString() }}</strong>
            <el-button
              v-if="item.status !== 'CANCELLED'"
              link
              type="danger"
              :loading="cancelingIds.includes(item.id)"
              @click="cancelBooking(item)"
            >
              取消订单
            </el-button>
          </div>
        </article>
      </div>
      <el-empty v-else description="你还没有提交过预订" />
    </section>

    <section class="traveler-info-grid">
      <article v-for="item in travelInfoCards" :key="item.title" class="traveler-info-card">
        <el-tag>{{ item.tag }}</el-tag>
        <strong>{{ item.title }}</strong>
        <span>{{ item.text }}</span>
      </article>
    </section>

    <section class="route-card-grid" v-loading="loading">
      <article v-for="item in recommendedRoutes" :key="item.id" class="travel-card" @mouseenter="showPreview(item, $event)" @mousemove="movePreview($event)" @mouseleave="hidePreview">
        <div class="travel-cover"><img :src="item.destination.coverImage" alt="route-cover" /><el-tag>{{ item.destination.theme }}</el-tag></div>
        <div class="travel-body">
          <div class="card-heading">
            <div><strong>{{ item.title }}</strong><span class="city-hover-trigger">{{ item.destination.province }} / {{ item.departureCity }} 出发</span></div>
            <el-button circle :type="favoriteIds.includes(item.id || 0) ? 'warning' : 'default'" :icon="Star" @click="toggleFavorite(item)" />
          </div>
          <p>{{ item.highlight }}</p>
          <div class="travel-facts">
            <span><el-icon><Calendar /></el-icon>{{ item.departureDate }}</span>
            <span><el-icon><SuitcaseLine /></el-icon>{{ item.durationDays }} 天</span>
            <span><el-icon><CollectionTag /></el-icon>剩余 {{ item.availableSeats }} 位</span>
          </div>
          <div class="travel-footer">
            <strong>¥{{ Number(item.price).toLocaleString() }}</strong>
            <div>
              <el-button plain @click="toggleCompare(item)">{{ compareIds.includes(item.id || 0) ? "取消对比" : "加入对比" }}</el-button>
              <el-button type="primary" @click="openBooking(item)">预订</el-button>
            </div>
          </div>
        </div>
      </article>
    </section>

    <div v-if="compareBarVisible" class="compare-floating-bar">
      <span>已加入对比 {{ compareIds.length }} / 3 条路线</span>
      <el-button size="small" @click="compareIds = []; compareBarVisible = false">清空对比</el-button>
    </div>

    <div v-if="previewVisible && hoveredRoute" class="destination-hover-preview" :style="{ left: `${previewX}px`, top: `${previewY}px` }">
      <div class="hover-preview-header"><div><strong>{{ hoveredRoute.destination.name }}</strong><span>{{ hoveredRoute.destination.theme }} / {{ hoveredRoute.departureCity }}</span></div><el-tag>{{ hoveredRoute.destination.rating }} 分</el-tag></div>
      <DestinationHoverScene :scene-key="hoveredRoute.destination.id || hoveredRoute.id || 0" />
      <div class="hover-preview-footer">
        <div class="preview-metric"><span>热度</span><strong>{{ hoveredRoute.destination.popularityScore }}</strong></div>
        <div class="preview-metric"><span>价格</span><strong>¥{{ Number(hoveredRoute.price).toLocaleString() }}</strong></div>
        <div class="preview-metric"><span>天数</span><strong>{{ hoveredRoute.durationDays }} 天</strong></div>
      </div>
    </div>

    <el-dialog v-model="bookingVisible" title="提交旅行预订" width="560px">
      <div v-if="selectedRoute" class="booking-summary"><strong>{{ selectedRoute.title }}</strong><span>{{ selectedRoute.departureCity }} 出发 / {{ selectedRoute.departureDate }} / ¥{{ Number(selectedRoute.price).toLocaleString() }} / 人</span></div>
      <el-form label-position="top">
        <el-form-item label="游客姓名"><el-input v-model="bookingForm.travelerName" /></el-form-item>
        <el-form-item label="手机号"><el-input v-model="bookingForm.phone" /></el-form-item>
        <el-form-item label="出行人数"><el-input-number v-model="bookingForm.travelers" :min="1" :max="selectedRoute?.availableSeats || 1" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="bookingVisible = false">取消</el-button><el-button type="primary" @click="submitBooking">确认预订</el-button></template>
    </el-dialog>
  </AdminLayout>
</template>
