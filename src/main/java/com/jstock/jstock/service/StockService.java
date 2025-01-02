package com.jstock.jstock.service;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.jstock.jstock.entity.Stock;
import com.jstock.jstock.repository.StockRepo;
import com.jstock.jstock.util.Response;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class StockService {

  private final StockRepo stockRepo;

  public ResponseEntity<Response<Stock>> createStock(Stock stock) {
    String foundStock = stockRepo.findByStockName(stock.getName()).orElse(null);
    if (foundStock != null) {
      // System.out.println("Stock already exists");
      return Response.sendErrorResponse("Stock name already there", 400);
    }
    Stock savedStock = stockRepo.save(stock);
    ;
    return Response.sendSuccessResponse("Stock created successfully", savedStock);
  }

  public List<Stock> getAllStocks() {
    List<Stock> allStocks = stockRepo.findAll();
    return allStocks;
  }

  public void updateProductPrice(String symbol, Double price) {
    Stock foundStock = stockRepo.findStockBySymbolName(symbol).orElse(null);
    if (foundStock == null) {
      return;
    }
    foundStock.setPrice(price);
    stockRepo.save(foundStock);
    return;
  }

}
