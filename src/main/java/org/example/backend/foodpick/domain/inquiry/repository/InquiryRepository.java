package org.example.backend.foodpick.domain.inquiry.repository;

import org.example.backend.foodpick.domain.inquiry.model.InquiryEntity;
import org.example.backend.foodpick.domain.inquiry.model.InquiryStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InquiryRepository extends JpaRepository<InquiryEntity, Long> {
    List<InquiryEntity> findByUserIdOrderByUpdatedAtDesc(Long userId);
    List<InquiryEntity> findAllByUser_Id(Long userId);
    List<InquiryEntity> findAllByOrderByUpdatedAtDesc();
    List<InquiryEntity> findByStatusOrderByUpdatedAtDesc(InquiryStatus status);
}
