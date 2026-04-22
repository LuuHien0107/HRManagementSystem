package vn.luuhien.springrestwithai.feature.file;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import vn.luuhien.springrestwithai.config.UploadProperties;
import vn.luuhien.springrestwithai.exception.InvalidRequestException;
import vn.luuhien.springrestwithai.feature.file.dto.FileUploadResponse;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class FileServiceImpl implements FileService {

    private static final Pattern FILE_NAME_PATTERN = Pattern.compile("^[A-Za-z0-9._-]+$");

    private final UploadProperties uploadProperties;

    public FileServiceImpl(UploadProperties uploadProperties) {
        this.uploadProperties = uploadProperties;
    }

    @Override
    @Transactional
    public FileUploadResponse uploadFile(MultipartFile file, String folder) {
        validateFilePresent(file);

        String normalizedFolder = normalizeFolder(folder);
        validateFolder(normalizedFolder);

        String originalFileName = normalizeFileName(file.getOriginalFilename());
        validateFileName(originalFileName);
        validateExtension(originalFileName);
        validateFileSize(file.getSize());

        String storedFileName = System.currentTimeMillis() + "_" + originalFileName;
        Path baseDir = Paths.get(uploadProperties.getBaseDir()).toAbsolutePath().normalize();
        Path targetDir = baseDir.resolve(normalizedFolder).normalize();
        Path targetPath = targetDir.resolve(storedFileName).normalize();

        if (!targetPath.startsWith(baseDir)) {
            throw new InvalidRequestException("Invalid file path");
        }

        try {
            Files.createDirectories(targetDir);
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ex) {
            throw new InvalidRequestException("Cannot store file");
        }

        return new FileUploadResponse(
                storedFileName,
                normalizedFolder,
                "/uploads/" + normalizedFolder + "/" + storedFileName,
                file.getSize(),
                Instant.now());
    }

    private void validateFilePresent(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new InvalidRequestException("No file provided");
        }
    }

    private String normalizeFolder(String folder) {
        return folder == null ? "" : folder.trim().toLowerCase(Locale.ROOT);
    }

    private void validateFolder(String folder) {
        Set<String> allowedFolders = normalizeSet(uploadProperties.getAllowedFolders());
        if (!allowedFolders.contains(folder)) {
            throw new InvalidRequestException("Folder must be avatars or logos");
        }
    }

    private String normalizeFileName(String originalFileName) {
        if (originalFileName == null) {
            return "";
        }
        String cleaned = StringUtils.cleanPath(originalFileName).trim();
        int slashIndex = Math.max(cleaned.lastIndexOf('/'), cleaned.lastIndexOf('\\'));
        return slashIndex >= 0 ? cleaned.substring(slashIndex + 1) : cleaned;
    }

    private void validateFileName(String fileName) {
        if (fileName.isBlank()) {
            throw new InvalidRequestException("File name must not be blank");
        }
        if (!FILE_NAME_PATTERN.matcher(fileName).matches()) {
            throw new InvalidRequestException("File name contains invalid characters");
        }
    }

    private void validateExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex <= 0 || dotIndex == fileName.length() - 1) {
            throw new InvalidRequestException("File extension not allowed (only jpg/jpeg/png/gif/webp)");
        }

        String extension = fileName.substring(dotIndex + 1).toLowerCase(Locale.ROOT);
        Set<String> allowedExtensions = normalizeSet(uploadProperties.getAllowedExtensions());
        if (!allowedExtensions.contains(extension)) {
            throw new InvalidRequestException("File extension not allowed (only jpg/jpeg/png/gif/webp)");
        }
    }

    private void validateFileSize(long size) {
        if (size > uploadProperties.getMaxSizeBytes()) {
            throw new InvalidRequestException("File size exceeds 5 MB");
        }
    }

    private Set<String> normalizeSet(List<String> values) {
        return values == null
                ? Set.of()
                : values.stream()
                        .filter(value -> value != null && !value.isBlank())
                        .map(value -> value.trim().toLowerCase(Locale.ROOT))
                        .collect(Collectors.toSet());
    }
}
