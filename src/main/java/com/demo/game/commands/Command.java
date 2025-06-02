package com.demo.game.commands;

import com.demo.game.objects.enums.CommandType;
import com.demo.game.objects.enums.Faction;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.NoArgsConstructor;

/**
 * Polecenie wysyłane do jednostki.
 * <p>
 * Implementacje są hermetyczne (sealed), dzięki czemu: • enum CommandType pozostaje prosty (bez parametrów),
 * • każda odmiana komendy wymusza przekazanie właściwych danych, •
 * serializacja/deserializacja JSON-a jest bezpieczna (brak „nieznanych” typów).
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "kind")
@JsonSubTypes({
    @JsonSubTypes.Type(value = MoveCommand.class, name = "move"),
    @JsonSubTypes.Type(value = ShootCommand.class, name = "shoot")
})
public sealed interface Command permits MoveCommand, ShootCommand {

  /**
   * Jaką czynność reprezentuje komenda (MOVE_ARCHER, SHOOT_CANNON itd.).
   */
  CommandType type();

  /**
   * Kto wydaje rozkaz. Przydaje się do walidacji np. cooldownu per-gracz.
   */
  Faction issuer();
}
