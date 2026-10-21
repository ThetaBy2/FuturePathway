package com.futurepathway.service;

import com.futurepathway.dto.CompanyDto;
import com.futurepathway.dto.CompanyRequest;
import com.futurepathway.entity.AppUser;
import com.futurepathway.entity.Company;
import com.futurepathway.enums.Role;
import com.futurepathway.exception.ResourceNotFoundException;
import com.futurepathway.mapper.CompanyMapper;
import com.futurepathway.repository.CompanyRepository;
import com.futurepathway.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CompanyService {

    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;
    private final StudentService studentService; // reuse requireUser
    private final CompanyMapper companyMapper;

    @Transactional(readOnly = true)
    public Page<CompanyDto> listCompanies(Pageable pageable) {
        return companyRepository.findAll(pageable).map(companyMapper::toDto);
    }

    @Transactional(readOnly = true)
    public CompanyDto getCompany(Long id) {
        return companyMapper.toDto(requireCompany(id));
    }

    @Transactional(readOnly = true)
    public CompanyDto myCompany(String email) {
        return companyMapper.toDto(requireCompanyByEmail(email));
    }

    @Transactional
    public CompanyDto updateCompany(Long id, CompanyRequest req, String requesterEmail) {
        Company company = requireCompany(id);
        AppUser requester = studentService.requireUser(requesterEmail);
        boolean owner = company.getUser() != null && company.getUser().getId().equals(requester.getId());
        if (!owner && requester.getRole() != Role.COLLEGE && requester.getRole() != Role.ADMIN) {
            throw new AccessDeniedException("Not allowed to edit this company");
        }
        company.setName(req.name().strip());
        company.setIndustryDomain(req.industryDomain());
        company.setWebsite(req.website());
        company.setHrContactEmail(req.hrContactEmail());
        return companyMapper.toDto(company);
    }

    @Transactional
    public void deleteCompany(Long id) {
        Company company = requireCompany(id);
        AppUser user = company.getUser();
        companyRepository.delete(company); // jobs → applications → interviews cascade
        if (user != null) {
            userRepository.delete(user);
        }
        log.info("Deleted company {}", id);
    }

    public Company requireCompany(Long id) {
        return companyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found: " + id));
    }

    public Company requireCompanyByEmail(String email) {
        AppUser user = studentService.requireUser(email);
        return companyRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("No company profile for user: " + email));
    }
}
