package com.fairshare.distributed_expense_splitter.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.openapitools.model.ExpenseCategory;
import org.openapitools.model.ExpenseDTO;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "expenses")
public class Expense {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(optional = false)
  @JoinColumn(name = "group_id")
  private Group group;

  private ExpenseCategory category;

  @ManyToOne(optional = false)
  @JoinColumn(name = "paid_by")
  private User paidBy;

  private String description;

  @Column(nullable = false)
  private Double amount;

  @Column(length = 3)
  @NotNull
  private String currency;

  private String notes;

  @NotNull
  private OffsetDateTime expenseDate;

  @CreationTimestamp
  private OffsetDateTime createdAt;

  @OneToMany(
    mappedBy = "expense",
    cascade = CascadeType.ALL,
    orphanRemoval = true
  )
  @NotNull
  private List<ExpenseSplit> splits = new ArrayList<>();

  public void addSplit(ExpenseSplit split) {
    splits.add(split);
    split.setExpense(this);
  }

  public static ExpenseDTO fromEntity(Expense expense) {
    if (expense == null) return null;

    ExpenseDTO dto = new ExpenseDTO();
    dto.setId(expense.getId());
    dto.setGroupId(expense.getGroup().getId());
    dto.setPaidBy(expense.getPaidBy().getId());
    dto.setDescription(expense.getDescription());
    dto.setAmount(expense.getAmount());
    dto.setCategory(expense.getCategory());
    dto.setCurrency(expense.getCurrency());
    dto.setNotes(expense.getNotes());
    dto.setExpenseDate(expense.getExpenseDate());
    dto.setSplits(expense.getSplits().stream().map(expenseSplit -> ExpenseSplit.fromEntity(expenseSplit)).toList());
    return dto;
  }
}
