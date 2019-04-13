package com.example.demo;

import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

import static org.assertj.core.api.BDDAssertions.then;

@RunWith(SpringRunner.class)
//@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
//@TestPropertySource(properties = {"management.port=0"})
@WebMvcTest(MagicSquareController.class)
public class ManagementEndpointsTests {

    /*@Value("${local.management.port}")
    private int mgt;

    @Autowired
    private TestRestTemplate testRestTemplate;*/

    @Autowired
    private MockMvc mockMvc;

    final String url_root = "localhost:9001/actuator";

    /*@Test
    public void shouldReturn200WhenSendingRequestToManagementEndpoint() throws Exception {
        @SuppressWarnings("rawtypes")
        ResponseEntity<Map> entity = this.testRestTemplate.getForEntity(
                "http://localhost:" + this.mgt + "/actuator/info", Map.class);

        then(entity.getStatusCode()).isEqualTo(HttpStatus.OK);
    }*/

    @Test
    public void shouldReturn200UsingMockMVC() throws Exception {
        this.mockMvc.perform(get(url_root))
                .andExpect(status().isOk());
    }

    @Test
    public void healthTest() throws  Exception {
        this.mockMvc.perform(get(url_root + "/health"))
                .andDo(print());
    }

}
