package com.demo.game.objects.dao;

import com.demo.game.commands.Command;
import com.demo.game.objects.enums.CommandType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import java.time.Instant;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Entity
public class GameEvent {

  @Id
  @GeneratedValue
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  private Game game;

  @ManyToOne(fetch = FetchType.LAZY)
  private Unit unit;

  @Enumerated(EnumType.STRING)
  private CommandType type;

  @Lob
  private String payload;

  /**
   * Czy operacja zakończyła się sukcesem (np. nie najechaliśmy na własną jednostkę)
   */
  private boolean success;

  private Instant executedAt;

  public GameEvent(Game game, Unit unit, Command cmd, boolean b, Instant instant) {

  }

  public GameEvent(Game game, Unit unit, CommandType type, String payload, boolean b, Instant instant) {
  }
}
