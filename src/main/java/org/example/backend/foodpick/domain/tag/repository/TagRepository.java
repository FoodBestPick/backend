package org.example.backend.foodpick.domain.tag.repository;

import org.example.backend.foodpick.domain.tag.model.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TagRepository extends JpaRepository<Tag, Long> {
    Optional<Tag> findByName(String name);
    Page<Tag> findByNameContaining(String name, Pageable pageable);
}