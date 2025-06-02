package com.demo.game.commands;

import com.demo.game.objects.enums.CommandType;
import com.demo.game.objects.enums.Direction;
import com.demo.game.objects.enums.Faction;

/**
 * Komenda ruchu (łucznik – 1 pole; transport – 1-3 pól).
 *
 * @param direction kierunek (UP / DOWN / LEFT / RIGHT)
 * @param steps     ile pól (1-3; walidujemy w CommandService)
 * @param issuer    kto wydaje rozkaz
 * @param type      musi być MOVE_ARCHER lub MOVE_TRANSPORT
 */
public record MoveCommand(Direction direction,
                          int steps,
                          Faction issuer,
                          CommandType type) implements Command {

  public MoveCommand {
    if (type != CommandType.MOVE_ARCHER && type != CommandType.MOVE_TRANSPORT) {
      throw new IllegalArgumentException("Wrong CommandType for MoveCommand");
    }
  }

  /**
   * Oblicz nowe współrzędne na podstawie pozycji startowej.
   */
  public int targetX(int startX) {
    return switch (direction) {
      case LEFT -> startX - steps;
      case RIGHT -> startX + steps;
      default -> startX;
    };
  }

  public int targetY(int startY) {
    return switch (direction) {
      case UP -> startY - steps;
      case DOWN -> startY + steps;
      default -> startY;
    };
  }
}

