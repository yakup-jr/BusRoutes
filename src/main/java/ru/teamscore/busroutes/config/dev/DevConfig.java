package ru.teamscore.busroutes.config.dev;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.PropertySource;

import java.time.Clock;

@Configuration
@Profile("dev")
@PropertySource("classpath:/application-dev.yml")
public class DevConfig {

    @Bean
    public Clock clock() {
        return Clock.systemDefaultZone();
    }

}
