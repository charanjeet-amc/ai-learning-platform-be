package com.ailearning.platform.repository;

import com.ailearning.platform.entity.Payment;
import com.ailearning.platform.entity.enums.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, UUID> {
    List<Payment> findByUserIdOrderByCreatedAtDesc(UUID userId);
    Optional<Payment> findByStripePaymentIntentId(String paymentIntentId);
    Optional<Payment> findByStripeSessionId(String sessionId);
    List<Payment> findByUserIdAndStatus(UUID userId, PaymentStatus status);
}
