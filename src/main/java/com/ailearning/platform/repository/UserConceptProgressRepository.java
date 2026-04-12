package com.ailearning.platform.repository;

import com.ailearning.platform.entity.UserConceptProgress;
import com.ailearning.platform.entity.enums.ConceptStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserConceptProgressRepository extends JpaRepository<UserConceptProgress, UUID> {

    Optional<UserConceptProgress> findByUserIdAndConceptId(UUID userId, UUID conceptId);

    List<UserConceptProgress> findByUserIdAndStatus(UUID userId, ConceptStatus status);

    @Query("SELECT ucp FROM UserConceptProgress ucp " +
           "WHERE ucp.user.id = :userId AND ucp.concept.topic.module.course.id = :courseId")
    List<UserConceptProgress> findByUserIdAndCourseId(
            @Param("userId") UUID userId, @Param("courseId") UUID courseId);

    @Query("SELECT COUNT(ucp) FROM UserConceptProgress ucp " +
           "WHERE ucp.user.id = :userId AND ucp.concept.topic.module.course.id = :courseId " +
           "AND ucp.status = 'MASTERED'")
    long countMasteredByUserAndCourse(@Param("userId") UUID userId, @Param("courseId") UUID courseId);

    @Query("SELECT ucp FROM UserConceptProgress ucp " +
           "WHERE ucp.user.id = :userId AND ucp.masteryLevel < :threshold " +
           "ORDER BY ucp.masteryLevel ASC")
    List<UserConceptProgress> findWeakConcepts(@Param("userId") UUID userId, @Param("threshold") double threshold);
}
