package ru.teamscore.busroutes.web.controllers;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.hibernate.validator.constraints.Length;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.teamscore.busroutes.model.enums.TravelSortOption;
import ru.teamscore.busroutes.model.models.Route;
import ru.teamscore.busroutes.model.models.Travel;
import ru.teamscore.busroutes.model.services.RouteService;
import ru.teamscore.busroutes.web.dtos.routes.CreateRouteDto;
import ru.teamscore.busroutes.web.dtos.routes.FullUpdateRouteDto;
import ru.teamscore.busroutes.web.dtos.routes.SummaryRouteDto;
import ru.teamscore.busroutes.web.dtos.routes.TravelDto;
import ru.teamscore.busroutes.web.mappers.RouteDtoMapper;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Validated
public class RouteController {
    private final RouteService routeService;
    private final RouteDtoMapper mapper;

    @PostMapping("/route")
    public ResponseEntity<SummaryRouteDto> addRoute(@RequestBody @Valid CreateRouteDto newRoute) {
        var routeModel = mapper.mapCreateRoute(newRoute);
        var savedRouteModel = routeService.addRoute(routeModel);
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.mapRoute(savedRouteModel));
    }

    @PostMapping("/route/{routeName}/copy")
    public ResponseEntity<SummaryRouteDto> copyRoute(@RequestParam boolean isReverseOrder,
                                                     @PathVariable @NotBlank(
                                                         message = "Route name cannot be empty")
                                                     @Length(min = 2, max = 255,
                                                         message = "Route name must be between 2 and 255 characters")
                                                     String routeName) {
        Route routeModel = routeService.copyRoute(routeName, isReverseOrder);
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.mapRoute(routeModel));
    }

    @GetMapping(value = "/route", params = {"stopName", "sort"})
    public ResponseEntity<Iterable<TravelDto>> getRoutesByStop(
        @RequestParam @NotBlank(message = "Route name cannot be empty")
        @Length(min = 2, max = 255, message = "Route name must be between 2 and 255 characters")
        String stopName, @RequestParam @NotBlank String sort) {
        List<Travel> travels =
            routeService.getRoutesByStop(stopName, TravelSortOption.valueOf(sort));
        return ResponseEntity.status(HttpStatus.OK).body(mapper.mapTravel(travels));
    }

    @GetMapping(value = "/route", params = {"fromStopName", "toStopName", "sort"})
    public ResponseEntity<Iterable<TravelDto>> getRoutesByStops(@RequestParam String fromStopName,
                                                                @RequestParam String toStopName,
                                                                @RequestParam String sort) {
        List<Travel> travels =
            routeService.getRoutesByStops(fromStopName, toStopName, TravelSortOption.valueOf(sort));
        return ResponseEntity.status(HttpStatus.OK).body(mapper.mapTravel(travels));
    }

    @GetMapping("/route/{name}")
    public ResponseEntity<SummaryRouteDto> getRouteByName(
        @PathVariable @NotBlank(message = "Route name cannot be empty")
        @Length(min = 2, max = 255, message = "Route name must be between 2 and 255 characters")
        String name) {
        Route routeModelByName = routeService.getRouteByName(name);
        return ResponseEntity.status(HttpStatus.OK).body(mapper.mapRoute(routeModelByName));
    }

    @PutMapping("/route/{name}")
    public ResponseEntity<SummaryRouteDto> updateRouteByName(
        @PathVariable @NotBlank(message = "Route name cannot be empty")
        @Length(min = 2, max = 255, message = "Route name must be between 2 and 255 characters")
        String name, @RequestBody @Valid FullUpdateRouteDto updatedRoute) {
        var routeModel = mapper.mapUpdateRoute(updatedRoute);
        var updatedRouteModel = routeService.updateRouteByName(name, routeModel);
        return ResponseEntity.status(HttpStatus.OK).body(mapper.mapRoute(updatedRouteModel));
    }

    @DeleteMapping("/route/{name}")
    public ResponseEntity<Void> removeRoute(
        @PathVariable @NotBlank(message = "Route name cannot be empty")
        @Length(min = 2, max = 255, message = "Route name must be between 2 and 255 characters")
        String name) {
        routeService.removeRoute(name);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
