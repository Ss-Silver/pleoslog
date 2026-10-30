import React from 'react';
import { Car, RefreshCw, KeyRound, Building2 } from 'lucide-react';
import { Vehicle } from '../types';

interface HeaderProps {
  vehicles: Vehicle[];
  selectedVehicle: Vehicle | null;
  onSelectVehicle: (v: Vehicle) => void;
  onOpenPairing: () => void;
  onResetDemo: () => void;
  isResetting: boolean;
}

export const Header: React.FC<HeaderProps> = ({
  vehicles,
  selectedVehicle,
  onSelectVehicle,
  onOpenPairing,
  onResetDemo,
  isResetting,
}) => {
  return (
    <header className="bg-hyundai-blue text-white shadow-md border-b border-hyundai-blue/40">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 h-16 flex items-center justify-between">
        {/* 로고 & 플랫폼 명 */}
        <div className="flex items-center space-x-3">
          <div className="w-10 h-10 rounded-xl bg-hyundai-sky/20 border border-hyundai-sky/40 flex items-center justify-center">
            <Car className="w-6 h-6 text-hyundai-sky" />
          </div>
          <div>
            <div className="flex items-center space-x-2">
              <span className="text-xl font-bold tracking-tight">Pleos Log</span>
              <span className="text-xs bg-hyundai-sky/30 text-cyan-200 px-2 py-0.5 rounded-full font-medium">
                In-Car Fleet
              </span>
            </div>
            <p className="text-xs text-gray-300 flex items-center space-x-1">
              <Building2 className="w-3 h-3 text-gray-400" />
              <span>{selectedVehicle?.companyName || '(주)플레오스 테크놀로지'}</span>
            </p>
          </div>
        </div>

        {/* 차량 선택 & 액션 버튼 */}
        <div className="flex items-center space-x-3">
          {/* 차량 선택 드롭다운 */}
          <div className="flex items-center space-x-2 bg-white/10 px-3 py-1.5 rounded-lg border border-white/20">
            <Car className="w-4 h-4 text-cyan-300" />
            <select
              className="bg-transparent text-sm font-medium text-white focus:outline-none cursor-pointer"
              value={selectedVehicle?.id || ''}
              onChange={(e) => {
                const found = vehicles.find((v) => v.id === Number(e.target.value));
                if (found) onSelectVehicle(found);
              }}
            >
              {vehicles.map((v) => (
                <option key={v.id} value={v.id} className="text-gray-900">
                  {v.vehicleNumber} ({v.modelName})
                </option>
              ))}
            </select>
          </div>

          {/* 6자리 페어링 버튼 */}
          <button
            onClick={onOpenPairing}
            className="flex items-center space-x-1.5 px-3 py-1.5 rounded-lg text-xs font-semibold bg-white/10 hover:bg-white/20 transition-colors border border-white/20"
          >
            <KeyRound className="w-3.5 h-3.5 text-yellow-300" />
            <span>차량 페어링</span>
          </button>

          {/* 시연용 원클릭 DB 리셋 버튼 */}
          <button
            onClick={onResetDemo}
            disabled={isResetting}
            title="발표 시연 전 DB를 초기 샘플 상태로 즉시 되돌립니다"
            className="flex items-center space-x-1.5 px-3 py-1.5 rounded-lg text-xs font-semibold bg-rose-500/20 text-rose-200 hover:bg-rose-500/30 transition-colors border border-rose-400/30 disabled:opacity-50"
          >
            <RefreshCw className={`w-3.5 h-3.5 text-rose-300 ${isResetting ? 'animate-spin' : ''}`} />
            <span>데모 리셋</span>
          </button>
        </div>
      </div>
    </header>
  );
};
