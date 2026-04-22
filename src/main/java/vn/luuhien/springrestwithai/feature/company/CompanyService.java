package vn.luuhien.springrestwithai.feature.company;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import vn.luuhien.springrestwithai.feature.company.dto.CompanyResponse;
import vn.luuhien.springrestwithai.feature.company.dto.CreateCompanyRequest;
import vn.luuhien.springrestwithai.feature.company.dto.UpdateCompanyRequest;

public interface CompanyService {

    CompanyResponse createCompany(CreateCompanyRequest request);

    CompanyResponse getCompanyById(Long id);

    Page<CompanyResponse> getAllCompanies(Pageable pageable);

    CompanyResponse updateCompany(UpdateCompanyRequest request);

    void deleteCompany(Long id);
}
