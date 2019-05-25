package com.example.demo;

import com.example.demo.Model.Container;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.Date;

@SpringBootApplication
public class DemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);

        while (true) {
            try {
                System.out.println(new Date());
                Container.check();
                Thread.sleep(2 * 1000);  //check timeout every 2 seconds
            }
            catch (InterruptedException e) {
                System.out.println("exception here");
            }
        }
    }

}
