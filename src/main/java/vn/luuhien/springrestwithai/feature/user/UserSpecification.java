package vn.luuhien.springrestwithai.feature.user;

import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import vn.luuhien.springrestwithai.feature.user.dto.UserFilterRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class UserSpecification {

    private UserSpecification() {
    }

    public static Specification<User> build(UserFilterRequest filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filter == null) {
                return cb.and(predicates.toArray(new Predicate[0]));
            }

            if (isNotBlank(filter.name())) {
                predicates.add(cb.like(
                        cb.lower(root.get("name")),
                        "%" + filter.name().trim().toLowerCase(Locale.ROOT) + "%"));
            }

            if (isNotBlank(filter.address())) {
                predicates.add(cb.like(
                        cb.lower(root.get("address")),
                        "%" + filter.address().trim().toLowerCase(Locale.ROOT) + "%"));
            }

            if (isNotBlank(filter.email())) {
                predicates.add(cb.like(
                        cb.lower(root.get("email")),
                        "%" + filter.email().trim().toLowerCase(Locale.ROOT) + "%"));
            }

            if (filter.ageFrom() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("age"), filter.ageFrom()));
            }

            if (filter.ageTo() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("age"), filter.ageTo()));
            }

            if (filter.gender() != null) {
                predicates.add(cb.equal(root.get("gender"), filter.gender()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private static boolean isNotBlank(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
