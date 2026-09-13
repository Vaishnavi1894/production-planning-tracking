<template>
  <div>
    <!-- Page Header -->
    <div class="page-header">
      <div>
        <h1 class="page-title">Product Master</h1>
        <p class="page-description">Maintain catalogue of manufactured parts, assemblies, and components.</p>
      </div>
      <button class="btn btn-primary" @click="openModal">
        <span>+ Add New Product</span>
      </button>
    </div>

    <!-- Search / Filter Bar -->
    <div class="card" style="padding: 1rem; margin-bottom: 1.25rem;">
      <div style="display: flex; gap: 1rem; align-items: center; flex-wrap: wrap;">
        <div style="flex: 1; min-width: 250px;">
          <input
            v-model="searchQuery"
            type="text"
            class="form-control"
            placeholder="🔍 Search by product name, code, or category..."
          />
        </div>
        <div style="font-size: 0.85rem; color: var(--text-muted);">
          Total Products: <strong>{{ products.length }}</strong>
        </div>
      </div>
    </div>

    <!-- Feedback Message -->
    <div v-if="successMessage" class="alert alert-success">
      <span>✅</span> {{ successMessage }}
    </div>
    <div v-if="errorMessage" class="alert alert-danger">
      <span>⚠️</span> {{ errorMessage }}
    </div>

    <!-- Products Table -->
    <div class="table-container">
      <table class="data-table">
        <thead>
          <tr>
            <th>ID</th>
            <th>Product Code</th>
            <th>Product Name</th>
            <th>Category</th>
            <th>Unit of Measure</th>
            <th>Description</th>
            <th style="text-align: right;">Action</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="filteredProducts.length === 0">
            <td colspan="7" class="empty-state">
              No products found. Click "+ Add New Product" to create one.
            </td>
          </tr>
          <tr v-for="p in filteredProducts" :key="p.id">
            <td>#{{ p.id }}</td>
            <td>
              <span class="badge badge-primary">{{ p.productCode }}</span>
            </td>
            <td><strong>{{ p.productName }}</strong></td>
            <td>
              <span class="badge" style="background: #f1f5f9; color: #475569;">{{ p.category }}</span>
            </td>
            <td>{{ p.unitOfMeasure }}</td>
            <td style="color: var(--text-muted); font-size: 0.85rem;">{{ p.description || '—' }}</td>
            <td style="text-align: right;">
              <button class="btn btn-secondary btn-sm" style="color: var(--danger);" @click="deleteProduct(p.id)">
                Delete
              </button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- Add Product Modal -->
    <div v-if="showModal" class="modal-overlay" @click.self="closeModal">
      <div class="modal-content">
        <div class="modal-header">
          <h3 class="modal-title">Add New Product</h3>
          <button style="background:none; border:none; font-size: 1.25rem; cursor:pointer;" @click="closeModal">✕</button>
        </div>

        <form @submit.prevent="submitProduct">
          <div class="modal-body">
            <div class="form-grid-2">
              <div class="form-group">
                <label class="form-label">Product Code *</label>
                <input
                  v-model="newProduct.productCode"
                  type="text"
                  class="form-control"
                  placeholder="e.g. PRD-105"
                  required
                />
              </div>
              <div class="form-group">
                <label class="form-label">Unit of Measure *</label>
                <select v-model="newProduct.unitOfMeasure" class="form-control" required>
                  <option value="Units">Units</option>
                  <option value="Pcs">Pcs</option>
                  <option value="Sets">Sets</option>
                  <option value="Kg">Kg</option>
                  <option value="Meters">Meters</option>
                </select>
              </div>
            </div>

            <div class="form-group">
              <label class="form-label">Product Name *</label>
              <input
                v-model="newProduct.productName"
                type="text"
                class="form-control"
                placeholder="e.g. Electric Motor Stator"
                required
              />
            </div>

            <div class="form-group">
              <label class="form-label">Category *</label>
              <input
                v-model="newProduct.category"
                type="text"
                class="form-control"
                placeholder="e.g. Electrical / Stamping"
                required
              />
            </div>

            <div class="form-group">
              <label class="form-label">Description (Optional)</label>
              <textarea
                v-model="newProduct.description"
                class="form-control"
                rows="2"
                placeholder="Brief notes regarding manufacturing process or specs..."
              ></textarea>
            </div>
          </div>

          <div class="modal-footer">
            <button type="button" class="btn btn-secondary" @click="closeModal">Cancel</button>
            <button type="submit" class="btn btn-primary" :disabled="submitting">
              {{ submitting ? 'Saving...' : 'Save Product' }}
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

const products = ref([]);
const searchQuery = ref('');
const showModal = ref(false);
const submitting = ref(false);
const successMessage = ref('');
const errorMessage = ref('');

const newProduct = ref({
  productCode: '',
  productName: '',
  category: '',
  unitOfMeasure: 'Units',
  description: ''
});

const loadProducts = async () => {
  try {
    const data = await api.getProducts();
    products.value = data || [];
  } catch (err) {
    console.error('Failed to load products:', err);
  }
};

const filteredProducts = computed(() => {
  if (!searchQuery.value.trim()) return products.value;
  const q = searchQuery.value.toLowerCase();
  return products.value.filter(p =>
    (p.productName && p.productName.toLowerCase().includes(q)) ||
    (p.productCode && p.productCode.toLowerCase().includes(q)) ||
    (p.category && p.category.toLowerCase().includes(q))
  );
});

const openModal = () => {
  newProduct.value = {
    productCode: 'PRD-' + Math.floor(100 + Math.random() * 900),
    productName: '',
    category: '',
    unitOfMeasure: 'Units',
    description: ''
  };
  showModal.value = true;
};

const closeModal = () => {
  showModal.value = false;
};

const submitProduct = async () => {
  submitting.value = true;
  errorMessage.value = '';
  successMessage.value = '';

  try {
    const saved = await api.createProduct(newProduct.value);
    products.value.push(saved);
    successMessage.value = `Product "${saved.productName}" added successfully!`;
    closeModal();
  } catch (err) {
    errorMessage.value = 'Failed to create product: ' + (err.message || 'Server error');
  } finally {
    submitting.value = false;
  }
};

const deleteProduct = async (id) => {
  if (!confirm('Are you sure you want to delete this product?')) return;
  try {
    await api.deleteProduct(id);
    products.value = products.value.filter(p => p.id !== id);
    successMessage.value = 'Product deleted successfully.';
  } catch (err) {
    errorMessage.value = 'Failed to delete product.';
  }
};

onMounted(() => {
  loadProducts();
});
</script>
