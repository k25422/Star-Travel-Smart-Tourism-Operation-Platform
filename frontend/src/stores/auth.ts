import { defineStore } from "pinia";
import { login as loginApi } from "../api/http";

export type UserRole = "ADMIN" | "USER" | "";

export const useAuthStore = defineStore("auth", {
  state: () => ({
    token: localStorage.getItem("tourism_token") || "",
    refreshToken: localStorage.getItem("tourism_refresh_token") || "",
    username: localStorage.getItem("tourism_username") || "",
    role: (localStorage.getItem("tourism_role") || "") as UserRole
  }),
  getters: {
    isAdmin: (state) => state.role === "ADMIN",
    isTraveler: (state) => state.role === "USER"
  },
  actions: {
    async login(username: string, password: string) {
      const result = await loginApi(username, password);
      this.token = result.accessToken;
      this.refreshToken = result.refreshToken;
      this.username = result.username;
      this.role = result.role as UserRole;
      localStorage.setItem("tourism_token", result.accessToken);
      localStorage.setItem("tourism_refresh_token", result.refreshToken);
      localStorage.setItem("tourism_username", result.username);
      localStorage.setItem("tourism_role", result.role);
    },
    logout() {
      this.token = "";
      this.refreshToken = "";
      this.username = "";
      this.role = "";
      localStorage.removeItem("tourism_token");
      localStorage.removeItem("tourism_refresh_token");
      localStorage.removeItem("tourism_username");
      localStorage.removeItem("tourism_role");
    }
  }
});