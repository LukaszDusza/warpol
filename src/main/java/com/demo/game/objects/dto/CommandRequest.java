package com.demo.game.objects.dto;

import com.demo.game.commands.Command;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.swagger.v3.oas.annotations.media.DiscriminatorMapping;
import io.swagger.v3.oas.annotations.media.Schema;


@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "kind")
@JsonSubTypes({
    @JsonSubTypes.Type(value = MoveRequest.class,  name = "move"),
    @JsonSubTypes.Type(value = ShootRequest.class, name = "shoot")
})
@Schema(
    discriminatorProperty = "kind",
    oneOf = {MoveRequest.class, ShootRequest.class},
    discriminatorMapping = {
        @DiscriminatorMapping(value = "move",  schema = MoveRequest.class),
        @DiscriminatorMapping(value = "shoot", schema = ShootRequest.class)
    }
)
public sealed interface CommandRequest permits MoveRequest, ShootRequest {
  Command toDomain();
}