package net.jsanz.discord.turnsbot.listeners.handlers;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;
import net.jsanz.discord.turnsbot.dto.TurnInfo;
import net.jsanz.discord.turnsbot.repository.TurnRepository;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@Slf4j
public class ForceTurnHandlerCommandImpl implements ForceTurnHandler {

    @Autowired
    private TurnRepository repository;

    @Override
    public boolean forceTurnProcess(MessageChannel privMessageChannel, String receivedMessage) {

        log.info("Received force turn message from '{}': '{}'", privMessageChannel.getName(), receivedMessage);

        List<String> commandTokens = parseInput(receivedMessage);

        commandTokens.forEach(
                token -> log.info("Parsed token: '{}'", token)
        );

        if (commandTokens.size() != 3) {
            reply(privMessageChannel, "Formato correcto: !passturn <nombre_partida> <nombre_usuario>");
            return false;
        }

        Optional<TurnInfo> foundTurn = repository.getTurnInfoByGameName(commandTokens.get(1));
        if (foundTurn.isEmpty()) {
            reply(privMessageChannel, "No tengo datos para una partida con ese nombre!: " + commandTokens.get(1));
            return false;
        }

        List<User> usersByName = privMessageChannel.getJDA().getUsersByName(commandTokens.get(2), true);
        if (usersByName.size() == 0) {
            reply(privMessageChannel, "No encuentro a un jugador con ese nombre!: " + commandTokens.get(2));
            return false;
        }

        TurnInfo oldTurnInfo = foundTurn.get();
        String nextUser = "<@" + usersByName.get(0).getId() + ">";
        TurnInfo newTurnInfo = new TurnInfo(nextUser, oldTurnInfo.getGameName(), oldTurnInfo.getTurnNumber() + " (forzado)");

        repository.saveOrUpdateTurn(newTurnInfo);

        reply(privMessageChannel, "Cambio realizado!");
        return true;
    }

    @NotNull
    private List<String> parseInput(String receivedMessage) {
        List<String> commandTokens = new ArrayList<>();
        Pattern regex = Pattern.compile("[^\\s\"']+|\"([^\"]*)\"|'([^']*)'");
        Matcher regexMatcher = regex.matcher(receivedMessage);
        while (regexMatcher.find()) {
            if (regexMatcher.group(1) != null) {
                // Add double-quoted string without the quotes
                commandTokens.add(regexMatcher.group(1));
            } else if (regexMatcher.group(2) != null) {
                // Add single-quoted string without the quotes
                commandTokens.add(regexMatcher.group(2));
            } else {
                // Add unquoted word
                commandTokens.add(regexMatcher.group());
            }
        }
        return commandTokens;
    }

    private void reply(MessageChannel privMessageChannel, String message) {
        privMessageChannel.sendMessage(message).queue();
    }
}
