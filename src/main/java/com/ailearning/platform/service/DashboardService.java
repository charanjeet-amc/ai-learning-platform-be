package com.ailearning.platform.service;

import com.ailearning.platform.dto.response.DashboardResponse;

import java.util.UUID;

public interface DashboardService {
    DashboardResponse getDashboard(UUID userId);
}
