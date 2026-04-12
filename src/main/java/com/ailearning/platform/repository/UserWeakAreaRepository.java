package com.ailearning.platform.repository;

import com.ailearning.platform.entity.UserWeakArea;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface UserWeakAreaRepository extends JpaRepository<UserWeakArea, UUID> {
    List<UserWeakArea> findByUserIdOrderByWeaknessScoreDesc(UUID userId);
}
