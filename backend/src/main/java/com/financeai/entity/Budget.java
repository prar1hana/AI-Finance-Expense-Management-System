package com.financeai.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "budgets", indexes = {
        @Index(name = "idx_budgets_user_month_year", columnList = "user_id, budget_month, budget_year")
}, uniqueConstraints = {
        @UniqueConstraint(name = "uk_budget_user_category_month_year",
                columnNames = {"user_id", "category", "budget_month", "budget_year"})
})
public class Budget {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false, length = 50)
    private String category;

    @NotNull @DecimalMin(value = "0.01")
    @Column(name = "limit_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal limitAmount;

    @NotNull @Min(1) @Max(12)
    @Column(name = "budget_month", nullable = false)
    private Integer month;

    @NotNull @Min(2000)
    @Column(name = "budget_year", nullable = false)
    private Integer year;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    public Budget() {}

    private Budget(Builder b) {
        this.id = b.id; this.category = b.category; this.limitAmount = b.limitAmount;
        this.month = b.month; this.year = b.year; this.user = b.user;
    }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private Long id; private String category; private BigDecimal limitAmount;
        private Integer month; private Integer year; private User user;

        public Builder id(Long id) { this.id = id; return this; }
        public Builder category(String c) { this.category = c; return this; }
        public Builder limitAmount(BigDecimal a) { this.limitAmount = a; return this; }
        public Builder month(Integer m) { this.month = m; return this; }
        public Builder year(Integer y) { this.year = y; return this; }
        public Builder user(User u) { this.user = u; return this; }
        public Budget build() { return new Budget(this); }
    }

    @PrePersist protected void onCreate() { createdAt = LocalDateTime.now(); updatedAt = LocalDateTime.now(); }
    @PreUpdate  protected void onUpdate() { updatedAt = LocalDateTime.now(); }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public BigDecimal getLimitAmount() { return limitAmount; }
    public void setLimitAmount(BigDecimal limitAmount) { this.limitAmount = limitAmount; }
    public Integer getMonth() { return month; }
    public void setMonth(Integer month) { this.month = month; }
    public Integer getYear() { return year; }
    public void setYear(Integer year) { this.year = year; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
}
