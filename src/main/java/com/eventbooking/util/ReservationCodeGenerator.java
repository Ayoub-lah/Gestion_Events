package com.eventbooking.util;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;

@Component
public class ReservationCodeGenerator {

    private Random random = new Random();

    public String generateCode() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyMMddHHmm"));
        String randomDigits = String.format("%03d", random.nextInt(1000));
        return "RES-" + timestamp + "-" + randomDigits;
    }
}