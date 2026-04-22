package vn.luuhien.springrestwithai.feature.company;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import vn.luuhien.springrestwithai.exception.DuplicateResourceException;
import vn.luuhien.springrestwithai.exception.ResourceNotFoundException;
import vn.luuhien.springrestwithai.feature.company.dto.CompanyResponse;
import vn.luuhien.springrestwithai.feature.company.dto.CreateCompanyRequest;
import vn.luuhien.springrestwithai.feature.company.dto.UpdateCompanyRequest;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CompanyServiceImplTest {

    @Mock
    private CompanyRepository companyRepository;

    @InjectMocks
    private CompanyServiceImpl companyService;

    @Test
    @DisplayName("Create company success")
    void createCompany_validRequest_returnCreatedCompany() {
        CreateCompanyRequest request = new CreateCompanyRequest("FPT Software", "IT outsourcing", "Ha Noi",
                "/logos/fpt.png");
        Company savedCompany = buildCompany(1L, "FPT Software", "IT outsourcing", "Ha Noi", "/logos/fpt.png");

        when(companyRepository.existsByNameIgnoreCase("FPT Software")).thenReturn(false);
        when(companyRepository.save(any(Company.class))).thenReturn(savedCompany);

        CompanyResponse response = companyService.createCompany(request);

        assertEquals(1L, response.id());
        assertEquals("FPT Software", response.name());
        verify(companyRepository).save(any(Company.class));
    }

    @Test
    @DisplayName("Create company duplicate name")
    void createCompany_duplicateName_throwDuplicateResourceException() {
        CreateCompanyRequest request = new CreateCompanyRequest("FPT Software", null, null, null);
        when(companyRepository.existsByNameIgnoreCase("FPT Software")).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> companyService.createCompany(request));
        verify(companyRepository, never()).save(any(Company.class));
    }

    @Test
    @DisplayName("Get company by id success")
    void getCompanyById_found_returnCompany() {
        Company company = buildCompany(1L, "LuuHienIT", "Education platform", "HCM", "/logos/lh.png");
        when(companyRepository.findById(1L)).thenReturn(Optional.of(company));

        CompanyResponse response = companyService.getCompanyById(1L);

        assertEquals(1L, response.id());
        assertEquals("LuuHienIT", response.name());
    }

    @Test
    @DisplayName("Get company by id not found")
    void getCompanyById_notFound_throwResourceNotFoundException() {
        when(companyRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> companyService.getCompanyById(999L));
    }

    @Test
    @DisplayName("Get all companies returns list")
    void getAllCompanies_hasData_returnNonEmptyPage() {
        Company company = buildCompany(1L, "LuuHienIT", "Education platform", "HCM", "/logos/lh.png");
        Page<Company> page = new PageImpl<>(List.of(company));

        when(companyRepository.findAll(PageRequest.of(0, 10))).thenReturn(page);

        Page<CompanyResponse> responsePage = companyService.getAllCompanies(PageRequest.of(0, 10));

        assertFalse(responsePage.isEmpty());
        assertEquals(1, responsePage.getTotalElements());
    }

    @Test
    @DisplayName("Get all companies returns empty list")
    void getAllCompanies_empty_returnEmptyPage() {
        Page<Company> page = new PageImpl<>(List.of());

        when(companyRepository.findAll(PageRequest.of(0, 10))).thenReturn(page);

        Page<CompanyResponse> responsePage = companyService.getAllCompanies(PageRequest.of(0, 10));

        assertTrue(responsePage.isEmpty());
    }

    @Test
    @DisplayName("Update company success")
    void updateCompany_validRequest_returnUpdatedCompany() {
        Company existing = buildCompany(2L, "FPT Software", "Old", "Ha Noi", "/logos/fpt.png");
        UpdateCompanyRequest request = new UpdateCompanyRequest(2L, "FPT Software Updated", "New", "Da Nang",
                "/logos/fpt-new.png");

        when(companyRepository.findById(2L)).thenReturn(Optional.of(existing));
        when(companyRepository.existsByNameIgnoreCaseAndIdNot("FPT Software Updated", 2L)).thenReturn(false);
        when(companyRepository.save(any(Company.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CompanyResponse response = companyService.updateCompany(request);

        assertEquals("FPT Software Updated", response.name());
        assertEquals("Da Nang", response.address());
    }

    @Test
    @DisplayName("Update company not found")
    void updateCompany_notFound_throwResourceNotFoundException() {
        UpdateCompanyRequest request = new UpdateCompanyRequest(100L, "Name", null, null, null);
        when(companyRepository.findById(100L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> companyService.updateCompany(request));
    }

    @Test
    @DisplayName("Update company duplicate name")
    void updateCompany_duplicateName_throwDuplicateResourceException() {
        Company existing = buildCompany(2L, "FPT Software", "Old", "Ha Noi", "/logos/fpt.png");
        UpdateCompanyRequest request = new UpdateCompanyRequest(2L, "LuuHienIT", "New", "Da Nang",
                "/logos/fpt-new.png");

        when(companyRepository.findById(2L)).thenReturn(Optional.of(existing));
        when(companyRepository.existsByNameIgnoreCaseAndIdNot("LuuHienIT", 2L)).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> companyService.updateCompany(request));
    }

    @Test
    @DisplayName("Delete company success")
    void deleteCompany_found_deleteSuccessfully() {
        Company existing = buildCompany(1L, "LuuHienIT", "Education", "HCM", "/logos/lh.png");
        when(companyRepository.findById(1L)).thenReturn(Optional.of(existing));

        companyService.deleteCompany(1L);

        verify(companyRepository).delete(existing);
    }

    @Test
    @DisplayName("Delete company not found")
    void deleteCompany_notFound_throwResourceNotFoundException() {
        when(companyRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> companyService.deleteCompany(999L));
    }

    private Company buildCompany(Long id, String name, String description, String address, String logo) {
        Company company = new Company();
        company.setId(id);
        company.setName(name);
        company.setDescription(description);
        company.setAddress(address);
        company.setLogo(logo);
        return company;
    }
}
