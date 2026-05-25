package com.fintrack.controller;

import com.fintrack.dto.response.Responses.MonthlyReportResponse;
import com.fintrack.dto.response.Responses.YearlyReportResponse;
import com.fintrack.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST endpoints for financial reports and analytics.
 */
@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    /**
     * GET /api/reports/monthly/{year}/{month}
     * Returns income/expense breakdown by category for the specified month.
     */
    @GetMapping("/monthly/{year}/{month}")
    public ResponseEntity<MonthlyReportResponse> getMonthlyReport(
            @PathVariable int year,
            @PathVariable int month) {

        if (month < 1 || month > 12) {
            throw new IllegalArgumentException("Invalid month");
        }

        return ResponseEntity.ok(reportService.getMonthlyReport(year, month));
    }

    /**
     * GET /api/reports/yearly/{year}
     * Returns aggregate income/expense breakdown by category for the specified year.
     */
    @GetMapping("/yearly/{year}")
    public ResponseEntity<YearlyReportResponse> getYearlyReport(@PathVariable int year) {
        return ResponseEntity.ok(reportService.getYearlyReport(year));
    }
}
