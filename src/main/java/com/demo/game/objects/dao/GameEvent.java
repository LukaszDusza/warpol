package com.demo.game.objects.dao;

import com.demo.game.objects.enums.CommandType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import java.time.Instant;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Entity
@Getter
@Setter
public class GameEvent {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(nullable = false)
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

  @Column(nullable = false)
  private Instant executedAt = Instant.now();


  public GameEvent(Game game, Unit unit, CommandType type, String payload, boolean success, Instant executedAt) {
    this.game = game;
    this.unit = unit;
    this.type = type;
    this.payload = payload;
    this.success = success;
    this.executedAt = executedAt;
  }

}
