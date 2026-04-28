package vn.luuhien.springrestwithai.feature.auth;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;

import vn.luuhien.springrestwithai.feature.role.Role;
import vn.luuhien.springrestwithai.feature.role.RoleRepository;
import vn.luuhien.springrestwithai.feature.user.User;
import vn.luuhien.springrestwithai.feature.user.UserRepository;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        refreshTokenRepository.deleteAll();
        userRepository.deleteAll();
        roleRepository.deleteAll();
    }

    @Test
    @DisplayName("POST /auth/register success")
    void register_success_return201() throws Exception {
        ensureUserRoleExists();

        String requestBody = """
                {
                  "name":"Nguyen Van A",
                  "email":"new-user@example.com",
                  "password":"password123",
                  "age":25,
                  "address":"Ho Chi Minh",
                  "gender":"MALE"
                }
                """;

        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.statusCode").value(201))
                .andExpect(jsonPath("$.data.email").value("new-user@example.com"))
                .andExpect(jsonPath("$.data.roles").isArray());
    }

    @Test
    @DisplayName("POST /auth/login success")
    void login_success_return200() throws Exception {
        Role userRole = ensureUserRoleExists();
        createUser("login-user@example.com", "password123", userRole);

        String requestBody = """
                {
                  "email":"login-user@example.com",
                  "password":"password123"
                }
                """;

        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.SET_COOKIE, org.hamcrest.Matchers.containsString("refresh_token=")))
                .andExpect(jsonPath("$.statusCode").value(200))
                .andExpect(jsonPath("$.data.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.data.refreshToken").isNotEmpty());
    }

    @Test
    @DisplayName("GET /auth/me success")
    void me_success_return200() throws Exception {
        Role userRole = ensureUserRoleExists();
        createUser("me-user@example.com", "password123", userRole);

        String loginBody = """
                {
                  "email":"me-user@example.com",
                  "password":"password123"
                }
                """;

        String loginResponse = mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginBody))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String accessToken = com.jayway.jsonpath.JsonPath.read(loginResponse, "$.data.accessToken");

        mockMvc.perform(get("/api/v1/auth/me")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value(200))
                .andExpect(jsonPath("$.data.email").value("me-user@example.com"));
    }

    @Test
    @DisplayName("GET /auth/me unauthorized")
    void me_unauthorized_return401() throws Exception {
        mockMvc.perform(get("/api/v1/auth/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Full auth flow: register -> login -> me -> refresh -> logout -> refresh fail")
    void fullAuthFlow_successThenRefreshFailAfterLogout() throws Exception {
        ensureUserRoleExists();

        String registerBody = """
                {
                  "name":"Flow User",
                  "email":"flow-user@example.com",
                  "password":"password123",
                  "age":26,
                  "address":"Ho Chi Minh",
                  "gender":"FEMALE"
                }
                """;

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerBody))
                .andExpect(status().isCreated());

        String loginBody = """
                {
                  "email":"flow-user@example.com",
                  "password":"password123"
                }
                """;

        String loginResponse = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.SET_COOKIE, org.hamcrest.Matchers.containsString("refresh_token=")))
                .andReturn()
                .getResponse()
                .getContentAsString();

        String accessToken = com.jayway.jsonpath.JsonPath.read(loginResponse, "$.data.accessToken");
        String refreshToken = com.jayway.jsonpath.JsonPath.read(loginResponse, "$.data.refreshToken");

        mockMvc.perform(get("/api/v1/auth/me")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.email").value("flow-user@example.com"));

        String refreshResponse = mockMvc.perform(post("/api/v1/auth/refresh")
                        .cookie(new jakarta.servlet.http.Cookie("refresh_token", refreshToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.SET_COOKIE, org.hamcrest.Matchers.containsString("refresh_token=")))
                .andReturn()
                .getResponse()
                .getContentAsString();

        String refreshedAccessToken = com.jayway.jsonpath.JsonPath.read(refreshResponse, "$.data.accessToken");
        String refreshedRefreshToken = com.jayway.jsonpath.JsonPath.read(refreshResponse, "$.data.refreshToken");

        mockMvc.perform(post("/api/v1/auth/logout")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + refreshedAccessToken))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.SET_COOKIE, org.hamcrest.Matchers.containsString("Max-Age=0")));

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .cookie(new jakarta.servlet.http.Cookie("refresh_token", refreshedRefreshToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized());
    }

    private Role ensureUserRoleExists() {
        return roleRepository.findFirstByNameIgnoreCase("USER")
                .orElseGet(() -> {
                    Role role = new Role();
                    role.setName("USER");
                    role.setDescription("Default user role");
                    return roleRepository.save(role);
                });
    }

    private void createUser(String email, String rawPassword, Role role) {
        User user = new User();
        user.setName("Auth User");
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setRoles(java.util.List.of(role));
        userRepository.save(user);
    }
}
