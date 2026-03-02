package at.hs.campus.wien.sde.urban_cycling_core.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import at.hs.campus.wien.sde.urban_cycling_core.dto.UserRequest;
import at.hs.campus.wien.sde.urban_cycling_core.dto.UserResponse;
import at.hs.campus.wien.sde.urban_cycling_core.model.User;
import at.hs.campus.wien.sde.urban_cycling_core.repository.UserRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

  private final UserRepository userRepository;

  @Transactional
  public UserResponse createUser(UserRequest request) {
    User user = userRepository.save(User.builder().username(request.getUsername()).build());

    return UserResponse.builder()
        .userId(user.getUserId())
        .username(user.getUsername())
        .build();
  }
}
