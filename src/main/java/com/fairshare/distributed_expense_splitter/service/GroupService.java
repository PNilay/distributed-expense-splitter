package com.fairshare.distributed_expense_splitter.service;

import com.fairshare.distributed_expense_splitter.entity.ErrorCode;
import com.fairshare.distributed_expense_splitter.entity.Group;
import com.fairshare.distributed_expense_splitter.entity.User;
import com.fairshare.distributed_expense_splitter.exception.GroupException;
import com.fairshare.distributed_expense_splitter.exception.UserException;
import com.fairshare.distributed_expense_splitter.repository.GroupRepository;
import com.fairshare.distributed_expense_splitter.repository.UserRepository;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.modelmapper.ModelMapper;
import org.openapitools.model.CreateGroupRequest;
import org.openapitools.model.GroupDTO;
import org.openapitools.model.GroupStatus;
import org.openapitools.model.UserDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class GroupService {

  @Autowired
  private GroupRepository groupRepository;

  @Autowired
  private UserRepository userRepository;

  private ModelMapper modelMapper = new ModelMapper();

  public GroupDTO createGroup(CreateGroupRequest req) throws UserException {
    User creator = userRepository
      .findById(req.getCreatedBy())
      .orElseThrow(() ->
        new UserException("Service.USER_NOT_FOUND", ErrorCode.USER_NOT_FOUND)
      );

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
      .orElseThrow(() ->
        new GroupException("Service.GROUP_NOT_FOUND", ErrorCode.GROUP_NOT_FOUND)
      );
    return Group.fromEntity(g);
  }

  public GroupDTO updateGroup(Long groupId, CreateGroupRequest req)
    throws GroupException {
    Group g = groupRepository
      .findById(groupId)
      .orElseThrow(() ->
        new GroupException("Service.GROUP_NOT_FOUND", ErrorCode.GROUP_NOT_FOUND)
      );

    if (req.getName() != null) g.setName(req.getName());
    if (req.getDescription() != null) g.setDescription(req.getDescription());

    Group saved = groupRepository.save(g);
    return Group.fromEntity(saved);
  }

  public void deleteGroup(Long groupId) throws GroupException {
    if (!groupRepository.existsById(groupId)) throw new GroupException(
      "Service.GROUP_NOT_FOUND",
      ErrorCode.GROUP_NOT_FOUND
    );
    groupRepository.deleteById(groupId);
  }

  public List<UserDTO> getGroupMembers(Long groupId) throws GroupException {
    Group group = groupRepository
      .findById(groupId)
      .orElseThrow(() ->
        new GroupException("Service.GROUP_NOT_FOUND", ErrorCode.GROUP_NOT_FOUND)
      );

    Set<User> members = group.getMembers();
    return members
      .stream()
      .map(user -> modelMapper.map(user, UserDTO.class))
      .toList();
  }

  public void addMember(Long groupId, Long userId)
    throws GroupException, UserException {
    Optional<Group> gOpt = groupRepository.findById(groupId);
    Group group = gOpt.orElseThrow(() ->
      new GroupException("Service.GROUP_NOT_FOUND", ErrorCode.GROUP_NOT_FOUND)
    );
    Optional<User> uOpt = userRepository.findById(userId);
    User user = uOpt.orElseThrow(() ->
      new UserException("Service.USER_NOT_FOUND", ErrorCode.USER_NOT_FOUND)
    );

    // Check if user is already a member
    if (
      group.getMembers().stream().anyMatch(u -> u.getId().equals(user.getId()))
    ) {
      throw new GroupException(
        "Service.USER_ALREADY_MEMBER",
        ErrorCode.USER_ALREADY_MEMBER
      );
    }

    // make sure members set is mutable before modifying (tests may use singleton sets)
    Set<User> members = group.getMembers();
    if (members == null) {
      members = new java.util.HashSet<>();
    } else if (!(members instanceof java.util.HashSet)) {
      members = new java.util.HashSet<>(members);
    }
    members.add(user);
    group.setMembers(members);
    groupRepository.save(group);
  }

  public void removeMember(Long groupId, Long userId)
    throws GroupException, UserException {
    Group group = groupRepository
      .findById(groupId)
      .orElseThrow(() ->
        new GroupException("Service.GROUP_NOT_FOUND", ErrorCode.GROUP_NOT_FOUND)
      );
    User user = userRepository
      .findById(userId)
      .orElseThrow(() ->
        new UserException("Service.USER_NOT_FOUND", ErrorCode.USER_NOT_FOUND)
      );

    if (
      !group.getMembers().stream().anyMatch(u -> u.getId().equals(user.getId()))
    ) {
      throw new GroupException(
        "Service.USER_NOT_MEMBER",
        ErrorCode.USER_NOT_MEMBER
      );
    }

    // make sure members set is mutable before modifying
    Set<User> members = group.getMembers();
    if (members == null) {
      members = new java.util.HashSet<>();
    } else if (!(members instanceof java.util.HashSet)) {
      members = new java.util.HashSet<>(members);
    }
    members.removeIf(u -> u.getId().equals(user.getId()));
    group.setMembers(members);
    groupRepository.save(group);
  }
}
