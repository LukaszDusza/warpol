package com.demo.game.objects.enums;

/**
 * Stan całej gry/sesji.
 *
 * NEW– gra została utworzona, ale nie wystartowała (plansza jeszcze niezalosowana
 *              lub żaden gracz nie wydał komendy).
 * ACTIVE– rozgrywka trwa; gracze mogą wydawać polecenia.
 * FINISHED– rozgrywka zakończona normalnie (któryś z graczy wygrał /
 *              spełniono warunek końca).
 * ABORTED– gra przerwana przed końcem (np. reset przez “Nowa gra” lub błąd krytyczny).
 */
public enum GameStatus {
  NEW,
  ACTIVE,
  FINISHED,
  ABORTED;

  /** Czy można w tym stanie przyjmować komendy od graczy? */
  public boolean isAcceptingCommands() {
    return this == ACTIVE;
  }

  /** Czy gra znajduje się w stanie końcowym (nieaktywna)? */
  public boolean isTerminal() {
    return this == FINISHED || this == ABORTED;
  }
}
