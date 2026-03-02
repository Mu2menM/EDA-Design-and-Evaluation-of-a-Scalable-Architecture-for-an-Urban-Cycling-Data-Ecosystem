package at.hs.campus.wien.sde.urban_cycling_core.dto;

import java.util.UUID;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserResponse {
  private UUID userId;
  private String username;
}