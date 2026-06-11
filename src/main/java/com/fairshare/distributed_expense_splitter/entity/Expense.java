package com.fairshare.distributed_expense_splitter.entity;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.openapitools.model.ExpenseCategory;
import org.openapitools.model.ExpenseDTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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

  private String currency;
  private String notes;
  private OffsetDateTime expenseDate;


  @CreationTimestamp
  private OffsetDateTime createdAt;

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
    return dto;
  }
}
