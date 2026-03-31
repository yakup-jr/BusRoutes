package ru.teamscore.busroutes.web.controllers.admin;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.teamscore.busroutes.model.commands.CreateRouteCommand;
import ru.teamscore.busroutes.model.exceptions.AlreadyExistsException;
import ru.teamscore.busroutes.model.exceptions.NotFoundException;
import ru.teamscore.busroutes.model.models.Route;
import ru.teamscore.busroutes.model.services.RouteService;
import ru.teamscore.busroutes.web.dtos.routes.CreateBusinessHoursDto;
import ru.teamscore.busroutes.web.dtos.routes.CreateRouteDto;
import ru.teamscore.busroutes.web.dtos.routes.CreateRouteStopDto;
import ru.teamscore.busroutes.web.dtos.routes.FullUpdateRouteDto;
import ru.teamscore.busroutes.web.mappers.RouteDtoMapper;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/adminpanel/routes")
@AllArgsConstructor
public class AdminRoutesController {

    private final RouteService routeService;
    private final RouteDtoMapper mapper;

    @GetMapping
    public String getRoutes(Model model,
                            @RequestParam(required = false, defaultValue = "0") int page,
                            @RequestParam(required = false, defaultValue = "10") int size) {
        Page<Route> routes = routeService.getRoutes(page, size);
        model.addAttribute("routes", routes);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", routes.getTotalPages());
        model.addAttribute("totalElements", routes.getTotalElements());
        model.addAttribute("size", size);
        model.addAttribute("hasPrevious", routes.hasPrevious());
        model.addAttribute("hasNext", routes.hasNext());
        return "/adminpanel/routes/index";
    }

    @GetMapping("/edit/{routeId}")
    public String getRoute(Model model, @PathVariable UUID routeId) {
        Route route = routeService.getRouteById(routeId);
        FullUpdateRouteDto fullUpdateRouteDto = mapper.toFullUpdateRouteDto(route);

        model.addAttribute("updateRouteDto", fullUpdateRouteDto);
        model.addAttribute("route", route);
        model.addAttribute("routeId", routeId);

        return "adminpanel/routes/edit";
    }

    @PostMapping("/edit/{routeId}")
    public String updateRoute(@PathVariable UUID routeId,
                              @Valid @ModelAttribute("updateRouteDto")
                              FullUpdateRouteDto fullUpdateRouteDto,
                              BindingResult bindingResult,
                              Model model,
                              RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("route", routeService.getRouteById(routeId));
            return "adminpanel/routes/edit";
        }

        try {
            var command = mapper.toCommand(fullUpdateRouteDto);
            routeService.updateRoute(routeId, command);

            redirectAttributes.addFlashAttribute("successMessage", "Route updated successfully!");
            return "redirect:/adminpanel/routes";
        } catch (Exception e) {
            bindingResult.reject("globalError", e.getMessage());
            model.addAttribute("route", routeService.getRouteById(routeId));
            return "adminpanel/routes/edit";
        }
    }

    @GetMapping("/create")
    public String showCreateForm(Model model) {
        CreateBusinessHoursDto hours = CreateBusinessHoursDto.builder()
            .startAt(LocalTime.of(8, 0))
            .endAt(LocalTime.of(20, 0))
            .build();

        List<CreateRouteStopDto> initialStops = new ArrayList<>();
        initialStops.add(CreateRouteStopDto.builder().stopOrder(1).arriveAtFromStart(0).build());
        initialStops.add(CreateRouteStopDto.builder().stopOrder(2).arriveAtFromStart(300).build());

        CreateRouteDto createDto = new CreateRouteDto("", "forward", 15L, hours, initialStops);

        model.addAttribute("createRouteDto", createDto);
        return "/adminpanel/routes/create";
    }

    @PostMapping("/create")
    public String createRoute(@Valid @ModelAttribute("createRouteDto") CreateRouteDto dto,
                              BindingResult bindingResult,
                              Model model) {
        if (bindingResult.hasErrors()) {
            return "adminpanel/routes/create";
        }

        try {
            CreateRouteCommand command = mapper.toCommand(dto);
            routeService.addRoute(command);
            return "redirect:/adminpanel/routes";

        } catch (AlreadyExistsException | IllegalArgumentException | NotFoundException e) {
            bindingResult.reject("globalError", e.getMessage());

            return "adminpanel/routes/create";
        }
    }

    @PostMapping("/copy/{routeId}")
    public String copyRoute(@PathVariable UUID routeId,
                            @RequestParam(defaultValue = "false") boolean isReverseOrder,
                            RedirectAttributes redirectAttributes) {
        routeService.copyRoute(routeId, isReverseOrder);

        redirectAttributes.addFlashAttribute("successMessage", "Route copied successfully!");
        return "redirect:/adminpanel/routes";
    }

    @DeleteMapping("/{routeId}")
    public String deleteRoute(@PathVariable UUID routeId,
                              RedirectAttributes redirectAttributes) {
        routeService.deleteRouteById(routeId);

        redirectAttributes.addFlashAttribute("successMessage", "Route deleted successfully!");
        return "redirect:/adminpanel/routes";
    }
}
