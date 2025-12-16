package com.eventbooking.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.ResponseEntity;

@RestController
public class TestReceiptController {

    @GetMapping("/api/test/receipt")
    public ResponseEntity<String> testReceipt() {
        return ResponseEntity.ok("Test endpoint works!");
    }

    @GetMapping("/api/test/receipt/{id}")
    public ResponseEntity<String> testReceiptById(@PathVariable Long id) {
        return ResponseEntity.ok("Test endpoint works for ID: " + id);
    }
}