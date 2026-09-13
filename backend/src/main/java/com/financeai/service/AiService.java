package com.financeai.service;

import com.financeai.dto.request.AiChatRequest;
import com.financeai.dto.response.AiCategoryResponse;
import com.financeai.dto.response.AiChatResponse;
import com.financeai.dto.response.BudgetResponse;
import com.financeai.entity.Expense;
import com.financeai.entity.Income;
import com.financeai.repository.BudgetRepository;
import com.financeai.repository.ExpenseRepository;
import com.financeai.repository.IncomeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.*;

@Service
public class AiService {

    private static final Logger log = LoggerFactory.getLogger(AiService.class);

    private static final List<String> VALID_CATEGORIES = List.of(
            "FOOD_DINING", "TRANSPORTATION", "HOUSING", "UTILITIES", "HEALTHCARE",
            "ENTERTAINMENT", "SHOPPING", "EDUCATION", "TRAVEL", "PERSONAL_CARE",
            "INSURANCE", "SAVINGS", "OTHER");

    private final String apiKey;
    private final String apiUrl;
    private final RestTemplate restTemplate;
    private final ExpenseRepository expenseRepository;
    private final IncomeRepository incomeRepository;
    private final BudgetRepository budgetRepository;
    private final BudgetService budgetService;

    public AiService(@Value("${gemini.api.key}") String apiKey,
                     @Value("${gemini.api.url}") String apiUrl,
                     RestTemplate restTemplate,
                     ExpenseRepository expenseRepository,
                     IncomeRepository incomeRepository,
                     BudgetRepository budgetRepository,
                     BudgetService budgetService) {
        this.apiKey = apiKey;
        this.apiUrl = apiUrl;
        this.restTemplate = restTemplate;
        this.expenseRepository = expenseRepository;
        this.incomeRepository = incomeRepository;
        this.budgetRepository = budgetRepository;
        this.budgetService = budgetService;
    }

    public AiCategoryResponse suggestCategory(String description) {
        String prompt = "You are a personal finance assistant. Given this expense description, "
                + "suggest the most appropriate category from this exact list:\n"
                + "FOOD_DINING, TRANSPORTATION, HOUSING, UTILITIES, HEALTHCARE, "
                + "ENTERTAINMENT, SHOPPING, EDUCATION, TRAVEL, PERSONAL_CARE, "
                + "INSURANCE, SAVINGS, OTHER\n\n"
                + "Expense description: \"" + description + "\"\n\n"
                + "Respond with ONLY the category name from the list, nothing else.";
        try {
            String response = callGemini(List.of(buildMessage("user", prompt)));
            String category = response.trim().toUpperCase().replaceAll("[^A-Z_]", "");
            if (!VALID_CATEGORIES.contains(category)) category = "OTHER";
            return new AiCategoryResponse(category);
        } catch (Exception e) {
            log.error("AI categorization failed", e);
            return new AiCategoryResponse("OTHER");
        }
    }

    public AiChatResponse chat(Long userId, String userName, AiChatRequest request) {
        String context = buildFinancialContext(userId, userName);
        List<Map<String, Object>> contents = new ArrayList<>();

        List<AiChatRequest.ConversationMessage> history = request.getConversationHistory();
        if (history == null || history.isEmpty()) {
            contents.add(buildMessage("user", context + "\n\nUser question: " + request.getMessage()));
        } else {
            contents.add(buildMessage("user", context));
            contents.add(buildMessage("model", "Understood. I have your financial data and will answer based only on it."));
            for (AiChatRequest.ConversationMessage msg : history) {
                String role = "user".equalsIgnoreCase(msg.getRole()) ? "user" : "model";
                contents.add(buildMessage(role, msg.getContent()));
            }
            contents.add(buildMessage("user", request.getMessage()));
        }

        try {
            return new AiChatResponse(callGemini(contents).trim());
        } catch (Exception e) {
            log.error("AI chat failed", e);
            return new AiChatResponse("I'm sorry, I'm unable to process your request right now. Please try again later.");
        }
    }

    private String buildFinancialContext(Long userId, String userName) {
        LocalDate now   = LocalDate.now();
        YearMonth ym    = YearMonth.of(now.getYear(), now.getMonthValue());
        LocalDate start = ym.atDay(1);
        LocalDate end   = ym.atEndOfMonth();
        String monthName = ym.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH);

        BigDecimal totalExpenses = expenseRepository.sumByUserAndDateRange(userId, start, end);
        BigDecimal totalIncome   = incomeRepository.sumByUserAndDateRange(userId, start, end);
        if (totalExpenses == null) totalExpenses = BigDecimal.ZERO;
        if (totalIncome == null)   totalIncome   = BigDecimal.ZERO;

        StringBuilder categorySection = new StringBuilder();
        for (Object[] row : expenseRepository.findCategoryTotals(userId, start, end)) {
            categorySection.append(String.format("  - %s: $%.2f%n", row[0], row[1]));
        }

        StringBuilder budgetSection = new StringBuilder();
        for (BudgetResponse b : budgetRepository.findByUserIdAndMonthAndYear(userId, ym.getMonthValue(), ym.getYear())
                .stream().map(bud -> budgetService.toResponse(bud, userId)).toList()) {
            budgetSection.append(String.format("  - %s: $%.2f limit, $%.2f spent (%.1f%%)%s%n",
                    b.category(), b.limitAmount(), b.spentAmount(),
                    b.utilizationPercent(), b.isExceeded() ? " [EXCEEDED]" : ""));
        }

        StringBuilder recentSection = new StringBuilder();
        for (Expense e : expenseRepository.findRecentByUserId(userId, PageRequest.of(0, 5))) {
            recentSection.append(String.format("  - %s: %s $%.2f (%s)%n",
                    e.getDate(), e.getDescription(), e.getAmount(), e.getCategory()));
        }

        return "You are a personal finance assistant for " + userName + ".\n"
                + "Use ONLY the data below to answer. Do not invent or estimate financial figures.\n"
                + "If you don't have enough data, say so honestly.\n\n"
                + "=== FINANCIAL DATA FOR " + monthName + " " + ym.getYear() + " ===\n\n"
                + "TOTAL INCOME: $" + String.format("%.2f", totalIncome) + "\n"
                + "TOTAL EXPENSES: $" + String.format("%.2f", totalExpenses) + "\n"
                + "NET BALANCE: $" + String.format("%.2f", totalIncome.subtract(totalExpenses)) + "\n\n"
                + "EXPENSES BY CATEGORY:\n" + (categorySection.isEmpty() ? "  No expenses this month.\n" : categorySection)
                + "\nBUDGETS:\n" + (budgetSection.isEmpty() ? "  No budgets set.\n" : budgetSection)
                + "\nRECENT TRANSACTIONS:\n" + (recentSection.isEmpty() ? "  No recent transactions.\n" : recentSection);
    }

    @SuppressWarnings("unchecked")
    private String callGemini(List<Map<String, Object>> contents) {
        String url = apiUrl + "?key=" + apiKey;
        Map<String, Object> body = new HashMap<>();
        body.put("contents", contents);
        body.put("generationConfig", Map.of("temperature", 0.3, "maxOutputTokens", 1024));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        ResponseEntity<Map> response = restTemplate.exchange(
                url, HttpMethod.POST, new HttpEntity<>(body, headers), Map.class);

        if (response.getBody() == null) throw new RuntimeException("Empty Gemini response");

        List<Map<String, Object>> candidates = (List<Map<String, Object>>) response.getBody().get("candidates");
        if (candidates == null || candidates.isEmpty()) throw new RuntimeException("No candidates");

        Map<String, Object> content = (Map<String, Object>) candidates.get(0).get("content");
        List<Map<String, Object>> parts = (List<Map<String, Object>>) content.get("parts");
        return (String) parts.get(0).get("text");
    }

    private Map<String, Object> buildMessage(String role, String text) {
        return Map.of("role", role, "parts", List.of(Map.of("text", text)));
    }
}
