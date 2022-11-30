package io.project.MegaCoolAnimeBot.config;



import lombok.Data;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;


@Configuration
@Data
@PropertySource("application.properties")
public class AppConfig {

    @Value("${bot.name}")
    String botName;

    @Value("${bot.token}")
    String token;




}
