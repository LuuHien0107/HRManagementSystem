package vn.luuhien.springrestwithai.feature.permission;

import jakarta.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import vn.luuhien.springrestwithai.dto.ApiResponse;
import vn.luuhien.springrestwithai.feature.permission.dto.CreatePermissionRequest;
import vn.luuhien.springrestwithai.feature.permission.dto.PermissionResponse;
import vn.luuhien.springrestwithai.feature.permission.dto.UpdatePermissionRequest;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/permissions")
public class PermissionController {

    private final PermissionService permissionService;

    public PermissionController(PermissionService permissionService) {
        this.permissionService = permissionService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<PermissionResponse>>> getAllPermissions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id,asc") String sort) {

        String[] sortParts = sort.split(",");
        String sortField = sortParts[0];
        Sort.Direction direction = sortParts.length > 1 && "desc".equalsIgnoreCase(sortParts[1])
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortField));
        Page<PermissionResponse> permissions = permissionService.getAllPermissions(pageable);

        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách quyền thành công", permissions));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PermissionResponse>> getPermissionById(@PathVariable Long id) {
        PermissionResponse permission = permissionService.getPermissionById(id);
        return ResponseEntity.ok(ApiResponse.success("Lấy thông tin quyền thành công", permission));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<PermissionResponse>> createPermission(
            @Valid @RequestBody CreatePermissionRequest request) {

        PermissionResponse createdPermission = permissionService.createPermission(request);
        URI location = URI.create("/api/v1/permissions/" + createdPermission.id());

        return ResponseEntity.created(location)
                .body(ApiResponse.created("Tạo quyền mới thành công", createdPermission));
    }

    @PutMapping
    public ResponseEntity<ApiResponse<PermissionResponse>> updatePermission(
            @Valid @RequestBody UpdatePermissionRequest request) {

        PermissionResponse updatedPermission = permissionService.updatePermission(request);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật quyền thành công", updatedPermission));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deletePermission(@PathVariable Long id) {
        permissionService.deletePermission(id);
        return ResponseEntity.ok(ApiResponse.success("Xóa quyền thành công", null));
    }
}
