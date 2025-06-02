package com.demo.game.objects.dto;

import com.demo.game.objects.dao.Unit;
import com.demo.game.objects.enums.Faction;

public record UnitDto(long id, Faction faction, String type, int x, int y, String status, int commands) {

  public static UnitDto from(Unit u) {
    return new UnitDto(
        u.getId(),
        u.getFaction(),
        u.getClass().getSimpleName().toUpperCase(),
        u.getX(),
        u.getY(),
        u.getStatus().name(),
        u.getCommandCount()
    );
  }
}
