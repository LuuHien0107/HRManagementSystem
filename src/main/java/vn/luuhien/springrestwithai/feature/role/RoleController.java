package vn.luuhien.springrestwithai.feature.role;

import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.luuhien.springrestwithai.dto.ApiResponse;
import vn.luuhien.springrestwithai.dto.PagedResult;
import vn.luuhien.springrestwithai.feature.role.dto.CreateRoleRequest;
import vn.luuhien.springrestwithai.feature.role.dto.RoleResponse;
import vn.luuhien.springrestwithai.feature.role.dto.UpdateRoleRequest;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/roles")
public class RoleController {

    private final RoleService roleService;

    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PagedResult<RoleResponse>>> getAllRoles(
            @PageableDefault(
                    page = 0,
                    size = 10,
                    sort = "createdAt",
                    direction = Sort.Direction.DESC) Pageable pageable) {
        Page<RoleResponse> roles = roleService.getAllRoles(pageable);

        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách vai trò thành công", PagedResult.from(roles)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<RoleResponse>> getRoleById(@PathVariable Long id) {
        RoleResponse role = roleService.getRoleById(id);
        return ResponseEntity.ok(ApiResponse.success("Lấy thông tin vai trò thành công", role));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<RoleResponse>> createRole(@Valid @RequestBody CreateRoleRequest request) {
        RoleResponse createdRole = roleService.createRole(request);
        URI location = URI.create("/api/v1/roles/" + createdRole.id());

        return ResponseEntity.created(location)
                .body(ApiResponse.created("Tạo vai trò mới thành công", createdRole));
    }

    @PutMapping
    public ResponseEntity<ApiResponse<RoleResponse>> updateRole(@Valid @RequestBody UpdateRoleRequest request) {
        RoleResponse updatedRole = roleService.updateRole(request);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật vai trò thành công", updatedRole));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteRole(@PathVariable Long id) {
        roleService.deleteRole(id);
        return ResponseEntity.ok(ApiResponse.success("Xóa vai trò thành công", null));
    }
}
