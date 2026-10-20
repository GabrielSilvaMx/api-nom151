package mx.com.nimbusti.api.nom151.integration.controller;

import mx.com.nimbusti.api.nom151.integration.dto.request.NomHashRequestDto;
import mx.com.nimbusti.api.nom151.integration.dto.request.ValidateNomHashRequestDto;
import mx.com.nimbusti.api.nom151.integration.dto.response.ValidateTsaHashResponseDto;
import mx.com.nimbusti.api.nom151.integration.service.Nom151Service;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/nom151")
@RequiredArgsConstructor
public class Nom151Controller {

    private final Nom151Service nom151Service;

    @PostMapping("/issue")
    public ResponseEntity<?> createNom151Certificate(@Valid @RequestBody NomHashRequestDto request) {
        var response = nom151Service.requestCertificate(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("/validate")
    public ResponseEntity<ValidateTsaHashResponseDto> validateNom151Certificate(@Valid @RequestBody ValidateNomHashRequestDto request) {
        var response = nom151Service.validateCertificate(request);
        return ResponseEntity.ok(response);
    }
}