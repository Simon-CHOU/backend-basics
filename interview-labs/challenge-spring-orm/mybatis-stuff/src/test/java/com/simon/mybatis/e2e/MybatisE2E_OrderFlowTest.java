package com.simon.mybatis.e2e;

import com.simon.mybatis.domain.Order;
import com.simon.mybatis.mapper.UserMapper;
import com.simon.mybatis.service.OrderFlowService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class MybatisE2E_OrderFlowTest {
    @Autowired OrderFlowService flow;
    @Autowired UserMapper users;

    @Test
    void E2E_createRenameLoad() {
        Long orderId = flow.createUserWithOrder("e2e_mb@x.com", "MB");
        Long userId = users.findByEmail("e2e_mb@x.com").getId();
        flow.renameUser(userId, "MB-NEW");
        Order loaded = flow.loadOrderWithItems(orderId);
        assertEquals(2, loaded.getItems().size());
    }
}

