package com.fairshare.distributed_expense_splitter.service;

import java.util.ArrayList;
import java.util.List;
import org.openapitools.model.User;
import org.springframework.stereotype.Service;

@Service
public class UserService {

  private List<User> users;

  public UserService() {
    this.users = new ArrayList<User>();
    this.users.add(new User().id(1L).name("John Doe").email("john.doe  @example.com"));
    this.users.add(new User().id(2L).name("Jane Smith").email("jane.s@exahd.com"));
    this.users.add(new User().id(3L).name("Bob Johnson").email("bob.john@example.com"));
  }

  public User createUser(User user) {
    this.users.add(user);
    return user;
  }

  public User getUserById() {
    User user = new User();
    user.setId(1L);
    user.setName("John Doe");
    user.setEmail("abc@gmail.com");
    return user;
  }

  public List<User> getAllUsers() {
    return this.users;
  }
}
