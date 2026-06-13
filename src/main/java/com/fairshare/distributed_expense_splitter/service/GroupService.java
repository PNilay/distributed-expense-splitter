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
import org.openapitools.api.GroupsApiDelegate;
import org.openapitools.model.AddMemberRequest;
import org.openapitools.model.CreateGroupRequest;
import org.openapitools.model.GroupDTO;
import org.openapitools.model.GroupStatus;
import org.openapitools.model.UserDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class GroupService implements GroupsApiDelegate {

  private final GroupRepository groupRepository;

  private final UserRepository userRepository;

  private ModelMapper modelMapper = new ModelMapper();

  public GroupService(GroupRepository groupRepository, UserRepository userRepository) {
    this.groupRepository = groupRepository;
    this.userRepository = userRepository;
  }

  @Override
  public ResponseEntity<GroupDTO> createGroup(CreateGroupRequest req) throws UserException {
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

    return ResponseEntity.status(HttpStatus.CREATED).body(Group.fromEntity(saved));
  }

  @Override
  public ResponseEntity<List<GroupDTO>> getGroups() {
    List<Group> groups = groupRepository.findAll();

    List<GroupDTO> dtoList = groups
      .stream()
      .map(group -> Group.fromEntity(group))
      .toList();

    return ResponseEntity.ok(dtoList);
  }

  @Override
  public ResponseEntity<GroupDTO> getGroup(Long groupId) throws GroupException {
    Group g = groupRepository
      .findById(groupId)
      .orElseThrow(() ->
        new GroupException("Service.GROUP_NOT_FOUND", ErrorCode.GROUP_NOT_FOUND)
      );
    return ResponseEntity.ok(Group.fromEntity(g));
  }

  @Override
  public ResponseEntity<GroupDTO> updateGroup(Long groupId, CreateGroupRequest req)
    throws GroupException {
    Group g = groupRepository
      .findById(groupId)
      .orElseThrow(() ->
        new GroupException("Service.GROUP_NOT_FOUND", ErrorCode.GROUP_NOT_FOUND)
      );

    if (req.getName() != null) g.setName(req.getName());
    if (req.getDescription() != null) g.setDescription(req.getDescription());

    Group saved = groupRepository.save(g);
    return ResponseEntity.ok(Group.fromEntity(saved));
  }

  @Override
  public ResponseEntity<Void> deleteGroup(Long groupId) throws GroupException {
    if (!groupRepository.existsById(groupId)) throw new GroupException(
      "Service.GROUP_NOT_FOUND",
      ErrorCode.GROUP_NOT_FOUND
    );
    groupRepository.deleteById(groupId);
    return ResponseEntity.noContent().build();
  }

  @Override
  public ResponseEntity<List<UserDTO>> getGroupMembers(Long groupId) throws GroupException {
    Group group = groupRepository
      .findById(groupId)
      .orElseThrow(() ->
        new GroupException("Service.GROUP_NOT_FOUND", ErrorCode.GROUP_NOT_FOUND)
      );

    Set<User> members = group.getMembers();
    List<UserDTO> dtoList = members
      .stream()
      .map(user -> modelMapper.map(user, UserDTO.class))
      .toList();

    return ResponseEntity.ok(dtoList);
  }

  @Override
  public ResponseEntity<Void> addMember(Long groupId, AddMemberRequest addMemberRequest)
    throws GroupException, UserException {
    Optional<Group> gOpt = groupRepository.findById(groupId);
    Group group = gOpt.orElseThrow(() ->
      new GroupException("Service.GROUP_NOT_FOUND", ErrorCode.GROUP_NOT_FOUND)
    );
    Long userId = addMemberRequest.getUserId();
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

    Set<User> members = group.getMembers();
    if (members == null) {
      members = new java.util.HashSet<>();
    } else if (!(members instanceof java.util.HashSet)) {
      members = new java.util.HashSet<>(members);
    }
    members.add(user);
    group.setMembers(members);
    groupRepository.save(group);
    return ResponseEntity.noContent().build();
  }

  @Override
  public ResponseEntity<Void> removeMember(Long groupId, Long userId)
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

    Set<User> members = group.getMembers();
    if (members == null) {
      members = new java.util.HashSet<>();
    } else if (!(members instanceof java.util.HashSet)) {
      members = new java.util.HashSet<>(members);
    }
    members.removeIf(u -> u.getId().equals(user.getId()));
    group.setMembers(members);
    groupRepository.save(group);
    return ResponseEntity.noContent().build();
  }
}
