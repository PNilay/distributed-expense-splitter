package com.fairshare.distributed_expense_splitter.service;

import com.fairshare.distributed_expense_splitter.entity.ErrorCode;
import com.fairshare.distributed_expense_splitter.entity.Expense;
import com.fairshare.distributed_expense_splitter.entity.ExpenseSplit;
import com.fairshare.distributed_expense_splitter.entity.Group;
import com.fairshare.distributed_expense_splitter.entity.User;
import com.fairshare.distributed_expense_splitter.exception.GroupException;
import com.fairshare.distributed_expense_splitter.exception.UserException;
import com.fairshare.distributed_expense_splitter.repository.ExpenseRepository;
import com.fairshare.distributed_expense_splitter.repository.GroupRepository;
import com.fairshare.distributed_expense_splitter.repository.UserRepository;
import java.util.List;
import java.util.Map;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Optional;
import java.util.PriorityQueue;
import java.util.Set;
import org.modelmapper.ModelMapper;
import org.openapitools.model.CreateGroupRequest;
import org.openapitools.model.CurrentUserBalance;
import org.openapitools.model.ExpenseListDTO;
import org.openapitools.model.GroupDTO;
import org.openapitools.model.GroupStatus;
import org.openapitools.model.GroupSummaryDTO;
import org.openapitools.model.SimplifiedDebt;
import org.openapitools.model.UserDTO;
import org.openapitools.model.GroupBalanceResponse;
import org.openapitools.model.UserBalance;
import org.openapitools.model.OptimizedTransactionDTO;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class GroupService {

  private final ExpenseService expenseService;

  private final GroupRepository groupRepository;

  private final UserRepository userRepository;

  private final ExpenseRepository expenseRepository;

  private ModelMapper modelMapper = new ModelMapper();

  public GroupService(
      GroupRepository groupRepository,
      UserRepository userRepository,
      ExpenseRepository expenseRepository, ExpenseService expenseService) {
    this.groupRepository = groupRepository;
    this.userRepository = userRepository;
    this.expenseRepository = expenseRepository;
    this.expenseService = expenseService;
  }

  public GroupDTO createGroup(CreateGroupRequest req) throws UserException {
    User creator = userRepository
        .findById(req.getCreatedBy())
        .orElseThrow(() -> new UserException("Service.USER_NOT_FOUND", ErrorCode.USER_NOT_FOUND));

    Group g = Group
        .builder()
        .name(req.getName())
        .description(req.getDescription())
        .createdBy(creator)
        .status(GroupStatus.ACTIVE)
        .member(creator)
        .build();
    Group saved = groupRepository.save(g);

    return Group.fromEntity(saved);
  }

  public GroupBalanceResponse getGroupBalances(Long groupId) {
    Group group = groupRepository
        .findById(groupId)
        .orElseThrow(() -> new GroupException("Service.GROUP_NOT_FOUND", ErrorCode.GROUP_NOT_FOUND));

    Map<Long, Double> balances = new HashMap<>();

    // Initialize balances for group's members
    if (group.getMembers() != null) {
      for (User u : group.getMembers()) {
        balances.put(u.getId(), 0.0);
      }
    }

    // Sum debts from expenses belonging to the group
    List<Expense> expenses = expenseRepository.findByGroupId(groupId);
    for (Expense e : expenses) {
      Long payer = e.getPaidBy().getId();
      for (ExpenseSplit s : e.getSplits()) {
        Long borrower = s.getUser().getId();
        double amt = s.getShareInCents();
        balances.put(payer, balances.getOrDefault(payer, 0.0) + amt);
        balances.put(borrower, balances.getOrDefault(borrower, 0.0) - amt);
      }
    }

    GroupBalanceResponse resp = new GroupBalanceResponse();
    List<UserBalance> list = new ArrayList<>();
    for (Map.Entry<Long, Double> e : balances.entrySet()) {
      UserBalance ub = new UserBalance();
      ub.setUserId(e.getKey());
      ub.setAmount(e.getValue());
      list.add(ub);
    }
    resp.setBalances(list);
    return resp;
  }

  public List<OptimizedTransactionDTO> getSimplifiedDebts(Long groupId) {
    GroupBalanceResponse gbr = getGroupBalances(groupId);
    Map<Long, Double> balances = new HashMap<>();
    for (UserBalance ub : gbr.getBalances()) {
      balances.put(ub.getUserId(), ub.getAmount());
    }

    PriorityQueue<Map.Entry<Long, Double>> creditors = new PriorityQueue<>(
        (a, b) -> Double.compare(b.getValue(), a.getValue()));
    PriorityQueue<Map.Entry<Long, Double>> debtors = new PriorityQueue<>(
        (a, b) -> Double.compare(a.getValue(), b.getValue()));

    for (Map.Entry<Long, Double> e : balances.entrySet()) {
      if (e.getValue() > 0.0)
        creditors.add(new AbstractMap.SimpleEntry<>(e.getKey(), e.getValue()));
      else if (e.getValue() < 0.0)
        debtors.add(new AbstractMap.SimpleEntry<>(e.getKey(), e.getValue()));
    }

    List<OptimizedTransactionDTO> out = new ArrayList<>();

    while (!creditors.isEmpty() && !debtors.isEmpty()) {
      Map.Entry<Long, Double> cred = creditors.poll();
      Map.Entry<Long, Double> debt = debtors.poll();
      double settle = Math.min(cred.getValue(), -debt.getValue());
      OptimizedTransactionDTO t = new OptimizedTransactionDTO();
      t.setFromUserId(debt.getKey());
      t.setToUserId(cred.getKey());
      t.setAmount(settle);
      out.add(t);

      double newCred = cred.getValue() - settle;
      double newDebt = debt.getValue() + settle; // debt is negative
      if (newCred > 0.000001)
        creditors.add(new AbstractMap.SimpleEntry<>(cred.getKey(), newCred));
      if (newDebt < -0.000001)
        debtors.add(new AbstractMap.SimpleEntry<>(debt.getKey(), newDebt));
    }

    return out;
  }

  public List<GroupSummaryDTO> getGroupSummaries(Long currentUserId) {
    if (currentUserId == null) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Current user identity is required");
    }

    List<Group> groups = groupRepository.findByMembers_Id(currentUserId);
    return groups.stream()
        .map(group -> buildGroupSummary(group, currentUserId))
        .toList();
  }

  public GroupSummaryDTO getGroupSummary(Long groupId, Long currentUserId) {
    Group group = groupRepository
        .findById(groupId)
        .orElseThrow(() -> new GroupException("Service.GROUP_NOT_FOUND", ErrorCode.GROUP_NOT_FOUND));

    if (currentUserId == null) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Current user identity is required");
    }
    if (!group.hasMember(currentUserId)) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User is not a member of this group");
    }

    return buildGroupSummary(group, currentUserId);
  }

  private GroupSummaryDTO buildGroupSummary(Group group, Long currentUserId) {
    List<Expense> expenses = expenseRepository.findByGroupId(group.getId());
    GroupSummaryDTO dto = new GroupSummaryDTO();
    dto.setId(group.getId().intValue());
    dto.setName(group.getName());
    dto.setDescription(group.getDescription());
    dto.setIcon(group.getIcon());
    dto.setStatus(GroupSummaryDTO.StatusEnum.fromValue(group.getStatus().toString()));
    dto.setCreatedAt(group.getCreatedAt());
    dto.setCreatedBy(group.getCreatedBy() != null ? group.getCreatedBy().getId().intValue() : null);
    dto.setExpenseCount(expenses.size());
    dto.setCurrentUserBalance(buildCurrentUserBalance(expenses, currentUserId));
    List<UserDTO> members = group.getMembers()
        .stream()
        .map(user -> modelMapper.map(user, UserDTO.class))
        .toList();
    dto.setMembers(members);
    dto.setSimplifiedDebts(getSimplifiedDebts(group.getId()).stream().map(this::mapToSimplifiedDebt).toList());
    return dto;
  }

  private CurrentUserBalance buildCurrentUserBalance(List<Expense> expenses, Long currentUserId) {
    double balance = 0.0;
    for (Expense expense : expenses) {
      Long payerId = expense.getPaidBy().getId();
      if (payerId.equals(currentUserId)) {
        for (ExpenseSplit split : expense.getSplits()) {
          if (!split.getUser().getId().equals(currentUserId)) {
            balance += split.getShareInCents();
          }
        }
      } else {
        for (ExpenseSplit split : expense.getSplits()) {
          if (split.getUser().getId().equals(currentUserId)) {
            balance -= split.getShareInCents();
          }
        }
      }
    }
    CurrentUserBalance currentUserBalance = new CurrentUserBalance();
    currentUserBalance.setAmount((float) balance);
    if (Math.abs(balance) < 0.00001) {
      currentUserBalance.setStatus(CurrentUserBalance.StatusEnum.SETTLED);
    } else if (balance > 0) {
      currentUserBalance.setStatus(CurrentUserBalance.StatusEnum.OWED);
    } else {
      currentUserBalance.setStatus(CurrentUserBalance.StatusEnum.OWES);
    }
    return currentUserBalance;
  }

  private SimplifiedDebt mapToSimplifiedDebt(OptimizedTransactionDTO tx) {
    SimplifiedDebt simplifiedDebt = new SimplifiedDebt();
    simplifiedDebt.setFromUserId(tx.getFromUserId() != null ? tx.getFromUserId().intValue() : null);
    simplifiedDebt.setToUserId(tx.getToUserId() != null ? tx.getToUserId().intValue() : null);
    simplifiedDebt.setAmount(tx.getAmount() != null ? tx.getAmount().floatValue() : null);
    return simplifiedDebt;
  }

  public List<GroupDTO> getGroups() {
    List<Group> groups = groupRepository.findAll();

    return groups
        .stream()
        .map(group -> Group.fromEntity(group))
        .toList();
  }

  public GroupDTO getGroup(Long groupId) throws GroupException {
    Group g = groupRepository
        .findById(groupId)
        .orElseThrow(() -> new GroupException("Service.GROUP_NOT_FOUND", ErrorCode.GROUP_NOT_FOUND));
    return Group.fromEntity(g);
  }

  public GroupDTO updateGroup(Long groupId, CreateGroupRequest req)
      throws GroupException {
    Group g = groupRepository
        .findById(groupId)
        .orElseThrow(() -> new GroupException("Service.GROUP_NOT_FOUND", ErrorCode.GROUP_NOT_FOUND));

    if (req.getName() != null)
      g.setName(req.getName());
    if (req.getDescription() != null)
      g.setDescription(req.getDescription());

    Group saved = groupRepository.save(g);
    return Group.fromEntity(saved);
  }

  public void deleteGroup(Long groupId) throws GroupException {
    if (!groupRepository.existsById(groupId))
      throw new GroupException(
          "Service.GROUP_NOT_FOUND",
          ErrorCode.GROUP_NOT_FOUND);
    groupRepository.deleteById(groupId);
  }

  public List<UserDTO> getGroupMembers(Long groupId) throws GroupException {
    Group group = groupRepository
        .findById(groupId)
        .orElseThrow(() -> new GroupException("Service.GROUP_NOT_FOUND", ErrorCode.GROUP_NOT_FOUND));

    Set<User> members = group.getMembers();
    return members
        .stream()
        .map(user -> modelMapper.map(user, UserDTO.class))
        .toList();
  }

  public void addMember(Long groupId, Long userId)
      throws GroupException, UserException {
    Optional<Group> gOpt = groupRepository.findById(groupId);
    Group group = gOpt.orElseThrow(() -> new GroupException("Service.GROUP_NOT_FOUND", ErrorCode.GROUP_NOT_FOUND));
    Optional<User> uOpt = userRepository.findById(userId);
    User user = uOpt.orElseThrow(() -> new UserException("Service.USER_NOT_FOUND", ErrorCode.USER_NOT_FOUND));

    // Check if user is already a member
    if (group.getMembers().stream().anyMatch(u -> u.getId().equals(user.getId()))) {
      throw new GroupException(
          "Service.USER_ALREADY_MEMBER",
          ErrorCode.USER_ALREADY_MEMBER);
    }

    // make sure members set is mutable before modifying (tests may use singleton
    // sets)
    Set<User> members = group.getMembers();
    if (members == null) {
      members = new HashSet<>();
    } else if (!(members instanceof HashSet)) {
      members = new HashSet<>(members);
    }
    members.add(user);
    group.setMembers(members);
    groupRepository.save(group);
  }

  public void removeMember(Long groupId, Long userId)
      throws GroupException, UserException {
    Group group = groupRepository
        .findById(groupId)
        .orElseThrow(() -> new GroupException("Service.GROUP_NOT_FOUND", ErrorCode.GROUP_NOT_FOUND));
    User user = userRepository
        .findById(userId)
        .orElseThrow(() -> new UserException("Service.USER_NOT_FOUND", ErrorCode.USER_NOT_FOUND));

    if (!group.getMembers().stream().anyMatch(u -> u.getId().equals(user.getId()))) {
      throw new GroupException(
          "Service.USER_NOT_MEMBER",
          ErrorCode.USER_NOT_MEMBER);
    }

    // make sure members set is mutable before modifying
    Set<User> members = group.getMembers();
    if (members == null) {
      members = new HashSet<>();
    } else if (!(members instanceof HashSet)) {
      members = new HashSet<>(members);
    }
    members.removeIf(u -> u.getId().equals(user.getId()));
    group.setMembers(members);
    groupRepository.save(group);
  }

  public ExpenseListDTO getGroupExpenses(Long groupId, int limit, Long beforeId, Long afterId) {
    groupRepository
        .findById(groupId)
        .orElseThrow(() -> new GroupException("Service.GROUP_NOT_FOUND", ErrorCode.GROUP_NOT_FOUND));

    // Also should verify if the user call for this group info is part of group
    // TO DO:
    return expenseService.getGroupExpenses(groupId, limit, beforeId, afterId);

  }

}
