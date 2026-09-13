package com.financeai.service;

import com.financeai.dto.request.IncomeRequest;
import com.financeai.dto.response.IncomeResponse;
import com.financeai.dto.response.PageResponse;
import com.financeai.entity.Income;
import com.financeai.entity.User;
import com.financeai.exception.ResourceNotFoundException;
import com.financeai.repository.IncomeRepository;
import com.financeai.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
public class IncomeService {

    private final IncomeRepository incomeRepository;
    private final UserRepository userRepository;

    public IncomeService(IncomeRepository incomeRepository, UserRepository userRepository) {
        this.incomeRepository = incomeRepository;
        this.userRepository = userRepository;
    }

    public PageResponse<IncomeResponse> getIncomes(Long userId, String source,
                                                    LocalDate startDate, LocalDate endDate,
                                                    Pageable pageable) {
        Page<Income> page = incomeRepository.findByFilters(userId, source, startDate, endDate, pageable);
        return toPageResponse(page);
    }

    @Transactional
    public IncomeResponse createIncome(Long userId, IncomeRequest request) {
        User user = findUser(userId);
        Income income = Income.builder()
                .description(request.getDescription())
                .amount(request.getAmount())
                .source(request.getSource())
                .date(request.getDate())
                .notes(request.getNotes())
                .user(user)
                .build();
        return toResponse(incomeRepository.save(income));
    }

    public IncomeResponse getIncomeById(Long userId, Long incomeId) {
        return toResponse(findIncomeByIdAndUser(incomeId, userId));
    }

    @Transactional
    public IncomeResponse updateIncome(Long userId, Long incomeId, IncomeRequest request) {
        Income income = findIncomeByIdAndUser(incomeId, userId);
        income.setDescription(request.getDescription());
        income.setAmount(request.getAmount());
        income.setSource(request.getSource());
        income.setDate(request.getDate());
        income.setNotes(request.getNotes());
        return toResponse(incomeRepository.save(income));
    }

    @Transactional
    public void deleteIncome(Long userId, Long incomeId) {
        incomeRepository.delete(findIncomeByIdAndUser(incomeId, userId));
    }

    private Income findIncomeByIdAndUser(Long incomeId, Long userId) {
        Income income = incomeRepository.findById(incomeId)
                .orElseThrow(() -> new ResourceNotFoundException("Income", incomeId));
        if (!income.getUser().getId().equals(userId)) {
            throw new ResourceNotFoundException("Income", incomeId);
        }
        return income;
    }

    private User findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));
    }

    public IncomeResponse toResponse(Income i) {
        return new IncomeResponse(i.getId(), i.getDescription(), i.getAmount(),
                i.getSource(), i.getDate(), i.getNotes(), i.getCreatedAt());
    }

    private PageResponse<IncomeResponse> toPageResponse(Page<Income> page) {
        return new PageResponse<>(page.getContent().stream().map(this::toResponse).toList(),
                page.getNumber(), page.getSize(), page.getTotalElements(),
                page.getTotalPages(), page.isLast());
    }
}
