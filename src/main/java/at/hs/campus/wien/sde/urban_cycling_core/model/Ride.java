package at.hs.campus.wien.sde.urban_cycling_core.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "rides")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Ride {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID rideId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  @ToString.Exclude
  private User user;

  @OneToMany(mappedBy = "ride", cascade = CascadeType.ALL, orphanRemoval = true)
  @OrderBy("timestamp asc")
  @Builder.Default
  @ToString.Exclude
  private List<Waypoint> waypoints = new ArrayList<>();

  private LocalDateTime startTime;
  private LocalDateTime endTime;  // Only set when ride is completed via endpoint
  private Double totalDistanceKm;
  private Double avgSpeedKmh;

  @Builder.Default
  private Boolean completed = false;
}