package com.fairshare.distributed_expense_splitter.service;

import com.fairshare.distributed_expense_splitter.entity.ErrorCode;
import com.fairshare.distributed_expense_splitter.entity.User;
import com.fairshare.distributed_expense_splitter.exception.UserException;
import com.fairshare.distributed_expense_splitter.repository.UserRepository;
import java.util.List;
import java.util.Optional;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.openapitools.model.CreateUserRequest;
import org.openapitools.model.UpdateUserRequest;
import org.openapitools.model.UserDTO;
import org.openapitools.model.UserStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {

  @Autowired
  private UserRepository userRepository;

  private ModelMapper modelMapper = new ModelMapper();

  public UserDTO createUser(CreateUserRequest createUserRequest)
    throws UserException {
    User user = modelMapper.map(createUserRequest, User.class);
    user.setStatus(UserStatus.ACTIVE);
    User saved = userRepository.save(user);
    return modelMapper.map(saved, UserDTO.class);
  }

  public UserDTO getUserById(Long userId) throws UserException {
    Optional<User> optional = userRepository.findById(userId);
    User user = optional.orElseThrow(() ->
      new UserException("Service.USER_NOT_FOUND", ErrorCode.USER_NOT_FOUND)
    );
    return modelMapper.map(user, UserDTO.class);
  }

  public List<UserDTO> getAllUsers() {
    Iterable<User> users = userRepository.findAll();
    return modelMapper.map(users, new TypeToken<List<UserDTO>>() {}.getType());
  }

  public boolean deleteUserById(Long userId) throws UserException {
    if (!userRepository.existsById(userId)) {
      throw new UserException(
        "Service.USER_NOT_FOUND",
        ErrorCode.USER_NOT_FOUND
      );
    }
    userRepository.deleteById(userId);
    return true;
  }

  public UserDTO updateUser(Long userId, UpdateUserRequest updateUserRequest) {
    User user = userRepository
      .findById(userId)
      .orElseThrow(() ->
        new UserException("Service.USER_NOT_FOUND", ErrorCode.USER_NOT_FOUND)
      );

    if (updateUserRequest.getName() != null) {
      user.setName(updateUserRequest.getName());
    }
    if (updateUserRequest.getEmail() != null) {
      user.setEmail(updateUserRequest.getEmail());
    }

    User saved = userRepository.save(user);
    return modelMapper.map(saved, UserDTO.class);
  }
}
