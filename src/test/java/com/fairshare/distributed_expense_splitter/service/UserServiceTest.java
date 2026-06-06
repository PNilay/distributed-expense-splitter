package com.fairshare.distributed_expense_splitter.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.fairshare.distributed_expense_splitter.entity.User;
import com.fairshare.distributed_expense_splitter.repository.UserRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openapitools.model.CreateUserRequest;
import org.openapitools.model.UserDTO;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

  @Mock
  private UserRepository userRepository;

  @InjectMocks
  private UserService userService;

  //   Create User Method Test Cases:
  @Test
  void createUser() {
    System.out.println("UserServiceTest.createUser");

    User user = User
      .builder()
      .id(1L)
      .name("Alice")
      .email("alice@example.com")
      .build();
    when(userRepository.save(any(User.class))).thenReturn(user);

    CreateUserRequest createUserRequest = new CreateUserRequest(
      "Alice",
      "alice@example.com"
    );

    UserDTO out = userService.createUser(createUserRequest);
    assertNotNull(out);
    assertEquals(1L, out.getId());
    assertEquals("Alice", out.getName());
    assertEquals("alice@example.com", out.getEmail());
  }

  // Get User By ID Method Test Cases:
  @Test
  void getUserByID_Success() throws Exception {
    User u = User.builder().id(2L).name("Bob").email("bob@example.com").build();
    when(userRepository.findById(2L)).thenReturn(Optional.ofNullable(u));

    UserDTO dto = userService.getUserById(2L);
    assertNotNull(dto);
    assertEquals(u.getId(), dto.getId());
    assertEquals(u.getName(), dto.getName());
    assertEquals(u.getEmail(), dto.getEmail());
  }

  @Test
  void getUserByID_NotFound() {
    when(userRepository.findById(99L)).thenReturn(Optional.empty());
    Exception ex = assertThrows(
      Exception.class,
      () -> userService.getUserById(99L)
    );
    assertTrue(ex.getMessage().contains("USER_NOT_FOUND"));
  }

  // Get All Users Method Test Cases:
  @Test
  void getAllUsers_returnsEmptyList() {
    when(userRepository.findAll()).thenReturn(List.of());
    List<UserDTO> list = userService.getAllUsers();
    assertNotNull(list);
    assertTrue(list.isEmpty());
  }

  @Test
  void getAllUsers_returnsList() {
    User u1 = User.builder().id(1L).name("A").email("a@e").build();
    User u2 = User.builder().id(2L).name("B").email("b@e").build();
    when(userRepository.findAll()).thenReturn(List.of(u1, u2));

    List<UserDTO> list = userService.getAllUsers();
    assertEquals(2, list.size());
    assertEquals(u1.getId(), list.get(0).getId());
    assertEquals(u1.getName(), list.get(0).getName());
    assertEquals(u1.getEmail(), list.get(0).getEmail());
    assertEquals(u2.getId(), list.get(1).getId());
    assertEquals(u2.getName(), list.get(1).getName());
    assertEquals(u2.getEmail(), list.get(1).getEmail());
  }

  // Delete User By ID Method Test Cases:
  @Test
  void deleteUserById_Success() throws Exception {
    when(userRepository.existsById(3L)).thenReturn(true);
    doNothing().when(userRepository).deleteById(3L);
    boolean result = userService.deleteUserById(3L);
    verify(userRepository).deleteById(3L);
    assertTrue(result);
  }

  @Test
  void deleteUserById_NotFound() {
    when(userRepository.existsById(99L)).thenReturn(false);
    Exception ex = assertThrows(
      Exception.class,
      () -> userService.deleteUserById(99L)
    );
    assertTrue(ex.getMessage().contains("USER_NOT_FOUND"));
  }
}
