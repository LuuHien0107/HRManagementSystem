package vn.luuhien.springrestwithai.feature.permission.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreatePermissionRequest(
        @NotBlank(message = "Tên quyền không được để trống") @Size(max = 100, message = "Tên quyền không được vượt quá 100 ký tự") String name,

        @NotBlank(message = "API path không được để trống") @Size(max = 255, message = "API path không được vượt quá 255 ký tự") String apiPath,

        @NotBlank(message = "HTTP method không được để trống") @Pattern(regexp = "GET|POST|PUT|PATCH|DELETE", message = "HTTP method không hợp lệ") String method,

        @NotBlank(message = "Module không được để trống") @Size(max = 100, message = "Module không được vượt quá 100 ký tự") String module) {
}
