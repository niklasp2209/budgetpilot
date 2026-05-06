package de.budgetpilot.finance.backend.accounting.mapper;

import de.budgetpilot.finance.backend.accounting.domain.AccountEntity;
import de.budgetpilot.finance.backend.accounting.domain.CategoryEntity;
import de.budgetpilot.finance.backend.accounting.domain.TransactionEntity;
import de.budgetpilot.finance.backend.accounting.dto.AccountResponse;
import de.budgetpilot.finance.backend.accounting.dto.CategoryResponse;
import de.budgetpilot.finance.backend.accounting.dto.TransactionResponse;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

/**
 * @author Niklas Petermeier
 * @since 06.05.2026
 */
@Component
public class AccountingMapper {
    public @NonNull AccountResponse toAccountResponse(@NonNull AccountEntity entity) {
        return new AccountResponse(entity.getId(), entity.getName(), entity.getCurrency());
    }

    public @NonNull CategoryResponse toCategoryResponse(@NonNull CategoryEntity entity) {
        return new CategoryResponse(entity.getId(), entity.getName(), entity.getType());
    }

    public @NonNull TransactionResponse toTransactionResponse(@NonNull TransactionEntity entity) {
        return new TransactionResponse(
                entity.getId(),
                entity.getAccountId(),
                entity.getCategoryId(),
                entity.getAmountCents(),
                entity.getCurrency(),
                entity.getBookedAt(),
                entity.getDescription()
        );
    }
}

