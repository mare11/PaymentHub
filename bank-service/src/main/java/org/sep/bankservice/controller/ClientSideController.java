package org.sep.bankservice.controller;

import org.sep.bankservice.model.Merchant;
import org.sep.bankservice.service.BankService;
import org.sep.paymentgatewayservice.method.api.PaymentCompleteRequest;
import org.sep.paymentgatewayservice.method.api.PaymentStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class ClientSideController {

    private final BankService bankService;

    @Autowired
    public ClientSideController(final BankService bankService) {
        this.bankService = bankService;
    }

    @GetMapping(value = "/registration")
    public String registration(@RequestParam("merchantId") final String merchantId, final Model model) {
        model.addAttribute("merchantId", merchantId);
        return "registration";
    }

    @ResponseBody
    @PostMapping(value = "/register_merchant", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> registerMerchant(@RequestBody final Merchant merchant) {
        return ResponseEntity.ok(this.bankService.registerMerchant(merchant));
    }

    @GetMapping(value = "/success_payment")
    public String successPayment(@RequestParam("orderId") final String orderId, final Model model) {
        model.addAttribute("returnUrl", this.bankService.completePayment(new PaymentCompleteRequest(orderId, PaymentStatus.SUCCESS)));
        model.addAttribute("message", "Your payment is completed successfully!");
        return "payment_completed";
    }

    @GetMapping(value = "/cancel_payment")
    public String cancelPayment(@RequestParam("orderId") final String orderId, final Model model) {
        model.addAttribute("returnUrl", this.bankService.completePayment(new PaymentCompleteRequest(orderId, PaymentStatus.CANCEL)));
        model.addAttribute("message", "Your payment is canceled!");
        return "payment_completed";
    }
}
