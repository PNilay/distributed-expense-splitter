package com.fairshare.distributed_expense_splitter.controller;

import com.fairshare.distributed_expense_splitter.service.UserService;
import jakarta.validation.Valid;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openapitools.api.UsersApi;
import org.openapitools.model.CreateUserRequest;
import org.openapitools.model.UpdateUserRequest;
import org.openapitools.model.UserDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/api")
public class UserController implements UsersApi {

  @Autowired
  private UserService userService;

  @Autowired
  private Environment environment;

  private static final Logger LOGGER = LogManager.getLogger(
    UserController.class
  );

  public UserController(UserService userService) {
    this.userService = userService;
  }

  // POST /users (Create User)
  @Override
  @PostMapping("/users")
  public ResponseEntity<UserDTO> createUser(
    @Valid @RequestBody CreateUserRequest createUserRequest
  ) {
    LOGGER.info(environment.getProperty("used.create.request", "User creation request received for email {0}"), createUserRequest.getEmail());
    UserDTO res = userService.createUser(createUserRequest);
    return new ResponseEntity<>(res, HttpStatus.CREATED);
  }

  // GET /users/{userId} (Get User)
  @Override
  @GetMapping("/users/{userId}")
  public ResponseEntity<UserDTO> getUser(@PathVariable("userId") Long userId) {
    LOGGER.info(environment.getProperty("used.get.request", "User retrieval request received for userId {}"), userId);
    UserDTO user = userService.getUserById(userId);
    return ResponseEntity.ok(user);
  }

  // GET /users (Get All Users)
  @Override
  @GetMapping("/users")
  public ResponseEntity<List<UserDTO>> getUsers() {
    LOGGER.info(environment.getProperty("used.get.all.request", "User retrieval request received for all users"));
    List<UserDTO> users = userService.getAllUsers();
    return ResponseEntity.ok(users);
  }

  @Override
  @PutMapping("/users/{userId}")
  public ResponseEntity<UserDTO> updateUser(
    @PathVariable("userId") Long userId,
    @Valid @RequestBody UpdateUserRequest updateUserRequest
  ) {
    LOGGER.info(environment.getProperty("used.update.request", "User update request received for userId {}"), userId);
    UserDTO user = userService.updateUser(userId, updateUserRequest);
    return new ResponseEntity<>(user, HttpStatus.OK);
  }

  // DELETE /users/{userId} (Delete User)
  @Override
  @DeleteMapping("/users/{userId}")
  public ResponseEntity<Void> deleteUserById(
    @PathVariable("userId") Long userId
  ) {
    LOGGER.info(environment.getProperty("used.delete.request", "User deletion request received for userId {}"), userId);
    userService.deleteUserById(userId);
    return new ResponseEntity<>(HttpStatus.NO_CONTENT);
  }
}
