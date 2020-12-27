package net.jsanz.discord.turnsbot.listeners.handlers;

import net.dv8tion.jda.api.entities.MessageChannel;

public interface TellCurrentTurnsHandler {
    void tellTurnsToUser(MessageChannel privMessageChannel);
}
