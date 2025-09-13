import React, { useState, useEffect } from 'react';
import { Card, Descriptions, Button, Space, message, Tag, Spin, Alert } from 'antd';
import { UserOutlined, LogoutOutlined, ReloadOutlined, SafetyCertificateOutlined } from '@ant-design/icons';
import { UserInfo, UserPermissions, UserProfile as UserProfileType } from '@types/user';
import UserService from '@services/userService';
import { clearAuth, formatUserDisplayName } from '@utils/auth';

/**
 * 用户资料展示组件
 */
interface UserProfileProps {
  onLogout?: () => void;
}

const UserProfile: React.FC<UserProfileProps> = ({ onLogout }) => {
  const [userInfo, setUserInfo] = useState<UserInfo | null>(null);
  const [userPermissions, setUserPermissions] = useState<UserPermissions | null>(null);
  const [userProfile, setUserProfile] = useState<UserProfileType | null>(null);
  const [loading, setLoading] = useState(true);
  const [refreshing, setRefreshing] = useState(false);
  const [error, setError] = useState<string | null>(null);

  /**
   * 加载用户数据
   */
  const loadUserData = async () => {
    try {
      setError(null);
      
      // 并行加载用户数据
      const [userInfoRes, permissionsRes, profileRes] = await Promise.allSettled([
        UserService.getCurrentUser(),
        UserService.getUserPermissions(),
        UserService.getUserProfile()
      ]);
      
      // 处理用户信息
      if (userInfoRes.status === 'fulfilled') {
        setUserInfo(userInfoRes.value);
      } else {
        console.error('获取用户信息失败:', userInfoRes.reason);
      }
      
      // 处理权限信息
      if (permissionsRes.status === 'fulfilled') {
        setUserPermissions(permissionsRes.value);
      } else {
        console.error('获取权限信息失败:', permissionsRes.reason);
      }
      
      // 处理用户资料
      if (profileRes.status === 'fulfilled') {
        setUserProfile(profileRes.value);
      } else {
        console.error('获取用户资料失败:', profileRes.reason);
      }
      
    } catch (error: any) {
      console.error('加载用户数据失败:', error);
      setError(error.message || '加载用户数据失败');
    } finally {
      setLoading(false);
      setRefreshing(false);
    }
  };

  /**
   * 刷新用户数据
   */
  const handleRefresh = async () => {
    setRefreshing(true);
    await loadUserData();
    message.success('数据已刷新');
  };

  /**
   * 处理注销
   */
  const handleLogout = async () => {
    try {
      await UserService.logout();
      clearAuth();
      message.success('注销成功');
      
      if (onLogout) {
        onLogout();
      } else {
        window.location.href = '/login';
      }
    } catch (error: any) {
      console.error('注销失败:', error);
      // 即使注销API失败，也清除本地认证信息
      clearAuth();
      message.warning('注销请求失败，但已清除本地登录状态');
      
      if (onLogout) {
        onLogout();
      } else {
        window.location.href = '/login';
      }
    }
  };

  // 组件挂载时加载数据
  useEffect(() => {
    loadUserData();
  }, []);

  // 加载中状态
  if (loading) {
    return (
      <div style={{ textAlign: 'center', padding: '50px' }}>
        <Spin size="large" />
        <div style={{ marginTop: '16px' }}>加载用户信息中...</div>
      </div>
    );
  }

  // 错误状态
  if (error && !userInfo) {
    return (
      <Alert
        message="加载失败"
        description={error}
        type="error"
        showIcon
        action={
          <Button size="small" onClick={handleRefresh}>
            重试
          </Button>
        }
      />
    );
  }

  return (
    <div style={{ maxWidth: '800px', margin: '0 auto', padding: '24px' }}>
      {/* 页面标题和操作按钮 */}
      <div style={{ 
        display: 'flex', 
        justifyContent: 'space-between', 
        alignItems: 'center', 
        marginBottom: '24px' 
      }}>
        <h1 style={{ margin: 0, display: 'flex', alignItems: 'center', gap: '8px' }}>
          <UserOutlined />
          用户信息
        </h1>
        <Space>
          <Button 
            icon={<ReloadOutlined />} 
            onClick={handleRefresh}
            loading={refreshing}
          >
            刷新
          </Button>
          <Button 
            type="primary" 
            danger 
            icon={<LogoutOutlined />} 
            onClick={handleLogout}
          >
            注销
          </Button>
        </Space>
      </div>

      <Space direction="vertical" size="large" style={{ width: '100%' }}>
        {/* 基本信息 */}
        {userInfo && (
          <Card title="基本信息" size="small">
            <Descriptions column={2} size="small">
              <Descriptions.Item label="用户ID">{userInfo.userId}</Descriptions.Item>
              <Descriptions.Item label="用户名">{userInfo.username}</Descriptions.Item>
              <Descriptions.Item label="显示名称">
                {formatUserDisplayName(userInfo.username, userInfo.displayName)}
              </Descriptions.Item>
              <Descriptions.Item label="邮箱">{userInfo.email || '未设置'}</Descriptions.Item>
              <Descriptions.Item label="角色">
                <Tag color="blue">{userInfo.role}</Tag>
              </Descriptions.Item>
              <Descriptions.Item label="状态">
                <Tag color={userInfo.active ? 'green' : 'red'}>
                  {userInfo.active ? '活跃' : '禁用'}
                </Tag>
              </Descriptions.Item>
            </Descriptions>
          </Card>
        )}

        {/* 权限信息 */}
        {userPermissions && (
          <Card 
            title={
              <span>
                <SafetyCertificateOutlined style={{ marginRight: '8px' }} />
                权限信息
              </span>
            } 
            size="small"
          >
            <Descriptions column={1} size="small">
              <Descriptions.Item label="管理员权限">
                <Tag color={userPermissions.isAdmin ? 'green' : 'default'}>
                  {userPermissions.isAdmin ? '是' : '否'}
                </Tag>
              </Descriptions.Item>
              <Descriptions.Item label="可访问资源">
                <div>
                  {userPermissions.permissions.map((permission, index) => (
                    <Tag key={index} color="blue" style={{ marginBottom: '4px' }}>
                      {permission}
                    </Tag>
                  ))}
                </div>
              </Descriptions.Item>
            </Descriptions>
          </Card>
        )}

        {/* 详细资料 */}
        {userProfile && (
          <Card title="详细资料" size="small">
            <Descriptions column={2} size="small">
              <Descriptions.Item label="姓名">{userProfile.fullName || '未设置'}</Descriptions.Item>
              <Descriptions.Item label="部门">{userProfile.department || '未设置'}</Descriptions.Item>
              <Descriptions.Item label="职位">{userProfile.position || '未设置'}</Descriptions.Item>
              <Descriptions.Item label="电话">{userProfile.phone || '未设置'}</Descriptions.Item>
              <Descriptions.Item label="地址" span={2}>
                {userProfile.address || '未设置'}
              </Descriptions.Item>
              <Descriptions.Item label="个人简介" span={2}>
                {userProfile.bio || '未设置'}
              </Descriptions.Item>
            </Descriptions>
          </Card>
        )}

        {/* 会话信息 */}
        {userInfo && (
          <Card title="会话信息" size="small">
            <Descriptions column={2} size="small">
              <Descriptions.Item label="会话ID">{userInfo.sessionId}</Descriptions.Item>
              <Descriptions.Item label="登录时间">
                {new Date(userInfo.loginTime).toLocaleString()}
              </Descriptions.Item>
              <Descriptions.Item label="最后活动">
                {new Date(userInfo.lastAccessTime).toLocaleString()}
              </Descriptions.Item>
              <Descriptions.Item label="会话过期">
                {new Date(userInfo.sessionExpiry).toLocaleString()}
              </Descriptions.Item>
            </Descriptions>
          </Card>
        )}
      </Space>
    </div>
  );
};

export default UserProfile;