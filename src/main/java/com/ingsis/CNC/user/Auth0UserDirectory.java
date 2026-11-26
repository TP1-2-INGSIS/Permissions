package com.ingsis.CNC.user;

import com.ingsis.CNC.user.Auth0ManagementClient.Auth0User;
import com.ingsis.CNC.user.dto.UserSummaryResponse;
import java.util.List;
import java.util.Locale;

/** Searches users in Auth0 by prefix over name, email and nickname. */
public class Auth0UserDirectory implements UserDirectory {

  /** Auth0 v3 requires at least 3 characters before a trailing wildcard. */
  static final int MIN_WILDCARD_CHARS = 3;

  private static final String LUCENE_SPECIAL = "+-!(){}[]^\"~*?:\\/&| ";

  private final Auth0ManagementClient client;

  public Auth0UserDirectory(Auth0ManagementClient client) {
    this.client = client;
  }

  @Override
  public List<UserSummaryResponse> search(String search, int limit) {
    return client.searchUsers(buildQuery(search), limit).stream()
        .filter(u -> u.userId() != null)
        .map(Auth0UserDirectory::toSummary)
        .toList();
  }

  static String buildQuery(String search) {
    String term = search == null ? "" : search.trim();
    if (term.isEmpty()) {
      return "";
    }
    String wildcard = term.length() >= MIN_WILDCARD_CHARS ? "*" : "";
    String escaped = escape(term) + wildcard;
    String email = escape(term.toLowerCase(Locale.ROOT)) + wildcard;
    return "name:" + escaped + " OR email:" + email + " OR nickname:" + escaped;
  }

  static String escape(String term) {
    StringBuilder sb = new StringBuilder();
    for (char c : term.toCharArray()) {
      if (LUCENE_SPECIAL.indexOf(c) >= 0) {
        sb.append('\\');
      }
      sb.append(c);
    }
    return sb.toString();
  }

  private static UserSummaryResponse toSummary(Auth0User u) {
    String name = firstNonBlank(u.name(), u.nickname(), u.email());
    return new UserSummaryResponse(u.userId(), name, u.email());
  }

  private static String firstNonBlank(String... values) {
    for (String v : values) {
      if (v != null && !v.isBlank()) {
        return v;
      }
    }
    return null;
  }
}
