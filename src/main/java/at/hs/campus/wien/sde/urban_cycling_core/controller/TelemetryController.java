package at.hs.campus.wien.sde.urban_cycling_core.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import at.hs.campus.wien.sde.urban_cycling_core.dto.TelemetryRequest;
import at.hs.campus.wien.sde.urban_cycling_core.dto.TelemetryResponse;
import at.hs.campus.wien.sde.urban_cycling_core.kafka.TelemetryProducerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class TelemetryController {

  private final TelemetryProducerService producerService;

  @PostMapping("/telemetry")
  public ResponseEntity<TelemetryResponse> ingestTelemetry(
      @Valid @RequestBody TelemetryRequest request) {

    producerService.publish(request);

    TelemetryResponse response = TelemetryResponse.builder()
        .message("Telemetry queued for processing")
        .status("QUEUED")
        .build();
    return new ResponseEntity<>(response, HttpStatus.ACCEPTED);
  }
}
