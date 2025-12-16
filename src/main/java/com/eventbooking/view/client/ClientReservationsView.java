package com.eventbooking.view.client;

import com.eventbooking.entity.Reservation;
import com.eventbooking.entity.User;
import com.eventbooking.service.ReservationService;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Route(value = "client/reservations", layout = ClientMainLayout.class)
@PageTitle("Mes Réservations - EventBooking")
public class ClientReservationsView extends VerticalLayout {

    private final ReservationService reservationService;
    private User currentUser;
    private Grid<Reservation> reservationGrid;

    @Autowired
    public ClientReservationsView(ReservationService reservationService) {
        this.reservationService = reservationService;

        // Récupérer l'utilisateur connecté
        Object userObj = VaadinSession.getCurrent().getAttribute("currentUser");
        this.currentUser = (userObj instanceof User) ? (User) userObj : null;

        if (currentUser == null) {
            UI.getCurrent().navigate("login");
            return;
        }

        injectReceiptStyles(); // Ajouter cette ligne
        initView();
        loadReservations();
    }

    private void initView() {
        setSpacing(false);
        setPadding(true);
        setWidthFull();
        getStyle().set("background", "#f8f9fa");

        // Header
        HorizontalLayout header = new HorizontalLayout();
        header.setWidthFull();
        header.setAlignItems(Alignment.CENTER);
        header.setJustifyContentMode(JustifyContentMode.BETWEEN);

        HorizontalLayout titleLayout = new HorizontalLayout();
        titleLayout.setAlignItems(Alignment.CENTER);
        titleLayout.setSpacing(true);

        Icon ticketIcon = VaadinIcon.TICKET.create();
        ticketIcon.setSize("32px");
        ticketIcon.getStyle().set("color", "#667eea");

        H2 title = new H2("Mes Réservations");
        title.getStyle()
                .set("margin", "0")
                .set("color", "#333");

        titleLayout.add(ticketIcon, title);

        // Bouton de recherche
        HorizontalLayout searchLayout = new HorizontalLayout();
        searchLayout.setSpacing(true);
        searchLayout.setAlignItems(Alignment.CENTER);

        TextField searchField = new TextField();
        searchField.setPlaceholder("Rechercher par code ou événement...");
        searchField.setPrefixComponent(VaadinIcon.SEARCH.create());
        searchField.setWidth("300px");

        Button searchBtn = new Button("Rechercher", VaadinIcon.SEARCH.create());
        searchBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        searchBtn.addClickListener(e -> searchReservations(searchField.getValue()));

        Button refreshBtn = new Button(VaadinIcon.REFRESH.create());
        refreshBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        refreshBtn.addClickListener(e -> {
            searchField.clear();
            loadReservations();
        });

        searchLayout.add(searchField, searchBtn, refreshBtn);

        header.add(titleLayout, searchLayout);

        // Statistiques
        HorizontalLayout stats = createStatsSection();

        // Grille des réservations
        reservationGrid = createReservationGrid();

        add(header, stats, reservationGrid);
    }

    private HorizontalLayout createStatsSection() {
        HorizontalLayout stats = new HorizontalLayout();
        stats.setSpacing(true);
        stats.setWidthFull();

        long totalReservations = reservationService.getUserReservationsCount(currentUser.getId());
        long confirmedReservations = reservationService.getUserConfirmedReservationsCount(currentUser.getId());

        stats.add(
                createStatCard(VaadinIcon.TICKET, "Total", String.valueOf(totalReservations), "Réservations"),
                createStatCard(VaadinIcon.CHECK, "Confirmées", String.valueOf(confirmedReservations), "Réservations"),
                createStatCard(VaadinIcon.EURO, "Dépensé",
                        reservationService.getOrganizerRevenue(currentUser.getId()) + " MAD", "Total")
        );

        return stats;
    }

    private VerticalLayout createStatCard(VaadinIcon icon, String title, String value, String subtitle) {
        VerticalLayout card = new VerticalLayout();
        card.setSpacing(false);
        card.setPadding(true);
        card.setAlignItems(Alignment.CENTER);
        card.getStyle()
                .set("background", "white")
                .set("border-radius", "12px")
                .set("box-shadow", "0 2px 8px rgba(0,0,0,0.1)")
                .set("flex", "1");

        Icon cardIcon = icon.create();
        cardIcon.setSize("24px");
        cardIcon.getStyle().set("color", "#667eea");

        H3 cardValue = new H3(value);
        cardValue.getStyle()
                .set("margin", "10px 0 5px 0")
                .set("color", "#333");

        H4 cardTitle = new H4(title);
        cardTitle.getStyle()
                .set("margin", "0 0 5px 0")
                .set("color", "#667eea");

        Span cardSubtitle = new Span(subtitle);
        cardSubtitle.getStyle()
                .set("color", "#666")
                .set("font-size", "12px");

        card.add(cardIcon, cardTitle, cardValue, cardSubtitle);
        return card;
    }

    private Grid<Reservation> createReservationGrid() {
        Grid<Reservation> grid = new Grid<>(Reservation.class, false);
        grid.setWidthFull();
        grid.setHeight("500px");
        grid.getStyle()
                .set("background", "white")
                .set("border-radius", "12px")
                .set("margin-top", "20px");

        grid.addColumn(reservation -> reservation.getEvenement().getTitre())
                .setHeader("Événement")
                .setAutoWidth(true)
                .setSortable(true);

        grid.addColumn(reservation -> reservation.getEvenement().getDateDebut()
                        .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")))
                .setHeader("Date")
                .setAutoWidth(true)
                .setSortable(true);

        grid.addColumn(Reservation::getNombrePlaces)
                .setHeader("Places")
                .setAutoWidth(true)
                .setSortable(true);

        grid.addColumn(reservation -> reservation.getMontantTotal() + " MAD")
                .setHeader("Montant")
                .setAutoWidth(true)
                .setSortable(true);

        grid.addColumn(Reservation::getCodeReservation)
                .setHeader("Code")
                .setAutoWidth(true);

        grid.addColumn(reservation -> {
            String status = reservation.getStatut().toString();
            String color = switch (status) {
                case "CONFIRMEE" -> "#38a169";
                case "EN_ATTENTE" -> "#ed8936";
                case "ANNULEE" -> "#e53e3e";
                default -> "#666";
            };

            Span badge = new Span(status);
            badge.getStyle()
                    .set("background", color)
                    .set("color", "white")
                    .set("padding", "4px 8px")
                    .set("border-radius", "12px")
                    .set("font-size", "12px");

            return badge;
        }).setHeader("Statut").setAutoWidth(true);

        grid.addComponentColumn(reservation -> {
            HorizontalLayout actions = new HorizontalLayout();
            actions.setSpacing(true);

            Button detailsBtn = new Button("Détails", VaadinIcon.EYE.create());
            detailsBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL);
            detailsBtn.addClickListener(e -> showReservationDetails(reservation));

            Button receiptBtn = new Button("Reçu", VaadinIcon.PRINT.create());
            receiptBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SMALL);
            receiptBtn.addClickListener(e -> printReceipt(reservation));

            // Optionnel : Bouton pour télécharger
            Button downloadBtn = new Button("Télécharger", VaadinIcon.DOWNLOAD.create());
            downloadBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL);
            downloadBtn.addClickListener(e -> {
                getUI().ifPresent(ui -> {
                    String url = "/api/reservations/" + reservation.getId() + "/receipt/download";
                    ui.getPage().executeJs("window.open($0, '_blank')", url);
                });
            });

            if (reservation.getStatut().toString().equals("EN_ATTENTE")) {
                Button cancelBtn = new Button("Annuler", VaadinIcon.CLOSE.create());
                cancelBtn.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_SMALL);
                cancelBtn.addClickListener(e -> cancelReservation(reservation));
                actions.add(cancelBtn);
            }

            actions.add(detailsBtn, receiptBtn, downloadBtn);
            return actions;
        }).setHeader("Actions").setAutoWidth(true);

        return grid;
    }

    private void loadReservations() {
        List<Reservation> reservations = reservationService.getReservationsByUser(currentUser.getId());
        reservationGrid.setItems(reservations);
    }

    private void searchReservations(String keyword) {
        List<Reservation> allReservations = reservationService.getReservationsByUser(currentUser.getId());

        if (keyword == null || keyword.trim().isEmpty()) {
            reservationGrid.setItems(allReservations);
            return;
        }

        List<Reservation> filtered = allReservations.stream()
                .filter(r -> r.getCodeReservation().toLowerCase().contains(keyword.toLowerCase()) ||
                        r.getEvenement().getTitre().toLowerCase().contains(keyword.toLowerCase()))
                .toList();

        reservationGrid.setItems(filtered);
    }

    private void showReservationDetails(Reservation reservation) {
        Dialog dialog = new Dialog();
        dialog.setWidth("600px");
        dialog.setMaxWidth("90vw");

        VerticalLayout content = new VerticalLayout();
        content.setSpacing(true);
        content.setPadding(true);

        // Header
        HorizontalLayout header = new HorizontalLayout();
        header.setWidthFull();
        header.setJustifyContentMode(JustifyContentMode.BETWEEN);
        header.setAlignItems(Alignment.CENTER);

        H3 title = new H3("Détails de la réservation");
        title.getStyle().set("margin", "0").set("color", "#667eea");

        Button closeBtn = new Button(VaadinIcon.CLOSE.create());
        closeBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        closeBtn.addClickListener(e -> dialog.close());

        header.add(title, closeBtn);

        // Contenu
        VerticalLayout details = new VerticalLayout();
        details.setSpacing(true);
        details.setPadding(true);
        details.getStyle()
                .set("background", "#f8f9fa")
                .set("border-radius", "8px");

        details.add(
                createDetailRow("Code:", reservation.getCodeReservation()),
                createDetailRow("Événement:", reservation.getEvenement().getTitre()),
                createDetailRow("Date:", reservation.getEvenement().getDateDebut()
                        .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))),
                createDetailRow("Lieu:", reservation.getEvenement().getLieu() + ", " + reservation.getEvenement().getVille()),
                createDetailRow("Nombre de places:", reservation.getNombrePlaces().toString()),
                createDetailRow("Montant total:", reservation.getMontantTotal() + " MAD"),
                createDetailRow("Statut:", reservation.getStatut().toString()),
                createDetailRow("Date réservation:", reservation.getDateReservation()
                        .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))),
                createDetailRow("Commentaire:",
                        reservation.getCommentaire() != null ? reservation.getCommentaire() : "Aucun")
        );

        content.add(header, details);
        dialog.add(content);
        dialog.open();
    }

    private HorizontalLayout createDetailRow(String label, String value) {
        HorizontalLayout row = new HorizontalLayout();
        row.setWidthFull();
        row.setJustifyContentMode(JustifyContentMode.BETWEEN);
        row.setAlignItems(Alignment.CENTER);

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

    private void printReceipt(Reservation reservation) {
        // Naviguer vers la vue reçu
        getUI().ifPresent(ui -> {
            ui.navigate("receipt/" + reservation.getId());
        });
    }

    private Div createReceiptPreview(Reservation reservation) {
        Div preview = new Div();
        preview.getElement().setProperty("innerHTML", generateReceiptHtml(reservation));
        return preview;
    }

    private String generateReceiptHtml(Reservation reservation) {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        return """
        <!DOCTYPE html>
        <html>
        <head>
            <meta charset='UTF-8'>
            <title>Reçu de Réservation - %s</title>
            <style>
                @media print {
                    .no-print { display: none !important; }
                    body { margin: 0; padding: 0; }
                }
                body { font-family: Arial, sans-serif; margin: 40px; }
                .receipt { max-width: 800px; margin: 0 auto; }
                .header { text-align: center; border-bottom: 2px solid #667eea; padding-bottom: 20px; margin-bottom: 30px; }
                .header h1 { color: #667eea; margin: 0; }
                .section { margin-bottom: 30px; }
                .section-title { color: #667eea; border-bottom: 1px solid #ddd; padding-bottom: 5px; margin-bottom: 15px; }
                table { width: 100%%; border-collapse: collapse; margin-bottom: 20px; }
                th { background: #f8f9fa; padding: 10px; text-align: left; font-weight: bold; border: 1px solid #ddd; }
                td { padding: 10px; border: 1px solid #ddd; }
                .total-row { background: #667eea; color: white; font-weight: bold; }
                .footer { text-align: center; margin-top: 50px; padding-top: 20px; border-top: 2px solid #667eea; }
                .print-button { background: #667eea; color: white; padding: 10px 20px; border: none; border-radius: 5px; cursor: pointer; margin: 20px 0; }
            </style>
        </head>
        <body>
            <div class='receipt'>
                <div class='header'>
                    <h1>REÇU DE RÉSERVATION</h1>
                    <p>EventBooking</p>
                    <p>123 Avenue Mohammed V, Casablanca<br>Tél: +212 5 22 00 00 00<br>Email: contact@eventbooking.ma</p>
                </div>
                
                <div class='section'>
                    <h3 class='section-title'>INFORMATIONS DE LA RÉSERVATION</h3>
                    <table>
                        <tr><th>Code de réservation:</th><td>%s</td></tr>
                        <tr><th>Date de réservation:</th><td>%s</td></tr>
                    </table>
                </div>
                
                <div class='section'>
                    <h3 class='section-title'>DÉTAILS DE L'ÉVÉNEMENT</h3>
                    <table>
                        <tr><th>Événement:</th><td>%s</td></tr>
                        <tr><th>Date:</th><td>%s</td></tr>
                        <tr><th>Lieu:</th><td>%s%s%s</td></tr>
                        <tr><th>Catégorie:</th><td>%s</td></tr>
                    </table>
                </div>
                
                <div class='section'>
                    <h3 class='section-title'>DÉTAILS DE PAIEMENT</h3>
                    <table>
                        <tr><th>Nombre de places:</th><td>%d</td></tr>
                        <tr><th>Prix unitaire:</th><td>%.2f MAD</td></tr>
                        <tr class='total-row'><th>TOTAL:</th><td style='text-align: right;'>%.2f MAD</td></tr>
                    </table>
                </div>
                
                <div class='section'>
                    <h3 class='section-title'>INFORMATIONS DU CLIENT</h3>
                    <table>
                        <tr><th>Nom complet:</th><td>%s %s</td></tr>
                        <tr><th>Email:</th><td>%s</td></tr>
                        <tr><th>Téléphone:</th><td>%s</td></tr>
                    </table>
                </div>
                
                <div class='section'>
                    <h3 class='section-title'>CONDITIONS ET REMARQUES</h3>
                    <ul>
                        <li>Ce reçu est valable comme justificatif de paiement.</li>
                        <li>Présentez ce reçu à l'entrée de l'événement.</li>
                        <li>Les réservations ne sont pas remboursables.</li>
                        <li>En cas d'annulation, veuillez contacter notre service client.</li>
                    </ul>
                    %s
                </div>
                
                <div class='footer'>
                    <button class='print-button no-print' onclick='window.print()'>Imprimer le reçu</button>
                    <p>Merci pour votre confiance !<br>EventBooking - Votre plateforme d'événements préférée</p>
                    <p style='color: #999; font-size: 11px;'>
                        Document généré le %s
                    </p>
                </div>
            </div>
        </body>
        </html>
        """.formatted(
                reservation.getCodeReservation(), // %s - title
                reservation.getCodeReservation(), // %s - code
                reservation.getDateReservation().format(dateFormatter), // %s - date
                reservation.getEvenement().getTitre(), // %s - event title
                reservation.getEvenement().getDateDebut().format(dateFormatter), // %s - event date
                reservation.getEvenement().getLieu() != null ? reservation.getEvenement().getLieu() + ", " : "", // %s - lieu part 1
                reservation.getEvenement().getVille(), // %s - ville
                reservation.getEvenement().getCategorie(), // %s - category
                reservation.getNombrePlaces(), // %d - places
                reservation.getMontantTotal() / reservation.getNombrePlaces(), // %.2f - unit price
                reservation.getMontantTotal(), // %.2f - total
                reservation.getUtilisateur().getPrenom(), // %s - first name
                reservation.getUtilisateur().getNom(), // %s - last name
                reservation.getUtilisateur().getEmail(), // %s - email
                reservation.getUtilisateur().getTelephone() != null ?
                        reservation.getUtilisateur().getTelephone() : "Non renseigné", // %s - phone
                reservation.getCommentaire() != null && !reservation.getCommentaire().isEmpty() ?
                        "<p><strong>Commentaire:</strong> " + reservation.getCommentaire() + "</p>" : "", // %s - comment
                java.time.LocalDateTime.now().format(dateFormatter) // %s - generation date
        );
    }

    private void injectReceiptStyles() {
        UI.getCurrent().getPage().executeJs(
                "const style = document.createElement('style');" +
                        "style.textContent = '" +
                        ".receipt-preview {" +
                        "    font-family: Arial, sans-serif;" +
                        "    background: white;" +
                        "    padding: 30px;" +
                        "    border-radius: 8px;" +
                        "    box-shadow: 0 2px 10px rgba(0,0,0,0.1);" +
                        "}" +
                        ".receipt-header {" +
                        "    text-align: center;" +
                        "    border-bottom: 2px solid #667eea;" +
                        "    padding-bottom: 20px;" +
                        "    margin-bottom: 30px;" +
                        "}" +
                        ".receipt-title {" +
                        "    color: #667eea;" +
                        "    margin: 0 0 10px 0;" +
                        "}" +
                        ".receipt-table {" +
                        "    width: 100%;" +
                        "    border-collapse: collapse;" +
                        "    margin-bottom: 20px;" +
                        "}" +
                        ".receipt-table th {" +
                        "    background: #f8f9fa;" +
                        "    padding: 12px;" +
                        "    text-align: left;" +
                        "    font-weight: bold;" +
                        "    border: 1px solid #ddd;" +
                        "}" +
                        ".receipt-table td {" +
                        "    padding: 12px;" +
                        "    border: 1px solid #ddd;" +
                        "}" +
                        ".receipt-total {" +
                        "    background: #667eea !important;" +
                        "    color: white;" +
                        "    font-weight: bold;" +
                        "    font-size: 16px;" +
                        "}" +
                        ".receipt-section {" +
                        "    margin-bottom: 30px;" +
                        "}" +
                        ".receipt-section-title {" +
                        "    color: #667eea;" +
                        "    border-bottom: 1px solid #ddd;" +
                        "    padding-bottom: 5px;" +
                        "    margin-bottom: 15px;" +
                        "}" +
                        "@media print {" +
                        "    .no-print {" +
                        "        display: none !important;" +
                        "    }" +
                        "    body {" +
                        "        margin: 0;" +
                        "        padding: 0;" +
                        "    }" +
                        "    .receipt-preview {" +
                        "        box-shadow: none;" +
                        "        padding: 0;" +
                        "    }" +
                        "}" +
                        "';" +
                        "document.head.appendChild(style);"
        );
    }

    private void cancelReservation(Reservation reservation) {
        Dialog confirmDialog = new Dialog();
        confirmDialog.setWidth("400px");

        VerticalLayout content = new VerticalLayout();
        content.setSpacing(true);
        content.setPadding(true);
        content.setAlignItems(Alignment.CENTER);

        Icon warningIcon = VaadinIcon.WARNING.create();
        warningIcon.setSize("48px");
        warningIcon.getStyle().set("color", "#e53e3e");

        H3 title = new H3("Annuler la réservation");
        title.getStyle()
                .set("color", "#e53e3e")
                .set("margin", "0");

        Paragraph message = new Paragraph(
                "Êtes-vous sûr de vouloir annuler cette réservation ? " +
                        "Cette action est irréversible."
        );
        message.getStyle()
                .set("text-align", "center")
                .set("color", "#666");

        HorizontalLayout buttons = new HorizontalLayout();
        buttons.setSpacing(true);

        Button confirmBtn = new Button("Oui, annuler", VaadinIcon.CHECK.create());
        confirmBtn.addThemeVariants(ButtonVariant.LUMO_ERROR);
        confirmBtn.addClickListener(e -> {
            try {
                reservationService.cancelReservation(reservation.getId(), "Annulé par l'utilisateur");
                Notification.show("Réservation annulée avec succès!", 3000, Notification.Position.MIDDLE);
                loadReservations();
                confirmDialog.close();
            } catch (Exception ex) {
                Notification.show("Erreur lors de l'annulation: " + ex.getMessage(),
                        5000, Notification.Position.MIDDLE);
            }
        });

        Button cancelBtn = new Button("Non", VaadinIcon.CLOSE.create());
        cancelBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        cancelBtn.addClickListener(e -> confirmDialog.close());

        buttons.add(confirmBtn, cancelBtn);

        content.add(warningIcon, title, message, buttons);
        confirmDialog.add(content);
        confirmDialog.open();
    }
}