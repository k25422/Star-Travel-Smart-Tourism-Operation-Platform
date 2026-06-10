<script setup lang="ts">
import { reactive, ref } from "vue";
import { useRouter } from "vue-router";
import { ElMessage } from "element-plus";
import { User, Lock, Van, Setting } from "@element-plus/icons-vue";
import { useAuthStore } from "../stores/auth";

const router = useRouter();
const auth = useAuthStore();
const loading = ref(false);
const form = reactive({ username: "admin", password: "admin123" });

function fillAdmin() {
  form.username = "admin";
  form.password = "admin123";
}

function fillTraveler() {
  form.username = "traveler";
  form.password = "user123";
}

async function submit() {
  loading.value = true;
  try {
    await auth.login(form.username, form.password);
    ElMessage.success(auth.isAdmin ? "管理员登录成功" : "游客登录成功");
    router.push(auth.isAdmin ? "/" : "/travel");
  } finally {
    loading.value = false;
  }
}
</script>

<template>
  <main class="login-page">
    <section class="login-hero">
      <p class="eyebrow">旅游运营平台</p>
      <h1>旅游运营管理系统</h1>
      <p>一个适合求职展示的前后端分离项目：管理员负责运营管理，游客可浏览路线、收藏对比并在线预订。</p>
      <div class="login-metrics">
        <span>JWT 双 Token 鉴权</span>
        <span>管理员与游客双角色</span>
        <span>路线、订单、用户 CRUD</span>
        <span>3D 航线与目的地预览</span>
      </div>
    </section>

    <section class="login-panel">
      <p class="eyebrow">角色登录</p>
      <h2>选择角色进入系统</h2>
      <div class="role-switch">
        <el-button :icon="Setting" plain @click="fillAdmin">管理员账号</el-button>
        <el-button :icon="Van" plain @click="fillTraveler">游客账号</el-button>
      </div>
      <el-form @submit.prevent="submit" label-position="top">
        <el-form-item label="账号"><el-input v-model="form.username" :prefix-icon="User" size="large" /></el-form-item>
        <el-form-item label="密码"><el-input v-model="form.password" :prefix-icon="Lock" type="password" show-password size="large" /></el-form-item>
        <el-button type="primary" size="large" :loading="loading" @click="submit">登录系统</el-button>
      </el-form>
      <div class="account-tips">
        <p><strong>管理员：</strong>admin / admin123，可管理路线、目的地、订单和用户。</p>
        <p><strong>游客：</strong>traveler / user123，可浏览路线、收藏对比、提交预订。</p>
      </div>
    </section>
  </main>
</template>
