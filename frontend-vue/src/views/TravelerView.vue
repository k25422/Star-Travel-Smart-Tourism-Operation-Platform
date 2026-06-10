<script setup lang="ts">
import { computed, reactive, ref } from "vue";
import { ElMessage } from "element-plus";
import { Calendar, CollectionTag, Search, Star, SuitcaseLine, Tickets } from "@element-plus/icons-vue";
import AdminLayout from "../components/AdminLayout.vue";
import { createBooking, getDestinations, getRoutes, type Destination, type TravelRoute } from "../api/http";

const loading = ref(false);
const bookingVisible = ref(false);
const selectedRoute = ref<TravelRoute | null>(null);
const keyword = ref("");
const themeFilter = ref("");
const maxPrice = ref(6000);
const favoriteIds = ref<number[]>(JSON.parse(localStorage.getItem("tourism_favorites") || "[]"));
const compareIds = ref<number[]>([]);
const destinations = ref<Destination[]>([]);
const routes = ref<TravelRoute[]>([]);

// 游客提交订单需要的表单字段，字段名必须和后端 BookingRequest 对应。
const bookingForm = reactive({
  travelerName: "张同学",
  phone: "13800000000",
  travelers: 2
});

const themes = computed(() => Array.from(new Set(destinations.value.map((item) => item.theme))));
const heroRoute = computed(() => routes.value[0]);
const recommendedRoutes = computed(() => filteredRoutes.value.slice(0, 6));
const compareRoutes = computed(() => routes.value.filter((item) => item.id && compareIds.value.includes(item.id)));

const filteredRoutes = computed(() => {
  const word = keyword.value.trim().toLowerCase();
  return routes.value.filter((item) => {
    const matchesWord = !word || [item.title, item.destination?.name, item.destination?.province, item.highlight]
      .filter(Boolean)
      .some((text) => String(text).toLowerCase().includes(word));
    const matchesTheme = !themeFilter.value || item.destination?.theme === themeFilter.value;
    const matchesPrice = Number(item.price) <= maxPrice.value;
    return matchesWord && matchesTheme && matchesPrice;
  });
});

async function loadData() {
  loading.value = true;
  try {
    const [routeData, destinationData] = await Promise.all([getRoutes(), getDestinations()]);
    routes.value = routeData;
    destinations.value = destinationData;
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
    return;
  }
  if (compareIds.value.length >= 3) {
    ElMessage.warning("最多同时对比 3 条路线");
    return;
  }
  compareIds.value.push(route.id);
}

function openBooking(route: TravelRoute) {
  selectedRoute.value = route;
  bookingVisible.value = true;
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
  ElMessage.success("预订成功，管理员可在订单中心看到这条订单");
  bookingVisible.value = false;
  await loadData();
}

loadData();
</script>

<template>
  <AdminLayout>
    <section class="traveler-hero" v-if="heroRoute">
      <img :src="heroRoute.destination.coverImage" alt="旅行封面" />
      <div class="traveler-hero-content">
        <p class="eyebrow">Traveler Portal</p>
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
        <small>{{ heroRoute.durationDays }} 天 · {{ heroRoute.departureCity }} 出发 · 剩余 {{ heroRoute.availableSeats }} 位</small>
      </div>
    </section>

    <section class="traveler-search">
      <el-input v-model="keyword" :prefix-icon="Search" placeholder="搜索目的地、路线、省份或亮点" clearable />
      <el-select v-model="themeFilter" placeholder="旅行主题" clearable>
        <el-option v-for="item in themes" :key="item" :label="item" :value="item" />
      </el-select>
      <div class="price-filter">
        <span>最高预算：¥{{ maxPrice }}</span>
        <el-slider v-model="maxPrice" :min="1000" :max="10000" :step="500" />
      </div>
    </section>

    <section class="metric-grid compact-metrics">
      <article class="metric-card">
        <span>可选路线</span>
        <strong>{{ filteredRoutes.length }}</strong>
        <small>根据当前筛选实时变化</small>
      </article>
      <article class="metric-card">
        <span>我的收藏</span>
        <strong>{{ favoriteIds.length }}</strong>
        <small>保存在浏览器本地</small>
      </article>
      <article class="metric-card">
        <span>对比路线</span>
        <strong>{{ compareIds.length }}</strong>
        <small>最多同时对比 3 条</small>
      </article>
      <article class="metric-card highlight">
        <span>推荐主题</span>
        <strong>{{ themeFilter || "全部" }}</strong>
        <small>支持按主题快速筛选</small>
      </article>
    </section>

    <section class="route-card-grid" v-loading="loading">
      <article v-for="item in recommendedRoutes" :key="item.id" class="travel-card">
        <div class="travel-cover">
          <img :src="item.destination.coverImage" alt="路线封面" />
          <el-tag>{{ item.destination.theme }}</el-tag>
        </div>
        <div class="travel-body">
          <div class="card-heading">
            <div>
              <strong>{{ item.title }}</strong>
              <span>{{ item.destination.province }} · {{ item.departureCity }} 出发</span>
            </div>
            <el-button circle :type="favoriteIds.includes(item.id || 0) ? 'warning' : 'default'" :icon="Star" @click="toggleFavorite(item)" />
          </div>
          <p>{{ item.highlight }}</p>
          <div class="travel-facts">
            <span><el-icon><Calendar /></el-icon>{{ item.departureDate }}</span>
            <span><el-icon><SuitcaseLine /></el-icon>{{ item.durationDays }} 天</span>
            <span><el-icon><CollectionTag /></el-icon>余 {{ item.availableSeats }} 位</span>
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

    <section class="panel compare-panel" v-if="compareRoutes.length">
      <div class="panel-title">
        <h2>路线对比</h2>
        <span>价格、天数、余位和目的地热度快速比较</span>
      </div>
      <el-table :data="compareRoutes">
        <el-table-column prop="title" label="路线" />
        <el-table-column label="目的地"><template #default="scope">{{ scope.row.destination.name }}</template></el-table-column>
        <el-table-column prop="durationDays" label="天数" width="90" />
        <el-table-column label="价格" width="120"><template #default="scope">¥{{ Number(scope.row.price).toLocaleString() }}</template></el-table-column>
        <el-table-column prop="availableSeats" label="余位" width="90" />
        <el-table-column label="热度" width="160"><template #default="scope"><el-progress :percentage="scope.row.destination.popularityScore" /></template></el-table-column>
      </el-table>
    </section>

    <el-dialog v-model="bookingVisible" title="提交旅行预订" width="560px">
      <div v-if="selectedRoute" class="booking-summary">
        <strong>{{ selectedRoute.title }}</strong>
        <span>{{ selectedRoute.departureCity }} 出发 · {{ selectedRoute.departureDate }} · ¥{{ Number(selectedRoute.price).toLocaleString() }}/人</span>
      </div>
      <el-form label-position="top">
        <el-form-item label="游客姓名">
          <el-input v-model="bookingForm.travelerName" />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="bookingForm.phone" />
        </el-form-item>
        <el-form-item label="出行人数">
          <el-input-number v-model="bookingForm.travelers" :min="1" :max="selectedRoute?.availableSeats || 1" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="bookingVisible = false">取消</el-button>
        <el-button type="primary" @click="submitBooking">确认预订</el-button>
      </template>
    </el-dialog>
  </AdminLayout>
</template>
