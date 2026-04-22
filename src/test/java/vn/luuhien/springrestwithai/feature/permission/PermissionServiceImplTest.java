package vn.luuhien.springrestwithai.feature.permission;

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
import vn.luuhien.springrestwithai.exception.InvalidRequestException;
import vn.luuhien.springrestwithai.exception.ResourceNotFoundException;
import vn.luuhien.springrestwithai.feature.permission.dto.CreatePermissionRequest;
import vn.luuhien.springrestwithai.feature.permission.dto.PermissionResponse;
import vn.luuhien.springrestwithai.feature.permission.dto.UpdatePermissionRequest;

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
class PermissionServiceImplTest {

    @Mock
    private PermissionRepository permissionRepository;

    @InjectMocks
    private PermissionServiceImpl permissionService;

    @Test
    @DisplayName("Create permission success")
    void createPermission_validRequest_returnCreatedPermission() {
        CreatePermissionRequest request = new CreatePermissionRequest("CREATE_USER", "/users", "POST", "USER");

        Permission savedPermission = buildPermission(1L, "CREATE_USER", "/users", "POST", "USER");

        when(permissionRepository.existsByApiPathAndMethod("/users", "POST")).thenReturn(false);
        when(permissionRepository.save(any(Permission.class))).thenReturn(savedPermission);

        PermissionResponse response = permissionService.createPermission(request);

        assertEquals(1L, response.id());
        assertEquals("POST", response.method());
        verify(permissionRepository).save(any(Permission.class));
    }

    @Test
    @DisplayName("Create permission duplicate apiPath and method")
    void createPermission_duplicateApiPathAndMethod_throwDuplicateResourceException() {
        CreatePermissionRequest request = new CreatePermissionRequest("CREATE_USER", "/users", "POST", "USER");

        when(permissionRepository.existsByApiPathAndMethod("/users", "POST")).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> permissionService.createPermission(request));
        verify(permissionRepository, never()).save(any(Permission.class));
    }

    @Test
    @DisplayName("Create permission invalid method")
    void createPermission_invalidMethod_throwInvalidRequestException() {
        CreatePermissionRequest request = new CreatePermissionRequest("CREATE_USER", "/users", "TRACE", "USER");

        assertThrows(InvalidRequestException.class, () -> permissionService.createPermission(request));
        verify(permissionRepository, never()).save(any(Permission.class));
    }

    @Test
    @DisplayName("Get permission by id success")
    void getPermissionById_found_returnPermission() {
        Permission permission = buildPermission(1L, "VIEW_USER", "/users", "GET", "USER");
        when(permissionRepository.findById(1L)).thenReturn(Optional.of(permission));

        PermissionResponse response = permissionService.getPermissionById(1L);

        assertEquals(1L, response.id());
        assertEquals("VIEW_USER", response.name());
    }

    @Test
    @DisplayName("Get permission by id not found")
    void getPermissionById_notFound_throwResourceNotFoundException() {
        when(permissionRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> permissionService.getPermissionById(99L));
    }

    @Test
    @DisplayName("Get all permissions returns list")
    void getAllPermissions_hasData_returnNonEmptyPage() {
        Permission permission = buildPermission(1L, "VIEW_USER", "/users", "GET", "USER");
        Page<Permission> page = new PageImpl<>(List.of(permission));

        when(permissionRepository.findAll(PageRequest.of(0, 10))).thenReturn(page);

        Page<PermissionResponse> responsePage = permissionService.getAllPermissions(PageRequest.of(0, 10));

        assertFalse(responsePage.isEmpty());
        assertEquals(1, responsePage.getTotalElements());
    }

    @Test
    @DisplayName("Get all permissions returns empty list")
    void getAllPermissions_empty_returnEmptyPage() {
        Page<Permission> page = new PageImpl<>(List.of());
        when(permissionRepository.findAll(PageRequest.of(0, 10))).thenReturn(page);

        Page<PermissionResponse> responsePage = permissionService.getAllPermissions(PageRequest.of(0, 10));

        assertTrue(responsePage.isEmpty());
    }

    @Test
    @DisplayName("Update permission success")
    void updatePermission_validRequest_returnUpdatedPermission() {
        Permission existing = buildPermission(1L, "CREATE_USER", "/users", "POST", "USER");
        UpdatePermissionRequest request = new UpdatePermissionRequest(1L, "UPDATE_USER", "/users", "PUT", "USER");

        when(permissionRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(permissionRepository.existsByApiPathAndMethodAndIdNot("/users", "PUT", 1L)).thenReturn(false);
        when(permissionRepository.save(any(Permission.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PermissionResponse response = permissionService.updatePermission(request);

        assertEquals("UPDATE_USER", response.name());
        assertEquals("PUT", response.method());
    }

    @Test
    @DisplayName("Update permission not found")
    void updatePermission_notFound_throwResourceNotFoundException() {
        UpdatePermissionRequest request = new UpdatePermissionRequest(100L, "UPDATE_USER", "/users", "PUT", "USER");
        when(permissionRepository.findById(100L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> permissionService.updatePermission(request));
    }

    @Test
    @DisplayName("Update permission duplicate apiPath and method")
    void updatePermission_duplicateApiPathAndMethod_throwDuplicateResourceException() {
        Permission existing = buildPermission(1L, "CREATE_USER", "/users", "POST", "USER");
        UpdatePermissionRequest request = new UpdatePermissionRequest(1L, "UPDATE_USER", "/users", "PUT", "USER");

        when(permissionRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(permissionRepository.existsByApiPathAndMethodAndIdNot("/users", "PUT", 1L)).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> permissionService.updatePermission(request));
    }

    @Test
    @DisplayName("Delete permission success")
    void deletePermission_found_deleteSuccessfully() {
        Permission existing = buildPermission(1L, "CREATE_USER", "/users", "POST", "USER");
        when(permissionRepository.findById(1L)).thenReturn(Optional.of(existing));

        permissionService.deletePermission(1L);

        verify(permissionRepository).delete(existing);
    }

    @Test
    @DisplayName("Delete permission not found")
    void deletePermission_notFound_throwResourceNotFoundException() {
        when(permissionRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> permissionService.deletePermission(999L));
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
