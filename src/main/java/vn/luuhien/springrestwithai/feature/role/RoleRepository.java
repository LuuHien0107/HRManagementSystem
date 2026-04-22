package vn.luuhien.springrestwithai.feature.role;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {

    boolean existsByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);

    Optional<Role> findFirstByNameIgnoreCase(String name);

    // Custom query để JOIN FETCH permissions
    @org.springframework.data.jpa.repository.Query("SELECT r FROM Role r LEFT JOIN FETCH r.permissions")
    java.util.List<Role> findAllWithPermissions();
}
