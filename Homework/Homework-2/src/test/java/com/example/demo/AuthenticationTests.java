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
public class AuthenticationTests {

    final Base64.Encoder encoder = Base64.getEncoder();

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void authSuccessTest() throws Exception {
        final String username = "auth";
        final String password = "password";
        String info = "Basic " + encoder.encodeToString((username + ":" + password).getBytes());
        this.mockMvc.perform(get("/magicSquare?order=3").header("Authorization", info))
                .andDo(print())
                .andExpect(jsonPath("$.message").value("Generate magic square successfully!"));
    }

    @Test
    public void authFailTest() throws Exception {
        final String username = "user";
        final String password = "password";
        String info = "Basic " + encoder.encodeToString((username + ":" + password).getBytes());
        this.mockMvc.perform(get("/magicSquare?order=3").header("Authorization", info))
                .andDo(print())
                .andExpect(jsonPath("$.message").value("Authentication failed!"));
    }

    @Test
    public void nonAuthTest() throws Exception {
        this.mockMvc.perform(get("/magicSquare?order=3"))
                .andDo(print())
                .andExpect(jsonPath("$.message").value("Authentication failed!"));
    }

}
