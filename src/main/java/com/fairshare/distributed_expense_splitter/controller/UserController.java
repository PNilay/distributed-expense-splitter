package com.fairshare.distributed_expense_splitter.controller;

import com.fairshare.distributed_expense_splitter.service.UserService;
import java.util.List;
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
public class UserController {

  private final UserService userService;

  public UserController(UserService userService) {
    this.userService = userService;
  }

  // POST user
  @PostMapping("/user")
  public ResponseEntity<UserDTO> createUser(@RequestBody UserDTO user) {
    System.out.println("Received user: " + user);
    UserDTO res = userService.createUser(user);
    return new ResponseEntity<>(res, HttpStatus.CREATED);
  }

  // Get user by id
  @GetMapping("/user/{id}")
  public ResponseEntity<UserDTO> getUserById(@PathVariable("id") Long userId)
    throws Exception {
    UserDTO user = userService.getUserById(userId);
    return ResponseEntity.ok(user);
  }

  // Get all users
  @GetMapping("/users")
  public ResponseEntity<List<UserDTO>> getAllUsers() {
    List<UserDTO> users = userService.getAllUsers();
    return ResponseEntity.ok(users);
  }

  // Delete user by id
  @DeleteMapping("/user/{id}")
  public ResponseEntity<String> deleteUserById(@PathVariable("id") Long userId) throws Exception {
    userService.deleteUserById(userId);
    String successMessage = "User with id " + userId + " deleted successfully.";
    return new ResponseEntity<>(successMessage, HttpStatus.OK);
  }
}
