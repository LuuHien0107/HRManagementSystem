package vn.luuhien.springrestwithai.feature.file;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import vn.luuhien.springrestwithai.support.TestDataFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
class FileControllerTest {

    private MockMvc mockMvc;
    private String allowedToken;
    private String forbiddenToken;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private TestDataFactory testDataFactory;

    @BeforeEach
    void setUp() throws IOException {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();

        allowedToken = testDataFactory.createUserTokenWithPermissions(
                "file-allowed-" + UUID.randomUUID() + "@example.com",
                List.of(new TestDataFactory.EndpointPermission("/api/v1/files", "POST", "FILE")));
        forbiddenToken = testDataFactory.createUserTokenWithoutPermissions(
                "file-forbidden-" + UUID.randomUUID() + "@example.com");

        Path baseDir = Paths.get("target/test-uploads");
        if (Files.exists(baseDir)) {
            Files.walk(baseDir)
                    .sorted(Comparator.reverseOrder())
                    .forEach(path -> {
                        try {
                            Files.delete(path);
                        } catch (IOException ignored) {
                        }
                    });
        }
    }

    @Test
    @DisplayName("POST /files upload success should return 201")
    void uploadFile_success_shouldReturn201() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "avatar.jpg",
                "image/jpeg",
                "sample-content".getBytes());

        mockMvc.perform(multipart("/api/v1/files")
                        .file(file)
                        .header("Authorization", "Bearer " + allowedToken)
                        .param("folder", "avatars"))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.statusCode").value(201))
                .andExpect(jsonPath("$.message").value("File uploaded"))
                .andExpect(jsonPath("$.data.fileName").isString())
                .andExpect(jsonPath("$.data.folder").value("avatars"))
                .andExpect(jsonPath("$.data.fileUrl").value(org.hamcrest.Matchers.startsWith("/uploads/avatars/")))
                .andExpect(jsonPath("$.data.size").value(file.getSize()));
    }

    @Test
    @DisplayName("POST /files with no file should return 400")
    void uploadFile_noFile_shouldReturn400() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "",
                "application/octet-stream",
                new byte[0]);

        mockMvc.perform(multipart("/api/v1/files")
                        .file(file)
                        .header("Authorization", "Bearer " + allowedToken)
                        .param("folder", "avatars"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.statusCode").value(400))
                .andExpect(jsonPath("$.message").value("No file provided"));
    }

    @Test
    @DisplayName("POST /files with invalid file name should return 400")
    void uploadFile_invalidFileName_shouldReturn400() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "my avatar.jpg",
                "image/jpeg",
                "sample-content".getBytes());

        mockMvc.perform(multipart("/api/v1/files")
                        .file(file)
                        .header("Authorization", "Bearer " + allowedToken)
                        .param("folder", "avatars"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.statusCode").value(400))
                .andExpect(jsonPath("$.message").value("File name contains invalid characters"));
    }

    @Test
    @DisplayName("POST /files with invalid extension should return 400")
    void uploadFile_invalidExtension_shouldReturn400() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "script.exe",
                "application/octet-stream",
                "sample-content".getBytes());

        mockMvc.perform(multipart("/api/v1/files")
                        .file(file)
                        .header("Authorization", "Bearer " + allowedToken)
                        .param("folder", "avatars"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.statusCode").value(400))
                .andExpect(jsonPath("$.message").value("File extension not allowed (only jpg/jpeg/png/gif/webp)"));
    }

    @Test
    @DisplayName("POST /files with invalid folder should return 400")
    void uploadFile_invalidFolder_shouldReturn400() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "avatar.jpg",
                "image/jpeg",
                "sample-content".getBytes());

        mockMvc.perform(multipart("/api/v1/files")
                        .file(file)
                        .header("Authorization", "Bearer " + allowedToken)
                        .param("folder", "docs"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.statusCode").value(400))
                .andExpect(jsonPath("$.message").value("Folder must be avatars or logos"));
    }

    @Test
    @DisplayName("POST /files oversize should return 400")
    void uploadFile_oversize_shouldReturn400() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "avatar.jpg",
                "image/jpeg",
                new byte[5_242_881]);

        mockMvc.perform(multipart("/api/v1/files")
                        .file(file)
                        .header("Authorization", "Bearer " + allowedToken)
                        .param("folder", "avatars"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.statusCode").value(400))
                .andExpect(jsonPath("$.message").value("File size exceeds 5 MB"));
    }

    @Test
    @DisplayName("POST /files unauthorized should return 401")
    void uploadFile_unauthorized_shouldReturn401() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "avatar.jpg",
                "image/jpeg",
                "sample-content".getBytes());

        mockMvc.perform(multipart("/api/v1/files")
                        .file(file)
                        .param("folder", "avatars"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /files forbidden should return 403")
    void uploadFile_forbidden_shouldReturn403() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "avatar.jpg",
                "image/jpeg",
                "sample-content".getBytes());

        mockMvc.perform(multipart("/api/v1/files")
                        .file(file)
                        .header("Authorization", "Bearer " + forbiddenToken)
                        .param("folder", "avatars"))
                .andExpect(status().isForbidden());
    }
}
