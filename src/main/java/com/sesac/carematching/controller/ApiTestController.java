package com.sesac.carematching.controller;

import java.util.Date;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class ApiTestController {

    @GetMapping("/test")
    public String hello() {
        return "안녕하세요. 현재 서버시간은 " + new Date() + "입니다. \n";
    }
}
