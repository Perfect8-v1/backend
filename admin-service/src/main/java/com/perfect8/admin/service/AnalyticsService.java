package com.perfect8.admin.service;

import org.springframework.stereotype.Service;

@Service
public class AnalyticsService {

    public String getAnalytics() {
        return "Analytics data";
    }

    public String getSalesAnalytics() {
        return "Sales analytics";
    }
}
