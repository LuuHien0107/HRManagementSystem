package vn.luuhien.springrestwithai.feature.user;

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

import vn.luuhien.springrestwithai.feature.company.Company;
import vn.luuhien.springrestwithai.feature.company.CompanyRepository;
import vn.luuhien.springrestwithai.feature.role.Role;
import vn.luuhien.springrestwithai.feature.role.RoleRepository;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class UserControllerMutationTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private RoleRepository roleRepository;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();

        userRepository.deleteAll();
        roleRepository.deleteAll();
        companyRepository.deleteAll();
    }

    @Test
    @DisplayName("PUT /users success")
    @WithMockUser
    void updateUser_success_return200() throws Exception {
        Company oldCompany = createCompany("Old Company");
        Company newCompany = createCompany("New Company");
        Role oldRole = createRole("HR");
        Role newRole = createRole("ADMIN");
        User user = createUser("Nguyen Van A", "a@example.com", oldCompany, oldRole);

        String requestBody = """
                {
                  "id":%d,
                  "name":"Nguyen Van A Updated",
                  "age":30,
                  "address":"Da Nang",
                  "gender":"FEMALE",
                  "avatar":"/avatars/a.png",
                  "companyId":%d,
                  "roleIds":[%d]
                }
                """.formatted(user.getId(), newCompany.getId(), newRole.getId());

        mockMvc.perform(put("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.statusCode").value(200))
                .andExpect(jsonPath("$.data.name").value("Nguyen Van A Updated"))
                .andExpect(jsonPath("$.data.company.id").value(newCompany.getId()))
                .andExpect(jsonPath("$.data.roles[0].id").value(newRole.getId()));
    }

    @Test
    @DisplayName("PUT /users validation error")
    @WithMockUser
    void updateUser_validationError_return400() throws Exception {
        String requestBody = """
                {
                  "id":null,
                  "name":"",
                  "age":0,
                  "roleIds":[]
                }
                """;

        mockMvc.perform(put("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.statusCode").value(400));
    }

    @Test
    @DisplayName("PUT /users not found")
    @WithMockUser
    void updateUser_notFound_return404() throws Exception {
        Role role = createRole("USER");

        String requestBody = """
                {
                  "id":999,
                  "name":"Not Exists",
                  "age":25,
                  "address":"N/A",
                  "gender":"OTHER",
                  "roleIds":[%d]
                }
                """.formatted(role.getId());

        mockMvc.perform(put("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.statusCode").value(404));
    }

    @Test
    @DisplayName("DELETE /users/{id} success")
    @WithMockUser
    void deleteUser_success_return200() throws Exception {
        Company company = createCompany("LuuHienIT");
        Role role = createRole("USER");
        User user = createUser("Delete Me", "delete@example.com", company, role);

        mockMvc.perform(delete("/api/v1/users/{id}", user.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.statusCode").value(200));
    }

    @Test
    @DisplayName("DELETE /users/{id} not found")
    @WithMockUser
    void deleteUser_notFound_return404() throws Exception {
        mockMvc.perform(delete("/api/v1/users/{id}", 999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.statusCode").value(404));
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
