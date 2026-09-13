package com.financeai.repository;

import com.financeai.entity.Budget;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BudgetRepository extends JpaRepository<Budget, Long> {

    List<Budget> findByUserIdAndMonthAndYear(Long userId, Integer month, Integer year);

    Optional<Budget> findByUserIdAndCategoryAndMonthAndYear(
            Long userId, String category, Integer month, Integer year);

    @Query("SELECT b FROM Budget b WHERE b.user.id = :userId")
    List<Budget> findAllByUserId(@Param("userId") Long userId);
}
