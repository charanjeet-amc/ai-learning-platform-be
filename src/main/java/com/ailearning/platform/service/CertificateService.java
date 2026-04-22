package com.ailearning.platform.service;

import com.ailearning.platform.dto.response.CertificateResponse;

import java.util.List;
import java.util.UUID;

public interface CertificateService {
    CertificateResponse getOrGenerate(UUID userId, UUID courseId);
    List<CertificateResponse> getUserCertificates(UUID userId);
    CertificateResponse verify(String verificationCode);
    byte[] generatePdf(String verificationCode);
}
