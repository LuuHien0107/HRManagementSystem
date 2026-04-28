package vn.luuhien.springrestwithai.security;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;

import vn.luuhien.springrestwithai.feature.permission.Permission;
import vn.luuhien.springrestwithai.feature.role.Role;
import vn.luuhien.springrestwithai.feature.role.RoleRepository;

@Component
public class PermissionAuthorizationManager
        implements AuthorizationManager<RequestAuthorizationContext> {

    private static final Logger log = LoggerFactory.getLogger(PermissionAuthorizationManager.class);
    private static final String ROLE_PREFIX = "ROLE_";

    private final RoleRepository roleRepository;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    // Cache: roleName -> List<Permission>
    // Load 1 lan, invalidate khi admin thay doi
    private Map<String, List<Permission>> rolePermissionsCache = Collections.emptyMap();

    public PermissionAuthorizationManager(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
        loadCache();
    }

    public void loadCache() {
        // 1 query JOIN FETCH tat ca roles + permissions
        List<Role> roles = roleRepository.findAllWithPermissions();

        Map<String, List<Permission>> cache = new HashMap<>();
        for (Role role : roles) {
            cache.computeIfAbsent(ROLE_PREFIX + role.getName(), k -> new ArrayList<>())
                    .addAll(role.getPermissions());
        }
        this.rolePermissionsCache = cache;
    }

    @Override
    public AuthorizationDecision authorize(
            Supplier<? extends Authentication> authSupplier,
            RequestAuthorizationContext context) {

        Authentication authentication = authSupplier.get();
        if (authentication == null || !authentication.isAuthenticated()) {
            return new AuthorizationDecision(false);
        }

        String requestPath = context.getRequest().getRequestURI();
        String httpMethod = context.getRequest().getMethod();

        // Lay roles cua user tu JWT
        List<String> userRoles = getUserRolesFromJwt(authentication);
        log.debug("RBAC authorize requestPath='{}' method='{}' rolesFromJwt={}", requestPath, httpMethod, userRoles);

        Set<String> candidateRoleKeys = normalizeRoleKeys(userRoles);
        Map<String, List<Permission>> currentCache = rolePermissionsCache != null
                ? rolePermissionsCache
                : Collections.emptyMap();

        // V?i m?i role -> l?y permissions t? cache -> check match
        for (String roleKey : candidateRoleKeys) {
            List<Permission> permissions = currentCache.getOrDefault(roleKey, Collections.emptyList());
            for (Permission perm : permissions) {
                if (perm.getMethod().equalsIgnoreCase(httpMethod)
                        && pathMatcher.match(perm.getApiPath(), requestPath)) {
                    log.debug(
                            "RBAC allow requestPath='{}' method='{}' by roleKey='{}' permission='{} {}'",
                            requestPath,
                            httpMethod,
                            roleKey,
                            perm.getMethod(),
                            perm.getApiPath());
                    return new AuthorizationDecision(true);
                }
            }
        }

        return new AuthorizationDecision(false);
    }

    @SuppressWarnings("unchecked")
    private List<String> getUserRolesFromJwt(Authentication authentication) {
        if (authentication instanceof JwtAuthenticationToken jwtToken) {
            Object rolesClaim = jwtToken.getToken().getClaim("roles");
            if (rolesClaim instanceof List<?> roles) {
                return (List<String>) roles;
            }
        }
        return Collections.emptyList();
    }

    private Set<String> normalizeRoleKeys(List<String> rolesFromJwt) {
        if (rolesFromJwt == null || rolesFromJwt.isEmpty()) {
            return Collections.emptySet();
        }

        Set<String> keys = new LinkedHashSet<>();
        for (String role : rolesFromJwt) {
            if (role == null || role.isBlank()) {
                continue;
            }
            String trimmed = role.trim();
            keys.add(trimmed);
            if (trimmed.startsWith(ROLE_PREFIX)) {
                keys.add(trimmed.substring(ROLE_PREFIX.length()));
            } else {
                keys.add(ROLE_PREFIX + trimmed);
            }
        }
        return keys;
    }
}
