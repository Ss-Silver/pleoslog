package com.pleoslog.domain.session.repository;

import com.pleoslog.domain.session.entity.DriveRoute;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DriveRouteRepository extends JpaRepository<DriveRoute, Long> {
    List<DriveRoute> findBySessionIdOrderByRecordedAtAsc(Long sessionId);
}
