package com.example.demo.Controller;

import com.alibaba.fastjson.JSONObject;
import com.example.demo.Model.Container;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping(value = "/api")
public class ProduceController {

    @ResponseBody
    @RequestMapping(value = "/produce")
    public JSONObject produce() {
        JSONObject resp = new JSONObject();

        resp.put("container", Container.produce());
        resp.put("message", "a new item produced");

        return resp;
    }

}
