package com.jstock.jstock.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.jstock.jstock.entity.MarketData;

@Repository
public interface MarketDataRepo extends JpaRepository<MarketData, UUID> {

}