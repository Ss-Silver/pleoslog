import { useState, useEffect, useCallback } from 'react';
import { Header } from './components/Header';
import { KpiSummary } from './components/KpiSummary';
import { LiveDrivingBanner } from './components/LiveDrivingBanner';
import { SessionTable } from './components/SessionTable';
import { RouteMapModal } from './components/RouteMapModal';
import { PairingModal } from './components/PairingModal';
import {
  getVehicles,
  getVehicleById,
  getSessionsByVehicle,
  getSessionDetail,
  downloadTaxReportExcel,
  resetAndSeedDemo,
} from './api/client';
import { Vehicle, DrivingSession, DrivingSessionDetail } from './types';

export function App() {
  const [vehicles, setVehicles] = useState<Vehicle[]>([]);
  const [selectedVehicle, setSelectedVehicle] = useState<Vehicle | null>(null);
  const [sessions, setSessions] = useState<DrivingSession[]>([]);
  const [activeSession, setActiveSession] = useState<DrivingSession | null>(null);

  const [selectedSessionDetail, setSelectedSessionDetail] = useState<DrivingSessionDetail | null>(null);
  const [isPairingOpen, setIsPairingOpen] = useState(false);
  const [isDownloading, setIsDownloading] = useState(false);
  const [isResetting, setIsResetting] = useState(false);
  const [toastMessage, setToastMessage] = useState<string | null>(null);

  const showToast = (msg: string) => {
    setToastMessage(msg);
    setTimeout(() => setToastMessage(null), 3500);
  };

  // 1. 차량 목록 조회
  const loadVehicles = useCallback(async () => {
    try {
      const data = await getVehicles();
      setVehicles(data);
      if (data.length > 0 && !selectedVehicle) {
        setSelectedVehicle(data[0]);
      }
    } catch (err) {
      console.error('차량 목록 로드 실패:', err);
    }
  }, [selectedVehicle]);

  // 2. 세션 목록 및 실시간 주행 상태 조회
  const loadSessions = useCallback(async () => {
    if (!selectedVehicle) return;
    try {
      const data = await getSessionsByVehicle(selectedVehicle.id);
      setSessions(data);

      // 주행 중인 세션 감지
      const driving = data.find((s) => s.status === 'DRIVING');
      setActiveSession(driving || null);

      // 차량 최신 ODO 갱신
      const updatedVehicle = await getVehicleById(selectedVehicle.id);
      setSelectedVehicle(updatedVehicle);
    } catch (err) {
      console.error('세션 목록 로드 실패:', err);
    }
  }, [selectedVehicle]);

  useEffect(() => {
    loadVehicles();
  }, [loadVehicles]);

  useEffect(() => {
    if (selectedVehicle) {
      loadSessions();
      // 실시간 D단/P단 주행 감지 4초 폴링
      const timer = setInterval(loadSessions, 4000);
      return () => clearInterval(timer);
    }
  }, [selectedVehicle, loadSessions]);

  // 3. 지도 궤적 보기 핸들러
  const handleOpenMap = async (sessionId: number) => {
    try {
      const detail = await getSessionDetail(sessionId);
      setSelectedSessionDetail(detail);
    } catch (err) {
      showToast('주행 궤적 데이터를 불러오지 못했습니다.');
    }
  };

  // 4. 국세청 엑셀 다운로드
  const handleDownloadExcel = async () => {
    if (!selectedVehicle) return;
    setIsDownloading(true);
    try {
      await downloadTaxReportExcel(selectedVehicle.id, 2026);
      showToast('국세청 법정 운행기록부(.xlsx) 다운로드가 완료되었습니다.');
    } catch (err) {
      showToast('엑셀 생성 중 오류가 발생했습니다.');
    } finally {
      setIsDownloading(false);
    }
  };

  // 5. 시연용 DB 리셋
  const handleResetDemo = async () => {
    if (!confirm('발표 시연을 위해 DB를 초기 샘플 데이터로 리셋하시겠습니까?')) return;
    setIsResetting(true);
    try {
      await resetAndSeedDemo();
      showToast('시연용 DB가 성공적으로 초기화 및 재시딩되었습니다!');
      await loadVehicles();
      if (selectedVehicle) {
        await loadSessions();
      }
    } catch (err) {
      showToast('데모 리셋 실패');
    } finally {
      setIsResetting(false);
    }
  };

  // 6. 핀코드 페어링 완료 핸들러
  const handlePaired = (pairedVehicle: Vehicle) => {
    setSelectedVehicle(pairedVehicle);
    showToast(`${pairedVehicle.vehicleNumber} (${pairedVehicle.modelName}) 차량이 연결되었습니다!`);
    loadVehicles();
  };

  return (
    <div className="min-h-screen bg-gray-50 flex flex-col">
      {/* 토스트 알림 */}
      {toastMessage && (
        <div className="fixed top-5 right-5 z-50 bg-gray-900 text-white text-xs font-semibold px-4 py-3 rounded-2xl shadow-xl flex items-center space-x-2 animate-bounce">
          <span>✨</span>
          <span>{toastMessage}</span>
        </div>
      )}

      {/* 헤더 네비게이션 */}
      <Header
        vehicles={vehicles}
        selectedVehicle={selectedVehicle}
        onSelectVehicle={setSelectedVehicle}
        onOpenPairing={() => setIsPairingOpen(true)}
        onResetDemo={handleResetDemo}
        isResetting={isResetting}
      />

      {/* 메인 컨테이너 */}
      <main className="flex-1 max-w-7xl w-full mx-auto px-4 sm:px-6 lg:px-8 py-8">
        {/* 실시간 D단 주행 감지 배너 */}
        <LiveDrivingBanner activeSession={activeSession} />

        {/* 국세청 공제 관리 KPI 지표 카드 */}
        <KpiSummary vehicle={selectedVehicle} sessions={sessions} />

        {/* 운행기록 타임라인 & 상세 테이블 */}
        <SessionTable
          sessions={sessions}
          onOpenMap={handleOpenMap}
          onDownloadExcel={handleDownloadExcel}
          isDownloading={isDownloading}
        />
      </main>

      {/* 푸터 */}
      <footer className="bg-white border-t border-gray-100 py-6 text-center text-xs text-gray-400">
        <p>© 2026 Pleos Log • 현대자동차그룹 차량용 앱 개발 공모전 (In-Car / Pleos OS)</p>
      </footer>

      {/* 경로 지도 모달 */}
      <RouteMapModal
        detail={selectedSessionDetail}
        onClose={() => setSelectedSessionDetail(null)}
      />

      {/* 차량 페어링 모달 */}
      <PairingModal
        isOpen={isPairingOpen}
        onClose={() => setIsPairingOpen(false)}
        onPaired={handlePaired}
      />
    </div>
  );
}

export default App;
