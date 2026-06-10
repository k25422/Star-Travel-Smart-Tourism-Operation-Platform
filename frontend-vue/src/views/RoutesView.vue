<script setup lang="ts">
import { computed, reactive, ref } from "vue";
import { ElMessage, ElMessageBox } from "element-plus";
import { Plus, Search, Refresh } from "@element-plus/icons-vue";
import AdminLayout from "../components/AdminLayout.vue";
import { createRoute, deleteRoute, getAdminDestinations, getAdminRoutes, updateRoute, type Destination, type TravelRoute } from "../api/http";

const loading = ref(false);
const dialogVisible = ref(false);
const editingId = ref<number | null>(null);
const routes = ref<TravelRoute[]>([]);
const destinations = ref<Destination[]>([]);
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

const themes = computed(() => Array.from(new Set(destinations.value.map((item) => item.theme))));
const cities = computed(() => Array.from(new Set(routes.value.map((item) => item.departureCity))));
const filteredRoutes = computed(() => {
  const word = keyword.value.trim().toLowerCase();
  return routes.value.filter((item) => {
    const matchesWord = !word || [item.title, item.departureCity, item.guideName, item.destination?.name, item.highlight]
      .filter(Boolean)
      .some((text) => String(text).toLowerCase().includes(word));
    const matchesTheme = !themeFilter.value || item.destination?.theme === themeFilter.value;
    const matchesCity = !cityFilter.value || item.departureCity === cityFilter.value;
    return matchesWord && matchesTheme && matchesCity;
  });
});

async function loadData() {
  loading.value = true;
  try {
    const [routeData, destinationData] = await Promise.all([getAdminRoutes(), getAdminDestinations()]);
    routes.value = routeData;
    destinations.value = destinationData;
  } finally {
    loading.value = false;
  }
}

function resetForm() {
  editingId.value = null;
  form.destinationId = destinations.value[0]?.id || 0;
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

function buildPayload(): TravelRoute {
  const destination = destinations.value.find((item) => item.id === form.destinationId);
  if (!destination) {
    throw new Error("请选择目的地");
  }
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
        <p class="eyebrow">Route Operations</p>
        <h1>路线运营</h1>
        <span>支持路线查询、库存维护、价格调整和出发计划管理。</span>
      </div>
      <el-button type="primary" :icon="Plus" @click="openCreate">新增路线</el-button>
    </header>

    <section class="filter-bar">
      <el-input v-model="keyword" :prefix-icon="Search" placeholder="搜索路线、目的地、导游、亮点" clearable />
      <el-select v-model="themeFilter" placeholder="主题筛选" clearable>
        <el-option v-for="item in themes" :key="item" :label="item" :value="item" />
      </el-select>
      <el-select v-model="cityFilter" placeholder="出发城市" clearable>
        <el-option v-for="item in cities" :key="item" :label="item" :value="item" />
      </el-select>
      <el-button :icon="Refresh" @click="loadData">刷新</el-button>
    </section>

    <section class="panel">
      <el-table :data="filteredRoutes" v-loading="loading" height="620">
        <el-table-column label="路线" min-width="260">
          <template #default="scope">
            <div class="table-title">
              <strong>{{ scope.row.title }}</strong>
              <span>{{ scope.row.highlight }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="目的地" min-width="180">
          <template #default="scope">
            <el-tag>{{ scope.row.destination.name }}</el-tag>
            <span class="muted-text"> {{ scope.row.destination.province }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="departureCity" label="出发城市" width="120" />
        <el-table-column prop="departureDate" label="出发日期" width="140" />
        <el-table-column prop="durationDays" label="天数" width="90" />
        <el-table-column label="价格" width="120">
          <template #default="scope">¥{{ Number(scope.row.price).toLocaleString() }}</template>
        </el-table-column>
        <el-table-column label="余位" width="120">
          <template #default="scope">
            <el-tag :type="scope.row.availableSeats <= 10 ? 'danger' : scope.row.availableSeats <= 20 ? 'warning' : 'success'">
              {{ scope.row.availableSeats }} 位
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="guideName" label="导游" width="110" />
        <el-table-column label="操作" width="170" fixed="right">
          <template #default="scope">
            <el-button link type="primary" @click="openEdit(scope.row)">编辑</el-button>
            <el-button link type="danger" @click="remove(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </section>

    <el-dialog v-model="dialogVisible" :title="editingId ? '编辑路线' : '新增路线'" width="760px">
      <el-form label-position="top" class="form-grid">
        <el-form-item label="路线名称">
          <el-input v-model="form.title" placeholder="例如：张家界天空森林深度游" />
        </el-form-item>
        <el-form-item label="目的地">
          <el-select v-model="form.destinationId" placeholder="请选择目的地">
            <el-option v-for="item in destinations" :key="item.id" :label="`${item.name} / ${item.theme}`" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="出发城市">
          <el-input v-model="form.departureCity" />
        </el-form-item>
        <el-form-item label="出发日期">
          <el-date-picker v-model="form.departureDate" type="date" value-format="YYYY-MM-DD" />
        </el-form-item>
        <el-form-item label="行程天数">
          <el-input-number v-model="form.durationDays" :min="1" :max="30" />
        </el-form-item>
        <el-form-item label="可售余位">
          <el-input-number v-model="form.availableSeats" :min="0" :max="999" />
        </el-form-item>
        <el-form-item label="单人价格">
          <el-input-number v-model="form.price" :min="0" :step="100" />
        </el-form-item>
        <el-form-item label="导游姓名">
          <el-input v-model="form.guideName" />
        </el-form-item>
        <el-form-item label="路线亮点" class="full-row">
          <el-input v-model="form.highlight" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submit">保存</el-button>
      </template>
    </el-dialog>
  </AdminLayout>
</template>