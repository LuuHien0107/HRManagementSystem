package vn.luuhien.springrestwithai.feature.auth;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import vn.luuhien.springrestwithai.exception.ResourceNotFoundException;
import vn.luuhien.springrestwithai.exception.UnauthorizedException;
import vn.luuhien.springrestwithai.feature.auth.dto.LoginRequest;
import vn.luuhien.springrestwithai.feature.auth.dto.LoginResponse;
import vn.luuhien.springrestwithai.feature.auth.dto.RefreshTokenRequest;
import vn.luuhien.springrestwithai.feature.auth.dto.RegisterRequest;
import vn.luuhien.springrestwithai.exception.DuplicateResourceException;
import vn.luuhien.springrestwithai.feature.role.Role;
import vn.luuhien.springrestwithai.feature.role.RoleRepository;
import vn.luuhien.springrestwithai.feature.user.User;
import vn.luuhien.springrestwithai.feature.user.UserRepository;
import vn.luuhien.springrestwithai.feature.user.dto.UserResponse;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtEncoder jwtEncoder;
    private final JwtDecoder jwtDecoder;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${jwt.expiration}")
    private long accessTokenExpirationMs;

    @Value("${jwt.refresh-expiration:259200000}")
    private long refreshTokenExpirationMs;

    public AuthServiceImpl(
            AuthenticationManager authenticationManager,
            JwtEncoder jwtEncoder,
            JwtDecoder jwtDecoder,
            UserRepository userRepository,
            RoleRepository roleRepository,
            PasswordEncoder passwordEncoder,
            RefreshTokenRepository refreshTokenRepository) {
        this.authenticationManager = authenticationManager;
        this.jwtEncoder = jwtEncoder;
        this.jwtDecoder = jwtDecoder;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.refreshTokenRepository = refreshTokenRepository;
    }

    @Override
    @Transactional
    public LoginResponse login(LoginRequest request, HttpServletRequest httpRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        User user = userRepository.findOptionalByEmail(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", authentication.getName()));
        String accessToken = generateAccessToken(authentication, user.getId());
        String refreshToken = generateRefreshToken(user);
        persistRefreshToken(user, refreshToken, extractDeviceInfo(httpRequest), extractIpAddress(httpRequest));

        return new LoginResponse(accessToken, refreshToken);
    }

    @Override
    @Transactional
    public UserResponse register(RegisterRequest request) {
        String normalizedEmail = normalizeEmail(request.email());
        if (userRepository.existsByEmailIgnoreCase(normalizedEmail)) {
            throw new DuplicateResourceException("User", "email", normalizedEmail);
        }

        Role defaultRole = roleRepository.findFirstByNameIgnoreCase("USER")
                .orElseThrow(() -> new ResourceNotFoundException("Role", "name", "USER"));

        User user = new User();
        user.setName(normalizeName(request.name()));
        user.setEmail(normalizedEmail);
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setAge(request.age());
        user.setAddress(trimToNull(request.address()));
        user.setGender(request.gender());
        user.setRoles(new ArrayList<>(List.of(defaultRole)));

        return UserResponse.fromEntity(userRepository.save(user));
    }

    @Override
    @Transactional
    public LoginResponse refresh(
            RefreshTokenRequest request,
            String refreshTokenFromCookie,
            HttpServletRequest httpRequest) {
        String refreshTokenValue = resolveRefreshToken(refreshTokenFromCookie, request);
        RefreshToken refreshToken = refreshTokenRepository.findByToken(refreshTokenValue)
                .orElseThrow(() -> new UnauthorizedException("Refresh token is invalid"));

        validateRefreshToken(refreshToken);
        validateRefreshJwt(refreshTokenValue);

        refreshToken.setRevoked(true);
        User user = refreshToken.getUser();

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                user.getEmail(),
                null,
                user.getRoles().stream()
                        .map(role -> (GrantedAuthority) role::getName)
                        .toList()
        );

        String newAccessToken = generateAccessToken(authentication, user.getId());
        String newRefreshToken = generateRefreshToken(user);
        persistRefreshToken(user, newRefreshToken, extractDeviceInfo(httpRequest), extractIpAddress(httpRequest));
        return new LoginResponse(newAccessToken, newRefreshToken);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getCurrentUser(String email) {
        User user = userRepository.findOptionalByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
        return UserResponse.fromEntity(user);
    }

    @Override
    @Transactional
    public void logout(String userEmail) {
        User user = userRepository.findOptionalByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", userEmail));
        List<RefreshToken> activeTokens = refreshTokenRepository.findByUserIdAndRevokedFalse(user.getId());
        for (RefreshToken token : activeTokens) {
            token.setRevoked(true);
        }
    }

    private String generateAccessToken(Authentication authentication, Long userId) {
        Instant now = Instant.now();

        List<String> roles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .subject(authentication.getName())
                .issuedAt(now)
                .expiresAt(now.plusMillis(accessTokenExpirationMs))
                .claim("userId", userId)
                .claim("roles", roles)
                .build();

        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        return jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }

    private String generateRefreshToken(User user) {
        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .id(UUID.randomUUID().toString())
                .subject(user.getEmail())
                .issuedAt(now)
                .expiresAt(now.plusMillis(refreshTokenExpirationMs))
                .claim("type", "refresh")
                .claim("userId", user.getId())
                .build();
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        return jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }

    private void persistRefreshToken(User user, String refreshTokenValue, String deviceInfo, String ipAddress) {
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setToken(refreshTokenValue);
        refreshToken.setExpiresAt(Instant.now().plusMillis(refreshTokenExpirationMs));
        refreshToken.setRevoked(false);
        refreshToken.setDeviceInfo(deviceInfo);
        refreshToken.setIpAddress(ipAddress);
        refreshTokenRepository.save(refreshToken);
    }

    private String resolveRefreshToken(String refreshTokenFromCookie, RefreshTokenRequest request) {
        if (refreshTokenFromCookie != null && !refreshTokenFromCookie.isBlank()) {
            return refreshTokenFromCookie;
        }
        if (request != null && request.getRefreshToken() != null && !request.getRefreshToken().isBlank()) {
            return request.getRefreshToken();
        }
        throw new UnauthorizedException("No refresh token provided");
    }

    private void validateRefreshToken(RefreshToken refreshToken) {
        if (refreshToken.isRevoked()) {
            throw new UnauthorizedException("Refresh token has been revoked");
        }
        if (refreshToken.getExpiresAt().isBefore(Instant.now())) {
            throw new UnauthorizedException("Refresh token has expired");
        }
    }

    private void validateRefreshJwt(String tokenValue) {
        try {
            if (!"refresh".equals(jwtDecoder.decode(tokenValue).getClaimAsString("type"))) {
                throw new UnauthorizedException("Token is not a refresh token");
            }
        } catch (JwtException ex) {
            throw new UnauthorizedException("Refresh token is invalid");
        }
    }

    private String extractDeviceInfo(HttpServletRequest request) {
        return Optional.ofNullable(request)
                .map(req -> req.getHeader("User-Agent"))
                .orElse(null);
    }

    private String extractIpAddress(HttpServletRequest request) {
        return Optional.ofNullable(request)
                .map(HttpServletRequest::getRemoteAddr)
                .orElse(null);
    }

    private String normalizeName(String value) {
        return value == null ? "" : value.trim();
    }

    private String normalizeEmail(String value) {
        return value == null ? "" : value.trim().toLowerCase();
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }
}
