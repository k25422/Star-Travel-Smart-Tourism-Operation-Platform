<script setup lang="ts">
import { computed, reactive, ref } from "vue";
import { ElMessage, ElMessageBox } from "element-plus";
import { Plus, Search, Refresh, View } from "@element-plus/icons-vue";
import AdminLayout from "../components/AdminLayout.vue";
import { createDestination, deleteDestination, getAdminDestinations, updateDestination, type Destination } from "../api/http";
import { displayDestination, zh } from "../utils/display";

const loading = ref(false);
const dialogVisible = ref(false);
const detailVisible = ref(false);
const editingId = ref<number | null>(null);
const destinations = ref<Destination[]>([]);
const keyword = ref("");
const themeFilter = ref("");
const selectedDestination = ref<Destination | null>(null);

const form = reactive<Destination>({
  name: "",
  province: "",
  theme: zh.mountain,
  description: "",
  coverImage: "https://images.unsplash.com/photo-1500530855697-b586d89ba3ee?auto=format&fit=crop&w=1200&q=80",
  rating: 4.8,
  popularityScore: 90
});

const displayRows = computed(() => destinations.value.map(displayDestination));
const themes = computed(() => Array.from(new Set(displayRows.value.map((item) => item.theme))));
const filteredDestinations = computed(() => {
  const word = keyword.value.trim().toLowerCase();
  return displayRows.value.filter((item) => {
    const matchesWord = !word || [item.name, item.province, item.theme, item.description]
      .some((text) => String(text).toLowerCase().includes(word));
    const matchesTheme = !themeFilter.value || item.theme === themeFilter.value;
    return matchesWord && matchesTheme;
  });
});

const destinationSummary = computed(() => ({
  total: displayRows.value.length,
  hot: displayRows.value.filter((item) => item.popularityScore >= 90).length,
  avgRating: displayRows.value.length ? (displayRows.value.reduce((sum, item) => sum + Number(item.rating), 0) / displayRows.value.length).toFixed(1) : "0.0",
  themeCount: themes.value.length
}));

async function loadData() {
  loading.value = true;
  try {
    destinations.value = await getAdminDestinations();
  } finally {
    loading.value = false;
  }
}

function openCreate() {
  editingId.value = null;
  form.name = "";
  form.province = "";
  form.theme = zh.mountain;
  form.description = "";
  form.coverImage = "https://images.unsplash.com/photo-1500530855697-b586d89ba3ee?auto=format&fit=crop&w=1200&q=80";
  form.rating = 4.8;
  form.popularityScore = 90;
  dialogVisible.value = true;
}

function openEdit(row: Destination) {
  editingId.value = row.id || null;
  form.name = row.name;
  form.province = row.province;
  form.theme = row.theme;
  form.description = row.description;
  form.coverImage = row.coverImage;
  form.rating = row.rating;
  form.popularityScore = row.popularityScore;
  dialogVisible.value = true;
}

function openDetail(row: Destination) {
  selectedDestination.value = row;
  detailVisible.value = true;
}

async function submit() {
  if (!form.name || !form.province || !form.theme || !form.description || !form.coverImage) {
    ElMessage.warning("请补全目的地名称、省份、主题、介绍和封面图");
    return;
  }
  const payload: Destination = { ...form, rating: Number(form.rating), popularityScore: Number(form.popularityScore) };
  if (editingId.value) {
    await updateDestination(editingId.value, payload);
    ElMessage.success("目的地已更新");
  } else {
    await createDestination(payload);
    ElMessage.success("目的地已新增");
  }
  dialogVisible.value = false;
  await loadData();
}

async function remove(row: Destination) {
  if (!row.id) return;
  await ElMessageBox.confirm(`确认删除目的地「${row.name}」吗？如果该目的地已被路线引用，后端会拒绝删除。`, "删除确认", { type: "warning" });
  await deleteDestination(row.id);
  ElMessage.success("目的地已删除");
  await loadData();
}

loadData();
</script>

<template>
  <AdminLayout>
    <header class="page-header">
      <div><p class="eyebrow">目的地管理</p><h1>目的地资源中心</h1><span>维护目的地基础资料、主题、评分、热度和封面图，支撑路线设计与营销展示。</span></div>
      <el-button type="primary" :icon="Plus" @click="openCreate">新增目的地</el-button>
    </header>

    <section class="metric-grid compact-metrics">
      <article class="metric-card"><span>目的地总数</span><strong>{{ destinationSummary.total }}</strong><small>当前系统资源池</small></article>
      <article class="metric-card"><span>热门目的地</span><strong>{{ destinationSummary.hot }}</strong><small>热度 90 分以上</small></article>
      <article class="metric-card"><span>平均评分</span><strong>{{ destinationSummary.avgRating }}</strong><small>用户感知质量</small></article>
      <article class="metric-card highlight"><span>主题数量</span><strong>{{ destinationSummary.themeCount }}</strong><small>便于产品组合规划</small></article>
    </section>

    <section class="filter-bar">
      <el-input v-model="keyword" :prefix-icon="Search" placeholder="搜索名称、省份、主题、介绍" clearable />
      <el-select v-model="themeFilter" placeholder="主题筛选" clearable><el-option v-for="item in themes" :key="item" :label="item" :value="item" /></el-select>
      <el-button :icon="Refresh" @click="loadData">刷新</el-button>
    </section>

    <section class="destination-grid" v-loading="loading">
      <article v-for="item in filteredDestinations" :key="item.id" class="destination-card">
        <img :src="item.coverImage" alt="destination-cover" />
        <div class="destination-card-body">
          <div class="card-heading"><div><strong>{{ item.name }}</strong><span>{{ item.province }} / {{ item.theme }}</span></div><el-tag>{{ item.rating }} 分</el-tag></div>
          <p>{{ item.description }}</p>
          <el-progress :percentage="item.popularityScore" :stroke-width="8" />
          <div class="card-actions">
            <el-button type="primary" plain :icon="View" @click="openDetail(item)">详情</el-button>
            <el-button type="warning" plain @click="openEdit(item)">编辑</el-button>
            <el-button type="danger" plain @click="remove(item)">删除</el-button>
          </div>
        </div>
      </article>
    </section>

    <el-dialog v-model="dialogVisible" :title="editingId ? '编辑目的地' : '新增目的地'" width="760px">
      <el-form label-position="top" class="form-grid">
        <el-form-item label="目的地名称"><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="省份"><el-input v-model="form.province" /></el-form-item>
        <el-form-item label="主题"><el-input v-model="form.theme" /></el-form-item>
        <el-form-item label="评分"><el-input-number v-model="form.rating" :min="0" :max="5" :step="0.1" /></el-form-item>
        <el-form-item label="热度"><el-input-number v-model="form.popularityScore" :min="0" :max="100" /></el-form-item>
        <el-form-item label="封面图 URL"><el-input v-model="form.coverImage" /></el-form-item>
        <el-form-item label="介绍" class="full-row"><el-input v-model="form.description" type="textarea" :rows="4" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="dialogVisible = false">取消</el-button><el-button type="primary" @click="submit">保存</el-button></template>
    </el-dialog>

    <el-drawer v-model="detailVisible" title="目的地详情" size="480px">
      <div v-if="selectedDestination" class="detail-stack">
        <img class="detail-cover" :src="selectedDestination.coverImage" alt="destination-cover" />
        <div class="detail-card">
          <h3>{{ selectedDestination.name }}</h3>
          <p>{{ selectedDestination.description }}</p>
          <div class="detail-line"><strong>省份：</strong>{{ selectedDestination.province }}</div>
          <div class="detail-line"><strong>主题：</strong>{{ selectedDestination.theme }}</div>
          <div class="detail-line"><strong>评分：</strong>{{ selectedDestination.rating }} 分</div>
          <div class="detail-line"><strong>热度：</strong>{{ selectedDestination.popularityScore }}</div>
        </div>
      </div>
    </el-drawer>
  </AdminLayout>
</template>
