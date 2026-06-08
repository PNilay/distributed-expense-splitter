package com.fairshare.distributed_expense_splitter.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.fairshare.distributed_expense_splitter.entity.Group;
import com.fairshare.distributed_expense_splitter.entity.User;
import com.fairshare.distributed_expense_splitter.repository.GroupRepository;
import com.fairshare.distributed_expense_splitter.repository.UserRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GroupServiceTest {

  @Mock
  private GroupRepository groupRepository;

  @Mock
  private UserRepository userRepository;

  @InjectMocks
  private GroupService groupService;

  @Test
  void addMember_addsAndSaves() throws Exception {
    Group g = Group.builder().id(10L).name("G").build();
    User u = User.builder().id(5L).name("U").email("u@e").build();
    when(groupRepository.findById(10L)).thenReturn(Optional.of(g));
    when(userRepository.findById(5L)).thenReturn(Optional.of(u));

    groupService.addMember(10L, 5L);

    verify(groupRepository).save(g);
    assertTrue(g.getMembers().contains(u));
  }

  @Test
  void addMember_throwsWhenGroupMissing() {
    when(groupRepository.findById(1L)).thenReturn(Optional.empty());
    when(userRepository.findById(2L)).thenReturn(Optional.of(new User()));
    Exception ex = assertThrows(Exception.class, () -> groupService.addMember(1L, 2L));
    assertTrue(ex.getMessage().contains("GROUP_NOT_FOUND"));
  }
}
