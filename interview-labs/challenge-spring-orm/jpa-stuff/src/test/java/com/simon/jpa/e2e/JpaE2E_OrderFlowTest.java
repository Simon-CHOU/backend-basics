package com.simon.jpa.e2e;

import com.simon.jpa.domain.Order;
import com.simon.jpa.dto.UserSummary;
import com.simon.jpa.repo.UserRepository;
import com.simon.jpa.service.OrderFlowService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@DirtiesContext
class JpaE2E_OrderFlowTest {

    @Autowired OrderFlowService flow;
    @Autowired UserRepository users;

    @Test
    @Transactional
    void E2E_createRenameLoadSummaries() {
        Long orderId = flow.createUserWithOrder("e2e@x.com", "E2E");

        Long userId = users.findByEmail("e2e@x.com").orElseThrow().getId();
        flow.renameUser(userId, "E2E-NEW");

        Order loaded = flow.loadOrderWithItems(orderId);
        assertEquals(2, loaded.getItems().size());
        assertEquals("E2E-NEW", loaded.getUser().getName());

        List<UserSummary> summaries = flow.listSummaries();
        assertTrue(summaries.stream().anyMatch(s -> s.email().equals("e2e@x.com")));
    }
}

