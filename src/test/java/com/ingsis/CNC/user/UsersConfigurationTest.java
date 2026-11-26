package com.ingsis.CNC.user;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

class UsersConfigurationTest {

  private final ApplicationContextRunner runner =
      new ApplicationContextRunner()
          .withUserConfiguration(UsersConfiguration.class, NoneUserDirectory.class);

  @Test
  void defaultsToNoneProviderWithEmptyResults() {
    runner.run(ctx -> {
      assertThat(ctx).hasSingleBean(NoneUserDirectory.class);
      assertThat(ctx).doesNotHaveBean(Auth0UserDirectory.class);
      assertThat(ctx.getBean(UserDirectory.class).search("ana", 5)).isEmpty();
    });
  }

  @Test
  void auth0ProviderCreatesDirectoryWhenConfigured() {
    runner
        .withPropertyValues(
            "users.provider=auth0",
            "auth0.domain=tenant.auth0.com",
            "auth0.mgmt.client-id=id",
            "auth0.mgmt.client-secret=secret")
        .run(ctx -> {
          assertThat(ctx).hasSingleBean(Auth0UserDirectory.class);
          assertThat(ctx).doesNotHaveBean(NoneUserDirectory.class);
        });
  }

  @Test
  void auth0ProviderFailsFastWhenVariablesAreMissing() {
    runner
        .withPropertyValues("users.provider=auth0", "auth0.domain=tenant.auth0.com")
        .run(ctx ->
            assertThat(ctx.getStartupFailure())
                .hasRootCauseInstanceOf(IllegalStateException.class)
                .rootCause()
                .hasMessageContaining("AUTH0_MGMT_CLIENT_ID"));
    runner
        .withPropertyValues("users.provider=auth0", "auth0.mgmt.client-id= ")
        .run(ctx ->
            assertThat(ctx.getStartupFailure()).rootCause().hasMessageContaining("AUTH0_DOMAIN"));
    runner
        .withPropertyValues("users.provider=auth0", "auth0.domain=d", "auth0.mgmt.client-id=i")
        .run(ctx ->
            assertThat(ctx.getStartupFailure())
                .rootCause()
                .hasMessageContaining("AUTH0_MGMT_CLIENT_SECRET"));
  }
}
