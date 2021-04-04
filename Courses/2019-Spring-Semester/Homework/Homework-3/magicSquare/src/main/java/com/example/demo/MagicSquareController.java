package com.example.demo;
import org.springframework.web.bind.annotation.*;

import java.util.Base64;

@RestController
public class MagicSquareController {

    final Base64.Decoder decoder = Base64.getDecoder();

    @RequestMapping(value = "/")
    @ResponseBody
    public MagicSquare magicSquare(@RequestHeader(name = "loginInfo", defaultValue = "") String loginInfo,
                                   @RequestParam(name = "order", defaultValue = "3") int order){
        if (loginInfo.equals(""))
            return new MagicSquare(0,false);
        String[] infos = new String(decoder.decode(loginInfo)).split(":");
        String username = infos[0];
        String password = infos[1];
        if (username.equals("auth") && password.equals("password"))
            return new MagicSquare(order, true);
        return new MagicSquare(0,false);
    }

}

