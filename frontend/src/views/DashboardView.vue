<script setup lang="ts">
import { computed, nextTick, onMounted, ref } from "vue";
import { BarChart, PieChart } from "echarts/charts";
import { GridComponent, LegendComponent, TooltipComponent } from "echarts/components";
import { init, use, type ECharts } from "echarts/core";
import { CanvasRenderer } from "echarts/renderers";
import { ElMessage } from "element-plus";
import AdminLayout from "../components/AdminLayout.vue";
import SharedGlobeScene from "../components/SharedGlobeScene.vue";
import { getAdminBookings, getDestinations, getRoutes, getStats, type Booking, type DashboardStats, type Destination, type TravelRoute } from "../api/http";
import { displayDestination, displayRoute, zh } from "../utils/display";

use([BarChart, PieChart, GridComponent, LegendComponent, TooltipComponent, CanvasRenderer]);

const loading = ref(false);
const stats = ref<DashboardStats>({ destinationCount: 0, routeCount: 0, bookingCount: 0, revenue: 0 });
const destinations = ref<Destination[]>([]);
const routes = ref<TravelRoute[]>([]);
const bookings = ref<Booking[]>([]);
const revenueChartRef = ref<HTMLDivElement | null>(null);
const themeChartRef = ref<HTMLDivElement | null>(null);
let revenueChart: ECharts | null = null;
let themeChart: ECharts | null = null;

const displayDestinations = computed(() => destinations.value.map(displayDestination));
const displayRoutes = computed(() => routes.value.map(displayRoute));
const topDestinations = computed(() => [...displayDestinations.value].sort((a, b) => b.popularityScore - a.popularityScore).slice(0, 4));
const lowSeatRoutes = computed(() => [...displayRoutes.value]
  .filter((item) => item.availableSeats <= 20)
  .sort((a, b) => a.availableSeats - b.availableSeats));
const avgPrice = computed(() => displayRoutes.value.length ? Math.round(displayRoutes.value.reduce((sum, item) => sum + Number(item.price), 0) / displayRoutes.value.length) : 0);
const confirmedRate = computed(() => bookings.value.length ? Math.round((bookings.value.filter((item) => item.status === "CONFIRMED").length / bookings.value.length) * 100) : 0);

async function loadData() {
  loading.value = true;
  try {
    const [statsData, destinationsData, routesData, bookingsData] = await Promise.all([
      getStats(),
      getDestinations(),
      getRoutes(),
      getAdminBookings()
    ]);
    stats.value = statsData;
    destinations.value = destinationsData;
    routes.value = routesData;
    bookings.value = bookingsData;
    await nextTick();
    renderCharts();
  } finally {
    loading.value = false;
  }
}

function renderCharts() {
  if (revenueChartRef.value) {
    revenueChart?.dispose();
    revenueChart = init(revenueChartRef.value);
    revenueChart.setOption({
      tooltip: { trigger: "axis" },
      grid: { left: 42, right: 18, top: 30, bottom: 62 },
      xAxis: { type: "category", data: displayRoutes.value.map((item) => item.title), axisLabel: { rotate: 18 } },
      yAxis: { type: "value" },
      series: [{
        name: "路线收入预估",
        type: "bar",
        data: displayRoutes.value.map((item) => Number(item.price) * Math.max(1, 40 - item.availableSeats)),
        itemStyle: { color: "#ff8db7", borderRadius: [8, 8, 0, 0] }
      }]
    });
  }

  if (themeChartRef.value) {
    themeChart?.dispose();
    themeChart = init(themeChartRef.value);
    const grouped = displayDestinations.value.reduce((result: Record<string, number>, item) => {
      result[item.theme] = (result[item.theme] || 0) + 1;
      return result;
    }, {});
    themeChart.setOption({
      tooltip: { trigger: "item" },
      legend: { bottom: 0 },
      series: [{
        name: "目的地主题",
        type: "pie",
        radius: ["48%", "72%"],
        center: ["50%", "45%"],
        data: Object.entries(grouped).map(([name, value]) => ({ name, value })),
        color: ["#ff8db7", "#ffb7d0", "#f4a1cf", "#d992f4"]
      }]
    });
  }
}

function refreshGlobe() {
  ElMessage.success("3D 航线星球已刷新");
}

onMounted(loadData);
</script>

<template>
  <AdminLayout>
    <header class="page-header">
      <div>
        <p class="eyebrow">管理员工作台</p>
        <h1>旅游运营数据总览</h1>
        <span>集中展示路线库存、目的地热度、订单状态、收入预估和 3D 航线视图。</span>
      </div>
      <el-button type="primary" :loading="loading" @click="loadData">刷新数据</el-button>
    </header>

    <section class="metric-grid">
      <article class="metric-card"><span>目的地数量</span><strong>{{ stats.destinationCount }}</strong><small>当前系统资源池</small></article>
      <article class="metric-card"><span>路线数量</span><strong>{{ stats.routeCount }}</strong><small>均价参考 ¥{{ avgPrice }}</small></article>
      <article class="metric-card"><span>订单数量</span><strong>{{ stats.bookingCount }}</strong><small>确认率 {{ confirmedRate }}%</small></article>
      <article class="metric-card highlight"><span>累计收入</span><strong>¥{{ Number(stats.revenue || 0).toLocaleString() }}</strong><small>按有效订单统计</small></article>
    </section>

    <section class="dashboard-grid enhanced-dashboard">
      <article class="panel span-2">
        <div class="panel-title"><h2>路线收入趋势</h2><span>按路线价格和余位估算运营收入</span></div>
        <div ref="revenueChartRef" class="chart-box"></div>
      </article>

      <article class="panel globe-panel enhanced-globe pink-globe-panel">
        <div class="panel-title"><h2>3D 航线星球视图</h2><span>动态轨道、飞线光点和淡粉色视觉主题</span></div>
        <div class="globe-toolbar one-button">
          <el-button type="primary" plain @click="refreshGlobe">刷新星图</el-button>
        </div>
        <SharedGlobeScene :orbit-speed="1.3" :auto-rotate="true" accent="#ff86b4" glow="#ffd8e8" />
        <div class="globe-stats">
          <span>当前航线：{{ displayRoutes.length }}</span>
          <span>主题范围：{{ zh.allThemes }}</span>
          <span>飞线状态：动态运行</span>
        </div>
      </article>

      <article class="panel">
        <div class="panel-title"><h2>目的地主题占比</h2><span>用于分析产品组合</span></div>
        <div ref="themeChartRef" class="chart-box compact"></div>
      </article>

      <article class="panel">
        <div class="panel-title"><h2>热门目的地</h2><span>按热度排序</span></div>
        <div class="destination-row" v-for="item in topDestinations" :key="item.id">
          <img :src="item.coverImage" alt="destination-cover" />
          <div><strong>{{ item.name }}</strong><p>{{ item.province }} / {{ item.theme }} / {{ item.rating }} 分</p></div>
          <el-progress :percentage="item.popularityScore" :stroke-width="8" />
        </div>
      </article>

      <article class="panel">
        <div class="panel-title"><h2>库存预警</h2><span>低于 20 个余位优先关注，共 {{ lowSeatRoutes.length }} 条</span></div>
        <div class="warning-list">
          <div v-for="item in lowSeatRoutes" :key="item.id" class="warning-item">
            <div><strong>{{ item.title }}</strong><span>{{ item.departureCity }} / {{ item.departureDate }}</span></div>
            <el-tag :type="item.availableSeats <= 10 ? 'danger' : 'warning'">{{ item.availableSeats }} 位</el-tag>
          </div>
        </div>
      </article>
    </section>
  </AdminLayout>
</template>
