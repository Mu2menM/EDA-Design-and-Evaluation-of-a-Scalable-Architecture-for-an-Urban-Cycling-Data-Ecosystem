package at.hs.campus.wien.sde.urban_cycling_core.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RideSummary {
  private UUID rideId;
  private LocalDateTime startTime;
  private LocalDateTime endTime;
  private Boolean completed;
  private int waypointCount;
}