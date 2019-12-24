package org.sep.scholar.controller;

import org.springframework.web.bind.annotation.GetMapping;

@org.springframework.stereotype.Controller
public class Controller {

    @GetMapping()
    public String index() {
        return "index";
    }

    @GetMapping(value = "/registration")
    public String testRegistration() {
        return "test_registration";
    }

    @GetMapping(value = "/payment")
    public String testPayment() {
        return "test_payment";
    }
}