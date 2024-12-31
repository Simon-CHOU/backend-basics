package com.simon.rabbitmq_firstlook;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
public class MsgControllerIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    public void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    /**
     * 测试通过的前提是，确保rabbitmq消息队列服务器已经启动
     * @throws Exception
     */
    @Test
    public void testSendMsg() throws Exception {
        // 创建一个测试用的JSON请求体
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("id", 11);
        requestBody.put("name", "simon");

        // 将请求体转换为JSON字符串
        String jsonRequest = objectMapper.writeValueAsString(requestBody);

        // 发起GET请求
        mockMvc.perform(post("/msg")
                       .contentType(MediaType.APPLICATION_JSON)
                       .content(jsonRequest))
                // 验证状态码为200
               .andExpect(status().isOk())
                // 验证响应内容为"success"
               .andExpect(content().string("success"));
    }
}
