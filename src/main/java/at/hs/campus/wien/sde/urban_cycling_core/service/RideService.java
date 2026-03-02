package at.hs.campus.wien.sde.urban_cycling_core.service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import at.hs.campus.wien.sde.urban_cycling_core.dto.EndRideRequest;
import at.hs.campus.wien.sde.urban_cycling_core.dto.EndRideResponse;
import at.hs.campus.wien.sde.urban_cycling_core.dto.RideResponse;
import at.hs.campus.wien.sde.urban_cycling_core.dto.RideSummary;
import at.hs.campus.wien.sde.urban_cycling_core.dto.WaypointResponse;
import at.hs.campus.wien.sde.urban_cycling_core.model.Ride;
import at.hs.campus.wien.sde.urban_cycling_core.model.Waypoint;
import at.hs.campus.wien.sde.urban_cycling_core.repository.RideRepository;
import at.hs.campus.wien.sde.urban_cycling_core.util.GeoUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class RideService {

  private final RideRepository rideRepository;

  @Transactional(readOnly = true)
  public RideResponse getRide(UUID rideId) {
    Ride ride = rideRepository.findById(rideId)
        .orElseThrow(() -> new RuntimeException("Ride not found"));

    // Calculate calculatedRideMetrics on the fly (MC-07)
    Map<String, Double> calculatedRideMetrics = calculateRideMetrics(ride);

    List<WaypointResponse> waypointResponses = ride.getWaypoints().stream()
        .map(wp -> WaypointResponse.builder()
            .id(wp.getId())
            .latitude(wp.getLatitude())
            .longitude(wp.getLongitude())
            .timestamp(wp.getTimestamp())
            .build())
        .toList();

    return RideResponse.builder()
        .rideId(ride.getRideId())
        .userId(ride.getUser().getUserId())
        .startTime(ride.getStartTime())
        .endTime(ride.getEndTime())
        .avgSpeedKmh(calculatedRideMetrics.get("avgSpeed"))
        .totalDistanceKm(calculatedRideMetrics.get("distance"))
        .waypoints(waypointResponses)
        .build();
  }

  public List<RideSummary> getRidesByUser(UUID userId) {
    List<Ride> rides = rideRepository.findByUserUserId(userId);

    return rides.stream()
        .map(ride -> RideSummary.builder()
            .rideId(ride.getRideId())
            .startTime(ride.getStartTime())
            .endTime(ride.getEndTime())
            .completed(ride.getCompleted())
            .waypointCount(ride.getWaypoints().size())
            .build())
        .toList();
  }

  public EndRideResponse endRide(@Valid EndRideRequest endRideRequest) {
    String message;
    Ride ride = rideRepository.getReferenceById(endRideRequest.getRideId());

    if (Boolean.TRUE.equals(ride.getCompleted())) {
      message = "Ride is already completed";
    } else {
      Map<String, Double> calculatedRideMetrics = calculateRideMetrics(ride);
      ride.setCompleted(true);
      ride.setEndTime(LocalDateTime.now());
      ride.setAvgSpeedKmh(calculatedRideMetrics.get("avgSpeed"));
      ride.setTotalDistanceKm(calculatedRideMetrics.get("distance"));
      rideRepository.save(ride);
      message = "Ride has been successfully ended";
    }
    return EndRideResponse.builder()
        .rideId(ride.getRideId())
        .message(message)
        .avgSpeedKmh(ride.getAvgSpeedKmh())
        .totalDistanceKm(ride.getTotalDistanceKm())
        .totalWaypoints(ride.getWaypoints().size())
        .startTime(ride.getStartTime())
        .endTime(ride.getEndTime())
        .build();
  }

  // MC-07 calculation logic
  private Map<String, Double> calculateRideMetrics(Ride ride) {
    List<Waypoint> waypoints = ride.getWaypoints();
    if (waypoints.size() < 2) {
      return Map.of("distance", 0.0, "avgSpeed", 0.0);
    }

    double totalDistance = 0.0;
    for (int i = 1; i < waypoints.size(); i++) {
      Waypoint prev = waypoints.get(i - 1);
      Waypoint curr = waypoints.get(i);
      totalDistance += GeoUtils.haversine(
          prev.getLatitude(), prev.getLongitude(),
          curr.getLatitude(), curr.getLongitude()
      );
    }

    // Average speed = total distance / total time (in hours)
    Duration duration = Duration.between(waypoints.get(0).getTimestamp(), waypoints.get(waypoints.size() - 1).getTimestamp());
    double hours = duration.toSeconds() / 3600.0;
    double avgSpeed = (hours > 0) ? totalDistance / hours : 0.0;

    return Map.of("distance", totalDistance, "avgSpeed", avgSpeed);
  }
}