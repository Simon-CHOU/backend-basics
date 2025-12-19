export interface ApprovalRequest {
  amount: number;
  purpose: string;
}

export interface ApprovalResponse {
  success: boolean;
  approvalId: string;
  status: 'approved' | 'rejected';
  message: string;
}
