/**
 * API Service for Production Planning & Tracking System
 * Handles all HTTP communication between the Vue.js frontend and the Micronaut Java backend.
 */

const BASE_URL = '/api';

async function request(endpoint, options = {}) {
  const config = {
    headers: {
      'Content-Type': 'application/json',
      ...options.headers,
    },
    ...options,
  };

  try {
    const response = await fetch(`${BASE_URL}${endpoint}`, config);
    if (!response.ok) {
      const errData = await response.json().catch(() => ({}));
      throw new Error(errData.message || `HTTP Error ${response.status}: ${response.statusText}`);
    }
    // For 204 No Content
    if (response.status === 204) return null;
    return await response.json();
  } catch (error) {
    console.error(`API Error on [${options.method || 'GET'}] ${endpoint}:`, error);
    throw error;
  }
}

export const api = {
  // 1. Authentication
  login: async (username, password) => {
    return request('/auth/login', {
      method: 'POST',
      body: JSON.stringify({ username, password })
    });
  },

  // 2. Product Master APIs
  getProducts: async () => {
    return request('/products');
  },
  createProduct: async (product) => {
    return request('/products', {
      method: 'POST',
      body: JSON.stringify(product)
    });
  },
  deleteProduct: async (id) => {
    return request(`/products/${id}`, {
      method: 'DELETE'
    });
  },

  // 3. Production Planning APIs
  getPlans: async () => {
    return request('/plans');
  },
  createPlan: async (plan) => {
    return request('/plans', {
      method: 'POST',
      body: JSON.stringify(plan)
    });
  },

  // 4. Daily Production Logging APIs
  getProduction: async () => {
    return request('/production');
  },
  recordProduction: async (entry) => {
    return request('/production', {
      method: 'POST',
      body: JSON.stringify(entry)
    });
  },

  // 5. Variance Report API
  getVarianceReport: async () => {
    return request('/reports/variance');
  }
};
