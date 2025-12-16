package com.eventbooking.controller;

import com.eventbooking.entity.Reservation;
import com.eventbooking.entity.User;
import com.eventbooking.service.ReservationService;
import com.vaadin.flow.server.VaadinSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;

@RestController
public class ReceiptController {

    @Autowired
    private ReservationService reservationService;

    @GetMapping("/api/reservations/{id}/receipt/debug")
    public ResponseEntity<String> debugReceipt(@PathVariable Long id) {
        try {
            System.out.println("=== DEBUG RECEIPT START ===");
            System.out.println("Requested receipt for ID: " + id);

            // Vérifier l'authentification
            Object userObj = VaadinSession.getCurrent().getAttribute("currentUser");
            System.out.println("User from session: " + userObj);

            if (!(userObj instanceof User)) {
                System.out.println("User not authenticated");
                return ResponseEntity.status(403).body("User not authenticated");
            }

            User currentUser = (User) userObj;
            System.out.println("Current user ID: " + currentUser.getId());
            System.out.println("Current user email: " + currentUser.getEmail());

            var reservationOpt = reservationService.getReservationById(id);
            System.out.println("Reservation found: " + reservationOpt.isPresent());

            if (reservationOpt.isEmpty()) {
                System.out.println("Reservation not found for ID: " + id);
                return ResponseEntity.status(404).body("Reservation not found");
            }

            Reservation reservation = reservationOpt.get();
            System.out.println("Reservation ID: " + reservation.getId());
            System.out.println("Reservation code: " + reservation.getCodeReservation());
            System.out.println("Reservation user ID: " + (reservation.getUtilisateur() != null ? reservation.getUtilisateur().getId() : "null"));

            // Vérifier que l'utilisateur est propriétaire
            boolean isOwner = reservation.getUtilisateur() != null &&
                    reservation.getUtilisateur().getId().equals(currentUser.getId());
            System.out.println("Is owner: " + isOwner);

            if (!isOwner) {
                System.out.println("User is not owner of reservation");
                return ResponseEntity.status(403).body("Access denied");
            }

            // Simple HTML pour test
            String simpleHtml = """
                <!DOCTYPE html>
                <html>
                <head><title>Test Receipt</title></head>
                <body>
                    <h1>Test Receipt Works!</h1>
                    <p>Reservation Code: %s</p>
                    <p>User: %s</p>
                    <p>Date: %s</p>
                </body>
                </html>
                """.formatted(
                    reservation.getCodeReservation(),
                    currentUser.getEmail(),
                    java.time.LocalDateTime.now().toString()
            );

            System.out.println("=== DEBUG RECEIPT END ===");

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_TYPE, "text/html; charset=UTF-8")
                    .body(simpleHtml);

        } catch (Exception e) {
            System.err.println("ERROR in debugReceipt: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError()
                    .body("Error: " + e.getMessage() + "\n" + e.getClass().getName());
        }
    }
}