// TelemetryRequest.java (MC-01)

package at.hs.campus.wien.sde.urban_cycling_core.dto;

import java.time.LocalDateTime;
import java.util.UUID;
import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TelemetryRequest {
  @NotNull
  private UUID userId;

  @NotNull
  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
  private LocalDateTime timestamp;

  @NotNull
  private Double latitude;

  @NotNull
  private Double longitude;
}