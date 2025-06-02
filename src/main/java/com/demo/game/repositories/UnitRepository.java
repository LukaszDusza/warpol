package com.demo.game.repositories;

import com.demo.game.objects.dao.Game;
import com.demo.game.objects.dao.Unit;
import com.demo.game.objects.enums.Faction;
import com.demo.game.objects.enums.UnitStatus;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UnitRepository extends JpaRepository<Unit, Long> {


  /**
   * Wszystkie AKTYWNE jednostki w danej grze.
   */
  List<Unit> findByGameAndStatus(Game game, UnitStatus status);

  /**
   * Aktywne jednostki podanego gracza/frakcji.
   */
  List<Unit> findByGameAndFactionAndStatus(Game game,
      Faction faction,
      UnitStatus status);

  /**
   * Czy pole (x,y) jest zajęte przez aktywną jednostkę?
   */
  boolean existsByGameAndXAndYAndStatus(Game game, int x, int y, UnitStatus status);

  /**
   * Szybkie zliczenie żywych / zniszczonych jednostek gracza – przydatne do końca gry.
   */
  long countByGameAndFactionAndStatus(Game game,
      Faction faction,
      UnitStatus status);

  /**
   * Pobierz jednostkę z blokadą PESSIMISTIC_WRITE – przed wykonaniem rozkazu.
   */
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select u from Unit u where u.id = :id")
  Optional<Unit> findWithLockById(@Param("id") Long id);


  /**
   * Wszystkie jednostki (żywe + zniszczone) z eager-fetchem gry – do podsumowań.
   */
  @EntityGraph(attributePaths = {"game"})
  List<Unit> findByGame(Game game);

  /**
   * Pierwsza (i jedyna) żywa jednostka na wskazanym polu – zwraca Optional.empty(), gdy pole wolne.
   */
  Optional<Unit> findFirstByGameAndXAndYAndStatus(Game game, int x, int y, UnitStatus status);

  /**
   * Wersja z blokadą pesymistyczną – przydaje się, gdy strzał i ruch mogą celować w to samo pole.
   */
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select u from Unit u where u.game = :game and u.x = :x and u.y = :y and u.status = :status")
  Optional<Unit> findFirstWithLockByGameAndXAndYAndStatus(@Param("game") Game game,
      @Param("x") int x,
      @Param("y") int y,
      @Param("status") UnitStatus status);

  /**
   * Wszystkie (żywe i zniszczone) jednostki danej frakcji w konkretnej grze. Używane przez GameService.doListUnits → /games/{id}/units.
   */
  List<Unit> findByGameAndFaction(Game game, Faction faction);
}

