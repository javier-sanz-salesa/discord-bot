package net.jsanz.discord.turnsbot.configuration;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.jsanz.discord.turnsbot.listeners.TurnsBotActivityListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

import javax.security.auth.login.LoginException;

@Configuration
@EnableScheduling
public class ApplicationConfig {
    @Bean
    JDA getJDA(@Value("${discord.token}") String discordToken, TurnsBotActivityListener listener) throws LoginException {
        JDA jda = JDABuilder
                .createDefault(discordToken)
                .setChunkingFilter(ChunkingFilter.ALL)
                .enableIntents(GatewayIntent.GUILD_MEMBERS)
                .setStatus(OnlineStatus.ONLINE)
                .build();
        jda.addEventListener(listener);

        return jda;
    }
}
