package com.demo.game.service;


import com.demo.game.objects.dao.Archer;
import com.demo.game.objects.dao.Cannon;
import com.demo.game.objects.dao.Game;
import com.demo.game.objects.dao.Transport;
import com.demo.game.objects.dao.Unit;
import com.demo.game.objects.dto.PlayerConfig;
import com.demo.game.objects.enums.Faction;
import com.demo.game.objects.enums.GameStatus;
import com.demo.game.objects.enums.UnitStatus;
import com.demo.game.repositories.GameRepository;
import com.demo.game.repositories.UnitRepository;
import jakarta.transaction.Transactional;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GameService {

  private final GameRepository gameRepo;
  private final UnitRepository unitRepo;

  private final SecureRandom rng = new SecureRandom();

  /* ============================================================= */
  /*  Utworzenie nowej gry – usuwa/starą oznacza ABORTED           */
  /* ============================================================= */

  @Transactional
  public Game createNewGame(int width, int height,
      PlayerConfig whiteCfg,
      PlayerConfig blackCfg) {

    // 1) zakończ wszystkie aktywne gry (uproszczone: ABORTED)
    gameRepo.findFirstByStatusOrderByStartedAtDesc(GameStatus.ACTIVE)
        .ifPresent(g -> {
          g.setStatus(GameStatus.ABORTED);
          gameRepo.save(g);
        });

    // 2) tworzymy nową grę
    Game game = new Game();
    game.setBoardWidth(width);
    game.setBoardHeight(height);
    game.setStartedAt(Instant.now());
    game.setStatus(GameStatus.ACTIVE);
    game = gameRepo.save(game);

    // 3) rozmieść jednostki losowo
    placeUnits(game, Faction.WHITE, whiteCfg);
    placeUnits(game, Faction.BLACK, blackCfg);

    return game;
  }

  private void placeUnits(Game game, Faction faction, PlayerConfig cfg) {
    int total = cfg.archers() + cfg.transports() + cfg.cannons();
    int capacity = game.getBoardWidth() * game.getBoardHeight();
    if (total > capacity) {
      throw new IllegalArgumentException("Board too small for requested units");
    }

    Set<String> occupied = new HashSet<>();

    // helper na szukanie pustych pól
    java.util.function.Supplier<int[]> freeCoord = () -> {
      int x, y;
      do {
        x = rng.nextInt(game.getBoardWidth());
        y = rng.nextInt(game.getBoardHeight());
      } while (!occupied.add(x + ":" + y));
      return new int[]{x, y};
    };

    List<Unit> batch = new ArrayList<>();

    // archers
    for (int i = 0; i < cfg.archers(); i++) {
      int[] pos = freeCoord.get();
      Archer a = new Archer();
      initialiseUnit(a, game, faction, pos[0], pos[1]);
      batch.add(a);
    }
    // transports
    for (int i = 0; i < cfg.transports(); i++) {
      int[] pos = freeCoord.get();
      Transport t = new Transport();
      initialiseUnit(t, game, faction, pos[0], pos[1]);
      batch.add(t);
    }
    // cannons
    for (int i = 0; i < cfg.cannons(); i++) {
      int[] pos = freeCoord.get();
      Cannon c = new Cannon();
      initialiseUnit(c, game, faction, pos[0], pos[1]);
      batch.add(c);
    }

    unitRepo.saveAll(batch);
  }

  private void initialiseUnit(Unit u, Game game, Faction faction, int x, int y) {
    u.setGame(game);
    u.setFaction(faction);
    u.setX(x);
    u.setY(y);
    u.setStatus(UnitStatus.ACTIVE);
    u.setCommandCount(0);
    u.setLastCommandAt(Instant.EPOCH);
  }

  /* ============================================================= */
  /*  Lista jednostek frakcji                                     */
  /* ============================================================= */

  @Transactional
  public List<Unit> listUnits(long gameId, Faction faction) {
    Game game = gameRepo.findById(gameId)
        .orElseThrow(() -> new NoSuchElementException("Game " + gameId + " not found"));
    return unitRepo.findByGameAndFaction(game, faction);
  }
}
