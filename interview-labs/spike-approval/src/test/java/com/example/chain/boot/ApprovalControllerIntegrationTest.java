package com.example.chain.boot;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class ApprovalControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void testTeamLeaderApproval() throws Exception {
        ApprovalRequest request = new ApprovalRequest(500.0, "Team Lunch");
        
        mockMvc.perform(post("/api/approval")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.approvedBy").value("Team Leader"))
                .andExpect(jsonPath("$.status").value("APPROVED"));
    }

    @Test
    public void testDeptManagerApproval() throws Exception {
        ApprovalRequest request = new ApprovalRequest(2500.0, "New Laptops");
        
        mockMvc.perform(post("/api/approval")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.approvedBy").value("Department Manager"))
                .andExpect(jsonPath("$.status").value("APPROVED"));
    }

    @Test
    public void testCEOApproval() throws Exception {
        ApprovalRequest request = new ApprovalRequest(10000.0, "New Office");
        
        mockMvc.perform(post("/api/approval")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.approvedBy").value("CEO"))
                .andExpect(jsonPath("$.status").value("APPROVED"));
    }
}
