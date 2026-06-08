package com.fairshare.distributed_expense_splitter.service;

import com.fairshare.distributed_expense_splitter.entity.Expense;
import com.fairshare.distributed_expense_splitter.entity.Group;
import com.fairshare.distributed_expense_splitter.entity.User;
import com.fairshare.distributed_expense_splitter.repository.ExpenseRepository;
import com.fairshare.distributed_expense_splitter.repository.GroupRepository;
import com.fairshare.distributed_expense_splitter.repository.UserRepository;
import java.time.OffsetDateTime;
import java.util.Optional;
import org.openapitools.model.CreateExpenseRequest;
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

  public void createExpense(CreateExpenseRequest req) throws Exception {
    Optional<Group> gOpt = groupRepository.findById(req.getGroupId());
    Group group = gOpt.orElseThrow(() -> new Exception("Service.GROUP_NOT_FOUND"));
    Optional<User> uOpt = userRepository.findById(req.getPaidBy());
    User paidBy = uOpt.orElseThrow(() -> new Exception("Service.USER_NOT_FOUND"));
    Expense e = Expense.builder()
      .group(group)
      .paidBy(paidBy)
      .description(req.getDescription())
      .amount(req.getAmount())
      .createdAt(OffsetDateTime.now())
      .build();
    expenseRepository.save(e);
  }
}
