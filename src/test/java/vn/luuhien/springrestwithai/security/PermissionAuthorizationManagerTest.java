package vn.luuhien.springrestwithai.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import vn.luuhien.springrestwithai.feature.permission.Permission;
import vn.luuhien.springrestwithai.feature.role.Role;
import vn.luuhien.springrestwithai.feature.role.RoleRepository;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PermissionAuthorizationManagerTest {

    @Mock
    private RoleRepository roleRepository;

    private PermissionAuthorizationManager authorizationManager;

    @BeforeEach
    void setUp() {
        when(roleRepository.findAllWithPermissions()).thenReturn(List.of());
        authorizationManager = new PermissionAuthorizationManager(roleRepository);
    }

    @Test
    @DisplayName("Allow when JWT role is raw and cache key is ROLE_ prefixed")
    void authorize_allowWhenJwtRoleRaw_cacheRolePrefixed() {
        Permission permission = buildPermission("/api/v1/companies", "GET");
        Role role = buildRole("SUPER_ADMIN", List.of(permission));
        when(roleRepository.findAllWithPermissions()).thenReturn(List.of(role));
        authorizationManager.loadCache();

        AuthorizationDecision decision = authorizationManager.authorize(
                jwtAuthSupplier(List.of("SUPER_ADMIN")),
                requestContext("/api/v1/companies", "GET"));

        assertTrue(decision != null && decision.isGranted());
    }

    @Test
    @DisplayName("Allow when JWT role is ROLE_ prefixed")
    void authorize_allowWhenJwtRolePrefixed() {
        Permission permission = buildPermission("/api/v1/companies", "GET");
        Role role = buildRole("SUPER_ADMIN", List.of(permission));
        when(roleRepository.findAllWithPermissions()).thenReturn(List.of(role));
        authorizationManager.loadCache();

        AuthorizationDecision decision = authorizationManager.authorize(
                jwtAuthSupplier(List.of("ROLE_SUPER_ADMIN")),
                requestContext("/api/v1/companies", "GET"));

        assertTrue(decision != null && decision.isGranted());
    }

    @Test
    @DisplayName("Deny when method does not match")
    void authorize_denyWhenMethodNotMatch() {
        Permission permission = buildPermission("/api/v1/companies", "GET");
        Role role = buildRole("SUPER_ADMIN", List.of(permission));
        when(roleRepository.findAllWithPermissions()).thenReturn(List.of(role));
        authorizationManager.loadCache();

        AuthorizationDecision decision = authorizationManager.authorize(
                jwtAuthSupplier(List.of("SUPER_ADMIN")),
                requestContext("/api/v1/companies", "POST"));

        assertFalse(decision != null && decision.isGranted());
    }

    @Test
    @DisplayName("Deny when no matched role permission")
    void authorize_denyWhenNoPermission() {
        Permission permission = buildPermission("/api/v1/users", "GET");
        Role role = buildRole("HR", List.of(permission));
        when(roleRepository.findAllWithPermissions()).thenReturn(List.of(role));
        authorizationManager.loadCache();

        AuthorizationDecision decision = authorizationManager.authorize(
                jwtAuthSupplier(List.of("SUPER_ADMIN")),
                requestContext("/api/v1/companies", "GET"));

        assertFalse(decision != null && decision.isGranted());
    }

    @Test
    @DisplayName("Deny when authentication is null")
    void authorize_denyWhenAuthenticationNull() {
        Supplier<Authentication> authSupplier = () -> null;
        AuthorizationDecision decision = authorizationManager.authorize(
                authSupplier,
                requestContext("/api/v1/companies", "GET"));

        assertFalse(decision != null && decision.isGranted());
    }

    private Supplier<Authentication> jwtAuthSupplier(List<String> roles) {
        Jwt jwt = new Jwt(
                "dummy-token",
                Instant.now(),
                Instant.now().plusSeconds(3600),
                Map.of("alg", "HS256"),
                Map.of("roles", roles));

        JwtAuthenticationToken token = new JwtAuthenticationToken(jwt, AuthorityUtils.NO_AUTHORITIES);
        return () -> token;
    }

    private RequestAuthorizationContext requestContext(String requestUri, String method) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setMethod(method);
        request.setRequestURI(requestUri);
        return new RequestAuthorizationContext(request);
    }

    private Permission buildPermission(String apiPath, String method) {
        Permission permission = new Permission();
        permission.setApiPath(apiPath);
        permission.setMethod(method);
        permission.setName("TEST_" + method + "_" + apiPath);
        permission.setModule("TEST");
        return permission;
    }

    private Role buildRole(String name, List<Permission> permissions) {
        Role role = new Role();
        role.setName(name);
        role.setPermissions(permissions);
        return role;
    }
}
