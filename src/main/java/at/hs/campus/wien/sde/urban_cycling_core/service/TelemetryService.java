package at.hs.campus.wien.sde.urban_cycling_core.service;

import java.time.LocalDateTime;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import at.hs.campus.wien.sde.urban_cycling_core.dto.TelemetryRequest;
import at.hs.campus.wien.sde.urban_cycling_core.dto.TelemetryResponse;
import at.hs.campus.wien.sde.urban_cycling_core.model.Ride;
import at.hs.campus.wien.sde.urban_cycling_core.model.User;
import at.hs.campus.wien.sde.urban_cycling_core.model.Waypoint;
import at.hs.campus.wien.sde.urban_cycling_core.repository.RideRepository;
import at.hs.campus.wien.sde.urban_cycling_core.repository.UserRepository;
import at.hs.campus.wien.sde.urban_cycling_core.repository.WaypointRepository;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TelemetryService {
  private final RideRepository rideRepository;
  private final WaypointRepository waypointRepository;
  private final MeterRegistry meterRegistry;  // For MC-04 metrics
  private final UserRepository userRepository;

  private Counter telemetryCounter;  // MC-04

  @Transactional
  public TelemetryResponse ingestTelemetry(TelemetryRequest request) {
    User user = userRepository.getReferenceById(request.getUserId());
    Ride ride = rideRepository.findLastOpenRideByUser(user.getUserId()).orElse(createNewRide(user, request.getTimestamp()));
    Waypoint waypoint = createAndSaveWaypoint(request, ride);
    ride.getWaypoints().add(waypoint);
    rideRepository.save(ride);

    // Increment metrics counter (MC-04)
    if (telemetryCounter == null) {
      telemetryCounter = Counter.builder("telemetry.ingested")
          .description("Number of telemetry points ingested")
          .register(meterRegistry);
    }
    telemetryCounter.increment();

    return TelemetryResponse.builder()
        .rideId(ride.getRideId())
        .waypointId(waypoint.getId())
        .message(ride.getWaypoints().size() == 1 ? "New ride started" : "Waypoint added to existing ride")
        .build();
  }

  private Waypoint createAndSaveWaypoint(TelemetryRequest request, Ride ride) {
    return waypointRepository.save(Waypoint.builder().ride(ride)
        .latitude(request.getLatitude())
        .longitude(request.getLongitude())
        .timestamp(request.getTimestamp())
        .build());
  }

  private Ride createNewRide(User user, LocalDateTime timestamp) {
    return Ride.builder()
        .user(user)
        .startTime(timestamp)
        .completed(false)
        .build();
  }
}