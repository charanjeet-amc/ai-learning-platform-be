package com.ailearning.platform.repository;

import com.ailearning.platform.entity.XPEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface XPEventRepository extends JpaRepository<XPEvent, UUID> {
    List<XPEvent> findByUserIdOrderByCreatedAtDesc(UUID userId);

    @Query("SELECT COALESCE(SUM(x.xpAmount), 0) FROM XPEvent x WHERE x.user.id = :userId")
    long sumXpByUserId(@Param("userId") UUID userId);
}
