import React, { useState } from 'react';
import { Form, Input, Button, Card, message, Typography, Space } from 'antd';
import { UserOutlined, LockOutlined, LoginOutlined } from '@ant-design/icons';
import { LoginRequest } from '@types/user';
import UserService from '@services/userService';
import { setToken, validateUsername, validatePassword, getPasswordStrength } from '@utils/auth';

const { Title, Text } = Typography;

/**
 * 登录表单组件
 */
interface LoginFormProps {
  onLoginSuccess?: () => void;
}

const LoginForm: React.FC<LoginFormProps> = ({ onLoginSuccess }) => {
  const [form] = Form.useForm();
  const [loading, setLoading] = useState(false);
  const [passwordStrength, setPasswordStrength] = useState<'weak' | 'medium' | 'strong'>('weak');

  /**
   * 处理登录提交
   */
  const handleLogin = async (values: LoginRequest) => {
    setLoading(true);
    
    try {
      console.log('开始登录:', values);
      
      // 调用登录API
      const response = await UserService.login(values);
      
      console.log('登录响应:', response);
      
      // 保存token到Cookie
      if (response.token) {
        setToken(response.token, 1); // 1天过期
        message.success(`登录成功！欢迎 ${response.user.displayName || response.user.username}`);
        
        // 调用成功回调
        if (onLoginSuccess) {
          onLoginSuccess();
        } else {
          // 默认跳转到首页
          window.location.href = '/';
        }
      } else {
        message.error('登录失败：未收到有效token');
      }
    } catch (error: any) {
      console.error('登录失败:', error);
      
      // 显示错误信息
      const errorMessage = error.response?.data?.message || 
                          error.response?.data?.error || 
                          error.message || 
                          '登录失败，请稍后重试';
      message.error(errorMessage);
    } finally {
      setLoading(false);
    }
  };

  /**
   * 处理密码变化
   */
  const handlePasswordChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const password = e.target.value;
    setPasswordStrength(getPasswordStrength(password));
  };

  /**
   * 获取密码强度颜色
   */
  const getPasswordStrengthColor = () => {
    switch (passwordStrength) {
      case 'weak': return '#ff4d4f';
      case 'medium': return '#faad14';
      case 'strong': return '#52c41a';
      default: return '#d9d9d9';
    }
  };

  /**
   * 快速填充测试账号
   */
  const fillTestAccount = () => {
    form.setFieldsValue({
      username: 'admin',
      password: 'admin123'
    });
    setPasswordStrength(getPasswordStrength('admin123'));
  };

  return (
    <div style={{
      display: 'flex',
      justifyContent: 'center',
      alignItems: 'center',
      minHeight: '100vh',
      background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)'
    }}>
      <Card 
        style={{ 
          width: 400, 
          boxShadow: '0 8px 32px rgba(0,0,0,0.1)',
          borderRadius: '12px'
        }}
        bodyStyle={{ padding: '32px' }}
      >
        <div style={{ textAlign: 'center', marginBottom: '32px' }}>
          <LoginOutlined style={{ fontSize: '48px', color: '#1890ff', marginBottom: '16px' }} />
          <Title level={2} style={{ margin: 0, color: '#262626' }}>
            用户登录
          </Title>
          <Text type="secondary">
            请输入您的用户名和密码
          </Text>
        </div>

        <Form
          form={form}
          name="login"
          onFinish={handleLogin}
          autoComplete="off"
          size="large"
        >
          <Form.Item
            name="username"
            rules={[
              { required: true, message: '请输入用户名' },
              {
                validator: (_, value) => {
                  if (!value || validateUsername(value)) {
                    return Promise.resolve();
                  }
                  return Promise.reject(new Error('用户名格式不正确（3-20个字符，只能包含字母、数字、下划线）'));
                }
              }
            ]}
          >
            <Input
              prefix={<UserOutlined />}
              placeholder="用户名"
              autoComplete="username"
            />
          </Form.Item>

          <Form.Item
            name="password"
            rules={[
              { required: true, message: '请输入密码' },
              {
                validator: (_, value) => {
                  if (!value || validatePassword(value)) {
                    return Promise.resolve();
                  }
                  return Promise.reject(new Error('密码长度必须在6-50个字符之间'));
                }
              }
            ]}
          >
            <Input.Password
              prefix={<LockOutlined />}
              placeholder="密码"
              autoComplete="current-password"
              onChange={handlePasswordChange}
            />
          </Form.Item>

          {/* 密码强度指示器 */}
          <div style={{ marginBottom: '16px' }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
              <Text type="secondary" style={{ fontSize: '12px' }}>密码强度:</Text>
              <div style={{
                width: '60px',
                height: '4px',
                backgroundColor: '#f0f0f0',
                borderRadius: '2px',
                overflow: 'hidden'
              }}>
                <div style={{
                  width: passwordStrength === 'weak' ? '33%' : passwordStrength === 'medium' ? '66%' : '100%',
                  height: '100%',
                  backgroundColor: getPasswordStrengthColor(),
                  transition: 'all 0.3s ease'
                }} />
              </div>
              <Text style={{ fontSize: '12px', color: getPasswordStrengthColor() }}>
                {passwordStrength === 'weak' ? '弱' : passwordStrength === 'medium' ? '中' : '强'}
              </Text>
            </div>
          </div>

          <Form.Item>
            <Button
              type="primary"
              htmlType="submit"
              loading={loading}
              block
              style={{ height: '44px', fontSize: '16px' }}
            >
              {loading ? '登录中...' : '登录'}
            </Button>
          </Form.Item>
        </Form>

        {/* 测试账号快速填充 */}
        <div style={{ textAlign: 'center', marginTop: '16px' }}>
          <Space direction="vertical" size="small">
            <Text type="secondary" style={{ fontSize: '12px' }}>
              测试账号
            </Text>
            <Button 
              type="link" 
              size="small" 
              onClick={fillTestAccount}
              style={{ padding: 0, height: 'auto' }}
            >
              点击填充测试账号 (admin/admin123)
            </Button>
          </Space>
        </div>
      </Card>
    </div>
  );
};

export default LoginForm;