package com.ingsis.CNC.user;

import java.time.Clock;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(name = "users.provider", havingValue = "auth0")
public class UsersConfiguration {

  @Bean
  public Auth0UserDirectory auth0UserDirectory(
      @Value("${auth0.domain:}") String domain,
      @Value("${auth0.mgmt.client-id:}") String clientId,
      @Value("${auth0.mgmt.client-secret:}") String clientSecret) {
    requireValue("AUTH0_DOMAIN", domain);
    requireValue("AUTH0_MGMT_CLIENT_ID", clientId);
    requireValue("AUTH0_MGMT_CLIENT_SECRET", clientSecret);
    return new Auth0UserDirectory(
        new Auth0ManagementClient(domain, clientId, clientSecret, Clock.systemUTC()));
  }

  private static void requireValue(String name, String value) {
    if (value == null || value.isBlank()) {
      throw new IllegalStateException("USERS_PROVIDER=auth0 requires " + name + " to be set");
    }
  }
}
