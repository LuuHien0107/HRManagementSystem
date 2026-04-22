package vn.luuhien.springrestwithai.feature.user;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import vn.luuhien.springrestwithai.exception.DuplicateResourceException;
import vn.luuhien.springrestwithai.exception.ResourceNotFoundException;
import vn.luuhien.springrestwithai.feature.company.Company;
import vn.luuhien.springrestwithai.feature.company.CompanyRepository;
import vn.luuhien.springrestwithai.feature.role.Role;
import vn.luuhien.springrestwithai.feature.role.RoleRepository;
import vn.luuhien.springrestwithai.feature.user.dto.CreateUserRequest;
import vn.luuhien.springrestwithai.feature.user.dto.UpdateUserRequest;
import vn.luuhien.springrestwithai.feature.user.dto.UserResponse;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(
            UserRepository userRepository,
            CompanyRepository companyRepository,
            RoleRepository roleRepository,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.companyRepository = companyRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public UserResponse createUser(CreateUserRequest request) {
        String normalizedEmail = normalizeEmail(request.email());
        if (userRepository.existsByEmailIgnoreCase(normalizedEmail)) {
            throw new DuplicateResourceException("User", "email", normalizedEmail);
        }

        User user = new User();
        user.setName(normalizeName(request.name()));
        user.setEmail(normalizedEmail);
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setAge(request.age());
        user.setAddress(trimToNull(request.address()));
        user.setGender(request.gender());
        user.setAvatar(trimToNull(request.avatar()));
        user.setCompany(resolveCompany(request.companyId()));
        user.setRoles(resolveRoles(request.roleIds()));

        return UserResponse.fromEntity(userRepository.save(user));
    }

    @Override
    public UserResponse getUserById(Long id) {
        return UserResponse.fromEntity(findUserById(id));
    }

    @Override
    public Page<UserResponse> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable).map(UserResponse::fromEntity);
    }

    @Override
    @Transactional
    public UserResponse updateUser(UpdateUserRequest request) {
        User existingUser = findUserById(request.id());

        existingUser.setName(normalizeName(request.name()));
        existingUser.setAge(request.age());
        existingUser.setAddress(trimToNull(request.address()));
        existingUser.setGender(request.gender());
        existingUser.setAvatar(trimToNull(request.avatar()));
        existingUser.setCompany(resolveCompany(request.companyId()));
        existingUser.setRoles(resolveRoles(request.roleIds()));

        return UserResponse.fromEntity(userRepository.save(existingUser));
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        User existingUser = findUserById(id);
        userRepository.delete(existingUser);
    }

    private User findUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
    }

    private Company resolveCompany(Long companyId) {
        if (companyId == null) {
            return null;
        }

        return companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Company", "id", companyId));
    }

    private List<Role> resolveRoles(List<Long> roleIds) {
        List<Long> uniqueIds = roleIds.stream().distinct().toList();
        List<Role> roles = roleRepository.findAllById(uniqueIds);

        if (roles.size() != uniqueIds.size()) {
            Set<Long> foundIds = new HashSet<>();
            for (Role role : roles) {
                foundIds.add(role.getId());
            }
            List<Long> missingIds = uniqueIds.stream().filter(id -> !foundIds.contains(id)).toList();
            throw new ResourceNotFoundException("Role", "ids", missingIds);
        }

        return new ArrayList<>(roles);
    }

    private String normalizeName(String value) {
        return value == null ? "" : value.trim();
    }

    private String normalizeEmail(String value) {
        return value == null ? "" : value.trim();
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }
}
