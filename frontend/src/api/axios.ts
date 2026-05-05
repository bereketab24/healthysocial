import axios from 'axios';
import { keycloak } from '../auth/keycloak';

export const api = axios.create({
  baseURL: '/api',
});

api.interceptors.request.use(async (config) => {
  if (keycloak.token) {
    try {
      await keycloak.updateToken(30);
      config.headers.Authorization = `Bearer ${keycloak.token}`;
    } catch (err) {
      keycloak.login();
    }
  }
  return config;
});
