// 后端 API 地址。默认连接本机 8080 端口的 Spring Boot 后端。
// 如果后端换端口，可以在浏览器控制台设置 localStorage.TOURISM_API_BASE。
const API_BASE = localStorage.getItem("TOURISM_API_BASE") || "http://localhost:8080/api";

// 前端页面的全局数据状态。接口返回的数据会先放到这里，再统一渲染页面。
const state = {
  destinations: [],
  routes: [],
  bookings: [],
  stats: null
};

// 获取页面上的 DOM 节点，后面渲染数据时会用到。
const apiState = document.querySelector("#apiState");
const statsGrid = document.querySelector("#statsGrid");
const destinationGrid = document.querySelector("#destinationGrid");
const themeFilter = document.querySelector("#themeFilter");
const routeSearch = document.querySelector("#routeSearch");
const routeTable = document.querySelector("#routeTable");
const routeSelect = document.querySelector("#routeSelect");
const bookingForm = document.querySelector("#bookingForm");
const formMessage = document.querySelector("#formMessage");
const bookingList = document.querySelector("#bookingList");

// 页面加载后，先请求后端数据，再初始化 3D 场景。
init();
initThreeScene();

async function init() {
  try {
    // Promise.all 表示 4 个接口同时请求，比一个一个请求更快。
    const [destinations, routes, bookings, stats] = await Promise.all([
      request("/destinations"),
      request("/routes"),
      request("/bookings"),
      request("/dashboard/stats")
    ]);

    // 把接口数据保存到全局 state。
    state.destinations = destinations;
    state.routes = routes;
    state.bookings = bookings;
    state.stats = stats;

    // 后端连接成功后，渲染整个页面。
    setApiState(true);
    renderAll();
  } catch (error) {
    // 如果后端没启动或接口异常，显示离线提示。
    setApiState(false, error.message);
    renderOfflineState();
  }
}

async function request(path, options = {}) {
  // 封装 fetch。以后所有接口请求都走这里，方便统一处理错误。
  const response = await fetch(`${API_BASE}${path}`, {
    headers: { "Content-Type": "application/json" },
    ...options
  });

  if (!response.ok) {
    // 后端返回 400/500 时，尽量读取后端返回的 message。
    let message = `Request failed: ${response.status}`;
    try {
      const body = await response.json();
      message = body.message || message;
    } catch (error) {
      // Keep the status message when response is not JSON.
    }
    throw new Error(message);
  }

  return response.json();
}

function setApiState(online, message) {
  // 更新左侧底部的后端连接状态。
  apiState.classList.toggle("online", online);
  apiState.classList.toggle("offline", !online);
  apiState.textContent = online
    ? `后端已连接：${API_BASE}`
    : `后端未连接：${message || "请先启动 Spring Boot 服务"}`;
}

function renderAll() {
  // 统一渲染所有页面区域。
  renderStats();
  renderThemeOptions();
  renderDestinations();
  renderRoutes();
  renderRouteOptions();
  renderBookings();
}

function renderStats() {
  // 渲染首页统计卡片。
  const stats = state.stats || {};
  const cards = [
    ["目的地", stats.destinationCount || 0],
    ["上线线路", stats.routeCount || 0],
    ["累计预订", stats.bookingCount || 0],
    ["可售座位", stats.availableSeats || 0],
    ["活跃收入", formatCurrency(stats.revenue || 0)],
    ["平均评分", Number(stats.averageRating || 0).toFixed(1)]
  ];

  statsGrid.innerHTML = cards.map(([label, value]) => `
    <div class="stat-card">
      <span>${label}</span>
      <strong>${value}</strong>
    </div>
  `).join("");
}

function renderThemeOptions() {
  // 从目的地数据里提取所有主题，填充筛选下拉框。
  const themes = [...new Set(state.destinations.map((item) => item.theme))];
  themeFilter.innerHTML = `<option value="all">全部主题</option>` + themes
    .map((theme) => `<option value="${escapeHtml(theme)}">${escapeHtml(theme)}</option>`)
    .join("");
}

function renderDestinations() {
  // 根据下拉框选择的主题，筛选目的地卡片。
  const activeTheme = themeFilter.value || "all";
  const destinations = activeTheme === "all"
    ? state.destinations
    : state.destinations.filter((item) => item.theme === activeTheme);

  destinationGrid.innerHTML = destinations.map((item) => `
    <div class="destination-card">
      <div class="destination-image" style="background-image:url('${escapeHtml(item.coverImage)}')"></div>
      <article>
        <h3>${escapeHtml(item.name)}</h3>
        <p>${escapeHtml(item.description)}</p>
        <div class="meta-row">
          <span class="tag">${escapeHtml(item.province)}</span>
          <span class="tag">${escapeHtml(item.theme)}</span>
          <span class="tag">评分 ${Number(item.rating).toFixed(1)}</span>
        </div>
      </article>
    </div>
  `).join("");
}

function renderRoutes() {
  // 根据搜索框输入，过滤路线表格。
  const keyword = routeSearch.value.trim().toLowerCase();
  const routes = state.routes.filter((route) => {
    const text = `${route.title} ${route.departureCity} ${route.guideName} ${route.destination.name}`.toLowerCase();
    return text.includes(keyword);
  });

  routeTable.innerHTML = routes.map((route) => `
    <tr>
      <td><strong>${escapeHtml(route.title)}</strong><span>${escapeHtml(route.highlight)}</span></td>
      <td>${escapeHtml(route.destination.name)}</td>
      <td><strong>${escapeHtml(route.departureCity)}</strong><span>${route.departureDate}</span></td>
      <td>${route.durationDays} 天</td>
      <td>${route.availableSeats}</td>
      <td>${formatCurrency(route.price)}</td>
    </tr>
  `).join("");
}

function renderRouteOptions() {
  // 把路线数据填充到预订表单的下拉框。
  routeSelect.innerHTML = state.routes
    .map((route) => `<option value="${route.id}">${escapeHtml(route.title)} · ${formatCurrency(route.price)} · 余位 ${route.availableSeats}</option>`)
    .join("");
}

function renderBookings() {
  // 渲染最近预订列表。slice().reverse() 表示不修改原数组，只倒序展示。
  bookingList.innerHTML = state.bookings.slice().reverse().map((booking) => `
    <div class="booking-item">
      <div>
        <strong>${escapeHtml(booking.travelerName)} · ${escapeHtml(booking.route.title)}</strong>
        <p>${booking.travelers} 人 · ${escapeHtml(booking.phone)} · ${escapeHtml(booking.route.destination.name)}</p>
      </div>
      <strong>${formatCurrency(booking.totalAmount)}</strong>
    </div>
  `).join("");
}

function renderOfflineState() {
  // 后端没连上时，首页统计卡片显示启动提示。
  statsGrid.innerHTML = [
    ["后端状态", "未连接"],
    ["启动命令", "mvn spring-boot:run"],
    ["API 地址", "localhost:8080"],
    ["数据库", "H2"]
  ].map(([label, value]) => `
    <div class="stat-card">
      <span>${label}</span>
      <strong>${value}</strong>
    </div>
  `).join("");
}

// 绑定筛选和搜索事件。
themeFilter.addEventListener("change", renderDestinations);
routeSearch.addEventListener("input", renderRoutes);

bookingForm.addEventListener("submit", async (event) => {
  // 阻止浏览器默认提交表单，改成用 fetch 调后端接口。
  event.preventDefault();
  formMessage.classList.remove("error");
  formMessage.textContent = "正在提交...";

  const formData = new FormData(bookingForm);
  // 把表单数据整理成后端 BookingRequest 需要的 JSON 结构。
  const payload = {
    routeId: Number(formData.get("routeId")),
    travelerName: formData.get("travelerName"),
    phone: formData.get("phone"),
    travelers: Number(formData.get("travelers"))
  };

  try {
    // 调用 POST /api/bookings 创建订单。
    await request("/bookings", {
      method: "POST",
      body: JSON.stringify(payload)
    });
    formMessage.textContent = "预订成功，库存和收入已更新。";
    bookingForm.reset();

    // 重新请求接口，刷新路线库存、统计卡片和订单列表。
    await init();
  } catch (error) {
    formMessage.classList.add("error");
    formMessage.textContent = error.message;
  }
});

function formatCurrency(value) {
  // 把数字格式化成人民币显示，例如 ¥3,699。
  return Number(value).toLocaleString("zh-CN", {
    style: "currency",
    currency: "CNY",
    maximumFractionDigits: 0
  });
}

function escapeHtml(value) {
  // 防止把用户输入直接插入 HTML 造成脚本注入。
  return String(value)
    .replace(/&/g, "&amp;")
    .replace(/</g, "&lt;")
    .replace(/>/g, "&gt;")
    .replace(/"/g, "&quot;")
    .replace(/'/g, "&#039;");
}

async function initThreeScene() {
  // 初始化 Three.js 3D 地球。如果 CDN 加载失败，会自动使用 CSS 地球。
  const canvas = document.querySelector("#worldCanvas");

  try {
    // 从 CDN 动态加载 Three.js。这样不需要 npm，也能有 3D 效果。
    const THREE = await import("https://cdn.jsdelivr.net/npm/three@0.160.0/build/three.module.js");
    const scene = new THREE.Scene();
    const camera = new THREE.PerspectiveCamera(45, 1, 0.1, 100);
    const renderer = new THREE.WebGLRenderer({ canvas, alpha: true, antialias: true });
    const group = new THREE.Group();

    // 创建地球主体：SphereGeometry 是球体，MeshStandardMaterial 是材质。
    const globe = new THREE.Mesh(
      new THREE.SphereGeometry(2.2, 48, 48),
      new THREE.MeshStandardMaterial({
        color: 0x2b9b83,
        roughness: 0.54,
        metalness: 0.12
      })
    );
    group.add(globe);
    group.position.set(1.25, 0.25, 0);

    // 创建三条环绕地球的轨道线。
    const ringMaterial = new THREE.MeshBasicMaterial({ color: 0xf2d06b, transparent: true, opacity: 0.58 });
    for (let i = 0; i < 3; i += 1) {
      const ring = new THREE.Mesh(new THREE.TorusGeometry(2.42 + i * 0.18, 0.01, 12, 120), ringMaterial);
      ring.rotation.x = Math.PI / 2 + i * 0.45;
      ring.rotation.y = i * 0.38;
      group.add(ring);
    }

    // 创建三个目的地标记点。
    const pinMaterial = new THREE.MeshStandardMaterial({ color: 0xe66b4f, emissive: 0x34100a });
    [[-0.8, 1.4, 1.55], [1.15, 0.12, 1.86], [0.15, -1.25, 1.82]].forEach((position) => {
      const pin = new THREE.Mesh(new THREE.SphereGeometry(0.08, 18, 18), pinMaterial);
      pin.position.set(position[0], position[1], position[2]);
      group.add(pin);
    });

    // 灯光让球体有明暗变化，看起来更立体。
    const light = new THREE.DirectionalLight(0xffffff, 2.2);
    light.position.set(2.8, 3.4, 4);
    scene.add(light);
    scene.add(new THREE.AmbientLight(0x9fe8d7, 0.55));
    scene.add(group);

    camera.position.set(0, 0.2, 6);

    const resize = () => {
      // 浏览器窗口变化时，重新设置画布大小，避免 3D 图形拉伸。
      const width = canvas.clientWidth;
      const height = canvas.clientHeight;
      renderer.setSize(width, height, false);
      camera.aspect = width / height;
      camera.updateProjectionMatrix();
    };

    const animate = () => {
      // 每一帧旋转一点点，形成动态 3D 效果。
      group.rotation.y += 0.004;
      group.rotation.x = Math.sin(Date.now() * 0.0007) * 0.08;
      renderer.render(scene, camera);
      requestAnimationFrame(animate);
    };

    window.addEventListener("resize", resize);
    document.body.classList.add("three-ready");
    resize();
    animate();
  } catch (error) {
    // Three.js 加载失败时，隐藏 canvas，让 CSS 3D 地球显示。
    canvas.style.display = "none";
  }
}
