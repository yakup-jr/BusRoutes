package ru.teamscore.busroutes.web.controllers.admin;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import ru.teamscore.busroutes.model.commands.CreateStopCommand;
import ru.teamscore.busroutes.model.commands.FullUpdateStopCommand;
import ru.teamscore.busroutes.model.exceptions.AlreadyExistsException;
import ru.teamscore.busroutes.model.exceptions.NotFoundException;
import ru.teamscore.busroutes.model.models.Stop;
import ru.teamscore.busroutes.model.services.RouteService;
import ru.teamscore.busroutes.model.services.StopService;
import ru.teamscore.busroutes.web.dtos.stops.CreateStopDto;
import ru.teamscore.busroutes.web.dtos.stops.FullUpdateStopDto;
import ru.teamscore.busroutes.web.dtos.stops.GeographicCoordinatesDto;
import ru.teamscore.busroutes.web.mappers.StopDtoMapper;

import java.util.UUID;

@Controller
@RequestMapping("/adminpanel/stops")
@AllArgsConstructor
public class AdminStopController {

    private final StopService stopService;
    private final RouteService routeService;
    private final StopDtoMapper mapper;

    private static final String EDIT_STOP_PATH = "adminpanel/stops/edit";
    private static final String REDIRECT_STOPS = "redirect:/adminpanel/stops";

    @GetMapping
    public String getAllStops(Model model,
                              @RequestParam(required = false, defaultValue = "0") int page,
                              @RequestParam(required = false, defaultValue = "30") int size) {
        Page<Stop> stops = stopService.getStops(page, size);
        model.addAttribute("stops", stops);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", stops.getTotalPages());
        model.addAttribute("totalElements", stops.getTotalElements());
        model.addAttribute("size", size);
        model.addAttribute("hasPrevious", stops.hasPrevious());
        model.addAttribute("hasNext", stops.hasNext());
        return "/adminpanel/stops/index";
    }

    @GetMapping("/{stopId}")
    public String getStopById(@PathVariable UUID stopId, Model model) {
        Stop stop = stopService.getStopById(stopId);

        FullUpdateStopDto stopDto = FullUpdateStopDto.builder()
            .name(stop.getName())
            .coordinates(GeographicCoordinatesDto.builder()
                .latitude(stop.getCoordinates().getLatitude())
                .longitude(stop.getCoordinates().getLongitude())
                .build())
            .build();

        model.addAttribute("stop", stop);
        model.addAttribute("stopDto", stopDto);
        model.addAttribute("stopName", stop.getName());

        return EDIT_STOP_PATH;
    }

    @PutMapping("/edit/{stopName}")
    public String updateStop(@PathVariable String stopName,
                             @Valid @ModelAttribute("stopDto") FullUpdateStopDto stopDto,
                             BindingResult bindingResult,
                             Model model) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("stopName", stopName);
            return EDIT_STOP_PATH;
        }

        try {
            FullUpdateStopCommand command = mapper.toCommand(stopDto);
            stopService.updateStopByName(stopName, command);
            return REDIRECT_STOPS;
        } catch (AlreadyExistsException | IllegalArgumentException | NotFoundException e) {
            model.addAttribute("stopName", stopName);
            bindingResult.reject("globalError", e.getMessage());
            return EDIT_STOP_PATH;
        }
    }

    @GetMapping("/create")
    public String createStop(Model model) {
        CreateStopDto createStopDto =
            CreateStopDto.builder().coordinates(GeographicCoordinatesDto.builder().build()).build();

        model.addAttribute("createStopDto", createStopDto);
        return "/adminpanel/stops/create";
    }

    @PostMapping("/create")
    public String createStop(@Valid @ModelAttribute("createStopDto") CreateStopDto createStopDto,
                             BindingResult bindingResult,
                             Model model) {

        if (bindingResult.hasErrors()) {
            return "adminpanel/stops/create";
        }

        try {
            CreateStopCommand command = mapper.toCommand(createStopDto);
            stopService.addStop(command);

            return REDIRECT_STOPS;
        } catch (AlreadyExistsException | IllegalArgumentException | NotFoundException e) {
            bindingResult.reject("globalError", e.getMessage());

            return "adminpanel/stops/create";
        }
    }

    @GetMapping("/delete/{stopName}")
    public String deleteStop(@PathVariable String stopName) {
        stopService.deleteStopByName(stopName);
        return REDIRECT_STOPS;
    }
}
