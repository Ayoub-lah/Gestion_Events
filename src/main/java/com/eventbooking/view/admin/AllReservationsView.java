package com.eventbooking.view.admin;

import com.eventbooking.entity.Reservation;
import com.eventbooking.entity.User;
import com.eventbooking.entity.enums.ReservationStatus;
import com.eventbooking.entity.enums.UserRole;
import com.eventbooking.service.ReservationService;
import com.eventbooking.view.admin.components.AdminSidebar;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.server.VaadinSession;
import jakarta.annotation.security.PermitAll;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Route("admin/reservations")
@PageTitle("Gestion Réservations | Event Booking Admin")
@PermitAll
public class AllReservationsView extends HorizontalLayout {

    @Autowired
    private ReservationService reservationService;

    private Grid<Reservation> reservationGrid;
    private ComboBox<ReservationStatus> statusFilter;
    private TextField searchField;
    private User currentUser;
    private List<Reservation> currentReservations;

    public AllReservationsView(ReservationService reservationService) {
        this.reservationService = reservationService;

        setSizeFull();
        setPadding(false);
        setSpacing(false);
        getStyle().set("overflow", "hidden");

        currentUser = (User) VaadinSession.getCurrent().getAttribute("currentUser");

        if (currentUser == null || currentUser.getRole() != UserRole.ADMIN) {
            add(new H2("Accès refusé - Administrateurs uniquement"));
            return;
        }

        createUI();
        loadReservations();
    }

    private void createUI() {
        // Ajouter la sidebar
        AdminSidebar sidebar = new AdminSidebar(currentUser, "admin/reservations");

        // Créer la zone de contenu
        VerticalLayout contentArea = createContentArea();

        add(sidebar, contentArea);
    }

    private VerticalLayout createContentArea() {
        VerticalLayout content = new VerticalLayout();
        content.setSizeFull();
        content.setPadding(true);
        content.setSpacing(true);
        content.getStyle()
                .set("background", "#f8fafc")
                .set("overflow-y", "auto");

        HorizontalLayout header = createHeader();
        HorizontalLayout filters = createFilters();
        reservationGrid = createReservationGrid();

        content.add(header, filters, reservationGrid);
        return content;
    }

    private HorizontalLayout createHeader() {
        HorizontalLayout header = new HorizontalLayout();
        header.setWidthFull();
        header.setJustifyContentMode(JustifyContentMode.BETWEEN);
        header.setAlignItems(Alignment.CENTER);

        H1 title = new H1("📋 Gestion des Réservations");
        title.getStyle().set("margin", "0").set("color", "#2e7d32");

        // Boutons d'export
        HorizontalLayout exportButtons = createExportButtons();

        header.add(title, exportButtons);
        return header;
    }

    private HorizontalLayout createExportButtons() {
        HorizontalLayout exportLayout = new HorizontalLayout();
        exportLayout.setSpacing(true);

        // Bouton Export CSV
        Button exportCsvBtn = new Button("Export CSV", new Icon(VaadinIcon.DOWNLOAD));
        exportCsvBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        exportCsvBtn.addClickListener(e -> exportToCSV());

        // Bouton Export PDF
        Button exportPdfBtn = new Button("Export PDF", new Icon(VaadinIcon.FILE_TEXT));
        exportPdfBtn.addThemeVariants(ButtonVariant.LUMO_SUCCESS);
        exportPdfBtn.addClickListener(e -> exportToPDF());

        exportLayout.add(exportCsvBtn, exportPdfBtn);
        return exportLayout;
    }

    private HorizontalLayout createFilters() {
        HorizontalLayout filters = new HorizontalLayout();
        filters.setWidthFull();
        filters.setSpacing(true);
        filters.setAlignItems(Alignment.END);

        searchField = new TextField("Rechercher");
        searchField.setPlaceholder("Code, utilisateur, événement...");
        searchField.setPrefixComponent(new Icon(VaadinIcon.SEARCH));
        searchField.setWidth("300px");
        searchField.addValueChangeListener(e -> filterReservations());

        statusFilter = new ComboBox<>("Statut");
        statusFilter.setItems(ReservationStatus.values());
        statusFilter.setItemLabelGenerator(this::getStatusLabel);
        statusFilter.setPlaceholder("Tous les statuts");
        statusFilter.setClearButtonVisible(true);
        statusFilter.addValueChangeListener(e -> filterReservations());

        Button refreshBtn = new Button("🔄 Actualiser");
        refreshBtn.addClickListener(e -> loadReservations());

        filters.add(searchField, statusFilter, refreshBtn);
        return filters;
    }

    private Grid<Reservation> createReservationGrid() {
        Grid<Reservation> grid = new Grid<>(Reservation.class, false);
        grid.setHeight("600px");

        grid.addColumn(Reservation::getId)
                .setHeader("ID")
                .setWidth("70px")
                .setFlexGrow(0);

        grid.addColumn(Reservation::getCodeReservation)
                .setHeader("Code Réservation")
                .setSortable(true)
                .setWidth("150px");

        grid.addColumn(res -> res.getUtilisateur().getPrenom() + " " + res.getUtilisateur().getNom())
                .setHeader("Client")
                .setSortable(true)
                .setWidth("180px");

        grid.addColumn(res -> res.getUtilisateur().getEmail())
                .setHeader("Email")
                .setWidth("200px");

        grid.addColumn(res -> res.getEvenement().getTitre())
                .setHeader("Événement")
                .setSortable(true)
                .setWidth("200px");

        grid.addColumn(Reservation::getNombrePlaces)
                .setHeader("Places")
                .setWidth("80px");

        grid.addColumn(res -> res.getMontantTotal() + " MAD")
                .setHeader("Montant")
                .setWidth("120px");

        grid.addColumn(res -> res.getDateReservation().format(
                        DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")))
                .setHeader("Date Réservation")
                .setSortable(true)
                .setWidth("170px");

        grid.addColumn(new ComponentRenderer<>(reservation -> {
            Span statusBadge = new Span(getStatusLabel(reservation.getStatut()));
            statusBadge.getStyle()
                    .set("background", getStatusColor(reservation.getStatut()))
                    .set("color", "white")
                    .set("padding", "5px 10px")
                    .set("border-radius", "12px")
                    .set("font-size", "12px")
                    .set("font-weight", "bold");
            return statusBadge;
        })).setHeader("Statut").setWidth("140px");

        grid.addComponentColumn(reservation -> {
            HorizontalLayout actions = new HorizontalLayout();
            actions.setSpacing(true);

            if (reservation.getStatut() == ReservationStatus.EN_ATTENTE) {
                Button confirmBtn = new Button(new Icon(VaadinIcon.CHECK));
                confirmBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_SUCCESS);
                confirmBtn.getElement().setAttribute("title", "Confirmer");
                confirmBtn.addClickListener(e -> confirmReservation(reservation));
                actions.add(confirmBtn);
            }

            if (reservation.getStatut() != ReservationStatus.ANNULEE) {
                Button cancelBtn = new Button(new Icon(VaadinIcon.BAN));
                cancelBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_ERROR);
                cancelBtn.getElement().setAttribute("title", "Annuler");
                cancelBtn.addClickListener(e -> confirmCancelReservation(reservation));
                actions.add(cancelBtn);
            }

            Button deleteBtn = new Button(new Icon(VaadinIcon.TRASH));
            deleteBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_ERROR);
            deleteBtn.getElement().setAttribute("title", "Supprimer");
            deleteBtn.addClickListener(e -> confirmDeleteReservation(reservation));
            actions.add(deleteBtn);

            return actions;
        }).setHeader("Actions").setWidth("180px").setFlexGrow(0);

        return grid;
    }

    private void loadReservations() {
        try {
            currentReservations = reservationService.getAllReservations();
            reservationGrid.setItems(currentReservations);
        } catch (Exception e) {
            showNotification("Erreur de chargement: " + e.getMessage(), NotificationVariant.LUMO_ERROR);
        }
    }

    private void filterReservations() {
        String searchTerm = searchField.getValue().toLowerCase();
        ReservationStatus selectedStatus = statusFilter.getValue();

        currentReservations = reservationService.getAllReservations().stream()
                .filter(reservation -> {
                    boolean matchesSearch = searchTerm.isEmpty() ||
                            reservation.getCodeReservation().toLowerCase().contains(searchTerm) ||
                            reservation.getUtilisateur().getNom().toLowerCase().contains(searchTerm) ||
                            reservation.getUtilisateur().getPrenom().toLowerCase().contains(searchTerm) ||
                            reservation.getUtilisateur().getEmail().toLowerCase().contains(searchTerm) ||
                            reservation.getEvenement().getTitre().toLowerCase().contains(searchTerm);

                    boolean matchesStatus = selectedStatus == null ||
                            reservation.getStatut() == selectedStatus;

                    return matchesSearch && matchesStatus;
                })
                .collect(Collectors.toList());

        reservationGrid.setItems(currentReservations);
    }

    // ============== EXPORT CSV ==============
    private void exportToCSV() {
        try {
            StreamResource resource = new StreamResource("reservations.csv", () -> {
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                PrintWriter writer = new PrintWriter(outputStream);

                // En-têtes CSV
                writer.println("ID,Code Réservation,Client,Email,Téléphone,Événement,Places,Montant (MAD),Date Réservation,Statut,Commentaire");

                // Données
                for (Reservation res : currentReservations) {
                    writer.printf("%d,\"%s\",\"%s %s\",\"%s\",\"%s\",\"%s\",%d,%.2f,\"%s\",\"%s\",\"%s\"%n",
                            res.getId(),
                            res.getCodeReservation(),
                            res.getUtilisateur().getPrenom(),
                            res.getUtilisateur().getNom(),
                            res.getUtilisateur().getEmail(),
                            res.getUtilisateur().getTelephone() != null ? res.getUtilisateur().getTelephone() : "",
                            res.getEvenement().getTitre(),
                            res.getNombrePlaces(),
                            res.getMontantTotal(),
                            res.getDateReservation().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")),
                            getStatusLabel(res.getStatut()),
                            res.getCommentaire() != null ? res.getCommentaire().replace("\"", "\"\"") : ""
                    );
                }

                writer.flush();
                return new ByteArrayInputStream(outputStream.toByteArray());
            });

            // Créer un lien de téléchargement
            Anchor downloadLink = new Anchor(resource, "");
            downloadLink.getElement().setAttribute("download", true);
            downloadLink.setId("csv-download-link");

            // Ajouter temporairement à la page et déclencher le téléchargement
            add(downloadLink);
            UI.getCurrent().getPage().executeJs("document.getElementById('csv-download-link').click();");

            // Retirer le lien après le téléchargement
            UI.getCurrent().getPage().executeJs(
                    "setTimeout(function() { " +
                            "   var link = document.getElementById('csv-download-link'); " +
                            "   if(link) link.remove(); " +
                            "}, 1000);"
            );

            showNotification("✓ Export CSV généré avec succès!", NotificationVariant.LUMO_SUCCESS);

        } catch (Exception e) {
            showNotification("Erreur lors de l'export CSV: " + e.getMessage(), NotificationVariant.LUMO_ERROR);
        }
    }

    // ============== EXPORT PDF ==============
    private void exportToPDF() {
        try {
            StreamResource resource = new StreamResource("reservations.pdf", () -> {
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

                // Générer un PDF simple (HTML to PDF)
                String htmlContent = generatePDFContent();

                // Pour un vrai PDF, vous devriez utiliser une bibliothèque comme iText ou Apache PDFBox
                // Ici on génère un fichier texte formaté comme exemple
                try {
                    outputStream.write(htmlContent.getBytes());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                return new ByteArrayInputStream(outputStream.toByteArray());
            });

            // Créer un lien de téléchargement
            Anchor downloadLink = new Anchor(resource, "");
            downloadLink.getElement().setAttribute("download", true);
            downloadLink.setId("pdf-download-link");

            add(downloadLink);
            UI.getCurrent().getPage().executeJs("document.getElementById('pdf-download-link').click();");

            UI.getCurrent().getPage().executeJs(
                    "setTimeout(function() { " +
                            "   var link = document.getElementById('pdf-download-link'); " +
                            "   if(link) link.remove(); " +
                            "}, 1000);"
            );

            showNotification("✓ Export PDF généré avec succès!", NotificationVariant.LUMO_SUCCESS);

        } catch (Exception e) {
            showNotification("Erreur lors de l'export PDF: " + e.getMessage(), NotificationVariant.LUMO_ERROR);
        }
    }

    private String generatePDFContent() {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html><html><head><meta charset='UTF-8'>");
        html.append("<style>");
        html.append("body { font-family: Arial, sans-serif; margin: 20px; }");
        html.append("h1 { color: #2e7d32; text-align: center; }");
        html.append("table { width: 100%; border-collapse: collapse; margin-top: 20px; }");
        html.append("th, td { border: 1px solid #ddd; padding: 8px; text-align: left; }");
        html.append("th { background-color: #2e7d32; color: white; }");
        html.append("tr:nth-child(even) { background-color: #f2f2f2; }");
        html.append(".footer { margin-top: 30px; text-align: center; color: #666; }");
        html.append("</style></head><body>");

        html.append("<h1>📋 Rapport des Réservations</h1>");
        html.append("<p><strong>Date d'export:</strong> ").append(
                java.time.LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
        ).append("</p>");
        html.append("<p><strong>Nombre total de réservations:</strong> ").append(currentReservations.size()).append("</p>");

        html.append("<table>");
        html.append("<thead><tr>");
        html.append("<th>ID</th><th>Code</th><th>Client</th><th>Email</th>");
        html.append("<th>Événement</th><th>Places</th><th>Montant</th><th>Date</th><th>Statut</th>");
        html.append("</tr></thead><tbody>");

        for (Reservation res : currentReservations) {
            html.append("<tr>");
            html.append("<td>").append(res.getId()).append("</td>");
            html.append("<td>").append(res.getCodeReservation()).append("</td>");
            html.append("<td>").append(res.getUtilisateur().getPrenom()).append(" ")
                    .append(res.getUtilisateur().getNom()).append("</td>");
            html.append("<td>").append(res.getUtilisateur().getEmail()).append("</td>");
            html.append("<td>").append(res.getEvenement().getTitre()).append("</td>");
            html.append("<td>").append(res.getNombrePlaces()).append("</td>");
            html.append("<td>").append(String.format("%.2f MAD", res.getMontantTotal())).append("</td>");
            html.append("<td>").append(res.getDateReservation()
                    .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))).append("</td>");
            html.append("<td>").append(getStatusLabel(res.getStatut())).append("</td>");
            html.append("</tr>");
        }

        html.append("</tbody></table>");
        html.append("<div class='footer'>");
        html.append("<p>Event Booking System - Rapport généré automatiquement</p>");
        html.append("</div>");
        html.append("</body></html>");

        return html.toString();
    }

    private void confirmReservation(Reservation reservation) {
        try {
            reservationService.confirmReservation(reservation.getId());
            showNotification("✓ Réservation confirmée", NotificationVariant.LUMO_SUCCESS);
            loadReservations();
        } catch (Exception e) {
            showNotification("Erreur: " + e.getMessage(), NotificationVariant.LUMO_ERROR);
        }
    }

    private void confirmCancelReservation(Reservation reservation) {
        ConfirmDialog dialog = new ConfirmDialog();
        dialog.setHeader("⚠️ Confirmer l'annulation");
        dialog.setText("Êtes-vous sûr de vouloir annuler cette réservation ?");

        dialog.setCancelable(true);
        dialog.setCancelText("Non");

        dialog.setConfirmText("Oui, annuler");
        dialog.setConfirmButtonTheme("error primary");
        dialog.addConfirmListener(e -> cancelReservation(reservation));

        dialog.open();
    }

    private void cancelReservation(Reservation reservation) {
        try {
            reservationService.cancelReservation(reservation.getId(), "Annulé par l'administrateur");
            showNotification("✓ Réservation annulée", NotificationVariant.LUMO_SUCCESS);
            loadReservations();
        } catch (Exception e) {
            showNotification("Erreur: " + e.getMessage(), NotificationVariant.LUMO_ERROR);
        }
    }

    private void confirmDeleteReservation(Reservation reservation) {
        ConfirmDialog dialog = new ConfirmDialog();
        dialog.setHeader("⚠️ Confirmer la suppression");
        dialog.setText("Êtes-vous sûr de vouloir supprimer cette réservation ? Cette action est irréversible.");

        dialog.setCancelable(true);
        dialog.setCancelText("Annuler");

        dialog.setConfirmText("Supprimer");
        dialog.setConfirmButtonTheme("error primary");
        dialog.addConfirmListener(e -> deleteReservation(reservation));

        dialog.open();
    }

    private void deleteReservation(Reservation reservation) {
        try {
            reservationService.deleteReservation(reservation.getId());
            showNotification("✓ Réservation supprimée", NotificationVariant.LUMO_SUCCESS);
            loadReservations();
        } catch (Exception e) {
            showNotification("Erreur: " + e.getMessage(), NotificationVariant.LUMO_ERROR);
        }
    }

    private String getStatusLabel(ReservationStatus status) {
        return switch (status) {
            case EN_ATTENTE -> "⏳ En Attente";
            case CONFIRMEE -> "✅ Confirmée";
            case ANNULEE -> "❌ Annulée";
        };
    }

    private String getStatusColor(ReservationStatus status) {
        return switch (status) {
            case EN_ATTENTE -> "#ed6c02";
            case CONFIRMEE -> "#2e7d32";
            case ANNULEE -> "#d32f2f";
        };
    }

    private void showNotification(String message, NotificationVariant variant) {
        Notification notification = new Notification(message, 3000);
        notification.addThemeVariants(variant);
        notification.setPosition(Notification.Position.TOP_CENTER);
        notification.open();
    }
}