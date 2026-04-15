package com.ailearning.platform.repository;

import com.ailearning.platform.entity.InstructorApplication;
import com.ailearning.platform.entity.enums.ApplicationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface InstructorApplicationRepository extends JpaRepository<InstructorApplication, UUID> {
    Optional<InstructorApplication> findByUserId(UUID userId);
    List<InstructorApplication> findByStatusOrderByCreatedAtAsc(ApplicationStatus status);
    boolean existsByUserId(UUID userId);
}
