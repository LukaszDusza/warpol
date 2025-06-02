package com.demo.game.objects.dto;

import com.demo.game.commands.Command;
import com.demo.game.commands.ShootCommand;
import com.demo.game.objects.enums.CommandType;
import com.demo.game.objects.enums.Faction;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(name = "ShootCommandRequest",
    description = "Polecenie strzału",
    example = "{" +
        "\"kind\":\"shoot\"," +
        "\"dx\":-2," +
        "\"dy\":3," +
        "\"issuer\":\"BLACK\"," +
        "\"type\":\"SHOOT_CANNON\"" +
        "}")
public record ShootRequest(
    int dx,
    int dy,
    @NotNull Faction issuer,
    @NotNull CommandType type
) implements CommandRequest {

  @Override
  public Command toDomain() {
    return new ShootCommand(dx, dy, issuer, type);
  }
}