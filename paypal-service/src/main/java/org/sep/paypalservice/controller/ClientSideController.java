package org.sep.paypalservice.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ClientSideController {

    @GetMapping(value = "/success_payment")
    public String testRegistration() {
        return "success_payment";
    }

    @GetMapping(value = "/cancel_payment")
    public String testPayment() {
        return "cancel_payment";
    }
}