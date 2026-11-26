package com.ingsis.CNC.user;

import com.ingsis.CNC.user.dto.UserSummaryResponse;
import java.util.List;

/** Source of users that can be searched when sharing a snippet. */
public interface UserDirectory {

  List<UserSummaryResponse> search(String search, int limit);
}
