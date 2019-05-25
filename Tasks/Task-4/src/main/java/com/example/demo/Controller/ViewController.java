package com.example.demo.Controller;

import com.alibaba.fastjson.JSONObject;
import com.example.demo.Model.Container;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping(value = "/api")
public class ViewController {

    @ResponseBody
    @RequestMapping(value = "/view")
    public JSONObject view() {
        JSONObject resp = new JSONObject();

        resp.put("container", Container.getListOfValue());

        return resp;
    }

}
