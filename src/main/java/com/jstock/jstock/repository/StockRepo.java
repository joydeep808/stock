package com.jstock.jstock.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.jstock.jstock.entity.Stock;

import io.lettuce.core.dynamic.annotation.Param;

@Repository
public interface StockRepo extends JpaRepository<Stock, String> {

  @Query(value = "SELECT name from stock where name = :stockName OR symbol = :stockName", nativeQuery = true)
  Optional<String> findByStockName(@Param("stockName") String stockName);

  @Query(value = "SELECT * from stock where symbol = :symbol OR name = :symbol", nativeQuery = true)
  Optional<Stock> findStockBySymbolName(@Param("symbol") String symbol);
}
