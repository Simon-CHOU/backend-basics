import React, { useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import LoginForm from '@components/LoginForm';
import { isLoggedIn } from '@utils/auth';

/**
 * 登录页面
 */
const LoginPage: React.FC = () => {
  const navigate = useNavigate();

  // 检查是否已登录，如果已登录则跳转到首页
  useEffect(() => {
    if (isLoggedIn()) {
      navigate('/', { replace: true });
    }
  }, [navigate]);

  /**
   * 登录成功回调
   */
  const handleLoginSuccess = () => {
    // 跳转到首页
    navigate('/', { replace: true });
  };

  return (
    <div>
      <LoginForm onLoginSuccess={handleLoginSuccess} />
    </div>
  );
};

export default LoginPage;