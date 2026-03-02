package at.hs.campus.wien.sde.urban_cycling_core.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EndRideResponse {
  private UUID rideId;
  private LocalDateTime startTime;
  private LocalDateTime endTime;
  private Double totalDistanceKm;
  private Double avgSpeedKmh;
  private Integer totalWaypoints;
  private String message;
}