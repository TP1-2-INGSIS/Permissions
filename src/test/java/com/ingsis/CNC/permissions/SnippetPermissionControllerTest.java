package com.ingsis.CNC.permissions;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ingsis.CNC.TestcontainersConfiguration;
import com.ingsis.CNC.user.User;
import com.ingsis.CNC.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class SnippetPermissionControllerTest {

  private static final String SNIPPET_ID = "6f1c0000-0000-0000-0000-000000000001";
  private static final String USER_ID = "auth0|user1";
  private static final String USER_ID_2 = "auth0|user2";

  @Autowired private MockMvc mockMvc;

  @Autowired private SnippetPermissionRepository snippetPermissionRepository;

  @Autowired private UserRepository userRepository;

  @BeforeEach
  void setUp() {
    snippetPermissionRepository.deleteAll();
    userRepository.deleteAll();
  }

  @Test
  @DisplayName("POST /permissions returns 201 Created on new permission")
  void createPermissionWhenNewShouldReturnCreated() throws Exception {
    String payload =
        """
        {
          "snippetId": "%s",
          "userId": "%s",
          "role": "OWNER"
        }
        """
            .formatted(SNIPPET_ID, USER_ID);

    mockMvc
        .perform(post("/permissions").contentType(MediaType.APPLICATION_JSON).content(payload))
        .andExpect(status().isCreated());
  }

  @Test
  @DisplayName("POST /permissions returns 409 Conflict when duplicate permission is posted")
  void createPermissionWhenAlreadyExistsShouldReturnConflict() throws Exception {
    String payload =
        """
        {
          "snippetId": "%s",
          "userId": "%s",
          "role": "OWNER"
        }
        """
            .formatted(SNIPPET_ID, USER_ID);

    mockMvc
        .perform(post("/permissions").contentType(MediaType.APPLICATION_JSON).content(payload))
        .andExpect(status().isCreated());

    mockMvc
        .perform(post("/permissions").contentType(MediaType.APPLICATION_JSON).content(payload))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.error", is("ALREADY_EXISTS")));
  }

  @Test
  @DisplayName("GET /permissions?userId returns list with created snippet and role")
  void getPermissionsByUserShouldReturnPermissionsList() throws Exception {
    String payload =
        """
        {
          "snippetId": "%s",
          "userId": "%s",
          "role": "OWNER"
        }
        """
            .formatted(SNIPPET_ID, USER_ID);

    mockMvc
        .perform(post("/permissions").contentType(MediaType.APPLICATION_JSON).content(payload))
        .andExpect(status().isCreated());

    mockMvc
        .perform(get("/permissions").param("userId", USER_ID))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(1)))
        .andExpect(jsonPath("$[0].snippetId", is(SNIPPET_ID)))
        .andExpect(jsonPath("$[0].role", is("OWNER")));
  }

  @Test
  @DisplayName("GET /permissions?userId with &role=READ returns empty list when role is OWNER")
  void getPermissionsByUserWithDifferentRoleShouldReturnEmptyList() throws Exception {
    String payload =
        """
        {
          "snippetId": "%s",
          "userId": "%s",
          "role": "OWNER"
        }
        """
            .formatted(SNIPPET_ID, USER_ID);

    mockMvc
        .perform(post("/permissions").contentType(MediaType.APPLICATION_JSON).content(payload))
        .andExpect(status().isCreated());

    mockMvc
        .perform(get("/permissions").param("userId", USER_ID).param("role", "READ"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(0)));
  }

  @Test
  @DisplayName("GET /permissions/check returns allowed=true and role=OWNER for write action")
  void checkPermissionWhenOwnerAndWriteActionShouldReturnAllowed() throws Exception {
    String payload =
        """
        {
          "snippetId": "%s",
          "userId": "%s",
          "role": "OWNER"
        }
        """
            .formatted(SNIPPET_ID, USER_ID);

    mockMvc
        .perform(post("/permissions").contentType(MediaType.APPLICATION_JSON).content(payload))
        .andExpect(status().isCreated());

    mockMvc
        .perform(
            get("/permissions/check")
                .param("userId", USER_ID)
                .param("snippetId", SNIPPET_ID)
                .param("action", "WRITE"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.allowed", is(true)))
        .andExpect(jsonPath("$.role", is("OWNER")));
  }

  @Test
  @DisplayName("GET /permissions/check returns allowed=false and role=null for unknown user")
  void checkPermissionWhenUnknownUserShouldReturnNotAllowed() throws Exception {
    mockMvc
        .perform(
            get("/permissions/check")
                .param("userId", "auth0|unknown")
                .param("snippetId", SNIPPET_ID)
                .param("action", "READ"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.allowed", is(false)))
        .andExpect(jsonPath("$.role", nullValue()));
  }

  @Test
  @DisplayName("GET /permissions/check returns allowed=false for READ role attempting WRITE action")
  void checkPermissionWhenReadRoleAttemptingWriteShouldReturnForbidden() throws Exception {
    String payload =
        """
        {
          "snippetId": "%s",
          "userId": "%s",
          "role": "READ"
        }
        """
            .formatted(SNIPPET_ID, USER_ID_2);

    mockMvc
        .perform(post("/permissions").contentType(MediaType.APPLICATION_JSON).content(payload))
        .andExpect(status().isCreated());

    mockMvc
        .perform(
            get("/permissions/check")
                .param("userId", USER_ID_2)
                .param("snippetId", SNIPPET_ID)
                .param("action", "WRITE"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.allowed", is(false)))
        .andExpect(jsonPath("$.role", is("READ")));
  }

  @Test
  @DisplayName("GET /permissions/snippets/{snippetId} returns list of user permissions")
  void getSnippetPermissionsShouldReturnUserPermissionsList() throws Exception {
    String payload =
        """
        {
          "snippetId": "%s",
          "userId": "%s",
          "role": "OWNER"
        }
        """
            .formatted(SNIPPET_ID, USER_ID);

    mockMvc
        .perform(post("/permissions").contentType(MediaType.APPLICATION_JSON).content(payload))
        .andExpect(status().isCreated());

    mockMvc
        .perform(get("/permissions/snippets/{snippetId}", SNIPPET_ID))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(1)))
        .andExpect(jsonPath("$[0].userId", is(USER_ID)))
        .andExpect(jsonPath("$[0].role", is("OWNER")));
  }

  @Test
  @DisplayName("GET /permissions/snippets/{snippetId} returns multiple user permissions")
  void getSnippetPermissionsWhenMultipleUsersShouldReturnAllUsers() throws Exception {
    String ownerPayload =
        """
        {
          "snippetId": "%s",
          "userId": "%s",
          "role": "OWNER"
        }
        """
            .formatted(SNIPPET_ID, USER_ID);

    String readPayload =
        """
        {
          "snippetId": "%s",
          "userId": "%s",
          "role": "READ"
        }
        """
            .formatted(SNIPPET_ID, USER_ID_2);

    mockMvc
        .perform(post("/permissions").contentType(MediaType.APPLICATION_JSON).content(ownerPayload))
        .andExpect(status().isCreated());

    mockMvc
        .perform(post("/permissions").contentType(MediaType.APPLICATION_JSON).content(readPayload))
        .andExpect(status().isCreated());

    mockMvc
        .perform(get("/permissions/snippets/{snippetId}", SNIPPET_ID))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(2)));
  }

  @Test
  @DisplayName("DELETE /permissions/snippets/{snippetId} deletes permissions and returns 204")
  void deleteSnippetPermissionsShouldDeleteAndReturnNoContent() throws Exception {
    String payload =
        """
        {
          "snippetId": "%s",
          "userId": "%s",
          "role": "OWNER"
        }
        """
            .formatted(SNIPPET_ID, USER_ID);

    mockMvc
        .perform(post("/permissions").contentType(MediaType.APPLICATION_JSON).content(payload))
        .andExpect(status().isCreated());

    mockMvc
        .perform(delete("/permissions/snippets/{snippetId}", SNIPPET_ID))
        .andExpect(status().isNoContent());

    mockMvc
        .perform(
            get("/permissions/check")
                .param("userId", USER_ID)
                .param("snippetId", SNIPPET_ID)
                .param("action", "READ"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.allowed", is(false)))
        .andExpect(jsonPath("$.role", nullValue()));
  }

  @Test
  @DisplayName("GET /users?search=ana returns list of matching UserSummaryResponse")
  void searchUsersShouldReturnMatchingUserSummaryResponseList() throws Exception {
    User savedUser = userRepository.save(new User("ana", "ana@example.com", "dummy-hash"));

    mockMvc
        .perform(get("/users").param("search", "ana"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(1)))
        .andExpect(jsonPath("$[0].id", is(savedUser.getId().toString())))
        .andExpect(jsonPath("$[0].name", is("ana")))
        .andExpect(jsonPath("$[0].email", is("ana@example.com")));
  }

  @Test
  @DisplayName("GET /users?search=notfound returns empty list when no users match")
  void searchUsersWhenNoMatchShouldReturnEmptyList() throws Exception {
    userRepository.save(new User("ana", "ana@example.com", "dummy-hash"));

    mockMvc
        .perform(get("/users").param("search", "notfound"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(0)));
  }

  @Test
  @DisplayName("GET /users?search=ana&limit=1 respects limit parameter")
  void searchUsersWithLimitShouldReturnLimitedList() throws Exception {
    userRepository.save(new User("ana1", "ana1@example.com", "dummy-hash-1"));
    userRepository.save(new User("ana2", "ana2@example.com", "dummy-hash-2"));

    mockMvc
        .perform(get("/users").param("search", "ana").param("limit", "1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(1)));
  }

  @Test
  @DisplayName("POST /permissions/share shares snippet with target user")
  void shareSnippetWhenOwnerShouldReturnPermissionResponse() throws Exception {
    String ownerPayload =
        """
        {
          "snippetId": "%s",
          "userId": "%s",
          "role": "OWNER"
        }
        """
            .formatted(SNIPPET_ID, USER_ID);

    mockMvc
        .perform(post("/permissions").contentType(MediaType.APPLICATION_JSON).content(ownerPayload))
        .andExpect(status().isCreated());

    String sharePayload =
        """
        {
          "ownerId": "%s",
          "targetUserId": "%s",
          "snippetId": "%s"
        }
        """
            .formatted(USER_ID, USER_ID_2, SNIPPET_ID);

    mockMvc
        .perform(
            post("/permissions/share")
                .contentType(MediaType.APPLICATION_JSON)
                .content(sharePayload))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.userId", is(USER_ID_2)))
        .andExpect(jsonPath("$.role", is("READ")));
  }

  @Test
  @DisplayName("POST /permissions/share returns 403 when non-owner attempts to share")
  void shareSnippetWhenNotOwnerShouldReturnForbidden() throws Exception {
    String sharePayload =
        """
        {
          "ownerId": "auth0|non-owner",
          "targetUserId": "%s",
          "snippetId": "%s"
        }
        """
            .formatted(USER_ID_2, SNIPPET_ID);

    mockMvc
        .perform(
            post("/permissions/share")
                .contentType(MediaType.APPLICATION_JSON)
                .content(sharePayload))
        .andExpect(status().isForbidden());
  }

  @Test
  @DisplayName("GET /permissions/user/{userId} returns snippet UUIDs")
  void getLegacySnippetsForUserShouldReturnSnippetUuidList() throws Exception {
    String payload =
        """
        {
          "snippetId": "%s",
          "userId": "%s",
          "role": "OWNER"
        }
        """
            .formatted(SNIPPET_ID, USER_ID);

    mockMvc
        .perform(post("/permissions").contentType(MediaType.APPLICATION_JSON).content(payload))
        .andExpect(status().isCreated());

    mockMvc
        .perform(get("/permissions/user/{userId}", USER_ID))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(1)))
        .andExpect(jsonPath("$[0]", is(SNIPPET_ID)));
  }

  @Test
  @DisplayName("GET /permissions/can-read returns canRead true/false")
  void canReadWhenHasPermissionShouldReturnTrue() throws Exception {
    String payload =
        """
        {
          "snippetId": "%s",
          "userId": "%s",
          "role": "READ"
        }
        """
            .formatted(SNIPPET_ID, USER_ID);

    mockMvc
        .perform(post("/permissions").contentType(MediaType.APPLICATION_JSON).content(payload))
        .andExpect(status().isCreated());

    mockMvc
        .perform(
            get("/permissions/can-read").param("userId", USER_ID).param("snippetId", SNIPPET_ID))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.canRead", is(true)));

    mockMvc
        .perform(
            get("/permissions/can-read")
                .param("userId", "auth0|unknown")
                .param("snippetId", SNIPPET_ID))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.canRead", is(false)));
  }

  @Test
  @DisplayName("GET /permissions/can-write returns canWrite true for OWNER, false for READ")
  void canWriteWhenOwnerShouldReturnTrue() throws Exception {
    String payload =
        """
        {
          "snippetId": "%s",
          "userId": "%s",
          "role": "OWNER"
        }
        """
            .formatted(SNIPPET_ID, USER_ID);

    mockMvc
        .perform(post("/permissions").contentType(MediaType.APPLICATION_JSON).content(payload))
        .andExpect(status().isCreated());

    mockMvc
        .perform(
            get("/permissions/can-write").param("userId", USER_ID).param("snippetId", SNIPPET_ID))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.canWrite", is(true)));

    mockMvc
        .perform(
            get("/permissions/can-write")
                .param("userId", "auth0|unknown")
                .param("snippetId", SNIPPET_ID))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.canWrite", is(false)));
  }

  @Test
  @DisplayName("GET /permissions/snippet/{snippetId} (legacy singular) returns user permissions")
  void getUsersOfSnippetLegacySingularEndpointShouldReturnList() throws Exception {
    String payload =
        """
        {
          "snippetId": "%s",
          "userId": "%s",
          "role": "OWNER"
        }
        """
            .formatted(SNIPPET_ID, USER_ID);

    mockMvc
        .perform(post("/permissions").contentType(MediaType.APPLICATION_JSON).content(payload))
        .andExpect(status().isCreated());

    mockMvc
        .perform(get("/permissions/snippet/{snippetId}", SNIPPET_ID))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(1)))
        .andExpect(jsonPath("$[0].userId", is(USER_ID)))
        .andExpect(jsonPath("$[0].role", is("OWNER")));
  }

  @Test
  @DisplayName("DELETE /permissions/snippet/{snippetId} (legacy singular) deletes permissions")
  void deleteSnippetLegacySingularEndpointShouldReturnNoContent() throws Exception {
    String payload =
        """
        {
          "snippetId": "%s",
          "userId": "%s",
          "role": "OWNER"
        }
        """
            .formatted(SNIPPET_ID, USER_ID);

    mockMvc
        .perform(post("/permissions").contentType(MediaType.APPLICATION_JSON).content(payload))
        .andExpect(status().isCreated());

    mockMvc
        .perform(delete("/permissions/snippet/{snippetId}", SNIPPET_ID))
        .andExpect(status().isNoContent());

    mockMvc
        .perform(
            get("/permissions/check")
                .param("userId", USER_ID)
                .param("snippetId", SNIPPET_ID)
                .param("action", "READ"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.allowed", is(false)))
        .andExpect(jsonPath("$.role", nullValue()));
  }
}
