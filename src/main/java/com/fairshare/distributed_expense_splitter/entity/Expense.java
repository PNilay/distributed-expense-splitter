package com.fairshare.distributed_expense_splitter.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.openapitools.jackson.nullable.JsonNullable;
import org.openapitools.model.ExpenseCategory;
import org.openapitools.model.ExpenseDTO;
import org.openapitools.model.SplitType;

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

  @ManyToOne(optional = true)
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
  @PastOrPresent(message = "Expense date cannot be in the future")
  private OffsetDateTime expenseDate;

  @CreationTimestamp
  private OffsetDateTime createdAt;

  @NotNull
  private SplitType splitType;

  @OneToMany(mappedBy = "expense", cascade = CascadeType.ALL, orphanRemoval = true)
  @NotNull
  private List<ExpenseSplit> splits = new ArrayList<>();

  public void addSplit(ExpenseSplit split) {
    splits.add(split);
    split.setExpense(this);
  }

  public void addSplits(List<ExpenseSplit> newSplits) {
    if (newSplits != null) {
      newSplits.forEach(this::addSplit);
    }
  }

  public static ExpenseDTO fromEntity(Expense expense) {
    if (expense == null)
      return null;

    ExpenseDTO dto = new ExpenseDTO();
    dto.setId(expense.getId());
    // dto.setGroupId(expense.getGroup() != null ? expense.getGroup().getId() :
    // null);
    // if (expense.getGroup() != null) {
    // dto.setGroupId(JsonNullable.of(expense.getGroup().getId()));
    // }

    dto.setGroupId(
        expense.getGroup() != null
            ? expense.getGroup().getId()
            : null);

    // dto.setGroupId(
    // Optional.ofNullable(expense.getGroup())
    // .map(Group::getId)
    // .map(JsonNullable::of)
    // .orElse(JsonNullable.undefined()));

    dto.setPaidBy(expense.getPaidBy().getId());
    dto.setDescription(expense.getDescription());
    dto.setAmount(expense.getAmount());
    dto.setCategory(expense.getCategory());
    dto.setCurrency(expense.getCurrency());
    dto.setNotes(expense.getNotes());
    dto.setExpenseDate(expense.getExpenseDate());
    dto.setSplitType(expense.getSplitType());
    dto.setSplits(expense.getSplits().stream().map(expenseSplit -> ExpenseSplit.fromEntity(expenseSplit)).toList());
    return dto;
  }
}
