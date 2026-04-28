package vn.luuhien.springrestwithai.feature.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import vn.luuhien.springrestwithai.feature.company.Company;
import vn.luuhien.springrestwithai.feature.company.CompanyRepository;
import vn.luuhien.springrestwithai.feature.role.Role;
import vn.luuhien.springrestwithai.feature.role.RoleRepository;
import vn.luuhien.springrestwithai.support.TestDataFactory;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class UserControllerTest {

    private MockMvc mockMvc;
    private String allowedToken;
    private String forbiddenToken;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private TestDataFactory testDataFactory;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();

        userRepository.deleteAll();
        roleRepository.deleteAll();
        companyRepository.deleteAll();

        allowedToken = testDataFactory.createUserTokenWithPermissions(
                "allowed-user@example.com",
                List.of(
                        new TestDataFactory.EndpointPermission("/api/v1/users", "POST", "USER"),
                        new TestDataFactory.EndpointPermission("/api/v1/users/**", "GET", "USER")
                ));
        forbiddenToken = testDataFactory.createUserTokenWithoutPermissions("forbidden-user@example.com");
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("POST /users success")
    void createUser_success_return201() throws Exception {
        Company company = createCompany("LuuHienIT");
        Role role = createRole("HR");

        String requestBody = """
                {
                  "name":"Nguyen Van A",
                  "email":"a@example.com",
                  "password":"password123",
                  "age":25,
                  "address":"HCM",
                  "gender":"MALE",
                  "companyId":%d,
                  "roleIds":[%d]
                }
                """.formatted(company.getId(), role.getId());

        mockMvc.perform(post("/api/v1/users")
                        .header("Authorization", "Bearer " + allowedToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.statusCode").value(201))
                .andExpect(jsonPath("$.message").value("Tạo người dùng mới thành công"))
                .andExpect(jsonPath("$.data.id").isNumber())
                .andExpect(jsonPath("$.data.company.id").value(company.getId()));
    }

    @Test
    @DisplayName("POST /users validation error")
    void createUser_validationError_return400() throws Exception {
        String requestBody = """
                {
                  "name":"",
                  "email":"invalid-email",
                  "password":"123",
                  "roleIds":[]
                }
                """;

        mockMvc.perform(post("/api/v1/users")
                        .header("Authorization", "Bearer " + allowedToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.statusCode").value(400))
                .andExpect(jsonPath("$.details").isArray());
    }

    @Test
    @DisplayName("POST /users duplicate")
    void createUser_duplicate_return409() throws Exception {
        Company company = createCompany("LuuHienIT");
        Role role = createRole("HR");

        User existing = new User();
        existing.setName("Existing User");
        existing.setEmail("dup@example.com");
        existing.setPassword("encoded");
        existing.setCompany(company);
        existing.setRoles(List.of(role));
        userRepository.save(existing);

        String requestBody = """
                {
                  "name":"Nguyen Van B",
                  "email":"dup@example.com",
                  "password":"password123",
                  "roleIds":[%d]
                }
                """.formatted(role.getId());

        mockMvc.perform(post("/api/v1/users")
                        .header("Authorization", "Bearer " + allowedToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.statusCode").value(409));
    }

    @Test
    @DisplayName("POST /users unauthorized")
    void createUser_unauthorized_return401() throws Exception {
        String requestBody = """
                {
                  "name":"Nguyen Van A",
                  "email":"a@example.com",
                  "password":"password123",
                  "roleIds":[1]
                }
                """;

        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /users/{id} success")
    void getUserById_success_return200() throws Exception {
        Company company = createCompany("LuuHienIT");
        Role role = createRole("HR");
        User user = createUser("Nguyen Van A", "a@example.com", company, role);

        mockMvc.perform(get("/api/v1/users/{id}", user.getId())
                        .header("Authorization", "Bearer " + allowedToken))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.statusCode").value(200))
                .andExpect(jsonPath("$.data.id").value(user.getId()))
                .andExpect(jsonPath("$.data.email").value("a@example.com"));
    }

    @Test
    @DisplayName("GET /users/{id} not found")
    void getUserById_notFound_return404() throws Exception {
        mockMvc.perform(get("/api/v1/users/{id}", 999L)
                        .header("Authorization", "Bearer " + allowedToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.statusCode").value(404));
    }

    @Test
    @DisplayName("GET /users/{id} forbidden")
    void getUserById_forbidden_return403() throws Exception {
        mockMvc.perform(get("/api/v1/users/{id}", 1L)
                        .header("Authorization", "Bearer " + forbiddenToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /users/{id} unauthorized")
    void getUserById_unauthorized_return401() throws Exception {
        mockMvc.perform(get("/api/v1/users/{id}", 1L))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /users returns paginated data")
    void getAllUsers_paginated_return200() throws Exception {
        Company company = createCompany("LuuHienIT");
        Role role = createRole("USER");

        createUser("User A", "a@example.com", company, role);
        createUser("User B", "b@example.com", company, role);

        mockMvc.perform(get("/api/v1/users?page=1&size=1&sort=id,asc")
                        .header("Authorization", "Bearer " + allowedToken))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.statusCode").value(200))
                .andExpect(jsonPath("$.data.result").isArray())
                .andExpect(jsonPath("$.data.meta.page").value(1))
                .andExpect(jsonPath("$.data.meta.pageSize").value(1))
                .andExpect(jsonPath("$.data.meta.total").value(2));
    }

    @Test
    @DisplayName("GET /users filter by name, address, email, age, gender")
    void getAllUsers_filterFields_return200() throws Exception {
        Company company = createCompany("LuuHienIT");
        Role role = createRole("USER");

        User matched = createUser("Nguyen Van A", "a@example.com", company, role);
        matched.setAddress("HCM");
        matched.setAge(25);
        matched.setGender(User.GenderEnum.MALE);
        userRepository.save(matched);

        User other = createUser("Tran Thi B", "b@example.com", company, role);
        other.setAddress("Ha Noi");
        other.setAge(30);
        other.setGender(User.GenderEnum.FEMALE);
        userRepository.save(other);

        mockMvc.perform(get("/api/v1/users?name=nguyen&address=hcm&email=a@example.com&ageFrom=25&ageTo=25&gender=MALE")
                        .header("Authorization", "Bearer " + allowedToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value(200))
                .andExpect(jsonPath("$.data.meta.total").value(1))
                .andExpect(jsonPath("$.data.result[0].email").value("a@example.com"));
    }

    private Company createCompany(String name) {
        Company company = new Company();
        company.setName(name);
        return companyRepository.save(company);
    }

    private Role createRole(String name) {
        Role role = new Role();
        role.setName(name);
        return roleRepository.save(role);
    }

    private User createUser(String name, String email, Company company, Role role) {
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setPassword("encoded-password");
        user.setCompany(company);
        user.setRoles(List.of(role));
        return userRepository.save(user);
    }
}
