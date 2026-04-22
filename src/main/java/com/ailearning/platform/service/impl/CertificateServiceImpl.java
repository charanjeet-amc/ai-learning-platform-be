package com.ailearning.platform.service.impl;

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
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        } catch (IOException e) {
            log.error("Failed to generate certificate PDF for code={}", verificationCode, e);
            throw new RuntimeException("Failed to generate certificate PDF", e);
        }
    }

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
        try (PDDocument doc = new PDDocument()) {
            // Landscape A4
            PDPage page = new PDPage(new PDRectangle(PDRectangle.A4.getHeight(), PDRectangle.A4.getWidth()));
            doc.addPage(page);

            float W = page.getMediaBox().getWidth();   // 841.89
            float H = page.getMediaBox().getHeight();  // 595.28

            PDFont bold = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
            PDFont regular = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
            PDFont oblique = new PDType1Font(Standard14Fonts.FontName.HELVETICA_OBLIQUE);

            try (PDPageContentStream cs = new PDPageContentStream(doc, page)) {
                // Background: deep indigo
                cs.setNonStrokingColor(30, 27, 75);
                cs.addRect(0, 0, W, H);
                cs.fill();

                // Outer gold border
                cs.setStrokingColor(212, 175, 55);
                cs.setLineWidth(3f);
                cs.addRect(20, 20, W - 40, H - 40);
                cs.stroke();

                // Inner gold border
                cs.setStrokingColor(212, 175, 55);
                cs.setLineWidth(0.8f);
                cs.addRect(28, 28, W - 56, H - 56);
                cs.stroke();

                // Platform name
                cs.setNonStrokingColor(212, 175, 55);
                drawCentered(cs, "AI LEARNING PLATFORM", bold, 13, W, H - 75);

                // Decorative line under platform name
                float lineHalf = 100f;
                cs.setStrokingColor(212, 175, 55);
                cs.setLineWidth(0.8f);
                cs.moveTo(W / 2 - lineHalf, H - 88);
                cs.lineTo(W / 2 + lineHalf, H - 88);
                cs.stroke();

                // "Certificate of Completion"
                cs.setNonStrokingColor(255, 255, 255);
                drawCentered(cs, "Certificate of Completion", bold, 36, W, H - 145);

                // "This certifies that"
                cs.setNonStrokingColor(196, 181, 253);
                drawCentered(cs, "This certifies that", oblique, 15, W, H - 195);

                // Student name in gold
                String studentName = cert.getUser().getFullName();
                cs.setNonStrokingColor(212, 175, 55);
                drawCentered(cs, studentName, bold, 40, W, H - 265);

                // Name underline
                float nameWidth = bold.getStringWidth(studentName) / 1000 * 40;
                cs.setStrokingColor(212, 175, 55);
                cs.setLineWidth(0.8f);
                cs.moveTo(W / 2 - nameWidth / 2, H - 278);
                cs.lineTo(W / 2 + nameWidth / 2, H - 278);
                cs.stroke();

                // "has successfully completed"
                cs.setNonStrokingColor(196, 181, 253);
                drawCentered(cs, "has successfully completed", oblique, 15, W, H - 315);

                // Course title in white
                String courseTitle = cert.getCourse().getTitle();
                cs.setNonStrokingColor(255, 255, 255);
                drawCentered(cs, courseTitle, bold, 22, W, H - 360);

                // Bottom separator
                cs.setStrokingColor(212, 175, 55);
                cs.setLineWidth(0.8f);
                cs.moveTo(60, 130);
                cs.lineTo(W - 60, 130);
                cs.stroke();

                // Issue date (left side)
                String dateStr = cert.getIssuedAt().format(DateTimeFormatter.ofPattern("MMMM dd, yyyy"));
                cs.setNonStrokingColor(196, 181, 253);
                drawText(cs, "Date of Issue", regular, 10, 80, 110);
                cs.setNonStrokingColor(255, 255, 255);
                drawText(cs, dateStr, bold, 12, 80, 90);

                // Verification code + URL (right side)
                String verifyUrl = frontendUrl + "/certificates/" + cert.getVerificationCode();
                String codeLabel = "Certificate ID: " + cert.getVerificationCode();
                float codeLabelWidth = regular.getStringWidth(codeLabel) / 1000 * 10;
                float urlWidth = regular.getStringWidth("Verify at: " + verifyUrl) / 1000 * 9;
                float rightEdge = W - 60;

                cs.setNonStrokingColor(196, 181, 253);
                drawText(cs, codeLabel, regular, 10, rightEdge - codeLabelWidth, 110);
                drawText(cs, "Verify at: " + verifyUrl, regular, 9, rightEdge - urlWidth, 90);
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            doc.save(baos);
            return baos.toByteArray();
        }
    }

    private void drawCentered(PDPageContentStream cs, String text, PDFont font, float size, float pageWidth, float y) throws IOException {
        float textWidth = font.getStringWidth(text) / 1000 * size;
        float x = (pageWidth - textWidth) / 2;
        drawText(cs, text, font, size, x, y);
    }

    private void drawText(PDPageContentStream cs, String text, PDFont font, float size, float x, float y) throws IOException {
        cs.beginText();
        cs.setFont(font, size);
        cs.newLineAtOffset(x, y);
        cs.showText(text);
        cs.endText();
    }
}
