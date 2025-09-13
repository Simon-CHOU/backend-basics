/**
 * 用户相关类型定义
 */

// 登录请求
export interface LoginRequest {
  username: string;
  password: string;
}

// 用户基本信息
export interface UserBasicInfo {
  userId: string;
  username: string;
  displayName: string;
  email: string;
}

// 登录响应
export interface LoginResponse {
  success: boolean;
  message: string;
  token?: string;
  user?: UserBasicInfo;
}

// 完整用户信息
export interface UserInfo {
  userId: string;
  username: string;
  displayName: string;
  email: string;
  department: string;
  roles: string[];
  permissions: string[];
  loginTime: string;
  lastAccessTime: string;
}

// 用户权限检查结果
export interface UserPermissions {
  canRead: boolean;
  canWrite: boolean;
  canDelete: boolean;
  canAdmin: boolean;
  canManage: boolean;
  isAdmin: boolean;
  isUser: boolean;
  isManager: boolean;
  allPermissions: string[];
  allRoles: string[];
}

// 用户基本资料
export interface UserProfile {
  userId: string;
  username: string;
  displayName: string;
  email: string;
  department: string;
  isLoggedIn: boolean;
}

// API响应基础类型
export interface ApiResponse<T = any> {
  success?: boolean;
  message?: string;
  data?: T;
  error?: string;
}

// 健康检查响应
export interface HealthResponse {
  status: 'UP' | 'DOWN';
  timestamp: string;
  service: string;
  version: string;
  redis: 'UP' | 'DOWN';
  error?: string;
}