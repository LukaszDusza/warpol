package com.demo.game.objects.dto;

import com.demo.game.objects.dao.Game;

public record GameDto(long id, int boardWidth, int boardHeight, String status) {

  public static GameDto from(Game g) {
    return new GameDto(g.getId(), g.getBoardWidth(), g.getBoardHeight(), g.getStatus().name());
  }
}
