package ru.teamscore.busroutes.web.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import ru.teamscore.busroutes.config.test.IntegrationTest;
import ru.teamscore.busroutes.web.dtos.routes.*;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.time.Duration;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.StreamSupport;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@IntegrationTest
@AutoConfigureMockMvc
class RouteControllerIntegrationTest {

    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private MockMvc mockMvc;

    private static final BusinessHoursDto BUSINESS_HOURS_DTO =
        new BusinessHoursDto(LocalTime.of(5, 30), LocalTime.of(23, 0));
    private static final List<SummaryRouteStopDto> ROUTE_STOP_DTOS =
        List.of(new SummaryRouteStopDto(0, 1, "Stop1"), new SummaryRouteStopDto(120, 2, "Stop2"));
    private static final CreateRouteDto CREATE_ROUTE_DTO =
        new CreateRouteDto("route1", "bus", Duration.ofMinutes(10), BUSINESS_HOURS_DTO,
            ROUTE_STOP_DTOS);

    @Test
    void addRoute_ReturnCreated() throws Exception {
        MvcResult mvcResult = mockMvc.perform(
                post("/api/v1/route").contentType(MediaType.APPLICATION_JSON)
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
        MvcResult mvcResult = mockMvc.perform(
                post("/api/v1/route/Route1/copy").param("isReverseOrder", "true")
                    .contentType(MediaType.APPLICATION_JSON)).andExpect(status().isCreated())
            .andReturn();

        SummaryRouteDto responseRouteDto =
            objectMapper.readValue(mvcResult.getResponse().getContentAsString(),
                SummaryRouteDto.class);
        assertThat(responseRouteDto.name()).isEqualTo("Route1_copy");
        assertThat(responseRouteDto.type()).isEqualTo("Bus");
        assertThat(responseRouteDto.businessHours()).isEqualTo(
            new BusinessHoursDto(LocalTime.of(6, 0), LocalTime.of(23, 0)));
        assertThat(responseRouteDto.interval()).isEqualTo(Duration.ofMinutes(10));
        assertThat(responseRouteDto.stops()).isEqualTo(
            List.of(new SummaryRouteStopDto(0, 1, "Stop2"),
                new SummaryRouteStopDto(600, 2, "Stop1")));
    }

    @Test
    void getRoutesByStop_ReturnListTravels() throws Exception {
        MvcResult result = mockMvc.perform(
                get("/api/v1/route").param("stopName", "Stop10").param("sort", "TIME_IN_ROUTE"))
            .andExpect(status().isOk()).andReturn();

        List<TravelDto> response = objectMapper.readValue(result.getResponse().getContentAsString(),
            new TypeReference<>() {
            });

        assertThat(response).hasSize(1);
        assertThat(response.get(0).route().name()).isEqualTo("Route3");
        assertThat(response.get(0).timeInRoute()).isEqualTo(Duration.ofMinutes(5));
        assertThat(response.get(0).nextArrival()).isEqualTo(LocalTime.of(17, 10));
        assertThat(response.get(0).route().stops()).hasSize(2);
    }

    @Test
    void getRoutesByStops_ReturnListTravels() throws Exception {
        MvcResult result = mockMvc.perform(
            get("/api/v1/route").param("fromStopName", "Stop3").param("toStopName", "Stop4")
                .param("sort", "NEAREST_ARRIVAL")).andExpect(status().isOk()).andReturn();

        List<TravelDto> response = objectMapper.readValue(result.getResponse().getContentAsString(),
            new TypeReference<>() {
            });

        assertThat(response).hasSize(1);
        assertThat(response.get(0).route().name()).isEqualTo("Route2");
        assertThat(response.get(0).timeInRoute()).isEqualTo(Duration.ofMinutes(10));
        assertThat(response.get(0).nextArrival()).isEqualTo(LocalTime.of(17, 15));
        assertThat(response.get(0).route().stops().get(0).stopName()).isEqualTo("Stop3");
        assertThat(response.get(0).route().stops().get(1).stopName()).isEqualTo("Stop4");
    }

    @Test
    void getRouteByName_ReturnRoute() throws Exception {
        MvcResult result =
            mockMvc.perform(get("/api/v1/route/Route1")).andExpect(status().isOk()).andReturn();

        SummaryRouteDto response = objectMapper.readValue(result.getResponse().getContentAsString(),
            SummaryRouteDto.class);

        assertThat(response.name()).isEqualTo("Route1");
        assertThat(response.type()).isEqualTo("Bus");
        assertThat(response.interval()).isEqualTo(Duration.ofMinutes(10));
        assertThat(response.businessHours()).isEqualTo(
            new BusinessHoursDto(LocalTime.of(6, 0), LocalTime.of(23, 0)));
        assertThat(response.stops()).hasSize(2);
        assertThat(response.stops().get(0)).isEqualTo(new SummaryRouteStopDto(0, 1, "Stop1"));
        assertThat(response.stops().get(1)).isEqualTo(new SummaryRouteStopDto(600, 2, "Stop2"));
    }

    @Test
    void updateRouteByName_ReturnUpdatedRoute() throws Exception {
        FullUpdateRouteDto updateDto =
            new FullUpdateRouteDto("route1", "trolleybus", Duration.ofMinutes(10),
                BUSINESS_HOURS_DTO, ROUTE_STOP_DTOS);

        MvcResult result = mockMvc.perform(
                put("/api/v1/route/Route1").contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(updateDto))).andExpect(status().isCreated())
            .andReturn();

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
        mockMvc.perform(delete("/api/v1/route/Route1")).andExpect(status().isNoContent());
    }

    @Test
    void deleteRoute_Return404() throws Exception {
        mockMvc.perform(delete("/api/v1/route/NonExistingRoute")).andExpect(status().isNotFound());
    }
}
