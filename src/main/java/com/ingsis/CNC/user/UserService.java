package com.ingsis.CNC.user;

import com.ingsis.CNC.user.dto.UserSummaryResponse;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class UserService {

  private static final int DEFAULT_LIMIT = 10;
  private static final int MAX_LIMIT = 100;

  private final UserDirectory userDirectory;

  public UserService(UserDirectory userDirectory) {
    this.userDirectory = userDirectory;
  }

  public List<UserSummaryResponse> searchUsers(String search, int limit) {
    int pageSize = Math.min(limit > 0 ? limit : DEFAULT_LIMIT, MAX_LIMIT);
    return userDirectory.search(search != null ? search : "", pageSize);
  }
}
