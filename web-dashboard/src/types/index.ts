export interface Vehicle {
  id: number;
  vehicleNumber: string;
  modelName: string;
  totalOdometer: number;
  pairingCode: string;
  companyName: string;
}

export type SessionStatus = 'DRIVING' | 'COMPLETED' | 'CANCELLED';
export type DrivePurpose = 'COMMUTE' | 'BUSINESS' | 'PERSONAL';

export interface DrivingSession {
  sessionId: number;
  vehicleId: number;
  vehicleNumber: string;
  modelName: string;
  userName: string;
  department: string;
  status: SessionStatus;
  startedAt: string;
  endedAt: string | null;
  startOdometer: number;
  endOdometer: number | null;
  drivenDistance: number | null;
  startAddress: string;
  endAddress: string | null;
  startLatitude?: number | null;
  startLongitude?: number | null;
  endLatitude?: number | null;
  endLongitude?: number | null;
  purposeType: DrivePurpose | null;
  purposeDescription: string;
  notes: string | null;
}

export interface RoutePoint {
  id: number;
  latitude: number;
  longitude: number;
  speed: number | null;
  recordedAt: string;
}

export interface DrivingSessionDetail {
  session: DrivingSession;
  routePoints: RoutePoint[];
}

export interface ApiResponse<T> {
  success: boolean;
  message: string;
  data: T;
}
