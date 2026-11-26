package com.ingsis.CNC.user;

import com.ingsis.CNC.user.dto.UserSummaryResponse;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/** Provider used when USERS_PROVIDER is "none" (the default): no users are searchable. */
@Component
@ConditionalOnProperty(name = "users.provider", havingValue = "none", matchIfMissing = true)
public class NoneUserDirectory implements UserDirectory {

  private static final Logger LOG = LoggerFactory.getLogger(NoneUserDirectory.class);

  public NoneUserDirectory() {
    LOG.warn(
        "USERS_PROVIDER=none: GET /users always returns an empty list. "
            + "Set USERS_PROVIDER=auth0 to search Auth0 users.");
  }

  @Override
  public List<UserSummaryResponse> search(String search, int limit) {
    return List.of();
  }
}
