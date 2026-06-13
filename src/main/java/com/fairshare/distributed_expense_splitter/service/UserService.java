package com.fairshare.distributed_expense_splitter.service;

import com.fairshare.distributed_expense_splitter.entity.ErrorCode;
import com.fairshare.distributed_expense_splitter.entity.User;
import com.fairshare.distributed_expense_splitter.exception.UserException;
import com.fairshare.distributed_expense_splitter.repository.UserRepository;
import java.util.List;
import java.util.Optional;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.openapitools.api.UsersApiDelegate;
import org.openapitools.model.CreateUserRequest;
import org.openapitools.model.UpdateUserRequest;
import org.openapitools.model.UserDTO;
import org.openapitools.model.UserStatus;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class UserService implements UsersApiDelegate {

  private final UserRepository userRepository;

  private ModelMapper modelMapper = new ModelMapper();

  public UserService(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  @Override
  public ResponseEntity<UserDTO> createUser(CreateUserRequest createUserRequest)
    throws UserException {
    User user = modelMapper.map(createUserRequest, User.class);
    user.setStatus(UserStatus.ACTIVE);
    User saved = userRepository.save(user);
    UserDTO dto = modelMapper.map(saved, UserDTO.class);
    return ResponseEntity.status(HttpStatus.CREATED).body(dto);
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

  @Override
  public ResponseEntity<Void> deleteUserById(Long userId) throws UserException {
    if (!userRepository.existsById(userId)) {
      throw new UserException(
        "Service.USER_NOT_FOUND",
        ErrorCode.USER_NOT_FOUND
      );
    }
    userRepository.deleteById(userId);
    return ResponseEntity.noContent().build();
  }

  @Override
  public ResponseEntity<UserDTO> updateUser(Long userId, UpdateUserRequest updateUserRequest) {
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
    UserDTO dto = modelMapper.map(saved, UserDTO.class);
    return ResponseEntity.ok(dto);
  }

  @Override
  public ResponseEntity<UserDTO> getUser(Long userId) throws UserException {
    UserDTO dto = getUserById(userId);
    return ResponseEntity.ok(dto);
  }

  @Override
  public ResponseEntity<List<UserDTO>> getUsers() {
    List<UserDTO> list = getAllUsers();
    return ResponseEntity.ok(list);
  }

}
