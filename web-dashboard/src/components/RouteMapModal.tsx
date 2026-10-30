import React from 'react';
import { X, MapPin, Gauge, Navigation } from 'lucide-react';
import { MapContainer, TileLayer, Polyline, Marker, Popup } from 'react-leaflet';
import L from 'leaflet';
import { DrivingSessionDetail } from '../types';

interface RouteMapModalProps {
  detail: DrivingSessionDetail | null;
  onClose: () => void;
}

// Leaflet 기본 마커 아이콘 설정 (SVG DivIcon 활용)
const startIcon = L.divIcon({
  html: `<div style="background-color: #10b981; color: white; border-radius: 9999px; width: 28px; height: 28px; display: flex; align-items: center; justify-content: center; font-weight: bold; font-size: 11px; border: 2px solid white; box-shadow: 0 4px 6px -1px rgba(0,0,0,0.2);">출발</div>`,
  className: '',
  iconSize: [28, 28],
  iconAnchor: [14, 14],
});

const endIcon = L.divIcon({
  html: `<div style="background-color: #ef4444; color: white; border-radius: 9999px; width: 28px; height: 28px; display: flex; align-items: center; justify-content: center; font-weight: bold; font-size: 11px; border: 2px solid white; box-shadow: 0 4px 6px -1px rgba(0,0,0,0.2);">도착</div>`,
  className: '',
  iconSize: [28, 28],
  iconAnchor: [14, 14],
});

export const RouteMapModal: React.FC<RouteMapModalProps> = ({ detail, onClose }) => {
  if (!detail) return null;

  const { session, routePoints } = detail;

  // 유효한 좌표 추출 (우선순위: routePoints, 없으면 start/end 좌표)
  const polylineCoords: [number, number][] = [];
  if (routePoints && routePoints.length > 0) {
    routePoints.forEach((p) => polylineCoords.push([p.latitude, p.longitude]));
  } else if (
    session.startLatitude != null &&
    session.startLongitude != null &&
    session.endLatitude != null &&
    session.endLongitude != null
  ) {
    polylineCoords.push([session.startLatitude, session.startLongitude]);
    polylineCoords.push([session.endLatitude, session.endLongitude]);
  }

  // 기본 지도 중심 좌표 (좌표 없으면 강남역 기본)
  const centerLat = polylineCoords.length > 0 ? polylineCoords[0][0] : 37.4979;
  const centerLng = polylineCoords.length > 0 ? polylineCoords[0][1] : 127.0276;

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/60 backdrop-blur-sm animate-fade-in">
      <div className="bg-white rounded-3xl shadow-2xl max-w-4xl w-full overflow-hidden border border-gray-100 flex flex-col max-h-[90vh]">
        {/* 모달 헤더 */}
        <div className="px-6 py-4 bg-hyundai-blue text-white flex items-center justify-between">
          <div className="flex items-center space-x-2.5">
            <div className="p-2 bg-white/10 rounded-xl">
              <Navigation className="w-5 h-5 text-cyan-300" />
            </div>
            <div>
              <h3 className="text-base font-bold text-white">주행 상세 경로 (GPS 궤적)</h3>
              <p className="text-xs text-gray-300">
                {session.vehicleNumber} ({session.userName}) • {session.drivenDistance || 0} km 주행
              </p>
            </div>
          </div>
          <button
            onClick={onClose}
            className="p-1.5 rounded-xl hover:bg-white/10 text-gray-300 hover:text-white transition-colors"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* 상단 주행 정보 카드 요약 */}
        <div className="p-4 bg-gray-50 border-b border-gray-100 grid grid-cols-1 sm:grid-cols-2 gap-3 text-xs">
          <div className="flex items-start space-x-2 bg-white p-3 rounded-xl border border-gray-200/60 shadow-sm">
            <MapPin className="w-4 h-4 text-emerald-600 mt-0.5 shrink-0" />
            <div>
              <span className="font-bold text-emerald-700">출발지:</span>
              <p className="text-gray-800 font-medium mt-0.5">{session.startAddress}</p>
              <span className="text-[11px] text-gray-400">
                출발 ODO: {session.startOdometer} km ({new Date(session.startedAt).toLocaleTimeString('ko-KR')})
              </span>
            </div>
          </div>

          <div className="flex items-start space-x-2 bg-white p-3 rounded-xl border border-gray-200/60 shadow-sm">
            <MapPin className="w-4 h-4 text-rose-600 mt-0.5 shrink-0" />
            <div>
              <span className="font-bold text-rose-700">도착지:</span>
              <p className="text-gray-800 font-medium mt-0.5">{session.endAddress || '주행 중'}</p>
              <span className="text-[11px] text-gray-400">
                도착 ODO: {session.endOdometer ? `${session.endOdometer} km` : '-'} (
                {session.endedAt ? new Date(session.endedAt).toLocaleTimeString('ko-KR') : '-'})
              </span>
            </div>
          </div>
        </div>

        {/* Leaflet 인터랙티브 지도 영역 */}
        <div className="flex-1 min-h-[420px] relative">
          {polylineCoords.length > 0 ? (
            <MapContainer
              center={[centerLat, centerLng]}
              zoom={12}
              style={{ height: '100%', width: '100%', minHeight: '420px' }}
            >
              <TileLayer
                attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a>'
                url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
              />

              {/* 출발 마커 */}
              <Marker position={polylineCoords[0]} icon={startIcon}>
                <Popup>
                  <div className="text-xs p-1">
                    <strong className="text-emerald-600">출발 지점</strong>
                    <p className="mt-1">{session.startAddress}</p>
                  </div>
                </Popup>
              </Marker>

              {/* 도착 마커 */}
              {polylineCoords.length > 1 && (
                <Marker position={polylineCoords[polylineCoords.length - 1]} icon={endIcon}>
                  <Popup>
                    <div className="text-xs p-1">
                      <strong className="text-rose-600">도착 지점</strong>
                      <p className="mt-1">{session.endAddress || '도착지'}</p>
                    </div>
                  </Popup>
                </Marker>
              )}

              {/* 주행 경로 폴리라인 */}
              <Polyline positions={polylineCoords} color="#002c5f" weight={5} opacity={0.8} dashArray="2, 8" />
            </MapContainer>
          ) : (
            <div className="h-full flex flex-col items-center justify-center text-gray-400 text-xs p-8">
              <Gauge className="w-8 h-8 text-gray-300 mb-2" />
              <span>기록된 GPS 좌표가 없습니다.</span>
            </div>
          )}
        </div>

        {/* 모달 푸터 */}
        <div className="p-4 bg-gray-50 border-t border-gray-100 flex items-center justify-between text-xs text-gray-500">
          <div>
            수집된 GPS 좌표: <strong className="text-gray-800">{routePoints?.length || 0}개</strong>
          </div>
          <button
            onClick={onClose}
            className="px-4 py-2 bg-gray-200 hover:bg-gray-300 text-gray-800 rounded-xl font-semibold transition-colors"
          >
            닫기
          </button>
        </div>
      </div>
    </div>
  );
};
