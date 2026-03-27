package ru.teamscore.busroutes.web.controllers;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
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
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Validated
public class RouteController {
    private final RouteService routeService;
    private final RouteDtoMapper mapper;

    @PostMapping("/route")
    public ResponseEntity<SummaryRouteDto> addRoute(
        @RequestBody @Valid CreateRouteDto createRouteDto) {
        var command = mapper.toCommand(createRouteDto);
        var savedRouteModel = routeService.addRoute(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toDto(savedRouteModel));
    }

    @PostMapping("/route/{routeId}/copy")
    public ResponseEntity<SummaryRouteDto> copyRoute(@RequestParam boolean isReverseOrder,
                                                     @PathVariable @NotNull
                                                     UUID routeId) {
        Route routeModel = routeService.copyRoute(routeId, isReverseOrder);
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toDto(routeModel));
    }

    @GetMapping(value = "/route", params = {"stopName", "sort"})
    public ResponseEntity<Iterable<TravelDto>> getRoutesByStop(
        @RequestParam @NotBlank(message = "Route name cannot be empty")
        String stopName, @RequestParam @NotBlank String sort) {
        List<Travel> travels =
            routeService.getRoutesByStop(stopName, TravelSortOption.valueOf(sort));
        return ResponseEntity.status(HttpStatus.OK).body(mapper.toDtos(travels));
    }

    @GetMapping(value = "/route", params = {"fromStopName", "toStopName", "sort"})
    public ResponseEntity<Iterable<TravelDto>> getRoutesByStops(@RequestParam String fromStopName,
                                                                @RequestParam String toStopName,
                                                                @RequestParam
                                                                TravelSortOption sort) {
        List<Travel> travels =
            routeService.getRoutesByStops(fromStopName, toStopName, sort);
        return ResponseEntity.status(HttpStatus.OK).body(mapper.toDtos(travels));
    }

    @GetMapping("/route/{name}")
    public ResponseEntity<Iterable<SummaryRouteDto>> getRouteByName(
        @PathVariable @NotBlank(message = "Route name cannot be empty")
        String name) {
        Iterable<Route> routeModelByName = routeService.getRouteByName(name);
        return ResponseEntity.status(HttpStatus.OK).body(mapper.toRouteDtos(routeModelByName));
    }

    @PutMapping("/route/{routeId}")
    public ResponseEntity<SummaryRouteDto> updateRoute(
        @PathVariable UUID routeId, @RequestBody @Valid FullUpdateRouteDto updateRouteDto) {
        var command = mapper.toCommand(updateRouteDto);
        var updatedRouteModel = routeService.updateRoute(routeId, command);
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toDto(updatedRouteModel));
    }

    @DeleteMapping("/route/{name}")
    public ResponseEntity<Void> deleteRoute(
        @PathVariable @NotBlank(message = "Route name cannot be empty")
        String name) {
        routeService.deleteRoute(name);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
