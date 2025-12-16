package com.eventbooking.view.client;

import com.eventbooking.entity.User;
import com.eventbooking.service.ReservationService;
import com.eventbooking.service.EventService;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.charts.Chart;
import com.vaadin.flow.component.charts.model.*;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Route(value = "client/dashboard", layout = ClientMainLayout.class)
@PageTitle("Dashboard Client - EventBooking")
public class ClientDashboardView extends VerticalLayout {

    private final ReservationService reservationService;
    private final EventService eventService;
    private User currentUser;

    @Autowired
    public ClientDashboardView(ReservationService reservationService, EventService eventService) {
        this.reservationService = reservationService;
        this.eventService = eventService;

        Object userObj = VaadinSession.getCurrent().getAttribute("currentUser");
        this.currentUser = (userObj instanceof User) ? (User) userObj : null;

        if (currentUser == null) {
            UI.getCurrent().navigate("login");
            return;
        }

        initView();
        loadDashboardData();
    }

    private void initView() {
        setSpacing(false);
        setPadding(true);
        setWidthFull();
        getStyle().set("background", "#f8f9fa");

        // Header avec bienvenue
        HorizontalLayout welcomeHeader = new HorizontalLayout();
        welcomeHeader.setWidthFull();
        welcomeHeader.setAlignItems(Alignment.CENTER);
        welcomeHeader.setSpacing(true);

        Icon dashboardIcon = VaadinIcon.DASHBOARD.create();
        dashboardIcon.setSize("32px");
        dashboardIcon.getStyle().set("color", "#667eea");

        VerticalLayout welcomeText = new VerticalLayout();
        welcomeText.setSpacing(false);
        welcomeText.setPadding(false);

        H2 welcomeTitle = new H2("Bonjour, " + currentUser.getPrenom() + " !");
        welcomeTitle.getStyle()
                .set("margin", "0 0 5px 0")
                .set("color", "#333");

        Span welcomeMessage = new Span("Voici votre tableau de bord personnel");
        welcomeMessage.getStyle()
                .set("color", "#666")
                .set("font-size", "14px");

        welcomeText.add(welcomeTitle, welcomeMessage);
        welcomeHeader.add(dashboardIcon, welcomeText);

        // Date du jour
        Span todayDate = new Span("Aujourd'hui: " +
                LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE dd MMMM yyyy")));
        todayDate.getStyle()
                .set("margin-left", "auto")
                .set("color", "#666")
                .set("font-size", "14px");

        welcomeHeader.add(todayDate);

        add(welcomeHeader);
    }

    private void loadDashboardData() {
        // Statistiques principales
        HorizontalLayout mainStats = createMainStats();
        add(mainStats);

        // Graphiques
        HorizontalLayout chartsRow = new HorizontalLayout();
        chartsRow.setWidthFull();
        chartsRow.setSpacing(true);

        Chart reservationsChart = createMonthlyReservationsChart();
        Chart spendingChart = createSpendingChart();

        chartsRow.add(reservationsChart, spendingChart);
        add(chartsRow);

        // Réservations récentes
        VerticalLayout recentSection = createRecentReservationsSection();
        add(recentSection);

        // Événements à venir
        VerticalLayout upcomingSection = createUpcomingEventsSection();
        add(upcomingSection);
    }

    private HorizontalLayout createMainStats() {
        HorizontalLayout stats = new HorizontalLayout();
        stats.setWidthFull();
        stats.setSpacing(true);

        // Calcul des statistiques
        long totalReservations = reservationService.getUserReservationsCount(currentUser.getId());
        long confirmedReservations = reservationService.getUserConfirmedReservationsCount(currentUser.getId());
        double totalSpent = reservationService.getOrganizerRevenue(currentUser.getId());
        long upcomingReservations = reservationService.getUpcomingReservationsByUser(currentUser.getId()).size();

        stats.add(
                createDashboardStatCard(
                        VaadinIcon.TICKET,
                        "Réservations totales",
                        String.valueOf(totalReservations),
                        "Dont " + confirmedReservations + " confirmées",
                        "#667eea"
                ),
                createDashboardStatCard(
                        VaadinIcon.EURO,
                        "Total dépensé",
                        String.format("%.2f", totalSpent) + " MAD",
                        "Montant total des réservations",
                        "#38a169"
                ),
                createDashboardStatCard(
                        VaadinIcon.CALENDAR_CLOCK,
                        "Événements à venir",
                        String.valueOf(upcomingReservations),
                        "Dans les prochains jours",
                        "#ed8936"
                ),
                createDashboardStatCard(
                        VaadinIcon.STAR,
                        "Fidélité",
                        calculateLoyaltyLevel(totalReservations),
                        "Niveau de fidélité",
                        "#805ad5"
                )
        );

        return stats;
    }

    private VerticalLayout createDashboardStatCard(VaadinIcon icon, String title, String value,
                                                   String subtitle, String color) {
        VerticalLayout card = new VerticalLayout();
        card.setSpacing(false);
        card.setPadding(true);
        card.getStyle()
                .set("background", "white")
                .set("border-radius", "12px")
                .set("box-shadow", "0 4px 12px rgba(0,0,0,0.1)")
                .set("border-left", "4px solid " + color)
                .set("flex", "1");

        HorizontalLayout header = new HorizontalLayout();
        header.setWidthFull();
        header.setJustifyContentMode(JustifyContentMode.BETWEEN);
        header.setAlignItems(Alignment.CENTER);

        Icon cardIcon = icon.create();
        cardIcon.setSize("24px");
        cardIcon.getStyle().set("color", color);

        header.add(cardIcon);

        H3 cardValue = new H3(value);
        cardValue.getStyle()
                .set("margin", "10px 0 5px 0")
                .set("color", "#333")
                .set("font-size", "28px");

        H4 cardTitle = new H4(title);
        cardTitle.getStyle()
                .set("margin", "0 0 5px 0")
                .set("color", "#333");

        Span cardSubtitle = new Span(subtitle);
        cardSubtitle.getStyle()
                .set("color", "#666")
                .set("font-size", "12px");

        card.add(header, cardValue, cardTitle, cardSubtitle);
        return card;
    }

    private String calculateLoyaltyLevel(long totalReservations) {
        if (totalReservations >= 10) return "Or";
        if (totalReservations >= 5) return "Argent";
        if (totalReservations >= 3) return "Bronze";
        return "Nouveau";
    }

    private Chart createMonthlyReservationsChart() {
        Chart chart = new Chart(ChartType.COLUMN);
        chart.setWidthFull();
        chart.setHeight("400px");

        Configuration configuration = chart.getConfiguration();
        configuration.setTitle("Réservations par mois");

        XAxis xAxis = configuration.getxAxis();
        xAxis.setCategories("Jan", "Fév", "Mar", "Avr", "Mai", "Jun",
                "Jul", "Aoû", "Sep", "Oct", "Nov", "Déc");

        YAxis yAxis = configuration.getyAxis();
        yAxis.setTitle("Nombre de réservations");

        Tooltip tooltip = configuration.getTooltip();

        PlotOptionsColumn plotOptions = new PlotOptionsColumn();
        plotOptions.setColorByPoint(true);

        DataSeries series = new DataSeries();

        // Données exemple (dans une vraie app, vous auriez des données réelles)
        series.setData(3, 5, 4, 7, 6, 8, 10, 9, 8, 12, 10, 11);

        configuration.addSeries(series);

        return chart;
    }

    private Chart createSpendingChart() {
        Chart chart = new Chart(ChartType.LINE);
        chart.setWidthFull();
        chart.setHeight("400px");

        Configuration configuration = chart.getConfiguration();
        configuration.setTitle("Dépenses mensuelles");

        XAxis xAxis = configuration.getxAxis();
        xAxis.setCategories("Jan", "Fév", "Mar", "Avr", "Mai", "Jun");

        YAxis yAxis = configuration.getyAxis();
        yAxis.setTitle("Montant (MAD)");

        Tooltip tooltip = configuration.getTooltip();

        DataSeries series = new DataSeries();
        series.setName("Dépenses");

        // Données exemple
        series.setData(450, 680, 520, 890, 720, 950);

        configuration.addSeries(series);

        return chart;
    }

    private VerticalLayout createRecentReservationsSection() {
        VerticalLayout section = new VerticalLayout();
        section.setSpacing(true);
        section.setPadding(true);
        section.setWidthFull();
        section.getStyle()
                .set("background", "white")
                .set("border-radius", "12px")
                .set("box-shadow", "0 4px 12px rgba(0,0,0,0.1)")
                .set("margin-top", "20px");

        HorizontalLayout header = new HorizontalLayout();
        header.setWidthFull();
        header.setJustifyContentMode(JustifyContentMode.BETWEEN);
        header.setAlignItems(Alignment.CENTER);

        H3 sectionTitle = new H3("Réservations récentes");
        sectionTitle.getStyle()
                .set("margin", "0")
                .set("color", "#667eea");

        Button viewAllBtn = new Button("Voir toutes", VaadinIcon.ARROW_RIGHT.create());
        viewAllBtn.addClickListener(e -> getUI().ifPresent(ui -> ui.navigate("client/reservations")));

        header.add(sectionTitle, viewAllBtn);

        // Liste des réservations récentes
        List<com.eventbooking.entity.Reservation> recentReservations =
                reservationService.getReservationsByUser(currentUser.getId());

        // Prendre les 5 plus récentes
        recentReservations = recentReservations.stream()
                .limit(5)
                .toList();

        VerticalLayout reservationsList = new VerticalLayout();
        reservationsList.setSpacing(true);
        reservationsList.setPadding(false);

        if (recentReservations.isEmpty()) {
            Paragraph noReservations = new Paragraph("Aucune réservation récente");
            noReservations.getStyle().set("color", "#666").set("text-align", "center");
            reservationsList.add(noReservations);
        } else {
            for (com.eventbooking.entity.Reservation reservation : recentReservations) {
                reservationsList.add(createReservationItem(reservation));
            }
        }

        section.add(header, reservationsList);
        return section;
    }

    private HorizontalLayout createReservationItem(com.eventbooking.entity.Reservation reservation) {
        HorizontalLayout item = new HorizontalLayout();
        item.setWidthFull();
        item.setAlignItems(Alignment.CENTER);
        item.setSpacing(true);
        item.getStyle()
                .set("padding", "15px")
                .set("border-bottom", "1px solid #eee")
                .set("border-radius", "8px")
                .set("background", "#f8f9fa");

        Icon eventIcon = VaadinIcon.CALENDAR.create();
        eventIcon.setSize("24px");
        eventIcon.getStyle().set("color", "#667eea");

        VerticalLayout details = new VerticalLayout();
        details.setSpacing(false);
        details.setPadding(false);

        H4 eventName = new H4(reservation.getEvenement().getTitre());
        eventName.getStyle()
                .set("margin", "0 0 5px 0")
                .set("color", "#333");

        HorizontalLayout meta = new HorizontalLayout();
        meta.setSpacing(true);
        meta.setAlignItems(Alignment.CENTER);

        Span date = new Span(reservation.getEvenement().getDateDebut()
                .format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        date.getStyle()
                .set("color", "#666")
                .set("font-size", "12px");

        Span places = new Span(reservation.getNombrePlaces() + " places");
        places.getStyle()
                .set("color", "#666")
                .set("font-size", "12px");

        Span amount = new Span(reservation.getMontantTotal() + " MAD");
        amount.getStyle()
                .set("color", "#38a169")
                .set("font-size", "12px")
                .set("font-weight", "bold");

        meta.add(date, places, amount);
        details.add(eventName, meta);

        // Statut
        String status = reservation.getStatut().toString();
        String color = switch (status) {
            case "CONFIRMEE" -> "#38a169";
            case "EN_ATTENTE" -> "#ed8936";
            case "ANNULEE" -> "#e53e3e";
            default -> "#666";
        };

        Span statusBadge = new Span(status);
        statusBadge.getStyle()
                .set("background", color)
                .set("color", "white")
                .set("padding", "5px 10px")
                .set("border-radius", "15px")
                .set("font-size", "12px")
                .set("margin-left", "auto");

        item.add(eventIcon, details, statusBadge);
        return item;
    }

    private VerticalLayout createUpcomingEventsSection() {
        VerticalLayout section = new VerticalLayout();
        section.setSpacing(true);
        section.setPadding(true);
        section.setWidthFull();
        section.getStyle()
                .set("background", "white")
                .set("border-radius", "12px")
                .set("box-shadow", "0 4px 12px rgba(0,0,0,0.1)")
                .set("margin-top", "20px");

        H3 sectionTitle = new H3("Événements à venir");
        sectionTitle.getStyle()
                .set("margin", "0 0 20px 0")
                .set("color", "#667eea");

        // Récupérer les événements à venir
        List<com.eventbooking.entity.Event> upcomingEvents = eventService.getUpcomingEvents();

        if (upcomingEvents.isEmpty()) {
            Paragraph noEvents = new Paragraph("Aucun événement à venir");
            noEvents.getStyle().set("color", "#666").set("text-align", "center");
            section.add(noEvents);
        } else {
            // Prendre 3 événements maximum
            upcomingEvents = upcomingEvents.stream()
                    .limit(3)
                    .toList();

            for (com.eventbooking.entity.Event event : upcomingEvents) {
                section.add(createUpcomingEventItem(event));
            }
        }

        return section;
    }

    // Dans la méthode createUpcomingEventItem, corriger le bouton:
    private HorizontalLayout createUpcomingEventItem(com.eventbooking.entity.Event event) {
        HorizontalLayout item = new HorizontalLayout();
        item.setWidthFull();
        item.setAlignItems(Alignment.CENTER);
        item.setSpacing(true);
        item.getStyle()
                .set("padding", "15px")
                .set("border-bottom", "1px solid #eee")
                .set("border-radius", "8px")
                .set("background", "#f0f9ff")
                .set("cursor", "pointer");

        item.addClickListener(e -> {
            getUI().ifPresent(ui -> ui.getPage().executeJs(
                    "window.location.href = '/event/" + event.getId() + "'"
            ));
        });

        Icon eventIcon = VaadinIcon.CALENDAR.create();
        eventIcon.setSize("24px");
        eventIcon.getStyle().set("color", "#3182ce");

        VerticalLayout details = new VerticalLayout();
        details.setSpacing(false);
        details.setPadding(false);

        H4 eventName = new H4(event.getTitre());
        eventName.getStyle()
                .set("margin", "0 0 5px 0")
                .set("color", "#3182ce");

        HorizontalLayout meta = new HorizontalLayout();
        meta.setSpacing(true);
        meta.setAlignItems(Alignment.CENTER);

        Span date = new Span(event.getDateDebut()
                .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
        date.getStyle()
                .set("color", "#666")
                .set("font-size", "12px");

        Span location = new Span(event.getVille());
        location.getStyle()
                .set("color", "#666")
                .set("font-size", "12px");

        Span price = new Span(event.getPrixUnitaire() != null ?
                event.getPrixUnitaire() + " MAD" : "GRATUIT");
        price.getStyle()
                .set("color", "#38a169")
                .set("font-size", "12px")
                .set("font-weight", "bold");

        meta.add(date, location, price);
        details.add(eventName, meta);

        Button reserveBtn = new Button("Réserver", VaadinIcon.TICKET.create());
        reserveBtn.addThemeVariants(com.vaadin.flow.component.button.ButtonVariant.LUMO_PRIMARY);
        reserveBtn.addClickListener(e -> {
            // Utiliser e.getSource() au lieu de e.stopPropagation()
            e.getSource().getElement().executeJs("event.stopPropagation()");
            getUI().ifPresent(ui -> ui.getPage().executeJs(
                    "window.location.href = '/event/" + event.getId() + "'"
            ));
        });

        item.add(eventIcon, details, reserveBtn);
        return item;
    }

}