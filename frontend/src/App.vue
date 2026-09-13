<template>
  <div class="app-container">
    <!-- Top Navigation Bar (Shown only when logged in) -->
    <nav v-if="currentUser" class="navbar">
      <div class="nav-wrapper">
        <router-link to="/reports" class="brand">
          <div class="brand-icon">🏭</div>
          <div>
            <div class="brand-title">Production Tracker</div>
            <div class="brand-subtitle">Planning & Tracking System</div>
          </div>
        </router-link>

        <ul class="nav-links">
          <li>
            <router-link to="/products" class="nav-link" active-class="active">
              <span>📦</span> Product Master
            </router-link>
          </li>
          <li>
            <router-link to="/planning" class="nav-link" active-class="active">
              <span>📋</span> Production Planning
            </router-link>
          </li>
          <li>
            <router-link to="/production" class="nav-link" active-class="active">
              <span>⏱️</span> Daily Entry
            </router-link>
          </li>
          <li>
            <router-link to="/reports" class="nav-link" active-class="active">
              <span>📊</span> Variance Report
            </router-link>
          </li>
        </ul>

        <div class="user-badge">
          <div class="user-info">
            <div class="user-name">{{ currentUser.name || 'Vaishnavi' }}</div>
            <div class="user-role">{{ currentUser.role || 'Plant Manager' }}</div>
          </div>
          <button class="btn-logout" @click="handleLogout">
            Logout
          </button>
        </div>
      </div>
    </nav>

    <!-- Main Page Content -->
    <main class="main-content">
      <router-view />
    </main>
  </div>
</template>

<script setup>
import { ref, watch, onMounted } from 'vue';
import { useRouter, useRoute } from 'vue-router';

const router = useRouter();
const route = useRoute();
const currentUser = ref(null);

const checkUser = () => {
  const stored = localStorage.getItem('user');
  if (stored) {
    try {
      currentUser.value = JSON.parse(stored);
    } catch {
      currentUser.value = null;
    }
  } else {
    currentUser.value = null;
  }
};

const handleLogout = () => {
  localStorage.removeItem('user');
  currentUser.value = null;
  router.push('/login');
};

watch(() => route.path, () => {
  checkUser();
});

onMounted(() => {
  checkUser();
});
</script>
