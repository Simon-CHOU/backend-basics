import React, { useState } from 'react';
import { Form, Input, InputNumber, Button, Card, Typography, message } from 'antd';
import { useNavigate } from 'react-router-dom';
import { submitApproval } from '../services/approvalService';
import { ApprovalRequest } from '../types/approval';

const { Title } = Typography;

const RequestPage: React.FC = () => {
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();
  const [form] = Form.useForm();

  const onFinish = async (values: ApprovalRequest) => {
    setLoading(true);
    try {
      const response = await submitApproval(values);
      navigate('/result', { state: { result: response, request: values } });
    } catch (error) {
      console.error('Approval request failed:', error);
      message.error('Failed to submit approval request. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', minHeight: '100vh', backgroundColor: '#f0f2f5' }}>
      <Card style={{ width: 500, boxShadow: '0 4px 12px rgba(0,0,0,0.1)' }}>
        <div style={{ textAlign: 'center', marginBottom: 24 }}>
          <Title level={2} style={{ color: '#1890ff' }}>Approval Request</Title>
        </div>
        
        <Form
          form={form}
          name="approval_request"
          layout="vertical"
          onFinish={onFinish}
          autoComplete="off"
        >
          <Form.Item
            label="Amount"
            name="amount"
            rules={[
              { required: true, message: 'Please input the amount!' },
              { type: 'number', min: 0.01, message: 'Amount must be greater than 0!' }
            ]}
          >
            <InputNumber
              style={{ width: '100%' }}
              prefix="$"
              placeholder="Enter amount"
              precision={2}
            />
          </Form.Item>

          <Form.Item
            label="Purpose"
            name="purpose"
            rules={[
              { required: true, message: 'Please input the purpose!' },
              { max: 200, message: 'Purpose must be less than 200 characters!' }
            ]}
          >
            <Input.TextArea
              rows={4}
              placeholder="Enter the purpose of this request"
              showCount
              maxLength={200}
            />
          </Form.Item>

          <Form.Item>
            <Button type="primary" htmlType="submit" loading={loading} block size="large">
              Submit Request
            </Button>
          </Form.Item>
        </Form>
      </Card>
    </div>
  );
};

export default RequestPage;
