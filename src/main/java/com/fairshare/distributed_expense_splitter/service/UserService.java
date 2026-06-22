package com.fairshare.distributed_expense_splitter.service;

import com.fairshare.distributed_expense_splitter.entity.ErrorCode;
import com.fairshare.distributed_expense_splitter.entity.Expense;
import com.fairshare.distributed_expense_splitter.entity.ExpenseSplit;
import com.fairshare.distributed_expense_splitter.entity.Group;
import com.fairshare.distributed_expense_splitter.entity.User;
import com.fairshare.distributed_expense_splitter.exception.UserException;
import com.fairshare.distributed_expense_splitter.repository.ExpenseRepository;
import com.fairshare.distributed_expense_splitter.repository.GroupRepository;
import com.fairshare.distributed_expense_splitter.repository.UserRepository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.openapitools.model.CreateUserRequest;
import org.openapitools.model.UpdateUserRequest;
import org.openapitools.model.UserDTO;
import org.openapitools.model.UserStatus;
import org.openapitools.model.RelatedUserDTO;
import org.openapitools.model.RelationshipContext;
import org.openapitools.jackson.nullable.JsonNullable;
import org.openapitools.model.ActivityDTO;
import org.openapitools.model.ActivityPageDTO;
import org.openapitools.model.UserBalanceSummaryDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private ExpenseRepository expenseRepository;

  @Autowired
  private GroupRepository groupRepository;

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
    User user = optional.orElseThrow(() -> new UserException("Service.USER_NOT_FOUND", ErrorCode.USER_NOT_FOUND));
    return modelMapper.map(user, UserDTO.class);
  }

  public List<UserDTO> getAllUsers() {
    Iterable<User> users = userRepository.findAll();
    return modelMapper.map(users, new TypeToken<List<UserDTO>>() {
    }.getType());
  }

  public boolean deleteUserById(Long userId) throws UserException {
    if (!userRepository.existsById(userId)) {
      throw new UserException(
          "Service.USER_NOT_FOUND",
          ErrorCode.USER_NOT_FOUND);
    }
    userRepository.deleteById(userId);
    return true;
  }

  public UserDTO updateUser(Long userId, UpdateUserRequest updateUserRequest) {
    User user = userRepository
        .findById(userId)
        .orElseThrow(() -> new UserException("Service.USER_NOT_FOUND", ErrorCode.USER_NOT_FOUND));

    if (updateUserRequest.getName() != null) {
      user.setName(updateUserRequest.getName());
    }
    if (updateUserRequest.getEmail() != null) {
      user.setEmail(updateUserRequest.getEmail());
    }

    User saved = userRepository.save(user);
    return modelMapper.map(saved, UserDTO.class);
  }

  public List<RelatedUserDTO> getRelatedUsers(Long userId) {
    userRepository
        .findById(userId)
        .orElseThrow(() -> new UserException("Service.USER_NOT_FOUND", ErrorCode.USER_NOT_FOUND));

    Map<Long, RelationshipContext> ctx = new HashMap<>();

    List<Long> groupMemberIds = groupRepository.findCoMemberIds(userId);
    for (Long memberId : groupMemberIds) {
      if (memberId.equals(userId))
        continue;

      ctx.put(memberId, RelationshipContext.GROUP);
    }

    Set<Long> expenseUserIds = new HashSet<>();
    expenseUserIds.addAll(expenseRepository.findExpenseContactIdsWhereUserIsPayerOrParticipant(userId));
    expenseUserIds.addAll(expenseRepository.findExpensePayerIdsWhereUserIsParticipant(userId));

    for (Long expenseUserId : expenseUserIds) {
      if (expenseUserId.equals(userId))
        continue;

      ctx.merge(expenseUserId, RelationshipContext.INDIVIDUAL,
          (oldV, newV) -> oldV == RelationshipContext.GROUP ? RelationshipContext.BOTH : oldV);
    }

    List<RelatedUserDTO> out = new ArrayList<>();
    for (Long otherId : ctx.keySet()) {
      User other = userRepository.findById(otherId).orElse(null);
      if (other == null)
        continue;
      RelatedUserDTO dto = new RelatedUserDTO();
      dto.setId(other.getId());
      dto.setName(other.getName());
      dto.setEmail(other.getEmail());
      dto.setStatus(other.getStatus());
      dto.setRelationshipContext(ctx.get(otherId));
      out.add(dto);
    }

    return out;
  }

  public UserBalanceSummaryDTO getUserBalanceSummary(Long userId) {
    userRepository
        .findById(userId)
        .orElseThrow(() -> new UserException("Service.USER_NOT_FOUND", ErrorCode.USER_NOT_FOUND));

    double totalOwedToYou = 0.0;
    double totalYouOwe = 0.0;

    List<Expense> expenses = expenseRepository.findAll();
    for (Expense e : expenses) {
      Long payer = e.getPaidBy().getId();
      for (ExpenseSplit s : e.getSplits()) {
        Long borrower = s.getUser().getId();
        double amt = s.getShareInCents();
        if (borrower.equals(userId) && !payer.equals(userId)) {
          totalYouOwe += amt;
        }
        if (payer.equals(userId) && !borrower.equals(userId)) {
          totalOwedToYou += amt;
        }
      }
    }

    UserBalanceSummaryDTO dto = new UserBalanceSummaryDTO();
    dto.setTotalOwedToYou(totalOwedToYou);
    dto.setTotalYouOwe(totalYouOwe);
    dto.setNetBalance(totalOwedToYou - totalYouOwe);
    return dto;
  }
}
