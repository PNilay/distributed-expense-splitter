package com.fairshare.distributed_expense_splitter.service;

import com.fairshare.distributed_expense_splitter.controller.ExpenseController;
import com.fairshare.distributed_expense_splitter.entity.ErrorCode;
import com.fairshare.distributed_expense_splitter.entity.Expense;
import com.fairshare.distributed_expense_splitter.entity.ExpenseSplit;
import com.fairshare.distributed_expense_splitter.entity.Group;
import com.fairshare.distributed_expense_splitter.entity.User;
import com.fairshare.distributed_expense_splitter.exception.ExpenseException;
import com.fairshare.distributed_expense_splitter.helper.ExpenseValidator;
import com.fairshare.distributed_expense_splitter.repository.ExpenseRepository;
import com.fairshare.distributed_expense_splitter.repository.GroupRepository;
import com.fairshare.distributed_expense_splitter.repository.UserRepository;
import jakarta.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openapitools.model.CreateExpenseRequest;
import org.openapitools.model.ExpenseDTO;
import org.openapitools.model.ExpenseSplitDTO;
import org.openapitools.model.SplitType;
import org.springframework.stereotype.Service;

@Service
public class ExpenseService {

  private static final Logger LOGGER = LogManager.getLogger(
    ExpenseController.class
  );

  private final ExpenseRepository expenseRepository;
  private final GroupRepository groupRepository;
  private final UserRepository userRepository;
  private final ExpenseValidator expenseValidator;

  ExpenseService(
    ExpenseValidator expenseValidator,
    UserRepository userRepository,
    GroupRepository groupRepository,
    ExpenseRepository expenseRepository
  ) {
    this.expenseValidator = expenseValidator;
    this.userRepository = userRepository;
    this.groupRepository = groupRepository;
    this.expenseRepository = expenseRepository;
  }

  @Transactional
  public ExpenseDTO createExpense(CreateExpenseRequest req)
    throws ExpenseException {
    LOGGER.info("Create Expense Request: " + req);
    Expense e = fromExpenseDTO(req);
    Expense res = expenseRepository.save(e);
    return Expense.fromEntity(res);
  }

  public Expense fromExpenseDTO(CreateExpenseRequest expenseDto) {
    Optional<Group> gOpt = groupRepository.findById(expenseDto.getGroupId());
    Group group = gOpt.orElseThrow(() ->
      new ExpenseException("Service.GROUP_NOT_FOUND", ErrorCode.GROUP_NOT_FOUND)
    );

    expenseValidator.validate(expenseDto, group);

    User paidBy = userRepository
      .findById(expenseDto.getPaidBy())
      .orElseThrow(() ->
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
    expense.setSplitType(expenseDto.getSplitType());
    expense.setExpenseDate(expenseDto.getExpenseDate());

    expense.addSplits(populateSplitAmounts(expenseDto));

    return expense;
  }

  private List<ExpenseSplit> populateSplitAmounts(
    CreateExpenseRequest expenseDto
  ) {
    List<ExpenseSplit> splits = new ArrayList<>();
    Double totalAmount = expenseDto.getAmount();
    SplitType type = expenseDto.getSplitType();
    List<ExpenseSplitDTO> splitDtos = expenseDto.getSplits();
    int totalUsers = splitDtos.size();

    switch (type) {
      case EXACT:
        for (ExpenseSplitDTO dto : splitDtos) {
          splits.add(createSplitEntity(dto.getUserId(), dto.getAmount()));
        }
        break;
      case EQUAL:
        // TO BE UPDATED LATER TO RESOLVE PENNY PROBLEM
        for (ExpenseSplitDTO dto : splitDtos) {
          splits.add(
            createSplitEntity(dto.getUserId(), totalAmount / totalUsers)
          );
        }
        break;
      case PERCENTAGE:
        for (ExpenseSplitDTO dto : splitDtos) {
          splits.add(
            createSplitEntity(
              dto.getUserId(),
              (dto.getPercentage() * totalAmount) / (100.0)
            )
          );
        }
        break;
      case SHARE:
        // TO BE UPDATED!!
        // Calculate total share
        int totalShares = splitDtos
          .stream()
          .mapToInt(ExpenseSplitDTO::getShare)
          .sum();

        //Calculate portion: totalAmount * (userShares / totalShares)
        for (ExpenseSplitDTO dto : splitDtos) {
          splits.add(
            createSplitEntity(
              dto.getUserId(),
              totalAmount * (double) (dto.getShare() / totalShares)
            )
          );
        }
        break;
    }

    return splits;
  }

  private ExpenseSplit createSplitEntity(Long userId, Double amount) {
    ExpenseSplit split = new ExpenseSplit();
    User userProxy = userRepository.getReferenceById(userId);
    split.setUser(userProxy);
    split.setShareInCents(amount);
    return split;
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
