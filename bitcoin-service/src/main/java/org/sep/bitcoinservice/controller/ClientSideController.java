package org.sep.bitcoinservice.controller;

import org.sep.bitcoinservice.service.BitcoinService;
import org.sep.paymentgatewayservice.method.api.PaymentCompleteRequest;
import org.sep.paymentgatewayservice.method.api.PaymentStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class ClientSideController {

    private final BitcoinService bitcoinService;

    @Autowired
    public ClientSideController(final BitcoinService bitcoinService) {
        this.bitcoinService = bitcoinService;
    }

    @GetMapping(value = "/registration")
    public String registration(@RequestParam("merchantId") final String merchantId, final Model model) {
        model.addAttribute("merchantId", merchantId);
        return "registration";
    }

    @GetMapping(value = "/success_payment")
    public String successPayment(@RequestParam("orderId") final String orderId, final Model model) {
        model.addAttribute("returnUrl", this.completePayment(orderId, PaymentStatus.SUCCESS));
        model.addAttribute("message", "Your payment is completed successfully!");
        return "payment_completed";
    }

    @GetMapping(value = "/cancel_payment")
    public String cancelPayment(@RequestParam("orderId") final String orderId, final Model model) {
        model.addAttribute("returnUrl", this.completePayment(orderId, PaymentStatus.CANCEL));
        model.addAttribute("message", "Your payment is canceled!");
        return "payment_completed";
    }

    private String completePayment(final String orderId, final PaymentStatus paymentStatus) {
        final PaymentCompleteRequest paymentCompleteRequest = PaymentCompleteRequest.builder()
                .orderId(orderId)
                .status(paymentStatus)
                .build();
        return this.bitcoinService.completePayment(paymentCompleteRequest);
    }
}
