package ru.teamscore.busroutes.web.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.teamscore.busroutes.config.test.IntegrationTest;
import ru.teamscore.busroutes.web.dtos.stops.CreateStopDto;
import ru.teamscore.busroutes.web.dtos.stops.FullUpdateStopDto;
import ru.teamscore.busroutes.web.dtos.stops.GeographicCoordinatesDto;
import tools.jackson.databind.ObjectMapper;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@IntegrationTest
@AutoConfigureMockMvc
class StopControllerIntegrationTest {

    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private MockMvc mockMvc;

    @Test
    void addStop() throws Exception {
        CreateStopDto createStopDto =
            CreateStopDto.builder()
                .name("NewStop")
                .coordinates(
                    GeographicCoordinatesDto.builder()
                        .latitude(53.198050)
                        .longitude(50.108750).build())
                .build();

        mockMvc.perform(post("/api/v1/stop").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createStopDto)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.name").value("NewStop"))
            .andExpect(jsonPath("$.coordinates.latitude").value(53.198050))
            .andExpect(jsonPath("$.coordinates.longitude").value(50.108750));
    }

    @Test
    void getStopByName_ReturnStop() throws Exception {
        mockMvc.perform(get("/api/v1/stop/Stop1")).andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("Stop1"))
            .andExpect(jsonPath("$.coordinates.latitude").value(55.7558))
            .andExpect(jsonPath("$.coordinates.longitude").value(37.6173));
    }

    @Test
    void getStopByName_ReturnNotFound() throws Exception {
        mockMvc.perform(get("/api/v1/stop/StopNonExists")).andExpect(status().isNotFound())
            .andExpect(jsonPath("$").exists());
    }

    @Test
    void updateStopByName_ReturnUpdatedStop() throws Exception {
        FullUpdateStopDto fullUpdateStopDto = FullUpdateStopDto.builder()
            .name("UpdatedStop1")
            .coordinates(
                GeographicCoordinatesDto.builder()
                    .latitude(53.198060)
                    .longitude(50.108760).build())
            .build();

        mockMvc.perform(put("/api/v1/stop/Stop1").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(fullUpdateStopDto)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.name").value("UpdatedStop1"))
            .andExpect(jsonPath("$.coordinates.latitude").value(53.198060))
            .andExpect(jsonPath("$.coordinates.longitude").value(50.108760));
    }

    @Test
    void updateStopByName_ReturnNotFound() throws Exception {
        FullUpdateStopDto fullUpdateStopDto =
            FullUpdateStopDto.builder()
                .name("UpdatedStop1")
                .coordinates(
                    GeographicCoordinatesDto.builder()
                        .latitude(53.198060)
                        .longitude(50.108760).build())
                .build();

        mockMvc.perform(put("/api/v1/stop/NonExistingStop").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(fullUpdateStopDto)))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$").exists());
    }

    @Test
    void deleteStopByName_ReturnNoContent() throws Exception {
        CreateStopDto createStopDto =
            CreateStopDto.builder()
                .name("NewStop")
                .coordinates(
                    GeographicCoordinatesDto.builder()
                        .latitude(53.198050)
                        .longitude(50.108750).build())
                .build();

        mockMvc.perform(post("/api/v1/stop").contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(createStopDto)));

        mockMvc.perform(delete("/api/v1/stop/NewStop")).andExpect(status().isNoContent());
    }

    @Test
    void deleteStopByName_StopInRoute_Return400() throws Exception {
        mockMvc.perform(delete("/api/v1/stop/Stop1")).andExpect(status().isConflict());
    }

    @Test
    void deleteStopByName_ReturnNotFound() throws Exception {
        mockMvc.perform(delete("/api/v1/stop/NonExistingStop")).andExpect(status().isNotFound())
            .andExpect(jsonPath("$").exists());
    }
}
