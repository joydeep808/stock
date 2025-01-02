package com.jstock.jstock.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.jstock.jstock.entity.UserBalanceTransaction;

@Repository
public interface UserBalanceTransactionRepo extends JpaRepository<UserBalanceTransaction, Long> {

}