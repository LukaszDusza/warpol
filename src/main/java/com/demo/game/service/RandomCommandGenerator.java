package com.demo.game.service;

import com.demo.game.commands.Command;
import com.demo.game.commands.MoveCommand;
import com.demo.game.commands.ShootCommand;
import com.demo.game.objects.dao.*;
import com.demo.game.objects.enums.*;
import com.demo.game.repositories.UnitRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.crossstore.ChangeSetPersister.NotFoundException;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Clock;
import java.util.List;

/**
 * Losowy generator komend – wykorzystywany przez endpoint /units/{id}/random.
 * <p>
 * • Wybiera legalny ruch/strzał biorąc pod uwagę typ jednostki i rozmiar planszy.<br>
 * • Nie sprawdza cooldownu – to robi CommandService.<br>
 * • Zawsze zwraca rozkaz wydany przez właściwą frakcję (issuer == unit.getFaction()).
 */
@Service
@RequiredArgsConstructor
public class RandomCommandGenerator {

  private final UnitRepository unitRepo;
//  private final Clock clock; // feature

  private final SecureRandom rng = new SecureRandom();

  public Command generate(long unitId, Faction issuer) throws NotFoundException {
    Unit unit = unitRepo.findById(unitId)
        .orElseThrow(NotFoundException::new);

    if (unit.getFaction() != issuer) {
      throw new IllegalArgumentException("Issuer faction does not own this unit");
    }

    /* ====== wybór wariantu komendy ====== */
    return switch (unit) {
      case Archer archer -> randomArcherCommand(archer, issuer);
      case Transport transport -> randomTransportCommand(transport, issuer);
      case Cannon cannon -> randomCannonCommand(cannon, issuer);
      default -> throw new IllegalStateException("Unknown unit type: " + unit.getClass());
    };
  }

  /* ---------------------------------------------------------------- */
  /*  ŁUCZNIK                                                         */
  /* ---------------------------------------------------------------- */

  private Command randomArcherCommand(Archer unit, Faction issuer) {
    boolean move = rng.nextBoolean();
    Direction dir = randomDirectionOrthogonal();
    if (move) {
      return new MoveCommand(dir, 1, issuer, CommandType.MOVE_ARCHER);
    } else {
      int distance = rng.nextInt(3) + 1; // 1–3 pól
      int dx = switch (dir) {
        case LEFT -> -distance;
        case RIGHT -> distance;
        default -> 0;
      };
      int dy = switch (dir) {
        case UP -> -distance;
        case DOWN -> distance;
        default -> 0;
      };
      return new ShootCommand(dx, dy, issuer, CommandType.SHOOT_ARCHER);
    }
  }

  /* ---------------------------------------------------------------- */
  /*  TRANSPORT                                                       */
  /* ---------------------------------------------------------------- */

  private Command randomTransportCommand(Transport t, Faction issuer) {
    Direction dir = randomDirectionOrthogonal();
    int steps = rng.nextInt(3) + 1; // 1–3
    return new MoveCommand(dir, steps, issuer, CommandType.MOVE_TRANSPORT);
  }

  /* ---------------------------------------------------------------- */
  /*  ARMATA                                                          */
  /* ---------------------------------------------------------------- */

  private Command randomCannonCommand(Cannon c, Faction issuer) {
    /* losujemy dx i dy z zakresu -3..3 (bez 0,0) */
    int dx, dy;
    do {
      dx = rng.nextInt(7) - 3; // -3..3
      dy = rng.nextInt(7) - 3;
    } while (dx == 0 && dy == 0);
    return new ShootCommand(dx, dy, issuer, CommandType.SHOOT_CANNON);
  }

  /* ---------------------------------------------------------------- */
  /*  Pomocnicze                                                       */
  /* ---------------------------------------------------------------- */

  private static final List<Direction> ORTHO = List.of(Direction.UP, Direction.DOWN, Direction.LEFT, Direction.RIGHT);

  private Direction randomDirectionOrthogonal() {
    return ORTHO.get(rng.nextInt(ORTHO.size()));
  }
}

