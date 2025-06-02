package com.demo.game.objects.dao;


import com.demo.game.objects.enums.GameStatus;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
public class Game {

  @Id
  @GeneratedValue
  private Long id;

  @Column(nullable = false, columnDefinition = "int default 8")
  private int boardWidth  = 8;
  @Column(nullable = false, columnDefinition = "int default 8")
  private int boardHeight = 8;
  private Instant startedAt = Instant.now();

  @Enumerated(EnumType.STRING)
  private GameStatus status = GameStatus.ACTIVE;

  @OneToMany(mappedBy = "game", cascade = CascadeType.ALL)
  private List<Unit> units = new ArrayList<>();

  @OneToMany(mappedBy = "game", cascade = CascadeType.ALL)
  private List<GameEvent> events = new ArrayList<>();
}
