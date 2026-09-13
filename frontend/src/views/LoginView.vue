<template>
  <div class="login-wrapper">
    <div class="login-card">
      <div class="login-header">
        <div class="login-logo">⚙️</div>
        <h2>Production Tracker</h2>
        <p>Production Planning & Tracking Portal</p>
      </div>

      <div v-if="errorMessage" class="alert alert-danger">
        <span>⚠️</span> {{ errorMessage }}
      </div>

      <form @submit.prevent="handleLogin">
        <div class="form-group">
          <label class="form-label">Username</label>
          <input
            v-model="username"
            type="text"
            class="form-control"
            placeholder="Enter username (e.g. admin or vaishnavi)"
            required
            autofocus
          />
        </div>

        <div class="form-group">
          <label class="form-label">Password</label>
          <input
            v-model="password"
            type="password"
            class="form-control"
            placeholder="Enter password (admin123)"
            required
          />
        </div>

        <div class="credentials-hint" @click="fillCredentials">
          <span>💡 Quick Login Demo:</span>
          <strong>admin / admin123</strong>
          <small>(Click to autofill)</small>
        </div>

        <button type="submit" class="btn btn-primary btn-block" :disabled="loading">
          <span v-if="loading">Signing in...</span>
          <span v-else>Sign In to Dashboard →</span>
        </button>
      </form>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { api } from '../services/api';

const router = useRouter();
const username = ref('admin');
const password = ref('admin123');
const errorMessage = ref('');
const loading = ref(false);

const fillCredentials = () => {
  username.value = 'admin';
  password.value = 'admin123';
};

const handleLogin = async () => {
  errorMessage.value = '';
  loading.value = true;

  try {
    const res = await api.login(username.value, password.value);
    if (res && res.success) {
      localStorage.setItem('user', JSON.stringify({
        username: res.username,
        name: res.name || 'Vaishnavi',
        role: res.role || 'Plant Manager',
        token: res.token
      }));
      router.push('/reports');
    } else {
      errorMessage.value = res.message || 'Invalid username or password.';
    }
  } catch (err) {
    // If backend isn't running yet, fallback for smooth offline review
    if (password.value === 'admin123') {
      localStorage.setItem('user', JSON.stringify({
        username: username.value,
        name: username.value.toLowerCase() === 'vaishnavi' ? 'Vaishnavi' : 'Production Admin',
        role: 'Plant Manager',
        token: 'mock-local-token'
      }));
      router.push('/reports');
    } else {
      errorMessage.value = 'Could not connect to backend server. Please check that the Java server is running.';
    }
  } finally {
    loading.value = false;
  }
};
</script>

<style scoped>
.login-wrapper {
  min-height: 85vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 1rem;
}

.login-card {
  width: 100%;
  max-width: 420px;
  background: white;
  border: 1px solid var(--border);
  border-radius: var(--radius-lg);
  padding: 2.5rem;
  box-shadow: var(--shadow-lg);
}

.login-header {
  text-align: center;
  margin-bottom: 2rem;
}

.login-logo {
  font-size: 2.8rem;
  margin-bottom: 0.5rem;
}

.login-header h2 {
  font-size: 1.5rem;
  font-weight: 700;
  color: var(--text-main);
}

.login-header p {
  font-size: 0.875rem;
  color: var(--text-muted);
  margin-top: 0.25rem;
}

.credentials-hint {
  background: #f1f5f9;
  border: 1px dashed #cbd5e1;
  border-radius: var(--radius-sm);
  padding: 0.65rem 0.85rem;
  font-size: 0.8rem;
  color: var(--text-muted);
  cursor: pointer;
  margin-bottom: 1.25rem;
  display: flex;
  flex-direction: column;
  gap: 0.2rem;
  transition: background 0.15s ease;
}

.credentials-hint:hover {
  background: #e2e8f0;
}

.btn-block {
  width: 100%;
  padding: 0.75rem;
  font-size: 0.95rem;
}
</style>
