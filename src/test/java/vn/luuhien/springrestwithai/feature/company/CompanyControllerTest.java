package vn.luuhien.springrestwithai.feature.company;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
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
class CompanyControllerTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private CompanyRepository companyRepository;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();
        companyRepository.deleteAll();
    }

    @Test
    @DisplayName("POST /companies success")
    @WithMockUser
    void createCompany_success_return201() throws Exception {
        String requestBody = """
                {
                  "name":"FPT Software",
                  "description":"IT outsourcing",
                  "address":"Ha Noi",
                  "logo":"/logos/fpt.png"
                }
                """;

        mockMvc.perform(post("/api/v1/companies")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.statusCode").value(201))
                .andExpect(jsonPath("$.message").value("Tạo công ty mới thành công"))
                .andExpect(jsonPath("$.data.id").isNumber());
    }

    @Test
    @DisplayName("POST /companies validation error")
    @WithMockUser
    void createCompany_validationError_return400() throws Exception {
        String requestBody = """
                {
                  "name":""
                }
                """;

        mockMvc.perform(post("/api/v1/companies")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.statusCode").value(400))
                .andExpect(jsonPath("$.details").isArray());
    }

    @Test
    @DisplayName("POST /companies duplicate")
    @WithMockUser
    void createCompany_duplicate_return409() throws Exception {
        Company company = new Company();
        company.setName("FPT Software");
        company.setDescription("IT outsourcing");
        company.setAddress("Ha Noi");
        company.setLogo("/logos/fpt.png");
        companyRepository.save(company);

        String requestBody = """
                {
                  "name":"fpt software",
                  "description":"Duplicate",
                  "address":"Ha Noi",
                  "logo":"/logos/fpt2.png"
                }
                """;

        mockMvc.perform(post("/api/v1/companies")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.statusCode").value(409));
    }

    @Test
    @DisplayName("POST /companies unauthorized")
    void createCompany_unauthorized_return401() throws Exception {
        String requestBody = """
                {
                  "name":"FPT Software"
                }
                """;

        mockMvc.perform(post("/api/v1/companies")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /companies/{id} success")
    @WithMockUser
    void getCompanyById_success_return200() throws Exception {
        Company company = new Company();
        company.setName("LuuHienIT");
        company.setDescription("Education platform");
        company.setAddress("HCM");
        company.setLogo("/logos/lh.png");
        Company saved = companyRepository.save(company);

        mockMvc.perform(get("/api/v1/companies/{id}", saved.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value(200))
                .andExpect(jsonPath("$.data.id").value(saved.getId()));
    }

    @Test
    @DisplayName("GET /companies/{id} not found")
    @WithMockUser
    void getCompanyById_notFound_return404() throws Exception {
        mockMvc.perform(get("/api/v1/companies/{id}", 999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.statusCode").value(404));
    }

    @Test
    @DisplayName("GET /companies/{id} unauthorized")
    void getCompanyById_unauthorized_return401() throws Exception {
        mockMvc.perform(get("/api/v1/companies/{id}", 1L))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /companies returns paginated data")
    @WithMockUser
    void getAllCompanies_paginated_return200() throws Exception {
        Company c1 = new Company();
        c1.setName("LuuHienIT");

        Company c2 = new Company();
        c2.setName("FPT Software");

        companyRepository.save(c1);
        companyRepository.save(c2);

        mockMvc.perform(get("/api/v1/companies?page=0&size=1&sort=id,asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value(200))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.size").value(1));
    }

    @Test
    @DisplayName("PUT /companies success")
    @WithMockUser
    void updateCompany_success_return200() throws Exception {
        Company company = new Company();
        company.setName("FPT Software");
        company.setDescription("IT outsourcing");
        company.setAddress("Ha Noi");
        company.setLogo("/logos/fpt.png");
        Company saved = companyRepository.save(company);

        String requestBody = """
                {
                  "id":%d,
                  "name":"FPT Software Updated",
                  "description":"Technology services",
                  "address":"Da Nang",
                  "logo":"/logos/fpt-new.png"
                }
                """.formatted(saved.getId());

        mockMvc.perform(put("/api/v1/companies")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value(200))
                .andExpect(jsonPath("$.data.name").value("FPT Software Updated"));
    }

    @Test
    @DisplayName("PUT /companies validation error")
    @WithMockUser
    void updateCompany_validationError_return400() throws Exception {
        String requestBody = """
                {
                  "id":null,
                  "name":""
                }
                """;

        mockMvc.perform(put("/api/v1/companies")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.statusCode").value(400));
    }

    @Test
    @DisplayName("PUT /companies not found")
    @WithMockUser
    void updateCompany_notFound_return404() throws Exception {
        String requestBody = """
                {
                  "id":999,
                  "name":"Not Exists",
                  "description":"N/A",
                  "address":"N/A",
                  "logo":"/logos/nope.png"
                }
                """;

        mockMvc.perform(put("/api/v1/companies")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.statusCode").value(404));
    }

    @Test
    @DisplayName("DELETE /companies/{id} success")
    @WithMockUser
    void deleteCompany_success_return200() throws Exception {
        Company company = new Company();
        company.setName("Delete Me");
        Company saved = companyRepository.save(company);

        mockMvc.perform(delete("/api/v1/companies/{id}", saved.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value(200));
    }

    @Test
    @DisplayName("DELETE /companies/{id} not found")
    @WithMockUser
    void deleteCompany_notFound_return404() throws Exception {
        mockMvc.perform(delete("/api/v1/companies/{id}", 999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.statusCode").value(404));
    }
}
