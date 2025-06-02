package com.demo.game.commands;

import com.demo.game.objects.enums.CommandType;
import com.demo.game.objects.enums.Faction;

/**
 * Komenda strzału: • Łucznik – kierunek + dystans (dx=0 albo dy=0). • Armata   – dx,dy mogą być jednocześnie ≠0 (strzał po skosie).
 *
 * @param dx     przesunięcie w osi X (ujemne = w lewo, dodatnie = w prawo)
 * @param dy     przesunięcie w osi Y (ujemne = w górę,  dodatnie = w dół)
 * @param issuer kto wydaje rozkaz
 * @param type   SHOOT_ARCHER albo SHOOT_CANNON
 */
public record ShootCommand(int dx,
                           int dy,
                           Faction issuer,
                           CommandType type) implements Command {

  public ShootCommand {
    if (type != CommandType.SHOOT_ARCHER && type != CommandType.SHOOT_CANNON) {
      throw new IllegalArgumentException("Wrong CommandType for ShootCommand");
    }
    if (type == CommandType.SHOOT_ARCHER && dx != 0 && dy != 0) {
      throw new IllegalArgumentException("Archer shoots only orthogonally");
    }
    if (dx == 0 && dy == 0) {
      throw new IllegalArgumentException("dx and dy cannot both be zero");
    }
  }

  public int targetX(int startX) {
    return startX + dx;
  }

  public int targetY(int startY) {
    return startY + dy;
  }

  /**
   * Dystans „po prostej” – przydatne do limitów zasięgu.
   */
  public int distance() {
    return Math.max(Math.abs(dx), Math.abs(dy));
  }
}
