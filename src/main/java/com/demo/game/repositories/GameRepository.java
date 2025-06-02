package com.demo.game.repositories;

import com.demo.game.objects.dao.Game;
import com.demo.game.objects.enums.GameStatus;
import jakarta.persistence.LockModeType;
import jakarta.transaction.Transactional;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface GameRepository extends JpaRepository<Game, Long> {

  /**
   * Ostatnia gra w danym stanie (np. ACTIVE lub NEW).
   */
  Optional<Game> findFirstByStatusOrderByStartedAtDesc(GameStatus status);

  /**
   * Pobierz wraz z blokadą do zapisu – przy krytycznych zmianach (np. reset gry).
   */
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select g from Game g where g.id = :id")
  Optional<Game> findWithLockById(@Param("id") Long id);

  /**
   * Ustaw jeden status dla wszystkich gier spełniających warunek (np. zamknij stare).
   */
  @Modifying
  @Transactional
  @Query("""
      update Game g
         set g.status = :newStatus
       where g.status = :onlyStatus
         and g.id <> :skipGameId
      """)
  int bulkUpdateStatus(@Param("newStatus") GameStatus newStatus,
      @Param("onlyStatus") GameStatus onlyStatus,
      @Param("skipGameId") Long skipGameId);
}

