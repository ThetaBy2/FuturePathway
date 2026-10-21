package com.futurepathway.service;

import com.futurepathway.dto.BranchStatDto;
import com.futurepathway.dto.CompanyStatDto;
import com.futurepathway.dto.DashboardDto;
import com.futurepathway.dto.PlacementStatsDto;
import com.futurepathway.entity.Company;
import com.futurepathway.enums.ApplicationStatus;
import com.futurepathway.repository.ApplicationRepository;
import com.futurepathway.repository.CompanyRepository;
import com.futurepathway.repository.JobRepository;
import com.futurepathway.repository.StudentRepository;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final StudentRepository studentRepository;
    private final CompanyRepository companyRepository;
    private final JobRepository jobRepository;
    private final ApplicationRepository applicationRepository;

    @Transactional(readOnly = true)
    public DashboardDto dashboard() {
        Map<String, Long> byStatus = applicationRepository.countGroupedByStatus().stream()
                .collect(Collectors.toMap(
                        row -> ((ApplicationStatus) row[0]).name(),
                        row -> (Long) row[1]));
        PlacementStatsDto stats = new PlacementStatsDto(
                studentRepository.count(),
                companyRepository.count(),
                jobRepository.count(),
                jobRepository.countByActiveTrue(),
                applicationRepository.count(),
                byStatus);

        // branch-wise: students + applications + selected
        Map<String, Long> studentsByBranch = new TreeMap<>();
        studentRepository.countGroupedByBranch()
                .forEach(row -> studentsByBranch.put(String.valueOf(row[0]), (Long) row[1]));
        Map<String, Long> appsByBranch = new HashMap<>();
        Map<String, Long> selectedByBranch = new HashMap<>();
        applicationRepository.countGroupedByBranchAndStatus().forEach(row -> {
            String branch = String.valueOf(row[0]);
            ApplicationStatus status = (ApplicationStatus) row[1];
            long count = (Long) row[2];
            appsByBranch.merge(branch, count, Long::sum);
            studentsByBranch.putIfAbsent(branch, 0L);
            if (status == ApplicationStatus.SELECTED) {
                selectedByBranch.merge(branch, count, Long::sum);
            }
        });
        List<BranchStatDto> byBranch = studentsByBranch.keySet().stream()
                .map(b -> new BranchStatDto(b, studentsByBranch.getOrDefault(b, 0L),
                        appsByBranch.getOrDefault(b, 0L), selectedByBranch.getOrDefault(b, 0L)))
                .toList();

        // company-wise: jobs + applications + selected
        Map<String, Long> appsByCompany = applicationRepository.countGroupedByCompany().stream()
                .collect(Collectors.toMap(row -> String.valueOf(row[0]), row -> (Long) row[1]));
        Map<String, Long> selectedByCompany = applicationRepository.countSelectedGroupedByCompany().stream()
                .collect(Collectors.toMap(row -> String.valueOf(row[0]), row -> (Long) row[1]));
        List<Company> companies = companyRepository.findAll();
        List<CompanyStatDto> byCompany = companies.stream()
                .map(c -> new CompanyStatDto(c.getName(), jobRepository.countByCompanyId(c.getId()),
                        appsByCompany.getOrDefault(c.getName(), 0L),
                        selectedByCompany.getOrDefault(c.getName(), 0L)))
                .toList();

        return new DashboardDto(stats, byBranch, byCompany);
    }
}
