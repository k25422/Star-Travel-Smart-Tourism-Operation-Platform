<script setup lang="ts">
import { computed, reactive, ref } from "vue";
import { ElMessage, ElMessageBox } from "element-plus";
import { Plus, Search, Refresh, View } from "@element-plus/icons-vue";
import AdminLayout from "../components/AdminLayout.vue";
import { createAdminUser, deleteAdminUser, getAdminUsers, updateAdminUser, type AdminUser } from "../api/http";

const loading = ref(false);
const dialogVisible = ref(false);
const detailVisible = ref(false);
const editingId = ref<number | null>(null);
const users = ref<AdminUser[]>([]);
const keyword = ref("");
const roleFilter = ref("");
const selectedUser = ref<AdminUser | null>(null);

const form = reactive<AdminUser>({ username: "", password: "", nickname: "", email: "", role: "USER", enabled: true });

const filteredUsers = computed(() => {
  const word = keyword.value.trim().toLowerCase();
  return users.value.filter((item) => {
    const matchesWord = !word || [item.username, item.nickname, item.email]
      .filter(Boolean)
      .some((text) => String(text).toLowerCase().includes(word));
    const matchesRole = !roleFilter.value || item.role === roleFilter.value;
    return matchesWord && matchesRole;
  });
});

const userSummary = computed(() => ({
  total: users.value.length,
  adminCount: users.value.filter((item) => item.role === "ADMIN").length,
  travelerCount: users.value.filter((item) => item.role === "USER").length,
  enabledCount: users.value.filter((item) => item.enabled).length
}));

async function loadData() {
  loading.value = true;
  try {
    users.value = await getAdminUsers();
  } finally {
    loading.value = false;
  }
}

function resetForm() {
  editingId.value = null;
  form.username = "";
  form.password = "";
  form.nickname = "";
  form.email = "";
  form.role = "USER";
  form.enabled = true;
}

function openCreate() {
  resetForm();
  dialogVisible.value = true;
}

function openEdit(row: AdminUser) {
  editingId.value = row.id || null;
  form.username = row.username;
  form.password = "";
  form.nickname = row.nickname;
  form.email = row.email;
  form.role = row.role;
  form.enabled = row.enabled;
  dialogVisible.value = true;
}

function openDetail(row: AdminUser) {
  selectedUser.value = row;
  detailVisible.value = true;
}

async function submit() {
  if (!form.username || !form.nickname || !form.email) {
    ElMessage.warning("请补全用户名、昵称和邮箱");
    return;
  }
  if (!editingId.value && (!form.password || form.password.length < 6)) {
    ElMessage.warning("新增用户时密码至少 6 位");
    return;
  }
  const payload: AdminUser = {
    username: form.username,
    password: form.password,
    nickname: form.nickname,
    email: form.email,
    role: form.role,
    enabled: form.enabled
  };
  if (editingId.value) {
    await updateAdminUser(editingId.value, payload);
    ElMessage.success("用户已更新");
  } else {
    await createAdminUser(payload);
    ElMessage.success("用户已新增");
  }
  dialogVisible.value = false;
  await loadData();
}

async function remove(row: AdminUser) {
  await ElMessageBox.confirm(`确认删除用户「${row.username}」吗？`, "删除确认", { type: "warning" });
  await deleteAdminUser(row.id as number);
  ElMessage.success("用户已删除");
  await loadData();
}

function roleLabel(role: AdminUser["role"]) {
  return role === "ADMIN" ? "管理员" : "游客";
}

loadData();
</script>

<template>
  <AdminLayout>
    <header class="page-header">
      <div><p class="eyebrow">用户管理</p><h1>用户与角色中心</h1><span>支持管理员与游客账号的查询、查看、新增、编辑、删除，补全后台模块完整度。</span></div>
      <el-button type="primary" :icon="Plus" @click="openCreate">新增用户</el-button>
    </header>

    <section class="metric-grid compact-metrics">
      <article class="metric-card"><span>用户总数</span><strong>{{ userSummary.total }}</strong><small>系统账号总量</small></article>
      <article class="metric-card"><span>管理员</span><strong>{{ userSummary.adminCount }}</strong><small>后台运营账号</small></article>
      <article class="metric-card"><span>游客</span><strong>{{ userSummary.travelerCount }}</strong><small>前台体验账号</small></article>
      <article class="metric-card highlight"><span>启用账号</span><strong>{{ userSummary.enabledCount }}</strong><small>当前可登录账号</small></article>
    </section>

    <section class="filter-bar">
      <el-input v-model="keyword" :prefix-icon="Search" placeholder="搜索用户名、昵称、邮箱" clearable />
      <el-select v-model="roleFilter" placeholder="角色筛选" clearable>
        <el-option label="管理员" value="ADMIN" />
        <el-option label="游客" value="USER" />
      </el-select>
      <el-button :icon="Refresh" @click="loadData">刷新</el-button>
    </section>

    <section class="panel">
      <el-table :data="filteredUsers" v-loading="loading" height="620">
        <el-table-column prop="username" label="用户名" min-width="140" />
        <el-table-column prop="nickname" label="昵称" min-width="140" />
        <el-table-column prop="email" label="邮箱" min-width="220" />
        <el-table-column label="角色" width="110"><template #default="scope"><el-tag :type="scope.row.role === 'ADMIN' ? 'success' : 'info'">{{ roleLabel(scope.row.role) }}</el-tag></template></el-table-column>
        <el-table-column label="状态" width="110"><template #default="scope"><el-tag :type="scope.row.enabled ? 'success' : 'danger'">{{ scope.row.enabled ? "启用" : "停用" }}</el-tag></template></el-table-column>
        <el-table-column label="创建时间" width="170"><template #default="scope">{{ scope.row.createdAt ? scope.row.createdAt.replace("T", " ").slice(0, 16) : "-" }}</template></el-table-column>
        <el-table-column label="操作" width="220">
          <template #default="scope">
            <el-button link type="primary" :icon="View" @click="openDetail(scope.row)">详情</el-button>
            <el-button link type="warning" @click="openEdit(scope.row)">编辑</el-button>
            <el-button link type="danger" @click="remove(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </section>

    <el-dialog v-model="dialogVisible" :title="editingId ? '编辑用户' : '新增用户'" width="760px">
      <el-form label-position="top" class="form-grid">
        <el-form-item label="用户名"><el-input v-model="form.username" /></el-form-item>
        <el-form-item :label="editingId ? '重置密码（可选）' : '登录密码'"><el-input v-model="form.password" type="password" show-password /></el-form-item>
        <el-form-item label="昵称"><el-input v-model="form.nickname" /></el-form-item>
        <el-form-item label="邮箱"><el-input v-model="form.email" /></el-form-item>
        <el-form-item label="角色"><el-select v-model="form.role"><el-option label="管理员" value="ADMIN" /><el-option label="游客" value="USER" /></el-select></el-form-item>
        <el-form-item label="是否启用"><el-switch v-model="form.enabled" active-text="启用" inactive-text="停用" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="dialogVisible = false">取消</el-button><el-button type="primary" @click="submit">保存</el-button></template>
    </el-dialog>

    <el-drawer v-model="detailVisible" title="用户详情" size="420px">
      <div v-if="selectedUser" class="detail-stack">
        <div class="detail-card">
          <h3>{{ selectedUser.nickname }}</h3>
          <div class="detail-line"><strong>用户名：</strong>{{ selectedUser.username }}</div>
          <div class="detail-line"><strong>邮箱：</strong>{{ selectedUser.email }}</div>
          <div class="detail-line"><strong>角色：</strong>{{ roleLabel(selectedUser.role) }}</div>
          <div class="detail-line"><strong>状态：</strong>{{ selectedUser.enabled ? "启用" : "停用" }}</div>
          <div class="detail-line"><strong>创建时间：</strong>{{ selectedUser.createdAt ? selectedUser.createdAt.replace("T", " ").slice(0, 16) : "-" }}</div>
        </div>
      </div>
    </el-drawer>
  </AdminLayout>
</template>
