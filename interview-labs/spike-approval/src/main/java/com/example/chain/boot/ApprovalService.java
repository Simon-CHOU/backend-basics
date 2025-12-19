package com.example.chain.boot;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ApprovalService {

    private final TeamLeader teamLeader;
    private final DepartmentManager departmentManager;
    private final CEO ceo;

    private ApprovalHandler chain;

    @PostConstruct
    public void init() {
        // Chain: TeamLeader -> DepartmentManager -> CEO
        teamLeader.setNext(departmentManager);
        departmentManager.setNext(ceo);
        
        this.chain = teamLeader;
    }

    public ApprovalResponse processRequest(ApprovalRequest request) {
        return chain.handle(request);
    }
}
