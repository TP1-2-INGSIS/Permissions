package com.ingsis.CNC.user;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.ingsis.CNC.user.dto.UserSummaryResponse;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class Auth0UserDirectoryTest {

  private static final String SECRET = "super-secret-value";

  private WireMockServer wireMock;
  private MutableClock clock;
  private Auth0UserDirectory directory;

  @BeforeEach
  void setUp() {
    wireMock = new WireMockServer(WireMockConfiguration.options().dynamicPort());
    wireMock.start();
    clock = new MutableClock(Instant.parse("2026-01-01T00:00:00Z"));
    directory =
        new Auth0UserDirectory(
            new Auth0ManagementClient(
                "http://localhost:" + wireMock.port() + "/", "client-id", SECRET, clock));
  }

  @AfterEach
  void tearDown() {
    wireMock.stop();
  }

  private void stubToken(int expiresIn) {
    wireMock.stubFor(
        post(urlPathEqualTo("/oauth/token"))
            .willReturn(
                aResponse()
                    .withHeader("Content-Type", "application/json")
                    .withBody(
                        "{\"access_token\":\"tok-1\",\"token_type\":\"Bearer\",\"expires_in\":"
                            + expiresIn
                            + "}")));
  }

  private void stubUsers(String body) {
    wireMock.stubFor(
        get(urlPathEqualTo("/api/v2/users"))
            .willReturn(aResponse().withHeader("Content-Type", "application/json").withBody(body)));
  }

  @Test
  void searchesByPrefixAndMapsUsers() {
    stubToken(86400);
    stubUsers(
        """
        [{"user_id":"auth0|1","name":"Ana Perez","email":"ana@x.com","nickname":"ana"},
         {"user_id":"google-oauth2|2","name":"","email":"bob@x.com","nickname":"bobby"},
         {"user_id":"auth0|3","email":"c@x.com"},
         {"user_id":"auth0|4","name":null,"email":null,"nickname":null},
         {"name":"no id"}]
        """);

    List<UserSummaryResponse> result = directory.search("Ana", 5);

    assertThat(result)
        .containsExactly(
            new UserSummaryResponse("auth0|1", "Ana Perez", "ana@x.com"),
            new UserSummaryResponse("google-oauth2|2", "bobby", "bob@x.com"),
            new UserSummaryResponse("auth0|3", "c@x.com", "c@x.com"),
            new UserSummaryResponse("auth0|4", null, null));
    wireMock.verify(
        postRequestedFor(urlPathEqualTo("/oauth/token"))
            .withRequestBody(
                equalToJson(
                    "{\"grant_type\":\"client_credentials\",\"client_id\":\"client-id\","
                        + "\"client_secret\":\""
                        + SECRET
                        + "\",\"audience\":\"https://localhost:"
                        + wireMock.port()
                        + "/api/v2/\"}")));
    wireMock.verify(
        getRequestedFor(urlPathEqualTo("/api/v2/users"))
            .withHeader("Authorization", equalTo("Bearer tok-1"))
            .withQueryParam("search_engine", equalTo("v3"))
            .withQueryParam("per_page", equalTo("5"))
            .withQueryParam("fields", equalTo("user_id,name,email,nickname"))
            .withQueryParam("q", equalTo("name:Ana* OR email:ana* OR nickname:Ana*")));
  }

  @Test
  void blankSearchSendsNoQuery() {
    stubToken(86400);
    stubUsers("[]");

    assertThat(directory.search("  ", 10)).isEmpty();
    assertThat(directory.search(null, 10)).isEmpty();

    wireMock.verify(
        getRequestedFor(urlPathEqualTo("/api/v2/users"))
            .withQueryParam("per_page", equalTo("10"))
            .withoutQueryParam("q"));
  }

  @Test
  void shortTermsDoNotUseWildcard() {
    assertThat(Auth0UserDirectory.buildQuery("an")).isEqualTo("name:an OR email:an OR nickname:an");
    assertThat(Auth0UserDirectory.buildQuery("ana"))
        .isEqualTo("name:ana* OR email:ana* OR nickname:ana*");
  }

  @Test
  void escapesLuceneSpecialCharacters() {
    assertThat(Auth0UserDirectory.escape("a+b (c) \"d\" *?:\\/"))
        .isEqualTo("a\\+b\\ \\(c\\)\\ \\\"d\\\"\\ \\*\\?\\:\\\\\\/");
    assertThat(Auth0UserDirectory.buildQuery("Ana@X.com"))
        .isEqualTo("name:Ana@X.com* OR email:ana@x.com* OR nickname:Ana@X.com*");
  }

  @Test
  void specialCharactersInSearchAreSentEscapedAndEncoded() {
    stubToken(86400);
    stubUsers("[]");

    directory.search("a&b c", 3);

    wireMock.verify(
        getRequestedFor(urlPathEqualTo("/api/v2/users"))
            .withQueryParam(
                "q", equalTo("name:a\\&b\\ c* OR email:a\\&b\\ c* OR nickname:a\\&b\\ c*")));
  }

  @Test
  void cachesTokenUntilShortlyBeforeExpiry() {
    stubToken(3600);
    stubUsers("[]");

    directory.search("ana", 5);
    directory.search("ana", 5);
    clock.advance(Duration.ofSeconds(3600 - 61));
    directory.search("ana", 5);
    wireMock.verify(1, postRequestedFor(urlPathEqualTo("/oauth/token")));

    clock.advance(Duration.ofSeconds(2));
    directory.search("ana", 5);
    wireMock.verify(2, postRequestedFor(urlPathEqualTo("/oauth/token")));
  }

  @Test
  void tokenRequestFailureIsReportedWithoutLeakingSecret() {
    wireMock.stubFor(
        post(urlPathEqualTo("/oauth/token"))
            .willReturn(aResponse().withStatus(401).withBody("{\"error\":\"" + SECRET + "\"}")));

    assertThatThrownBy(() -> directory.search("ana", 5))
        .isInstanceOf(UsersProviderException.class)
        .hasMessage("Auth0 token request failed with status 401")
        .satisfies(e -> assertThat(e.getMessage()).doesNotContain(SECRET));
  }

  @Test
  void tokenResponseWithoutTokenFails() {
    wireMock.stubFor(
        post(urlPathEqualTo("/oauth/token"))
            .willReturn(aResponse().withHeader("Content-Type", "application/json").withBody("{}")));

    assertThatThrownBy(() -> directory.search("ana", 5))
        .isInstanceOf(UsersProviderException.class)
        .hasMessageContaining("access token");
  }

  @Test
  void searchErrorIsReported() {
    stubToken(86400);
    wireMock.stubFor(get(urlPathEqualTo("/api/v2/users")).willReturn(aResponse().withStatus(429)));

    assertThatThrownBy(() -> directory.search("ana", 5))
        .isInstanceOf(UsersProviderException.class)
        .hasMessage("Auth0 user search failed with status 429");
  }

  @Test
  void unauthorizedSearchRefreshesTokenAndRetriesOnce() {
    stubToken(86400);
    wireMock.stubFor(
        get(urlPathEqualTo("/api/v2/users"))
            .inScenario("retry")
            .whenScenarioStateIs("Started")
            .willReturn(aResponse().withStatus(401))
            .willSetStateTo("fresh"));
    wireMock.stubFor(
        get(urlPathEqualTo("/api/v2/users"))
            .inScenario("retry")
            .whenScenarioStateIs("fresh")
            .willReturn(
                aResponse()
                    .withHeader("Content-Type", "application/json")
                    .withBody(
                        "[{\"user_id\":\"auth0|1\",\"name\":\"Ana\",\"email\":\"a@x.com\"}]")));

    assertThat(directory.search("ana", 5)).hasSize(1);
    wireMock.verify(2, postRequestedFor(urlPathEqualTo("/oauth/token")));
  }

  @Test
  void repeatedUnauthorizedFails() {
    stubToken(86400);
    wireMock.stubFor(get(urlPathEqualTo("/api/v2/users")).willReturn(aResponse().withStatus(401)));

    assertThatThrownBy(() -> directory.search("ana", 5))
        .isInstanceOf(UsersProviderException.class)
        .hasMessage("Auth0 user search failed with status 401");
  }

  @Test
  void connectionFailureIsReported() {
    wireMock.stop();

    assertThatThrownBy(() -> directory.search("ana", 5))
        .isInstanceOf(UsersProviderException.class)
        .hasMessageContaining("Auth0 token request failed");
  }

  @Test
  void domainWithoutSchemeBuildsHttpsUrls() {
    Auth0ManagementClient client =
        new Auth0ManagementClient("tenant.auth0.com", "id", SECRET, Clock.systemUTC());
    assertThat(client).isNotNull();
  }

  private static final class MutableClock extends Clock {
    private Instant now;

    MutableClock(Instant now) {
      this.now = now;
    }

    void advance(Duration d) {
      now = now.plus(d);
    }

    @Override
    public java.time.ZoneId getZone() {
      return ZoneOffset.UTC;
    }

    @Override
    public Clock withZone(java.time.ZoneId zone) {
      return this;
    }

    @Override
    public Instant instant() {
      return now;
    }
  }
}
