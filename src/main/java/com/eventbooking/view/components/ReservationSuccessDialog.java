package com.eventbooking.view.components;

import com.eventbooking.entity.Reservation;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;

public class ReservationSuccessDialog extends Dialog {

    public ReservationSuccessDialog(Reservation reservation) {
        setCloseOnOutsideClick(false);
        setWidth("500px");
        setMaxWidth("90vw");

        VerticalLayout content = new VerticalLayout();
        content.setSpacing(true);
        content.setPadding(true);
        content.setAlignItems(FlexComponent.Alignment.CENTER);

        // Icon de succès
        Icon successIcon = VaadinIcon.CHECK_CIRCLE.create();
        successIcon.setSize("64px");
        successIcon.getStyle().set("color", "#38a169");

        // Titre
        H3 title = new H3("Réservation confirmée!");
        title.getStyle()
                .set("color", "#38a169")
                .set("margin", "0");

        // Message
        Paragraph message = new Paragraph(
                "Votre réservation a été enregistrée avec succès. " +
                        "Vous recevrez un email de confirmation."
        );
        message.getStyle()
                .set("text-align", "center")
                .set("color", "#666");

        // Détails de la réservation
        Div details = new Div();
        details.getStyle()
                .set("background", "#f8f9fa")
                .set("border-radius", "8px")
                .set("padding", "16px")
                .set("width", "100%");

        VerticalLayout detailsContent = new VerticalLayout();
        detailsContent.setSpacing(true);
        detailsContent.setPadding(false);

        // Code de réservation
        HorizontalLayout codeRow = createDetailRow("Code réservation:", reservation.getCodeReservation());
        codeRow.getStyle().set("font-weight", "bold");

        // Événement
        HorizontalLayout eventRow = createDetailRow("Événement:", reservation.getEvenement().getTitre());

        // Date
        HorizontalLayout dateRow = createDetailRow("Date:",
                reservation.getEvenement().getDateDebut().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));

        // Nombre de places
        HorizontalLayout placesRow = createDetailRow("Places:", reservation.getNombrePlaces().toString());

        // Montant total
        HorizontalLayout amountRow = createDetailRow("Montant total:", reservation.getMontantTotal() + " MAD");

        detailsContent.add(codeRow, eventRow, dateRow, placesRow, amountRow);
        details.add(detailsContent);

        // Boutons
        HorizontalLayout buttons = new HorizontalLayout();
        buttons.setSpacing(true);
        buttons.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);

        // Bouton pour copier le code
        Button copyButton = new Button("Copier le code", VaadinIcon.COPY.create());
        copyButton.addThemeVariants(ButtonVariant.LUMO_CONTRAST);
        copyButton.addClickListener(e -> {
            getElement().executeJs("""
                navigator.clipboard.writeText($0).then(() => {
                    $0 = "Copié!";
                    setTimeout(() => $0 = "Copier le code", 2000);
                });
            """, reservation.getCodeReservation());
        });

        // Bouton pour voir mes réservations
        Button myReservationsButton = new Button("Mes réservations", VaadinIcon.LIST_UL.create());
        myReservationsButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        myReservationsButton.addClickListener(e -> {
            close();
            getUI().ifPresent(ui -> ui.navigate("client/reservations"));
        });

        // Bouton Fermer
        Button closeButton = new Button("Fermer", VaadinIcon.CHECK.create());
        closeButton.addThemeVariants(ButtonVariant.LUMO_SUCCESS);
        closeButton.addClickListener(e -> close());

        buttons.add(copyButton, myReservationsButton, closeButton);

        content.add(successIcon, title, message, details, buttons);
        add(content);
    }

    private HorizontalLayout createDetailRow(String label, String value) {
        HorizontalLayout row = new HorizontalLayout();
        row.setSpacing(true);
        row.setWidthFull();
        row.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);

        Div labelDiv = new Div();
        labelDiv.setText(label);
        labelDiv.getStyle()
                .set("font-weight", "500")
                .set("color", "#666");

        Div valueDiv = new Div();
        valueDiv.setText(value);
        valueDiv.getStyle()
                .set("font-weight", "600")
                .set("color", "#333");

        row.add(labelDiv, valueDiv);
        return row;
    }
}