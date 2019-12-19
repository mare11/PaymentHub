package org.sep.scholar.controller;

import org.springframework.web.bind.annotation.GetMapping;

@org.springframework.stereotype.Controller
public class TestController {

    @GetMapping
    public String test() {
        return "test";
    }
}
