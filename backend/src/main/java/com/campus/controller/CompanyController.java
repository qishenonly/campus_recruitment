package com.campus.controller;

import com.campus.model.Company;
import com.campus.service.CompanyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/companies")
public class CompanyController {

    @Autowired
    private CompanyService companyService;

    @GetMapping
    public ResponseEntity<Page<Company>> getAllCompanies(Pageable pageable) {
        return ResponseEntity.ok(companyService.findAllCompanies(pageable));
    }

    @GetMapping("/search")
    public ResponseEntity<Page<Company>> searchCompanies(@RequestParam String keyword, Pageable pageable) {
        return ResponseEntity.ok(companyService.searchCompanies(keyword, pageable));
    }
} 