package at.hs.campus.wien.sde.urban_cycling_core.dto;

import lombok.Builder;
import lombok.Data;
import java.util.UUID;

@Data
@Builder
public class UserAnalyticsResponse {
  private UUID userId;
  private int totalRides;
  private double totalDistanceKm;
  private double avgDistancePerRideKm;
  private double avgSpeedKmh;
  private double maxRideDistanceKm;
  private long totalRideTimeMinutes;
}