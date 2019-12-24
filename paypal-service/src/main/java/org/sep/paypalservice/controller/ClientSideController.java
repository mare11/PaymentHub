package org.sep.paypalservice.controller;

import org.sep.paymentgatewayservice.methodapi.PaymentCompleteRequest;
import org.sep.paymentgatewayservice.methodapi.PaymentStatus;
import org.sep.paypalservice.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class ClientSideController {

    private final PaymentService paymentService;

    @Autowired
    public ClientSideController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @GetMapping(value = "/success_payment")
    public String successPayment(@RequestParam("token") String orderId, Model model) {
        model.addAttribute("returnUrl", this.completePayment(orderId, PaymentStatus.SUCCESS));
        model.addAttribute("message", "Your payment is completed successfully!");
        return "payment_completed";
    }

    @GetMapping(value = "/cancel_payment")
    public String cancelPayment(@RequestParam("token") String orderId, Model model) {
        model.addAttribute("returnUrl", this.completePayment(orderId, PaymentStatus.CANCEL));
        model.addAttribute("message", "Your payment is canceled!");
        return "payment_completed";
    }

    private String completePayment(String orderId, PaymentStatus paymentStatus) {
        PaymentCompleteRequest paymentCompleteRequest = PaymentCompleteRequest.builder()
                .orderId(orderId)
                .status(paymentStatus)
                .build();
        return this.paymentService.completePayment(paymentCompleteRequest);
    }
}