<script setup lang="ts">
import { computed, ref } from "vue";
import { ElMessage, ElMessageBox } from "element-plus";
import { Refresh, Search, View, Delete } from "@element-plus/icons-vue";
import AdminLayout from "../components/AdminLayout.vue";
import { deleteBooking, getAdminBookings, updateBookingStatus, type Booking } from "../api/http";
import { displayBooking } from "../utils/display";

const loading = ref(false);
const bookings = ref<Booking[]>([]);
const keyword = ref("");
const statusFilter = ref("");
const detailVisible = ref(false);
const selectedBooking = ref<Booking | null>(null);

const statusMap = {
  PENDING: { label: "待确认", type: "warning" },
  CONFIRMED: { label: "已确认", type: "success" },
  CANCELLED: { label: "已取消", type: "danger" }
} as const;

const displayRows = computed(() => bookings.value.map(displayBooking));
const filteredBookings = computed(() => {
  const word = keyword.value.trim().toLowerCase();
  return displayRows.value.filter((item) => {
    const matchesWord = !word || [item.travelerName, item.phone, item.route?.title, item.route?.departureCity]
      .filter(Boolean)
      .some((text) => String(text).toLowerCase().includes(word));
    const matchesStatus = !statusFilter.value || item.status === statusFilter.value;
    return matchesWord && matchesStatus;
  });
});

const statusSummary = computed(() => ({
  pending: displayRows.value.filter((item) => item.status === "PENDING").length,
  confirmed: displayRows.value.filter((item) => item.status === "CONFIRMED").length,
  cancelled: displayRows.value.filter((item) => item.status === "CANCELLED").length,
  revenue: displayRows.value.reduce((sum, item) => item.status === "CANCELLED" ? sum : sum + Number(item.totalAmount), 0)
}));

async function loadData() {
  loading.value = true;
  try {
    bookings.value = await getAdminBookings();
  } finally {
    loading.value = false;
  }
}

async function changeStatus(row: Booking, status: Booking["status"]) {
  if (row.status === status) return;
  await ElMessageBox.confirm(`确认将订单 #${row.id} 改为「${statusMap[status].label}」吗？`, "状态变更", { type: "warning" });
  await updateBookingStatus(row.id, status);
  ElMessage.success("订单状态已更新");
  await loadData();
}

async function remove(row: Booking) {
  await ElMessageBox.confirm(`确认删除订单 #${row.id} 吗？删除有效订单会自动回补路线库存。`, "删除确认", { type: "warning" });
  await deleteBooking(row.id);
  ElMessage.success("订单已删除");
  await loadData();
}

function openDetail(row: Booking) {
  selectedBooking.value = displayBooking(row);
  detailVisible.value = true;
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
    <header class="page-header">
      <div><p class="eyebrow">订单中心</p><h1>订单工作台</h1><span>支持订单查询、状态流转、详情查看、删除订单，满足后台管理 CRUD 展示。</span></div>
      <el-button :icon="Refresh" type="primary" @click="loadData">刷新订单</el-button>
    </header>

    <section class="metric-grid compact-metrics">
      <article class="metric-card"><span>待确认</span><strong>{{ statusSummary.pending }}</strong><small>需要客服跟进</small></article>
      <article class="metric-card"><span>已确认</span><strong>{{ statusSummary.confirmed }}</strong><small>进入出行准备</small></article>
      <article class="metric-card"><span>已取消</span><strong>{{ statusSummary.cancelled }}</strong><small>不计入有效收入</small></article>
      <article class="metric-card highlight"><span>有效收入</span><strong>¥{{ statusSummary.revenue.toLocaleString() }}</strong><small>自动排除取消订单</small></article>
    </section>

    <section class="filter-bar">
      <el-input v-model="keyword" :prefix-icon="Search" placeholder="搜索游客、手机号、路线、城市" clearable />
      <el-select v-model="statusFilter" placeholder="订单状态" clearable>
        <el-option label="待确认" value="PENDING" />
        <el-option label="已确认" value="CONFIRMED" />
        <el-option label="已取消" value="CANCELLED" />
      </el-select>
    </section>

    <section class="panel">
      <el-table :data="filteredBookings" v-loading="loading" height="600">
        <el-table-column label="订单号" width="90"><template #default="scope">#{{ scope.row.id }}</template></el-table-column>
        <el-table-column label="游客信息" width="180"><template #default="scope"><div class="table-title"><strong>{{ scope.row.travelerName }}</strong><span>{{ scope.row.phone }}</span></div></template></el-table-column>
        <el-table-column label="路线信息" min-width="260"><template #default="scope"><div class="table-title"><strong>{{ scope.row.route.title }}</strong><span>{{ scope.row.route.departureCity }} / {{ scope.row.route.departureDate }}</span></div></template></el-table-column>
        <el-table-column prop="travelers" label="人数" width="80" />
        <el-table-column label="金额" width="120"><template #default="scope">¥{{ Number(scope.row.totalAmount).toLocaleString() }}</template></el-table-column>
        <el-table-column label="状态" width="110"><template #default="scope"><el-tag :type="statusInfo(scope.row.status).type">{{ statusInfo(scope.row.status).label }}</el-tag></template></el-table-column>
        <el-table-column label="创建时间" width="170"><template #default="scope">{{ formatDate(scope.row.createdAt) }}</template></el-table-column>
        <el-table-column label="操作" width="300">
          <template #default="scope">
            <el-button link type="primary" :icon="View" @click="openDetail(scope.row)">详情</el-button>
            <el-button link type="success" @click="changeStatus(scope.row, 'CONFIRMED')">确认</el-button>
            <el-button link type="warning" @click="changeStatus(scope.row, 'PENDING')">待确认</el-button>
            <el-button link type="danger" @click="changeStatus(scope.row, 'CANCELLED')">取消</el-button>
            <el-button link type="danger" :icon="Delete" @click="remove(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </section>

    <el-drawer v-model="detailVisible" title="订单详情" size="460px">
      <div v-if="selectedBooking" class="detail-stack">
        <div class="detail-card">
          <h3>#{{ selectedBooking.id }}</h3>
          <div class="detail-line"><strong>游客：</strong>{{ selectedBooking.travelerName }}</div>
          <div class="detail-line"><strong>手机号：</strong>{{ selectedBooking.phone }}</div>
          <div class="detail-line"><strong>路线：</strong>{{ selectedBooking.route.title }}</div>
          <div class="detail-line"><strong>人数：</strong>{{ selectedBooking.travelers }}</div>
          <div class="detail-line"><strong>金额：</strong>¥{{ Number(selectedBooking.totalAmount).toLocaleString() }}</div>
          <div class="detail-line"><strong>状态：</strong>{{ statusInfo(selectedBooking.status).label }}</div>
          <div class="detail-line"><strong>创建时间：</strong>{{ formatDate(selectedBooking.createdAt) }}</div>
        </div>
      </div>
    </el-drawer>
  </AdminLayout>
</template>
