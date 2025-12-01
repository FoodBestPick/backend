package org.example.backend.foodpick.domain.tag.repository;

import org.example.backend.foodpick.domain.tag.model.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.List;

public interface TagRepository extends JpaRepository<Tag, Long> {
    Optional<Tag> findByName(String name);
    Page<Tag> findByNameContaining(String name, Pageable pageable);
    
    // ✅ 카테고리별 조회 추가
    List<Tag> findByCategory(String category);
}