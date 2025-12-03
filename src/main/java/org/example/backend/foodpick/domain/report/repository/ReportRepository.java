package org.example.backend.foodpick.domain.report.repository;

import org.example.backend.foodpick.domain.report.model.ReportEntity;
import org.example.backend.foodpick.domain.report.model.ReportStatus;
import org.example.backend.foodpick.domain.report.model.ReportTargetType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReportRepository extends JpaRepository<ReportEntity, Long> {

    Page<ReportEntity> findByStatus(ReportStatus status, Pageable pageable);

    Page<ReportEntity> findByTargetType(ReportTargetType targetType, Pageable pageable);

    Page<ReportEntity> findByStatusAndTargetType(
            ReportStatus status,
            ReportTargetType targetType,
            Pageable pageable
    );
}
