package ru.teamscore.busroutes.model.services;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class RouteServiceFacade {
    private RouteService routeService;
    private StopService stopService;

    public void removeStopWithCheck(String stopName) {
        if (routeService.isStopInUse(stopName)) {
            throw new IllegalArgumentException("Stop in use");
        }
        stopService.removeStopByName(stopName);
    }
}
