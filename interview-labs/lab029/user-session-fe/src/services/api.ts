import axios, { AxiosInstance, AxiosRequestConfig, AxiosResponse } from 'axios';
import Cookies from 'js-cookie';
import { message } from 'antd';

/**
 * API服务基础配置
 * 
 * 功能：
 * 1. 配置axios实例
 * 2. 请求/响应拦截器
 * 3. 错误处理
 * 4. Token管理
 */

// API基础URL
const BASE_URL = import.meta.env.VITE_API_BASE_URL || '/api';

// 创建axios实例
const apiClient: AxiosInstance = axios.create({
  baseURL: BASE_URL,
  timeout: 10000,
  withCredentials: true, // 允许携带Cookie
  headers: {
    'Content-Type': 'application/json',
  },
});

// 请求拦截器
apiClient.interceptors.request.use(
  (config: AxiosRequestConfig) => {
    // 从Cookie获取token
    const token = Cookies.get('token');
    
    if (token && config.headers) {
      // 添加token到请求头
      config.headers['token'] = token;
      config.headers['Authorization'] = `Bearer ${token}`;
    }
    
    console.log('发送请求:', {
      method: config.method?.toUpperCase(),
      url: config.url,
      data: config.data,
      headers: config.headers,
    });
    
    return config;
  },
  (error) => {
    console.error('请求拦截器错误:', error);
    return Promise.reject(error);
  }
);

// 响应拦截器
apiClient.interceptors.response.use(
  (response: AxiosResponse) => {
    console.log('收到响应:', {
      status: response.status,
      url: response.config.url,
      data: response.data,
    });
    
    return response;
  },
  (error) => {
    console.error('响应拦截器错误:', error);
    
    // 处理不同的错误状态
    if (error.response) {
      const { status, data } = error.response;
      
      switch (status) {
        case 401:
          // 未授权，清除token并跳转到登录页
          Cookies.remove('token');
          message.error('登录已过期，请重新登录');
          
          // 如果不在登录页，则跳转到登录页
          if (window.location.pathname !== '/login') {
            window.location.href = '/login';
          }
          break;
          
        case 403:
          message.error('权限不足');
          break;
          
        case 404:
          message.error('请求的资源不存在');
          break;
          
        case 500:
          message.error('服务器内部错误');
          break;
          
        default:
          message.error(data?.message || data?.error || '请求失败');
      }
    } else if (error.request) {
      // 网络错误
      message.error('网络连接失败，请检查网络设置');
    } else {
      // 其他错误
      message.error('请求配置错误');
    }
    
    return Promise.reject(error);
  }
);

// 通用API请求方法
export const api = {
  // GET请求
  get: <T = any>(url: string, config?: AxiosRequestConfig): Promise<T> => {
    return apiClient.get(url, config).then(response => response.data);
  },
  
  // POST请求
  post: <T = any>(url: string, data?: any, config?: AxiosRequestConfig): Promise<T> => {
    return apiClient.post(url, data, config).then(response => response.data);
  },
  
  // PUT请求
  put: <T = any>(url: string, data?: any, config?: AxiosRequestConfig): Promise<T> => {
    return apiClient.put(url, data, config).then(response => response.data);
  },
  
  // DELETE请求
  delete: <T = any>(url: string, config?: AxiosRequestConfig): Promise<T> => {
    return apiClient.delete(url, config).then(response => response.data);
  },
};

// 导出axios实例（用于特殊情况）
export { apiClient };

// 导出默认实例
export default api;