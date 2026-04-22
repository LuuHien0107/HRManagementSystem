package vn.luuhien.springrestwithai.feature.role;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import vn.luuhien.springrestwithai.feature.role.dto.CreateRoleRequest;
import vn.luuhien.springrestwithai.feature.role.dto.RoleResponse;
import vn.luuhien.springrestwithai.feature.role.dto.UpdateRoleRequest;

public interface RoleService {

    RoleResponse createRole(CreateRoleRequest request);

    RoleResponse getRoleById(Long id);

    Page<RoleResponse> getAllRoles(Pageable pageable);

    RoleResponse updateRole(UpdateRoleRequest request);

    void deleteRole(Long id);
}
