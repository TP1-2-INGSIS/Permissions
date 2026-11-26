package com.ingsis.CNC.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ingsis.CNC.user.dto.UserSummaryResponse;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

  @Mock private UserDirectory directory;

  @Test
  void delegatesToDirectory() {
    List<UserSummaryResponse> users = List.of(new UserSummaryResponse("id", "ana", "a@x.com"));
    when(directory.search("ana", 5)).thenReturn(users);

    assertThat(new UserService(directory).searchUsers("ana", 5)).isEqualTo(users);
  }

  @Test
  void clampsLimitAndNormalizesNullSearch() {
    UserService service = new UserService(directory);

    service.searchUsers(null, 0);
    service.searchUsers("x", 1000);

    verify(directory).search("", 10);
    verify(directory).search("x", 100);
  }
}
