package com.ailearning.platform.repository;

import com.ailearning.platform.entity.UserWeakArea;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserWeakAreaRepository extends JpaRepository<UserWeakArea, UUID> {

    @Query("SELECT wa FROM UserWeakArea wa JOIN FETCH wa.concept " +
           "WHERE wa.user.id = :userId AND wa.weaknessScore > 0 " +
           "ORDER BY wa.weaknessScore DESC")
    List<UserWeakArea> findWeakAreasByUserId(@Param("userId") UUID userId);

    Optional<UserWeakArea> findByUserIdAndConceptId(UUID userId, UUID conceptId);
}
