package com.fairshare.distributed_expense_splitter.controller;

import com.fairshare.distributed_expense_splitter.entity.Expense;
import com.fairshare.distributed_expense_splitter.service.GroupService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openapitools.api.GroupsApi;
import org.openapitools.model.AddMemberRequest;
import org.openapitools.model.CreateGroupRequest;
import org.openapitools.model.ExpenseDTO;
import org.openapitools.model.ExpenseListDTO;
import org.openapitools.model.GroupDTO;
import org.openapitools.model.GroupSummaryDTO;
import org.openapitools.model.GroupBalanceResponse;
import org.openapitools.model.OptimizedTransactionDTO;
import org.openapitools.model.UserDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/v1/api")
public class GroupController implements GroupsApi {

  private final GroupService groupService;

  public GroupController(GroupService groupService) {
    this.groupService = groupService;
  }

  private static final Logger LOGGER = LogManager.getLogger(
      GroupController.class);

  @Override
  @PostMapping("/groups")
  public ResponseEntity<GroupDTO> createGroup(
      @Valid @RequestBody CreateGroupRequest req) {
    LOGGER.info(
        "Group creation request received for group name {}",
        req.getName());
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
      @PathVariable("groupId") Long groupId) {
    LOGGER.info("Group retrieval request received for groupId {}", groupId);
    GroupDTO dto = groupService.getGroup(groupId);
    return ResponseEntity.ok(dto);
  }

  @PutMapping("/groups/{groupId}")
  @Override
  public ResponseEntity<GroupDTO> updateGroup(
      @PathVariable("groupId") Long groupId,
      @Valid @RequestBody CreateGroupRequest req) {
    LOGGER.info("Group update request received for groupId {}", groupId);
    GroupDTO dto = groupService.updateGroup(groupId, req);
    return ResponseEntity.ok(dto);
  }

  @DeleteMapping("/groups/{groupId}")
  @Override
  public ResponseEntity<Void> deleteGroup(
      @PathVariable("groupId") Long groupId) {
    LOGGER.info("Group deletion request received for groupId {}", groupId);
    groupService.deleteGroup(groupId);
    return new ResponseEntity<>(HttpStatus.NO_CONTENT);
  }

  @GetMapping("/groups/{groupId}/members")
  @Override
  public ResponseEntity<List<UserDTO>> getGroupMembers(
      @PathVariable("groupId") Long groupId) {
    LOGGER.info(
        "Group members retrieval request received for groupId {}",
        groupId);
    List<UserDTO> members = groupService.getGroupMembers(groupId);
    return ResponseEntity.ok(members);
  }

  @PostMapping("/groups/{groupId}/members")
  @Override
  public ResponseEntity<Void> addMember(
      @PathVariable("groupId") Long groupId,
      @Valid @RequestBody AddMemberRequest addMemberRequest) {
    LOGGER.info(
        "Add member request received for groupId {} and userId {}",
        groupId,
        addMemberRequest.getUserId());
    groupService.addMember(groupId, addMemberRequest.getUserId());
    return new ResponseEntity<>(HttpStatus.NO_CONTENT);
  }

  @DeleteMapping("/groups/{groupId}/members/{userId}")
  public ResponseEntity<Void> removeMember(
      @PathVariable("groupId") Long groupId,
      @PathVariable("userId") Long userId) {
    LOGGER.info(
        "Remove member request received for groupId {} and userId {}",
        groupId,
        userId);
    groupService.removeMember(groupId, userId);
    return new ResponseEntity<>(HttpStatus.NO_CONTENT);
  }

  @Override
  @GetMapping("/groups/{groupId}/summary")
  public ResponseEntity<GroupSummaryDTO> groupsGroupIdSummaryGet(
      @PathVariable("groupId") Integer groupId) {
    LOGGER.info("Group summary request received for groupId {}", groupId);
    Long currentUserId = extractCurrentUserId();
    GroupSummaryDTO dto = groupService.getGroupSummary(groupId.longValue(), currentUserId);
    return ResponseEntity.ok(dto);
  }

  @Override
  @GetMapping("/groups/summaries")
  public ResponseEntity<List<GroupSummaryDTO>> groupsSummariesGet() {
    LOGGER.info("Group summaries request received");
    Long currentUserId = extractCurrentUserId();
    List<GroupSummaryDTO> list = groupService.getGroupSummaries(currentUserId);
    return ResponseEntity.ok(list);
  }

  @GetMapping("/groups/{groupId}/balances")
  public ResponseEntity<GroupBalanceResponse> getGroupBalances(@PathVariable("groupId") Long groupId) {
    LOGGER.info("Group balances request for groupId {}", groupId);
    GroupBalanceResponse resp = groupService.getGroupBalances(groupId);
    return ResponseEntity.ok(resp);
  }

  @GetMapping("/groups/{groupId}/simplified-debts")
  public ResponseEntity<java.util.List<OptimizedTransactionDTO>> getSimplifiedDebts(
      @PathVariable("groupId") Long groupId) {
    LOGGER.info("Simplified debts request for groupId {}", groupId);
    java.util.List<OptimizedTransactionDTO> list = groupService.getSimplifiedDebts(groupId);
    return ResponseEntity.ok(list);
  }

  @Override
  @GetMapping("/groups/{groupId}/expenses")
  public ResponseEntity<ExpenseListDTO> getGroupExpenses(
      @PathVariable("groupId") Long groupId,
      @RequestParam(value = "limit", defaultValue = "20") Integer limit,
      @RequestParam(value = "before_id", required = false) Long beforeId,
      @RequestParam(value = "after_id", required = false) Long afterId) {
    LOGGER.info("getGroupExpense:");
    ExpenseListDTO expenses = groupService.getGroupExpenses(groupId, limit,
        beforeId, afterId);
    return ResponseEntity.ok(expenses);
  }

  private Long extractCurrentUserId() {
    ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
    if (attrs == null) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized");
    }
    HttpServletRequest request = attrs.getRequest();
    String headerValue = request.getHeader("X-User-Id");
    if (headerValue == null || headerValue.isBlank()) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing X-User-Id header");
    }
    try {
      return Long.valueOf(headerValue);
    } catch (NumberFormatException ex) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid X-User-Id header value");
    }
  }
}
