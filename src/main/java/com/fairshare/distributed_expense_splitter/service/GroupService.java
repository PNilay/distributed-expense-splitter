package com.fairshare.distributed_expense_splitter.service;

import com.fairshare.distributed_expense_splitter.entity.Group;
import com.fairshare.distributed_expense_splitter.entity.User;
import com.fairshare.distributed_expense_splitter.repository.GroupRepository;
import com.fairshare.distributed_expense_splitter.repository.UserRepository;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class GroupService {

  @Autowired
  private GroupRepository groupRepository;

  @Autowired
  private UserRepository userRepository;

  public void addMember(Long groupId, Long userId) throws Exception {
    Optional<Group> gOpt = groupRepository.findById(groupId);
    Group group = gOpt.orElseThrow(() -> new Exception("Service.GROUP_NOT_FOUND"));
    Optional<User> uOpt = userRepository.findById(userId);
    User user = uOpt.orElseThrow(() -> new Exception("Service.USER_NOT_FOUND"));
    group.getMembers().add(user);
    groupRepository.save(group); 
  }
}
