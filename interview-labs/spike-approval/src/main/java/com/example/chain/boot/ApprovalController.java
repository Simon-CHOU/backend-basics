package com.example.chain.boot;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/approval")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Slf4j
public class ApprovalController {

    private final ApprovalService approvalService;

    @PostMapping("/submit")
    public ApprovalResponse approve(@RequestBody ApprovalRequest request) {
        log.info("Received approval request: amount={}, purpose={}", request.getAmount(), request.getPurpose());
        return approvalService.processRequest(request);
    }
}
