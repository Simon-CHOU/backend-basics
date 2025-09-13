import api from './api';
import {
  LoginRequest,
  LoginResponse,
  UserInfo,
  UserPermissions,
  UserProfile,
  HealthResponse,
} from '@types/user';

/**
 * 用户相关API服务
 * 
 * 提供所有用户相关的API调用方法
 */

export class UserService {
  
  /**
   * 用户登录
   * @param loginData 登录数据
   * @returns 登录响应
   */
  static async login(loginData: LoginRequest): Promise<LoginResponse> {
    try {
      const response = await api.post<LoginResponse>('/auth/login', loginData);
      return response;
    } catch (error) {
      console.error('登录失败:', error);
      throw error;
    }
  }
  
  /**
   * 用户注销
   * @returns 注销结果
   */
  static async logout(): Promise<string> {
    try {
      const response = await api.post<string>('/auth/logout');
      return response;
    } catch (error) {
      console.error('注销失败:', error);
      throw error;
    }
  }
  
  /**
   * 验证token有效性
   * @returns 验证结果
   */
  static async validateToken(): Promise<string> {
    try {
      const response = await api.get<string>('/auth/validate');
      return response;
    } catch (error) {
      console.error('Token验证失败:', error);
      throw error;
    }
  }
  
  /**
   * 获取当前用户完整信息
   * @returns 用户信息
   */
  static async getCurrentUser(): Promise<UserInfo> {
    try {
      const response = await api.get<UserInfo>('/user/current');
      return response;
    } catch (error) {
      console.error('获取用户信息失败:', error);
      throw error;
    }
  }
  
  /**
   * 获取当前用户ID
   * @returns 用户ID
   */
  static async getCurrentUserId(): Promise<{ userId: string }> {
    try {
      const response = await api.get<{ userId: string }>('/user/id');
      return response;
    } catch (error) {
      console.error('获取用户ID失败:', error);
      throw error;
    }
  }
  
  /**
   * 检查用户权限
   * @returns 权限信息
   */
  static async getUserPermissions(): Promise<UserPermissions> {
    try {
      const response = await api.get<UserPermissions>('/user/permissions');
      return response;
    } catch (error) {
      console.error('获取用户权限失败:', error);
      throw error;
    }
  }
  
  /**
   * 获取用户基本资料
   * @returns 用户基本资料
   */
  static async getUserProfile(): Promise<UserProfile> {
    try {
      const response = await api.get<UserProfile>('/user/profile');
      return response;
    } catch (error) {
      console.error('获取用户资料失败:', error);
      throw error;
    }
  }
  
  /**
   * 健康检查
   * @returns 系统健康状态
   */
  static async healthCheck(): Promise<HealthResponse> {
    try {
      const response = await api.get<HealthResponse>('/health');
      return response;
    } catch (error) {
      console.error('健康检查失败:', error);
      throw error;
    }
  }
  
  /**
   * 详细健康检查
   * @returns 详细系统健康状态
   */
  static async detailedHealthCheck(): Promise<any> {
    try {
      const response = await api.get<any>('/health/detailed');
      return response;
    } catch (error) {
      console.error('详细健康检查失败:', error);
      throw error;
    }
  }
}

// 导出默认实例
export default UserService;