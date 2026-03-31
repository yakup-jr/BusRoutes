package ru.teamscore.busroutes.web.controllers.rest;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.hibernate.validator.constraints.Length;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.teamscore.busroutes.model.models.Stop;
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
    public ResponseEntity<StopDto> addStop(@RequestBody @Valid CreateStopDto createStopDto) {
        var stopModel = mapper.toCommand(createStopDto);
        var savedStopModel = stopService.addStop(stopModel);
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toDto(savedStopModel));
    }

    @GetMapping("/stop/{name}")
    public ResponseEntity<StopDto> getStopByName(
        @PathVariable @NotBlank(message = "Stop name cannot be empty")
        @Length(min = 2, max = 255, message = "Stop name must be between 2 and 255 characters")
        String name) {
        return ResponseEntity.status(HttpStatus.OK)
            .body(mapper.toDto(stopService.getStopByName(name)));
    }

    @GetMapping("/stop/search")
    public ResponseEntity<Page<StopDto>> searchStopByPart(
        @RequestParam @NotBlank(message = "Stop name cannot be empty") String part,
        @RequestParam(required = false, defaultValue = "0") int page,
        @RequestParam(required = false, defaultValue = "10") int size
    ) {
        Page<Stop> stops =
            stopService.getStopsByNameStartingWithIgnoreCase(part, page, size);
        return ResponseEntity.status(HttpStatus.OK).body(stops.map(mapper::toDto));
    }

    @PutMapping("/stop/{name}")
    public ResponseEntity<StopDto> updateStopByName(
        @PathVariable @NotBlank(message = "Stop name cannot be empty")
        @Length(min = 2, max = 255, message = "Stop name must be between 2 and 255 characters")
        String name, @RequestBody FullUpdateStopDto updateStopDto) {
        var command = mapper.toCommand(updateStopDto);
        var updatedStopModel = stopService.updateStopByName(name, command);
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toDto(updatedStopModel));
    }

    @DeleteMapping("/stop/{name}")
    public ResponseEntity<Void> deleteStopByName(
        @PathVariable @NotBlank(message = "Stop name cannot be empty")
        @Length(min = 2, max = 255, message = "Stop name must be between 2 and 255 characters")
        String name) {
        stopService.deleteStopByName(name);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
