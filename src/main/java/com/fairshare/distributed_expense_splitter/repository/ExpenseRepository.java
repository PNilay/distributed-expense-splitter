package com.fairshare.distributed_expense_splitter.repository;

import com.fairshare.distributed_expense_splitter.entity.Expense;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long> {
	List<Expense> findByGroupId(Long groupId);

	@Query("SELECT DISTINCT s2.user.id FROM Expense e " +
			"JOIN e.splits s1 " +
			"JOIN e.splits s2 " +
			"WHERE e.paidBy.id = :userId OR s1.user.id = :userId")
	List<Long> findExpenseContactIdsWhereUserIsPayerOrParticipant(@Param("userId") Long userId);

	// Finds all payer IDs of expenses where the current user is a participant
	@Query("SELECT DISTINCT e.paidBy.id FROM Expense e JOIN e.splits s WHERE s.user.id = :userId")
	List<Long> findExpensePayerIdsWhereUserIsParticipant(@Param("userId") Long userId);
}
