package com.fairshare.distributed_expense_splitter.service;

import com.fairshare.distributed_expense_splitter.entity.User;
import com.fairshare.distributed_expense_splitter.repository.UserRepository;
import java.util.List;
import java.util.Optional;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.openapitools.model.UserDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {

  @Autowired
  private UserRepository userRepository;

  private ModelMapper modelMapper = new ModelMapper();

  public UserDTO createUser(UserDTO userDto) {
    return modelMapper.map(
      userRepository.save(modelMapper.map(userDto, User.class)),
      UserDTO.class
    );
  }

  public UserDTO getUserById(Long userId) throws Exception {
    Optional<User> optional = userRepository.findById(userId);
    User user = optional.orElseThrow(() ->
      new Exception("Service.USER_NOT_FOUND")
    );
    return modelMapper.map(user, UserDTO.class);
  }

  public List<UserDTO> getAllUsers() {
    Iterable<User> users = userRepository.findAll();
    return modelMapper.map(users, new TypeToken<List<UserDTO>>() {}.getType());
  }

  public void deleteUserById(Long userId) throws Exception {
    if (!userRepository.existsById(userId)) {
      throw new Exception("Service.USER_NOT_FOUND");
    }
    userRepository.deleteById(userId);
  }
}
