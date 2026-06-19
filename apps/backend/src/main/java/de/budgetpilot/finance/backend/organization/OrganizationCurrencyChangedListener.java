package de.budgetpilot.finance.backend.organization;

import de.budgetpilot.finance.backend.accounting.repository.AccountRepository;
import de.budgetpilot.finance.backend.accounting.repository.TransactionRepository;
import de.budgetpilot.finance.backend.budget.repository.BudgetRepository;
import de.budgetpilot.finance.backend.organization.event.OrganizationCurrencyChangedEvent;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Niklas Petermeier
 * @since 19.06.2026
 */
@Component
@RequiredArgsConstructor
public class OrganizationCurrencyChangedListener {
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final BudgetRepository budgetRepository;

    @EventListener
    @Transactional
    public void onOrganizationCurrencyChanged(@NonNull OrganizationCurrencyChangedEvent event) {
        accountRepository.updateCurrencyByOrganizationId(event.organizationId(), event.currency());
        transactionRepository.updateCurrencyByOrganizationId(event.organizationId(), event.currency());
        budgetRepository.updateCurrencyByOrganizationId(event.organizationId(), event.currency());
    }
}
