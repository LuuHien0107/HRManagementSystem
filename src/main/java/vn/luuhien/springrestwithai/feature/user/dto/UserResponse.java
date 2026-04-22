package vn.luuhien.springrestwithai.feature.user.dto;

import vn.luuhien.springrestwithai.feature.company.Company;
import vn.luuhien.springrestwithai.feature.role.Role;
import vn.luuhien.springrestwithai.feature.user.User;

import java.time.Instant;
import java.util.List;

public record UserResponse(
        Long id,
        String name,
        String email,
        Integer age,
        String address,
        User.GenderEnum gender,
        String avatar,
        CompanySummary company,
        List<RoleSummary> roles,
        Instant createdAt,
        Instant updatedAt) {

    public static UserResponse fromEntity(User user) {
        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getAge(),
                user.getAddress(),
                user.getGender(),
                user.getAvatar(),
                CompanySummary.fromEntity(user.getCompany()),
                user.getRoles() == null
                        ? List.of()
                        : user.getRoles().stream().map(RoleSummary::fromEntity).toList(),
                user.getCreatedAt(),
                user.getUpdatedAt());
    }

    public record CompanySummary(Long id, String name) {
        static CompanySummary fromEntity(Company company) {
            if (company == null) {
                return null;
            }
            return new CompanySummary(company.getId(), company.getName());
        }
    }

    public record RoleSummary(Long id, String name) {
        static RoleSummary fromEntity(Role role) {
            return new RoleSummary(role.getId(), role.getName());
        }
    }
}
