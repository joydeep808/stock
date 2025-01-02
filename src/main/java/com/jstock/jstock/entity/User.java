package com.jstock.jstock.entity;

import java.time.ZonedDateTime;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Entity
@AllArgsConstructor
@Table(name = "users")
@Data
public class User {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  @NotNull(message = "Name is required")
  private String name;
  @NotNull(message = "Email is required")
  private String email;
  private String password;
  private Role role = Role.USER;
  private Long createdAt;
  private Long lastLogin;

  public enum Role {
    USER,
    ADMIN
  }

  public User() {
    this.createdAt = ZonedDateTime.now().toInstant().toEpochMilli();
  }

  public User(String name, String email, String password) {
    this.name = name;
    this.email = email;
    this.password = password;
    this.createdAt = ZonedDateTime.now().toInstant().toEpochMilli();
  }
}