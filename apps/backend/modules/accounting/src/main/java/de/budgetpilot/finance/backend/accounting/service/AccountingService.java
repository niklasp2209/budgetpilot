package de.budgetpilot.finance.backend.accounting.service;

import de.budgetpilot.finance.backend.accounting.domain.AccountEntity;
import de.budgetpilot.finance.backend.accounting.domain.CategoryEntity;
import de.budgetpilot.finance.backend.accounting.domain.TransactionEntity;
import de.budgetpilot.finance.backend.accounting.dto.CreateAccountRequest;
import de.budgetpilot.finance.backend.accounting.dto.CreateCategoryRequest;
import de.budgetpilot.finance.backend.accounting.dto.CreateTransactionRequest;
import de.budgetpilot.finance.backend.accounting.exception.AccountingNotFoundException;
import de.budgetpilot.finance.backend.accounting.repository.AccountRepository;
import de.budgetpilot.finance.backend.accounting.repository.CategoryRepository;
import de.budgetpilot.finance.backend.accounting.repository.TransactionRepository;
import de.budgetpilot.finance.backend.organization.authorization.OrganizationAuthorizationService;
import de.budgetpilot.finance.backend.organization.authorization.OrganizationPermission;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/**
 * @author Niklas Petermeier
 * @since 06.05.2026
 */
@Service
@RequiredArgsConstructor
public class AccountingService {
    private final AccountRepository accountRepository;
    private final CategoryRepository categoryRepository;
    private final TransactionRepository transactionRepository;
    private final OrganizationAuthorizationService organizationAuthorizationService;

    @Transactional
    public @NonNull AccountEntity createAccount(
            @NonNull UUID organizationId,
            @NonNull String authenticatedEmail,
            @NonNull CreateAccountRequest request
    ) {
        organizationAuthorizationService.requirePermission(
                organizationId, authenticatedEmail, OrganizationPermission.ACCOUNTING_WRITE
        );
        String name = request.name().trim();
        String currency = request.currency().trim().toUpperCase(Locale.ROOT);
        return accountRepository.save(AccountEntity.createNew(organizationId, name, currency));
    }

    @Transactional(readOnly = true)
    public @NonNull List<AccountEntity> listAccounts(@NonNull UUID organizationId, @NonNull String authenticatedEmail) {
        organizationAuthorizationService.requirePermission(
                organizationId, authenticatedEmail, OrganizationPermission.ACCOUNTING_READ
        );
        return accountRepository.findByOrganizationId(organizationId);
    }

    @Transactional
    public @NonNull CategoryEntity createCategory(
            @NonNull UUID organizationId,
            @NonNull String authenticatedEmail,
            @NonNull CreateCategoryRequest request
    ) {
        organizationAuthorizationService.requirePermission(
                organizationId, authenticatedEmail, OrganizationPermission.ACCOUNTING_WRITE
        );
        return categoryRepository.save(CategoryEntity.createNew(organizationId, request.name().trim(), request.type()));
    }

    @Transactional(readOnly = true)
    public @NonNull List<CategoryEntity> listCategories(@NonNull UUID organizationId, @NonNull String authenticatedEmail) {
        organizationAuthorizationService.requirePermission(
                organizationId, authenticatedEmail, OrganizationPermission.ACCOUNTING_READ
        );
        return categoryRepository.findByOrganizationId(organizationId);
    }

    @Transactional
    public @NonNull TransactionEntity createTransaction(
            @NonNull UUID organizationId,
            @NonNull String authenticatedEmail,
            @NonNull CreateTransactionRequest request
    ) {
        organizationAuthorizationService.requirePermission(
                organizationId, authenticatedEmail, OrganizationPermission.ACCOUNTING_WRITE
        );

        AccountEntity account = accountRepository.findById(request.accountId())
                .filter(value -> value.getOrganizationId().equals(organizationId))
                .orElseThrow(() -> new AccountingNotFoundException("Account not found."));

        CategoryEntity category = categoryRepository.findById(request.categoryId())
                .filter(value -> value.getOrganizationId().equals(organizationId))
                .orElseThrow(() -> new AccountingNotFoundException("Category not found."));

        String currency = request.currency().trim().toUpperCase(Locale.ROOT);
        if (!account.getCurrency().equals(currency)) {
            throw new IllegalArgumentException("Transaction currency must match account currency.");
        }

        return transactionRepository.save(TransactionEntity.createNew(
                organizationId,
                account.getId(),
                category.getId(),
                request.amountCents(),
                currency,
                request.bookedAt(),
                request.description()
        ));
    }

    @Transactional(readOnly = true)
    public @NonNull List<TransactionEntity> listTransactions(
            @NonNull UUID organizationId,
            @NonNull String authenticatedEmail,
            @Nullable OffsetDateTime from,
            @Nullable OffsetDateTime to
    ) {
        organizationAuthorizationService.requirePermission(
                organizationId, authenticatedEmail, OrganizationPermission.ACCOUNTING_READ
        );
        OffsetDateTime effectiveFrom = from != null ? from : OffsetDateTime.now().minusDays(30);
        OffsetDateTime effectiveTo = to != null ? to : OffsetDateTime.now().plusDays(1);
        return transactionRepository.findByOrganizationIdAndBookedAtBetween(organizationId, effectiveFrom, effectiveTo);
    }
}
