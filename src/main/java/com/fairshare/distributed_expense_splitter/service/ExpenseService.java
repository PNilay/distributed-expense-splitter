package com.fairshare.distributed_expense_splitter.service;

import com.fairshare.distributed_expense_splitter.entity.ErrorCode;
import com.fairshare.distributed_expense_splitter.entity.Expense;
import com.fairshare.distributed_expense_splitter.entity.Group;
import com.fairshare.distributed_expense_splitter.entity.User;
import com.fairshare.distributed_expense_splitter.exception.ExpenseException;
import com.fairshare.distributed_expense_splitter.repository.ExpenseRepository;
import com.fairshare.distributed_expense_splitter.repository.GroupRepository;
import com.fairshare.distributed_expense_splitter.repository.UserRepository;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import org.openapitools.model.CreateExpenseRequest;
import org.openapitools.model.ExpenseDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ExpenseService {

  @Autowired
  private ExpenseRepository expenseRepository;

  @Autowired
  private GroupRepository groupRepository;

  @Autowired
  private UserRepository userRepository;

  public ExpenseDTO createExpense(CreateExpenseRequest req)
    throws ExpenseException {
    Optional<Group> gOpt = groupRepository.findById(req.getGroupId());
    Group group = gOpt.orElseThrow(() ->
      new ExpenseException("Service.GROUP_NOT_FOUND", ErrorCode.GROUP_NOT_FOUND)
    );

    Optional<User> uOpt = userRepository.findById(req.getPaidBy());
    User paidBy = uOpt.orElseThrow(() ->
      new ExpenseException("Service.USER_NOT_FOUND", ErrorCode.USER_NOT_FOUND)
    );

    Expense e = Expense
      .builder()
      .group(group)
      .paidBy(paidBy)
      .description(req.getDescription())
      .amount(req.getAmount())
      .createdAt(OffsetDateTime.now())
      .build();
    Expense res = expenseRepository.save(e);

    return Expense.fromEntity(res);
  }

  public List<ExpenseDTO> getExpenses() {
    List<Expense> list = expenseRepository.findAll();

    return list.stream().map(expense -> Expense.fromEntity(expense)).toList();
  }

  public ExpenseDTO getExpense(Long expenseId) throws ExpenseException {
    Expense e = expenseRepository
      .findById(expenseId)
      .orElseThrow(() ->
        new ExpenseException(
          "Service.EXPENSE_NOT_FOUND",
          ErrorCode.EXPENSE_NOT_FOUND
        )
      );
    return Expense.fromEntity(e);
  }

  public ExpenseDTO updateExpense(Long expenseId, CreateExpenseRequest req)
    throws ExpenseException {
    Expense e = expenseRepository
      .findById(expenseId)
      .orElseThrow(() ->
        new ExpenseException(
          "Service.EXPENSE_NOT_FOUND",
          ErrorCode.EXPENSE_NOT_FOUND
        )
      );

    if (req.getGroupId() != null) {
      Group group = groupRepository
        .findById(req.getGroupId())
        .orElseThrow(() ->
          new ExpenseException(
            "Service.GROUP_NOT_FOUND",
            ErrorCode.GROUP_NOT_FOUND
          )
        );
      e.setGroup(group);
    }
    if (req.getPaidBy() != null) {
      User user = userRepository
        .findById(req.getPaidBy())
        .orElseThrow(() ->
          new ExpenseException(
            "Service.USER_NOT_FOUND",
            ErrorCode.USER_NOT_FOUND
          )
        );
      e.setPaidBy(user);
    }
    if (req.getAmount() != null) {
      e.setAmount(req.getAmount());
    }
    if (req.getDescription() != null) {
      e.setDescription(req.getDescription());
    }
    return Expense.fromEntity(expenseRepository.save(e));
  }

  public void deleteExpense(Long expenseId) throws ExpenseException {
    if (!expenseRepository.existsById(expenseId)) throw new ExpenseException(
      "Service.EXPENSE_NOT_FOUND",
      ErrorCode.EXPENSE_NOT_FOUND
    );
    expenseRepository.deleteById(expenseId);
  }

  public List<ExpenseDTO> getGroupExpenses(Long groupId) {
    groupRepository
      .findById(groupId)
      .orElseThrow(() ->
        new ExpenseException(
          "Service.GROUP_NOT_FOUND",
          ErrorCode.GROUP_NOT_FOUND
        )
      );
    List<Expense> list = expenseRepository.findByGroupId(groupId);

    return list.stream().map(expense -> Expense.fromEntity(expense)).toList();
  }
}
