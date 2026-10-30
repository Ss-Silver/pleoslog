import React from 'react';
import { Gauge, Navigation, Briefcase, Percent, CheckCircle2 } from 'lucide-react';
import { DrivingSession, Vehicle } from '../types';

interface KpiSummaryProps {
  vehicle: Vehicle | null;
  sessions: DrivingSession[];
}

export const KpiSummary: React.FC<KpiSummaryProps> = ({ vehicle, sessions }) => {
  // 완료된 세션만 집계
  const completedSessions = sessions.filter((s) => s.status === 'COMPLETED');

  // 총 주행거리 (km)
  const totalDriven = completedSessions.reduce((acc, s) => acc + (s.drivenDistance || 0), 0);

  // 업무용 거리 (출퇴근 + 일반업무)
  const businessDriven = completedSessions
    .filter((s) => s.purposeType === 'BUSINESS' || s.purposeType === 'COMMUTE')
    .reduce((acc, s) => acc + (s.drivenDistance || 0), 0);

  // 비업무용 거리
  const personalDriven = completedSessions
    .filter((s) => s.purposeType === 'PERSONAL')
    .reduce((acc, s) => acc + (s.drivenDistance || 0), 0);

  // 법정 업무사용비율 (%)
  const businessRatio = totalDriven > 0 ? (businessDriven / totalDriven) * 100 : 100;

  return (
    <div className="grid grid-cols-1 md:grid-cols-4 gap-4 mb-6">
      {/* 1. 현재 계기판 누적 ODO */}
      <div className="bg-white rounded-2xl p-5 shadow-sm border border-gray-100 flex flex-col justify-between">
        <div className="flex items-center justify-between text-gray-500 mb-2">
          <span className="text-xs font-semibold uppercase tracking-wider">차량 계기판 (ODO)</span>
          <div className="p-2 bg-blue-50 text-blue-600 rounded-xl">
            <Gauge className="w-5 h-5" />
          </div>
        </div>
        <div>
          <div className="flex items-baseline space-x-1">
            <span className="text-3xl font-extrabold text-gray-900 tracking-tight">
              {vehicle?.totalOdometer?.toLocaleString('ko-KR', { minimumFractionDigits: 1, maximumFractionDigits: 1 }) || '0.0'}
            </span>
            <span className="text-sm font-semibold text-gray-500">km</span>
          </div>
          <div className="mt-2 text-xs text-emerald-600 font-medium flex items-center space-x-1">
            <CheckCircle2 className="w-3.5 h-3.5" />
            <span>차량 센서 실시간 동기화</span>
          </div>
        </div>
      </div>

      {/* 2. 연간 총 주행거리 */}
      <div className="bg-white rounded-2xl p-5 shadow-sm border border-gray-100 flex flex-col justify-between">
        <div className="flex items-center justify-between text-gray-500 mb-2">
          <span className="text-xs font-semibold uppercase tracking-wider">총 주행거리 / 횟수</span>
          <div className="p-2 bg-indigo-50 text-indigo-600 rounded-xl">
            <Navigation className="w-5 h-5" />
          </div>
        </div>
        <div>
          <div className="flex items-baseline space-x-1">
            <span className="text-3xl font-extrabold text-gray-900 tracking-tight">
              {totalDriven.toLocaleString('ko-KR', { minimumFractionDigits: 1, maximumFractionDigits: 1 })}
            </span>
            <span className="text-sm font-semibold text-gray-500">km</span>
          </div>
          <div className="mt-2 text-xs text-gray-500">
            총 <span className="font-bold text-gray-800">{completedSessions.length}회</span> 운행 완료
          </div>
        </div>
      </div>

      {/* 3. 업무용 사용거리 */}
      <div className="bg-white rounded-2xl p-5 shadow-sm border border-gray-100 flex flex-col justify-between">
        <div className="flex items-center justify-between text-gray-500 mb-2">
          <span className="text-xs font-semibold uppercase tracking-wider">업무용 인정 주행</span>
          <div className="p-2 bg-cyan-50 text-cyan-600 rounded-xl">
            <Briefcase className="w-5 h-5" />
          </div>
        </div>
        <div>
          <div className="flex items-baseline space-x-1">
            <span className="text-3xl font-extrabold text-hyundai-blue tracking-tight">
              {businessDriven.toLocaleString('ko-KR', { minimumFractionDigits: 1, maximumFractionDigits: 1 })}
            </span>
            <span className="text-sm font-semibold text-gray-500">km</span>
          </div>
          <div className="mt-2 text-xs text-gray-400">
            비업무용: {personalDriven.toFixed(1)} km
          </div>
        </div>
      </div>

      {/* 4. 법정 업무사용비율 (%) */}
      <div className="bg-white rounded-2xl p-5 shadow-sm border border-gray-100 flex flex-col justify-between">
        <div className="flex items-center justify-between text-gray-500 mb-2">
          <span className="text-xs font-semibold uppercase tracking-wider">법정 업무사용비율</span>
          <div className="p-2 bg-amber-50 text-amber-600 rounded-xl">
            <Percent className="w-5 h-5" />
          </div>
        </div>
        <div>
          <div className="flex items-baseline justify-between mb-1.5">
            <span className="text-3xl font-extrabold text-emerald-600 tracking-tight">
              {businessRatio.toFixed(1)}%
            </span>
            <span className="text-xs font-bold text-emerald-700 bg-emerald-50 px-2 py-0.5 rounded-full border border-emerald-200">
              전액 비용인정
            </span>
          </div>
          {/* 프로그레스 바 */}
          <div className="w-full bg-gray-100 rounded-full h-2 overflow-hidden">
            <div
              className="bg-emerald-500 h-2 rounded-full transition-all duration-500"
              style={{ width: `${Math.min(100, businessRatio)}%` }}
            />
          </div>
        </div>
      </div>
    </div>
  );
};
