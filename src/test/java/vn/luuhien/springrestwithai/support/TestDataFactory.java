package vn.luuhien.springrestwithai.support;

import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.stereotype.Component;
import vn.luuhien.springrestwithai.feature.permission.Permission;
import vn.luuhien.springrestwithai.feature.permission.PermissionRepository;
import vn.luuhien.springrestwithai.feature.role.Role;
import vn.luuhien.springrestwithai.feature.role.RoleRepository;
import vn.luuhien.springrestwithai.feature.user.User;
import vn.luuhien.springrestwithai.feature.user.UserRepository;
import vn.luuhien.springrestwithai.security.PermissionAuthorizationManager;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Component
public class TestDataFactory {

    private final PermissionRepository permissionRepository;
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final JwtEncoder jwtEncoder;
    private final PermissionAuthorizationManager permissionAuthorizationManager;

    public TestDataFactory(
            PermissionRepository permissionRepository,
            RoleRepository roleRepository,
            UserRepository userRepository,
            JwtEncoder jwtEncoder,
            PermissionAuthorizationManager permissionAuthorizationManager) {
        this.permissionRepository = permissionRepository;
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.jwtEncoder = jwtEncoder;
        this.permissionAuthorizationManager = permissionAuthorizationManager;
    }

    public String createUserTokenWithPermissions(String email, List<EndpointPermission> endpointPermissions) {
        List<Permission> permissions = new ArrayList<>();
        for (EndpointPermission endpointPermission : endpointPermissions) {
            permissions.add(findOrCreatePermission(endpointPermission));
        }

        Role role = new Role();
        role.setName("TEST_ROLE_" + UUID.randomUUID().toString().replace("-", ""));
        role.setDescription("Test role with endpoint permissions");
        role.setPermissions(permissions);
        role = roleRepository.save(role);

        User user = new User();
        user.setEmail(normalizeEmail(email));
        user.setName("Test User");
        user.setPassword("encoded-password");
        user.setRoles(List.of(role));
        user = userRepository.save(user);

        permissionAuthorizationManager.loadCache();
        return generateJwt(user);
    }

    public String createUserTokenWithoutPermissions(String email) {
        Role emptyRole = new Role();
        emptyRole.setName("TEST_EMPTY_ROLE_" + UUID.randomUUID().toString().replace("-", ""));
        emptyRole.setDescription("Test role without permissions");
        emptyRole.setPermissions(new ArrayList<>());
        emptyRole = roleRepository.save(emptyRole);

        User user = new User();
        user.setEmail(normalizeEmail(email));
        user.setName("Forbidden User");
        user.setPassword("encoded-password");
        user.setRoles(List.of(emptyRole));
        user = userRepository.save(user);

        permissionAuthorizationManager.loadCache();
        return generateJwt(user);
    }

    private String generateJwt(User user) {
        Instant now = Instant.now();

        List<String> roles = user.getRoles().stream()
                .map(role -> "ROLE_" + role.getName())
                .toList();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .subject(user.getEmail())
                .issuedAt(now)
                .expiresAt(now.plusSeconds(3600))
                .claim("userId", user.getId())
                .claim("roles", roles)
                .build();

        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        return jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }

    private String normalizeEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
    }

    private String normalizeModule(String module) {
        if (module == null || module.isBlank()) {
            return "TEST";
        }
        return module.trim().toUpperCase(Locale.ROOT);
    }

    private Permission findOrCreatePermission(EndpointPermission endpointPermission) {
        String normalizedPath = endpointPermission.apiPath().trim();
        String normalizedMethod = endpointPermission.method().trim().toUpperCase(Locale.ROOT);
        return permissionRepository.findAll().stream()
                .filter(permission -> normalizedPath.equals(permission.getApiPath())
                        && normalizedMethod.equalsIgnoreCase(permission.getMethod()))
                .findFirst()
                .orElseGet(() -> {
                    Permission permission = new Permission();
                    permission.setName("TEST_" + normalizedMethod + "_" + UUID.randomUUID());
                    permission.setApiPath(normalizedPath);
                    permission.setMethod(normalizedMethod);
                    permission.setModule(normalizeModule(endpointPermission.module()));
                    return permissionRepository.save(permission);
                });
    }

    public record EndpointPermission(String apiPath, String method, String module) {
    }
}
