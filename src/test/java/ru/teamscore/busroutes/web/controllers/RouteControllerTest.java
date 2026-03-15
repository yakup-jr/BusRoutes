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
import java.util.UUID;
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

    private static final CreateBusinessHoursDto CREATE_BUSINESS_HOURS_DTO =
        CreateBusinessHoursDto.builder().startAt(LocalTime.of(6, 0)).endAt(LocalTime.of(23, 0))
            .build();
    private static final BusinessHoursDto BUSINESS_HOURS_DTO =
        BusinessHoursDto.builder().id(UUID.randomUUID()).startAt(LocalTime.of(5, 30)).endAt(LocalTime.of(23, 0)).build();
    private static final List<SummaryRouteStopDto> ROUTE_STOP_DTOS = List.of(
        SummaryRouteStopDto.builder().id(UUID.randomUUID()).arriveAtFromStart(0).stopOrder(1)
            .stopName("Stop1").build(),
        SummaryRouteStopDto.builder().id(UUID.randomUUID()).arriveAtFromStart(120).stopOrder(2)
            .stopName("Stop2").build());
    private static final SummaryRouteDto SUMMARY_ROUTE_DTO =
        SummaryRouteDto.builder().name("route1").type("bus").interval(Duration.ofMinutes(10))
            .businessHours(BUSINESS_HOURS_DTO).stops(ROUTE_STOP_DTOS).build();
    private static final CreateRouteDto CREATE_ROUTE_DTO =
        new CreateRouteDto("route1", "bus", Duration.ofMinutes(10), CREATE_BUSINESS_HOURS_DTO,
            ROUTE_STOP_DTOS);

    @BeforeEach
    void setUp() {
        Stop s1 = Stop.valueOf(UUID.randomUUID(), "Stop1",
            Stop.GeographicCoordinates.valueOf(53.198050, 50.108750));
        Stop s2 = Stop.valueOf(UUID.randomUUID(), "Stop2",
            Stop.GeographicCoordinates.valueOf(53.195873, 50.104954));

        List<RouteStop> stopsRoute1 = List.of(RouteStop.valueOf(UUID.randomUUID(), 0, 1, s1),
            RouteStop.valueOf(UUID.randomUUID(), 120, 2, s2));

        BusinessHours businessHours =
            BusinessHours.valueOf(UUID.randomUUID(), BUSINESS_HOURS_DTO.startAt(),
                BUSINESS_HOURS_DTO.endAt());

        route1 =
            Route.valueOf(UUID.randomUUID(), "route1", "bus", stopsRoute1, Duration.ofMinutes(10),
                businessHours);
        travel1 = Travel.valueOf(route1, Duration.ofMinutes(10), LocalTime.of(6, 0));
    }

    @Test
    void addRoute_ReturnCreated() throws Exception {
        when(routeService.addRoute(any(CreateRouteCommand.class))).thenReturn(route1);

        MvcResult mvcResult = mockMvc.perform(
                post("/api/v1/route").contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(CREATE_ROUTE_DTO)))
            .andExpect(status().isCreated()).andReturn();

        SummaryRouteDto routeDto =
            objectMapper.readValue(mvcResult.getResponse().getContentAsString(),
                SummaryRouteDto.class);
        assertThat(routeDto.name()).isEqualTo(CREATE_ROUTE_DTO.name());
        assertThat(routeDto.type()).isEqualTo(CREATE_ROUTE_DTO.type());
        assertThat(routeDto.businessHours()).usingRecursiveComparison().ignoringFields("id")
            .isEqualTo(BUSINESS_HOURS_DTO);
        assertThat(routeDto.interval()).isEqualTo(CREATE_ROUTE_DTO.interval());
        assertThat(routeDto.stops()).usingRecursiveComparison().ignoringFields("id")
            .isEqualTo(ROUTE_STOP_DTOS);
    }

    @Test
    void copyRoute_ReturnCreated() throws Exception {
        Route reversedRoute = route1.copy().reverseRoute();
        when(routeService.copyRoute(any(UUID.class), eq(true))).thenReturn(reversedRoute);

        MvcResult mvcResult = mockMvc.perform(
                post(String.format("/api/v1/route/%s/copy", route1.getId().toString())).param(
                    "isReverseOrder", "true").contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isCreated()).andReturn();

        SummaryRouteDto responseRouteDto =
            objectMapper.readValue(mvcResult.getResponse().getContentAsString(),
                SummaryRouteDto.class);
        assertThat(responseRouteDto.name()).isEqualTo(
            String.format("%s_copy", SUMMARY_ROUTE_DTO.name()));
        assertThat(responseRouteDto.type()).isEqualTo("bus");
        assertThat(responseRouteDto.businessHours()).usingRecursiveComparison().ignoringFields("id")
            .isEqualTo(SUMMARY_ROUTE_DTO.businessHours());
        assertThat(responseRouteDto.interval()).isEqualTo(SUMMARY_ROUTE_DTO.interval());
        assertThat(responseRouteDto.stops().get(0)).isEqualTo(
            SummaryRouteStopDto.builder().arriveAtFromStart(0).stopOrder(1).stopName("Stop2")
                .build());
        assertThat(responseRouteDto.stops().get(1)).isEqualTo(
            SummaryRouteStopDto.builder().arriveAtFromStart(120).stopOrder(2).stopName("Stop1")
                .build());
    }

    @Test
    void getRoutesByStop_ReturnListTravels() throws Exception {
        when(routeService.getRoutesByStop("Stop1", TravelSortOption.TIME_IN_ROUTE)).thenReturn(
            List.of(travel1));

        MvcResult result = mockMvc.perform(
                get("/api/v1/route").param("stopName", "Stop1").param("sort", "TIME_IN_ROUTE"))
            .andExpect(status().isOk()).andReturn();

        List<TravelDto> response = objectMapper.readValue(result.getResponse().getContentAsString(),
            new TypeReference<>() {
            });

        assertThat(response).hasSize(1);
        assertThat(response.get(0).timeInRoute()).isEqualTo(travel1.getTimeInRoute());
        assertThat(response.get(0).nextArrival()).isEqualTo(travel1.getNextArrival());
        assertThat(response.get(0).route().name()).isEqualTo(route1.getName());
        assertThat(response.get(0).route().stops()).hasSize(2);
    }

    @Test
    void getRoutesByStops_ReturnListTravels() throws Exception {
        when(routeService.getRoutesByStops("Stop1", "Stop2",
            TravelSortOption.NEAREST_ARRIVAL)).thenReturn(List.of(travel1));

        MvcResult result = mockMvc.perform(
            get("/api/v1/route").param("fromStopName", "Stop1").param("toStopName", "Stop2")
                .param("sort", "NEAREST_ARRIVAL")).andExpect(status().isOk()).andReturn();

        List<TravelDto> response = objectMapper.readValue(result.getResponse().getContentAsString(),
            new TypeReference<>() {
            });

        assertThat(response).hasSize(1);
        assertThat(response.get(0).route().name()).isEqualTo(route1.getName());
        assertThat(response.get(0).route().stops().get(0).stopName()).isEqualTo("Stop1");
        assertThat(response.get(0).route().stops().get(1).stopName()).isEqualTo("Stop2");
    }

    @Test
    void getRouteByName_ReturnRoute() throws Exception {
        when(routeService.getRouteByName("route1")).thenReturn(List.of(route1));

        MvcResult result =
            mockMvc.perform(get("/api/v1/route/route1")).andExpect(status().isOk()).andReturn();

        List<SummaryRouteDto> response =
            objectMapper.readValue(result.getResponse().getContentAsString(),
                new TypeReference<>() {
                });

        assertThat(response.get(0).name()).isEqualTo(route1.getName());
        assertThat(response.get(0).type()).isEqualTo(route1.getType());
        assertThat(response.get(0).interval()).isEqualTo(route1.getInterval());
        assertThat(response.get(0).businessHours().startAt()).isEqualTo(
            route1.getBusinessHours().getStartAt());
        assertThat(response.get(0).stops()).hasSize(2);
        assertThat(response.get(0).stops().get(0).stopName()).isEqualTo("Stop1");
    }

    @Test
    void updateRouteByName_ReturnUpdatedRoute() throws Exception {
        Route updatedRoute =
            Route.valueOf(UUID.randomUUID(), "route1", "trolleybus", route1.getStops(),
                route1.getInterval(), route1.getBusinessHours());

        when(routeService.updateRoute(eq(updatedRoute.getId()),
            any(FullUpdateRouteCommand.class))).thenReturn(updatedRoute);

        FullUpdateRouteDto updateDto =
            new FullUpdateRouteDto("route1", "trolleybus",
                Duration.ofMinutes(10),
                BUSINESS_HOURS_DTO, ROUTE_STOP_DTOS);

        MvcResult result = mockMvc.perform(
                put(String.format("/api/v1/route/%s", updatedRoute.getId())).contentType(
                        MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(updateDto)))
            .andExpect(status().isCreated()).andReturn();

        SummaryRouteDto response = objectMapper.readValue(result.getResponse().getContentAsString(),
            SummaryRouteDto.class);

        assertThat(response.name()).isEqualTo(updateDto.name());
        assertThat(response.type()).isEqualTo(updateDto.type());
        assertThat(response.interval()).isEqualTo(updateDto.interval());
        assertThat(response.stops()).hasSize(
            (int) StreamSupport.stream(updateDto.stops().spliterator(), false).count());
    }

    @Test
    void deleteRoute_ReturnNoContent() throws Exception {
        doNothing().when(routeService).deleteRoute("route1");

        mockMvc.perform(delete("/api/v1/route/route1")).andExpect(status().isNoContent());

        verify(routeService, times(1)).deleteRoute("route1");
    }
}
