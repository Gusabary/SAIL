package com.example.demo;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import java.util.Base64;

@RunWith(SpringRunner.class)
@WebMvcTest(MagicSquareController.class)
public class HTTPTests {

    final Base64.Encoder encoder = Base64.getEncoder();
    final String username = "auth";
    final String password = "password";
    String info = "Basic " + encoder.encodeToString((username + ":" + password).getBytes());

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void testOddOrder1() throws Exception {
        this.mockMvc.perform(get("/magicSquare?order=1").header("Authorization", info))
                .andDo(print())
                .andExpect(jsonPath("$.message").value("Generate magic square successfully!"))
                .andExpect(jsonPath("$.square.length()").value(1))
                .andExpect(jsonPath("$.square[0].length()").value(1));
    }

    @Test
    public void testOddOrder2() throws Exception {
        this.mockMvc.perform(get("/magicSquare?order=5").header("Authorization", info))
                .andDo(print())
                .andExpect(jsonPath("$.message").value("Generate magic square successfully!"))
                .andExpect(jsonPath("$.square.length()").value(5))
                .andExpect(jsonPath("$.square[4].length()").value(5));
    }

    @Test
    public void testEvenOrder() throws Exception {
        this.mockMvc.perform(get("/magicSquare?order=4").header("Authorization", info))
                .andDo(print())
                .andExpect(jsonPath("$.message").value("Order should be odd!"))
                .andExpect(jsonPath("$.square.length()").isEmpty());
    }

    @Test
    public void testNegativeOrder() throws Exception {
        this.mockMvc.perform(get("/magicSquare?order=-3").header("Authorization", info))
                .andDo(print())
                .andExpect(jsonPath("$.message").value("Order should be positive!"))
                .andExpect(jsonPath("$.square.length()").isEmpty());
    }

    @Test
    public void testZeroOrder() throws Exception {
        this.mockMvc.perform(get("/magicSquare?order=0").header("Authorization", info))
                .andDo(print())
                .andExpect(jsonPath("$.message").value("Order should be positive!"))
                .andExpect(jsonPath("$.square.length()").isEmpty());
    }
}
