package com.demo.game.repositories;

import com.demo.game.objects.dao.Game;
import com.demo.game.objects.dao.GameEvent;
import com.demo.game.objects.dao.Unit;
import java.time.Instant;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface GameEventRepository extends JpaRepository<GameEvent, Long> {


  /**
   * Cała oś czasu danej gry, od najnowszych do najstarszych.
   */
  List<GameEvent> findByGameOrderByExecutedAtDesc(Game game);

  /**
   * Zdarzenia konkretnej jednostki – np. do debugowania AI.
   */
  List<GameEvent> findByUnitOrderByExecutedAtDesc(Unit unit);

  /**
   * Strumień zdarzeń od podanego czasu (np. dla SSE / WebSocket).
   */
  @Query("""
      select e
        from GameEvent e
       where e.game = :game
         and e.executedAt > :since
       order by e.executedAt
      """)
  List<GameEvent> findEventsSince(@Param("game") Game game,
      @Param("since") Instant since);


  /**
   * Ostatni czas wykonania rozkazu na jednostce – pomoc przy cooldownie.
   */
  @Query("select max(e.executedAt) from GameEvent e where e.unit.id = :unitId")
  Instant findLastCommandTimestamp(@Param("unitId") Long unitId);
}
