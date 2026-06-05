package com.fairshare.distributed_expense_splitter.controller;

import com.fairshare.distributed_expense_splitter.service.UserService;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import java.util.List;
import org.openapitools.model.User;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
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
  public ResponseEntity<User> createUser(@RequestBody User user) {
    System.out.println("Received user: " + user);
    User res = userService.createUser(user);
    return new ResponseEntity<>(res, HttpStatus.CREATED);
  }

  // Get user by id
  @GetMapping("/user/{id}")
  public ResponseEntity<User> getUserById() {
    User user = userService.getUserById();
    return ResponseEntity.ok(user);
  }

  // Get all users
  @GetMapping("/users")
  public ResponseEntity<List<User>> getAllUsers() {
    List<User> users = userService.getAllUsers();
    return ResponseEntity.ok(users);
  }

  // Delete user by id
  @DeleteMapping("/user/{id}")
  public ResponseEntity<String> deleteUserById() {
    return ResponseEntity.ok("User deleted!");
  }
}
