package net.jsanz.discord.turnsbot.listeners.schedule;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.TextChannel;
import net.jsanz.discord.turnsbot.repository.TurnRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.text.MessageFormat;

@Component
@Slf4j
public class TellTurnsSchedulerImpl implements TellTurnsScheduler {

    @Autowired
    private TurnRepository repository;

    @Autowired
    private JDA jda;

    @Override
    @Scheduled(cron = "0 0 8-22/2 ? * MON-FRI")
    public void tellTurnsOnWeekdays() {
        tellTurns();
    }

    @Override
    @Scheduled(cron = "0 55 9-23/2 ? * SUN,SAT")
    public void tellTurnsOnWeekends() {
        tellTurns();
    }

    private void tellTurns() {
        log.info("Broadcasting scheduled turns announcement");

        TextChannel broadcastChannel = jda.getTextChannelsByName("civ-talk", true).get(0);
        repository.getAllTurns().forEach(turnInfo -> {
            broadcastChannel.sendMessage(
                    MessageFormat.format(
                            "Partida ''{0}'' - Turno {1}: El siguiente es {2}",
                            turnInfo.getGameName(),
                            turnInfo.getTurnNumber(),
                            turnInfo.getNextUser())
            ).queue();
        });
    }
}
