package vn.luuhien.springrestwithai.feature.user;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import vn.luuhien.springrestwithai.feature.user.dto.CreateUserRequest;
import vn.luuhien.springrestwithai.feature.user.dto.UpdateUserRequest;
import vn.luuhien.springrestwithai.feature.user.dto.UserFilterRequest;
import vn.luuhien.springrestwithai.feature.user.dto.UserResponse;

public interface UserService {

    UserResponse createUser(CreateUserRequest request);

    UserResponse getUserById(Long id);

    Page<UserResponse> getAllUsers(UserFilterRequest filter, Pageable pageable);

    UserResponse updateUser(UpdateUserRequest request);

    void deleteUser(Long id);
}
