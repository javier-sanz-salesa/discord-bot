package net.jsanz.discord.turnsbot.listeners;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.Event;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.jsanz.discord.turnsbot.dto.TurnInfo;
import net.jsanz.discord.turnsbot.listeners.handlers.ForceTurnHandler;
import net.jsanz.discord.turnsbot.listeners.handlers.TellCurrentTurnsHandler;
import net.jsanz.discord.turnsbot.repository.TurnRepository;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Component
public class DiscordListener extends ListenerAdapter implements TurnsBotActivityListener {

    @Value("${discord.selfname}")
    private String selfName;

    @Autowired
    private TurnRepository repository;

    @Autowired
    private ForceTurnHandler forceTurnHandler;

    @Autowired
    private TellCurrentTurnsHandler tellCurrentTurnsHandler;

    @Value("${bot.tellTurnsOnStartup:false}")
    private boolean tellTurnsOnStartup;

    private static final Pattern PATTERN_USER = Pattern.compile("<@\\d+>");
    private static final Pattern PATTERN_GAME = Pattern.compile("Game:\\s*(.+)");
    private static final Pattern PATTERN_TURN = Pattern.compile("Turn:\\s*(.+)");

    @Override
    public void onReady(@NotNull ReadyEvent event) {
        readTurnsFromLatestMessages(event);
        if (tellTurnsOnStartup) {
            log.info("Telling initial turns on startup");
            printDashboard(event.getJDA().getTextChannelsByName("civ-talk", true).get(0));
        } else {
            log.info("Muting initial turns announcement because config");
        }
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        String authorName = event.getAuthor().getName();
        if (authorName.equalsIgnoreCase(selfName)) {
            return;
        }

        String channelName = event.getChannel().getName();

        if (channelName.equalsIgnoreCase("civ"))  {
            extractTurnInfoFromMessage(event.getMessage());
        }
    }

    @Override
    public void onPrivateMessageReceived(@Nonnull PrivateMessageReceivedEvent event) {
        if (event.getAuthor().getName().equalsIgnoreCase(selfName)) {
            return;
        }

        String receivedMessage = event.getMessage().getContentRaw();
        MessageChannel privMessageChannel = event.getMessage().getChannel();

        log.info("Received private message from user '{}' in channel '{}'({}): '{}'",
                event.getAuthor().getName(),
                privMessageChannel.getName(),
                event.getMessage().getChannel().getType(),
                receivedMessage);

        if (receivedMessage.toLowerCase().startsWith("!passturn ")) {
            forceTurnHandler.forceTurnProcess(privMessageChannel, receivedMessage);
        } else if (receivedMessage.toLowerCase().startsWith("!turns")) {
            tellCurrentTurnsHandler.tellTurnsToUser(privMessageChannel);
        } else {
            privMessageChannel.sendMessage("No te he entendido, repite el comando por favor!").queue();
            privMessageChannel.sendMessage("Comandos permitidos: !turns").queue();
            privMessageChannel.sendMessage("Comandos permitidos: !passturn <nombre_partida> <nombre_usuario>").queue();
        }
    }

    private void readTurnsFromLatestMessages(@NotNull Event event) {
        TextChannel civ = event.getJDA().getTextChannelsByName("civ", true).get(0);
        List<Message> messages = civ.getHistory().retrievePast(30).complete();
        Collections.reverse(messages);

        messages.forEach(this::extractTurnInfoFromMessage);
    }

    private void extractTurnInfoFromMessage(Message msg) {
        String contentRaw = msg.getContentRaw();
        Matcher matcherUser = PATTERN_USER.matcher(contentRaw);
        Matcher matcherGame = PATTERN_GAME.matcher(contentRaw);
        Matcher matcherTurn = PATTERN_TURN.matcher(contentRaw);
        if (matcherUser.find() && matcherGame.find() && matcherTurn.find()) {
            String extractedUser = matcherUser.group();
            String extractedGameName = matcherGame.group(1);
            String extractedTurnNumber = matcherTurn.group(1);

            repository.saveOrUpdateTurn(new TurnInfo(extractedUser, extractedGameName, extractedTurnNumber));
        }
    }

    private void printDashboard(TextChannel civTalk) {
        repository.getAllTurns().forEach(turnInfo -> {
            log.info("Partida '{}' - Turno {}: El siguiente es {}", turnInfo.getGameName(), turnInfo.getTurnNumber(), turnInfo.getNextUser());

            civTalk.sendMessage(
                    MessageFormat.format(
                            "Partida ''{0}'' - Turno {1}: El siguiente es {2}",
                            turnInfo.getGameName(),
                            turnInfo.getTurnNumber(),
                            turnInfo.getNextUser())
            ).queue();
        });
    }
}
