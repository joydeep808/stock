package com.jstock.jstock.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.jstock.jstock.entity.Order;

import io.lettuce.core.dynamic.annotation.Param;

@Repository
public interface OrderRepo extends JpaRepository<Order, String> {

  @Query(value = "SELECT * from orders where user_id = :userId", nativeQuery = true)
  Page<Order> findAllByUserId(@Param("userId") Long userId, Pageable pageable);
}