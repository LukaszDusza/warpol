package com.demo.game.objects.dao;


import com.demo.game.objects.enums.CommandType;
import com.demo.game.objects.enums.Faction;
import com.demo.game.objects.enums.UnitStatus;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Version;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "unit_type")
public abstract class Unit {

  @Id
  @GeneratedValue
  private Long id;

  /**
   * Do której gry należy jednostka.
   */
  @ManyToOne(fetch = FetchType.LAZY)
  private Game game;

  /**
   * Kolor gracza: WHITE / BLACK.
   */
  @Enumerated(EnumType.STRING)
  private Faction faction;

  /**
   * Bieżąca pozycja na planszy.
   */
  @Column(nullable = false, columnDefinition = "int default 0")
  private int x = 0;

  @Column(nullable = false, columnDefinition = "int default 0")
  private int y = 0;

  /**
   * Czy żyje.
   */
  @Enumerated(EnumType.STRING)
  private UnitStatus status = UnitStatus.ACTIVE;

  /**
   * Ile rozkazów wykonała od początku gry.
   */
  @Column(nullable = false, columnDefinition = "int default 0")
  private int commandCount = 0;

  /**
   * Kiedy ostatnio wykonano na niej rozkaz – do throttlingu czasowego.
   */
  private Instant lastCommandAt = Instant.EPOCH;

  /**
   * Optimistic-locking, niezbędny przy równoległych żądaniach.
   */
  @Version
  @Column(nullable = false, columnDefinition = "bigint default 0")
  private long version;

  /* --- metody pomocnicze --- */
  public boolean canExecute(CommandType type, Clock clock) {
    Duration minGap = type.getCooldown();
    return Duration.between(lastCommandAt, clock.instant()).compareTo(minGap) >= 0;
  }

}
