import React, { useState } from 'react';
import { X, KeyRound, CheckCircle2, AlertCircle } from 'lucide-react';
import { pairVehicleByCode } from '../api/client';
import { Vehicle } from '../types';

interface PairingModalProps {
  isOpen: boolean;
  onClose: () => void;
  onPaired: (vehicle: Vehicle) => void;
}

export const PairingModal: React.FC<PairingModalProps> = ({ isOpen, onClose, onPaired }) => {
  const [pin, setPin] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  if (!isOpen) return null;

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (pin.length !== 6) {
      setError('6자리 핀코드를 입력해주세요.');
      return;
    }

    setLoading(true);
    setError(null);
    try {
      const vehicle = await pairVehicleByCode(pin);
      onPaired(vehicle);
      onClose();
    } catch (err: any) {
      setError(err?.response?.data?.message || '유효하지 않은 페어링 코드입니다.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/60 backdrop-blur-sm animate-fade-in">
      <div className="bg-white rounded-3xl shadow-2xl max-w-md w-full overflow-hidden border border-gray-100">
        <div className="p-6">
          <div className="flex items-center justify-between mb-4">
            <div className="flex items-center space-x-2">
              <div className="p-2 bg-blue-50 text-hyundai-blue rounded-xl">
                <KeyRound className="w-5 h-5" />
              </div>
              <h3 className="text-base font-bold text-gray-900">차량 핀코드 페어링</h3>
            </div>
            <button onClick={onClose} className="text-gray-400 hover:text-gray-600">
              <X className="w-5 h-5" />
            </button>
          </div>

          <p className="text-xs text-gray-500 mb-6">
            차량 In-Car 디스플레이 화면에 표시된 <strong>6자리 핀코드</strong>를 입력하시면, 내 계정과 차량이 1:1로 즉시 연결됩니다.
          </p>

          <form onSubmit={handleSubmit} className="space-y-4">
            <div>
              <input
                type="text"
                maxLength={6}
                value={pin}
                onChange={(e) => setPin(e.target.value.replace(/\D/g, ''))}
                placeholder="000000"
                className="w-full text-center tracking-[0.5em] text-2xl font-mono font-bold py-3 px-4 rounded-2xl border-2 border-gray-200 focus:border-hyundai-blue focus:outline-none transition-colors"
              />
              <div className="flex justify-between text-[11px] text-gray-400 mt-2 px-1">
                <span>예시: 729104 (아이오닉 6)</span>
                <span>831920 (GV80)</span>
              </div>
            </div>

            {error && (
              <div className="flex items-center space-x-1.5 text-xs text-rose-600 bg-rose-50 p-2.5 rounded-xl border border-rose-200">
                <AlertCircle className="w-4 h-4 shrink-0" />
                <span>{error}</span>
              </div>
            )}

            <button
              type="submit"
              disabled={loading || pin.length !== 6}
              className="w-full py-3 bg-hyundai-blue hover:bg-hyundai-blue/90 text-white rounded-xl font-bold text-sm shadow-md transition-all disabled:opacity-50 flex items-center justify-center space-x-2"
            >
              <CheckCircle2 className="w-4 h-4" />
              <span>{loading ? '연결 확인 중...' : '차량 연결 완료'}</span>
            </button>
          </form>
        </div>
      </div>
    </div>
  );
};
