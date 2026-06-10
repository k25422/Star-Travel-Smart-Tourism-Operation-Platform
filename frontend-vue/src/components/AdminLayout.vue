<script setup lang="ts">
import { computed } from "vue";
import { useRoute, useRouter } from "vue-router";
import { useAuthStore } from "../stores/auth";
import { DataLine, Location, Tickets, SuitcaseLine, SwitchButton, Van } from "@element-plus/icons-vue";

const route = useRoute();
const router = useRouter();
const auth = useAuthStore();

// 管理员看到后台运营菜单；游客只看到旅行门户。
const navItems = computed(() => {
  if (auth.isTraveler) {
    return [{ path: "/travel", label: "旅行门户", icon: Van }];
  }
  return [
    { path: "/", label: "数据总览", icon: DataLine },
    { path: "/routes", label: "路线运营", icon: SuitcaseLine },
    { path: "/destinations", label: "目的地管理", icon: Location },
    { path: "/bookings", label: "订单中心", icon: Tickets },
    { path: "/travel", label: "游客视图预览", icon: Van }
  ];
});

const activePath = computed(() => route.path);
const roleLabel = computed(() => auth.isAdmin ? "管理员" : "游客");
const roleDescription = computed(() => auth.isAdmin ? "可维护业务数据" : "可浏览和预订路线");

function logout() {
  auth.logout();
  router.push("/login");
}
</script>

<template>
  <main class="admin-shell">
    <aside class="sidebar">
      <div class="brand-block">
        <span class="brand-mark">T</span>
        <div>
          <strong>Tourism Pro</strong>
          <small>旅游运营管理系统</small>
        </div>
      </div>

      <div class="role-card">
        <el-tag :type="auth.isAdmin ? 'success' : 'warning'">{{ roleLabel }}</el-tag>
        <span>{{ roleDescription }}</span>
      </div>

      <nav class="side-nav">
        <button
          v-for="item in navItems"
          :key="item.path"
          class="nav-item"
          :class="{ active: activePath === item.path }"
          @click="router.push(item.path)"
        >
          <el-icon><component :is="item.icon" /></el-icon>
          <span>{{ item.label }}</span>
        </button>
      </nav>

      <div class="user-box">
        <span>{{ auth.username || "未登录" }}</span>
        <small>{{ roleLabel }}</small>
        <el-button :icon="SwitchButton" plain @click="logout">退出登录</el-button>
      </div>
    </aside>

    <section class="main-panel">
      <slot />
    </section>
  </main>
</template>