package at.hs.campus.wien.sde.urban_cycling_core.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class WaypointResponse {
  private UUID id;
  private Double latitude;
  private Double longitude;
  private LocalDateTime timestamp;
}