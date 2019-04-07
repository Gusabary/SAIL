package com.example.demo;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MagicSquareController {

    @RequestMapping("/magicSquare")
    public MagicSquare magicSquare(@RequestParam(value="order") int order){
        return new MagicSquare(order);
    }
}

