package net.jsanz.discord.turnsbot.listeners.handlers;

import net.dv8tion.jda.api.entities.MessageChannel;

public interface ForceTurnHandler {
    boolean forceTurnProcess(MessageChannel privMessageChannel, String receivedMessage);
}
