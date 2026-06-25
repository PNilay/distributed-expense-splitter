package com.fairshare.distributed_expense_splitter.service;

import com.fairshare.distributed_expense_splitter.controller.ExpenseController;
import com.fairshare.distributed_expense_splitter.entity.ErrorCode;
import com.fairshare.distributed_expense_splitter.entity.Expense;
import com.fairshare.distributed_expense_splitter.entity.ExpenseSplit;
import com.fairshare.distributed_expense_splitter.entity.Group;
import com.fairshare.distributed_expense_splitter.entity.User;
import com.fairshare.distributed_expense_splitter.exception.ExpenseException;
import com.fairshare.distributed_expense_splitter.helper.ExpenseSplitter;
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
import org.openapitools.model.ExpensePageDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.openapitools.model.ExpenseSplitDTO;
import org.openapitools.model.SplitType;
import org.openapitools.model.SettlementRequest;
import org.springframework.stereotype.Service;

@Service
public class ExpenseService {

  private static final Logger LOGGER = LogManager.getLogger(
      ExpenseController.class);

  private final ExpenseRepository expenseRepository;
  private final GroupRepository groupRepository;
  private final UserRepository userRepository;
  private final ExpenseValidator expenseValidator;

  ExpenseService(
      ExpenseValidator expenseValidator,
      UserRepository userRepository,
      GroupRepository groupRepository,
      ExpenseRepository expenseRepository) {
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
    Group group = gOpt.orElseThrow(() -> new ExpenseException("Service.GROUP_NOT_FOUND", ErrorCode.GROUP_NOT_FOUND));

    expenseValidator.validate(expenseDto, group);

    User paidBy = userRepository
        .findById(expenseDto.getPaidBy())
        .orElseThrow(() -> new ExpenseException("Service.USER_NOT_FOUND", ErrorCode.USER_NOT_FOUND));

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

  private List<ExpenseSplit> populateSplitAmounts(CreateExpenseRequest expenseDto) {
    List<ExpenseSplit> splits = new ArrayList<>();
    Double totalAmount = expenseDto.getAmount();
    SplitType type = expenseDto.getSplitType();
    List<ExpenseSplitDTO> splitDtos = expenseDto.getSplits();
    int idx = 0;
    switch (type) {
      case EXACT:
        for (ExpenseSplitDTO dto : splitDtos) {
          splits.add(createSplitEntity(dto.getUserId(), dto.getAmount()));
        }
        break;
      case EQUAL:
        List<Double> split_evenly = ExpenseSplitter.splitEvenlyWithCurrency(totalAmount, splitDtos.size(),
            expenseDto.getCurrency());
        idx = 0;
        for (ExpenseSplitDTO dto : splitDtos) {
          splits.add(
              createSplitEntity(dto.getUserId(), split_evenly.get(idx)));
          idx++;
        }
        break;
      case PERCENTAGE:
        List<Double> percentages = splitDtos.stream().map(ExpenseSplitDTO::getPercentage).toList();

        List<Double> split_by_percentages = ExpenseSplitter.splitByPercentage(totalAmount, percentages,
            expenseDto.getCurrency());

        idx = 0;
        for (ExpenseSplitDTO dto : splitDtos) {
          splits.add(
              createSplitEntity(
                  dto.getUserId(), split_by_percentages.get(idx)));
          idx++;
        }
        break;
      case SHARE:
        List<Integer> shares = splitDtos.stream().map(ExpenseSplitDTO::getShare).toList();
        List<Double> split_by_shares = ExpenseSplitter.splitByShare(totalAmount, shares, expenseDto.getCurrency());

        // Calculate portion: totalAmount * (userShares / totalShares)
        idx = 0;
        for (ExpenseSplitDTO dto : splitDtos) {
          splits.add(createSplitEntity(dto.getUserId(), split_by_shares.get(idx)));
          idx++;
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
        .orElseThrow(() -> new ExpenseException("Service.EXPENSE_NOT_FOUND", ErrorCode.EXPENSE_NOT_FOUND));
    return Expense.fromEntity(e);
  }

  @Transactional
  public ExpenseDTO updateExpense(Long expenseId, CreateExpenseRequest request) throws ExpenseException {
    // Check if Expense exists
    Expense existingExpense = expenseRepository
        .findById(expenseId)
        .orElseThrow(() -> new ExpenseException("Service.EXPENSE_NOT_FOUND", ErrorCode.EXPENSE_NOT_FOUND));

    // Is the user allowed to make chnages (only if the user is part of group!) -->
    // Require addition field in input
    // TO BE IMPLEMENTED

    Group existsinGroup = existingExpense.getGroup();

    if (!existsinGroup.getId().equals(request.getGroupId())) {
      throw new IllegalArgumentException("Moving an expense to a different group is not allowed.");
    }

    expenseValidator.validate(request, existsinGroup);
    User paidBy = userRepository
        .findById(request.getPaidBy())
        .orElseThrow(() -> new ExpenseException("Service.USER_NOT_FOUND", ErrorCode.USER_NOT_FOUND));

    existingExpense.setPaidBy(paidBy);
    existingExpense.setDescription(request.getDescription());
    existingExpense.setAmount(request.getAmount());
    existingExpense.setCategory(request.getCategory());
    existingExpense.setCurrency(request.getCurrency());
    existingExpense.setNotes(request.getNotes());
    existingExpense.setSplitType(request.getSplitType());
    existingExpense.setExpenseDate(request.getExpenseDate());

    existingExpense.getSplits().clear();
    existingExpense.addSplits(populateSplitAmounts(request));

    return Expense.fromEntity(expenseRepository.save(existingExpense));
  }

  public void deleteExpense(Long expenseId) throws ExpenseException {
    if (!expenseRepository.existsById(expenseId))
      throw new ExpenseException("Service.EXPENSE_NOT_FOUND", ErrorCode.EXPENSE_NOT_FOUND);
    expenseRepository.deleteById(expenseId);
  }

  public List<ExpenseDTO> getGroupExpenses(Long groupId) {
    groupRepository
        .findById(groupId)
        .orElseThrow(() -> new ExpenseException("Service.GROUP_NOT_FOUND", ErrorCode.GROUP_NOT_FOUND));
    List<Expense> list = expenseRepository.findByGroupId(groupId);
    return list.stream().map(expense -> Expense.fromEntity(expense)).toList();
  }

  public ExpensePageDTO getUserExpensesPaginated(Long userId, Integer page, Integer size) {
    // validate user exists
    userRepository
        .findById(userId)
        .orElseThrow(() -> new ExpenseException("Service.USER_NOT_FOUND", ErrorCode.USER_NOT_FOUND));

    PageRequest pr = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "expenseDate"));
    Page<Expense> p = expenseRepository.findByUserId(userId, pr);

    ExpensePageDTO dto = new ExpensePageDTO();
    List<ExpenseDTO> content = p.getContent().stream().map(Expense::fromEntity).toList();
    dto.setContent(content);
    dto.setPageNumber(p.getNumber());
    dto.setPageSize(p.getSize());
    dto.setTotalElements(p.getTotalElements());
    dto.setTotalPages(p.getTotalPages());
    dto.setIsLast(p.isLast());
    return dto;
  }

  @Transactional
  public ExpenseDTO settleDebt(SettlementRequest req) {
    // validate users
    User from = userRepository
        .findById(req.getFromUserId())
        .orElseThrow(() -> new ExpenseException("Service.USER_NOT_FOUND", ErrorCode.USER_NOT_FOUND));
    User to = userRepository
        .findById(req.getToUserId())
        .orElseThrow(() -> new ExpenseException("Service.USER_NOT_FOUND", ErrorCode.USER_NOT_FOUND));

    Expense expense = new Expense();
    if (req.getGroupId() != null) {
      Long groupId = req.getGroupId().get();
      Group g = groupRepository
          .findById(groupId)
          .orElseThrow(() -> new ExpenseException("Service.GROUP_NOT_FOUND", ErrorCode.GROUP_NOT_FOUND));
      expense.setGroup(g);
    }

    expense.setPaidBy(from);
    expense.setAmount(req.getAmount());
    expense.setCurrency(req.getCurrency());
    expense.setExpenseDate(req.getSettlementDate());
    expense.setDescription(req.getNotes() != null ? req.getNotes() : "Settlement");
    expense.setSplitType(SplitType.EXACT);

    // single split representing the settlement receiver
    expense.addSplit(createSplitEntity(req.getToUserId(), req.getAmount()));

    Expense saved = expenseRepository.save(expense);
    return Expense.fromEntity(saved);
  }
}
