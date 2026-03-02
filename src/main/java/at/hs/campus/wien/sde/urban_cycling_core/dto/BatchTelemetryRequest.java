package at.hs.campus.wien.sde.urban_cycling_core.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class BatchTelemetryRequest {
  @NotEmpty(message = "At least one telemetry point is required")
  @Valid
  private List<TelemetryRequest> points;
}