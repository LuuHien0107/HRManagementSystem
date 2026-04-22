package vn.luuhien.springrestwithai.feature.role;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import vn.luuhien.springrestwithai.exception.DuplicateResourceException;
import vn.luuhien.springrestwithai.exception.ResourceNotFoundException;
import vn.luuhien.springrestwithai.feature.permission.Permission;
import vn.luuhien.springrestwithai.feature.permission.PermissionRepository;
import vn.luuhien.springrestwithai.feature.role.dto.CreateRoleRequest;
import vn.luuhien.springrestwithai.feature.role.dto.RoleResponse;
import vn.luuhien.springrestwithai.feature.role.dto.UpdateRoleRequest;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@Transactional(readOnly = true)
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;

    public RoleServiceImpl(RoleRepository roleRepository, PermissionRepository permissionRepository) {
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
    }

    @Override
    @Transactional
    public RoleResponse createRole(CreateRoleRequest request) {
        String normalizedName = normalizeName(request.name());
        if (roleRepository.existsByNameIgnoreCase(normalizedName)) {
            throw new DuplicateResourceException("Role", "name", normalizedName);
        }

        Role role = new Role();
        applyRequestData(role, normalizedName, request.description(), request.permissionIds());
        return RoleResponse.fromEntity(roleRepository.save(role));
    }

    @Override
    public RoleResponse getRoleById(Long id) {
        return RoleResponse.fromEntity(findRoleById(id));
    }

    @Override
    public Page<RoleResponse> getAllRoles(Pageable pageable) {
        return roleRepository.findAll(pageable).map(RoleResponse::fromEntity);
    }

    @Override
    @Transactional
    public RoleResponse updateRole(UpdateRoleRequest request) {
        Role existingRole = findRoleById(request.id());
        String normalizedName = normalizeName(request.name());

        if (roleRepository.existsByNameIgnoreCaseAndIdNot(normalizedName, request.id())) {
            throw new DuplicateResourceException("Role", "name", normalizedName);
        }

        applyRequestData(existingRole, normalizedName, request.description(), request.permissionIds());
        return RoleResponse.fromEntity(roleRepository.save(existingRole));
    }

    @Override
    @Transactional
    public void deleteRole(Long id) {
        Role existingRole = findRoleById(id);
        roleRepository.delete(existingRole);
    }

    private Role findRoleById(Long id) {
        return roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Role", "id", id));
    }

    private String normalizeName(String name) {
        return name == null ? "" : name.trim();
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }

    private void applyRequestData(Role role, String name, String description, List<Long> permissionIds) {
        role.setName(name);
        role.setDescription(trimToNull(description));
        role.setPermissions(resolvePermissions(permissionIds));
    }

    private List<Permission> resolvePermissions(List<Long> permissionIds) {
        if (permissionIds == null || permissionIds.isEmpty()) {
            return new ArrayList<>();
        }

        List<Long> uniqueIds = permissionIds.stream().distinct().toList();
        List<Permission> permissions = permissionRepository.findAllById(uniqueIds);

        if (permissions.size() != uniqueIds.size()) {
            Set<Long> foundIds = new HashSet<>();
            for (Permission permission : permissions) {
                foundIds.add(permission.getId());
            }
            List<Long> missingIds = uniqueIds.stream().filter(id -> !foundIds.contains(id)).toList();
            throw new ResourceNotFoundException("Permission", "ids", missingIds);
        }

        return new ArrayList<>(permissions);
    }
}
