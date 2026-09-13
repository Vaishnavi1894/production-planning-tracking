<template>
  <div>
    <!-- Page Header -->
    <div class="page-header">
      <div>
        <h1 class="page-title">Daily Production Entry</h1>
        <p class="page-description">Log actual manufactured quantities per line, shift, and date.</p>
      </div>
      <button class="btn btn-primary" @click="openModal">
        <span>+ Record Production Entry</span>
      </button>
    </div>

    <!-- Summary Stats -->
    <div class="stat-grid">
      <div class="stat-card">
        <span class="stat-label">Total Daily Logs</span>
        <span class="stat-value" style="color: var(--primary);">{{ entries.length }}</span>
        <span class="stat-desc">Recorded production batches</span>
      </div>
      <div class="stat-card">
        <span class="stat-label">Total Produced Volume</span>
        <span class="stat-value" style="color: var(--success);">{{ totalProducedQuantity.toLocaleString() }}</span>
        <span class="stat-desc">Cumulative manufactured units</span>
      </div>
    </div>

    <!-- Alert Notifications -->
    <div v-if="successMessage" class="alert alert-success">
      <span>✅</span> {{ successMessage }}
    </div>
    <div v-if="errorMessage" class="alert alert-danger">
      <span>⚠️</span> {{ errorMessage }}
    </div>

    <!-- Entries Table -->
    <div class="table-container">
      <table class="data-table">
        <thead>
          <tr>
            <th>Entry ID</th>
            <th>Date</th>
            <th>Product Name</th>
            <th>Shift</th>
            <th>Produced Qty</th>
            <th>Remarks / Notes</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="entries.length === 0">
            <td colspan="6" class="empty-state">
              No daily production logs recorded yet. Click "+ Record Production Entry" to add one.
            </td>
          </tr>
          <tr v-for="entry in entries" :key="entry.id">
            <td>
              <span class="badge" style="background: #f1f5f9; color: #334155;">#LOG-{{ entry.id }}</span>
            </td>
            <td><strong>{{ entry.entryDate }}</strong></td>
            <td>{{ entry.productName || 'Product #' + entry.productId }}</td>
            <td>
              <span
                class="badge"
                :class="{
                  'badge-primary': entry.shift === 'Morning',
                  'badge-warning': entry.shift === 'Evening',
                  'badge-danger': entry.shift === 'Night'
                }"
              >
                {{ entry.shift }} Shift
              </span>
            </td>
            <td>
              <strong style="font-size: 1.05rem; color: var(--success);">
                +{{ entry.producedQuantity.toLocaleString() }}
              </strong> units
            </td>
            <td style="color: var(--text-muted); font-size: 0.85rem;">{{ entry.remarks || '—' }}</td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- Record Production Modal -->
    <div v-if="showModal" class="modal-overlay" @click.self="closeModal">
      <div class="modal-content">
        <div class="modal-header">
          <h3 class="modal-title">Record Daily Production</h3>
          <button style="background:none; border:none; font-size: 1.25rem; cursor:pointer;" @click="closeModal">✕</button>
        </div>

        <form @submit.prevent="submitEntry">
          <div class="modal-body">
            <div class="form-group">
              <label class="form-label">Select Product *</label>
              <select v-model="newEntry.productId" class="form-control" required @change="onProductSelect">
                <option value="" disabled>-- Select Product --</option>
                <option v-for="p in products" :key="p.id" :value="p.id">
                  {{ p.productCode }} - {{ p.productName }}
                </option>
              </select>
            </div>

            <div class="form-grid-2">
              <div class="form-group">
                <label class="form-label">Date *</label>
                <input
                  v-model="newEntry.entryDate"
                  type="date"
                  class="form-control"
                  required
                />
              </div>
              <div class="form-group">
                <label class="form-label">Shift *</label>
                <select v-model="newEntry.shift" class="form-control" required>
                  <option value="Morning">Morning Shift</option>
                  <option value="Evening">Evening Shift</option>
                  <option value="Night">Night Shift</option>
                </select>
              </div>
            </div>

            <div class="form-group">
              <label class="form-label">Actual Produced Quantity *</label>
              <input
                v-model.number="newEntry.producedQuantity"
                type="number"
                min="1"
                step="1"
                class="form-control"
                placeholder="e.g. 150"
                required
              />
            </div>

            <div class="form-group">
              <label class="form-label">Remarks / Operator Log (Optional)</label>
              <textarea
                v-model="newEntry.remarks"
                class="form-control"
                rows="2"
                placeholder="e.g. Line 2 optimal speed, zero quality defects..."
              ></textarea>
            </div>
          </div>

          <div class="modal-footer">
            <button type="button" class="btn btn-secondary" @click="closeModal">Cancel</button>
            <button type="submit" class="btn btn-primary" :disabled="submitting">
              {{ submitting ? 'Logging...' : 'Record Production' }}
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

const entries = ref([]);
const products = ref([]);
const showModal = ref(false);
const submitting = ref(false);
const successMessage = ref('');
const errorMessage = ref('');

const todayStr = () => new Date().toISOString().split('T')[0];

const newEntry = ref({
  productId: '',
  productName: '',
  entryDate: todayStr(),
  producedQuantity: 100,
  shift: 'Morning',
  remarks: ''
});

const totalProducedQuantity = computed(() => {
  return entries.value.reduce((sum, e) => sum + (Number(e.producedQuantity) || 0), 0);
});

const loadData = async () => {
  try {
    const [pData, eData] = await Promise.all([
      api.getProducts(),
      api.getProduction()
    ]);
    products.value = pData || [];
    entries.value = eData || [];
  } catch (err) {
    console.error('Failed to load production entries:', err);
  }
};

const onProductSelect = () => {
  const selected = products.value.find(p => p.id === newEntry.value.productId);
  if (selected) {
    newEntry.value.productName = selected.productName;
  }
};

const openModal = () => {
  newEntry.value = {
    productId: products.value.length > 0 ? products.value[0].id : '',
    productName: products.value.length > 0 ? products.value[0].productName : '',
    entryDate: todayStr(),
    producedQuantity: 150,
    shift: 'Morning',
    remarks: ''
  };
  showModal.value = true;
};

const closeModal = () => {
  showModal.value = false;
};

const submitEntry = async () => {
  submitting.value = true;
  errorMessage.value = '';
  successMessage.value = '';

  try {
    const saved = await api.recordProduction(newEntry.value);
    entries.value.unshift(saved); // add to top of list
    successMessage.value = `Logged ${saved.producedQuantity} units for "${saved.productName}" successfully!`;
    closeModal();
  } catch (err) {
    errorMessage.value = 'Failed to record entry: ' + (err.message || 'Server error');
  } finally {
    submitting.value = false;
  }
};

onMounted(() => {
  loadData();
});
</script>
