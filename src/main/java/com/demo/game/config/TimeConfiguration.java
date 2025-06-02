package com.demo.game.config;

import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TimeConfiguration {

  /**
   * Systemowy zegar w UTC
   */
  @Bean
  public Clock clock() {
    return Clock.systemUTC();
  }
}