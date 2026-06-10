<script setup lang="ts">
import { computed, nextTick, onBeforeUnmount, onMounted, ref } from "vue";
import * as echarts from "echarts";
import * as THREE from "three";
import { ElMessage } from "element-plus";
import AdminLayout from "../components/AdminLayout.vue";
import { getAdminBookings, getDestinations, getRoutes, getStats, type Booking, type DashboardStats, type Destination, type TravelRoute } from "../api/http";

const stats = ref<DashboardStats>({ destinationCount: 0, routeCount: 0, bookingCount: 0, revenue: 0 });
const destinations = ref<Destination[]>([]);
const routes = ref<TravelRoute[]>([]);
const bookings = ref<Booking[]>([]);
const loading = ref(false);
const autoRotate = ref(true);
const routeSpeed = ref(1.2);
const selectedTheme = ref("全部主题");

const revenueChartRef = ref<HTMLDivElement | null>(null);
const themeChartRef = ref<HTMLDivElement | null>(null);
const globeRef = ref<HTMLDivElement | null>(null);
let revenueChart: echarts.ECharts | null = null;
let themeChart: echarts.ECharts | null = null;
let globeCleanup: (() => void) | null = null;

const lowSeatRoutes = computed(() => [...routes.value]
  .filter((item) => item.availableSeats <= 20)
  .sort((a, b) => a.availableSeats - b.availableSeats));
const avgPrice = computed(() => {
  if (!routes.value.length) return 0;
  return Math.round(routes.value.reduce((sum, item) => sum + Number(item.price), 0) / routes.value.length);
});
const confirmedRate = computed(() => {
  if (!bookings.value.length) return 0;
  const confirmed = bookings.value.filter((item) => item.status === "CONFIRMED").length;
  return Math.round((confirmed / bookings.value.length) * 100);
});
const topDestinations = computed(() => [...destinations.value].sort((a, b) => b.popularityScore - a.popularityScore).slice(0, 4));
const themes = computed(() => ["全部主题", ...Array.from(new Set(destinations.value.map((item) => item.theme)))]);
const globeRoutes = computed(() => {
  if (selectedTheme.value === "全部主题") return routes.value;
  return routes.value.filter((item) => item.destination.theme === selectedTheme.value);
});

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
    renderGlobe();
  } finally {
    loading.value = false;
  }
}

function renderCharts() {
  if (revenueChartRef.value) {
    revenueChart?.dispose();
    revenueChart = echarts.init(revenueChartRef.value);
    revenueChart.setOption({
      tooltip: { trigger: "axis" },
      grid: { left: 44, right: 18, top: 28, bottom: 52 },
      xAxis: { type: "category", data: routes.value.map((item) => item.title), axisLabel: { rotate: 18 } },
      yAxis: { type: "value" },
      series: [
        {
          name: "路线收入预估",
          type: "bar",
          data: routes.value.map((item) => Number(item.price) * Math.max(1, 40 - item.availableSeats)),
          itemStyle: { color: "#0f7a68", borderRadius: [6, 6, 0, 0] }
        },
        {
          name: "余位",
          type: "line",
          yAxisIndex: 0,
          data: routes.value.map((item) => item.availableSeats * 120),
          smooth: true,
          symbolSize: 8,
          lineStyle: { color: "#dc6f59", width: 3 }
        }
      ]
    });
  }

  if (themeChartRef.value) {
    themeChart?.dispose();
    themeChart = echarts.init(themeChartRef.value);
    const grouped = destinations.value.reduce<Record<string, number>>((result, item) => {
      result[item.theme] = (result[item.theme] || 0) + 1;
      return result;
    }, {});
    themeChart.setOption({
      tooltip: { trigger: "item" },
      legend: { bottom: 0 },
      series: [
        {
          name: "目的地主题",
          type: "pie",
          radius: ["48%", "72%"],
          center: ["50%", "45%"],
          data: Object.entries(grouped).map(([name, value]) => ({ name, value })),
          color: ["#0f7a68", "#e2a93b", "#df6f57", "#4c6fff", "#6a9f58"]
        }
      ]
    });
  }
}

function makeArc(start: any, end: any, height: number) {
  // 二次贝塞尔曲线：start 和 end 是起点终点，mid 决定弧线抬高程度。
  const mid = start.clone().add(end).multiplyScalar(0.5).normalize().multiplyScalar(height);
  return new THREE.QuadraticBezierCurve3(start, mid, end);
}

function renderGlobe() {
  if (!globeRef.value) return;
  globeCleanup?.();

  const container = globeRef.value;
  const scene = new THREE.Scene();
  const camera = new THREE.PerspectiveCamera(45, container.clientWidth / container.clientHeight, 0.1, 1000);
  camera.position.set(0, 0, 7.4);

  const renderer = new THREE.WebGLRenderer({ antialias: true, alpha: true });
  renderer.setPixelRatio(Math.min(window.devicePixelRatio, 2));
  renderer.setSize(container.clientWidth, container.clientHeight);
  container.innerHTML = "";
  container.appendChild(renderer.domElement);

  const group = new THREE.Group();
  scene.add(group);

  const globe = new THREE.Mesh(
    new THREE.SphereGeometry(2.05, 72, 72),
    new THREE.MeshStandardMaterial({ color: "#0f7a68", roughness: 0.48, metalness: 0.12 })
  );
  group.add(globe);

  const wire = new THREE.Mesh(
    new THREE.SphereGeometry(2.08, 40, 40),
    new THREE.MeshBasicMaterial({ color: "#ffffff", wireframe: true, transparent: true, opacity: 0.14 })
  );
  group.add(wire);

  const halo = new THREE.Mesh(
    new THREE.SphereGeometry(2.35, 64, 64),
    new THREE.MeshBasicMaterial({ color: "#9fe8d8", transparent: true, opacity: 0.08 })
  );
  group.add(halo);

  // 星点背景：让 3D 区域更有空间感。
  const starGeometry = new THREE.BufferGeometry();
  const starPositions: number[] = [];
  for (let i = 0; i < 260; i += 1) {
    starPositions.push((Math.random() - 0.5) * 14, (Math.random() - 0.5) * 9, (Math.random() - 0.5) * 8);
  }
  starGeometry.setAttribute("position", new THREE.Float32BufferAttribute(starPositions, 3));
  const stars = new THREE.Points(starGeometry, new THREE.PointsMaterial({ color: "#ffffff", size: 0.025, transparent: true, opacity: 0.65 }));
  scene.add(stars);

  const movingDots: Array<{ mesh: any; curve: any; offset: number }> = [];
  globeRoutes.value.slice(0, 10).forEach((item, index) => {
    const angle = (index / Math.max(globeRoutes.value.length, 1)) * Math.PI * 2;
    const radius = 2.22;
    const start = new THREE.Vector3(Math.cos(angle) * radius, Math.sin(angle * 0.7) * 1.25, Math.sin(angle) * radius);
    const end = new THREE.Vector3(Math.cos(angle + 1.35) * radius, Math.sin(angle + 1.2) * 1.25, Math.sin(angle + 1.35) * radius);
    const curve = makeArc(start, end, 3.1 + item.destination.popularityScore / 80);
    const geometry = new THREE.BufferGeometry().setFromPoints(curve.getPoints(48));
    const color = index % 3 === 0 ? "#f0b949" : index % 3 === 1 ? "#ffffff" : "#ff8f70";
    const line = new THREE.Line(geometry, new THREE.LineBasicMaterial({ color, transparent: true, opacity: 0.85 }));
    group.add(line);

    const dot = new THREE.Mesh(
      new THREE.SphereGeometry(0.045, 16, 16),
      new THREE.MeshBasicMaterial({ color })
    );
    group.add(dot);
    movingDots.push({ mesh: dot, curve, offset: index / 10 });
  });

  scene.add(new THREE.AmbientLight(0xffffff, 1.6));
  const light = new THREE.DirectionalLight(0xffffff, 2.4);
  light.position.set(4, 5, 6);
  scene.add(light);

  let frameId = 0;
  let tick = 0;
  const animate = () => {
    tick += 0.006 * routeSpeed.value;
    if (autoRotate.value) {
      group.rotation.y += 0.0035;
      stars.rotation.y -= 0.0008;
    }
    movingDots.forEach((item) => {
      const point = item.curve.getPoint((tick + item.offset) % 1);
      item.mesh.position.copy(point);
    });
    renderer.render(scene, camera);
    frameId = window.requestAnimationFrame(animate);
  };
  animate();

  const resize = () => {
    if (!container.clientWidth || !container.clientHeight) return;
    camera.aspect = container.clientWidth / container.clientHeight;
    camera.updateProjectionMatrix();
    renderer.setSize(container.clientWidth, container.clientHeight);
  };
  window.addEventListener("resize", resize);

  globeCleanup = () => {
    window.removeEventListener("resize", resize);
    window.cancelAnimationFrame(frameId);
    renderer.dispose();
    container.innerHTML = "";
  };
}

function refreshGlobe() {
  renderGlobe();
  ElMessage.success("3D 航线视图已刷新");
}

onMounted(loadData);
onBeforeUnmount(() => {
  revenueChart?.dispose();
  themeChart?.dispose();
  globeCleanup?.();
});
</script>

<template>
  <AdminLayout>
    <header class="page-header">
      <div>
        <p class="eyebrow">管理员工作台</p>
        <h1>旅游运营数据总览</h1>
        <span>路线库存、目的地热度、订单状态、收入预估和动态航线集中展示。</span>
      </div>
      <el-button type="primary" :loading="loading" @click="loadData">刷新数据</el-button>
    </header>

    <section class="metric-grid">
      <article class="metric-card">
        <span>目的地数量</span>
        <strong>{{ stats.destinationCount }}</strong>
        <small>覆盖 {{ destinations.length }} 个运营资源</small>
      </article>
      <article class="metric-card">
        <span>路线数量</span>
        <strong>{{ stats.routeCount }}</strong>
        <small>均价 ¥{{ avgPrice }}</small>
      </article>
      <article class="metric-card">
        <span>订单数量</span>
        <strong>{{ stats.bookingCount }}</strong>
        <small>确认率 {{ confirmedRate }}%</small>
      </article>
      <article class="metric-card highlight">
        <span>累计收入</span>
        <strong>¥{{ Number(stats.revenue || 0).toLocaleString() }}</strong>
        <small>来自已创建订单</small>
      </article>
    </section>

    <section class="dashboard-grid enhanced-dashboard">
      <article class="panel span-2">
        <div class="panel-title">
          <h2>路线收入与库存趋势</h2>
          <span>ECharts 混合图：柱状图看收入，折线看库存压力</span>
        </div>
        <div ref="revenueChartRef" class="chart-box"></div>
      </article>

      <article class="panel globe-panel enhanced-globe">
        <div class="panel-title">
          <h2>3D 航线运营视图</h2>
          <span>可筛选主题、控制旋转和飞线速度</span>
        </div>
        <div class="globe-toolbar">
          <el-select v-model="selectedTheme" size="small" @change="refreshGlobe">
            <el-option v-for="item in themes" :key="item" :label="item" :value="item" />
          </el-select>
          <el-switch v-model="autoRotate" active-text="旋转" inactive-text="暂停" />
          <div class="speed-control">
            <span>速度</span>
            <el-slider v-model="routeSpeed" :min="0.5" :max="2.5" :step="0.1" />
          </div>
        </div>
        <div ref="globeRef" class="globe-box"></div>
        <div class="globe-stats">
          <span>当前航线：{{ globeRoutes.length }}</span>
          <span>主题：{{ selectedTheme }}</span>
          <span>飞线：动态光点</span>
        </div>
      </article>

      <article class="panel">
        <div class="panel-title">
          <h2>目的地主题占比</h2>
          <span>用于分析产品组合</span>
        </div>
        <div ref="themeChartRef" class="chart-box compact"></div>
      </article>

      <article class="panel">
        <div class="panel-title">
          <h2>热门目的地</h2>
          <span>按热度排序</span>
        </div>
        <div class="destination-row" v-for="item in topDestinations" :key="item.id">
          <img :src="item.coverImage" alt="目的地封面" />
          <div>
            <strong>{{ item.name }}</strong>
            <p>{{ item.province }} · {{ item.theme }} · {{ item.rating }} 分</p>
          </div>
          <el-progress :percentage="item.popularityScore" :stroke-width="8" />
        </div>
      </article>

      <article class="panel">
        <div class="panel-title">
          <h2>库存预警</h2>
          <span>低于 20 个余位优先关注，共 {{ lowSeatRoutes.length }} 条</span>
        </div>
        <div class="warning-list">
          <div v-for="item in lowSeatRoutes" :key="item.id" class="warning-item">
            <div>
              <strong>{{ item.title }}</strong>
              <span>{{ item.departureCity }} · {{ item.departureDate }}</span>
            </div>
            <el-tag :type="item.availableSeats <= 10 ? 'danger' : 'warning'">{{ item.availableSeats }} 位</el-tag>
          </div>
        </div>
      </article>
    </section>
  </AdminLayout>
</template>
