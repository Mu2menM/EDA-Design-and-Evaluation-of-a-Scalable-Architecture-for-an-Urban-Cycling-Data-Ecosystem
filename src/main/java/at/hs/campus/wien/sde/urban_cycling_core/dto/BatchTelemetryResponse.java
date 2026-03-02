package at.hs.campus.wien.sde.urban_cycling_core.dto;

import lombok.Builder;
import lombok.Data;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class BatchTelemetryResponse {
  private int totalPoints;
  private int acceptedPoints;
  private List<UUID> rideIds;        // Ride IDs involved (could be multiple)
  private String message;
  private String status;              // "BATCH_QUEUED"
}