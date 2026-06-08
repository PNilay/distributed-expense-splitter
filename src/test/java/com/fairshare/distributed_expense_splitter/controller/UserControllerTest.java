package com.fairshare.distributed_expense_splitter.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.fairshare.distributed_expense_splitter.entity.ErrorCode;
import com.fairshare.distributed_expense_splitter.exception.UserException;
import com.fairshare.distributed_expense_splitter.service.UserService;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openapitools.model.CreateUserRequest;
import org.openapitools.model.UpdateUserRequest;
import org.openapitools.model.UserDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

  @Mock
  private UserService userService;

  @InjectMocks
  private UserController userController;

  // Create User Method Test Cases:
  @Test
  void createUser_returnsCreated() {
    CreateUserRequest req = new CreateUserRequest();
    req.setName("X");
    req.setEmail("x@e");

    UserDTO saved = new UserDTO();
    saved.setId(11L);
    saved.setName("X");
    saved.setEmail("x@e");
    when(userService.createUser(any())).thenReturn(saved);

    ResponseEntity<UserDTO> res = userController.createUser(req);
    assertEquals(HttpStatus.CREATED, res.getStatusCode());
    assertEquals(11L, res.getBody().getId());
    assertEquals("X", res.getBody().getName());
    assertEquals("x@e", res.getBody().getEmail());
  }

  //   Get User By ID Method Test Cases:
  @Test
  void getUser_returnsFoundOnSuccess() throws Exception {
    UserDTO dto = new UserDTO();
    dto.setId(5L);
    when(userService.getUserById(5L)).thenReturn(dto);
    ResponseEntity<UserDTO> res = userController.getUser(5L);
    assertEquals(HttpStatus.OK, res.getStatusCode());
    assertEquals(5L, res.getBody().getId());
  }

  @Test
  void getUser_returnsNotFoundOnUserNotFound() throws UserException {
    when(userService.getUserById(6L))
      .thenThrow(
        new UserException("Service.USER_NOT_FOUND", ErrorCode.USER_NOT_FOUND)
      );
    assertThrows(
      UserException.class,
      () -> {
        userController.getUser(6L);
      }
    );
  }

  // Get All Users Method Test Cases:
  @Test
  void getUsers_returnsOk() {
    List<UserDTO> users = new ArrayList<>();
    UserDTO u1 = new UserDTO();
    u1.setId(1L);
    u1.setName("A");
    u1.setEmail("a@e");
    users.add(u1);
    UserDTO u2 = new UserDTO();
    u2.setId(2L);
    u2.setName("B");
    u2.setEmail("b@e");
    users.add(u2);
    when(userService.getAllUsers()).thenReturn(users);

    ResponseEntity<List<UserDTO>> res = userController.getUsers();
    assertEquals(HttpStatus.OK, res.getStatusCode());
    assertEquals(2, res.getBody().size());
    assertEquals(1L, res.getBody().get(0).getId());
    assertEquals("A", res.getBody().get(0).getName());
    assertEquals("a@e", res.getBody().get(0).getEmail());
    assertEquals(2L, res.getBody().get(1).getId());
    assertEquals("B", res.getBody().get(1).getName());
    assertEquals("b@e", res.getBody().get(1).getEmail());
  }

  @Test
  void getUsers_returnsEmptyList() {
    when(userService.getAllUsers()).thenReturn(new ArrayList<>());
    ResponseEntity<List<UserDTO>> res = userController.getUsers();
    assertEquals(HttpStatus.OK, res.getStatusCode());
    assertNotNull(res.getBody());
    assertTrue(res.getBody().isEmpty());
  }

  // Update User Method Test Cases:
  @Test
  void updateUser_returnsOkOnSuccess() throws Exception {
    UpdateUserRequest req = new UpdateUserRequest();
    req.setName("Y");
    UserDTO dto = new UserDTO();
    dto.setId(3L);
    dto.setName("Y");
    when(userService.updateUser(eq(3L), any())).thenReturn(dto);  
    ResponseEntity<UserDTO> res = userController.updateUser(3L, req);
    assertEquals(HttpStatus.OK, res.getStatusCode());
    assertEquals(3L, res.getBody().getId());
    assertEquals("Y", res.getBody().getName());
  }

  @Test
  void updateUser_returnsNotFoundOnUserNotFound() throws UserException {
    UpdateUserRequest req = new UpdateUserRequest();
    when(userService.updateUser(eq(4L), any()))
      .thenThrow(
        new UserException("Service.USER_NOT_FOUND", ErrorCode.USER_NOT_FOUND)
      );
    assertThrows(UserException.class, () -> userController.updateUser(4L, req));
  }
  // Delete User By ID Method Test Cases:
  @Test
  void deleteUserById_returnsOkOnSuccess() throws Exception {
    when(userService.deleteUserById(7L)).thenReturn(true);
    ResponseEntity<Void> res = userController.deleteUserById(7L);
    assertEquals(HttpStatus.NO_CONTENT, res.getStatusCode());
  }

  @Test
  void deleteUserById_returnsNotFoundOnUserNotFound() throws UserException {
    when(userService.deleteUserById(8L))
      .thenThrow(
        new UserException("Service.USER_NOT_FOUND", ErrorCode.USER_NOT_FOUND)
      );

      assertThrows(UserException.class, () -> userController.deleteUserById(8L));
      
  }
}
