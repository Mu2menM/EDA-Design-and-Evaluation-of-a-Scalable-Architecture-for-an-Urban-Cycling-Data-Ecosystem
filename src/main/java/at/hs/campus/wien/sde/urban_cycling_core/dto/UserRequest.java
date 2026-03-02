package at.hs.campus.wien.sde.urban_cycling_core.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UserRequest {
  @NotBlank
  private String username;
}