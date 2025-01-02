package com.jstock.jstock.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.jstock.jstock.entity.User;
import com.jstock.jstock.mapper.user.UserMapperWithBalance;

@Repository
public interface UserRepo extends JpaRepository<User, Long> {

  @Query(value = "SELECT * from users where email = :email", nativeQuery = true)
  Optional<User> findUserByEmail(String email);

  @Query(value = "SELECT u.id, u.name, u.email, ua.balance from users u inner join user_account ua on ua.user_id = u.id where u.email = :email", nativeQuery = true)
  Optional<UserMapperWithBalance> findUserWithBalanceByEmail(String email);

}