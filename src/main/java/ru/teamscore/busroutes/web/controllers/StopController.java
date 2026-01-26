package ru.teamscore.busroutes.web.controllers;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.hibernate.validator.constraints.Length;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.teamscore.busroutes.model.services.StopService;
import ru.teamscore.busroutes.web.dtos.stops.CreateStopDto;
import ru.teamscore.busroutes.web.dtos.stops.FullUpdateStopDto;
import ru.teamscore.busroutes.web.dtos.stops.StopDto;
import ru.teamscore.busroutes.web.mappers.StopDtoMapper;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Validated
public class StopController {
    private final StopService stopService;
    private final StopDtoMapper mapper;

    @PostMapping("/stop")
    public ResponseEntity<StopDto> addStop(@RequestBody @Valid CreateStopDto newStop) {
        var stopModel = mapper.map(newStop);
        var savedStopModel = stopService.addStop(stopModel);
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.map(savedStopModel));
    }

    @GetMapping("/stop/{name}")
    public ResponseEntity<StopDto> getStopByName(
        @PathVariable @NotBlank(message = "Route name cannot be empty")
        @Length(min = 2, max = 255, message = "Route name must be between 2 and 255 characters")
        String name) {
        return ResponseEntity.status(HttpStatus.OK)
            .body(mapper.map(stopService.getStopByName(name)));
    }

    @PutMapping("/stop/{name}")
    public ResponseEntity<StopDto> updateStopByName(
        @PathVariable @NotBlank(message = "Route name cannot be empty")
        @Length(min = 2, max = 255, message = "Route name must be between 2 and 255 characters")
        String name, @RequestBody FullUpdateStopDto updatedStop) {
        var fullUpdateStopCommand = mapper.map(updatedStop);
        var updatedStopModel = stopService.updateStopByName(name, fullUpdateStopCommand);
        return ResponseEntity.status(HttpStatus.OK).body(mapper.map(updatedStopModel));
    }

    @DeleteMapping("/stop/{name}")
    public ResponseEntity<Void> removeStopByName(
        @PathVariable @NotBlank(message = "Route name cannot be empty")
        @Length(min = 2, max = 255, message = "Route name must be between 2 and 255 characters")
        String name) {
        stopService.removeStopByName(name);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
