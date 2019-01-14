package com.trace.server.trace.controller;

import com.trace.server.kafka.KafkaProducer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DemoController {

    @Autowired
    KafkaProducer kafkaProducer;

    @RequestMapping(value = "/send")
    public String send(String name) {
        kafkaProducer.sendChannelMess("test", name);
        return "hellow";
    }

}
