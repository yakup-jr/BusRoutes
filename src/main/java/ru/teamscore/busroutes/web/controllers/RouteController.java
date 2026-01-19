package ru.teamscore.busroutes.web.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.teamscore.busroutes.model.enums.TravelSortOption;
import ru.teamscore.busroutes.model.models.Route;
import ru.teamscore.busroutes.model.models.Travel;
import ru.teamscore.busroutes.model.services.RouteService;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class RouteController {
    private final RouteService routeService;

    @PostMapping("/route")
    public ResponseEntity<Route> addRoute(@RequestBody @Valid Route newRoute) {
        return ResponseEntity.status(HttpStatus.CREATED).body(routeService.addRoute(newRoute));
    }

    @PostMapping("/route/{routeName}/copy")
    public ResponseEntity<Route> copyRoute(@RequestParam boolean isReverseOrder,
                                           @PathVariable String routeName) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(routeService.copyRoute(routeName, isReverseOrder));
    }

    @GetMapping(value = "/route", params = {"stopName", "sort"})
    public ResponseEntity<List<Travel>> getRoutesByStop(@RequestParam String stopName,
                                                        @RequestParam String sort) {
        return ResponseEntity.status(HttpStatus.OK)
            .body(routeService.getRoutesByStop(stopName, TravelSortOption.valueOf(sort)));
    }

    @GetMapping(value = "/route", params = {"fromStopName", "toStopName", "sort"})
    public ResponseEntity<List<Travel>> getRoutesByStops(@RequestParam String fromStopName,
                                                         @RequestParam String toStopName,
                                                         @RequestParam String sort) {
        return ResponseEntity.status(HttpStatus.OK).body(
            routeService.getRoutesByStops(fromStopName, toStopName,
                TravelSortOption.valueOf(sort)));
    }

    @GetMapping("/route/{name}")
    public ResponseEntity<Route> getRouteByName(@PathVariable String name) {
        return ResponseEntity.status(HttpStatus.OK).body(routeService.getRouteByName(name));
    }

    @PutMapping("/route/{name}")
    public ResponseEntity<Route> updateRouteByName(@PathVariable String name,
                                                   @RequestBody @Valid Route updatedRoute) {
        return ResponseEntity.status(HttpStatus.OK)
            .body(routeService.updateRouteByName(name, updatedRoute));
    }

    @DeleteMapping("/route/{name}")
    public ResponseEntity<Void> removeRoute(@PathVariable String name) {
        routeService.removeRoute(name);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
