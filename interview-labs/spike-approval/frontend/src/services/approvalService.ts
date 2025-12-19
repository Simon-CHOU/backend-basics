import axios from 'axios';
import { ApprovalRequest, ApprovalResponse } from '../types/approval';

const API_URL = '/api/approval';

export const submitApproval = async (request: ApprovalRequest): Promise<ApprovalResponse> => {
  const response = await axios.post<ApprovalResponse>(`${API_URL}/submit`, request);
  return response.data;
};
