package net.jsanz.discord.turnsbot.listeners.handlers;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.jsanz.discord.turnsbot.repository.TurnRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.MessageFormat;

@Component
@Slf4j
public class TellCurrentTurnsHandlerCommandImpl implements TellCurrentTurnsHandler {

    @Autowired
    private TurnRepository repository;

    @Override
    public void tellTurnsToUser(MessageChannel privMessageChannel) {

        log.info("Received tell turns message from '{}'", privMessageChannel.getName());

        repository.getAllTurns().forEach(turnInfo -> {
            privMessageChannel.sendMessage(
                    MessageFormat.format(
                            "Partida ''{0}'' - Turno {1}: El siguiente es {2}",
                            turnInfo.getGameName(),
                            turnInfo.getTurnNumber(),
                            turnInfo.getNextUser())
            ).queue();
        });
    }
}
