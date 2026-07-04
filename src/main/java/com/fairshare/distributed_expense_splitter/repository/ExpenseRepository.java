package com.fairshare.distributed_expense_splitter.repository;

import com.fairshare.distributed_expense_splitter.entity.Expense;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
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

	@Query("SELECT DISTINCT e FROM Expense e LEFT JOIN e.splits s WHERE e.paidBy.id = :userId OR s.user.id = :userId")
	Page<Expense> findByUserId(@Param("userId") Long userId, Pageable pageable);

	// 1. Initial Load: Get newest items
	@Query(value = "SELECT * FROM expenses WHERE group_id = :groupId ORDER BY id DESC LIMIT :limit", nativeQuery = true)
	List<Expense> findInitialExpenses(@Param("groupId") Long groupId, @Param("limit") int limit);

	// 2. Scrolling Down: Get older items (id < beforeId)
	@Query(value = "SELECT * FROM expenses WHERE group_id = :groupId AND id < :beforeId ORDER BY id DESC LIMIT :limit", nativeQuery = true)
	List<Expense> findExpensesBefore(@Param("groupId") Long groupId, @Param("beforeId") Long beforeId,
			@Param("limit") int limit);

	// 3. Scrolling Up: Get newer items (id > afterId)
	@Query(value = "SELECT * FROM (SELECT * FROM expenses WHERE group_id = :groupId AND id > :afterId ORDER BY id ASC LIMIT :limit) sub ORDER BY sub.id DESC", nativeQuery = true)
	List<Expense> findExpensesAfter(@Param("groupId") Long groupId, @Param("afterId") Long afterId,
			@Param("limit") int limit);
}
