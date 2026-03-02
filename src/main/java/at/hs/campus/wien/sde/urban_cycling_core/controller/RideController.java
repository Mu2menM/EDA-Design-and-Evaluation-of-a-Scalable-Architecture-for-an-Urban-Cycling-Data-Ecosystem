package at.hs.campus.wien.sde.urban_cycling_core.controller;

import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import at.hs.campus.wien.sde.urban_cycling_core.dto.EndRideRequest;
import at.hs.campus.wien.sde.urban_cycling_core.dto.EndRideResponse;
import at.hs.campus.wien.sde.urban_cycling_core.dto.RideResponse;
import at.hs.campus.wien.sde.urban_cycling_core.service.RideService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/rides")
@RequiredArgsConstructor
public class RideController {

  private final RideService rideService;

  @PostMapping("/end")
  public ResponseEntity<EndRideResponse> endRide(@Valid @RequestBody EndRideRequest request) {
    EndRideResponse response = rideService.endRide(request);
    return ResponseEntity.ok(response);
  }

  @GetMapping("/{rideId}")
  public ResponseEntity<RideResponse> getRide(@PathVariable UUID rideId) {
    RideResponse response = rideService.getRide(rideId);
    return ResponseEntity.ok(response);
  }

}