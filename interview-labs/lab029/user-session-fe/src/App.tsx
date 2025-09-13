import React from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { ConfigProvider } from 'antd';
import zhCN from 'antd/locale/zh_CN';
import LoginPage from '@pages/LoginPage';
import HomePage from '@pages/HomePage';
import { isLoggedIn } from '@utils/auth';
import 'antd/dist/reset.css';

/**
 * 受保护的路由组件
 */
interface ProtectedRouteProps {
  children: React.ReactNode;
}

const ProtectedRoute: React.FC<ProtectedRouteProps> = ({ children }) => {
  return isLoggedIn() ? <>{children}</> : <Navigate to="/login" replace />;
};

/**
 * 主应用组件
 */
const App: React.FC = () => {
  return (
    <ConfigProvider locale={zhCN}>
      <Router>
        <div className="App">
          <Routes>
            {/* 登录页面 */}
            <Route path="/login" element={<LoginPage />} />
            
            {/* 首页（受保护） */}
            <Route 
              path="/" 
              element={
                <ProtectedRoute>
                  <HomePage />
                </ProtectedRoute>
              } 
            />
            
            {/* 默认重定向到首页 */}
            <Route path="*" element={<Navigate to="/" replace />} />
          </Routes>
        </div>
      </Router>
    </ConfigProvider>
  );
};

export default App;