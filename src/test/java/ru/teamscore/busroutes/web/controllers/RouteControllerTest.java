package ru.teamscore.busroutes.web.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.teamscore.busroutes.model.enums.TravelSortOption;
import ru.teamscore.busroutes.model.models.*;
import ru.teamscore.busroutes.model.services.RouteService;
import tools.jackson.databind.ObjectMapper;

import java.time.Duration;
import java.time.LocalTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RouteController.class)
class RouteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private RouteService routeService;

    private Stop stop2;
    private Route route1;
    private Travel travel1;

    @BeforeEach
    void setUp() {
        Stop stop1 = Stop.valueOf("Stop1", Stop.GeographicCoordinates.valueOf(53.198050,
            50.108750));
        stop2 = Stop.valueOf("Stop2", Stop.GeographicCoordinates.valueOf(53.195873, 50.104954));

        List<RouteStop> stopsRoute1 =
            List.of(RouteStop.valueOf(0, 1, stop1), RouteStop.valueOf(120, 2, stop2));

        BusinessHours businessHours =
            BusinessHours.valueOf(LocalTime.of(5, 30), LocalTime.of(23, 0));
        route1 = Route.valueOf("route1", "bus", stopsRoute1, Duration.ofMinutes(10), businessHours);
        travel1 = Travel.valueOf(route1, Duration.ofMinutes(10), LocalTime.of(6, 0));
    }

    @Test
    void addRoute_ReturnCreated() throws Exception {
        when(routeService.addRoute(any(Route.class))).thenReturn(route1);

        mockMvc.perform(post("/api/v1/route").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(route1))).andExpect(status().isCreated())
            .andExpect(jsonPath("$.name").value("route1"))
            .andExpect(jsonPath("$.type").value("bus"));
    }

    @Test
    void copyRoute_ReturnCreated() throws Exception {
        when(routeService.copyRoute(any(String.class), eq(true))).thenReturn(route1);

        mockMvc.perform(post("/api/v1/route/route1/copy").param("isReverseOrder", "true")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.name").value("route1"));
    }

    @Test
    void getRoutesByStop_ReturnListTravels() throws Exception {
        when(routeService.getRoutesByStop("Stop1", TravelSortOption.TIME_IN_ROUTE)).thenReturn(
            List.of(travel1));

        mockMvc.perform(
                get("/api/v1/route").param("stopName", "Stop1").param("sort", "TIME_IN_ROUTE"))
            .andExpect(status().isOk()).andExpect(jsonPath("$[0].route.name").value("route1"));
    }

    @Test
    void getRoutesByStops_ReturnListTravels() throws Exception {
        when(routeService.getRoutesByStops("Stop1", "Stop2",
            TravelSortOption.NEAREST_ARRIVAL)).thenReturn(List.of(travel1));

        mockMvc.perform(
                get("/api/v1/route").param("fromStopName", "Stop1").param("toStopName", "Stop2")
                    .param("sort", "NEAREST_ARRIVAL")).andExpect(status().isOk())
            .andExpect(jsonPath("$[0].route.name").value("route1"));
    }

    @Test
    void getRouteByName_ReturnRoute() throws Exception {
        when(routeService.getRouteByName("route1")).thenReturn(route1);

        mockMvc.perform(get("/api/v1/route/route1")).andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("route1"))
            .andExpect(jsonPath("$.type").value("bus"));
    }

    @Test
    void updateRouteByName_ReturnUpdatedRoute() throws Exception {
        when(routeService.updateRouteByName(eq("route1"), any(Route.class))).thenReturn(route1);

        mockMvc.perform(put("/api/v1/route/route1").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(route1))).andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("route1"));
    }

    @Test
    void removeRoute_ReturnNoContent() throws Exception {
        doNothing().when(routeService).removeRoute("route1");

        mockMvc.perform(delete("/api/v1/route/route1")).andExpect(status().isNoContent());

        verify(routeService, times(1)).removeRoute("route1");
    }
}
