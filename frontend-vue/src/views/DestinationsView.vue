<script setup lang="ts">
import { computed, reactive, ref } from "vue";
import { ElMessage, ElMessageBox } from "element-plus";
import { Plus, Search, Refresh } from "@element-plus/icons-vue";
import AdminLayout from "../components/AdminLayout.vue";
import { createDestination, deleteDestination, getAdminDestinations, updateDestination, type Destination } from "../api/http";

const loading = ref(false);
const dialogVisible = ref(false);
const editingId = ref<number | null>(null);
const destinations = ref<Destination[]>([]);
const keyword = ref("");
const themeFilter = ref("");

const form = reactive<Destination>({
  name: "",
  province: "",
  theme: "Mountain",
  description: "",
  coverImage: "https://images.unsplash.com/photo-1500530855697-b586d89ba3ee?auto=format&fit=crop&w=1200&q=80",
  rating: 4.8,
  popularityScore: 90
});

const themes = computed(() => Array.from(new Set(destinations.value.map((item) => item.theme))));
const filteredDestinations = computed(() => {
  const word = keyword.value.trim().toLowerCase();
  return destinations.value.filter((item) => {
    const matchesWord = !word || [item.name, item.province, item.theme, item.description]
      .some((text) => String(text).toLowerCase().includes(word));
    const matchesTheme = !themeFilter.value || item.theme === themeFilter.value;
    return matchesWord && matchesTheme;
  });
});

async function loadData() {
  loading.value = true;
  try {
    destinations.value = await getAdminDestinations();
  } finally {
    loading.value = false;
  }
}

function resetForm() {
  editingId.value = null;
  form.name = "";
  form.province = "";
  form.theme = "Mountain";
  form.description = "";
  form.coverImage = "https://images.unsplash.com/photo-1500530855697-b586d89ba3ee?auto=format&fit=crop&w=1200&q=80";
  form.rating = 4.8;
  form.popularityScore = 90;
}

function openCreate() {
  resetForm();
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
  await ElMessageBox.confirm(`确认删除目的地「${row.name}」吗？已被路线引用时后端会拒绝删除。`, "删除确认", { type: "warning" });
  await deleteDestination(row.id);
  ElMessage.success("目的地已删除");
  await loadData();
}

loadData();
</script>

<template>
  <AdminLayout>
    <header class="page-header">
      <div>
        <p class="eyebrow">Destination CRM</p>
        <h1>目的地管理</h1>
        <span>维护目的地资源、主题标签、封面图、评分和热度，供路线运营使用。</span>
      </div>
      <el-button type="primary" :icon="Plus" @click="openCreate">新增目的地</el-button>
    </header>

    <section class="filter-bar">
      <el-input v-model="keyword" :prefix-icon="Search" placeholder="搜索名称、省份、主题、介绍" clearable />
      <el-select v-model="themeFilter" placeholder="主题筛选" clearable>
        <el-option v-for="item in themes" :key="item" :label="item" :value="item" />
      </el-select>
      <el-button :icon="Refresh" @click="loadData">刷新</el-button>
    </section>

    <section class="destination-grid" v-loading="loading">
      <article v-for="item in filteredDestinations" :key="item.id" class="destination-card">
        <img :src="item.coverImage" alt="目的地封面" />
        <div class="destination-card-body">
          <div class="card-heading">
            <div>
              <strong>{{ item.name }}</strong>
              <span>{{ item.province }} · {{ item.theme }}</span>
            </div>
            <el-tag>{{ item.rating }} 分</el-tag>
          </div>
          <p>{{ item.description }}</p>
          <el-progress :percentage="item.popularityScore" :stroke-width="8" />
          <div class="card-actions">
            <el-button type="primary" plain @click="openEdit(item)">编辑</el-button>
            <el-button type="danger" plain @click="remove(item)">删除</el-button>
          </div>
        </div>
      </article>
    </section>

    <el-dialog v-model="dialogVisible" :title="editingId ? '编辑目的地' : '新增目的地'" width="760px">
      <el-form label-position="top" class="form-grid">
        <el-form-item label="目的地名称">
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="省份">
          <el-input v-model="form.province" />
        </el-form-item>
        <el-form-item label="主题">
          <el-input v-model="form.theme" placeholder="Mountain / Island / Culture" />
        </el-form-item>
        <el-form-item label="评分">
          <el-input-number v-model="form.rating" :min="0" :max="5" :step="0.1" />
        </el-form-item>
        <el-form-item label="热度">
          <el-slider v-model="form.popularityScore" :min="0" :max="100" />
        </el-form-item>
        <el-form-item label="封面图 URL">
          <el-input v-model="form.coverImage" />
        </el-form-item>
        <el-form-item label="介绍" class="full-row">
          <el-input v-model="form.description" type="textarea" :rows="4" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submit">保存</el-button>
      </template>
    </el-dialog>
  </AdminLayout>
</template>