package com.financeai.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "incomes", indexes = {
        @Index(name = "idx_incomes_user_date", columnList = "user_id, date")
})
public class Income {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank @Size(max = 255)
    @Column(nullable = false, length = 255)
    private String description;

    @NotNull @DecimalMin(value = "0.01")
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @NotBlank
    @Column(nullable = false, length = 50)
    private String source;

    @NotNull
    @Column(nullable = false)
    private LocalDate date;

    @Size(max = 500)
    @Column(length = 500)
    private String notes;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    public Income() {}

    private Income(Builder b) {
        this.id = b.id; this.description = b.description; this.amount = b.amount;
        this.source = b.source; this.date = b.date; this.notes = b.notes; this.user = b.user;
    }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private Long id; private String description; private BigDecimal amount;
        private String source; private LocalDate date; private String notes; private User user;

        public Builder id(Long id) { this.id = id; return this; }
        public Builder description(String d) { this.description = d; return this; }
        public Builder amount(BigDecimal a) { this.amount = a; return this; }
        public Builder source(String s) { this.source = s; return this; }
        public Builder date(LocalDate d) { this.date = d; return this; }
        public Builder notes(String n) { this.notes = n; return this; }
        public Builder user(User u) { this.user = u; return this; }
        public Income build() { return new Income(this); }
    }

    @PrePersist protected void onCreate() { createdAt = LocalDateTime.now(); updatedAt = LocalDateTime.now(); }
    @PreUpdate  protected void onUpdate() { updatedAt = LocalDateTime.now(); }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime t) { this.createdAt = t; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime t) { this.updatedAt = t; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
}
