package org.sep.sellerservice.controller;

import org.sep.sellerservice.service.PaymentMethodService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class ChooseMethodController {

    private final PaymentMethodService paymentDataService;

    @Autowired
    public ChooseMethodController(PaymentMethodService paymentDataService) {
        this.paymentDataService = paymentDataService;
    }

    @GetMapping(value = "/choose_method")
    public String chooseMethod(@RequestParam(value = "id") Long id, Model model) {
        model.addAttribute("seller", id);
        model.addAttribute("paymentMethods", this.paymentDataService.findAll());
        return "choose_methods";
    }
}