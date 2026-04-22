package vn.luuhien.springrestwithai.feature.permission;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import vn.luuhien.springrestwithai.feature.permission.dto.CreatePermissionRequest;
import vn.luuhien.springrestwithai.feature.permission.dto.PermissionResponse;
import vn.luuhien.springrestwithai.feature.permission.dto.UpdatePermissionRequest;

public interface PermissionService {

    PermissionResponse createPermission(CreatePermissionRequest request);

    PermissionResponse getPermissionById(Long id);

    Page<PermissionResponse> getAllPermissions(Pageable pageable);

    PermissionResponse updatePermission(UpdatePermissionRequest request);

    void deletePermission(Long id);
}
