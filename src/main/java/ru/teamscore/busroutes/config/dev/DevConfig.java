package ru.teamscore.busroutes.config.dev;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.PropertySource;
import ru.teamscore.busroutes.model.models.Route;
import ru.teamscore.busroutes.model.models.Stop;

import java.util.concurrent.CopyOnWriteArrayList;

@Configuration
@Profile("dev")
@PropertySource("classpath:/application-dev.yml")
public class DevConfig {

    @Bean
    public CopyOnWriteArrayList<Route> routes() {
        return new CopyOnWriteArrayList<>();
    }

    @Bean
    public CopyOnWriteArrayList<Stop> stops() {
        return new CopyOnWriteArrayList<>();
    }
}
