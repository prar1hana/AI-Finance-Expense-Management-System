package com.financeai.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.financeai.dto.request.ExpenseRequest;
import com.financeai.dto.response.ExpenseResponse;
import com.financeai.dto.response.PageResponse;
import com.financeai.entity.User;
import com.financeai.exception.ResourceNotFoundException;
import com.financeai.security.CustomUserDetailsService;
import com.financeai.security.JwtAuthenticationFilter;
import com.financeai.security.JwtTokenProvider;
import com.financeai.service.ExpenseService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ExpenseController.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE,
                classes = JwtAuthenticationFilter.class))
@ActiveProfiles("test")
class ExpenseControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockBean private ExpenseService expenseService;
    @MockBean private JwtTokenProvider jwtTokenProvider;
    @MockBean private CustomUserDetailsService customUserDetailsService;

    private User mockUser() {
        return User.builder().id(1L).name("Test").email("test@example.com").password("pw").build();
    }

    private ExpenseResponse sampleResponse() {
        return new ExpenseResponse(1L, "Lunch", new BigDecimal("12.50"),
                "FOOD_DINING", LocalDate.now(), null, LocalDateTime.now());
    }

    @Test
    void getExpenses_authenticated_returns200() throws Exception {
        PageResponse<ExpenseResponse> page = new PageResponse<>(
                List.of(sampleResponse()), 0, 10, 1L, 1, true);
        when(expenseService.getExpenses(any(), any(), any(), any(), any())).thenReturn(page);

        mockMvc.perform(get("/api/expenses").with(user(mockUser())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].description").value("Lunch"));
    }

    @Test
    void getExpenses_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/api/expenses"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void createExpense_validRequest_returns201() throws Exception {
        ExpenseRequest request = new ExpenseRequest();
        request.setDescription("Lunch");
        request.setAmount(new BigDecimal("12.50"));
        request.setCategory("FOOD_DINING");
        request.setDate(LocalDate.now());
        when(expenseService.createExpense(any(), any())).thenReturn(sampleResponse());

        mockMvc.perform(post("/api/expenses").with(user(mockUser())).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.description").value("Lunch"));
    }

    @Test
    void createExpense_missingDescription_returns400() throws Exception {
        ExpenseRequest request = new ExpenseRequest();
        request.setAmount(new BigDecimal("12.50"));
        request.setCategory("FOOD_DINING");
        request.setDate(LocalDate.now());

        mockMvc.perform(post("/api/expenses").with(user(mockUser())).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deleteExpense_notFound_returns404() throws Exception {
        org.mockito.Mockito.doThrow(new ResourceNotFoundException("Expense", 99L))
                .when(expenseService).deleteExpense(any(), eq(99L));

        mockMvc.perform(delete("/api/expenses/99").with(user(mockUser())).with(csrf()))
                .andExpect(status().isNotFound());
    }
}
