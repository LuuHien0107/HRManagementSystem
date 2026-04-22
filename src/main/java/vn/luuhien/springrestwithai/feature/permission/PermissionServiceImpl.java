package vn.luuhien.springrestwithai.feature.permission;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import vn.luuhien.springrestwithai.exception.DuplicateResourceException;
import vn.luuhien.springrestwithai.exception.InvalidRequestException;
import vn.luuhien.springrestwithai.exception.ResourceNotFoundException;
import vn.luuhien.springrestwithai.feature.permission.dto.CreatePermissionRequest;
import vn.luuhien.springrestwithai.feature.permission.dto.PermissionResponse;
import vn.luuhien.springrestwithai.feature.permission.dto.UpdatePermissionRequest;

import java.util.Set;

@Service
@Transactional(readOnly = true)
public class PermissionServiceImpl implements PermissionService {

    private static final Set<String> ALLOWED_METHODS = Set.of("GET", "POST", "PUT", "PATCH", "DELETE");

    private final PermissionRepository permissionRepository;

    public PermissionServiceImpl(PermissionRepository permissionRepository) {
        this.permissionRepository = permissionRepository;
    }

    @Override
    @Transactional
    public PermissionResponse createPermission(CreatePermissionRequest request) {
        String normalizedMethod = normalizeMethod(request.method());
        String normalizedModule = normalizeModule(request.module());

        if (permissionRepository.existsByApiPathAndMethod(request.apiPath(), normalizedMethod)) {
            throw new DuplicateResourceException("Permission", "apiPath+method",
                    request.apiPath() + "|" + normalizedMethod);
        }

        Permission permission = new Permission();
        permission.setName(request.name().trim());
        permission.setApiPath(request.apiPath().trim());
        permission.setMethod(normalizedMethod);
        permission.setModule(normalizedModule);

        return PermissionResponse.fromEntity(permissionRepository.save(permission));
    }

    @Override
    public PermissionResponse getPermissionById(Long id) {
        return PermissionResponse.fromEntity(findPermissionById(id));
    }

    @Override
    public Page<PermissionResponse> getAllPermissions(Pageable pageable) {
        return permissionRepository.findAll(pageable).map(PermissionResponse::fromEntity);
    }

    @Override
    @Transactional
    public PermissionResponse updatePermission(UpdatePermissionRequest request) {
        Permission existingPermission = findPermissionById(request.id());

        String normalizedMethod = normalizeMethod(request.method());
        String normalizedModule = normalizeModule(request.module());

        if (permissionRepository.existsByApiPathAndMethodAndIdNot(request.apiPath(), normalizedMethod, request.id())) {
            throw new DuplicateResourceException("Permission", "apiPath+method",
                    request.apiPath() + "|" + normalizedMethod);
        }

        existingPermission.setName(request.name().trim());
        existingPermission.setApiPath(request.apiPath().trim());
        existingPermission.setMethod(normalizedMethod);
        existingPermission.setModule(normalizedModule);

        return PermissionResponse.fromEntity(permissionRepository.save(existingPermission));
    }

    @Override
    @Transactional
    public void deletePermission(Long id) {
        Permission existingPermission = findPermissionById(id);
        permissionRepository.delete(existingPermission);
    }

    private Permission findPermissionById(Long id) {
        return permissionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Permission", "id", id));
    }

    private String normalizeMethod(String method) {
        String normalized = method == null ? "" : method.trim().toUpperCase();
        if (!ALLOWED_METHODS.contains(normalized)) {
            throw new InvalidRequestException("HTTP method không hợp lệ");
        }
        return normalized;
    }

    private String normalizeModule(String module) {
        return module == null ? "" : module.trim().toUpperCase();
    }
}
