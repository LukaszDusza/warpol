package com.demo.game.objects.enums;

public enum UnitStatus {

  /** Jednostka jest sprawna. */
  ACTIVE(true),

  /** Jednostka została wyeliminowana. */
  DESTROYED(false);

  private final boolean alive;

  UnitStatus(boolean alive) {
    this.alive = alive;
  }

  /** @return {@code true} jeśli jednostka pozostaje aktywna (niezniszczona). */
  public boolean isAlive() {
    return alive;
  }
}