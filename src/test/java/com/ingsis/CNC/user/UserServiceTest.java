package com.ingsis.CNC.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ingsis.CNC.user.dto.UserSummaryResponse;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

  @Mock private UserRepository userRepository;

  private UserService userService;

  @BeforeEach
  void setUp() {
    userService = new UserService(userRepository);
  }

  @Test
  void searchUsersMapsEntityToResponse() {
    User user = new User("ana", "ana@mail.com", "pass");
    user.setId(UUID.randomUUID());

    when(userRepository.findByUserNameContainingIgnoreCaseOrEmailContainingIgnoreCase(
            eq("ana"), eq("ana"), any(Pageable.class)))
        .thenReturn(List.of(user));

    List<UserSummaryResponse> results = userService.searchUsers("ana", 10);

    assertThat(results).hasSize(1);
    assertThat(results.get(0).id()).isEqualTo(user.getId().toString());
    assertThat(results.get(0).name()).isEqualTo("ana");
    assertThat(results.get(0).email()).isEqualTo("ana@mail.com");
  }

  @Test
  void searchUsersClampsLimitBetweenOneAndOneHundred() {
    ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);

    userService.searchUsers("query", 200);
    verify(userRepository)
        .findByUserNameContainingIgnoreCaseOrEmailContainingIgnoreCase(
            eq("query"), eq("query"), captor.capture());
    assertThat(captor.getValue().getPageSize()).isEqualTo(100);

    userService.searchUsers("query", 0);
    verify(userRepository, org.mockito.Mockito.times(2))
        .findByUserNameContainingIgnoreCaseOrEmailContainingIgnoreCase(
            eq("query"), eq("query"), captor.capture());
    assertThat(captor.getValue().getPageSize()).isEqualTo(10);
  }
}
