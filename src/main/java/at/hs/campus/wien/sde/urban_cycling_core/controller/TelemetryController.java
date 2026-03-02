package at.hs.campus.wien.sde.urban_cycling_core.controller;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import at.hs.campus.wien.sde.urban_cycling_core.dto.BatchTelemetryRequest;
import at.hs.campus.wien.sde.urban_cycling_core.dto.BatchTelemetryResponse;
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

  @PostMapping("/telemetry/batch")
  public ResponseEntity<BatchTelemetryResponse> ingestBatchTelemetry(
      @Valid @RequestBody BatchTelemetryRequest batchRequest) {

    List<TelemetryRequest> points = batchRequest.getPoints();

    log.info("Received batch of {} telemetry points", points.size());

    // Publish each point to Kafka
    producerService.publishBatch(points);

    BatchTelemetryResponse response = BatchTelemetryResponse.builder()
        .totalPoints(points.size())
        .acceptedPoints(points.size())
        .message("Batch telemetry queued for processing")
        .status("BATCH_QUEUED")
        .build();

    return new ResponseEntity<>(response, HttpStatus.ACCEPTED);
  }
}
