package vn.luuhien.springrestwithai.feature.role.dto;

import vn.luuhien.springrestwithai.feature.permission.Permission;
import vn.luuhien.springrestwithai.feature.role.Role;

import java.time.Instant;
import java.util.List;

public record RoleResponse(
        Long id,
        String name,
        String description,
        List<PermissionSummary> permissions,
        Instant createdAt,
        Instant updatedAt) {

    public static RoleResponse fromEntity(Role role) {
        return new RoleResponse(
                role.getId(),
                role.getName(),
                role.getDescription(),
                role.getPermissions() == null
                        ? List.of()
                        : role.getPermissions().stream().map(PermissionSummary::fromEntity).toList(),
                role.getCreatedAt(),
                role.getUpdatedAt());
    }

    public record PermissionSummary(
            Long id,
            String name,
            String apiPath,
            String method,
            String module) {

        static PermissionSummary fromEntity(Permission permission) {
            return new PermissionSummary(
                    permission.getId(),
                    permission.getName(),
                    permission.getApiPath(),
                    permission.getMethod(),
                    permission.getModule());
        }
    }
}
