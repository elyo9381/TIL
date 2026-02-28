package com.yowon.psc.case003.repro.api;

import com.yowon.psc.case003.repro.application.JoinHintBenchmarkService;
import com.yowon.psc.case003.repro.application.JoinHintReproService;
import com.yowon.psc.case003.repro.domain.JoinHintBenchmarkReport;
import com.yowon.psc.case003.repro.domain.JoinHintReproReport;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/case-003/join-hint")
public class Case003JoinHintController {

    private final JoinHintReproService service;
    private final JoinHintBenchmarkService benchmarkService;

    public Case003JoinHintController(
        JoinHintReproService service,
        JoinHintBenchmarkService benchmarkService
    ) {
        this.service = service;
        this.benchmarkService = benchmarkService;
    }

    @GetMapping("/report")
    public JoinHintReproReport report(
        @RequestParam(name = "serviceType", defaultValue = "VIP") String serviceType,
        @RequestParam(name = "limit", defaultValue = "20") int limit
    ) {
        return service.run(serviceType, Math.max(1, limit));
    }

    @GetMapping("/benchmark")
    public JoinHintBenchmarkReport benchmark(
        @RequestParam(name = "serviceType", defaultValue = "VIP") String serviceType,
        @RequestParam(name = "limit", defaultValue = "20") int limit,
        @RequestParam(name = "iterations", defaultValue = "10") int iterations
    ) {
        return benchmarkService.benchmark(serviceType, limit, iterations);
    }
}
