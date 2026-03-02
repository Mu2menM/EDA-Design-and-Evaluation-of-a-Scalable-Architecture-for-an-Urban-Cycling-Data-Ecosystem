package at.hs.campus.wien.sde.urban_cycling_core.controller;

import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import at.hs.campus.wien.sde.urban_cycling_core.dto.RideSummary;
import at.hs.campus.wien.sde.urban_cycling_core.dto.UserRequest;
import at.hs.campus.wien.sde.urban_cycling_core.dto.UserResponse;
import at.hs.campus.wien.sde.urban_cycling_core.service.RideService;
import at.hs.campus.wien.sde.urban_cycling_core.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

  private final UserService userService;
  private final RideService rideService;

  @PostMapping
  public ResponseEntity<UserResponse> createUser(@Valid @RequestBody UserRequest request) {
    UserResponse response = userService.createUser(request);
    return new ResponseEntity<>(response, HttpStatus.CREATED);
  }

  @GetMapping("/{userId}/rides")
  public ResponseEntity<List<RideSummary>> getUserRides(@PathVariable UUID userId) {
    List<RideSummary> rides = rideService.getRidesByUser(userId);
    return ResponseEntity.ok(rides);
  }
}