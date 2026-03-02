package at.hs.campus.wien.sde.urban_cycling_core.service;

import java.time.Duration;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;

import at.hs.campus.wien.sde.urban_cycling_core.dto.UserAnalyticsResponse;
import at.hs.campus.wien.sde.urban_cycling_core.model.Ride;
import at.hs.campus.wien.sde.urban_cycling_core.repository.RideRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnalyticsService {

  private final RideRepository rideRepository;

  public UserAnalyticsResponse getUserAnalytics(UUID userId) {
    List<Ride> completedRides = rideRepository.findCompletedRidesByUser(userId);

    if (completedRides.isEmpty()) {
      return UserAnalyticsResponse.builder()
          .userId(userId)
          .totalRides(0)
          .totalDistanceKm(0.0)
          .avgDistancePerRideKm(0.0)
          .avgSpeedKmh(0.0)
          .maxRideDistanceKm(0.0)
          .totalRideTimeMinutes(0)
          .build();
    }

    double totalDistance = completedRides.stream()
        .mapToDouble(r -> r.getTotalDistanceKm() != null ? r.getTotalDistanceKm() : 0.0)
        .sum();

    double totalSpeed = completedRides.stream()
        .mapToDouble(r -> r.getAvgSpeedKmh() != null ? r.getAvgSpeedKmh() : 0.0)
        .sum();

    double maxDistance = completedRides.stream()
        .mapToDouble(r -> r.getTotalDistanceKm() != null ? r.getTotalDistanceKm() : 0.0)
        .max()
        .orElse(0.0);

    long totalMinutes = completedRides.stream()
        .filter(r -> r.getStartTime() != null && r.getEndTime() != null)
        .mapToLong(r -> Duration.between(r.getStartTime(), r.getEndTime()).toMinutes())
        .sum();

    return UserAnalyticsResponse.builder()
        .userId(userId)
        .totalRides(completedRides.size())
        .totalDistanceKm(totalDistance)
        .avgDistancePerRideKm(totalDistance / completedRides.size())
        .avgSpeedKmh(totalSpeed / completedRides.size())
        .maxRideDistanceKm(maxDistance)
        .totalRideTimeMinutes(totalMinutes)
        .build();
  }
}
