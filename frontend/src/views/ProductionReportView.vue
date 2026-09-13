<template>
  <div>
    <!-- Page Header -->
    <div class="page-header">
      <div>
        <h1 class="page-title">Production Summary & Variance Report</h1>
        <p class="page-description">
          Tracking Planned vs. Actual Production, Output Variance, and Target Achievement %.
        </p>
      </div>
      <div style="display: flex; gap: 0.75rem;">
        <button class="btn btn-secondary" @click="exportToCSV">
          <span>📥 Export CSV</span>
        </button>
        <button class="btn btn-secondary" @click="printReport">
          <span>🖨️ Print</span>
        </button>
        <button class="btn btn-primary" @click="loadReport" :disabled="loading">
          <span>🔄 {{ loading ? 'Refreshing...' : 'Refresh Data' }}</span>
        </button>
      </div>
    </div>

    <!-- KPI Summary Cards (Expected Outcome) -->
    <div class="stat-grid">
      <!-- 1. Planned Quantity -->
      <div class="stat-card">
        <span class="stat-label">Total Planned Quantity</span>
        <span class="stat-value" style="color: var(--primary);">
          {{ totalPlanned.toLocaleString() }}
        </span>
        <span class="stat-desc">Cumulative target units across all products</span>
      </div>

      <!-- 2. Produced Quantity -->
      <div class="stat-card">
        <span class="stat-label">Total Produced Quantity</span>
        <span class="stat-value" style="color: var(--success);">
          {{ totalProduced.toLocaleString() }}
        </span>
        <span class="stat-desc">Actual manufactured volume to date</span>
      </div>

      <!-- 3. Difference (Variance) -->
      <div class="stat-card">
        <span class="stat-label">Total Difference</span>
        <span
          class="stat-value"
          :style="{ color: totalDifference >= 0 ? 'var(--success)' : 'var(--danger)' }"
        >
          {{ totalDifference > 0 ? '+' : '' }}{{ totalDifference.toLocaleString() }}
        </span>
        <span class="stat-desc">Produced Quantity minus Planned Quantity</span>
      </div>

      <!-- 4. Overall Achievement % -->
      <div class="stat-card">
        <span class="stat-label">Overall Achievement %</span>
        <span
          class="stat-value"
          :style="{ color: overallAchievement >= 100 ? 'var(--success)' : (overallAchievement >= 80 ? 'var(--warning)' : 'var(--danger)') }"
        >
          {{ overallAchievement.toFixed(1) }}%
        </span>
        <div class="progress-bar-bg">
          <div
            class="progress-bar-fill"
            :class="{
              'progress-success': overallAchievement >= 100,
              'progress-warning': overallAchievement >= 80 && overallAchievement < 100,
              'progress-danger': overallAchievement < 80
            }"
            :style="{ width: Math.min(overallAchievement, 100) + '%' }"
          ></div>
        </div>
      </div>
    </div>

    <!-- Filters & Search Toolbar -->
    <div class="card" style="padding: 1rem; margin-bottom: 1.5rem;">
      <div style="display: flex; gap: 1rem; align-items: center; justify-content: space-between; flex-wrap: wrap;">
        <div style="display: flex; gap: 0.5rem; align-items: center; flex-wrap: wrap;">
          <span style="font-size: 0.85rem; font-weight: 600; color: var(--text-muted);">Status Filter:</span>
          <button
            class="btn btn-sm"
            :class="selectedStatus === 'ALL' ? 'btn-primary' : 'btn-secondary'"
            @click="selectedStatus = 'ALL'"
          >
            All ({{ reportItems.length }})
          </button>
          <button
            class="btn btn-sm"
            :class="selectedStatus === 'ACHIEVED' ? 'btn-primary' : 'btn-secondary'"
            @click="selectedStatus = 'ACHIEVED'"
          >
            Achieved ({{ countStatus('ACHIEVED') }})
          </button>
          <button
            class="btn btn-sm"
            :class="selectedStatus === 'ON_TRACK' ? 'btn-primary' : 'btn-secondary'"
            @click="selectedStatus = 'ON_TRACK'"
          >
            On Track ({{ countStatus('ON_TRACK') }})
          </button>
          <button
            class="btn btn-sm"
            :class="selectedStatus === 'LAGGING' ? 'btn-primary' : 'btn-secondary'"
            @click="selectedStatus = 'LAGGING'"
          >
            Lagging ({{ countStatus('LAGGING') }})
          </button>
        </div>

        <div style="min-width: 260px;">
          <input
            v-model="searchQuery"
            type="text"
            class="form-control"
            placeholder="🔍 Filter product name or code..."
          />
        </div>
      </div>
    </div>

    <!-- Variance Analysis Table -->
    <div class="table-container">
      <table class="data-table">
        <thead>
          <tr>
            <th>Product Code</th>
            <th>Product Name</th>
            <th>Category</th>
            <th style="text-align: right;">Planned Qty</th>
            <th style="text-align: right;">Produced Qty</th>
            <th style="text-align: right;">Difference</th>
            <th style="min-width: 170px;">Achievement %</th>
            <th style="text-align: center;">Status</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="filteredItems.length === 0">
            <td colspan="8" class="empty-state">
              No variance records match your current filters.
            </td>
          </tr>
          <tr v-for="item in filteredItems" :key="item.productId">
            <td>
              <span class="badge badge-primary">{{ item.productCode }}</span>
            </td>
            <td>
              <strong>{{ item.productName }}</strong>
              <div style="font-size: 0.75rem; color: var(--text-muted);">Unit: {{ item.unitOfMeasure }}</div>
            </td>
            <td>
              <span class="badge" style="background: #f1f5f9; color: #475569;">{{ item.category }}</span>
            </td>
            <td style="text-align: right; font-weight: 600;">
              {{ item.plannedQuantity.toLocaleString() }}
            </td>
            <td style="text-align: right; font-weight: 700; color: var(--success);">
              {{ item.producedQuantity.toLocaleString() }}
            </td>
            <td
              style="text-align: right; font-weight: 700;"
              :style="{ color: item.difference >= 0 ? 'var(--success)' : 'var(--danger)' }"
            >
              {{ item.difference > 0 ? '+' : '' }}{{ item.difference.toLocaleString() }}
            </td>
            <td>
              <div style="display: flex; justify-content: space-between; font-weight: 600; font-size: 0.85rem;">
                <span>{{ item.achievementPercentage.toFixed(1) }}%</span>
              </div>
              <div class="progress-bar-bg">
                <div
                  class="progress-bar-fill"
                  :class="{
                    'progress-success': item.achievementPercentage >= 100,
                    'progress-warning': item.achievementPercentage >= 80 && item.achievementPercentage < 100,
                    'progress-danger': item.achievementPercentage < 80
                  }"
                  :style="{ width: Math.min(item.achievementPercentage, 100) + '%' }"
                ></div>
              </div>
            </td>
            <td style="text-align: center;">
              <span
                class="badge"
                :class="{
                  'badge-success': item.status === 'ACHIEVED',
                  'badge-warning': item.status === 'ON_TRACK',
                  'badge-danger': item.status === 'LAGGING'
                }"
              >
                {{ formatStatus(item.status) }}
              </span>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- Mathematical Formulas Reference Card -->
    <div class="card" style="margin-top: 2rem; background: #fafafa;">
      <h3 style="font-size: 1rem; margin-bottom: 0.75rem; color: var(--text-main);">
        📐 Calculation Logic & Formulas Used:
      </h3>
      <div style="display: grid; grid-template-columns: repeat(auto-fit, minmax(280px, 1fr)); gap: 1rem; font-size: 0.875rem;">
        <div style="background: white; padding: 0.85rem; border-radius: var(--radius-sm); border: 1px solid var(--border);">
          <strong>1. Difference (Variance):</strong>
          <p style="margin-top: 0.25rem; font-family: monospace; color: var(--primary);">Difference = Produced Quantity - Planned Quantity</p>
          <small style="color: var(--text-muted);">Positive means ahead of target; negative means production deficit.</small>
        </div>
        <div style="background: white; padding: 0.85rem; border-radius: var(--radius-sm); border: 1px solid var(--border);">
          <strong>2. Achievement %:</strong>
          <p style="margin-top: 0.25rem; font-family: monospace; color: var(--primary);">Achievement % = (Produced Qty / Planned Qty) × 100</p>
          <small style="color: var(--text-muted);">≥ 100% = Achieved | 80% to 99% = On Track | &lt; 80% = Lagging</small>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { api } from '../services/api';

const reportItems = ref([]);
const loading = ref(false);
const selectedStatus = ref('ALL');
const searchQuery = ref('');

const loadReport = async () => {
  loading.value = true;
  try {
    const data = await api.getVarianceReport();
    reportItems.value = data || [];
  } catch (err) {
    console.error('Failed to load variance report:', err);
  } finally {
    loading.value = false;
  }
};

const totalPlanned = computed(() => {
  return reportItems.value.reduce((sum, item) => sum + (Number(item.plannedQuantity) || 0), 0);
});

const totalProduced = computed(() => {
  return reportItems.value.reduce((sum, item) => sum + (Number(item.producedQuantity) || 0), 0);
});

const totalDifference = computed(() => {
  return totalProduced.value - totalPlanned.value;
});

const overallAchievement = computed(() => {
  if (totalPlanned.value === 0) return 0;
  return (totalProduced.value / totalPlanned.value) * 100;
});

const countStatus = (status) => {
  return reportItems.value.filter(i => i.status === status).length;
};

const formatStatus = (status) => {
  if (status === 'ACHIEVED') return '● Achieved';
  if (status === 'ON_TRACK') return '◐ On Track';
  if (status === 'LAGGING') return '○ Lagging';
  return status;
};

const filteredItems = computed(() => {
  let list = reportItems.value;

  if (selectedStatus.value !== 'ALL') {
    list = list.filter(i => i.status === selectedStatus.value);
  }

  if (searchQuery.value.trim()) {
    const q = searchQuery.value.toLowerCase();
    list = list.filter(i =>
      (i.productName && i.productName.toLowerCase().includes(q)) ||
      (i.productCode && i.productCode.toLowerCase().includes(q)) ||
      (i.category && i.category.toLowerCase().includes(q))
    );
  }

  return list;
});

const exportToCSV = () => {
  const headers = ['Product Code', 'Product Name', 'Category', 'Unit', 'Planned Quantity', 'Produced Quantity', 'Difference', 'Achievement %', 'Status'];
  const rows = reportItems.value.map(i => [
    i.productCode,
    `"${i.productName}"`,
    `"${i.category}"`,
    i.unitOfMeasure,
    i.plannedQuantity,
    i.producedQuantity,
    i.difference,
    `${i.achievementPercentage}%`,
    i.status
  ]);

  const csvContent = 'data:text/csv;charset=utf-8,' + [headers.join(','), ...rows.map(r => r.join(','))].join('\n');
  const encodedUri = encodeURI(csvContent);
  const link = document.createElement('a');
  link.setAttribute('href', encodedUri);
  link.setAttribute('download', `Production_Variance_Report_${new Date().toISOString().split('T')[0]}.csv`);
  document.body.appendChild(link);
  link.click();
  document.body.removeChild(link);
};

const printReport = () => {
  window.print();
};

onMounted(() => {
  loadReport();
});
</script>
