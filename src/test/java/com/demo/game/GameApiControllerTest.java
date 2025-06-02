package com.demo.game;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.demo.game.commands.MoveCommand;
import com.demo.game.controllers.GameApiController;
import com.demo.game.objects.dao.Game;
import com.demo.game.objects.dto.MoveRequest;
import com.demo.game.objects.enums.CommandType;
import com.demo.game.objects.enums.Direction;
import com.demo.game.objects.enums.Faction;
import com.demo.game.objects.enums.GameStatus;
import com.demo.game.service.CommandService;
import com.demo.game.service.GameService;
import com.demo.game.service.RandomCommandGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;


@WebMvcTest(GameApiController.class)
@AutoConfigureMockMvc
class GameApiControllerTest {

  @Autowired
  MockMvc mockMvc;

  @Autowired
  ObjectMapper mapper;

  @MockitoBean
  GameService gameService;

  @MockitoBean
  CommandService commandService;

  @MockitoBean
  RandomCommandGenerator randomCommandGenerator;

  /* ---------------------------------------------------------
     1. POST /api/games
     --------------------------------------------------------- */
  @Test
  @DisplayName("createNewGame() zwraca 201 i deleguje do GameService")
  void shouldCreateNewGame() throws Exception {
    // language=json
    Game saved = new Game();
    saved.setId(1L);
    saved.setBoardWidth(10);
    saved.setBoardHeight(8);
    saved.setStatus(GameStatus.NEW);

    given(gameService.createNewGame(anyInt(), anyInt(), any(), any()))
        .willReturn(saved);
  }

  /* ---------------------------------------------------------
     2. GET /api/games/{gameId}/units
     --------------------------------------------------------- */
  @Test
  @DisplayName("listUnits() zwraca 200 i deleguje do GameService")
  void shouldListUnits() throws Exception {
    long gameId = 42L;

    given(gameService.listUnits(eq(gameId), eq(Faction.WHITE)))
        .willReturn(java.util.List.of()); // pusta lista wystarcza

    mockMvc.perform(get("/api/games/{id}/units", gameId)
            .param("faction", "WHITE"))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON));

    verify(gameService).listUnits(gameId, Faction.WHITE);
  }

  /* ---------------------------------------------------------
     3. POST /api/units/{unitId}/command
     --------------------------------------------------------- */
  @Test
  @DisplayName("sendCommand() zwraca 202 i deleguje do CommandService")
  void shouldSendCommand() throws Exception {
    long unitId = 7L;
    MoveRequest req = new MoveRequest(Direction.RIGHT, 1, Faction.WHITE);
    String json = mapper.writeValueAsString(req);

    mockMvc.perform(post("/api/units/{unitId}/command", unitId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(json))
        .andExpect(status().isAccepted());

    verify(commandService).executeCommand(eq(unitId), any());
  }

  /* ---------------------------------------------------------
     4. POST /api/units/{unitId}/random
     --------------------------------------------------------- */
  @Test
  @DisplayName("randomCommand() zwraca 202, generuje i wykonuje komendę")
  void shouldSendRandomCommand() throws Exception {
    long unitId = 15L;

    given(randomCommandGenerator.generate(unitId, Faction.BLACK))
        .willReturn(new MoveCommand(Direction.DOWN, 1, Faction.BLACK,
            CommandType.MOVE_TRANSPORT));

    mockMvc.perform(post("/api/units/{unitId}/random", unitId)
            .param("issuer", "BLACK"))
        .andExpect(status().isAccepted());

    verify(randomCommandGenerator).generate(unitId, Faction.BLACK);
    verify(commandService).executeCommand(eq(unitId), any());
  }
}