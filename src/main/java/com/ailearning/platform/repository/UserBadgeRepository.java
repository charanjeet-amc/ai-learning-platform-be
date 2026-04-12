package com.ailearning.platform.repository;

import com.ailearning.platform.entity.UserBadge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface UserBadgeRepository extends JpaRepository<UserBadge, UUID> {
    List<UserBadge> findByUserIdOrderByEarnedAtDesc(UUID userId);
    boolean existsByUserIdAndBadgeId(UUID userId, UUID badgeId);
}
