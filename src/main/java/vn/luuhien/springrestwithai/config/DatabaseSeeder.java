package vn.luuhien.springrestwithai.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import vn.luuhien.springrestwithai.feature.company.Company;
import vn.luuhien.springrestwithai.feature.company.CompanyRepository;
import vn.luuhien.springrestwithai.feature.permission.Permission;
import vn.luuhien.springrestwithai.feature.permission.PermissionRepository;
import vn.luuhien.springrestwithai.feature.role.Role;
import vn.luuhien.springrestwithai.feature.role.RoleRepository;
import vn.luuhien.springrestwithai.feature.user.User;
import vn.luuhien.springrestwithai.feature.user.UserRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@Profile("!test")
@ConditionalOnProperty(name = "app.seed-data", havingValue = "true")
public class DatabaseSeeder implements CommandLineRunner {

    private static final String DEFAULT_PASSWORD = "123456";

    private final PermissionRepository permissionRepository;
    private final RoleRepository roleRepository;
    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DatabaseSeeder(
            PermissionRepository permissionRepository,
            RoleRepository roleRepository,
            CompanyRepository companyRepository,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder) {
        this.permissionRepository = permissionRepository;
        this.roleRepository = roleRepository;
        this.companyRepository = companyRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        seedPermissions();
        seedRoles();
        seedCompanies();
        seedUsers();
    }

    private void seedPermissions() {
        createPermissionIfMissing("Get users", "/api/v1/users", "GET", "USER");
        createPermissionIfMissing("Create user", "/api/v1/users", "POST", "USER");
        createPermissionIfMissing("Update user", "/api/v1/users", "PUT", "USER");
        createPermissionIfMissing("Delete user", "/api/v1/users/{id}", "DELETE", "USER");

        createPermissionIfMissing("Get roles", "/api/v1/roles", "GET", "ROLE");
        createPermissionIfMissing("Create role", "/api/v1/roles", "POST", "ROLE");
        createPermissionIfMissing("Update role", "/api/v1/roles", "PUT", "ROLE");
        createPermissionIfMissing("Delete role", "/api/v1/roles/{id}", "DELETE", "ROLE");

        createPermissionIfMissing("Get companies", "/api/v1/companies", "GET", "COMPANY");
        createPermissionIfMissing("Create company", "/api/v1/companies", "POST", "COMPANY");
        createPermissionIfMissing("Update company", "/api/v1/companies", "PUT", "COMPANY");
        createPermissionIfMissing("Delete company", "/api/v1/companies/{id}", "DELETE", "COMPANY");
    }

    private void createPermissionIfMissing(String name, String apiPath, String method, String module) {
        String normalizedMethod = method.trim().toUpperCase(Locale.ROOT);
        String normalizedModule = module.trim().toUpperCase(Locale.ROOT);

        if (permissionRepository.existsByApiPathAndMethod(apiPath, normalizedMethod)) {
            return;
        }

        Permission permission = new Permission();
        permission.setName(name);
        permission.setApiPath(apiPath);
        permission.setMethod(normalizedMethod);
        permission.setModule(normalizedModule);
        permissionRepository.save(permission);
    }

    private void seedRoles() {
        Map<String, Permission> permissionByName = permissionRepository.findAll().stream()
                .collect(Collectors.toMap(permission -> normalize(permission.getName()), Function.identity()));

        Role superAdminRole = createOrUpdateRole(
                "SUPER_ADMIN",
                "Super administrator role",
                new ArrayList<>(permissionByName.values()));

        Permission getUsersPermission = permissionByName.get(normalize("Get users"));
        Permission createUserPermission = permissionByName.get(normalize("Create user"));
        Permission updateUserPermission = permissionByName.get(normalize("Update user"));
        Permission getCompaniesPermission = permissionByName.get(normalize("Get companies"));

        List<Permission> hrPermissions = new ArrayList<>();
        addPermissionIfPresent(hrPermissions, getUsersPermission);
        addPermissionIfPresent(hrPermissions, createUserPermission);
        addPermissionIfPresent(hrPermissions, updateUserPermission);
        addPermissionIfPresent(hrPermissions, getCompaniesPermission);

        Role hrRole = createOrUpdateRole("HR", "Human resources role", hrPermissions);

        List<Permission> managerPermissions = new ArrayList<>();
        addPermissionIfPresent(managerPermissions, getUsersPermission);
        addPermissionIfPresent(managerPermissions, getCompaniesPermission);

        Role managerRole = createOrUpdateRole("MANAGER", "Manager role", managerPermissions);

        List<Permission> userPermissions = new ArrayList<>();
        addPermissionIfPresent(userPermissions, getUsersPermission);

        createOrUpdateRole("USER", "Basic user role", userPermissions);
    }

    private Role createOrUpdateRole(String name, String description, List<Permission> permissions) {
        Role role = findRoleByNameIgnoreCase(name);
        if (role == null) {
            role = new Role();
            role.setName(name);
        }

        role.setDescription(description);
        role.setPermissions(new ArrayList<>(permissions));

        return roleRepository.save(role);
    }

    private void addPermissionIfPresent(List<Permission> target, Permission permission) {
        if (permission != null) {
            target.add(permission);
        }
    }

    private void seedCompanies() {
        createCompanyIfMissing(
                "LuuHien Tech",
                "Main technology company",
                "Ho Chi Minh City, Viet Nam",
                "/uploads/logos/luuhientech.png");

        createCompanyIfMissing(
                "ABC Software",
                "Software outsourcing company",
                "Ha Noi, Viet Nam",
                "/uploads/logos/abc-software.png");

        createCompanyIfMissing(
                "Bla Bla Corporation",
                "Multi-domain business corporation",
                "Da Nang, Viet Nam",
                "/uploads/logos/bla-bla-corporation.png");
    }

    private void createCompanyIfMissing(String name, String description, String address, String logo) {
        if (companyRepository.existsByNameIgnoreCase(name)) {
            return;
        }

        Company company = new Company();
        company.setName(name);
        company.setDescription(description);
        company.setAddress(address);
        company.setLogo(logo);
        companyRepository.save(company);
    }

    private void seedUsers() {
        Role superAdminRole = findRoleByNameIgnoreCase("SUPER_ADMIN");
        Role hrRole = findRoleByNameIgnoreCase("HR");
        Role managerRole = findRoleByNameIgnoreCase("MANAGER");
        Role userRole = findRoleByNameIgnoreCase("USER");

        Company luuHienTech = findCompanyByNameIgnoreCase("LuuHien Tech");
        Company abcSoftware = findCompanyByNameIgnoreCase("ABC Software");
        Company blaBlaCorporation = findCompanyByNameIgnoreCase("Bla Bla Corporation");

        createUserIfMissing(
                "admin@example.com",
                "Super Admin",
                DEFAULT_PASSWORD,
                30,
                "Ho Chi Minh City, Viet Nam",
                User.GenderEnum.MALE,
                "/uploads/avatars/super-admin.png",
                luuHienTech,
                superAdminRole != null ? List.of(superAdminRole) : List.of());

        createUserIfMissing(
                "hr@example.com",
                "HR Manager",
                DEFAULT_PASSWORD,
                28,
                "Ha Noi, Viet Nam",
                User.GenderEnum.FEMALE,
                "/uploads/avatars/hr-manager.png",
                abcSoftware,
                hrRole != null ? List.of(hrRole) : List.of());

        createUserIfMissing(
                "manager@example.com",
                "Product Manager",
                DEFAULT_PASSWORD,
                29,
                "Da Nang, Viet Nam",
                User.GenderEnum.MALE,
                "/uploads/avatars/manager.png",
                blaBlaCorporation,
                managerRole != null ? List.of(managerRole) : List.of());

        createUserIfMissing(
                "user@example.com",
                "Demo User",
                DEFAULT_PASSWORD,
                25,
                "Da Nang, Viet Nam",
                User.GenderEnum.FEMALE,
                "/uploads/avatars/default-user.png",
                abcSoftware,
                userRole != null ? List.of(userRole) : List.of());
    }

    private void createUserIfMissing(
            String email,
            String name,
            String rawPassword,
            Integer age,
            String address,
            User.GenderEnum gender,
            String avatar,
            Company company,
            List<Role> roles) {
        if (userRepository.existsByEmailIgnoreCase(email)) {
            return;
        }

        User user = new User();
        user.setEmail(email.trim().toLowerCase(Locale.ROOT));
        user.setName(name);
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setAge(age);
        user.setAddress(address);
        user.setGender(gender);
        user.setAvatar(avatar);
        user.setCompany(company);
        user.setRoles(new ArrayList<>(roles));

        userRepository.save(user);
    }

    private Role findRoleByNameIgnoreCase(String roleName) {
        String normalized = normalize(roleName);
        return roleRepository.findAll().stream()
                .filter(role -> normalize(role.getName()).equals(normalized))
                .findFirst()
                .orElse(null);
    }

    private Company findCompanyByNameIgnoreCase(String companyName) {
        String normalized = normalize(companyName);
        return companyRepository.findAll().stream()
                .filter(company -> normalize(company.getName()).equals(normalized))
                .findFirst()
                .orElse(null);
    }

    private String normalize(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().toLowerCase(Locale.ROOT);
    }
}
