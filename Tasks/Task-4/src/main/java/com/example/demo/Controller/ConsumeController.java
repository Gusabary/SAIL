package com.example.demo.Controller;

import com.alibaba.fastjson.JSONObject;
import com.example.demo.Model.Container;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping(value = "/api")
public class ConsumeController {

    @ResponseBody
    @RequestMapping(value = "/consume")
    public JSONObject consume() {
        JSONObject resp = new JSONObject();

        if (Container.isEmpty()) {
            resp.put("message", "container is empty now");
        }
        else {
            resp.put("message", "an item consumed");
            resp.put("container", Container.consume());
        }

        return resp;
    }

}
