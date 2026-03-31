package ru.teamscore.busroutes.web.controllers.admin;

import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.teamscore.busroutes.model.models.Route;
import ru.teamscore.busroutes.model.services.RouteService;
import ru.teamscore.busroutes.web.dtos.routes.FullUpdateRouteDto;
import ru.teamscore.busroutes.web.mappers.RouteDtoMapper;

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
                            @RequestParam(required = false, defaultValue = "30") int size) {
        Page<Route> routes = routeService.getRoutes(page, size);
        model.addAttribute("routes", routes);
        return "/adminpanel/routes/index";
    }

    @GetMapping("/edit/{routeId}")
    public String getRoute(Model model, @PathVariable UUID routeId) {
        Route route = routeService.getRouteById(routeId);

        FullUpdateRouteDto fullUpdateRouteDto = mapper.toFullUpdateRouteDto(route);

        model.addAttribute("updateRouteDto", fullUpdateRouteDto);
        model.addAttribute("route", route);
        return "/adminpanel/routes/edit";
    }

    @PostMapping("/edit/{routeId}")
    public String updateRoute(@ModelAttribute("updateRouteDto")
                              FullUpdateRouteDto fullUpdateRouteDto,
                              @PathVariable UUID routeId,
                              RedirectAttributes redirectAttributes) {
        var command = mapper.toCommand(fullUpdateRouteDto);
        routeService.updateRoute(routeId, command);

        redirectAttributes.addFlashAttribute("successMessage", "Route updated successfully!");
        return "redirect:/adminpanel/routes";
    }
}
