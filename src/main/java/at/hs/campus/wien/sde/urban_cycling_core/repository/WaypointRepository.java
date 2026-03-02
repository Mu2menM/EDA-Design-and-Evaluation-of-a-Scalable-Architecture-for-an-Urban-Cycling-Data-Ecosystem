package at.hs.campus.wien.sde.urban_cycling_core.repository;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

import at.hs.campus.wien.sde.urban_cycling_core.model.Waypoint;

public interface WaypointRepository extends JpaRepository<Waypoint, UUID> {
}