package com.demo.game.objects.dto;

import com.demo.game.commands.Command;
import com.demo.game.commands.MoveCommand;
import com.demo.game.objects.enums.CommandType;
import com.demo.game.objects.enums.Direction;
import com.demo.game.objects.enums.Faction;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Schema(name = "MoveCommandRequest",
    description = "Polecenie ruchu jednostki",
    example = "{" +
        "\"kind\":\"move\"," +
        "\"direction\":\"RIGHT\"," +
        "\"steps\":1," +
        "\"issuer\":\"WHITE\"" +
        "}")
public record MoveRequest(
    @NotNull Direction direction,
    @Min(1) @Max(3) int steps,
    @NotNull Faction issuer
) implements CommandRequest {

  @Override
  public Command toDomain() {

    return new MoveCommand(direction, steps, issuer, CommandType.MOVE_TRANSPORT);
  }
}