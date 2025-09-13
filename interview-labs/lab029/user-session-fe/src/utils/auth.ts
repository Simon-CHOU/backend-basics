import Cookies from 'js-cookie';

/**
 * 认证相关工具函数
 */

// Token的Cookie键名
const TOKEN_KEY = 'token';

/**
 * 获取存储的token
 * @returns token字符串或null
 */
export const getToken = (): string | null => {
  return Cookies.get(TOKEN_KEY) || null;
};

/**
 * 设置token到Cookie
 * @param token token字符串
 * @param expires 过期时间（天数），默认1天
 */
export const setToken = (token: string, expires: number = 1): void => {
  Cookies.set(TOKEN_KEY, token, {
    expires,
    path: '/',
    secure: false, // 开发环境设为false，生产环境应设为true
    sameSite: 'lax'
  });
};

/**
 * 移除token
 */
export const removeToken = (): void => {
  Cookies.remove(TOKEN_KEY, { path: '/' });
};

/**
 * 检查是否已登录（有token）
 * @returns 是否已登录
 */
export const isLoggedIn = (): boolean => {
  const token = getToken();
  return token !== null && token.trim() !== '';
};

/**
 * 清除所有认证信息
 */
export const clearAuth = (): void => {
  removeToken();
  // 可以在这里清除其他认证相关的本地存储
};

/**
 * 跳转到登录页
 */
export const redirectToLogin = (): void => {
  // 清除认证信息
  clearAuth();
  
  // 跳转到登录页
  window.location.href = '/login';
};

/**
 * 跳转到首页
 */
export const redirectToHome = (): void => {
  window.location.href = '/';
};

/**
 * 格式化用户显示名称
 * @param username 用户名
 * @param displayName 显示名称
 * @returns 格式化后的显示名称
 */
export const formatUserDisplayName = (username: string, displayName?: string): string => {
  if (displayName && displayName.trim() !== '') {
    return displayName;
  }
  return username;
};

/**
 * 验证用户名格式
 * @param username 用户名
 * @returns 是否有效
 */
export const validateUsername = (username: string): boolean => {
  if (!username || username.trim() === '') {
    return false;
  }
  
  // 用户名长度限制：3-20个字符
  if (username.length < 3 || username.length > 20) {
    return false;
  }
  
  // 用户名只能包含字母、数字、下划线
  const usernameRegex = /^[a-zA-Z0-9_]+$/;
  return usernameRegex.test(username);
};

/**
 * 验证密码格式
 * @param password 密码
 * @returns 是否有效
 */
export const validatePassword = (password: string): boolean => {
  if (!password || password.trim() === '') {
    return false;
  }
  
  // 密码长度限制：6-50个字符
  if (password.length < 6 || password.length > 50) {
    return false;
  }
  
  return true;
};

/**
 * 获取密码强度
 * @param password 密码
 * @returns 强度等级：weak, medium, strong
 */
export const getPasswordStrength = (password: string): 'weak' | 'medium' | 'strong' => {
  if (!password || password.length < 6) {
    return 'weak';
  }
  
  let score = 0;
  
  // 长度加分
  if (password.length >= 8) score += 1;
  if (password.length >= 12) score += 1;
  
  // 包含小写字母
  if (/[a-z]/.test(password)) score += 1;
  
  // 包含大写字母
  if (/[A-Z]/.test(password)) score += 1;
  
  // 包含数字
  if (/\d/.test(password)) score += 1;
  
  // 包含特殊字符
  if (/[^a-zA-Z0-9]/.test(password)) score += 1;
  
  if (score <= 2) return 'weak';
  if (score <= 4) return 'medium';
  return 'strong';
};