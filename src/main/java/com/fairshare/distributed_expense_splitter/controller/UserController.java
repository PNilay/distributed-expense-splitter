package com.fairshare.distributed_expense_splitter.controller;

import com.fairshare.distributed_expense_splitter.service.UserService;
import jakarta.validation.Valid;
import java.util.List;
import org.openapitools.api.UsersApi;
import org.openapitools.model.CreateUserRequest;
import org.openapitools.model.UserDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/api")
public class UserController implements UsersApi {

  private final UserService userService;

  public UserController(UserService userService) {
    this.userService = userService;
  }

  // POST /users (Create User)
  @Override
  @PostMapping("/users")
  public ResponseEntity<UserDTO> createUser(
    @Valid @RequestBody CreateUserRequest createUserRequest
  ) {
    UserDTO res = userService.createUser(createUserRequest);
    return new ResponseEntity<>(res, HttpStatus.CREATED);
  }

  // GET /users/{userId} (Get User)
  @Override
  @GetMapping("/users/{userId}")
  public ResponseEntity<UserDTO> getUser(@PathVariable("userId") Long userId) {
    try {
      UserDTO user = userService.getUserById(userId);
      return ResponseEntity.ok(user);
    } catch (Exception e) {
      String errorMessage = e.getMessage();
      if ("Service.USER_NOT_FOUND".equals(errorMessage)) {
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
      }
      return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  // GET /users (Get All Users)
  @Override
  @GetMapping("/users")
  public ResponseEntity<List<UserDTO>> getUsers() {
    List<UserDTO> users = userService.getAllUsers();
    return ResponseEntity.ok(users);
  }

  // DELETE /users/{userId} (Delete user by id)
  @DeleteMapping("/users/{userId}")
  public ResponseEntity<String> deleteUserById(
    @PathVariable("userId") Long userId
  ) throws Exception {
    userService.deleteUserById(userId);
    String successMessage = "User with id " + userId + " deleted successfully.";
    return new ResponseEntity<>(successMessage, HttpStatus.OK);
  }
}
