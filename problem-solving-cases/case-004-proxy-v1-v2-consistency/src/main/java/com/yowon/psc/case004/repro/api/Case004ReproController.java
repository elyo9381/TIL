package com.yowon.psc.case004.repro.api;

import com.yowon.psc.case004.repro.application.Case004ReproService;
import com.yowon.psc.case004.repro.domain.Case004CompareCommand;
import com.yowon.psc.case004.repro.domain.Case004Profile;
import com.yowon.psc.case004.repro.domain.Case004ProfileReport;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/case-004/proxy/repro")
public class Case004ReproController {

    private final Case004ReproService service;
    private final String defaultDomain;

    public Case004ReproController(
        Case004ReproService service,
        @Value("${app.repro.default-domain:domain-a}") String defaultDomain
    ) {
        this.service = service;
        this.defaultDomain = defaultDomain;
    }

    @GetMapping("/report")
    public Case004ProfileReport report(
        @RequestParam(name = "profile", defaultValue = "v2-target") String profile,
        @RequestParam(name = "domain", required = false) String domain,
        @RequestParam(name = "concurrency", defaultValue = "200") int concurrency,
        @RequestParam(name = "poolSize", defaultValue = "10") int poolSize
    ) {
        return service.report(
            Case004Profile.from(profile),
            new Case004CompareCommand(
                domain == null || domain.isBlank() ? defaultDomain : domain,
                concurrency,
                poolSize
            )
        );
    }
}
