package com.fairshare.distributed_expense_splitter.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

import java.util.List;

import com.fairshare.distributed_expense_splitter.service.GroupService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openapitools.model.CreateGroupRequest;
import org.openapitools.model.GroupDTO;
import org.openapitools.model.AddMemberRequest;
import org.openapitools.model.UserDTO;
import java.util.ArrayList;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
class GroupControllerTest {

  @Mock
  private GroupService groupService;

  @InjectMocks
  private GroupController groupController;

  @Test
  void createGroup_Success() {
    GroupDTO dto = new GroupDTO();
    dto.setId(1L);
    dto.setName("Test Group");
    dto.setDescription("A group for testing");
    when(groupService.createGroup(any())).thenReturn(dto);

    CreateGroupRequest req = new CreateGroupRequest();
    req.setName("Test Group");
    req.setDescription("A group for testing");
    req.setCreatedBy(1L);
    ResponseEntity<GroupDTO> res = groupController.createGroup(req);

    assertEquals(HttpStatus.CREATED, res.getStatusCode());
    assertEquals(1L, res.getBody().getId());
    assertEquals("Test Group", res.getBody().getName());
    assertEquals("A group for testing", res.getBody().getDescription());
  }

  @Test
  void getGroups_Success() {
    GroupDTO dto1 = new GroupDTO();
    dto1.setId(1L); 
    dto1.setName("Group 1");
    GroupDTO dto2 = new GroupDTO();
    dto2.setId(2L);
    dto2.setName("Group 2");
    when(groupService.getGroups()).thenReturn(List.of(dto1, dto2));

    ResponseEntity<List<GroupDTO>> res = groupController.getGroups();
    assertEquals(HttpStatus.OK, res.getStatusCode());
    assertEquals(2, res.getBody().size());
    assertEquals(1L, res.getBody().get(0).getId());
    assertEquals("Group 1", res.getBody().get(0).getName());
    assertEquals(2L, res.getBody().get(1).getId());
    assertEquals("Group 2", res.getBody().get(1).getName());  
  }

  @Test
  void getGroup_Success() throws Exception {
    GroupDTO dto = new GroupDTO();
    dto.setId(5L);
    dto.setName("My Group");
    when(groupService.getGroup(5L)).thenReturn(dto);

    ResponseEntity<GroupDTO> res = groupController.getGroup(5L);
    assertEquals(HttpStatus.OK, res.getStatusCode());
    assertEquals(5L, res.getBody().getId());
    assertEquals("My Group", res.getBody().getName());
  }

  @Test
  void updateGroup_Success() throws Exception {
    CreateGroupRequest req = new CreateGroupRequest();
    req.setName("Updated");
    req.setDescription("desc");
    GroupDTO dto = new GroupDTO();
    dto.setId(6L);
    dto.setName("Updated");
    when(groupService.updateGroup(6L, req)).thenReturn(dto);

    ResponseEntity<GroupDTO> res = groupController.updateGroup(6L, req);
    assertEquals(HttpStatus.OK, res.getStatusCode());
    assertEquals(6L, res.getBody().getId());
    assertEquals("Updated", res.getBody().getName());
  }

  @Test
  void deleteGroup_Success() throws Exception {
    doNothing().when(groupService).deleteGroup(7L);
    ResponseEntity<Void> res = groupController.deleteGroup(7L);
    assertEquals(HttpStatus.NO_CONTENT, res.getStatusCode());
  }

  @Test
  void getGroupMembers_Success() throws Exception {
    UserDTO u1 = new UserDTO();
    u1.setId(11L);
    u1.setName("U1");
    List<UserDTO> members = new ArrayList<>();
    members.add(u1);
    when(groupService.getGroupMembers(8L)).thenReturn(members);

    ResponseEntity<List<UserDTO>> res = groupController.getGroupMembers(8L);
    assertEquals(HttpStatus.OK, res.getStatusCode());
    assertEquals(1, res.getBody().size());
    assertEquals(11L, res.getBody().get(0).getId());
  }

  @Test
  void addMember_Success() throws Exception {
    AddMemberRequest req = new AddMemberRequest();
    req.setUserId(12L);
    doNothing().when(groupService).addMember(9L, 12L);
    ResponseEntity<Void> res = groupController.addMember(9L, req);
    assertEquals(HttpStatus.NO_CONTENT, res.getStatusCode());
  }

  @Test
  void removeMember_Success() throws Exception {
    doNothing().when(groupService).removeMember(10L, 13L);
    ResponseEntity<Void> res = groupController.removeMember(10L, 13L);
    assertEquals(HttpStatus.NO_CONTENT, res.getStatusCode());
  }

}
