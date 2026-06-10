<script setup lang="ts">
import { computed, reactive, ref } from "vue";
import { ElMessage, ElMessageBox } from "element-plus";
import { Plus, Search, Refresh, CopyDocument, View } from "@element-plus/icons-vue";
import AdminLayout from "../components/AdminLayout.vue";
import { createRoute, deleteRoute, getAdminDestinations, getAdminRoutes, updateRoute, type Destination, type TravelRoute } from "../api/http";
import { displayDestination, displayRoute } from "../utils/display";

const loading = ref(false);
const dialogVisible = ref(false);
const detailVisible = ref(false);
const editingId = ref<number | null>(null);
const routes = ref<TravelRoute[]>([]);
const destinations = ref<Destination[]>([]);
const selectedRoute = ref<TravelRoute | null>(null);
const keyword = ref("");
const themeFilter = ref("");
const cityFilter = ref("");

const form = reactive({
  destinationId: 0,
  title: "",
  durationDays: 3,
  price: 1999,
  availableSeats: 20,
  departureCity: "上海",
  departureDate: "2026-07-01",
  guideName: "",
  highlight: ""
});

const displayDestinations = computed(() => destinations.value.map(displayDestination));
const displayRoutes = computed(() => routes.value.map(displayRoute));
const themes = computed(() => Array.from(new Set(displayRoutes.value.map((item) => item.destination.theme))));
const cities = computed(() => Array.from(new Set(displayRoutes.value.map((item) => item.departureCity))));
const filteredRoutes = computed(() => {
  const word = keyword.value.trim().toLowerCase();
  return displayRoutes.value.filter((item) => {
    const matchesWord = !word || [item.title, item.departureCity, item.guideName, item.destination?.name, item.highlight]
      .filter(Boolean)
      .some((text) => String(text).toLowerCase().includes(word));
    const matchesTheme = !themeFilter.value || item.destination?.theme === themeFilter.value;
    const matchesCity = !cityFilter.value || item.departureCity === cityFilter.value;
    return matchesWord && matchesTheme && matchesCity;
  });
});

const routeSummary = computed(() => ({
  total: displayRoutes.value.length,
  lowInventory: displayRoutes.value.filter((item) => item.availableSeats <= 20).length,
  avgPrice: displayRoutes.value.length ? Math.round(displayRoutes.value.reduce((sum, item) => sum + Number(item.price), 0) / displayRoutes.value.length) : 0,
  upcoming: displayRoutes.value.filter((item) => item.departureDate >= "2026-07-01").length
}));

async function loadData() {
  loading.value = true;
  try {
    const [routeData, destinationData] = await Promise.all([getAdminRoutes(), getAdminDestinations()]);
    routes.value = routeData;
    destinations.value = destinationData;
    if (!form.destinationId) form.destinationId = displayDestination(destinationData[0])?.id || 0;
  } finally {
    loading.value = false;
  }
}

function resetForm() {
  editingId.value = null;
  form.destinationId = displayDestinations.value[0]?.id || 0;
  form.title = "";
  form.durationDays = 3;
  form.price = 1999;
  form.availableSeats = 20;
  form.departureCity = "上海";
  form.departureDate = "2026-07-01";
  form.guideName = "";
  form.highlight = "";
}

function openCreate() {
  resetForm();
  dialogVisible.value = true;
}

function openEdit(row: TravelRoute) {
  editingId.value = row.id || null;
  form.destinationId = row.destination?.id || 0;
  form.title = row.title;
  form.durationDays = row.durationDays;
  form.price = Number(row.price);
  form.availableSeats = row.availableSeats;
  form.departureCity = row.departureCity;
  form.departureDate = row.departureDate;
  form.guideName = row.guideName;
  form.highlight = row.highlight;
  dialogVisible.value = true;
}

function openDetail(row: TravelRoute) {
  selectedRoute.value = row;
  detailVisible.value = true;
}

function duplicateRoute(row: TravelRoute) {
  editingId.value = null;
  form.destinationId = row.destination?.id || 0;
  form.title = `${row.title} - 复制版`;
  form.durationDays = row.durationDays;
  form.price = Number(row.price);
  form.availableSeats = row.availableSeats;
  form.departureCity = row.departureCity;
  form.departureDate = row.departureDate;
  form.guideName = row.guideName;
  form.highlight = row.highlight;
  dialogVisible.value = true;
}

function buildPayload(): TravelRoute {
  const destination = displayDestinations.value.find((item) => item.id === form.destinationId);
  if (!destination) throw new Error("missing destination");
  return {
    destination,
    title: form.title,
    durationDays: Number(form.durationDays),
    price: Number(form.price),
    availableSeats: Number(form.availableSeats),
    departureCity: form.departureCity,
    departureDate: form.departureDate,
    guideName: form.guideName,
    highlight: form.highlight
  };
}

async function submit() {
  if (!form.title || !form.destinationId || !form.guideName || !form.highlight) {
    ElMessage.warning("请补全路线名称、目的地、导游和亮点信息");
    return;
  }
  const payload = buildPayload();
  if (editingId.value) {
    await updateRoute(editingId.value, payload);
    ElMessage.success("路线已更新");
  } else {
    await createRoute(payload);
    ElMessage.success("路线已新增");
  }
  dialogVisible.value = false;
  await loadData();
}

async function remove(row: TravelRoute) {
  if (!row.id) return;
  await ElMessageBox.confirm(`确认删除路线「${row.title}」吗？`, "删除确认", { type: "warning" });
  await deleteRoute(row.id);
  ElMessage.success("路线已删除");
  await loadData();
}

loadData();
</script>

<template>
  <AdminLayout>
    <header class="page-header">
      <div>
        <p class="eyebrow">路线运营</p>
        <h1>路线运营管理</h1>
        <span>支持路线筛选、详情查看、新增、复制、编辑、删除，覆盖完整 CRUD 展示。</span>
      </div>
      <el-button type="primary" :icon="Plus" @click="openCreate">新增路线</el-button>
    </header>

    <section class="metric-grid compact-metrics">
      <article class="metric-card"><span>路线总数</span><strong>{{ routeSummary.total }}</strong><small>后台在售路线</small></article>
      <article class="metric-card"><span>库存预警</span><strong>{{ routeSummary.lowInventory }}</strong><small>余位 20 以下需关注</small></article>
      <article class="metric-card"><span>平均客单价</span><strong>¥{{ routeSummary.avgPrice }}</strong><small>用于定价参考</small></article>
      <article class="metric-card highlight"><span>即将出发</span><strong>{{ routeSummary.upcoming }}</strong><small>适合重点营销</small></article>
    </section>

    <section class="filter-bar">
      <el-input v-model="keyword" :prefix-icon="Search" placeholder="搜索路线、目的地、导游、亮点" clearable />
      <el-select v-model="themeFilter" placeholder="主题筛选" clearable><el-option v-for="item in themes" :key="item" :label="item" :value="item" /></el-select>
      <el-select v-model="cityFilter" placeholder="出发城市" clearable><el-option v-for="item in cities" :key="item" :label="item" :value="item" /></el-select>
      <el-button :icon="Refresh" @click="loadData">刷新</el-button>
    </section>

    <section class="panel">
      <el-table :data="filteredRoutes" v-loading="loading" height="620">
        <el-table-column label="路线信息" min-width="260"><template #default="scope"><div class="table-title"><strong>{{ scope.row.title }}</strong><span>{{ scope.row.highlight }}</span></div></template></el-table-column>
        <el-table-column label="目的地" width="160"><template #default="scope">{{ scope.row.destination.name }}</template></el-table-column>
        <el-table-column prop="departureCity" label="出发城市" width="110" />
        <el-table-column prop="departureDate" label="出发日期" width="125" />
        <el-table-column prop="durationDays" label="天数" width="80" />
        <el-table-column label="价格" width="110"><template #default="scope">¥{{ Number(scope.row.price).toLocaleString() }}</template></el-table-column>
        <el-table-column prop="availableSeats" label="余位" width="90" />
        <el-table-column prop="guideName" label="导游" width="120" show-overflow-tooltip />
        <el-table-column label="操作" width="280">
          <template #default="scope">
            <el-button link type="primary" :icon="View" @click="openDetail(scope.row)">详情</el-button>
            <el-button link type="warning" @click="openEdit(scope.row)">编辑</el-button>
            <el-button link type="success" :icon="CopyDocument" @click="duplicateRoute(scope.row)">复制</el-button>
            <el-button link type="danger" @click="remove(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </section>

    <el-dialog v-model="dialogVisible" :title="editingId ? '编辑路线' : '新增路线'" width="780px">
      <el-form label-position="top" class="form-grid">
        <el-form-item label="路线名称"><el-input v-model="form.title" /></el-form-item>
        <el-form-item label="目的地"><el-select v-model="form.destinationId"><el-option v-for="item in displayDestinations" :key="item.id" :label="item.name" :value="item.id" /></el-select></el-form-item>
        <el-form-item label="出发城市"><el-input v-model="form.departureCity" /></el-form-item>
        <el-form-item label="出发日期"><el-date-picker v-model="form.departureDate" type="date" value-format="YYYY-MM-DD" /></el-form-item>
        <el-form-item label="行程天数"><el-input-number v-model="form.durationDays" :min="1" :max="30" /></el-form-item>
        <el-form-item label="可售余位"><el-input-number v-model="form.availableSeats" :min="0" :max="200" /></el-form-item>
        <el-form-item label="单人价格"><el-input-number v-model="form.price" :min="1" :step="100" /></el-form-item>
        <el-form-item label="导游姓名"><el-input v-model="form.guideName" /></el-form-item>
        <el-form-item label="路线亮点" class="full-row"><el-input v-model="form.highlight" type="textarea" :rows="3" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="dialogVisible = false">取消</el-button><el-button type="primary" @click="submit">保存</el-button></template>
    </el-dialog>

    <el-drawer v-model="detailVisible" title="路线详情" size="480px">
      <div v-if="selectedRoute" class="detail-stack">
        <img class="detail-cover" :src="selectedRoute.destination.coverImage" alt="route-cover" />
        <div class="detail-card">
          <h3>{{ selectedRoute.title }}</h3>
          <p>{{ selectedRoute.highlight }}</p>
          <div class="detail-line"><strong>目的地：</strong>{{ selectedRoute.destination.name }} / {{ selectedRoute.destination.theme }}</div>
          <div class="detail-line"><strong>出发：</strong>{{ selectedRoute.departureCity }} / {{ selectedRoute.departureDate }}</div>
          <div class="detail-line"><strong>行程：</strong>{{ selectedRoute.durationDays }} 天</div>
          <div class="detail-line"><strong>价格：</strong>¥{{ Number(selectedRoute.price).toLocaleString() }}</div>
          <div class="detail-line"><strong>导游：</strong>{{ selectedRoute.guideName }}</div>
          <div class="detail-line"><strong>余位：</strong>{{ selectedRoute.availableSeats }} 位</div>
        </div>
      </div>
    </el-drawer>
  </AdminLayout>
</template>
