package net.jsanz.discord.turnsbot.dto;

import lombok.Data;

@Data
public class TurnInfo {
    private final String nextUser;
    private final String gameName;
    private final String turnNumber;
}
