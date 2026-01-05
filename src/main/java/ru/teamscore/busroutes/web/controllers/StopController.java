package ru.teamscore.busroutes.web.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.teamscore.busroutes.model.models.Stop;
import ru.teamscore.busroutes.model.services.StopService;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class StopController {
    private final StopService stopService;

    @PostMapping("/stop")
    public ResponseEntity<Stop> addStop(@RequestBody Stop newStop) {
        return ResponseEntity.status(HttpStatus.CREATED).body(stopService.addStop(newStop));
    }

    @GetMapping("/stop/{name}")
    public ResponseEntity<Stop> getStopByName(@PathVariable String name) {
        return ResponseEntity.status(HttpStatus.OK).body(stopService.getStopByName(name));
    }

    @PutMapping("/stop/{name}")
    public ResponseEntity<Stop> updateStopByName(@PathVariable String name,
                                                 @RequestBody Stop updatedStop) {
        return ResponseEntity.status(HttpStatus.OK)
            .body(stopService.updateStopByName(name, updatedStop));
    }

    @DeleteMapping("/stop/{name}")
    public ResponseEntity<Void> removeStopByName(@PathVariable String name) {
        stopService.removeStopByName(name);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
