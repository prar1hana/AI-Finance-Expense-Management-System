package com.financeai.repository;

import com.financeai.entity.Income;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface IncomeRepository extends JpaRepository<Income, Long> {

    @Query("SELECT i FROM Income i WHERE i.user.id = :userId " +
           "AND (:source IS NULL OR i.source = :source) " +
           "AND (:startDate IS NULL OR i.date >= :startDate) " +
           "AND (:endDate IS NULL OR i.date <= :endDate)")
    Page<Income> findByFilters(
            @Param("userId") Long userId,
            @Param("source") String source,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            Pageable pageable);

    @Query("SELECT COALESCE(SUM(i.amount), 0) FROM Income i " +
           "WHERE i.user.id = :userId " +
           "AND i.date >= :startDate AND i.date <= :endDate")
    BigDecimal sumByUserAndDateRange(
            @Param("userId") Long userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    @Query("SELECT i FROM Income i WHERE i.user.id = :userId ORDER BY i.date DESC, i.createdAt DESC")
    List<Income> findRecentByUserId(@Param("userId") Long userId, Pageable pageable);
}
