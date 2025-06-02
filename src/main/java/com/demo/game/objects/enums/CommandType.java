package com.demo.game.objects.enums;


import java.time.Duration;
import lombok.Getter;

@Getter
public enum CommandType {

  MOVE_ARCHER(Duration.ofSeconds(5)),
  MOVE_TRANSPORT(Duration.ofSeconds(7)),
  SHOOT_ARCHER(Duration.ofSeconds(10)),
  SHOOT_CANNON(Duration.ofSeconds(13));

  private final Duration cooldown;

  CommandType(Duration cd) {
    this.cooldown = cd;
  }

}
