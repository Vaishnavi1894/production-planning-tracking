<template>
  <div>
    <!-- Page Header -->
    <div class="page-header">
      <div>
        <h1 class="page-title">Production Planning</h1>
        <p class="page-description">Establish monthly and batch production target quantities for products.</p>
      </div>
      <button class="btn btn-primary" @click="openModal">
        <span>+ Create Production Plan</span>
      </button>
    </div>

    <!-- Summary Stats -->
    <div class="stat-grid">
      <div class="stat-card">
        <span class="stat-label">Total Active Plans</span>
        <span class="stat-value" style="color: var(--primary);">{{ plans.length }}</span>
        <span class="stat-desc">Scheduled production batches</span>
      </div>
      <div class="stat-card">
        <span class="stat-label">Total Planned Volume</span>
        <span class="stat-value" style="color: var(--text-main);">{{ totalPlannedQuantity.toLocaleString() }}</span>
        <span class="stat-desc">Cumulative planned units</span>
      </div>
    </div>

    <!-- Alert Notifications -->
    <div v-if="successMessage" class="alert alert-success">
      <span>✅</span> {{ successMessage }}
    </div>
    <div v-if="errorMessage" class="alert alert-danger">
      <span>⚠️</span> {{ errorMessage }}
    </div>

    <!-- Plans Table -->
    <div class="table-container">
      <table class="data-table">
        <thead>
          <tr>
            <th>Plan ID</th>
            <th>Product Name</th>
            <th>Target Period</th>
            <th>Planned Quantity</th>
            <th>Notes / Objective</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="plans.length === 0">
            <td colspan="5" class="empty-state">
              No production plans created yet. Click "+ Create Production Plan" to add a target.
            </td>
          </tr>
          <tr v-for="plan in plans" :key="plan.id">
            <td>
              <span class="badge badge-primary">#PLN-{{ plan.id }}</span>
            </td>
            <td><strong>{{ plan.productName || 'Product #' + plan.productId }}</strong></td>
            <td>{{ plan.planMonth }}</td>
            <td>
              <strong style="font-size: 1.05rem; color: var(--primary);">
                {{ plan.plannedQuantity.toLocaleString() }}
              </strong> units
            </td>
            <td style="color: var(--text-muted); font-size: 0.85rem;">{{ plan.notes || '—' }}</td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- Create Plan Modal -->
    <div v-if="showModal" class="modal-overlay" @click.self="closeModal">
      <div class="modal-content">
        <div class="modal-header">
          <h3 class="modal-title">Create Production Plan Target</h3>
          <button style="background:none; border:none; font-size: 1.25rem; cursor:pointer;" @click="closeModal">✕</button>
        </div>

        <form @submit.prevent="submitPlan">
          <div class="modal-body">
            <div class="form-group">
              <label class="form-label">Select Product *</label>
              <select v-model="newPlan.productId" class="form-control" required @change="onProductSelect">
                <option value="" disabled>-- Select a Product from Master --</option>
                <option v-for="p in products" :key="p.id" :value="p.id">
                  {{ p.productCode }} - {{ p.productName }}
                </option>
              </select>
            </div>

            <div class="form-grid-2">
              <div class="form-group">
                <label class="form-label">Target Period / Month *</label>
                <input
                  v-model="newPlan.planMonth"
                  type="month"
                  class="form-control"
                  required
                />
              </div>
              <div class="form-group">
                <label class="form-label">Planned Quantity *</label>
                <input
                  v-model.number="newPlan.plannedQuantity"
                  type="number"
                  min="1"
                  step="1"
                  class="form-control"
                  placeholder="e.g. 500"
                  required
                />
              </div>
            </div>

            <div class="form-group">
              <label class="form-label">Notes / Instructions (Optional)</label>
              <textarea
                v-model="newPlan.notes"
                class="form-control"
                rows="2"
                placeholder="e.g. High priority export order, inspect QA tolerances..."
              ></textarea>
            </div>
          </div>

          <div class="modal-footer">
            <button type="button" class="btn btn-secondary" @click="closeModal">Cancel</button>
            <button type="submit" class="btn btn-primary" :disabled="submitting">
              {{ submitting ? 'Saving Plan...' : 'Save Plan' }}
            </button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { api } from '../services/api';

const plans = ref([]);
const products = ref([]);
const showModal = ref(false);
const submitting = ref(false);
const successMessage = ref('');
const errorMessage = ref('');

const currentMonthStr = () => {
  const d = new Date();
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}`;
};

const newPlan = ref({
  productId: '',
  productName: '',
  planMonth: currentMonthStr(),
  plannedQuantity: 100,
  notes: ''
});

const totalPlannedQuantity = computed(() => {
  return plans.value.reduce((sum, p) => sum + (Number(p.plannedQuantity) || 0), 0);
});

const loadData = async () => {
  try {
    const [pData, plData] = await Promise.all([
      api.getProducts(),
      api.getPlans()
    ]);
    products.value = pData || [];
    plans.value = plData || [];
  } catch (err) {
    console.error('Failed to load plans or products:', err);
  }
};

const onProductSelect = () => {
  const selected = products.value.find(p => p.id === newPlan.value.productId);
  if (selected) {
    newPlan.value.productName = selected.productName;
  }
};

const openModal = () => {
  newPlan.value = {
    productId: products.value.length > 0 ? products.value[0].id : '',
    productName: products.value.length > 0 ? products.value[0].productName : '',
    planMonth: currentMonthStr(),
    plannedQuantity: 500,
    notes: ''
  };
  showModal.value = true;
};

const closeModal = () => {
  showModal.value = false;
};

const submitPlan = async () => {
  submitting.value = true;
  errorMessage.value = '';
  successMessage.value = '';

  try {
    const saved = await api.createPlan(newPlan.value);
    plans.value.push(saved);
    successMessage.value = `Production Plan for "${saved.productName}" created successfully!`;
    closeModal();
  } catch (err) {
    errorMessage.value = 'Failed to create plan: ' + (err.message || 'Server error');
  } finally {
    submitting.value = false;
  }
};

onMounted(() => {
  loadData();
});
</script>
