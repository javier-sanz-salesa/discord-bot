package net.jsanz.discord.turnsbot.listeners;

import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;

public interface TurnsBotActivityListener {
    void onReady(@NotNull ReadyEvent event);
    void onMessageReceived(MessageReceivedEvent event);
}
