package ru.teamscore.busroutes.web.controllers.admin;

import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.teamscore.busroutes.model.commands.CreateStopCommand;
import ru.teamscore.busroutes.model.commands.FullUpdateStopCommand;
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
        return "/adminpanel/stops/edit";
    }

    @PostMapping("/edit/{stopName}")
    public String updateStop(@PathVariable String stopName,
                             @ModelAttribute("stopDto") FullUpdateStopDto stopDto,
                             RedirectAttributes ra) {
        FullUpdateStopCommand command = mapper.toCommand(stopDto);
        stopService.updateStopByName(stopName, command);

        ra.addFlashAttribute("successMessage", "Stop updated successfully!");
        return "redirect:/adminpanel/stops";
    }

    @GetMapping("/create")
    public String createStop(Model model) {
        CreateStopDto createStopDto =
            CreateStopDto.builder().coordinates(GeographicCoordinatesDto.builder().build()).build();

        model.addAttribute("createStopDto", createStopDto);
        return "/adminpanel/stops/create";
    }

    @PostMapping("/create")
    public String createStop(@ModelAttribute("createStopDto") CreateStopDto createStopDto,
                             RedirectAttributes redirectAttributes) {
        CreateStopCommand command = mapper.toCommand(createStopDto);
        stopService.addStop(command);

        redirectAttributes.addFlashAttribute("successMessage", "Stop created successfully!");
        return "redirect:/adminpanel/stops";
    }

    @GetMapping("/delete/{stopName}")
    public String deleteStop(@PathVariable String stopName, RedirectAttributes redirectAttributes) {
        stopService.deleteStopByName(stopName);
        redirectAttributes.addFlashAttribute("successMessage", "Successfully deleted");
        return "redirect:/adminpanel/stops";
    }
}
