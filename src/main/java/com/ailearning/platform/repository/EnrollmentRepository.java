package com.ailearning.platform.repository;

import com.ailearning.platform.entity.Enrollment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EnrollmentRepository extends JpaRepository<Enrollment, UUID> {
    Optional<Enrollment> findByUserIdAndCourseId(UUID userId, UUID courseId);
    List<Enrollment> findByUserIdOrderByEnrolledAtDesc(UUID userId);
    boolean existsByUserIdAndCourseId(UUID userId, UUID courseId);
    long countByCourseId(UUID courseId);
}
