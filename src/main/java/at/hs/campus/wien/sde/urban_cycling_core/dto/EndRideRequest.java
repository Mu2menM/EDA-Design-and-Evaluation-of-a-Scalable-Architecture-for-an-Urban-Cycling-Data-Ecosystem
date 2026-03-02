package at.hs.campus.wien.sde.urban_cycling_core.dto;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class EndRideRequest {
  @NotNull
  private UUID rideId;
}
