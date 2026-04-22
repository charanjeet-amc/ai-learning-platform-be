package com.ailearning.platform.service.impl;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import com.ailearning.platform.dto.response.CertificateResponse;
import com.ailearning.platform.entity.Certificate;
import com.ailearning.platform.entity.Course;
import com.ailearning.platform.entity.Enrollment;
import com.ailearning.platform.entity.User;
import com.ailearning.platform.exception.ResourceNotFoundException;
import com.ailearning.platform.repository.CertificateRepository;
import com.ailearning.platform.repository.CourseRepository;
import com.ailearning.platform.repository.EnrollmentRepository;
import com.ailearning.platform.repository.UserRepository;
import com.ailearning.platform.service.CertificateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class CertificateServiceImpl implements CertificateService {

    private final CertificateRepository certificateRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final TemplateEngine templateEngine;

    @Value("${app.frontend-url:https://ai-learning-platform-ui-pi.vercel.app}")
    private String frontendUrl;

    @Override
    @Transactional
    public CertificateResponse getOrGenerate(UUID userId, UUID courseId) {
        return certificateRepository.findByUserIdAndCourseId(userId, courseId)
                .map(this::toResponse)
                .orElseGet(() -> {
                    Enrollment enrollment = enrollmentRepository
                            .findByUserIdAndCourseId(userId, courseId)
                            .orElseThrow(() -> new IllegalStateException("Not enrolled in this course"));

                    if (!Boolean.TRUE.equals(enrollment.getCompleted())) {
                        throw new IllegalStateException("Course not yet completed");
                    }

                    User user = userRepository.findById(userId)
                            .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
                    Course course = courseRepository.findById(courseId)
                            .orElseThrow(() -> new ResourceNotFoundException("Course", "id", courseId));

                    Certificate cert = Certificate.builder()
                            .user(user)
                            .course(course)
                            .verificationCode(UUID.randomUUID().toString())
                            .issuedAt(LocalDateTime.now())
                            .completedAt(enrollment.getCompletedAt())
                            .progressPercent(enrollment.getProgressPercent())
                            .build();

                    return toResponse(certificateRepository.save(cert));
                });
    }

    @Override
    @Transactional(readOnly = true)
    public List<CertificateResponse> getUserCertificates(UUID userId) {
        return certificateRepository.findByUserIdOrderByIssuedAtDesc(userId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public CertificateResponse verify(String verificationCode) {
        Certificate cert = certificateRepository.findByVerificationCode(verificationCode)
                .orElseThrow(() -> new ResourceNotFoundException("Certificate", "verificationCode", verificationCode));
        return toResponse(cert);
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] generatePdf(String verificationCode) {
        Certificate cert = certificateRepository.findByVerificationCode(verificationCode)
                .orElseThrow(() -> new ResourceNotFoundException("Certificate", "verificationCode", verificationCode));
        try {
            return buildPdf(cert);
        } catch (Exception e) {
            log.error("Failed to generate certificate PDF for code={}", verificationCode, e);
            throw new RuntimeException("Failed to generate certificate PDF", e);
        }
    }

    // ── Private helpers ────────────────────────────────────────────────────────

    private CertificateResponse toResponse(Certificate cert) {
        String verifyUrl = frontendUrl + "/certificates/" + cert.getVerificationCode();
        return new CertificateResponse(
                cert.getId(),
                cert.getCourse().getId().toString(),
                cert.getCourse().getTitle(),
                cert.getCourse().getThumbnailUrl(),
                cert.getUser().getFullName(),
                cert.getVerificationCode(),
                verifyUrl,
                cert.getIssuedAt(),
                cert.getCompletedAt(),
                cert.getProgressPercent()
        );
    }

    private byte[] buildPdf(Certificate cert) throws IOException {
        // 1. Populate Thymeleaf context
        Context ctx = new Context();
        ctx.setVariable("studentName", cert.getUser().getFullName());
        ctx.setVariable("courseTitle", cert.getCourse().getTitle());
        LocalDateTime completionDate = cert.getCompletedAt() != null ? cert.getCompletedAt() : cert.getIssuedAt();
        ctx.setVariable("issuedDate",
                completionDate.format(DateTimeFormatter.ofPattern("MMMM d, yyyy")));
        ctx.setVariable("verificationCode", cert.getVerificationCode());
        ctx.setVariable("verificationUrl",
                frontendUrl + "/certificates/" + cert.getVerificationCode());

        // 2. Render HTML from template
        String html = templateEngine.process("certificate", ctx);

        // 3. Convert HTML → PDF via OpenHTML to PDF (HTML/CSS → PDFBox)
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.withHtmlContent(html, null);
            builder.toStream(baos);
            builder.run();
            return baos.toByteArray();
        }
    }
}
