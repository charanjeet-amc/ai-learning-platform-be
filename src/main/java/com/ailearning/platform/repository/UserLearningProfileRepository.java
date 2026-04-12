package com.ailearning.platform.repository;

import com.ailearning.platform.entity.UserLearningProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface UserLearningProfileRepository extends JpaRepository<UserLearningProfile, UUID> {
}
