package com.fairshare.distributed_expense_splitter.repository;

import com.fairshare.distributed_expense_splitter.entity.Group;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GroupRepository extends JpaRepository<Group, Long> {
}
