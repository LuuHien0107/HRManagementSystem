package vn.luuhien.springrestwithai.feature.role;

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
import vn.luuhien.springrestwithai.feature.permission.Permission;
import vn.luuhien.springrestwithai.feature.permission.PermissionRepository;
import vn.luuhien.springrestwithai.feature.user.UserRepository;
import vn.luuhien.springrestwithai.support.TestDataFactory;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class RoleControllerTest {

    private MockMvc mockMvc;
    private String allowedToken;
    private String forbiddenToken;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PermissionRepository permissionRepository;

    @Autowired
    private UserRepository userRepository;

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
        permissionRepository.deleteAll();

        allowedToken = testDataFactory.createUserTokenWithPermissions(
                "role-allowed@example.com",
                List.of(
                        new TestDataFactory.EndpointPermission("/api/v1/roles", "GET", "ROLE"),
                        new TestDataFactory.EndpointPermission("/api/v1/roles", "POST", "ROLE"),
                        new TestDataFactory.EndpointPermission("/api/v1/roles", "PUT", "ROLE"),
                        new TestDataFactory.EndpointPermission("/api/v1/roles/**", "GET", "ROLE"),
                        new TestDataFactory.EndpointPermission("/api/v1/roles/**", "DELETE", "ROLE")
                ));
        forbiddenToken = testDataFactory.createUserTokenWithoutPermissions("role-forbidden@example.com");

        userRepository.deleteAll();
        roleRepository.deleteAll();
        permissionRepository.deleteAll();
    }

    @Test
    @DisplayName("POST /roles success")
    void createRole_success_return201() throws Exception {
        Permission permission = createPermission("CREATE_USER", "/users", "POST", "USER");

        String requestBody = """
                {
                  "name":"HR",
                  "description":"Human resources",
                  "permissionIds":[%d]
                }
                """.formatted(permission.getId());

        mockMvc.perform(post("/api/v1/roles")
                        .header("Authorization", "Bearer " + allowedToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.statusCode").value(201))
                .andExpect(jsonPath("$.message").value("Tạo vai trò mới thành công"))
                .andExpect(jsonPath("$.data.id").isNumber())
                .andExpect(jsonPath("$.data.permissions").isArray());
    }

    @Test
    @DisplayName("POST /roles validation error")
    void createRole_validationError_return400() throws Exception {
        String requestBody = """
                {
                  "name":"",
                  "description":"x",
                  "permissionIds":null
                }
                """;

        mockMvc.perform(post("/api/v1/roles")
                        .header("Authorization", "Bearer " + allowedToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.statusCode").value(400))
                .andExpect(jsonPath("$.details").isArray());
    }

    @Test
    @DisplayName("POST /roles duplicate")
    void createRole_duplicate_return409() throws Exception {
        Permission permission = createPermission("CREATE_USER", "/users", "POST", "USER");

        Role role = new Role();
        role.setName("ADMIN");
        role.setDescription("System admin");
        role.setPermissions(List.of(permission));
        roleRepository.save(role);

        String requestBody = """
                {
                  "name":"admin",
                  "description":"Duplicate",
                  "permissionIds":[%d]
                }
                """.formatted(permission.getId());

        mockMvc.perform(post("/api/v1/roles")
                        .header("Authorization", "Bearer " + allowedToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.statusCode").value(409));
    }

    @Test
    @DisplayName("POST /roles unauthorized")
    void createRole_unauthorized_return401() throws Exception {
        String requestBody = """
                {
                  "name":"ADMIN",
                  "description":"System admin",
                  "permissionIds":[]
                }
                """;

        mockMvc.perform(post("/api/v1/roles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /roles forbidden")
    void createRole_forbidden_return403() throws Exception {
        String requestBody = """
                {
                  "name":"ADMIN",
                  "description":"System admin",
                  "permissionIds":[]
                }
                """;

        mockMvc.perform(post("/api/v1/roles")
                        .header("Authorization", "Bearer " + forbiddenToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /roles/{id} success")
    void getRoleById_success_return200() throws Exception {
        Permission permission = createPermission("VIEW_USERS", "/users", "GET", "USER");

        Role role = new Role();
        role.setName("MANAGER");
        role.setDescription("Department manager");
        role.setPermissions(List.of(permission));
        Role savedRole = roleRepository.save(role);

        mockMvc.perform(get("/api/v1/roles/{id}", savedRole.getId())
                        .header("Authorization", "Bearer " + allowedToken))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.statusCode").value(200))
                .andExpect(jsonPath("$.data.id").value(savedRole.getId()));
    }

    @Test
    @DisplayName("GET /roles/{id} not found")
    void getRoleById_notFound_return404() throws Exception {
        mockMvc.perform(get("/api/v1/roles/{id}", 999L)
                        .header("Authorization", "Bearer " + allowedToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.statusCode").value(404));
    }

    @Test
    @DisplayName("GET /roles/{id} unauthorized")
    void getRoleById_unauthorized_return401() throws Exception {
        mockMvc.perform(get("/api/v1/roles/{id}", 1L))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /roles returns paginated data")
    void getAllRoles_paginated_return200() throws Exception {
        Permission p1 = createPermission("CREATE_USER", "/users", "POST", "USER");
        Permission p2 = createPermission("VIEW_USERS", "/users", "GET", "USER");

        Role r1 = new Role();
        r1.setName("ADMIN");
        r1.setDescription("System admin");
        r1.setPermissions(List.of(p1, p2));

        Role r2 = new Role();
        r2.setName("HR");
        r2.setDescription("Human resources");
        r2.setPermissions(List.of(p2));

        roleRepository.save(r1);
        roleRepository.save(r2);

        mockMvc.perform(get("/api/v1/roles?page=1&size=1&sort=id,asc")
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
    @DisplayName("PUT /roles success")
    void updateRole_success_return200() throws Exception {
        Permission p1 = createPermission("CREATE_USER", "/users", "POST", "USER");
        Permission p2 = createPermission("UPDATE_USER", "/users", "PUT", "USER");

        Role role = new Role();
        role.setName("HR");
        role.setDescription("Old description");
        role.setPermissions(List.of(p1));
        Role savedRole = roleRepository.save(role);

        String requestBody = """
                {
                  "id":%d,
                  "name":"HR_UPDATED",
                  "description":"Updated description",
                  "permissionIds":[%d,%d]
                }
                """.formatted(savedRole.getId(), p1.getId(), p2.getId());

        mockMvc.perform(put("/api/v1/roles")
                        .header("Authorization", "Bearer " + allowedToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.statusCode").value(200))
                .andExpect(jsonPath("$.data.name").value("HR_UPDATED"))
                .andExpect(jsonPath("$.data.permissions").isArray());
    }

    @Test
    @DisplayName("PUT /roles validation error")
    void updateRole_validationError_return400() throws Exception {
        String requestBody = """
                {
                  "id":null,
                  "name":"",
                  "description":"x",
                  "permissionIds":null
                }
                """;

        mockMvc.perform(put("/api/v1/roles")
                        .header("Authorization", "Bearer " + allowedToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.statusCode").value(400));
    }

    @Test
    @DisplayName("PUT /roles not found")
    void updateRole_notFound_return404() throws Exception {
        String requestBody = """
                {
                  "id":999,
                  "name":"Not Exists",
                  "description":"N/A",
                  "permissionIds":[]
                }
                """;

        mockMvc.perform(put("/api/v1/roles")
                        .header("Authorization", "Bearer " + allowedToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.statusCode").value(404));
    }

    @Test
    @DisplayName("PUT /roles unauthorized")
    void updateRole_unauthorized_return401() throws Exception {
        String requestBody = """
                {
                  "id":1,
                  "name":"ADMIN",
                  "description":"System admin",
                  "permissionIds":[]
                }
                """;

        mockMvc.perform(put("/api/v1/roles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("DELETE /roles/{id} success")
    void deleteRole_success_return200() throws Exception {
        Role role = new Role();
        role.setName("DELETE_ME");
        role.setDescription("Temp role");
        Role savedRole = roleRepository.save(role);

        mockMvc.perform(delete("/api/v1/roles/{id}", savedRole.getId())
                        .header("Authorization", "Bearer " + allowedToken))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.statusCode").value(200));
    }

    @Test
    @DisplayName("DELETE /roles/{id} not found")
    void deleteRole_notFound_return404() throws Exception {
        mockMvc.perform(delete("/api/v1/roles/{id}", 999L)
                        .header("Authorization", "Bearer " + allowedToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.statusCode").value(404));
    }

    @Test
    @DisplayName("DELETE /roles/{id} unauthorized")
    void deleteRole_unauthorized_return401() throws Exception {
        mockMvc.perform(delete("/api/v1/roles/{id}", 1L))
                .andExpect(status().isUnauthorized());
    }

    private Permission createPermission(String name, String apiPath, String method, String module) {
        Permission permission = new Permission();
        permission.setName(name);
        permission.setApiPath(apiPath);
        permission.setMethod(method);
        permission.setModule(module);
        return permissionRepository.save(permission);
    }
}
