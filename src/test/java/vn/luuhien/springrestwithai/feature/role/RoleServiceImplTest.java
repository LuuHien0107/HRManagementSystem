package vn.luuhien.springrestwithai.feature.role;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import vn.luuhien.springrestwithai.exception.DuplicateResourceException;
import vn.luuhien.springrestwithai.exception.ResourceNotFoundException;
import vn.luuhien.springrestwithai.feature.permission.Permission;
import vn.luuhien.springrestwithai.feature.permission.PermissionRepository;
import vn.luuhien.springrestwithai.feature.role.dto.CreateRoleRequest;
import vn.luuhien.springrestwithai.feature.role.dto.RoleResponse;
import vn.luuhien.springrestwithai.feature.role.dto.UpdateRoleRequest;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RoleServiceImplTest {

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PermissionRepository permissionRepository;

    @InjectMocks
    private RoleServiceImpl roleService;

    @Test
    @DisplayName("Create role success")
    void createRole_validRequest_returnCreatedRole() {
        Permission p1 = buildPermission(1L, "CREATE_USER", "/users", "POST", "USER");
        Permission p2 = buildPermission(2L, "VIEW_USERS", "/users", "GET", "USER");
        CreateRoleRequest request = new CreateRoleRequest("HR", "Human resources", List.of(1L, 2L));

        Role savedRole = buildRole(1L, "HR", "Human resources", List.of(p1, p2));

        when(roleRepository.existsByNameIgnoreCase("HR")).thenReturn(false);
        when(permissionRepository.findAllById(List.of(1L, 2L))).thenReturn(List.of(p1, p2));
        when(roleRepository.save(any(Role.class))).thenReturn(savedRole);

        RoleResponse response = roleService.createRole(request);

        assertEquals(1L, response.id());
        assertEquals("HR", response.name());
        assertEquals(2, response.permissions().size());
        verify(roleRepository).save(any(Role.class));
    }

    @Test
    @DisplayName("Create role duplicate name")
    void createRole_duplicateName_throwDuplicateResourceException() {
        CreateRoleRequest request = new CreateRoleRequest("ADMIN", "System admin", List.of());

        when(roleRepository.existsByNameIgnoreCase("ADMIN")).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> roleService.createRole(request));
        verify(roleRepository, never()).save(any(Role.class));
    }

    @Test
    @DisplayName("Create role permission not found")
    void createRole_permissionNotFound_throwResourceNotFoundException() {
        Permission p1 = buildPermission(1L, "CREATE_USER", "/users", "POST", "USER");
        CreateRoleRequest request = new CreateRoleRequest("HR", "Human resources", List.of(1L, 99L));

        when(roleRepository.existsByNameIgnoreCase("HR")).thenReturn(false);
        when(permissionRepository.findAllById(List.of(1L, 99L))).thenReturn(List.of(p1));

        assertThrows(ResourceNotFoundException.class, () -> roleService.createRole(request));
        verify(roleRepository, never()).save(any(Role.class));
    }

    @Test
    @DisplayName("Get role by id success")
    void getRoleById_found_returnRole() {
        Permission permission = buildPermission(1L, "VIEW_USERS", "/users", "GET", "USER");
        Role role = buildRole(1L, "ADMIN", "System admin", List.of(permission));

        when(roleRepository.findById(1L)).thenReturn(Optional.of(role));

        RoleResponse response = roleService.getRoleById(1L);

        assertEquals(1L, response.id());
        assertEquals("ADMIN", response.name());
        assertEquals(1, response.permissions().size());
    }

    @Test
    @DisplayName("Get role by id not found")
    void getRoleById_notFound_throwResourceNotFoundException() {
        when(roleRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> roleService.getRoleById(999L));
    }

    @Test
    @DisplayName("Get all roles returns list")
    void getAllRoles_hasData_returnNonEmptyPage() {
        Role role = buildRole(1L, "ADMIN", "System admin", List.of());
        Page<Role> page = new PageImpl<>(List.of(role));

        when(roleRepository.findAll(PageRequest.of(0, 10))).thenReturn(page);

        Page<RoleResponse> responsePage = roleService.getAllRoles(PageRequest.of(0, 10));

        assertFalse(responsePage.isEmpty());
        assertEquals(1, responsePage.getTotalElements());
    }

    @Test
    @DisplayName("Get all roles returns empty list")
    void getAllRoles_empty_returnEmptyPage() {
        Page<Role> page = new PageImpl<>(List.of());
        when(roleRepository.findAll(PageRequest.of(0, 10))).thenReturn(page);

        Page<RoleResponse> responsePage = roleService.getAllRoles(PageRequest.of(0, 10));

        assertTrue(responsePage.isEmpty());
    }

    @Test
    @DisplayName("Update role success")
    void updateRole_validRequest_returnUpdatedRole() {
        Permission p1 = buildPermission(1L, "CREATE_USER", "/users", "POST", "USER");
        Permission p2 = buildPermission(2L, "VIEW_USERS", "/users", "GET", "USER");
        Role existing = buildRole(2L, "HR", "Old", List.of(p1));
        UpdateRoleRequest request = new UpdateRoleRequest(2L, "MANAGER", "Updated", List.of(1L, 2L));

        when(roleRepository.findById(2L)).thenReturn(Optional.of(existing));
        when(roleRepository.existsByNameIgnoreCaseAndIdNot("MANAGER", 2L)).thenReturn(false);
        when(permissionRepository.findAllById(List.of(1L, 2L))).thenReturn(List.of(p1, p2));
        when(roleRepository.save(any(Role.class))).thenAnswer(invocation -> invocation.getArgument(0));

        RoleResponse response = roleService.updateRole(request);

        assertEquals("MANAGER", response.name());
        assertEquals(2, response.permissions().size());
    }

    @Test
    @DisplayName("Update role not found")
    void updateRole_notFound_throwResourceNotFoundException() {
        UpdateRoleRequest request = new UpdateRoleRequest(100L, "HR", null, List.of());
        when(roleRepository.findById(100L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> roleService.updateRole(request));
    }

    @Test
    @DisplayName("Update role duplicate name")
    void updateRole_duplicateName_throwDuplicateResourceException() {
        Role existing = buildRole(2L, "HR", "Old", List.of());
        UpdateRoleRequest request = new UpdateRoleRequest(2L, "ADMIN", "Updated", List.of());

        when(roleRepository.findById(2L)).thenReturn(Optional.of(existing));
        when(roleRepository.existsByNameIgnoreCaseAndIdNot("ADMIN", 2L)).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> roleService.updateRole(request));
    }

    @Test
    @DisplayName("Update role permission not found")
    void updateRole_permissionNotFound_throwResourceNotFoundException() {
        Permission p1 = buildPermission(1L, "CREATE_USER", "/users", "POST", "USER");
        Role existing = buildRole(2L, "HR", "Old", List.of(p1));
        UpdateRoleRequest request = new UpdateRoleRequest(2L, "HR", "Updated", List.of(1L, 999L));

        when(roleRepository.findById(2L)).thenReturn(Optional.of(existing));
        when(roleRepository.existsByNameIgnoreCaseAndIdNot("HR", 2L)).thenReturn(false);
        when(permissionRepository.findAllById(List.of(1L, 999L))).thenReturn(List.of(p1));

        assertThrows(ResourceNotFoundException.class, () -> roleService.updateRole(request));
        verify(roleRepository, never()).save(any(Role.class));
    }

    @Test
    @DisplayName("Delete role success")
    void deleteRole_found_deleteSuccessfully() {
        Role existing = buildRole(1L, "ADMIN", "System admin", List.of());
        when(roleRepository.findById(1L)).thenReturn(Optional.of(existing));

        roleService.deleteRole(1L);

        verify(roleRepository).delete(existing);
    }

    @Test
    @DisplayName("Delete role not found")
    void deleteRole_notFound_throwResourceNotFoundException() {
        when(roleRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> roleService.deleteRole(999L));
    }

    private Role buildRole(Long id, String name, String description, List<Permission> permissions) {
        Role role = new Role();
        role.setId(id);
        role.setName(name);
        role.setDescription(description);
        role.setPermissions(permissions);
        return role;
    }

    private Permission buildPermission(Long id, String name, String apiPath, String method, String module) {
        Permission permission = new Permission();
        permission.setId(id);
        permission.setName(name);
        permission.setApiPath(apiPath);
        permission.setMethod(method);
        permission.setModule(module);
        return permission;
    }
}
