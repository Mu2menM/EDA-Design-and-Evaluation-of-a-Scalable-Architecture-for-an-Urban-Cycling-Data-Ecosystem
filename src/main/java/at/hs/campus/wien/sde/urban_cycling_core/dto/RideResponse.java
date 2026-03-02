package at.hs.campus.wien.sde.urban_cycling_core.dto;

// RideResponse.java (MC-03)

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RideResponse {
  private UUID rideId;
  private UUID userId;
  private LocalDateTime startTime;
  private LocalDateTime endTime;
  private Double totalDistanceKm;      // MC-07
  private Double avgSpeedKmh;           // MC-07
  private List<WaypointResponse> waypoints;
}