import { createRouter, createWebHistory } from "vue-router";
import { useAuthStore } from "../stores/auth";

const DashboardView = () => import("../views/DashboardView.vue");
const LoginView = () => import("../views/LoginView.vue");
const RoutesView = () => import("../views/RoutesView.vue");
const BookingsView = () => import("../views/BookingsView.vue");
const DestinationsView = () => import("../views/DestinationsView.vue");
const TravelerView = () => import("../views/TravelerView.vue");

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: "/login", component: LoginView },
    { path: "/travel", component: TravelerView, meta: { requiresAuth: true, roles: ["USER", "ADMIN"] } },
    { path: "/", component: DashboardView, meta: { requiresAuth: true, roles: ["ADMIN"] } },
    { path: "/routes", component: RoutesView, meta: { requiresAuth: true, roles: ["ADMIN"] } },
    { path: "/destinations", component: DestinationsView, meta: { requiresAuth: true, roles: ["ADMIN"] } },
    { path: "/bookings", component: BookingsView, meta: { requiresAuth: true, roles: ["ADMIN"] } }
  ]
});

// 前端路由守卫：负责“不同角色登录看到不同页面”。
router.beforeEach((to) => {
  const auth = useAuthStore();
  const hasValidRole = auth.role === "ADMIN" || auth.role === "USER";

  if (to.meta.requiresAuth && (!auth.token || !auth.refreshToken || !hasValidRole)) {
    auth.logout();
    return "/login";
  }

  if (to.path === "/login" && auth.token && auth.refreshToken && hasValidRole) {
    return auth.isAdmin ? "/" : "/travel";
  }

  const roles = to.meta.roles as string[] | undefined;
  if (roles && !roles.includes(auth.role)) {
    const fallbackPath = auth.isAdmin ? "/" : "/travel";
    return to.path === fallbackPath ? "/login" : fallbackPath;
  }
});

export default router;
