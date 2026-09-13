package com.financeai.config;

import com.financeai.entity.Budget;
import com.financeai.entity.Expense;
import com.financeai.entity.Income;
import com.financeai.entity.User;
import com.financeai.repository.BudgetRepository;
import com.financeai.repository.ExpenseRepository;
import com.financeai.repository.IncomeRepository;
import com.financeai.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.time.LocalDate;

@Configuration
@Profile("dev")
public class DataLoader {

    private static final Logger log = LoggerFactory.getLogger(DataLoader.class);

    @Bean
    public CommandLineRunner loadSampleData(
            UserRepository userRepo,
            ExpenseRepository expenseRepo,
            IncomeRepository incomeRepo,
            BudgetRepository budgetRepo,
            PasswordEncoder passwordEncoder) {

        return args -> {
            if (userRepo.existsByEmail("demo@example.com")) return;

            User user = User.builder()
                    .name("Demo User")
                    .email("demo@example.com")
                    .password(passwordEncoder.encode("password123"))
                    .build();
            user = userRepo.save(user);
            log.info("Sample data: created demo user (demo@example.com / password123)");

            LocalDate start = LocalDate.now().withDayOfMonth(1);

            Expense[] expenses = {
                Expense.builder().description("Monthly rent").amount(new BigDecimal("1200.00")).category("HOUSING").date(start).user(user).build(),
                Expense.builder().description("Grocery shopping").amount(new BigDecimal("85.50")).category("FOOD_DINING").date(start.plusDays(2)).notes("Weekly groceries").user(user).build(),
                Expense.builder().description("Uber to office").amount(new BigDecimal("12.00")).category("TRANSPORTATION").date(start.plusDays(3)).user(user).build(),
                Expense.builder().description("Netflix subscription").amount(new BigDecimal("15.99")).category("ENTERTAINMENT").date(start.plusDays(4)).user(user).build(),
                Expense.builder().description("Electricity bill").amount(new BigDecimal("78.00")).category("UTILITIES").date(start.plusDays(5)).user(user).build(),
                Expense.builder().description("Restaurant dinner").amount(new BigDecimal("65.00")).category("FOOD_DINING").date(start.plusDays(6)).notes("Date night").user(user).build(),
                Expense.builder().description("Gym membership").amount(new BigDecimal("40.00")).category("PERSONAL_CARE").date(start.plusDays(7)).user(user).build(),
                Expense.builder().description("Amazon purchase").amount(new BigDecimal("55.00")).category("SHOPPING").date(start.plusDays(8)).user(user).build(),
                Expense.builder().description("Doctor visit").amount(new BigDecimal("30.00")).category("HEALTHCARE").date(start.plusDays(9)).notes("Copay").user(user).build(),
                Expense.builder().description("Coffee shop").amount(new BigDecimal("24.50")).category("FOOD_DINING").date(start.plusDays(10)).user(user).build(),
            };
            for (Expense e : expenses) expenseRepo.save(e);

            Income[] incomes = {
                Income.builder().description("Monthly salary").amount(new BigDecimal("4500.00")).source("SALARY").date(start).notes("Net after tax").user(user).build(),
                Income.builder().description("Freelance project").amount(new BigDecimal("800.00")).source("FREELANCE").date(start.plusDays(4)).notes("Web design project").user(user).build(),
            };
            for (Income i : incomes) incomeRepo.save(i);

            int month = start.getMonthValue();
            int year  = start.getYear();
            Budget[] budgets = {
                Budget.builder().category("FOOD_DINING").limitAmount(new BigDecimal("400.00")).month(month).year(year).user(user).build(),
                Budget.builder().category("TRANSPORTATION").limitAmount(new BigDecimal("100.00")).month(month).year(year).user(user).build(),
                Budget.builder().category("HOUSING").limitAmount(new BigDecimal("1300.00")).month(month).year(year).user(user).build(),
                Budget.builder().category("ENTERTAINMENT").limitAmount(new BigDecimal("50.00")).month(month).year(year).user(user).build(),
                Budget.builder().category("UTILITIES").limitAmount(new BigDecimal("120.00")).month(month).year(year).user(user).build(),
            };
            for (Budget b : budgets) budgetRepo.save(b);

            log.info("Sample data loaded: 10 expenses, 2 incomes, 5 budgets for demo@example.com");
        };
    }
}
