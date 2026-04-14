package com.ailearning.platform.repository;

import com.ailearning.platform.entity.UserAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface UserAttemptRepository extends JpaRepository<UserAttempt, UUID> {
    List<UserAttempt> findByUserIdAndQuestionIdOrderByCreatedAtDesc(UUID userId, UUID questionId);

    List<UserAttempt> findByUserIdOrderByCreatedAtDesc(UUID userId);

    long countByUserId(UUID userId);

    long countByUserIdAndCorrectTrue(UUID userId);

    @Query("SELECT COUNT(ua) FROM UserAttempt ua WHERE ua.user.id = :userId AND ua.question.concept.id = :conceptId AND ua.correct = true")
    long countCorrectAttempts(@Param("userId") UUID userId, @Param("conceptId") UUID conceptId);

    @Query("SELECT COUNT(ua) FROM UserAttempt ua WHERE ua.user.id = :userId AND ua.question.concept.id = :conceptId")
    long countTotalAttempts(@Param("userId") UUID userId, @Param("conceptId") UUID conceptId);

    @Query("SELECT COUNT(ua) FROM UserAttempt ua WHERE ua.user.id = :userId AND ua.question.concept.topic.module.course.id = :courseId")
    long countByCourseAndUser(@Param("userId") UUID userId, @Param("courseId") UUID courseId);
}
