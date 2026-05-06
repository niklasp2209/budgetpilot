package de.budgetpilot.finance.backend.accounting.controller;

import de.budgetpilot.finance.backend.accounting.dto.*;
import de.budgetpilot.finance.backend.accounting.mapper.AccountingMapper;
import de.budgetpilot.finance.backend.accounting.service.AccountingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

/**
 * @author Niklas Petermeier
 * @since 06.05.2026
 */
@RestController
@RequiredArgsConstructor
public class AccountingController {
    private final AccountingService accountingService;
    private final AccountingMapper accountingMapper;

    @PostMapping("/api/v1/organizations/{organizationId}/accounts")
    @ResponseStatus(HttpStatus.CREATED)
    public @NonNull AccountResponse createAccount(
            @PathVariable @NonNull UUID organizationId,
            @Valid @RequestBody @NonNull CreateAccountRequest request,
            @AuthenticationPrincipal @NonNull Jwt jwt
    ) {
        return accountingMapper.toAccountResponse(
                accountingService.createAccount(organizationId, extractEmail(jwt), request)
        );
    }

    @GetMapping("/api/v1/organizations/{organizationId}/accounts")
    public @NonNull List<AccountResponse> listAccounts(
            @PathVariable @NonNull UUID organizationId,
            @AuthenticationPrincipal @NonNull Jwt jwt
    ) {
        return accountingService.listAccounts(organizationId, extractEmail(jwt)).stream()
                .map(accountingMapper::toAccountResponse)
                .toList();
    }

    @PostMapping("/api/v1/organizations/{organizationId}/categories")
    @ResponseStatus(HttpStatus.CREATED)
    public @NonNull CategoryResponse createCategory(
            @PathVariable @NonNull UUID organizationId,
            @Valid @RequestBody @NonNull CreateCategoryRequest request,
            @AuthenticationPrincipal @NonNull Jwt jwt
    ) {
        return accountingMapper.toCategoryResponse(
                accountingService.createCategory(organizationId, extractEmail(jwt), request)
        );
    }

    @GetMapping("/api/v1/organizations/{organizationId}/categories")
    public @NonNull List<CategoryResponse> listCategories(
            @PathVariable @NonNull UUID organizationId,
            @AuthenticationPrincipal @NonNull Jwt jwt
    ) {
        return accountingService.listCategories(organizationId, extractEmail(jwt)).stream()
                .map(accountingMapper::toCategoryResponse)
                .toList();
    }

    @PostMapping("/api/v1/organizations/{organizationId}/transactions")
    @ResponseStatus(HttpStatus.CREATED)
    public @NonNull TransactionResponse createTransaction(
            @PathVariable @NonNull UUID organizationId,
            @Valid @RequestBody @NonNull CreateTransactionRequest request,
            @AuthenticationPrincipal @NonNull Jwt jwt
    ) {
        return accountingMapper.toTransactionResponse(
                accountingService.createTransaction(organizationId, extractEmail(jwt), request)
        );
    }

    @GetMapping("/api/v1/organizations/{organizationId}/transactions")
    public @NonNull List<TransactionResponse> listTransactions(
            @PathVariable @NonNull UUID organizationId,
            @RequestParam(name = "from", required = false) @Nullable OffsetDateTime from,
            @RequestParam(name = "to", required = false) @Nullable OffsetDateTime to,
            @AuthenticationPrincipal @NonNull Jwt jwt
    ) {
        return accountingService.listTransactions(organizationId, extractEmail(jwt), from, to).stream()
                .map(accountingMapper::toTransactionResponse)
                .toList();
    }

    private @NonNull String extractEmail(@NonNull Jwt jwt) {
        String subject = jwt.getSubject();
        if (subject == null || subject.isBlank()) {
            throw new IllegalStateException("JWT subject is missing.");
        }
        return subject;
    }
}

