<script setup lang="ts">
import { computed, ref } from "vue";
import { ElMessage, ElMessageBox } from "element-plus";
import { Refresh, Search } from "@element-plus/icons-vue";
import AdminLayout from "../components/AdminLayout.vue";
import { getAdminBookings, updateBookingStatus, type Booking } from "../api/http";

const loading = ref(false);
const bookings = ref<Booking[]>([]);
const keyword = ref("");
const statusFilter = ref("");

const statusMap: Record<Booking["status"], { label: string; type: "info" | "success" | "warning" | "danger" }> = {
  PENDING: { label: "待确认", type: "warning" },
  CONFIRMED: { label: "已确认", type: "success" },
  CANCELLED: { label: "已取消", type: "danger" }
};

const filteredBookings = computed(() => {
  const word = keyword.value.trim().toLowerCase();
  return bookings.value.filter((item) => {
    const matchesWord = !word || [item.travelerName, item.phone, item.route?.title, item.route?.departureCity]
      .filter(Boolean)
      .some((text) => String(text).toLowerCase().includes(word));
    const matchesStatus = !statusFilter.value || item.status === statusFilter.value;
    return matchesWord && matchesStatus;
  });
});

const statusSummary = computed(() => ({
  pending: bookings.value.filter((item) => item.status === "PENDING").length,
  confirmed: bookings.value.filter((item) => item.status === "CONFIRMED").length,
  cancelled: bookings.value.filter((item) => item.status === "CANCELLED").length,
  revenue: bookings.value.reduce((sum, item) => item.status === "CANCELLED" ? sum : sum + Number(item.totalAmount), 0)
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
      <div>
        <p class="eyebrow">Booking Workflow</p>
        <h1>订单中心</h1>
        <span>查询订单、识别状态、执行确认或取消，展示后台业务流转能力。</span>
      </div>
      <el-button :icon="Refresh" type="primary" @click="loadData">刷新订单</el-button>
    </header>

    <section class="metric-grid compact-metrics">
      <article class="metric-card">
        <span>待确认</span>
        <strong>{{ statusSummary.pending }}</strong>
        <small>需要客服跟进</small>
      </article>
      <article class="metric-card">
        <span>已确认</span>
        <strong>{{ statusSummary.confirmed }}</strong>
        <small>可进入出行准备</small>
      </article>
      <article class="metric-card">
        <span>已取消</span>
        <strong>{{ statusSummary.cancelled }}</strong>
        <small>不计入有效收入</small>
      </article>
      <article class="metric-card highlight">
        <span>有效收入</span>
        <strong>¥{{ statusSummary.revenue.toLocaleString() }}</strong>
        <small>排除已取消订单</small>
      </article>
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
        <el-table-column label="订单号" width="90">
          <template #default="scope">#{{ scope.row.id }}</template>
        </el-table-column>
        <el-table-column label="游客" width="160">
          <template #default="scope">
            <div class="table-title">
              <strong>{{ scope.row.travelerName }}</strong>
              <span>{{ scope.row.phone }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="路线" min-width="260">
          <template #default="scope">
            <div class="table-title">
              <strong>{{ scope.row.route.title }}</strong>
              <span>{{ scope.row.route.departureCity }} · {{ scope.row.route.departureDate }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="travelers" label="人数" width="90" />
        <el-table-column label="金额" width="130">
          <template #default="scope">¥{{ Number(scope.row.totalAmount).toLocaleString() }}</template>
        </el-table-column>
        <el-table-column label="状态" width="120">
          <template #default="scope">
            <el-tag :type="statusInfo(scope.row.status).type">{{ statusInfo(scope.row.status).label }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="创建时间" width="170">
          <template #default="scope">{{ formatDate(scope.row.createdAt) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="scope">
            <el-button link type="success" @click="changeStatus(scope.row, 'CONFIRMED')">确认</el-button>
            <el-button link type="warning" @click="changeStatus(scope.row, 'PENDING')">待确认</el-button>
            <el-button link type="danger" @click="changeStatus(scope.row, 'CANCELLED')">取消</el-button>
          </template>
        </el-table-column>
      </el-table>
    </section>
  </AdminLayout>
</template>