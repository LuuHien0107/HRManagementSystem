package vn.luuhien.springrestwithai.feature.file;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import vn.luuhien.springrestwithai.config.UploadProperties;
import vn.luuhien.springrestwithai.exception.InvalidRequestException;
import vn.luuhien.springrestwithai.feature.file.dto.FileUploadResponse;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FileServiceImplTest {

    @Mock
    private UploadProperties uploadProperties;

    @InjectMocks
    private FileServiceImpl fileService;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        lenient().when(uploadProperties.getBaseDir()).thenReturn(tempDir.toString());
        lenient().when(uploadProperties.getMaxSizeBytes()).thenReturn(5_242_880L);
        lenient().when(uploadProperties.getAllowedExtensions())
                .thenReturn(List.of("jpg", "jpeg", "png", "gif", "webp"));
        lenient().when(uploadProperties.getAllowedFolders()).thenReturn(List.of("avatars", "logos"));
    }

    @Test
    @DisplayName("uploadFile with valid input should store file and return metadata")
    void uploadFile_validInput_shouldStoreFileAndReturnMetadata() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "avatar.jpg",
                "image/jpeg",
                "hello-image".getBytes());

        FileUploadResponse response = fileService.uploadFile(file, "avatars");

        assertEquals("avatars", response.folder());
        assertTrue(response.fileName().endsWith("_avatar.jpg"));
        assertTrue(response.fileUrl().startsWith("/uploads/avatars/"));
        assertEquals(file.getSize(), response.size());
        assertTrue(Files.exists(tempDir.resolve("avatars").resolve(response.fileName())));
    }

    @Test
    @DisplayName("uploadFile with empty file should throw InvalidRequestException")
    void uploadFile_emptyFile_shouldThrowInvalidRequestException() {
        MockMultipartFile emptyFile = new MockMultipartFile("file", "empty.jpg", "image/jpeg", new byte[0]);

        InvalidRequestException ex = assertThrows(InvalidRequestException.class,
                () -> fileService.uploadFile(emptyFile, "avatars"));

        assertEquals("No file provided", ex.getMessage());
    }

    @Test
    @DisplayName("uploadFile with invalid folder should throw InvalidRequestException")
    void uploadFile_invalidFolder_shouldThrowInvalidRequestException() {
        MockMultipartFile file = new MockMultipartFile("file", "avatar.jpg", "image/jpeg", "abc".getBytes());

        InvalidRequestException ex = assertThrows(InvalidRequestException.class,
                () -> fileService.uploadFile(file, "documents"));

        assertEquals("Folder must be avatars or logos", ex.getMessage());
    }

    @Test
    @DisplayName("uploadFile with invalid file name should throw InvalidRequestException")
    void uploadFile_invalidFileName_shouldThrowInvalidRequestException() {
        MockMultipartFile file = new MockMultipartFile("file", "my avatar.jpg", "image/jpeg", "abc".getBytes());

        InvalidRequestException ex = assertThrows(InvalidRequestException.class,
                () -> fileService.uploadFile(file, "avatars"));

        assertEquals("File name contains invalid characters", ex.getMessage());
    }

    @Test
    @DisplayName("uploadFile with disallowed extension should throw InvalidRequestException")
    void uploadFile_disallowedExtension_shouldThrowInvalidRequestException() {
        MockMultipartFile file = new MockMultipartFile("file", "avatar.pdf", "application/pdf", "abc".getBytes());

        InvalidRequestException ex = assertThrows(InvalidRequestException.class,
                () -> fileService.uploadFile(file, "avatars"));

        assertEquals("File extension not allowed (only jpg/jpeg/png/gif/webp)", ex.getMessage());
    }

    @Test
    @DisplayName("uploadFile with oversize file should throw InvalidRequestException")
    void uploadFile_oversizeFile_shouldThrowInvalidRequestException() {
        byte[] content = new byte[5_242_881];
        MockMultipartFile file = new MockMultipartFile("file", "avatar.jpg", "image/jpeg", content);

        InvalidRequestException ex = assertThrows(InvalidRequestException.class,
                () -> fileService.uploadFile(file, "avatars"));

        assertEquals("File size exceeds 5 MB", ex.getMessage());
    }
}
