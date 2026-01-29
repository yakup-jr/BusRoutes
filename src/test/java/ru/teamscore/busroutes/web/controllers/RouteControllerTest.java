package ru.teamscore.busroutes.web.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import ru.teamscore.busroutes.model.commands.CreateRouteCommand;
import ru.teamscore.busroutes.model.commands.FullUpdateRouteCommand;
import ru.teamscore.busroutes.model.enums.TravelSortOption;
import ru.teamscore.busroutes.model.models.*;
import ru.teamscore.busroutes.model.services.RouteService;
import ru.teamscore.busroutes.web.dtos.routes.*;
import ru.teamscore.busroutes.web.mappers.RouteDtoMapper;
import ru.teamscore.busroutes.web.mappers.RouteDtoMapperImpl;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.time.Duration;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.StreamSupport;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RouteController.class)
@Import(RouteDtoMapperImpl.class)
class RouteControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private RouteDtoMapper mapper;

    @MockitoBean
    private RouteService routeService;

    private Route route1;
    private Travel travel1;

    private static final BusinessHoursDto BUSINESS_HOURS_DTO =
        new BusinessHoursDto(LocalTime.of(5, 30), LocalTime.of(23, 0));
    private static final List<SummaryRouteStopDto> ROUTE_STOP_DTOS = List.of(
        new SummaryRouteStopDto(0, 1, "Stop1"),
        new SummaryRouteStopDto(120, 2, "Stop2")
    );
    private static final SummaryRouteDto SUMMARY_ROUTE_DTO = new SummaryRouteDto(
        "route1",
        "bus",
        Duration.ofMinutes(10),
        BUSINESS_HOURS_DTO,
        ROUTE_STOP_DTOS
    );
    private static final CreateRouteDto CREATE_ROUTE_DTO = new CreateRouteDto(
        "route1", "bus", Duration.ofMinutes(10), BUSINESS_HOURS_DTO, ROUTE_STOP_DTOS
    );

    @BeforeEach
    void setUp() {
        Stop s1 = Stop.valueOf("Stop1", Stop.GeographicCoordinates.valueOf(53.198050, 50.108750));
        Stop s2 = Stop.valueOf("Stop2", Stop.GeographicCoordinates.valueOf(53.195873, 50.104954));

        List<RouteStop> stopsRoute1 = List.of(
            RouteStop.valueOf(0, 1, s1),
            RouteStop.valueOf(120, 2, s2)
        );

        BusinessHours businessHours = BusinessHours.valueOf(
            BUSINESS_HOURS_DTO.startAt(),
            BUSINESS_HOURS_DTO.endAt()
        );

        route1 = Route.valueOf("route1", "bus", stopsRoute1, Duration.ofMinutes(10), businessHours);
        travel1 = Travel.valueOf(route1, Duration.ofMinutes(10), LocalTime.of(6, 0));
    }

    @Test
    void addRoute_ReturnCreated() throws Exception {
        when(routeService.addRoute(any(CreateRouteCommand.class))).thenReturn(route1);

        MvcResult mvcResult =
            mockMvc.perform(post("/api/v1/route").contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(CREATE_ROUTE_DTO)))
                .andExpect(status().isCreated()).andReturn();

        SummaryRouteDto routeDto =
            objectMapper.readValue(mvcResult.getResponse().getContentAsString(),
                SummaryRouteDto.class);
        assertThat(routeDto.name()).isEqualTo(CREATE_ROUTE_DTO.name());
        assertThat(routeDto.type()).isEqualTo(CREATE_ROUTE_DTO.type());
        assertThat(routeDto.businessHours()).isEqualTo(CREATE_ROUTE_DTO.businessHours());
        assertThat(routeDto.interval()).isEqualTo(CREATE_ROUTE_DTO.interval());
        assertThat(routeDto.stops()).isEqualTo(ROUTE_STOP_DTOS);
    }

    @Test
    void copyRoute_ReturnCreated() throws Exception {
        Route reversedRoute = route1.copy().reverseRoute();
        when(routeService.copyRoute(any(String.class), eq(true))).thenReturn(reversedRoute);

        MvcResult mvcResult =
            mockMvc.perform(post("/api/v1/route/route1/copy").param("isReverseOrder", "true")
                    .contentType(MediaType.APPLICATION_JSON)).andExpect(status().isCreated())
                .andReturn();

        SummaryRouteDto responseRouteDto =
            objectMapper.readValue(mvcResult.getResponse().getContentAsString(),
                SummaryRouteDto.class);
        assertThat(responseRouteDto.name()).isEqualTo(
            String.format("%s_copy", SUMMARY_ROUTE_DTO.name()));
        assertThat(responseRouteDto.type()).isEqualTo(SUMMARY_ROUTE_DTO.type());
        assertThat(responseRouteDto.businessHours()).isEqualTo(SUMMARY_ROUTE_DTO.businessHours());
        assertThat(responseRouteDto.interval()).isEqualTo(SUMMARY_ROUTE_DTO.interval());
        assertThat(responseRouteDto.stops().get(0)).isEqualTo(
            new SummaryRouteStopDto(0, 1, "Stop2"));
        assertThat(responseRouteDto.stops().get(1)).isEqualTo(new SummaryRouteStopDto(120, 2,
            "Stop1"));
    }

    @Test
    void getRoutesByStop_ReturnListTravels() throws Exception {
        when(routeService.getRoutesByStop("Stop1", TravelSortOption.TIME_IN_ROUTE))
            .thenReturn(List.of(travel1));

        MvcResult result = mockMvc.perform(
                get("/api/v1/route")
                    .param("stopName", "Stop1")
                    .param("sort", "TIME_IN_ROUTE"))
            .andExpect(status().isOk())
            .andReturn();

        List<TravelDto> response = objectMapper.readValue(
            result.getResponse().getContentAsString(),
            new TypeReference<>() {
            }
        );

        assertThat(response).hasSize(1);
        assertThat(response.get(0).timeInRoute()).isEqualTo(travel1.getTimeInRoute());
        assertThat(response.get(0).nextArrival()).isEqualTo(travel1.getNextArrival());
        assertThat(response.get(0).route().name()).isEqualTo(route1.getName());
        assertThat(response.get(0).route().stops()).hasSize(2);
    }

    @Test
    void getRoutesByStops_ReturnListTravels() throws Exception {
        when(routeService.getRoutesByStops("Stop1", "Stop2", TravelSortOption.NEAREST_ARRIVAL))
            .thenReturn(List.of(travel1));

        MvcResult result = mockMvc.perform(
                get("/api/v1/route")
                    .param("fromStopName", "Stop1")
                    .param("toStopName", "Stop2")
                    .param("sort", "NEAREST_ARRIVAL"))
            .andExpect(status().isOk())
            .andReturn();

        List<TravelDto> response = objectMapper.readValue(
            result.getResponse().getContentAsString(),
            new TypeReference<>() {
            }
        );

        assertThat(response).hasSize(1);
        assertThat(response.get(0).route().name()).isEqualTo(route1.getName());
        assertThat(response.get(0).route().stops().get(0).stopName()).isEqualTo("Stop1");
        assertThat(response.get(0).route().stops().get(1).stopName()).isEqualTo("Stop2");
    }

    @Test
    void getRouteByName_ReturnRoute() throws Exception {
        when(routeService.getRouteByName("route1")).thenReturn(route1);

        MvcResult result = mockMvc.perform(get("/api/v1/route/route1"))
            .andExpect(status().isOk())
            .andReturn();

        SummaryRouteDto response = objectMapper.readValue(
            result.getResponse().getContentAsString(),
            SummaryRouteDto.class
        );

        assertThat(response.name()).isEqualTo(route1.getName());
        assertThat(response.type()).isEqualTo(route1.getType());
        assertThat(response.interval()).isEqualTo(route1.getInterval());
        assertThat(response.businessHours().startAt()).isEqualTo(
            route1.getBusinessHours().getStartAt());
        assertThat(response.stops()).hasSize(2);
        assertThat(response.stops().get(0).stopName()).isEqualTo("Stop1");
    }

    @Test
    void updateRouteByName_ReturnUpdatedRoute() throws Exception {
        Route updatedRoute = Route.valueOf("route1", "trolleybus", route1.getStops(),
            route1.getInterval(), route1.getBusinessHours());

        when(routeService.updateRouteByName(eq("route1"), any(FullUpdateRouteCommand.class)))
            .thenReturn(updatedRoute);

        FullUpdateRouteDto updateDto = new FullUpdateRouteDto(
            "route1", "trolleybus", Duration.ofMinutes(10), BUSINESS_HOURS_DTO, ROUTE_STOP_DTOS);

        MvcResult result = mockMvc.perform(put("/api/v1/route/route1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDto)))
            .andExpect(status().isCreated())
            .andReturn();

        SummaryRouteDto response = objectMapper.readValue(
            result.getResponse().getContentAsString(),
            SummaryRouteDto.class
        );

        assertThat(response.name()).isEqualTo(updateDto.name());
        assertThat(response.type()).isEqualTo(updateDto.type());
        assertThat(response.interval()).isEqualTo(updateDto.interval());
        assertThat(response.stops()).hasSize(
            (int) StreamSupport.stream(updateDto.stops().spliterator()
                , false).count());
    }

    @Test
    void deleteRoute_ReturnNoContent() throws Exception {
        doNothing().when(routeService).deleteRoute("route1");

        mockMvc.perform(delete("/api/v1/route/route1"))
            .andExpect(status().isNoContent());

        verify(routeService, times(1)).deleteRoute("route1");
    }
}
