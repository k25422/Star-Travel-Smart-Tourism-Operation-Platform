<script setup lang="ts">
import { onBeforeUnmount, onMounted, ref, watch } from "vue";
import * as THREE from "three";

const props = withDefaults(defineProps<{
  orbitSpeed?: number;
  accent?: string;
  glow?: string;
  autoRotate?: boolean;
}>(), {
  orbitSpeed: 1,
  accent: "#ff7cab",
  glow: "#ffd4e4",
  autoRotate: true
});

const host = ref<HTMLDivElement | null>(null);
let renderer: any = null;
let scene: any = null;
let camera: any = null;
let frameId = 0;
let rootGroup: any = null;
const orbitDots: Array<{ mesh: any; radius: number; offset: number; yOffset: number }> = [];

function hexToColor(hex: string) {
  return new THREE.Color(hex);
}

function clearScene() {
  orbitDots.length = 0;
  if (scene && rootGroup) {
    scene.remove(rootGroup);
  }
  rootGroup = null;
}

function buildScene() {
  if (!scene) return;
  clearScene();

  rootGroup = new THREE.Group();
  scene.add(rootGroup);

  const globe = new THREE.Mesh(
    new THREE.SphereGeometry(2.08, 72, 72),
    new THREE.MeshStandardMaterial({
      color: hexToColor(props.accent),
      roughness: 0.32,
      metalness: 0.18,
      transparent: true,
      opacity: 0.92
    })
  );
  rootGroup.add(globe);

  const wire = new THREE.Mesh(
    new THREE.SphereGeometry(2.14, 42, 42),
    new THREE.MeshBasicMaterial({
      color: hexToColor("#fff7fb"),
      wireframe: true,
      transparent: true,
      opacity: 0.22
    })
  );
  rootGroup.add(wire);

  const halo = new THREE.Mesh(
    new THREE.SphereGeometry(2.45, 50, 50),
    new THREE.MeshBasicMaterial({
      color: hexToColor(props.glow),
      transparent: true,
      opacity: 0.08
    })
  );
  rootGroup.add(halo);

  for (let i = 0; i < 6; i += 1) {
    const torus = new THREE.Mesh(
      new THREE.TorusGeometry(2.7 + i * 0.18, 0.012 + i * 0.002, 12, 140),
      new THREE.MeshBasicMaterial({
        color: i % 2 === 0 ? hexToColor(props.glow) : hexToColor("#ffffff"),
        transparent: true,
        opacity: 0.25 - i * 0.02
      })
    );
    torus.rotation.x = Math.PI / 2 + i * 0.18;
    torus.rotation.y = i * 0.38;
    torus.rotation.z = i * 0.16;
    rootGroup.add(torus);
  }

  const pulseGeometry = new THREE.SphereGeometry(0.07, 18, 18);
  for (let i = 0; i < 12; i += 1) {
    const dot = new THREE.Mesh(
      pulseGeometry,
      new THREE.MeshBasicMaterial({ color: i % 3 === 0 ? hexToColor("#ffffff") : hexToColor(props.glow) })
    );
    rootGroup.add(dot);
    orbitDots.push({
      mesh: dot,
      radius: 2.75 + (i % 4) * 0.18,
      offset: i * 0.52,
      yOffset: ((i % 3) - 1) * 0.28
    });
  }

  const stars = new THREE.BufferGeometry();
  const starPositions: number[] = [];
  for (let i = 0; i < 340; i += 1) {
    starPositions.push((Math.random() - 0.5) * 18, (Math.random() - 0.5) * 11, (Math.random() - 0.5) * 10);
  }
  stars.setAttribute("position", new THREE.Float32BufferAttribute(starPositions, 3));
  const starField = new THREE.Points(
    stars,
    new THREE.PointsMaterial({ color: "#fff7fb", size: 0.03, transparent: true, opacity: 0.65 })
  );
  rootGroup.add(starField);
}

function render() {
  if (!renderer || !scene || !camera || !rootGroup) return;
  const t = Date.now() * 0.001 * props.orbitSpeed;

  if (props.autoRotate) {
    rootGroup.rotation.y += 0.0035;
    rootGroup.rotation.x = Math.sin(t * 0.7) * 0.06;
  }

  orbitDots.forEach((item, index) => {
    const angle = t + item.offset;
    item.mesh.position.set(
      Math.cos(angle) * item.radius,
      item.yOffset + Math.sin(angle * 1.4) * 0.18,
      Math.sin(angle) * item.radius
    );
    const scale = 0.9 + Math.sin(t * 2 + index) * 0.25;
    item.mesh.scale.setScalar(scale);
  });

  renderer.render(scene, camera);
  frameId = window.requestAnimationFrame(render);
}

function init() {
  if (!host.value) return;
  scene = new THREE.Scene();
  camera = new THREE.PerspectiveCamera(42, host.value.clientWidth / host.value.clientHeight, 0.1, 1000);
  camera.position.set(0, 0, 8);

  renderer = new THREE.WebGLRenderer({ antialias: true, alpha: true });
  renderer.setPixelRatio(Math.min(window.devicePixelRatio, 2));
  renderer.setSize(host.value.clientWidth, host.value.clientHeight);
  host.value.innerHTML = "";
  host.value.appendChild(renderer.domElement);

  scene.add(new THREE.AmbientLight(0xffffff, 1.7));
  const light = new THREE.DirectionalLight(0xffffff, 2.8);
  light.position.set(4, 5, 6);
  scene.add(light);
  const back = new THREE.PointLight(hexToColor(props.glow), 2.4, 18);
  back.position.set(-5, 2, -3);
  scene.add(back);

  buildScene();
  render();
}

function resize() {
  if (!host.value || !renderer || !camera) return;
  camera.aspect = host.value.clientWidth / host.value.clientHeight;
  camera.updateProjectionMatrix();
  renderer.setSize(host.value.clientWidth, host.value.clientHeight);
}

onMounted(() => {
  init();
  window.addEventListener("resize", resize);
});

watch(() => [props.accent, props.glow], buildScene);

onBeforeUnmount(() => {
  window.removeEventListener("resize", resize);
  window.cancelAnimationFrame(frameId);
  renderer?.dispose();
});
</script>

<template>
  <div ref="host" class="shared-globe-canvas"></div>
</template>
