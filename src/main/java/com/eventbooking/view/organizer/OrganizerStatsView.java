package com.eventbooking.view.organizer;

import com.eventbooking.entity.User;
import com.eventbooking.entity.enums.UserRole;
import com.eventbooking.service.EventService;
import com.eventbooking.service.ReservationService;
import com.eventbooking.service.UserService;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.charts.Chart;
import com.vaadin.flow.component.charts.model.*;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Route(value = "organizer/stats", layout = OrganizerMainLayout.class)
@PageTitle("Statistiques | Event Booking")
@RolesAllowed("ORGANIZER")
public class OrganizerStatsView extends VerticalLayout {

    private final EventService eventService;
    private final ReservationService reservationService;
    private final UserService userService;
    private User currentUser;

    @Autowired
    public OrganizerStatsView(EventService eventService,
                              ReservationService reservationService,
                              UserService userService) {
        this.eventService = eventService;
        this.reservationService = reservationService;
        this.userService = userService;

        currentUser = (User) VaadinSession.getCurrent().getAttribute("currentUser");
        if (currentUser == null || currentUser.getRole() != UserRole.ORGANIZER) {
            UI.getCurrent().navigate("login");
            return;
        }

        setSizeFull();
        setPadding(false);
        setSpacing(false);

        createUI();
    }

    private void createUI() {
        H1 title = new H1("📊 Statistiques");
        title.getStyle()
                .set("margin", "1rem")
                .set("color", "var(--lumo-primary-text-color)");

        // Statistiques principales
        HorizontalLayout mainStats = createMainStats();
        mainStats.getStyle().set("margin", "1rem");

        // Graphiques
        HorizontalLayout chartsRow1 = new HorizontalLayout();
        chartsRow1.setWidthFull();
        chartsRow1.setSpacing(true);
        chartsRow1.setPadding(true);

        chartsRow1.add(
                createReservationsChart(),
                createRevenueChart()
        );

        HorizontalLayout chartsRow2 = new HorizontalLayout();
        chartsRow2.setWidthFull();
        chartsRow2.setSpacing(true);
        chartsRow2.setPadding(true);

        chartsRow2.add(
                createEventsByCategoryChart(),
                createMonthlyStatsChart()
        );

        add(title, mainStats, chartsRow1, chartsRow2);
    }

    private HorizontalLayout createMainStats() {
        HorizontalLayout layout = new HorizontalLayout();
        layout.setWidthFull();
        layout.setSpacing(true);

        try {
            long totalEvents = eventService.getOrganizerEventsCount(currentUser.getId());
            long publishedEvents = eventService.getOrganizerPublishedEventsCount(currentUser.getId());
            long totalReservations = reservationService.getOrganizerReservationsCount(currentUser.getId());
            double totalRevenue = reservationService.getOrganizerRevenue(currentUser.getId());
            long activeClients = userService.getUsersByRole(UserRole.CLIENT).stream()
                    .filter(User::getActif)
                    .count();
            double avgTicket = totalReservations > 0 ? totalRevenue / totalReservations : 0;

            layout.add(
                    createStatCard("🎉", "Événements",
                            String.valueOf(totalEvents),
                            "Publiés: " + publishedEvents, "#1976d2"),

                    createStatCard("💰", "Revenu Total",
                            String.format("%.2f DH", totalRevenue),
                            "Moyenne: " + String.format("%.2f DH", avgTicket), "#7b1fa2")

            );
        } catch (Exception e) {
            // Gérer l'erreur
        }

        return layout;
    }

    private VerticalLayout createStatCard(String icon, String title, String value,
                                          String subtitle, String color) {
        VerticalLayout card = new VerticalLayout();
        card.setPadding(true);
        card.setSpacing(false);
        card.getStyle()
                .set("border", "1px solid var(--lumo-contrast-10pct)")
                .set("border-radius", "var(--lumo-border-radius-m)")
                .set("background", "var(--lumo-base-color)")
                .set("width", "100%");

        HorizontalLayout header = new HorizontalLayout();
        header.setWidthFull();
        header.setJustifyContentMode(JustifyContentMode.BETWEEN);

        Span iconSpan = new Span(icon);
        iconSpan.getStyle().set("font-size", "1.5rem");

        Span titleSpan = new Span(title);
        titleSpan.getStyle()
                .set("font-size", "var(--lumo-font-size-m)")
                .set("color", "var(--lumo-secondary-text-color)");

        header.add(iconSpan, titleSpan);

        H2 valueH2 = new H2(value);
        valueH2.getStyle()
                .set("margin", "0.5rem 0 0.25rem 0")
                .set("color", color)
                .set("font-size", "1.75rem");

        Span subtitleSpan = new Span(subtitle);
        subtitleSpan.getStyle()
                .set("font-size", "var(--lumo-font-size-xs)")
                .set("color", "var(--lumo-tertiary-text-color)");

        card.add(header, valueH2, subtitleSpan);
        return card;
    }

    private Chart createReservationsChart() {
        Chart chart = new Chart(ChartType.COLUMN);
        Configuration conf = chart.getConfiguration();
        conf.setTitle("Réservations par Événement");

        XAxis xAxis = new XAxis();
        // Ajouter les titres des événements
        eventService.getEventsByOrganizer(currentUser.getId()).forEach(event -> {
            xAxis.addCategory(event.getTitre());
        });
        conf.addxAxis(xAxis);

        YAxis yAxis = new YAxis();
        yAxis.setTitle("Nombre de réservations");
        conf.addyAxis(yAxis);

        DataSeries series = new DataSeries("Réservations");
        eventService.getEventsByOrganizer(currentUser.getId()).forEach(event -> {
            int reservationsCount = reservationService.getReservationsByEvent(event.getId()).size();
            series.add(new DataSeriesItem(event.getTitre(), reservationsCount));
        });

        conf.addSeries(series);

        return chart;
    }

    private Chart createRevenueChart() {
        Chart chart = new Chart(ChartType.PIE);
        Configuration conf = chart.getConfiguration();
        conf.setTitle("Revenu par Événement");

        DataSeries series = new DataSeries();

        eventService.getEventsByOrganizer(currentUser.getId()).forEach(event -> {
            double eventRevenue = reservationService.getReservationsByEvent(event.getId()).stream()
                    .filter(r -> r.getStatut().name().equals("CONFIRMEE"))
                    .mapToDouble(r -> r.getMontantTotal() != null ? r.getMontantTotal() : 0)
                    .sum();

            if (eventRevenue > 0) {
                series.add(new DataSeriesItem(event.getTitre(), eventRevenue));
            }
        });

        conf.addSeries(series);
        return chart;
    }

    private Chart createEventsByCategoryChart() {
        Chart chart = new Chart(ChartType.BAR);
        Configuration conf = chart.getConfiguration();
        conf.setTitle("Événements par Catégorie");

        XAxis xAxis = new XAxis();
        xAxis.setCategories("CONCERT", "THEATRE", "CONFERENCE", "SPORT", "AUTRE");
        conf.addxAxis(xAxis);

        YAxis yAxis = new YAxis();
        yAxis.setTitle("Nombre d'événements");
        conf.addyAxis(yAxis);

        DataSeries series = new DataSeries("Événements");

        Map<String, Long> eventsByCategory = eventService.getEventsByOrganizer(currentUser.getId()).stream()
                .collect(Collectors.groupingBy(
                        e -> e.getCategorie().name(),
                        Collectors.counting()
                ));

        series.add(new DataSeriesItem("CONCERT",
                eventsByCategory.getOrDefault("CONCERT", 0L)));
        series.add(new DataSeriesItem("THEATRE",
                eventsByCategory.getOrDefault("THEATRE", 0L)));
        series.add(new DataSeriesItem("CONFERENCE",
                eventsByCategory.getOrDefault("CONFERENCE", 0L)));
        series.add(new DataSeriesItem("SPORT",
                eventsByCategory.getOrDefault("SPORT", 0L)));
        series.add(new DataSeriesItem("AUTRE",
                eventsByCategory.getOrDefault("AUTRE", 0L)));

        conf.addSeries(series);
        return chart;
    }

    private Chart createMonthlyStatsChart() {
        Chart chart = new Chart(ChartType.LINE);
        Configuration conf = chart.getConfiguration();
        conf.setTitle("Statistiques Mensuelles");

        XAxis xAxis = new XAxis();
        xAxis.setCategories("Jan", "Fév", "Mar", "Avr", "Mai", "Jun",
                "Jul", "Aoû", "Sep", "Oct", "Nov", "Déc");
        conf.addxAxis(xAxis);



        YAxis yAxis = new YAxis();
        yAxis.setTitle("Nombre");
        conf.addyAxis(yAxis);

        // Série des événements (exemple)
        DataSeries eventsSeries = new DataSeries("Événements");
        // Ajouter des données factices pour l'exemple
        for (int i = 1; i <= 12; i++) {
            eventsSeries.add(new DataSeriesItem(i, Math.random() * 10));
        }

        // Série des réservations
        DataSeries reservationsSeries = new DataSeries("Réservations");
        for (int i = 1; i <= 12; i++) {
            reservationsSeries.add(new DataSeriesItem(i, Math.random() * 50));
        }

        conf.addSeries(eventsSeries);
        conf.addSeries(reservationsSeries);

        return chart;
    }
}