package vn.luuhien.springrestwithai.feature.company.dto;

import java.time.Instant;

import vn.luuhien.springrestwithai.feature.company.Company;

public record CompanyResponse(
        Long id,
        String name,
        String description,
        String address,
        String logo,
        Instant createdAt,
        Instant updatedAt) {

    public static CompanyResponse fromEntity(Company company) {
        return new CompanyResponse(
                company.getId(),
                company.getName(),
                company.getDescription(),
                company.getAddress(),
                company.getLogo(),
                company.getCreatedAt(),
                company.getUpdatedAt());
    }
}
