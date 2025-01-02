package com.jstock.jstock.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.jstock.jstock.entity.Trade;

@Repository
public interface TradeRepo extends JpaRepository<Trade, String> {

  @Query(value = "SELECT * from trade where order_id = :orderId", nativeQuery = true)
  List<Trade> findByOrderId(String orderId);
}