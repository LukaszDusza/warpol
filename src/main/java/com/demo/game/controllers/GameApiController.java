package com.demo.game.controllers;

import com.demo.game.commands.Command;
import com.demo.game.objects.dao.Game;
import com.demo.game.objects.dao.Unit;
import com.demo.game.objects.dto.CommandRequest;
import com.demo.game.objects.dto.GameDto;
import com.demo.game.objects.dto.NewGameRequest;
import com.demo.game.objects.dto.UnitDto;
import com.demo.game.objects.enums.Faction;
import com.demo.game.service.CommandService;
import com.demo.game.service.GameService;
import com.demo.game.service.RandomCommandGenerator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.crossstore.ChangeSetPersister.NotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping(path = "/api", produces = MediaType.APPLICATION_JSON_VALUE)
@Validated
@RequiredArgsConstructor
@Tag(name = "Game API", description = "Operacje na grze i jednostkach")
public class GameApiController {

  private final GameService gameService;
  private final CommandService commandService;
  private final RandomCommandGenerator randomCommandGenerator;

  /* ----------------------------------------------------------- */
  /* 1. Tworzenie nowej gry                                      */
  /* ----------------------------------------------------------- */

  @Operation(summary = "Utwórz nową grę",
      description = "Zamyka ewentualną poprzednią rozgrywkę i startuje nową planszę.",
      requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
          required = true,
          content = @Content(schema = @Schema(implementation = NewGameRequest.class))
      ),
      responses = {
          @ApiResponse(responseCode = "201", description = "Gra utworzona",
              content = @Content(schema = @Schema(implementation = GameDto.class)))
      })
  @PostMapping(path = "/games", consumes = MediaType.APPLICATION_JSON_VALUE)
  @ResponseStatus(HttpStatus.CREATED)
  public GameDto createNewGame(@Valid @RequestBody NewGameRequest req) {
    Game game = gameService.createNewGame(req.boardWidth(), req.boardHeight(), req.whiteConfig(), req.blackConfig());
    return GameDto.from(game);
  }

  /* ----------------------------------------------------------- */
  /* 2. Lista jednostek                                          */
  /* ----------------------------------------------------------- */

  @Operation(summary = "Pobierz jednostki gracza",
      parameters = {
          @Parameter(name = "gameId", description = "Id gry", required = true),
          @Parameter(name = "faction", description = "WHITE lub BLACK", required = true)
      },
      responses = @ApiResponse(responseCode = "200", description = "Lista jednostek",
          content = @Content(schema = @Schema(implementation = UnitDto.class, type = "array")))
  )
  @GetMapping("/games/{gameId}/units")
  public List<UnitDto> listUnits(@PathVariable long gameId, @RequestParam Faction faction) {
    List<Unit> units = gameService.listUnits(gameId, faction);
    return units.stream().map(UnitDto::from).collect(Collectors.toList());
  }

  /* ----------------------------------------------------------- */
  /* 3. Wysłanie rozkazu                                         */
  /* ----------------------------------------------------------- */

  @Operation(summary = "Wyślij rozkaz do jednostki",
      parameters = @Parameter(name = "unitId", description = "Id jednostki", required = true),
      requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
          required = true,
          content = @Content(schema = @Schema(implementation = CommandRequest.class))
      ),
      responses = @ApiResponse(responseCode = "202", description = "Rozkaz przyjęty")
  )
  @PostMapping(path = "/units/{unitId}/command", consumes = MediaType.APPLICATION_JSON_VALUE)
  @ResponseStatus(HttpStatus.ACCEPTED)
  public void sendCommand(@PathVariable long unitId, @Valid @RequestBody CommandRequest body) throws NotFoundException {
    commandService.executeCommand(unitId, body.toDomain());
  }

  /* ----------------------------------------------------------- */
  /* 4. Losowy rozkaz                                            */
  /* ----------------------------------------------------------- */

  @Operation(summary = "Losowy rozkaz (AI)",
      parameters = {
          @Parameter(name = "unitId", description = "Id jednostki", required = true),
          @Parameter(name = "issuer", description = "WHITE lub BLACK", required = true)
      },
      responses = @ApiResponse(responseCode = "202", description = "Rozkaz przyjęty")
  )
  @PostMapping("/units/{unitId}/random")
  @ResponseStatus(HttpStatus.ACCEPTED)
  public void randomCommand(@PathVariable long unitId, @RequestParam Faction issuer) throws NotFoundException {
    Command cmd = randomCommandGenerator.generate(unitId, issuer);
    commandService.executeCommand(unitId, cmd);
  }
}
