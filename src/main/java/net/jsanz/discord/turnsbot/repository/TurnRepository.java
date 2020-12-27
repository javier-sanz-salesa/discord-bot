package net.jsanz.discord.turnsbot.repository;

import net.jsanz.discord.turnsbot.dto.TurnInfo;

import java.util.List;
import java.util.Optional;

public interface TurnRepository {
    void saveOrUpdateTurn(TurnInfo turn);
    Optional<TurnInfo> getTurnInfoByGameName(String gameName);
    List<TurnInfo> getAllTurns();
}
