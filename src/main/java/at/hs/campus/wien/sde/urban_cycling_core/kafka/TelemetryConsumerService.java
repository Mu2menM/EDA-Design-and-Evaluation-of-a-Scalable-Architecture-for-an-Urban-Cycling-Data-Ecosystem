package at.hs.campus.wien.sde.urban_cycling_core.kafka;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.MDC;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import at.hs.campus.wien.sde.urban_cycling_core.dto.TelemetryRequest;
import at.hs.campus.wien.sde.urban_cycling_core.model.Ride;
import at.hs.campus.wien.sde.urban_cycling_core.model.User;
import at.hs.campus.wien.sde.urban_cycling_core.model.Waypoint;
import at.hs.campus.wien.sde.urban_cycling_core.repository.RideRepository;
import at.hs.campus.wien.sde.urban_cycling_core.repository.UserRepository;
import at.hs.campus.wien.sde.urban_cycling_core.repository.WaypointRepository;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class TelemetryConsumerService {

  private final UserRepository userRepository;
  private final RideRepository rideRepository;
  private final WaypointRepository waypointRepository;
  private final MeterRegistry meterRegistry;

  private Counter telemetryPersistedCounter;
  private Timer persistenceTimer;
  private Timer endToEndLatencyTimer;

  @PostConstruct
  private void initMetrics() {
    // Initialize counters and timers at startup (not lazy)
    telemetryPersistedCounter = Counter.builder("telemetry.persisted")
        .description("Number of telemetry points persisted to DB")
        .register(meterRegistry);

    persistenceTimer = Timer.builder("telemetry.persistence.time")
        .description("Time taken to persist a telemetry point to DB")
        .publishPercentiles(0.5, 0.95, 0.99)
        .publishPercentileHistogram()
        .sla(Duration.ofMillis(10), Duration.ofMillis(25), Duration.ofMillis(50),
            Duration.ofMillis(100), Duration.ofMillis(200))
        .register(meterRegistry);

    endToEndLatencyTimer = Timer.builder("telemetry.end.to.end.latency")
        .description("Time from request receipt to persistence (SC-05)")
        .publishPercentiles(0.5, 0.95, 0.99)
        .publishPercentileHistogram()
        .sla(Duration.ofMillis(50), Duration.ofMillis(100), Duration.ofMillis(200),
            Duration.ofMillis(500), Duration.ofSeconds(1), Duration.ofSeconds(2))
        .register(meterRegistry);
  }

  @KafkaListener(topics = "${kafka.topic}", groupId = "${spring.kafka.consumer.group-id}")
  @Transactional
  public void consume(ConsumerRecord<String, TelemetryRequest> record) {
    Timer.Sample sample = Timer.start(meterRegistry);

    TelemetryRequest request = record.value();

    // Extract reception time from header
    long receptionTime = -1;
    if (record.headers().lastHeader("reception-time") != null) {
      receptionTime = Long.parseLong(new String(record.headers().lastHeader("reception-time").value()));
    }

    // Add MDC for logging
    MDC.put("userId", request.getUserId().toString());
    MDC.put("timestamp", request.getTimestamp().toString());

    try {
      // Find or create ride
      User user = userRepository.findById(request.getUserId())
          .orElseThrow(() -> new RuntimeException("User not found: " + request.getUserId()));

      Ride ride = rideRepository.findLastOpenRideByUser(user.getUserId())
          .orElseGet(() -> createNewRide(user, request.getTimestamp()));

      Waypoint waypoint = Waypoint.builder()
          .ride(ride)
          .latitude(request.getLatitude())
          .longitude(request.getLongitude())
          .timestamp(request.getTimestamp())
          .build();
      waypointRepository.save(waypoint);
      ride.getWaypoints().add(waypoint);
      rideRepository.save(ride);

      // Record end-to-end latency
      if (receptionTime > 0) {
        long now = System.currentTimeMillis();
        long latencyMs = now - receptionTime;
        endToEndLatencyTimer.record(latencyMs, TimeUnit.MILLISECONDS);
      }

      // Record persistence time
      sample.stop(persistenceTimer);

      // Increment counter
      telemetryPersistedCounter.increment();

      // Log structured info
      log.info("Telemetry persisted successfully. rideId: {}, waypointId: {}, latencyMs: {}",
          ride.getRideId(), waypoint.getId(),
          receptionTime > 0 ? (System.currentTimeMillis() - receptionTime) : "N/A");

    } catch (Exception e) {
      log.error("Failed to process telemetry", e);
      throw e;
    } finally {
      MDC.clear();
    }
  }

  private Ride createNewRide(User user, LocalDateTime timestamp) {
    Ride ride = Ride.builder()
        .user(user)
        .startTime(timestamp)
        .completed(false)
        .build();
    return rideRepository.save(ride);
  }
}