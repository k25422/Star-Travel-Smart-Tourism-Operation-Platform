import axios from "axios";
import { ElMessage } from "element-plus";

// 目的地表：对应后端 Destination 实体。
export interface Destination {
  id?: number;
  name: string;
  province: string;
  theme: string;
  description: string;
  coverImage: string;
  rating: number;
  popularityScore: number;
}

// 旅游路线表：对应后端 TravelRoute 实体。
export interface TravelRoute {
  id?: number;
  destination: Destination;
  title: string;
  durationDays: number;
  price: number;
  availableSeats: number;
  departureCity: string;
  departureDate: string;
  guideName: string;
  highlight: string;
}

// 订单表：对应后端 Booking 实体。
export interface Booking {
  id: number;
  route: TravelRoute;
  travelerName: string;
  phone: string;
  travelers: number;
  totalAmount: number;
  status: "PENDING" | "CONFIRMED" | "CANCELLED";
  createdAt: string;
}

export interface BookingRequest {
  routeId: number;
  travelerName: string;
  phone: string;
  travelers: number;
  requestId: string;
}

export interface DashboardStats {
  destinationCount: number;
  routeCount: number;
  bookingCount: number;
  revenue: number;
}

// Axios 实例：baseURL=/api，前端会通过 Vite 代理转发到 Spring Boot 的 8080。
export const http = axios.create({
  baseURL: "/api",
  timeout: 10000
});

let isRedirectingToLogin = false;

function clearAuthState() {
  localStorage.removeItem("tourism_token");
  localStorage.removeItem("tourism_refresh_token");
  localStorage.removeItem("tourism_username");
  localStorage.removeItem("tourism_role");
}

function redirectToLogin() {
  if (isRedirectingToLogin || window.location.pathname === "/login") {
    return;
  }
  isRedirectingToLogin = true;
  window.location.replace("/login");
}

// 请求拦截器：登录后每个请求自动带上 JWT。
http.interceptors.request.use((config) => {
  const token = localStorage.getItem("tourism_token");
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// 响应拦截器：统一显示错误提示，小白调试时能直接看到原因。
http.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 403) {
      ElMessage.error("当前账号没有权限访问该内容，请重新登录");
      clearAuthState();
      redirectToLogin();
      return Promise.reject(error);
    }
    const message = error.response?.data?.message || error.message || "请求失败，请检查后端服务是否启动";
    ElMessage.error(message);
    return Promise.reject(error);
  }
);

// 后端有些接口直接返回数据，有些接口返回 { code, message, data }，这里统一拆包。
function unwrap<T>(payload: any): T {
  return payload && Object.prototype.hasOwnProperty.call(payload, "data") ? payload.data : payload;
}

export async function login(username: string, password: string) {
  const { data } = await http.post("/auth/login", { username, password });
  return unwrap<{ accessToken: string; refreshToken: string; username: string; role: string }>(data);
}

export async function getStats() {
  const { data } = await http.get("/dashboard/stats");
  return unwrap<DashboardStats>(data);
}

export async function getRoutes() {
  const { data } = await http.get("/routes");
  return unwrap<TravelRoute[]>(data);
}

export async function getDestinations() {
  const { data } = await http.get("/destinations");
  return unwrap<Destination[]>(data);
}

export async function createBooking(payload: BookingRequest) {
  const { data } = await http.post("/bookings", payload);
  return unwrap<Booking>(data);
}

export async function getMyBookings() {
  const { data } = await http.get("/bookings");
  return unwrap<Booking[]>(data);
}

export async function cancelMyBooking(id: number) {
  const { data } = await http.patch(`/bookings/${id}/cancel`);
  return unwrap<Booking>(data);
}

export async function getAdminRoutes() {
  const { data } = await http.get("/admin/routes");
  return unwrap<TravelRoute[]>(data);
}

export async function createRoute(route: TravelRoute) {
  const { data } = await http.post("/admin/routes", route);
  return unwrap<TravelRoute>(data);
}

export async function updateRoute(id: number, route: TravelRoute) {
  const { data } = await http.put(`/admin/routes/${id}`, route);
  return unwrap<TravelRoute>(data);
}

export async function deleteRoute(id: number) {
  const { data } = await http.delete(`/admin/routes/${id}`);
  return unwrap<string>(data);
}

export async function getAdminDestinations() {
  const { data } = await http.get("/admin/destinations");
  return unwrap<Destination[]>(data);
}

export async function createDestination(destination: Destination) {
  const { data } = await http.post("/admin/destinations", destination);
  return unwrap<Destination>(data);
}

export async function updateDestination(id: number, destination: Destination) {
  const { data } = await http.put(`/admin/destinations/${id}`, destination);
  return unwrap<Destination>(data);
}

export async function deleteDestination(id: number) {
  const { data } = await http.delete(`/admin/destinations/${id}`);
  return unwrap<string>(data);
}

export async function getAdminBookings() {
  const { data } = await http.get("/admin/bookings");
  return unwrap<Booking[]>(data);
}

export async function updateBookingStatus(id: number, status: Booking["status"]) {
  const { data } = await http.patch(`/admin/bookings/${id}/status`, { status });
  return unwrap<Booking>(data);
}
