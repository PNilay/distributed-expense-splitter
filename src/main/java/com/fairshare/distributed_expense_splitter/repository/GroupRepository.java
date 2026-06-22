package com.fairshare.distributed_expense_splitter.repository;

import com.fairshare.distributed_expense_splitter.entity.Group;

import java.util.List;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface GroupRepository extends JpaRepository<Group, Long> {

    @Query("SELECT DISTINCT m.id FROM Group g JOIN g.members m WHERE g.id IN " +
            "(SELECT g2.id FROM Group g2 JOIN g2.members m2 WHERE m2.id = :userId)")
    List<Long> findCoMemberIds(@Param("userId") Long userId);

}
