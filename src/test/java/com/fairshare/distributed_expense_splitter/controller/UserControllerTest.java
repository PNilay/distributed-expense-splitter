package com.fairshare.distributed_expense_splitter.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;

import com.fairshare.distributed_expense_splitter.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openapitools.model.CreateUserRequest;
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
  void getUser_returnsNotFoundOnUserNotFound() throws Exception {
    when(userService.getUserById(6L))
      .thenThrow(new Exception("Service.USER_NOT_FOUND"));
    ResponseEntity<UserDTO> res = userController.getUser(6L);
    assertEquals(HttpStatus.NOT_FOUND, res.getStatusCode());
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

    // Delete User By ID Method Test Cases:
    @Test
    void deleteUserById_returnsOkOnSuccess() throws Exception {
        when(userService.deleteUserById(7L)).thenReturn(true);
        ResponseEntity<String> res = userController.deleteUserById(7L);
        assertEquals(HttpStatus.OK, res.getStatusCode());
        assertEquals("User with id 7 deleted successfully.", res.getBody());
    }   

    @Test
    void deleteUserById_returnsNotFoundOnUserNotFound() throws Exception {
        when(userService.deleteUserById(8L)).thenThrow(new Exception("Service.USER_NOT_FOUND"));
        ResponseEntity<String> res = userController.deleteUserById(8L);
        assertEquals(HttpStatus.NOT_FOUND, res.getStatusCode());
    }

}
