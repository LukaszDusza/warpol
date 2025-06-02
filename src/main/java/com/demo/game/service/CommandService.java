package com.demo.game.service;

import com.demo.game.commands.Command;
import com.demo.game.commands.MoveCommand;
import com.demo.game.commands.ShootCommand;
import com.demo.game.objects.dao.Archer;
import com.demo.game.objects.dao.Game;
import com.demo.game.objects.dao.GameEvent;
import com.demo.game.objects.dao.Transport;
import com.demo.game.objects.dao.Unit;
import com.demo.game.objects.enums.CommandType;
import com.demo.game.objects.enums.UnitStatus;
import com.demo.game.repositories.GameEventRepository;
import com.demo.game.repositories.UnitRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import java.time.Clock;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.crossstore.ChangeSetPersister.NotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CommandService {

  private final UnitRepository unitRepo;
  private final GameEventRepository eventRepo;
  private final ObjectMapper objectMapper;
  private final Clock clock;

  @Transactional
  public void executeCommand(Long unitId, Command cmd) throws NotFoundException {

    Unit unit = unitRepo.findWithLockById(unitId)
        .orElseThrow(NotFoundException::new);

    /* --- delegacja do specyficznych implementacji --- */
    switch (cmd) {
      case MoveCommand mv -> applyMove(unit, mv);
      case ShootCommand sh -> applyShot(unit, sh);
      default -> throw new IllegalStateException("Unsupported command: " + cmd);
    }

    /* --- aktualizacja wspólnych pól na jednostce --- */
    unit.setLastCommandAt(clock.instant());
    unit.setCommandCount(unit.getCommandCount() + 1);

    /* --- zapis do historii --- */
    String payload;
    try {
      payload = objectMapper.writeValueAsString(cmd);
    } catch (JsonProcessingException e) {
      throw new IllegalStateException("Cannot serialize command", e);
    }

    GameEvent event = new GameEvent(
        unit.getGame(),
        unit,
        cmd.type(),        // CommandType
        payload,           // JSON
        true,              // success
        clock.instant()
    );
    eventRepo.save(event);
  }


  /**
   * Wykonuje ruch jednostki.
   * <p>
   * Reguły: • Łucznik     – zawsze dokładnie 1 pole orthogonalnie.
   * • Transport   – 1-3 pól orthogonalnie; niszczy przeciwnika, jeżeli stanie na jego polu. • Własnej jednostki nie wolno „najechać” –
   * wtedy ruch jest anulowany, a cooldown mimo to liczy się normalnie.
   * • Nie wolno wyjść poza planszę ani „przeskoczyć” nad żywą jednostką.
   */
  private void applyMove(Unit unit, MoveCommand cmd) {

    /* --- walidacja zgodności typu rozkazu z typem jednostki --- */
    if (unit instanceof Archer && cmd.type() != CommandType.MOVE_ARCHER) {
      throw new IllegalArgumentException("Archer can execute only MOVE_ARCHER");
    }
    if (unit instanceof Transport && cmd.type() != CommandType.MOVE_TRANSPORT) {
      throw new IllegalArgumentException("Transport can execute only MOVE_TRANSPORT");
    }

    /* --- dodatkowe limity kroków --- */
    if (unit instanceof Archer && cmd.steps() != 1) {
      throw new IllegalArgumentException("Archer moves exactly 1 square");
    }
    if (unit instanceof Transport && (cmd.steps() < 1 || cmd.steps() > 3)) {
      throw new IllegalArgumentException("Transport moves 1-3 squares");
    }

    Game game = unit.getGame();
    int width = game.getBoardWidth();
    int height = game.getBoardHeight();

    /* Kierunek jako wektor (dx,dy) długości 1 */
    int dx = switch (cmd.direction()) {
      case LEFT -> -1;
      case RIGHT -> 1;
      default -> 0;
    };
    int dy = switch (cmd.direction()) {
      case UP -> -1;
      case DOWN -> 1;
      default -> 0;
    };

    /* Bierzemy kolejne pola po drodze, aby:
       • nie „przeskakiwać” innych jednostek,
       • rozstrzygnąć kolizję z ostatnim polem. */
    int x = unit.getX();
    int y = unit.getY();

    for (int step = 1; step <= cmd.steps(); step++) {

      x += dx;
      y += dy;

      // wyjście poza planszę -> błąd
      if (x < 0 || x >= width || y < 0 || y >= height) {
        throw new IllegalArgumentException("Move goes outside the board");
      }

      /* Czy pole zajęte przez żywą jednostkę? */
      Optional<Unit> occupant =
          unitRepo.findFirstByGameAndXAndYAndStatus(game, x, y, UnitStatus.ACTIVE);

      if (occupant.isPresent()) {

        Unit other = occupant.get();

        // w trakcie marszu (nie ostatni krok) – blokada
        if (step < cmd.steps()) {
          throw new IllegalArgumentException("Path blocked by another unit at (" + x + "," + y + ")");
        }

        /* Ostatnie pole – dwa scenariusze */
        if (other.getFaction() == unit.getFaction()) {
          /* Próba najazdu na swoją jednostkę: ruch anulowany, ale cooldown już „poszedł”. */
          return;     // pozostajemy na miejscu
        } else {
          /* Najazd na przeciwnika – przeciwnik ginie, transport zajmuje pole. */
          other.setStatus(UnitStatus.DESTROYED);
          unit.setX(x);
          unit.setY(y);
          return;
        }
      }
    }

    /* zwykły ruch na puste pole. */
    unit.setX(x);
    unit.setY(y);
  }


  /**
   * Wykonuje strzał jednostki.
   * <p>
   * Reguły: • Archer strzela orthogonalnie (walidacja już w ShootCommand).
   * • Cannon może także po skosie. • Jeśli w polu docelowym znajduje się dowolna żywa jednostka (własna lub cudza) – zostaje
   * zniszczona. • Brak „przeszkód po drodze” – pocisk leci nad pustymi polami.
   */
  private void applyShot(Unit unit, ShootCommand cmd) {

    Game game = unit.getGame();
    int width = game.getBoardWidth();
    int height = game.getBoardHeight();

    int targetX = cmd.targetX(unit.getX());
    int targetY = cmd.targetY(unit.getY());

    // wyjście poza planszę -> błąd
    if (targetX < 0 || targetX >= width || targetY < 0 || targetY >= height) {
      throw new IllegalArgumentException("Shot goes outside the board");
    }

    /* Znajdź żywą jednostkę w polu trafienia */
    unitRepo.findFirstByGameAndXAndYAndStatus(game, targetX, targetY, UnitStatus.ACTIVE)
        .ifPresent(hit -> hit.setStatus(UnitStatus.DESTROYED));
  }

}
