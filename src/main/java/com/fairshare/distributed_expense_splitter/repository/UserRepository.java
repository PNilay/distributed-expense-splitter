package com.fairshare.distributed_expense_splitter.repository;

import com.fairshare.distributed_expense_splitter.entity.User;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.JpaRepository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
}