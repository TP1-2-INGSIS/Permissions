package com.ingsis.CNC.user;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ingsis.CNC.user.dto.UserSummaryResponse;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

  @Mock private UserService userService;

  private MockMvc mockMvc;

  @BeforeEach
  void setUp() {
    mockMvc = MockMvcBuilders.standaloneSetup(new UserController(userService)).build();
  }

  @Test
  void searchUsersReturnsUserSummaryList() throws Exception {
    when(userService.searchUsers("ana", 5))
        .thenReturn(List.of(new UserSummaryResponse("id-1", "ana", "ana@mail.com")));

    mockMvc
        .perform(get("/users").param("search", "ana").param("limit", "5"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].id").value("id-1"))
        .andExpect(jsonPath("$[0].name").value("ana"))
        .andExpect(jsonPath("$[0].email").value("ana@mail.com"));
  }

  @Test
  void searchUsersWithDefaults() throws Exception {
    when(userService.searchUsers("", 10))
        .thenReturn(List.of(new UserSummaryResponse("id-2", "bob", "bob@mail.com")));

    mockMvc
        .perform(get("/users"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].name").value("bob"));
  }
}
