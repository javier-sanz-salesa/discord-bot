package net.jsanz.discord.turnsbot.repository;

import lombok.extern.slf4j.Slf4j;
import net.jsanz.discord.turnsbot.dto.TurnInfo;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
@Slf4j
public class TurnRepositoryMapImpl implements TurnRepository {

    private final Map<String, TurnInfo> turnMap = new ConcurrentHashMap<>();

    @Override
    public void saveOrUpdateTurn(TurnInfo turn) {
        log.info("Saving new turn info: '{}'", turn);
        turnMap.put(turn.getGameName(), turn);
    }

    @Override
    public Optional<TurnInfo> getTurnInfoByGameName(String gameName) {
        return Optional.ofNullable(turnMap.get(gameName));
    }

    @Override
    public List<TurnInfo> getAllTurns() {
        return new ArrayList<>(turnMap.values());
    }
}
