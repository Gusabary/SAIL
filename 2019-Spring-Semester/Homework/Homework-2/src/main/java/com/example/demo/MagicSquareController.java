package com.example.demo;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Base64;

@RestController
public class MagicSquareController {

    final Base64.Decoder decoder = Base64.getDecoder();

    @RequestMapping("/magicSquare")
    public MagicSquare magicSquare(@RequestHeader(name = "Authorization", defaultValue = "") String auth,
                                   @RequestParam(name = "order", defaultValue = "3") int order){
        if (auth.equals(""))
            return new MagicSquare(0,false);
        String[] infos = new String(decoder.decode(auth.substring(6))).split(":");
        String username = infos[0];
        String password = infos[1];
        if (username.equals("auth") && password.equals("password"))
            return new MagicSquare(order, true);
        return new MagicSquare(0,false);
    }

}

