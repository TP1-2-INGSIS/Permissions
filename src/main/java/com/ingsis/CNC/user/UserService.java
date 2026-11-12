package com.ingsis.CNC.user;

import com.ingsis.CNC.user.dto.UserSummaryResponse;
import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class UserService {

  private final UserRepository userRepository;

  public UserService(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  public List<UserSummaryResponse> searchUsers(String search, int limit) {
    int pageSize = Math.min(limit > 0 ? limit : 10, 100);
    Pageable pageable = PageRequest.of(0, pageSize);
    String term = search != null ? search : "";
    List<User> users =
        userRepository.findByUserNameContainingIgnoreCaseOrEmailContainingIgnoreCase(
            term, term, pageable);
    return users.stream()
        .map(u ->
            new UserSummaryResponse(
                u.getId() != null ? u.getId().toString() : null, u.getUserName(), u.getEmail()))
        .toList();
  }
}
