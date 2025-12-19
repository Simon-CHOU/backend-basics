package com.example.chain.standard;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class StandardChainTest {

    @Test
    public void testChainHandling() {
        // Setup Chain
        SupportHandler expert = new ExpertSupportHandler(null);
        SupportHandler advanced = new AdvancedSupportHandler(expert);
        SupportHandler basic = new BasicSupportHandler(advanced);

        // Test Basic Request
        SupportRequest request1 = new SupportRequest(SupportLevel.BASIC, "Password reset");
        String result1 = basic.handleRequest(request1);
        assertEquals("BasicSupport: Handled Password reset", result1);

        // Test Advanced Request
        SupportRequest request2 = new SupportRequest(SupportLevel.ADVANCED, "Database optimization");
        String result2 = basic.handleRequest(request2);
        assertEquals("AdvancedSupport: Handled Database optimization", result2);

        // Test Expert Request
        SupportRequest request3 = new SupportRequest(SupportLevel.EXPERT, "System architecture design");
        String result3 = basic.handleRequest(request3);
        assertEquals("ExpertSupport: Handled System architecture design", result3);
        
        // Test Unhandled Request
        SupportRequest request4 = new SupportRequest(SupportLevel.UNKNOWN, "Alien invasion");
        String result4 = basic.handleRequest(request4);
        assertEquals("Request cannot be handled", result4);
    }
}
