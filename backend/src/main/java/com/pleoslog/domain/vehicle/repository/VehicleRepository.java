package com.pleoslog.domain.vehicle.repository;

import com.pleoslog.domain.vehicle.entity.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VehicleRepository extends JpaRepository<Vehicle, Long> {
    Optional<Vehicle> findByVehicleNumber(String vehicleNumber);
    Optional<Vehicle> findByPairingCode(String pairingCode);
    List<Vehicle> findByCompanyId(Long companyId);
}
