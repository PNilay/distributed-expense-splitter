package com.fairshare.distributed_expense_splitter.service;

import com.fairshare.distributed_expense_splitter.controller.ExpenseController;
import com.fairshare.distributed_expense_splitter.entity.ErrorCode;
import com.fairshare.distributed_expense_splitter.entity.Expense;
import com.fairshare.distributed_expense_splitter.entity.ExpenseSplit;
import com.fairshare.distributed_expense_splitter.entity.Group;
import com.fairshare.distributed_expense_splitter.entity.User;
import com.fairshare.distributed_expense_splitter.exception.ExpenseException;
import com.fairshare.distributed_expense_splitter.repository.ExpenseRepository;
import com.fairshare.distributed_expense_splitter.repository.GroupRepository;
import com.fairshare.distributed_expense_splitter.repository.UserRepository;
import java.util.List;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openapitools.model.CreateExpenseRequest;
import org.openapitools.model.ExpenseDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ExpenseService {

  private static final Logger LOGGER = LogManager.getLogger(
    ExpenseController.class
  );

  @Autowired
  private ExpenseRepository expenseRepository;

  @Autowired
  private GroupRepository groupRepository;

  @Autowired
  private UserRepository userRepository;

  public ExpenseDTO createExpense(CreateExpenseRequest req)
    throws ExpenseException {
    Expense e = fromExpenseDTO(req);
    Expense res = expenseRepository.save(e);
    return Expense.fromEntity(res);
  }

  public Expense fromExpenseDTO(CreateExpenseRequest expenseDto) {

    LOGGER.info("Input Expense: "+ expenseDto);
    Optional<Group> gOpt = groupRepository.findById(expenseDto.getGroupId());
    Group group = gOpt.orElseThrow(() ->
      new ExpenseException("Service.GROUP_NOT_FOUND", ErrorCode.GROUP_NOT_FOUND)
    );

    Optional<User> uOpt = userRepository.findById(expenseDto.getPaidBy());
    User paidBy = uOpt.orElseThrow(() ->
      new ExpenseException("Service.USER_NOT_FOUND", ErrorCode.USER_NOT_FOUND)
    );

    Expense expense = new Expense();
    expense.setGroup(group);
    expense.setPaidBy(paidBy);
    expense.setDescription(expenseDto.getDescription());
    expense.setAmount(expenseDto.getAmount());
    expense.setCategory(expenseDto.getCategory());
    expense.setCurrency(expenseDto.getCurrency());
    expense.setNotes(expenseDto.getNotes());
    expense.setExpenseDate(expenseDto.getExpenseDate());

    expenseDto
      .getSplits()
      .stream()
      .forEach(spiltDto -> {
        ExpenseSplit res = new ExpenseSplit();

        User splitBy = userRepository
          .findById(spiltDto.getUserId())
          .orElseThrow(() ->
            new ExpenseException(
              "Service.USER_NOT_FOUND",
              ErrorCode.USER_NOT_FOUND
            )
          );

        res.setUser(splitBy);
        res.setShareInCents(spiltDto.getAmount());
        expense.addSplit(res);
      });

    return expense;
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
