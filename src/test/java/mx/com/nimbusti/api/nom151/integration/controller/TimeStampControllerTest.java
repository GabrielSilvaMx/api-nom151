package mx.com.nimbusti.api.nom151.integration.controller;

import mx.com.nimbusti.api.nom151.integration.dto.request.TsaHashRequestDto;
import mx.com.nimbusti.api.nom151.integration.dto.response.TsaHashResponseDto;
import mx.com.nimbusti.api.nom151.integration.service.TimeStampService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TimeStampController.class)
class TimeStampControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TimeStampService timeStampService;

    @Test
    @DisplayName("POST /api/v1/timestamps/stamp-hash debe retornar status 201 CREATED con un payload válido")
    void shouldReturn201ForValidStampHashRequest() throws Exception {
        // Arrange
        String validHash = "741e4284fc27be60854ab665f34b4345c1ae38e4822d056b7521f684661e3a6a";[cite: 1, 3]
        TsaHashRequestDto request = new TsaHashRequestDto(validHash);
        TsaHashResponseDto mockResponse = new TsaHashResponseDto(true, validHash, "BASE64_TOKEN", "6e05ccc4-5b79-479c-9d89-774a05a4f0e1");[cite: 1, 3]

        when(timeStampService.createTimeStampFromHash(any(TsaHashRequestDto.class))).thenReturn(mockResponse);

        // Act & Assert
        mockMvc.perform(post("/api/v1/timestamps/stamp-hash")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(true))
                .andExpect(jsonPath("$.uuid").value("6e05ccc4-5b79-479c-9d89-774a05a4f0e1"))[cite: 1, 3]
                .andExpect(jsonPath("$.hash-processed").value(validHash));[cite: 1, 3]
    }

    @Test
    @DisplayName("POST /api/v1/timestamps/stamp-hash debe retornar 400 BAD REQUEST cuando el hash no cumple la expresión regular")
    void shouldReturn400ForInvalidHashFormat() throws Exception {
        // Arrange - Hash de 10 caracteres
        TsaHashRequestDto request = new TsaHashRequestDto("invalid123");

        // Act & Assert
        mockMvc.perform(post("/api/v1/timestamps/stamp-hash")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}