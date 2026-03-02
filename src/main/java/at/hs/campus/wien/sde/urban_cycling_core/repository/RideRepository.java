package at.hs.campus.wien.sde.urban_cycling_core.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import at.hs.campus.wien.sde.urban_cycling_core.model.Ride;

public interface RideRepository extends JpaRepository<Ride, UUID> {
  @Query(value = "SELECT * FROM rides r WHERE r.user_id = :userId AND r.completed = FALSE LIMIT 1",
      nativeQuery = true)
  Optional<Ride> findLastOpenRideByUser(@Param("userId") UUID userId);

  List<Ride> findByUserUserId(UUID userId);

}
