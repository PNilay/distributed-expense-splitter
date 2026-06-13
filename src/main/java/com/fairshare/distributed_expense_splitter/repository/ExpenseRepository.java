package com.fairshare.distributed_expense_splitter.repository;

import com.fairshare.distributed_expense_splitter.entity.Expense;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long> {
	List<Expense> findByGroupId(Long groupId);
}
