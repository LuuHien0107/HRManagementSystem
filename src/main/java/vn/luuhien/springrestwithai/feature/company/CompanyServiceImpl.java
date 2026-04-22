package vn.luuhien.springrestwithai.feature.company;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import vn.luuhien.springrestwithai.exception.DuplicateResourceException;
import vn.luuhien.springrestwithai.exception.ResourceNotFoundException;
import vn.luuhien.springrestwithai.feature.company.dto.CompanyResponse;
import vn.luuhien.springrestwithai.feature.company.dto.CreateCompanyRequest;
import vn.luuhien.springrestwithai.feature.company.dto.UpdateCompanyRequest;

@Service
@Transactional(readOnly = true)
public class CompanyServiceImpl implements CompanyService {

    private final CompanyRepository companyRepository;

    public CompanyServiceImpl(CompanyRepository companyRepository) {
        this.companyRepository = companyRepository;
    }

    @Override
    @Transactional
    public CompanyResponse createCompany(CreateCompanyRequest request) {
        String normalizedName = normalizeName(request.name());
        if (companyRepository.existsByNameIgnoreCase(normalizedName)) {
            throw new DuplicateResourceException("Company", "name", normalizedName);
        }

        Company company = new Company();
        applyRequestData(company, normalizedName, request.description(), request.address(), request.logo());
        return CompanyResponse.fromEntity(companyRepository.save(company));
    }

    @Override
    public CompanyResponse getCompanyById(Long id) {
        return CompanyResponse.fromEntity(findCompanyById(id));
    }

    @Override
    public Page<CompanyResponse> getAllCompanies(Pageable pageable) {
        return companyRepository.findAll(pageable).map(CompanyResponse::fromEntity);
    }

    @Override
    @Transactional
    public CompanyResponse updateCompany(UpdateCompanyRequest request) {
        Company existingCompany = findCompanyById(request.id());
        String normalizedName = normalizeName(request.name());

        if (companyRepository.existsByNameIgnoreCaseAndIdNot(normalizedName, request.id())) {
            throw new DuplicateResourceException("Company", "name", normalizedName);
        }

        applyRequestData(existingCompany, normalizedName, request.description(), request.address(), request.logo());
        return CompanyResponse.fromEntity(companyRepository.save(existingCompany));
    }

    @Override
    @Transactional
    public void deleteCompany(Long id) {
        Company existingCompany = findCompanyById(id);
        companyRepository.delete(existingCompany);
    }

    private Company findCompanyById(Long id) {
        return companyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Company", "id", id));
    }

    private String normalizeName(String name) {
        return name == null ? "" : name.trim();
    }

    private void applyRequestData(Company company, String name, String description, String address, String logo) {
        company.setName(name);
        company.setDescription(trimToNull(description));
        company.setAddress(trimToNull(address));
        company.setLogo(trimToNull(logo));
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }
}
