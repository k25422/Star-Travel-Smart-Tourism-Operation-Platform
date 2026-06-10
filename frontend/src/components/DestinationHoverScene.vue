<script setup lang="ts">
import { onBeforeUnmount, onMounted, ref, watch } from "vue";
import * as THREE from "three";

const props = defineProps<{
  sceneKey: number;
}>();

const sceneRef = ref<HTMLDivElement | null>(null);
let renderer: any = null;
let scene: any = null;
let camera: any = null;
let frameId = 0;
let group: any = null;

function clearGroup() {
  if (!group || !scene) return;
  scene.remove(group);
  group = null;
}

function addMountainScene() {
  const rock = new THREE.MeshStandardMaterial({ color: "#92664a", roughness: 0.9 });
  const snow = new THREE.MeshStandardMaterial({ color: "#eef3fa", roughness: 0.35 });
  for (let i = 0; i < 4; i += 1) {
    const peak = new THREE.Mesh(new THREE.ConeGeometry(0.55 + i * 0.08, 1.55 + i * 0.22, 6), rock);
    peak.position.set(-1.55 + i * 1.02, -0.2, -0.45 + i * 0.12);
    group.add(peak);
    const cap = new THREE.Mesh(new THREE.ConeGeometry(0.22 + i * 0.03, 0.38, 6), snow);
    cap.position.set(peak.position.x, peak.position.y + 0.78, peak.position.z);
    group.add(cap);
  }
}

function addIslandScene() {
  const water = new THREE.Mesh(new THREE.CylinderGeometry(2.4, 2.4, 0.24, 40), new THREE.MeshStandardMaterial({ color: "#36b0d6", metalness: 0.2, roughness: 0.18 }));
  water.position.y = -1.1;
  group.add(water);
  const island = new THREE.Mesh(new THREE.SphereGeometry(0.9, 32, 32), new THREE.MeshStandardMaterial({ color: "#d8bb70", roughness: 1 }));
  island.scale.set(1.45, 0.54, 1.1);
  island.position.y = -0.58;
  group.add(island);
  const trunk = new THREE.Mesh(new THREE.CylinderGeometry(0.05, 0.07, 0.82, 10), new THREE.MeshStandardMaterial({ color: "#6d4a34" }));
  trunk.position.set(0.32, -0.08, 0.1);
  trunk.rotation.z = -0.4;
  group.add(trunk);
  for (let i = 0; i < 5; i += 1) {
    const leaf = new THREE.Mesh(new THREE.ConeGeometry(0.28, 0.9, 4), new THREE.MeshStandardMaterial({ color: i % 2 === 0 ? "#2da86a" : "#54c281" }));
    leaf.position.set(0.56, 0.22, 0.06);
    leaf.rotation.z = -1.2 + i * 0.62;
    leaf.rotation.x = 0.45;
    group.add(leaf);
  }
}

function addCultureScene() {
  const base = new THREE.Mesh(new THREE.CylinderGeometry(1.75, 1.75, 0.18, 30), new THREE.MeshStandardMaterial({ color: "#a67c50", roughness: 0.88 }));
  base.position.y = -1.08;
  group.add(base);
  for (let i = 0; i < 5; i += 1) {
    const pillar = new THREE.Mesh(new THREE.CylinderGeometry(0.12, 0.12, 1.25, 16), new THREE.MeshStandardMaterial({ color: "#d7b583", roughness: 0.7 }));
    pillar.position.set(-1 + i * 0.5, -0.28 + Math.abs(2 - i) * 0.05, -0.08);
    group.add(pillar);
  }
  const arch = new THREE.Mesh(new THREE.TorusGeometry(0.95, 0.12, 16, 40, Math.PI), new THREE.MeshStandardMaterial({ color: "#ecc690", roughness: 0.56 }));
  arch.rotation.z = Math.PI;
  arch.position.set(0, 0.38, -0.08);
  group.add(arch);
  const road = new THREE.Mesh(new THREE.BoxGeometry(2.2, 0.05, 0.55), new THREE.MeshStandardMaterial({ color: "#705139", roughness: 1 }));
  road.position.set(0, -0.96, 0.58);
  group.add(road);
}

function addLeisureScene() {
  const lake = new THREE.Mesh(new THREE.CylinderGeometry(2.05, 2.05, 0.18, 40), new THREE.MeshStandardMaterial({ color: "#4da8d8", roughness: 0.22, metalness: 0.2 }));
  lake.position.y = -1.08;
  group.add(lake);
  const pavilion = new THREE.Mesh(new THREE.CylinderGeometry(0.45, 0.45, 0.42, 6), new THREE.MeshStandardMaterial({ color: "#c85d53", roughness: 0.85 }));
  pavilion.position.set(-0.2, -0.6, 0.2);
  group.add(pavilion);
  const roof = new THREE.Mesh(new THREE.ConeGeometry(0.62, 0.45, 6), new THREE.MeshStandardMaterial({ color: "#5f4030", roughness: 0.82 }));
  roof.position.set(-0.2, -0.12, 0.2);
  group.add(roof);
  for (let i = 0; i < 3; i += 1) {
    const tree = new THREE.Mesh(new THREE.SphereGeometry(0.26 + i * 0.03, 20, 20), new THREE.MeshStandardMaterial({ color: i % 2 === 0 ? "#68a85b" : "#4e8f47" }));
    tree.position.set(0.55 + i * 0.45, -0.35 + i * 0.05, -0.2 + i * 0.12);
    group.add(tree);
  }
}

function buildScene() {
  if (!scene) return;
  clearGroup();
  group = new THREE.Group();
  scene.add(group);

  const variant = props.sceneKey % 4;
  if (variant === 1) addMountainScene();
  if (variant === 2) addIslandScene();
  if (variant === 3) addCultureScene();
  if (variant === 0) addLeisureScene();
}

function init() {
  if (!sceneRef.value) return;
  scene = new THREE.Scene();
  camera = new THREE.PerspectiveCamera(38, sceneRef.value.clientWidth / sceneRef.value.clientHeight, 0.1, 100);
  camera.position.set(0, 1.2, 6);
  renderer = new THREE.WebGLRenderer({ antialias: true, alpha: true });
  renderer.setPixelRatio(Math.min(window.devicePixelRatio, 2));
  renderer.setSize(sceneRef.value.clientWidth, sceneRef.value.clientHeight);
  sceneRef.value.innerHTML = "";
  sceneRef.value.appendChild(renderer.domElement);

  scene.add(new THREE.AmbientLight(0xffffff, 1.9));
  const light = new THREE.DirectionalLight(0xffffff, 2.6);
  light.position.set(4, 6, 5);
  scene.add(light);
  const backLight = new THREE.PointLight("#7fe7d0", 2.2, 20);
  backLight.position.set(-4, 2, -3);
  scene.add(backLight);

  buildScene();

  const animate = () => {
    if (group) {
      group.rotation.y += 0.01;
      group.rotation.x = Math.sin(Date.now() * 0.0011) * 0.08;
    }
    renderer.render(scene, camera);
    frameId = window.requestAnimationFrame(animate);
  };
  animate();
}

onMounted(init);
watch(() => props.sceneKey, buildScene);

onBeforeUnmount(() => {
  window.cancelAnimationFrame(frameId);
  if (renderer) renderer.dispose();
});
</script>

<template>
  <div ref="sceneRef" class="hover-scene-canvas"></div>
</template>