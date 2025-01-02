package com.jstock.jstock.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.jstock.jstock.entity.MarketData;

@Repository
public interface MarketHistoryRepo extends JpaRepository<MarketData, String> {

}
