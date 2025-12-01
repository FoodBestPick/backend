package org.example.backend.foodpick.domain.tag.repository;

import org.example.backend.foodpick.domain.tag.model.TagBridge;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TagBridgeRepository extends JpaRepository<TagBridge, Long> {
    void deleteAllByTag_Id(Long tagId);
    List<TagBridge> findAllByRestaurant_Id(Long restaurantId);
    void deleteAllByRestaurant_Id(Long restaurantId);
    List<TagBridge> findAllByTag_IdIn(List<Long> tagIds);
}