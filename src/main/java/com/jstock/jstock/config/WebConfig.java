package com.jstock.jstock.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

  @Override
  public void addCorsMappings(CorsRegistry registry) {
    // Allow CORS requests from localhost:5173 (your frontend) and allow credentials
    registry.addMapping("/**") // Apply to all endpoints
        .allowedOrigins("*") // Your frontend URL
        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // Allowed HTTP methods
        .allowCredentials(true) // Allow credentials (cookies, authorization headers, etc.)
        .allowedHeaders("*"); // Allow all headers (you can specify if needed)
  }
}