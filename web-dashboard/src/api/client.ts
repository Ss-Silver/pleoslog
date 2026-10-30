import axios from 'axios';
import { ApiResponse, DrivingSession, DrivingSessionDetail, Vehicle } from '../types';

const api = axios.create({
  baseURL: '/api/v1',
  headers: {
    'Content-Type': 'application/json',
  },
});

export const getVehicles = async (): Promise<Vehicle[]> => {
  const res = await api.get<ApiResponse<Vehicle[]>>('/vehicles');
  return res.data.data;
};

export const getVehicleById = async (id: number): Promise<Vehicle> => {
  const res = await api.get<ApiResponse<Vehicle>>(`/vehicles/${id}`);
  return res.data.data;
};

export const pairVehicleByCode = async (code: string): Promise<Vehicle> => {
  const res = await api.get<ApiResponse<Vehicle>>(`/vehicles/pair/${code}`);
  return res.data.data;
};

export const getSessionsByVehicle = async (vehicleId: number): Promise<DrivingSession[]> => {
  const res = await api.get<ApiResponse<DrivingSession[]>>(`/sessions/vehicle/${vehicleId}`);
  return res.data.data;
};

export const getSessionDetail = async (sessionId: number): Promise<DrivingSessionDetail> => {
  const res = await api.get<ApiResponse<DrivingSessionDetail>>(`/sessions/${sessionId}`);
  return res.data.data;
};

export const downloadTaxReportExcel = async (vehicleId: number, year: number): Promise<void> => {
  const res = await api.get(`/reports/tax-excel?vehicleId=${vehicleId}&year=${year}`, {
    responseType: 'blob',
  });
  const blob = new Blob([res.data], {
    type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
  });
  const url = window.URL.createObjectURL(blob);
  const link = document.createElement('a');
  link.href = url;
  link.setAttribute('download', `업무용승용차_운행기록부_${year}년_차량${vehicleId}.xlsx`);
  document.body.appendChild(link);
  link.click();
  link.remove();
  window.URL.revokeObjectURL(url);
};

export const resetAndSeedDemo = async (): Promise<string> => {
  const res = await api.post<ApiResponse<string>>('/demo/reset-seed');
  return res.data.message;
};
