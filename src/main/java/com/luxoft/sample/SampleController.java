package com.luxoft.sample;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
public class SampleController {

    private static Logger log = LoggerFactory.getLogger(SampleController.class);
    
    @RequestMapping("/")
    public String index() {
        log.debug("hello");
        return "hello";
    }

}