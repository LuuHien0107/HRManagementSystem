package vn.luuhien.springrestwithai.feature.user;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;

import vn.luuhien.springrestwithai.exception.DuplicateResourceException;
import vn.luuhien.springrestwithai.exception.ResourceNotFoundException;
import vn.luuhien.springrestwithai.feature.company.Company;
import vn.luuhien.springrestwithai.feature.company.CompanyRepository;
import vn.luuhien.springrestwithai.feature.role.Role;
import vn.luuhien.springrestwithai.feature.role.RoleRepository;
import vn.luuhien.springrestwithai.feature.user.dto.CreateUserRequest;
import vn.luuhien.springrestwithai.feature.user.dto.UpdateUserRequest;
import vn.luuhien.springrestwithai.feature.user.dto.UserFilterRequest;
import vn.luuhien.springrestwithai.feature.user.dto.UserResponse;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private CompanyRepository companyRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    @DisplayName("createUser success")
    void createUser_validRequest_returnCreatedUser() {
        Company company = buildCompany(1L, "LuuHienIT");
        Role role = buildRole(3L, "HR");

        CreateUserRequest request = new CreateUserRequest(
                "Nguyen Van A",
                "user@example.com",
                "password123",
                25,
                "HCM",
                User.GenderEnum.MALE,
                null,
                1L,
                List.of(3L));

        User savedUser = buildUser(1L, "Nguyen Van A", "user@example.com", "encoded-password", company, List.of(role));

        when(userRepository.existsByEmailIgnoreCase("user@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encoded-password");
        when(companyRepository.findById(1L)).thenReturn(Optional.of(company));
        when(roleRepository.findAllById(List.of(3L))).thenReturn(List.of(role));
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        UserResponse response = userService.createUser(request);

        assertEquals(1L, response.id());
        assertEquals("user@example.com", response.email());
        assertEquals(1L, response.company().id());
        assertEquals(1, response.roles().size());
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("createUser duplicate email")
    void createUser_duplicateEmail_throwDuplicateResourceException() {
        CreateUserRequest request = new CreateUserRequest(
                "Nguyen Van A",
                "dup@example.com",
                "password123",
                25,
                "HCM",
                User.GenderEnum.MALE,
                null,
                null,
                List.of(1L));

        when(userRepository.existsByEmailIgnoreCase("dup@example.com")).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> userService.createUser(request));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("createUser related company not found")
    void createUser_companyNotFound_throwResourceNotFoundException() {
        CreateUserRequest request = new CreateUserRequest(
                "Nguyen Van A",
                "user@example.com",
                "password123",
                25,
                "HCM",
                User.GenderEnum.MALE,
                null,
                99L,
                List.of(1L));

        when(userRepository.existsByEmailIgnoreCase("user@example.com")).thenReturn(false);
        when(companyRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.createUser(request));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("getUserById success")
    void getUserById_found_returnUser() {
        Company company = buildCompany(1L, "LuuHienIT");
        Role role = buildRole(3L, "HR");
        User user = buildUser(1L, "Nguyen Van A", "user@example.com", "encoded", company, List.of(role));

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        UserResponse response = userService.getUserById(1L);

        assertEquals(1L, response.id());
        assertEquals("Nguyen Van A", response.name());
    }

    @Test
    @DisplayName("getUserById not found")
    void getUserById_notFound_throwResourceNotFoundException() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.getUserById(999L));
    }

    @Test
    @DisplayName("getAllUsers returns list")
    void getAllUsers_hasData_returnNonEmptyPage() {
        User user = buildUser(1L, "Nguyen Van A", "user@example.com", "encoded", null, List.of());
        Page<User> page = new PageImpl<>(List.of(user));

        when(userRepository.findAll(any(org.springframework.data.jpa.domain.Specification.class), eq(PageRequest.of(0, 10))))
                .thenReturn(page);

        Page<UserResponse> responsePage = userService.getAllUsers(new UserFilterRequest(null, null, null, null, null, null),
                PageRequest.of(0, 10));

        assertFalse(responsePage.isEmpty());
        assertEquals(1, responsePage.getTotalElements());
    }

    @Test
    @DisplayName("getAllUsers returns empty list")
    void getAllUsers_empty_returnEmptyPage() {
        when(userRepository.findAll(any(org.springframework.data.jpa.domain.Specification.class), eq(PageRequest.of(0, 10))))
                .thenReturn(new PageImpl<>(List.of()));

        Page<UserResponse> responsePage = userService.getAllUsers(new UserFilterRequest(null, null, null, null, null, null),
                PageRequest.of(0, 10));

        assertTrue(responsePage.isEmpty());
    }

    @Test
    @DisplayName("updateUser success")
    void updateUser_validRequest_returnUpdatedUser() {
        Company oldCompany = buildCompany(1L, "Old Company");
        Company newCompany = buildCompany(2L, "New Company");
        Role oldRole = buildRole(3L, "HR");
        Role newRole = buildRole(5L, "USER");

        User existing = buildUser(2L, "Old Name", "user@example.com", "encoded", oldCompany, List.of(oldRole));

        UpdateUserRequest request = new UpdateUserRequest(
                2L,
                "New Name",
                31,
                "Da Nang",
                User.GenderEnum.FEMALE,
                "/avatars/new.png",
                2L,
                List.of(5L));

        when(userRepository.findById(2L)).thenReturn(Optional.of(existing));
        when(companyRepository.findById(2L)).thenReturn(Optional.of(newCompany));
        when(roleRepository.findAllById(List.of(5L))).thenReturn(List.of(newRole));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserResponse response = userService.updateUser(request);

        assertEquals("New Name", response.name());
        assertEquals(2L, response.company().id());
        assertEquals(1, response.roles().size());
        assertEquals(5L, response.roles().get(0).id());
    }

    @Test
    @DisplayName("updateUser not found")
    void updateUser_notFound_throwResourceNotFoundException() {
        UpdateUserRequest request = new UpdateUserRequest(
                100L,
                "Name",
                20,
                "Address",
                User.GenderEnum.OTHER,
                null,
                null,
                List.of(1L));

        when(userRepository.findById(100L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.updateUser(request));
    }

    @Test
    @DisplayName("updateUser validation fail role not found")
    void updateUser_roleNotFound_throwResourceNotFoundException() {
        User existing = buildUser(2L, "Name", "user@example.com", "encoded", null, List.of());
        UpdateUserRequest request = new UpdateUserRequest(
                2L,
                "Name",
                20,
                "Address",
                User.GenderEnum.OTHER,
                null,
                null,
                List.of(1L, 999L));

        Role role = buildRole(1L, "ADMIN");

        when(userRepository.findById(2L)).thenReturn(Optional.of(existing));
        when(roleRepository.findAllById(List.of(1L, 999L))).thenReturn(List.of(role));

        assertThrows(ResourceNotFoundException.class, () -> userService.updateUser(request));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("deleteUser success")
    void deleteUser_found_deleteSuccessfully() {
        User existing = buildUser(1L, "Name", "user@example.com", "encoded", null, List.of());
        when(userRepository.findById(1L)).thenReturn(Optional.of(existing));

        userService.deleteUser(1L);

        verify(userRepository).delete(existing);
    }

    @Test
    @DisplayName("deleteUser not found")
    void deleteUser_notFound_throwResourceNotFoundException() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.deleteUser(999L));
    }

    private User buildUser(Long id, String name, String email, String password, Company company, List<Role> roles) {
        User user = new User();
        user.setId(id);
        user.setName(name);
        user.setEmail(email);
        user.setPassword(password);
        user.setCompany(company);
        user.setRoles(roles);
        return user;
    }

    private Company buildCompany(Long id, String name) {
        Company company = new Company();
        company.setId(id);
        company.setName(name);
        return company;
    }

    private Role buildRole(Long id, String name) {
        Role role = new Role();
        role.setId(id);
        role.setName(name);
        return role;
    }
}
