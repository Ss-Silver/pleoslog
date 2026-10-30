import React, { useState } from 'react';
import { Download, Map, Search, FileSpreadsheet, ArrowRight, Calendar, User } from 'lucide-react';
import { DrivingSession, DrivePurpose } from '../types';

interface SessionTableProps {
  sessions: DrivingSession[];
  onOpenMap: (sessionId: number) => void;
  onDownloadExcel: () => void;
  isDownloading: boolean;
}

export const SessionTable: React.FC<SessionTableProps> = ({
  sessions,
  onOpenMap,
  onDownloadExcel,
  isDownloading,
}) => {
  const [filterPurpose, setFilterPurpose] = useState<string>('ALL');
  const [searchTerm, setSearchTerm] = useState<string>('');

  // 필터링 적용
  const filteredSessions = sessions.filter((s) => {
    // 탭 필터
    if (filterPurpose !== 'ALL' && s.purposeType !== filterPurpose) {
      return false;
    }
    // 검색어 필터 (운전자명, 출발지, 도착지, 비고)
    if (searchTerm) {
      const term = searchTerm.toLowerCase();
      const matchUser = s.userName.toLowerCase().includes(term);
      const matchStart = s.startAddress?.toLowerCase().includes(term);
      const matchEnd = s.endAddress?.toLowerCase().includes(term);
      const matchNotes = s.notes?.toLowerCase().includes(term);
      if (!matchUser && !matchStart && !matchEnd && !matchNotes) {
        return false;
      }
    }
    return true;
  });

  const getPurposeBadge = (purpose: DrivePurpose | null, desc: string) => {
    switch (purpose) {
      case 'COMMUTE':
        return (
          <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-semibold bg-blue-50 text-blue-700 border border-blue-200">
            {desc || '출·퇴근용'}
          </span>
        );
      case 'BUSINESS':
        return (
          <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-semibold bg-emerald-50 text-emerald-700 border border-emerald-200">
            {desc || '일반 업무용'}
          </span>
        );
      case 'PERSONAL':
        return (
          <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-semibold bg-gray-100 text-gray-700 border border-gray-200">
            {desc || '비업무용'}
          </span>
        );
      default:
        return (
          <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-semibold bg-yellow-50 text-yellow-700 border border-yellow-200">
            주행 중
          </span>
        );
    }
  };

  return (
    <div className="bg-white rounded-2xl shadow-sm border border-gray-100 overflow-hidden">
      {/* 테이블 상단 컨트롤 바 */}
      <div className="p-5 border-b border-gray-100 flex flex-col md:flex-row md:items-center md:justify-between gap-4">
        {/* 목적별 탭 필터 */}
        <div className="flex items-center space-x-1.5 bg-gray-100 p-1 rounded-xl text-xs font-semibold">
          {[
            { id: 'ALL', label: '전체 보기' },
            { id: 'BUSINESS', label: '일반 업무용' },
            { id: 'COMMUTE', label: '출·퇴근용' },
            { id: 'PERSONAL', label: '비업무용' },
          ].map((tab) => (
            <button
              key={tab.id}
              onClick={() => setFilterPurpose(tab.id)}
              className={`px-3 py-1.5 rounded-lg transition-all ${
                filterPurpose === tab.id
                  ? 'bg-white text-gray-900 shadow-sm font-bold'
                  : 'text-gray-500 hover:text-gray-700'
              }`}
            >
              {tab.label}
            </button>
          ))}
        </div>

        {/* 검색창 & 엑셀 다운로드 버튼 */}
        <div className="flex items-center space-x-3">
          <div className="relative">
            <Search className="w-4 h-4 text-gray-400 absolute left-3 top-1/2 -translate-y-1/2" />
            <input
              type="text"
              placeholder="운전자, 출발/도착지 검색..."
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
              className="pl-9 pr-3 py-1.5 rounded-xl border border-gray-200 text-xs w-48 sm:w-60 focus:outline-none focus:ring-2 focus:ring-hyundai-blue/20 focus:border-hyundai-blue"
            />
          </div>

          <button
            onClick={onDownloadExcel}
            disabled={isDownloading}
            className="flex items-center space-x-1.5 px-4 py-2 bg-emerald-600 hover:bg-emerald-700 text-white rounded-xl text-xs font-bold shadow-sm transition-all disabled:opacity-50"
          >
            <FileSpreadsheet className="w-4 h-4" />
            <span>국세청 서식 엑셀 다운로드</span>
            <Download className="w-3.5 h-3.5" />
          </button>
        </div>
      </div>

      {/* 테이블 본체 */}
      <div className="overflow-x-auto">
        <table className="w-full text-left text-xs text-gray-600">
          <thead className="bg-gray-50/70 text-gray-400 uppercase tracking-wider text-[11px] font-semibold border-b border-gray-100">
            <tr>
              <th className="px-5 py-3.5">운행 일시</th>
              <th className="px-4 py-3.5">운전자</th>
              <th className="px-5 py-3.5">출발지 / 도착지</th>
              <th className="px-4 py-3.5 text-right">계기판 (ODO)</th>
              <th className="px-4 py-3.5 text-right">주행거리</th>
              <th className="px-4 py-3.5 text-center">운행 목적</th>
              <th className="px-4 py-3.5">비고</th>
              <th className="px-4 py-3.5 text-center">경로</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-gray-100">
            {filteredSessions.length === 0 ? (
              <tr>
                <td colSpan={8} className="px-5 py-12 text-center text-gray-400 text-sm">
                  등록된 운행 기록이 없습니다.
                </td>
              </tr>
            ) : (
              filteredSessions.map((session) => (
                <tr key={session.sessionId} className="hover:bg-gray-50/80 transition-colors">
                  {/* 운행 일시 */}
                  <td className="px-5 py-4 whitespace-nowrap">
                    <div className="flex items-center space-x-2 text-gray-900 font-medium">
                      <Calendar className="w-3.5 h-3.5 text-gray-400" />
                      <span>{new Date(session.startedAt).toLocaleDateString('ko-KR')}</span>
                    </div>
                    <div className="text-gray-400 text-[11px] mt-0.5 ml-5.5">
                      {new Date(session.startedAt).toLocaleTimeString('ko-KR', { hour: '2-digit', minute: '2-digit' })}
                      {session.endedAt && (
                        <> ~ {new Date(session.endedAt).toLocaleTimeString('ko-KR', { hour: '2-digit', minute: '2-digit' })}</>
                      )}
                    </div>
                  </td>

                  {/* 운전자 */}
                  <td className="px-4 py-4 whitespace-nowrap">
                    <div className="flex items-center space-x-1.5 text-gray-800 font-semibold">
                      <User className="w-3.5 h-3.5 text-gray-400" />
                      <span>{session.userName}</span>
                    </div>
                    <div className="text-gray-400 text-[11px] ml-5">{session.department}</div>
                  </td>

                  {/* 출발지 / 도착지 */}
                  <td className="px-5 py-4 max-w-xs">
                    <div className="text-gray-900 font-medium truncate" title={session.startAddress}>
                      {session.startAddress || '-'}
                    </div>
                    <div className="flex items-center space-x-1 text-gray-400 text-[11px] mt-0.5 truncate" title={session.endAddress || ''}>
                      <ArrowRight className="w-3 h-3 text-cyan-600 shrink-0" />
                      <span className="truncate">{session.endAddress || (session.status === 'DRIVING' ? '주행 진행 중' : '-')}</span>
                    </div>
                  </td>

                  {/* ODO (출발 -> 도착) */}
                  <td className="px-4 py-4 text-right whitespace-nowrap">
                    <div className="text-gray-500 font-mono text-[11px]">
                      {session.startOdometer.toFixed(1)} km
                    </div>
                    <div className="text-gray-900 font-mono font-bold">
                      ➔ {session.endOdometer ? `${session.endOdometer.toFixed(1)} km` : '-'}
                    </div>
                  </td>

                  {/* 주행거리 */}
                  <td className="px-4 py-4 text-right whitespace-nowrap">
                    <span className="text-sm font-extrabold text-hyundai-blue font-mono">
                      {session.drivenDistance != null ? `${session.drivenDistance.toFixed(1)}` : '-'}
                    </span>
                    <span className="text-gray-400 text-[11px] ml-0.5">km</span>
                  </td>

                  {/* 운행 목적 뱃지 */}
                  <td className="px-4 py-4 text-center whitespace-nowrap">
                    {getPurposeBadge(session.purposeType, session.purposeDescription)}
                  </td>

                  {/* 비고 */}
                  <td className="px-4 py-4 text-gray-500 max-w-[140px] truncate" title={session.notes || ''}>
                    {session.notes || '-'}
                  </td>

                  {/* 지도 보기 버튼 */}
                  <td className="px-4 py-4 text-center whitespace-nowrap">
                    <button
                      onClick={() => onOpenMap(session.sessionId)}
                      title="주행 중 수집된 GPS 궤적 지도 보기"
                      className="inline-flex items-center space-x-1 px-2.5 py-1.5 rounded-lg text-xs font-semibold bg-gray-100 hover:bg-hyundai-blue hover:text-white transition-all text-gray-700"
                    >
                      <Map className="w-3.5 h-3.5" />
                      <span>경로</span>
                    </button>
                  </td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>
    </div>
  );
};
