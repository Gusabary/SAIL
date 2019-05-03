package com.example.demo;

import com.alibaba.fastjson.JSONObject;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Base64;

@RestController
public class LoginController {

    final Base64.Encoder encoder = Base64.getEncoder();

    @RequestMapping(value = "/")
    @ResponseBody
    public String login(@RequestBody JSONObject request) {
        String username = request.getString("username");
        String password = request.getString("password");
        String toEncode = username + ":" + password;
        return encoder.encodeToString(toEncode.getBytes());
    }
}
