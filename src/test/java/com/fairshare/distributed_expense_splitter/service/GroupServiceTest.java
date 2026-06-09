package com.fairshare.distributed_expense_splitter.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.fairshare.distributed_expense_splitter.entity.Group;
import com.fairshare.distributed_expense_splitter.entity.User;
import com.fairshare.distributed_expense_splitter.repository.GroupRepository;
import com.fairshare.distributed_expense_splitter.repository.UserRepository;
import java.util.Optional;
import java.util.Set;
import java.util.HashSet;
import org.openapitools.model.CreateGroupRequest;
import org.openapitools.model.GroupDTO;
import org.openapitools.model.UserDTO;
import com.fairshare.distributed_expense_splitter.exception.GroupException;
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
  void getGroup_returnsDto() throws Exception {
    Group g = Group.builder().id(20L).name("G20").description("d").build();
    when(groupRepository.findById(20L)).thenReturn(Optional.of(g));

    GroupDTO dto = groupService.getGroup(20L);

    assertNotNull(dto);
    assertEquals(20L, dto.getId());
    assertEquals("G20", dto.getName());
  }

  @Test
  void getGroup_throwsWhenMissing() {
    when(groupRepository.findById(21L)).thenReturn(Optional.empty());
    GroupException ex = assertThrows(GroupException.class, () -> groupService.getGroup(21L));
    assertTrue(ex.getMessage().contains("GROUP_NOT_FOUND"));
  }

  @Test
  void updateGroup_updatesAndSaves() throws Exception {
    Group g = Group.builder().id(30L).name("Old").description("old").build();
    when(groupRepository.findById(30L)).thenReturn(Optional.of(g));
    when(groupRepository.save(any())).thenAnswer(i -> i.getArgument(0));

    CreateGroupRequest req = new CreateGroupRequest();
    req.setName("New");
    req.setDescription("newdesc");

    GroupDTO res = groupService.updateGroup(30L, req);
    assertEquals(30L, res.getId());
    assertEquals("New", res.getName());
    assertEquals("newdesc", res.getDescription());
  }

  @Test
  void deleteGroup_success() throws Exception {
    when(groupRepository.existsById(40L)).thenReturn(true);
    doNothing().when(groupRepository).deleteById(40L);

    groupService.deleteGroup(40L);

    verify(groupRepository).deleteById(40L);
  }

  @Test
  void deleteGroup_throwsWhenMissing() {
    when(groupRepository.existsById(41L)).thenReturn(false);
    GroupException ex = assertThrows(GroupException.class, () -> groupService.deleteGroup(41L));
    assertTrue(ex.getMessage().contains("GROUP_NOT_FOUND"));
  }

  @Test
  void getGroupMembers_returnsMembers() throws Exception {
    User u = User.builder().id(51L).name("U51").email("u@e").build();
    Set<User> members = new HashSet<>();
    members.add(u);
    Group g = Group.builder().id(50L).name("G50").members(members).build();
    when(groupRepository.findById(50L)).thenReturn(Optional.of(g));

    java.util.List<UserDTO> list = groupService.getGroupMembers(50L);
    assertEquals(1, list.size());
    assertEquals(51L, list.get(0).getId());
  }

  @Test
  void removeMember_success() throws Exception {
    User u = User.builder().id(61L).name("U61").email("u@e").build();
    Set<User> members = new HashSet<>();
    members.add(u);
    Group g = Group.builder().id(60L).name("G60").members(members).build();
    when(groupRepository.findById(60L)).thenReturn(Optional.of(g));
    when(userRepository.findById(61L)).thenReturn(Optional.of(u));
    when(groupRepository.save(any())).thenAnswer(i -> i.getArgument(0));

    groupService.removeMember(60L, 61L);

    verify(groupRepository).save(g);
    assertTrue(g.getMembers().isEmpty());
  }

  @Test
  void removeMember_throwsWhenNotMember() throws Exception {
    Group g = Group.builder().id(70L).name("G70").members(new HashSet<>()).build();
    when(groupRepository.findById(70L)).thenReturn(Optional.of(g));
    when(userRepository.findById(72L)).thenReturn(Optional.of(new User()));

    GroupException ex = assertThrows(GroupException.class, () -> groupService.removeMember(70L, 72L));
    assertTrue(ex.getMessage().contains("USER_NOT_MEMBER"));
  }
}
