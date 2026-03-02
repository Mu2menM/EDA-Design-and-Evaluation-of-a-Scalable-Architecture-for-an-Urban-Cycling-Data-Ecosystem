package at.hs.campus.wien.sde.urban_cycling_core.controller;

import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import at.hs.campus.wien.sde.urban_cycling_core.dto.UserAnalyticsResponse;
import at.hs.campus.wien.sde.urban_cycling_core.service.AnalyticsService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

  private final AnalyticsService analyticsService;

  @GetMapping("/user/{userId}")
  public ResponseEntity<UserAnalyticsResponse> getUserAnalytics(@PathVariable UUID userId) {
    UserAnalyticsResponse response = analyticsService.getUserAnalytics(userId);
    return ResponseEntity.ok(response);
  }
}
