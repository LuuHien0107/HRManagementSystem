package vn.luuhien.springrestwithai.feature.auth;

import vn.luuhien.springrestwithai.feature.auth.dto.LoginRequest;
import vn.luuhien.springrestwithai.feature.auth.dto.LoginResponse;
import vn.luuhien.springrestwithai.feature.auth.dto.RefreshTokenRequest;
import vn.luuhien.springrestwithai.feature.auth.dto.RegisterRequest;
import vn.luuhien.springrestwithai.feature.user.dto.UserResponse;

import jakarta.servlet.http.HttpServletRequest;

public interface AuthService {

    LoginResponse login(LoginRequest request, HttpServletRequest httpRequest);

    UserResponse register(RegisterRequest request);

    LoginResponse refresh(RefreshTokenRequest request, String refreshTokenFromCookie, HttpServletRequest httpRequest);

    UserResponse getCurrentUser(String email);

    void logout(String userEmail);
}

