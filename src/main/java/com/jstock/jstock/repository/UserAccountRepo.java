package com.jstock.jstock.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.jstock.jstock.entity.UserAccount;

@Repository
public interface UserAccountRepo extends JpaRepository<UserAccount, Long> {

  @Query(value = "SELECT * from user_account where user_id = :userId", nativeQuery = true)
  Optional<UserAccount> findByUserId(Long userId);

}