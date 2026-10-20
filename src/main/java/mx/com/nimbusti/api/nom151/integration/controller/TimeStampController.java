package mx.com.nimbusti.api.nom151.integration.controller;

import mx.com.nimbusti.api.nom151.integration.dto.request.*;
import mx.com.nimbusti.api.nom151.integration.dto.response.*;
import mx.com.nimbusti.api.nom151.integration.service.TimeStampService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/timestamps")
@RequiredArgsConstructor
public class TimeStampController {

    private final TimeStampService timeStampService;

    @PostMapping(value = "/stamp-file", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<TsaHashResponseDto> stampDocument(@RequestParam("file") MultipartFile file) {
        TsaHashResponseDto response = timeStampService.createTimeStampFromDocument(file);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("/stamp-hash")
    public ResponseEntity<TsaHashResponseDto> stampHash(@Valid @RequestBody TsaHashRequestDto request) {
        TsaHashResponseDto response = timeStampService.createTimeStampFromHash(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("/retrieve")
    public ResponseEntity<GetTsaHashResponseDto> retrieveByUuid(@Valid @RequestBody GetTsaHashRequestDto request) {
        GetTsaHashResponseDto response = timeStampService.retrieveTimeStamp(request.uuid());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/validate")
    public ResponseEntity<ValidateTsaHashResponseDto> validate(@Valid @RequestBody ValidateTsaHashRequestDto request) {
        ValidateTsaHashResponseDto response = timeStampService.validateTimeStamp(request);
        return ResponseEntity.ok(response);
    }
}