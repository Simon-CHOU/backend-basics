import React from 'react';
import { Result, Button, Card, Descriptions, Typography, Tooltip } from 'antd';
import { useLocation, useNavigate } from 'react-router-dom';
import { InfoCircleOutlined } from '@ant-design/icons';
import { ApprovalResponse, ApprovalRequest } from '../types/approval';

const { Text } = Typography;

interface LocationState {
  result: ApprovalResponse;
  request: ApprovalRequest;
}

const ResultPage: React.FC = () => {
  const location = useLocation();
  const navigate = useNavigate();
  const state = location.state as LocationState;

  console.log('ResultPage state:', state); // Debug log

  if (!state) {
    return (
      <Result
        status="warning"
        title="No Result Data"
        subTitle="Please submit a request first."
        extra={
          <Button type="primary" onClick={() => navigate('/')}>
            Go to Home
          </Button>
        }
      />
    );
  }

  const { result, request } = state;
  const isApproved = result.status === 'approved';

  return (
    <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', minHeight: '100vh', backgroundColor: '#f0f2f5', padding: 24 }}>
      <Card style={{ width: 600, boxShadow: '0 4px 12px rgba(0,0,0,0.1)' }}>
        <Result
          status={isApproved ? 'success' : 'error'}
          title={isApproved ? 'Request Approved' : 'Request Rejected'}
          subTitle={isApproved ? null : result.message}
          extra={[
            <Button type="primary" key="home" onClick={() => navigate('/')}>
              Submit Another Request
            </Button>,
          ]}
        >
          <div style={{ background: '#fafafa', padding: 24, borderRadius: 8 }}>
            <Descriptions title="Request Details" column={1} bordered>
              <Descriptions.Item label="Amount">
                <Text strong>${request.amount.toFixed(2)}</Text>
              </Descriptions.Item>
              <Descriptions.Item label="Purpose">
                {request.purpose}
              </Descriptions.Item>
              {result.approver && (
                <Descriptions.Item 
                  label={
                    <span>
                      Approved By
                      <Tooltip title={
                        <div>
                          <div>&lt; 1000: Team Leader</div>
                          <div>&lt; 5000: Department Manager</div>
                          <div>&ge; 5000: CEO</div>
                        </div>
                      }>
                        <InfoCircleOutlined style={{ marginLeft: 8, color: '#1890ff', cursor: 'help' }} />
                      </Tooltip>
                    </span>
                  }
                >
                  <Text type="success" strong>{result.approver}</Text>
                </Descriptions.Item>
              )}
              {result.approvalId && (
                <Descriptions.Item label="Approval ID">
                  <Text copyable>{result.approvalId}</Text>
                </Descriptions.Item>
              )}
            </Descriptions>
          </div>
        </Result>
      </Card>
    </div>
  );
};

export default ResultPage;
