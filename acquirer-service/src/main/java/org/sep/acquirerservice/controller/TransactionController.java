package org.sep.acquirerservice.controller;

import org.sep.acquirerservice.model.CardDto;
import org.sep.acquirerservice.model.TransactionResponse;
import org.sep.acquirerservice.service.AcquirerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/transaction")
public class TransactionController {

    private final AcquirerService acquirerService;

    @Autowired
    public TransactionController(AcquirerService acquirerService) {
        this.acquirerService = acquirerService;
    }

    @GetMapping(value = "/{id}")
    public String getTransaction(@PathVariable String id, Model model) {
        model.addAttribute("transaction", acquirerService.getTransactionById(id));
        return "template";
    }

    @ResponseBody
    @PostMapping(value = "/execute/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public TransactionResponse executeTransaction(@PathVariable String id, @RequestBody CardDto cardDto) {
        return acquirerService.executeTransaction(id, cardDto);
    }


}
