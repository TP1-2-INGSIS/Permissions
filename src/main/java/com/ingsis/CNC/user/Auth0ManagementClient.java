package com.ingsis.CNC.user;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

/** Minimal Auth0 Management API client: client-credentials token (cached) and user search. */
public class Auth0ManagementClient {

  private static final Duration TOKEN_SAFETY_MARGIN = Duration.ofSeconds(60);

  private final RestClient restClient;
  private final String audience;
  private final String clientId;
  private final String clientSecret;
  private final Clock clock;

  private String cachedToken;
  private Instant tokenExpiresAt = Instant.MIN;

  /**
   * @param domain Auth0 tenant domain (e.g. "tenant.us.auth0.com"). A value that already starts
   *     with a scheme is used as the base URL as is (useful for tests).
   */
  public Auth0ManagementClient(String domain, String clientId, String clientSecret, Clock clock) {
    String trimmed = domain.trim().replaceAll("/+$", "");
    boolean hasScheme = trimmed.startsWith("http://") || trimmed.startsWith("https://");
    String host = hasScheme ? trimmed.substring(trimmed.indexOf("://") + 3) : trimmed;
    String baseUrl = hasScheme ? trimmed : "https://" + trimmed;
    this.audience = "https://" + host + "/api/v2/";
    this.clientId = clientId;
    this.clientSecret = clientSecret;
    this.clock = clock;
    SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
    factory.setConnectTimeout(Duration.ofSeconds(5));
    factory.setReadTimeout(Duration.ofSeconds(10));
    this.restClient = RestClient.builder().baseUrl(baseUrl).requestFactory(factory).build();
  }

  /** Searches users. {@code query} may be blank, in which case no filter is applied. */
  public List<Auth0User> searchUsers(String query, int limit) {
    try {
      return doSearch(query, limit);
    } catch (RestClientResponseException e) {
      if (e.getStatusCode().value() == 401) {
        invalidateToken();
        return wrap(() -> doSearch(query, limit));
      }
      throw failure("Auth0 user search failed with status " + e.getStatusCode().value(), e);
    } catch (RestClientException e) {
      throw failure("Auth0 user search failed: " + e.getClass().getSimpleName(), e);
    }
  }

  private List<Auth0User> wrap(java.util.function.Supplier<List<Auth0User>> call) {
    try {
      return call.get();
    } catch (RestClientResponseException e) {
      throw failure("Auth0 user search failed with status " + e.getStatusCode().value(), e);
    } catch (RestClientException e) {
      throw failure("Auth0 user search failed: " + e.getClass().getSimpleName(), e);
    }
  }

  private List<Auth0User> doSearch(String query, int limit) {
    String token = token();
    Auth0User[] users =
        restClient
            .get()
            .uri(uriBuilder -> {
              uriBuilder
                  .path("/api/v2/users")
                  .queryParam("search_engine", "v3")
                  .queryParam("per_page", limit)
                  .queryParam("fields", "user_id,name,email,nickname");
              if (query != null && !query.isBlank()) {
                uriBuilder.queryParam("q", "{q}");
                return uriBuilder.build(Map.of("q", query));
              }
              return uriBuilder.build();
            })
            .headers(h -> h.setBearerAuth(token))
            .retrieve()
            .body(Auth0User[].class);
    return users == null ? List.of() : List.of(users);
  }

  private synchronized String token() {
    Instant now = clock.instant();
    if (cachedToken != null && now.isBefore(tokenExpiresAt)) {
      return cachedToken;
    }
    TokenResponse response;
    try {
      response =
          restClient
              .post()
              .uri("/oauth/token")
              .contentType(MediaType.APPLICATION_JSON)
              .body(
                  Map.of(
                      "grant_type", "client_credentials",
                      "client_id", clientId,
                      "client_secret", clientSecret,
                      "audience", audience))
              .retrieve()
              .body(TokenResponse.class);
    } catch (RestClientResponseException e) {
      throw failure("Auth0 token request failed with status " + e.getStatusCode().value(), e);
    } catch (RestClientException e) {
      throw failure("Auth0 token request failed: " + e.getClass().getSimpleName(), e);
    }
    if (response == null || response.accessToken() == null || response.accessToken().isBlank()) {
      throw new UsersProviderException("Auth0 token response did not contain an access token");
    }
    cachedToken = response.accessToken();
    long ttl = response.expiresIn() != null && response.expiresIn() > 0 ? response.expiresIn() : 0;
    tokenExpiresAt = now.plusSeconds(ttl).minus(TOKEN_SAFETY_MARGIN);
    return cachedToken;
  }

  private synchronized void invalidateToken() {
    cachedToken = null;
    tokenExpiresAt = Instant.MIN;
  }

  // The cause is dropped from the message on purpose: only status/type is exposed.
  private static UsersProviderException failure(String message, RuntimeException cause) {
    return new UsersProviderException(message, cause);
  }

  record TokenResponse(
      @JsonProperty("access_token") String accessToken,
      @JsonProperty("expires_in") Long expiresIn) {}

  public record Auth0User(
      @JsonProperty("user_id") String userId, String name, String email, String nickname) {}
}
