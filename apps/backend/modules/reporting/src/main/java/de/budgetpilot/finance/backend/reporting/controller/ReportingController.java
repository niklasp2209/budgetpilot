package de.budgetpilot.finance.backend.reporting.controller;

import de.budgetpilot.finance.backend.reporting.dto.BudgetVsActualReportResponse;
import de.budgetpilot.finance.backend.reporting.dto.CashflowReportResponse;
import de.budgetpilot.finance.backend.reporting.dto.CategoryAmountResponse;
import de.budgetpilot.finance.backend.reporting.service.ReportingService;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

/**
 * @author Niklas Petermeier
 * @since 11.05.2026
 */
@RestController
@RequiredArgsConstructor
public class ReportingController {
    private final ReportingService reportingService;

    @GetMapping("/api/v1/organizations/{organizationId}/reports/cashflow")
    /**
     * Returns income, expense, and net totals for a date range.
     *
     * @param organizationId organization identifier
     * @param from optional range start
     * @param to optional range end
     * @param jwt authenticated JWT token
     * @return cashflow report
     */
    public @NonNull CashflowReportResponse cashflow(
            @PathVariable @NonNull UUID organizationId,
            @RequestParam(name = "from", required = false) @Nullable OffsetDateTime from,
            @RequestParam(name = "to", required = false) @Nullable OffsetDateTime to,
            @AuthenticationPrincipal @NonNull Jwt jwt
    ) {
        return reportingService.cashflow(organizationId, extractEmail(jwt), from, to);
    }

    @GetMapping("/api/v1/organizations/{organizationId}/reports/by-category")
    /**
     * Returns expense totals grouped by category for a date range.
     *
     * @param organizationId organization identifier
     * @param from optional range start
     * @param to optional range end
     * @param jwt authenticated JWT token
     * @return expense amounts per category
     */
    public @NonNull List<CategoryAmountResponse> byCategory(
            @PathVariable @NonNull UUID organizationId,
            @RequestParam(name = "from", required = false) @Nullable OffsetDateTime from,
            @RequestParam(name = "to", required = false) @Nullable OffsetDateTime to,
            @AuthenticationPrincipal @NonNull Jwt jwt
    ) {
        return reportingService.byCategory(organizationId, extractEmail(jwt), from, to);
    }

    @GetMapping("/api/v1/organizations/{organizationId}/reports/budget-vs-actual")
    /**
     * Compares budgeted and actual expense amounts per category for one budget month.
     *
     * @param organizationId organization identifier
     * @param budgetId budget identifier
     * @param jwt authenticated JWT token
     * @return budget versus actual report
     */
    public @NonNull BudgetVsActualReportResponse budgetVsActual(
            @PathVariable @NonNull UUID organizationId,
            @RequestParam @NonNull UUID budgetId,
            @AuthenticationPrincipal @NonNull Jwt jwt
    ) {
        return reportingService.budgetVsActual(organizationId, budgetId, extractEmail(jwt));
    }

    /**
     * Extracts email from JWT subject.
     *
     * @param jwt authenticated JWT token
     * @return subject email
     */
    private @NonNull String extractEmail(@NonNull Jwt jwt) {
        String subject = jwt.getSubject();
        if (subject == null || subject.isBlank()) {
            throw new IllegalStateException("JWT subject is missing.");
        }
        return subject;
    }
}
