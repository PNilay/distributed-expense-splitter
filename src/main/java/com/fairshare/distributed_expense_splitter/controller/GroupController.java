package com.fairshare.distributed_expense_splitter.controller;

import com.fairshare.distributed_expense_splitter.service.GroupService;
import jakarta.validation.Valid;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openapitools.api.GroupsApi;
import org.openapitools.model.AddMemberRequest;
import org.openapitools.model.CreateGroupRequest;
import org.openapitools.model.GroupDTO;
import org.openapitools.model.UserDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/api")
public class GroupController implements GroupsApi {

  private final GroupService groupService;

  public GroupController(GroupService groupService) {
    this.groupService = groupService;
  }

  private static final Logger LOGGER = LogManager.getLogger(
    GroupController.class
  );

  @Override
  @PostMapping("/groups")
  public ResponseEntity<GroupDTO> createGroup(
    @Valid @RequestBody CreateGroupRequest req
  ) {
    LOGGER.info(
      "Group creation request received for group name {}",
      req.getName()
    );
    GroupDTO dto = groupService.createGroup(req);
    return new ResponseEntity<>(dto, HttpStatus.CREATED);
  }

  @GetMapping("/groups")
  @Override
  public ResponseEntity<List<GroupDTO>> getGroups() {
    LOGGER.info("Group list retrieval request received");
    List<GroupDTO> list = groupService.getGroups();
    return ResponseEntity.ok(list);
  }

  @GetMapping("/groups/{groupId}")
  @Override
  public ResponseEntity<GroupDTO> getGroup(
    @PathVariable("groupId") Long groupId
  ) {
    LOGGER.info("Group retrieval request received for groupId {}", groupId);
    GroupDTO dto = groupService.getGroup(groupId);
    return ResponseEntity.ok(dto);
  }

  @PutMapping("/groups/{groupId}")
  @Override
  public ResponseEntity<GroupDTO> updateGroup(
    @PathVariable("groupId") Long groupId,
    @Valid @RequestBody CreateGroupRequest req
  ) {
    LOGGER.info("Group update request received for groupId {}", groupId);
    GroupDTO dto = groupService.updateGroup(groupId, req);
    return ResponseEntity.ok(dto);
  }

  @DeleteMapping("/groups/{groupId}")
  @Override
  public ResponseEntity<Void> deleteGroup(
    @PathVariable("groupId") Long groupId
  ) {
    LOGGER.info("Group deletion request received for groupId {}", groupId);
    groupService.deleteGroup(groupId);
    return new ResponseEntity<>(HttpStatus.NO_CONTENT);
  }

  @GetMapping("/groups/{groupId}/members")
  @Override
  public ResponseEntity<List<UserDTO>> getGroupMembers(
    @PathVariable("groupId") Long groupId
  ) {
    LOGGER.info(
      "Group members retrieval request received for groupId {}",
      groupId
    );
    List<UserDTO> members = groupService.getGroupMembers(groupId);
    return ResponseEntity.ok(members);
  }

  @PostMapping("/groups/{groupId}/members")
  @Override
  public ResponseEntity<Void> addMember(
    @PathVariable("groupId") Long groupId,
    @Valid @RequestBody AddMemberRequest addMemberRequest
  ) {
    LOGGER.info(
      "Add member request received for groupId {} and userId {}",
      groupId,
      addMemberRequest.getUserId()
    );
    groupService.addMember(groupId, addMemberRequest.getUserId());
    return new ResponseEntity<>(HttpStatus.NO_CONTENT);
  }

  @DeleteMapping("/groups/{groupId}/members/{userId}")
  public ResponseEntity<Void> removeMember(
    @PathVariable("groupId") Long groupId,
    @PathVariable("userId") Long userId
  ) {
    LOGGER.info(
      "Remove member request received for groupId {} and userId {}",
      groupId,
      userId
    );
    groupService.removeMember(groupId, userId);
    return new ResponseEntity<>(HttpStatus.NO_CONTENT);
  }
}
