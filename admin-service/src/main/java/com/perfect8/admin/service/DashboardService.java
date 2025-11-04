package com.perfect8.admin.service;

import org.springframework.stereotype.Service;

@Service
public class DashboardService {

    public String getDashboardData() {
        return "Dashboard data";
    }

    public String getSummary() {
        return "Dashboard summary";
    }
}
