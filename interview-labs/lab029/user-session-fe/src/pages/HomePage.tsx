import React, { useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import UserProfile from '@components/UserProfile';
import { isLoggedIn } from '@utils/auth';

/**
 * 首页
 */
const HomePage: React.FC = () => {
  const navigate = useNavigate();

  // 检查登录状态
  useEffect(() => {
    if (!isLoggedIn()) {
      navigate('/login', { replace: true });
    }
  }, [navigate]);

  /**
   * 注销回调
   */
  const handleLogout = () => {
    navigate('/login', { replace: true });
  };

  // 如果未登录，不渲染内容（等待跳转）
  if (!isLoggedIn()) {
    return null;
  }

  return (
    <div>
      <UserProfile onLogout={handleLogout} />
    </div>
  );
};

export default HomePage;