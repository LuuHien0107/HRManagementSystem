package vn.luuhien.springrestwithai.feature.auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import vn.luuhien.springrestwithai.dto.ApiResponse;
import vn.luuhien.springrestwithai.exception.UnauthorizedException;
import vn.luuhien.springrestwithai.feature.auth.dto.LoginRequest;
import vn.luuhien.springrestwithai.feature.auth.dto.LoginResponse;
import vn.luuhien.springrestwithai.feature.auth.dto.RefreshTokenRequest;
import vn.luuhien.springrestwithai.feature.auth.dto.RegisterRequest;
import vn.luuhien.springrestwithai.feature.user.dto.UserResponse;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private static final long REFRESH_TOKEN_COOKIE_MAX_AGE_SECONDS = 259200;
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse response) {
        LoginResponse loginResponse = authService.login(request, httpRequest);
        response.addHeader(HttpHeaders.SET_COOKIE, buildRefreshTokenCookie(loginResponse.getRefreshToken()).toString());
        return ResponseEntity.ok(ApiResponse.success(loginResponse));
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserResponse>> register(@Valid @RequestBody RegisterRequest request) {
        UserResponse createdUser = authService.register(request);
        URI location = URI.create("/api/v1/users/" + createdUser.id());
        return ResponseEntity.created(location).body(ApiResponse.created("User registered", createdUser));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<LoginResponse>> refresh(
            @RequestBody(required = false) RefreshTokenRequest request,
            @CookieValue(value = "refresh_token", required = false) String refreshTokenCookie,
            HttpServletRequest httpRequest,
            HttpServletResponse response) {
        LoginResponse loginResponse = authService.refresh(request, refreshTokenCookie, httpRequest);
        response.addHeader(HttpHeaders.SET_COOKIE, buildRefreshTokenCookie(loginResponse.getRefreshToken()).toString());
        return ResponseEntity.ok(ApiResponse.success("Token refreshed", loginResponse));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(Authentication authentication, HttpServletResponse response) {
        if (authentication == null) {
            throw new UnauthorizedException("Unauthorized");
        }
        authService.logout(authentication.getName());
        response.addHeader(HttpHeaders.SET_COOKIE, clearRefreshTokenCookie().toString());
        return ResponseEntity.ok(ApiResponse.success("Logout successful", null));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> me(Authentication authentication) {
        if (authentication == null) {
            throw new UnauthorizedException("Unauthorized");
        }
        UserResponse currentUser = authService.getCurrentUser(authentication.getName());
        return ResponseEntity.ok(ApiResponse.success(currentUser));
    }

    private ResponseCookie buildRefreshTokenCookie(String refreshToken) {
        return ResponseCookie.from("refresh_token", refreshToken)
                .httpOnly(true)
                .secure(true)
                .sameSite("Lax")
                .path("/api/v1/auth")
                .maxAge(REFRESH_TOKEN_COOKIE_MAX_AGE_SECONDS)
                .build();
    }

    private ResponseCookie clearRefreshTokenCookie() {
        return ResponseCookie.from("refresh_token", "")
                .httpOnly(true)
                .secure(true)
                .sameSite("Lax")
                .path("/api/v1/auth")
                .maxAge(0)
                .build();
    }
}
