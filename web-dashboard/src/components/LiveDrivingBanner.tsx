import React from 'react';
import { Radio, MapPin, Gauge, Clock } from 'lucide-react';
import { DrivingSession } from '../types';

interface LiveDrivingBannerProps {
  activeSession: DrivingSession | null;
}

export const LiveDrivingBanner: React.FC<LiveDrivingBannerProps> = ({ activeSession }) => {
  if (!activeSession) return null;

  return (
    <div className="mb-6 bg-gradient-to-r from-blue-900 to-indigo-900 rounded-2xl p-5 text-white shadow-lg border border-blue-700/50 relative overflow-hidden">
      {/* 백그라운드 펄스 글로우 효과 */}
      <div className="absolute top-0 right-0 -mt-8 -mr-8 w-40 h-40 bg-cyan-500/20 rounded-full blur-3xl pointer-events-none" />

      <div className="flex flex-col md:flex-row items-start md:items-center justify-between gap-4">
        {/* 왼쪽: 실시간 주행 감지 뱃지 & 안내 */}
        <div className="flex items-center space-x-4">
          <div className="relative">
            <div className="w-12 h-12 rounded-2xl bg-cyan-500/20 border border-cyan-400/40 flex items-center justify-center text-cyan-300">
              <Radio className="w-6 h-6 animate-pulse" />
            </div>
            <span className="absolute -top-1 -right-1 flex h-3 w-3">
              <span className="animate-ping absolute inline-flex h-full w-full rounded-full bg-emerald-400 opacity-75"></span>
              <span className="relative inline-flex rounded-full h-3 w-3 bg-emerald-500"></span>
            </span>
          </div>
          <div>
            <div className="flex items-center space-x-2">
              <span className="text-xs font-bold uppercase tracking-wider bg-emerald-500/20 text-emerald-300 px-2 py-0.5 rounded-full border border-emerald-400/30">
                LIVE IN-CAR TRACKING
              </span>
              <h3 className="text-base font-bold text-white">
                {activeSession.vehicleNumber} ({activeSession.modelName}) 주행 중
              </h3>
            </div>
            <p className="text-sm text-gray-300 mt-0.5 flex items-center space-x-1">
              <MapPin className="w-3.5 h-3.5 text-cyan-400 shrink-0" />
              <span className="truncate max-w-md">{activeSession.startAddress || '출발지 수집 중...'}</span>
            </p>
          </div>
        </div>

        {/* 오른쪽: 주행 시작 정보 */}
        <div className="flex items-center space-x-6 text-sm">
          <div className="flex items-center space-x-2 text-gray-300">
            <Gauge className="w-4 h-4 text-cyan-400" />
            <div>
              <div className="text-xs text-gray-400">출발 ODO</div>
              <div className="font-semibold text-white">{activeSession.startOdometer} km</div>
            </div>
          </div>
          <div className="flex items-center space-x-2 text-gray-300">
            <Clock className="w-4 h-4 text-cyan-400" />
            <div>
              <div className="text-xs text-gray-400">출발 시각</div>
              <div className="font-semibold text-white">
                {new Date(activeSession.startedAt).toLocaleTimeString('ko-KR', { hour: '2-digit', minute: '2-digit' })}
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};
