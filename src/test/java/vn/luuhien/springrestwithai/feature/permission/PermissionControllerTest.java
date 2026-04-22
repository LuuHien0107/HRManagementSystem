package vn.luuhien.springrestwithai.feature.permission;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")

class PermissionControllerTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private PermissionRepository permissionRepository;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();
        permissionRepository.deleteAll();
    }

    @Test
    @DisplayName("POST /permissions success")
    @WithMockUser
    void createPermission_success_return201() throws Exception {
        String requestBody = "{\"name\":\"CREATE_USER\",\"apiPath\":\"/users\",\"method\":\"POST\",\"module\":\"USER\"}";

        mockMvc.perform(post("/api/v1/permissions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.statusCode").value(201))
                .andExpect(jsonPath("$.message").value("Tạo quyền mới thành công"))
                .andExpect(jsonPath("$.data.id").isNumber());
    }

    @Test
    @DisplayName("POST /permissions validation error")
    @WithMockUser
    void createPermission_validationError_return400() throws Exception {
        String requestBody = "{\"name\":\"\",\"apiPath\":\"\",\"method\":\"TRACE\",\"module\":\"\"}";

        mockMvc.perform(post("/api/v1/permissions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.statusCode").value(400))
                .andExpect(jsonPath("$.details").isArray());
    }

    @Test
    @DisplayName("POST /permissions duplicate")
    @WithMockUser
    void createPermission_duplicate_return409() throws Exception {
        Permission permission = new Permission();
        permission.setName("CREATE_USER");
        permission.setApiPath("/users");
        permission.setMethod("POST");
        permission.setModule("USER");
        permissionRepository.save(permission);

        String requestBody = "{\"name\":\"CREATE_USER_DUP\",\"apiPath\":\"/users\",\"method\":\"POST\",\"module\":\"USER\"}";

        mockMvc.perform(post("/api/v1/permissions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.statusCode").value(409));
    }

    @Test
    @DisplayName("POST /permissions unauthorized")
    void createPermission_unauthorized_return401() throws Exception {
        String requestBody = "{\"name\":\"CREATE_USER\",\"apiPath\":\"/users\",\"method\":\"POST\",\"module\":\"USER\"}";

        mockMvc.perform(post("/api/v1/permissions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /permissions/{id} success")
    @WithMockUser
    void getPermissionById_success_return200() throws Exception {
        Permission permission = new Permission();
        permission.setName("VIEW_USER");
        permission.setApiPath("/users");
        permission.setMethod("GET");
        permission.setModule("USER");
        Permission saved = permissionRepository.save(permission);

        mockMvc.perform(get("/api/v1/permissions/{id}", saved.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value(200))
                .andExpect(jsonPath("$.data.id").value(saved.getId()));
    }

    @Test
    @DisplayName("GET /permissions/{id} not found")
    @WithMockUser
    void getPermissionById_notFound_return404() throws Exception {
        mockMvc.perform(get("/api/v1/permissions/{id}", 999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.statusCode").value(404));
    }

    @Test
    @DisplayName("GET /permissions/{id} unauthorized")
    void getPermissionById_unauthorized_return401() throws Exception {
        mockMvc.perform(get("/api/v1/permissions/{id}", 1L))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /permissions returns paginated data")
    @WithMockUser
    void getAllPermissions_paginated_return200() throws Exception {
        Permission p1 = new Permission();
        p1.setName("VIEW_USER");
        p1.setApiPath("/users");
        p1.setMethod("GET");
        p1.setModule("USER");

        Permission p2 = new Permission();
        p2.setName("CREATE_USER");
        p2.setApiPath("/users");
        p2.setMethod("POST");
        p2.setModule("USER");

        permissionRepository.save(p1);
        permissionRepository.save(p2);

        mockMvc.perform(get("/api/v1/permissions?page=0&size=1&sort=id,asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value(200))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.size").value(1));
    }

    @Test
    @DisplayName("PUT /permissions success")
    @WithMockUser
    void updatePermission_success_return200() throws Exception {
        Permission permission = new Permission();
        permission.setName("CREATE_USER");
        permission.setApiPath("/users");
        permission.setMethod("POST");
        permission.setModule("USER");
        Permission saved = permissionRepository.save(permission);

        String requestBody = "{\"id\":%d,\"name\":\"UPDATE_USER\",\"apiPath\":\"/users\",\"method\":\"PUT\",\"module\":\"USER\"}"
                .formatted(saved.getId());

        mockMvc.perform(put("/api/v1/permissions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value(200))
                .andExpect(jsonPath("$.data.method").value("PUT"));
    }

    @Test
    @DisplayName("PUT /permissions validation error")
    @WithMockUser
    void updatePermission_validationError_return400() throws Exception {
        String requestBody = "{\"id\":null,\"name\":\"\",\"apiPath\":\"\",\"method\":\"TRACE\",\"module\":\"\"}";

        mockMvc.perform(put("/api/v1/permissions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.statusCode").value(400));
    }

    @Test
    @DisplayName("PUT /permissions not found")
    @WithMockUser
    void updatePermission_notFound_return404() throws Exception {
        String requestBody = "{\"id\":999,\"name\":\"UPDATE_USER\",\"apiPath\":\"/users\",\"method\":\"PUT\",\"module\":\"USER\"}";

        mockMvc.perform(put("/api/v1/permissions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.statusCode").value(404));
    }

    @Test
    @DisplayName("PUT /permissions unauthorized")
    void updatePermission_unauthorized_return401() throws Exception {
        String requestBody = "{\"id\":1,\"name\":\"UPDATE_USER\",\"apiPath\":\"/users\",\"method\":\"PUT\",\"module\":\"USER\"}";

        mockMvc.perform(put("/api/v1/permissions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("DELETE /permissions/{id} success")
    @WithMockUser
    void deletePermission_success_return200() throws Exception {
        Permission permission = new Permission();
        permission.setName("DELETE_USER");
        permission.setApiPath("/users/{id}");
        permission.setMethod("DELETE");
        permission.setModule("USER");
        Permission saved = permissionRepository.save(permission);

        mockMvc.perform(delete("/api/v1/permissions/{id}", saved.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value(200));
    }

    @Test
    @DisplayName("DELETE /permissions/{id} not found")
    @WithMockUser
    void deletePermission_notFound_return404() throws Exception {
        mockMvc.perform(delete("/api/v1/permissions/{id}", 999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.statusCode").value(404));
    }

    @Test
    @DisplayName("DELETE /permissions/{id} unauthorized")
    void deletePermission_unauthorized_return401() throws Exception {
        mockMvc.perform(delete("/api/v1/permissions/{id}", 1L))
                .andExpect(status().isUnauthorized());
    }
}
