package com.jstock.jstock.service.redis;

import java.time.Duration;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RedisService {
  private final RedisTemplate<String, Object> redisTemplate;
  private final ObjectMapper objectMapper;

  public <T> T get(String key, Class<T> c) {
    Object value = redisTemplate.opsForValue().get(key);
    if (value == null) {
      return null;
    }
    return objectMapper.convertValue(value, c);
  }

  public <T> boolean set(String key, T value, Integer expiration) {
    try {
      redisTemplate.opsForValue().set(key, value, Duration.ofSeconds(expiration));
      return true;
    } catch (Exception e) {
      return false;
    }
  }

  public <T> boolean set(String key, T value) {
    try {
      redisTemplate.opsForValue().set(key, value);
      return true;
    } catch (Exception e) {
      System.out.println(e.getLocalizedMessage());
      return false;
    }
  }

  public <T> boolean update(String key, T value) {
    return redisTemplate.opsForValue().setIfPresent(key, value);
  }

  // public boolean decrementBalance(String key , ) {
  // try {
  // redisTemplate.opsForValue().decrement(key ,);
  // } catch (Exception e) {
  // // TODO: handle exception
  // }

  // }

}
