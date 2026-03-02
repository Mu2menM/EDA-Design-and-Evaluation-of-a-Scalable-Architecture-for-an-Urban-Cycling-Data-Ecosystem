package at.hs.campus.wien.sde.urban_cycling_core.repository;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

import at.hs.campus.wien.sde.urban_cycling_core.model.User;

public interface UserRepository extends JpaRepository<User, UUID> {

}