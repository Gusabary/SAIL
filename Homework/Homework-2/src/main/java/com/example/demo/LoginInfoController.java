package com.example.demo;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class LoginInfoController {

    @RequestMapping(value = "/login", method = RequestMethod.POST)
    public String loginInfo(@RequestParam(name = "username", defaultValue = "user") String username){
        return username;
    }

    @RequestMapping(value = "/login", method = RequestMethod.GET)
    public String loginInfo2(@RequestParam(name = "username", defaultValue = "user") String username){
        return username + "|2";
    }
}

