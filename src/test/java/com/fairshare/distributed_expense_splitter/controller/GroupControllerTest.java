package com.fairshare.distributed_expense_splitter.controller;

import com.fairshare.distributed_expense_splitter.service.GroupService;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GroupControllerTest {

  @Mock
  private GroupService groupService;

  @InjectMocks
  private GroupController groupController;


}
