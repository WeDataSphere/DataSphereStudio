package com.webank.wedatasphere.dss.framework.workspace.restful;

import com.webank.wedatasphere.dss.framework.admin.service.DssAdminUserService;
import lombok.SneakyThrows;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import javax.ws.rs.core.MediaType;

@TestMethodOrder(MethodOrderer.OrderAnnotation. class )
@SpringBootTest(classes=com.webank.wedatasphere.dss.Application.class)
//@AutoConfigureMockMvc
class DSSWorkspaceUserRestfulTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(DSSWorkspaceUserRestfulTest.class);

//    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DssAdminUserService dssUserService;

    @BeforeEach
    void before(WebApplicationContext context) {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .addFilter((request, response, chain) -> {
                    response.setCharacterEncoding("UTF-8");
                    chain.doFilter(request, response);
                }, "/*")
                .build();
    }
    @Order ( 2 )
    @SneakyThrows
    @org.junit.jupiter.api.Test
    void revokeUserRole() {
        MockHttpServletResponse response = mockMvc.perform(MockMvcRequestBuilders.post("/dss/framework/workspace/revokeUserRole")
                .content("{\n" +
                        "  \"userName\": \"hadoop\",\n" +
                        "  \"workspaceIds\": [224],\n" +
                        "  \"roleIds\": [1,2,3]\n" +
                        "}")
                .header("Token-Code", "HPMS-KhFGSQkdaaCPBYfE")
                .contentType(MediaType.APPLICATION_JSON)
        ).andReturn().getResponse();
        LOGGER.info(response.getContentAsString());
        Assertions.assertEquals(200,response.getStatus());
    }
    @Order ( 1 )
    @SneakyThrows
    @Test
    void getWorkspaceUserRole() {
        MockHttpServletResponse response = mockMvc.perform(MockMvcRequestBuilders.get("/dss/framework/workspace/getUserRole")
                .param("userName", "hadoop")
                .header("Token-Code", "HPMS-KhFGSQkdaaCPBYfE")
                .contentType("application/json")
        ).andReturn().getResponse();
        LOGGER.info(response.getContentAsString());
        Assertions.assertEquals(200,response.getStatus());
    }

    @Order ( 3 )
    @Test
    void getWorkspaceUserRole2() {
        getWorkspaceUserRole();
    }

}