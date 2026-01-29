package ru.teamscore.busroutes.web.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.teamscore.busroutes.model.commands.CreateStopCommand;
import ru.teamscore.busroutes.model.commands.FullUpdateStopCommand;
import ru.teamscore.busroutes.model.enums.ItemType;
import ru.teamscore.busroutes.model.exceptions.NotFoundException;
import ru.teamscore.busroutes.model.models.Stop;
import ru.teamscore.busroutes.model.services.StopService;
import ru.teamscore.busroutes.web.dtos.stops.CreateStopDto;
import ru.teamscore.busroutes.web.dtos.stops.FullUpdateStopDto;
import ru.teamscore.busroutes.web.dtos.stops.GeographicCoordinatesDto;
import ru.teamscore.busroutes.web.mappers.StopDtoMapper;
import ru.teamscore.busroutes.web.mappers.StopDtoMapperImpl;
import tools.jackson.databind.ObjectMapper;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(StopController.class)
@Import({StopDtoMapperImpl.class})
class StopControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private StopDtoMapper mapper;

    @MockitoBean
    private StopService stopService;

    @Test
    void addStop() throws Exception {
        CreateStopDto createStopDto =
            new CreateStopDto("Stop1", new GeographicCoordinatesDto(53.198050, 50.108750));
        Stop stop = Stop.valueOf("Stop1", Stop.GeographicCoordinates.valueOf(53.198050, 50.108750));

        when(stopService.addStop(any(CreateStopCommand.class))).thenReturn(stop);

        mockMvc.perform(post("/api/v1/stop").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createStopDto)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.name").value("Stop1"))
            .andExpect(jsonPath("$.coordinates.latitude").value(53.198050))
            .andExpect(jsonPath("$.coordinates.longitude").value(50.108750));
    }

    @Test
    void getStopByName_ReturnStop() throws Exception {
        Stop stop = Stop.valueOf("Stop1", Stop.GeographicCoordinates.valueOf(53.198050, 50.108750));

        when(stopService.getStopByName(any(String.class))).thenReturn(stop);

        mockMvc.perform(get("/api/v1/stop/Stop1")).andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("Stop1"))
            .andExpect(jsonPath("$.coordinates.latitude").value(53.198050))
            .andExpect(jsonPath("$.coordinates.longitude").value(50.108750));
    }

    @Test
    void getStopByName_ReturnNotFound() throws Exception {
        when(stopService.getStopByName(any(String.class))).thenThrow(
            new NotFoundException("StopNonExists", ItemType.STOP));

        mockMvc.perform(get("/api/v1/stop/StopNonExists")).andExpect(status().isNotFound())
            .andExpect(jsonPath("$").exists());
    }

    @Test
    void updateStopByName_ReturnUpdatedStop() throws Exception {
        FullUpdateStopDto fullUpdateStopDto = new FullUpdateStopDto("UpdatedStop1",
            new GeographicCoordinatesDto(53.198060, 50.108760));
        FullUpdateStopCommand fullUpdateStopCommand = new FullUpdateStopCommand("UpdatedStop1",
            new FullUpdateStopCommand.FullUpdateGeographicCommand(53.198060, 50.108760));
        Stop stop = Stop.valueOf("UpdatedStop1", Stop.GeographicCoordinates.valueOf(53.198060,
            50.108760));

        when(stopService.updateStopByName("Stop1", fullUpdateStopCommand)).thenReturn(stop);

        mockMvc.perform(put("/api/v1/stop/Stop1").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(fullUpdateStopDto))).andExpect(status().isCreated())
            .andExpect(jsonPath("$.name").value("UpdatedStop1"))
            .andExpect(jsonPath("$.coordinates.latitude").value(53.198060))
            .andExpect(jsonPath("$.coordinates.longitude").value(50.108760));
    }

    @Test
    void updateStopByName_ReturnNotFound() throws Exception {
        FullUpdateStopDto fullUpdateStopDto =
            new FullUpdateStopDto("UpdatedStop1", new GeographicCoordinatesDto(53.198060,
                50.108760));
        FullUpdateStopCommand fullUpdateStopCommand = new FullUpdateStopCommand("UpdatedStop1",
            new FullUpdateStopCommand.FullUpdateGeographicCommand(53.198060, 50.108760));

        when(stopService.updateStopByName("NonExistingStop", fullUpdateStopCommand)).thenThrow(
            new NotFoundException("NonExistingStop", ItemType.STOP));

        mockMvc.perform(put("/api/v1/stop/NonExistingStop").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(fullUpdateStopDto)))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$").exists());
    }

    @Test
    void deleteStopByName_ReturnNoContent() throws Exception {
        mockMvc.perform(delete("/api/v1/stop/Stop1")).andExpect(status().isNoContent());
    }

    @Test
    void deleteStopByName_ReturnNotFound() throws Exception {
        doThrow(new NotFoundException("NonExistingStop", ItemType.STOP)).when(stopService)
            .deleteStopByName("NonExistingStop");

        mockMvc.perform(delete("/api/v1/stop/NonExistingStop")).andExpect(status().isNotFound())
            .andExpect(jsonPath("$").exists());
    }
}
