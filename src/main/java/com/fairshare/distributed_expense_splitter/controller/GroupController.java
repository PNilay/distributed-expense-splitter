package com.fairshare.distributed_expense_splitter.controller;

import com.fairshare.distributed_expense_splitter.service.GroupService;
import org.openapitools.api.GroupsApi;
import org.openapitools.model.AddMemberRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/api")
public class GroupController implements GroupsApi {

  private final GroupService groupService;

  public GroupController(GroupService groupService) {
    this.groupService = groupService;
  }

  @Override
  @PostMapping("/groups/{groupId}/members")
  public ResponseEntity<Void> addMember(@PathVariable("groupId") Long groupId,
      @Valid @RequestBody AddMemberRequest addMemberRequest) {
    try {
      groupService.addMember(groupId, addMemberRequest.getUserId());
      return ResponseEntity.noContent().build();
    } catch (Exception e) {
      return ResponseEntity.status(404).build();
    }
  }
}
