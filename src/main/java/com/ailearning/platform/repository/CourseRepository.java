package com.ailearning.platform.repository;

import com.ailearning.platform.entity.Course;
import com.ailearning.platform.entity.enums.DifficultyLevel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CourseRepository extends JpaRepository<Course, UUID> {

    Page<Course> findByPublishedTrue(Pageable pageable);

    Page<Course> findByPublishedTrueAndDifficulty(DifficultyLevel difficulty, Pageable pageable);

    @Query("SELECT c FROM Course c WHERE c.published = true AND " +
           "(LOWER(c.title) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(c.description) LIKE LOWER(CONCAT('%', :query, '%')))")
    Page<Course> searchCourses(@Param("query") String query, Pageable pageable);

    @Query("SELECT c FROM Course c WHERE c.published = true AND c.industryVertical = :vertical")
    Page<Course> findByIndustryVertical(@Param("vertical") String vertical, Pageable pageable);

    List<Course> findByCreatedById(UUID userId);

    boolean existsBySlug(String slug);

    @Query("SELECT c FROM Course c WHERE c.published = true ORDER BY c.enrollmentCount DESC")
    List<Course> findPopularCourses(Pageable pageable);
}
