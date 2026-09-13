import { createRouter, createWebHistory } from 'vue-router';
import LoginView from '../views/LoginView.vue';
import ProductMasterView from '../views/ProductMasterView.vue';
import ProductionPlanningView from '../views/ProductionPlanningView.vue';
import ProductionEntryView from '../views/ProductionEntryView.vue';
import ProductionReportView from '../views/ProductionReportView.vue';

const routes = [
  { path: '/login', name: 'Login', component: LoginView },
  { path: '/products', name: 'ProductMaster', component: ProductMasterView, meta: { requiresAuth: true } },
  { path: '/planning', name: 'ProductionPlanning', component: ProductionPlanningView, meta: { requiresAuth: true } },
  { path: '/production', name: 'ProductionEntry', component: ProductionEntryView, meta: { requiresAuth: true } },
  { path: '/reports', name: 'ProductionReport', component: ProductionReportView, meta: { requiresAuth: true } },
  { path: '/', redirect: '/reports' },
  { path: '/:pathMatch(.*)*', redirect: '/reports' }
];

const router = createRouter({
  history: createWebHistory(),
  routes
});

router.beforeEach((to, from, next) => {
  const user = localStorage.getItem('user');
  if (to.meta.requiresAuth && !user) {
    next('/login');
  } else if (to.path === '/login' && user) {
    next('/reports');
  } else {
    next();
  }
});

export default router;
