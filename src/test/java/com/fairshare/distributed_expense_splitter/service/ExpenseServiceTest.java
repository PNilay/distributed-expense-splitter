package com.fairshare.distributed_expense_splitter.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.fairshare.distributed_expense_splitter.entity.Group;
import com.fairshare.distributed_expense_splitter.entity.User;
import com.fairshare.distributed_expense_splitter.repository.ExpenseRepository;
import com.fairshare.distributed_expense_splitter.repository.GroupRepository;
import com.fairshare.distributed_expense_splitter.repository.UserRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openapitools.model.CreateExpenseRequest;

@ExtendWith(MockitoExtension.class)
class ExpenseServiceTest {

  @Mock
  private ExpenseRepository expenseRepository;

  @Mock
  private GroupRepository groupRepository;

  @Mock
  private UserRepository userRepository;

  @InjectMocks
  private ExpenseService expenseService;

  @Test
  void createExpense_savesEntity() throws Exception {
    Group g = Group.builder().id(3L).name("grp").build();
    User u = User.builder().id(4L).name("payer").email("p@e").build();
    when(groupRepository.findById(3L)).thenReturn(Optional.of(g));
    when(userRepository.findById(4L)).thenReturn(Optional.of(u));

    CreateExpenseRequest req = new CreateExpenseRequest();
    req.setGroupId(3L);
    req.setPaidBy(4L);
    req.setAmount(12.5);
    req.setDescription("d");

    var resp = expenseService.createExpense(req);

    verify(expenseRepository).save(any());
    assertEquals(org.springframework.http.HttpStatus.CREATED.value(), resp.getStatusCode().value());
  }
}
