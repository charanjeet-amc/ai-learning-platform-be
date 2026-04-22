package com.ailearning.platform.repository;

import com.ailearning.platform.entity.Certificate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CertificateRepository extends JpaRepository<Certificate, UUID> {
    Optional<Certificate> findByUserIdAndCourseId(UUID userId, UUID courseId);
    Optional<Certificate> findByVerificationCode(String verificationCode);
    List<Certificate> findByUserIdOrderByIssuedAtDesc(UUID userId);
}
