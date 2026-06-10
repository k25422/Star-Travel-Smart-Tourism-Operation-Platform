import axios from "axios";
import { ElMessage } from "element-plus";

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

export interface AdminUser {
  id?: number;
  username: string;
  password?: string;
  nickname: string;
  email: string;
  role: "ADMIN" | "USER";
  enabled: boolean;
  createdAt?: string;
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

export const http = axios.create({ baseURL: "/api", timeout: 10000 });

let isRefreshing = false;
let pendingQueue: Array<(token: string) => void> = [];
let isRedirectingToLogin = false;

function flushQueue(token: string) {
  pendingQueue.forEach((resolve) => resolve(token));
  pendingQueue = [];
}

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

http.interceptors.request.use((config) => {
  const token = localStorage.getItem("tourism_token");
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

http.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error.config;
    const refreshToken = localStorage.getItem("tourism_refresh_token");

    if (error.response?.status === 401 && refreshToken && !originalRequest._retry && !originalRequest.url?.includes("/auth/refresh")) {
      originalRequest._retry = true;

      if (isRefreshing) {
        return new Promise((resolve) => {
          pendingQueue.push((token: string) => {
            originalRequest.headers.Authorization = `Bearer ${token}`;
            resolve(http(originalRequest));
          });
        });
      }

      isRefreshing = true;
      try {
        const { data } = await axios.post("/api/auth/refresh", { refreshToken });
        const payload = data.data;
        localStorage.setItem("tourism_token", payload.accessToken);
        localStorage.setItem("tourism_refresh_token", payload.refreshToken);
        flushQueue(payload.accessToken);
        originalRequest.headers.Authorization = `Bearer ${payload.accessToken}`;
        return http(originalRequest);
      } catch (refreshError) {
        clearAuthState();
        redirectToLogin();
        return Promise.reject(refreshError);
      } finally {
        isRefreshing = false;
      }
    }

    if (error.response?.status === 403) {
      ElMessage.error("当前账号没有权限访问该内容，请重新登录");
      clearAuthState();
      redirectToLogin();
      return Promise.reject(error);
    }

    const message = error.response?.data?.message || error.message || "Request failed";
    ElMessage.error(message);
    return Promise.reject(error);
  }
);

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

export async function deleteBooking(id: number) {
  const { data } = await http.delete(`/admin/bookings/${id}`);
  return unwrap<string>(data);
}

export async function getAdminUsers() {
  const { data } = await http.get("/admin/users");
  return unwrap<AdminUser[]>(data);
}

export async function createAdminUser(payload: AdminUser) {
  const { data } = await http.post("/admin/users", payload);
  return unwrap<AdminUser>(data);
}

export async function updateAdminUser(id: number, payload: AdminUser) {
  const { data } = await http.put(`/admin/users/${id}`, payload);
  return unwrap<AdminUser>(data);
}

export async function deleteAdminUser(id: number) {
  const { data } = await http.delete(`/admin/users/${id}`);
  return unwrap<string>(data);
}
