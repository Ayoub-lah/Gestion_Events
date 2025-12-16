package com.eventbooking.view.organizer;

import com.eventbooking.entity.User;
import com.eventbooking.entity.enums.UserRole;
import com.eventbooking.service.EventService;
import com.eventbooking.service.ReservationService;
import com.eventbooking.service.UserService;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.charts.Chart;
import com.vaadin.flow.component.charts.model.*;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Route(value = "organizer/dashboard", layout = com.eventbooking.view.organizer.OrganizerMainLayout.class)
@PageTitle("Dashboard Organisateur | Event Booking")
@RolesAllowed("ORGANIZER")
public class OrganizerDashboardView extends VerticalLayout {

    private final EventService eventService;
    private final ReservationService reservationService;
    private final UserService userService;
    private User currentUser;

    @Autowired
    public OrganizerDashboardView(EventService eventService,
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
        // Header
        H1 header = new H1("Dashboard Organisateur");
        header.getStyle()
                .set("margin", "1rem")
                .set("color", "var(--lumo-primary-text-color)");

        // Statistiques en haut
        HorizontalLayout statsLayout = createStatsCards();
        statsLayout.getStyle().set("margin", "1rem");

        // Graphiques et données
        HorizontalLayout chartsLayout = new HorizontalLayout();
        chartsLayout.setWidthFull();
        chartsLayout.setPadding(true);
        chartsLayout.setSpacing(true);

        // Graphique des réservations
        VerticalLayout reservationsChart = createReservationsChart();
        reservationsChart.setWidth("50%");

        // Prochains événements
        VerticalLayout upcomingEvents = createUpcomingEventsSection();
        upcomingEvents.setWidth("50%");

        chartsLayout.add(reservationsChart, upcomingEvents);

        // Actions rapides
        VerticalLayout quickActions = createQuickActions();
        quickActions.getStyle().set("margin", "1rem");

        add(header, statsLayout, chartsLayout, quickActions);
    }

    private HorizontalLayout createStatsCards() {
        HorizontalLayout layout = new HorizontalLayout();
        layout.setWidthFull();
        layout.setSpacing(true);

        try {
            long totalEvents = eventService.getOrganizerEventsCount(currentUser.getId());
            long publishedEvents = eventService.getOrganizerPublishedEventsCount(currentUser.getId());
            long totalReservations = reservationService.getOrganizerReservationsCount(currentUser.getId());
            double totalRevenue = reservationService.getOrganizerRevenue(currentUser.getId());


            layout.add(
                    createStatCard("🎉", "Événements",
                            String.valueOf(totalEvents),
                            "Publiés: " + publishedEvents,
                            "#1976d2",
                            "organizer/events"),




                    createStatCard("💰", "Revenu Total",
                            String.format("%.2f DH", totalRevenue),
                            "Ce mois",
                            "#7b1fa2",
                            "organizer/stats")
            );
        } catch (Exception e) {
            Span error = new Span("Erreur de chargement des statistiques");
            error.getStyle().set("color", "var(--lumo-error-text-color)");
            layout.add(error);
        }

        return layout;
    }

    private VerticalLayout createStatCard(String icon, String title, String value,
                                          String subtitle, String color, String route) {
        VerticalLayout card = new VerticalLayout();
        card.setPadding(true);
        card.setSpacing(false);
        card.getStyle()
                .set("border", "1px solid var(--lumo-contrast-10pct)")
                .set("border-radius", "var(--lumo-border-radius-m)")
                .set("background", "var(--lumo-base-color)")
                .set("cursor", "pointer")
                .set("transition", "all 0.3s ease")
                .set("width", "100%");

        card.addClickListener(e -> UI.getCurrent().navigate(route));

        card.getElement().addEventListener("mouseenter", e -> {
            card.getStyle().set("box-shadow", "var(--lumo-box-shadow-m)");
        });

        card.getElement().addEventListener("mouseleave", e -> {
            card.getStyle().set("box-shadow", "none");
        });

        // Icon and title
        HorizontalLayout header = new HorizontalLayout();
        header.setWidthFull();
        header.setJustifyContentMode(JustifyContentMode.BETWEEN);
        header.setAlignItems(Alignment.CENTER);

        Span iconSpan = new Span(icon);
        iconSpan.getStyle().set("font-size", "1.5rem");

        Span titleSpan = new Span(title);
        titleSpan.getStyle()
                .set("font-size", "var(--lumo-font-size-m)")
                .set("font-weight", "600")
                .set("color", "var(--lumo-secondary-text-color)");

        header.add(iconSpan, titleSpan);

        // Value
        H2 valueH2 = new H2(value);
        valueH2.getStyle()
                .set("margin", "0.5rem 0 0.25rem 0")
                .set("color", color)
                .set("font-size", "1.75rem");

        // Subtitle
        Span subtitleSpan = new Span(subtitle);
        subtitleSpan.getStyle()
                .set("font-size", "var(--lumo-font-size-xs)")
                .set("color", "var(--lumo-tertiary-text-color)");

        card.add(header, valueH2, subtitleSpan);
        return card;
    }

    private VerticalLayout createReservationsChart() {
        VerticalLayout layout = new VerticalLayout();
        layout.setPadding(true);
        layout.setSpacing(false);
        layout.getStyle()
                .set("border", "1px solid var(--lumo-contrast-10pct)")
                .set("border-radius", "var(--lumo-border-radius-m)")
                .set("background", "var(--lumo-base-color)");

        H3 title = new H3("📊 Réservations par Événement");
        title.getStyle().set("margin", "0 0 1rem 0");

        Chart chart = new Chart(ChartType.PIE);
        Configuration conf = chart.getConfiguration();
        conf.setTitle("");

        DataSeries series = new DataSeries();

        // Récupérer les données des événements de l'organisateur
        eventService.getEventsByOrganizer(currentUser.getId()).forEach(event -> {
            int reservationsCount = reservationService.getReservationsByEvent(event.getId()).size();
            if (reservationsCount > 0) {
                series.add(new DataSeriesItem(event.getTitre(), reservationsCount));
            }
        });

        conf.addSeries(series);

        PlotOptionsPie plotOptions = new PlotOptionsPie();
        plotOptions.setAllowPointSelect(true);
        plotOptions.setCursor(Cursor.POINTER);
        plotOptions.setShowInLegend(true);
        conf.setPlotOptions(plotOptions);

        layout.add(title, chart);
        return layout;
    }

    private VerticalLayout createUpcomingEventsSection() {
        VerticalLayout layout = new VerticalLayout();
        layout.setPadding(true);
        layout.setSpacing(false);
        layout.getStyle()
                .set("border", "1px solid var(--lumo-contrast-10pct)")
                .set("border-radius", "var(--lumo-border-radius-m)")
                .set("background", "var(--lumo-base-color)");

        H3 title = new H3("🎉 Prochains Événements");
        title.getStyle().set("margin", "0 0 1rem 0");

        // Grid pour les événements à venir
        Grid<com.eventbooking.entity.Event> grid = new Grid<>(com.eventbooking.entity.Event.class, false);
        grid.addColumn(new ComponentRenderer<>(event -> {
            HorizontalLayout layout1 = new HorizontalLayout();
            layout1.setSpacing(true);
            layout1.setAlignItems(Alignment.CENTER);

            Span icon = new Span("🎫");
            icon.getStyle().set("font-size", "1.25rem");

            VerticalLayout eventInfo = new VerticalLayout();
            eventInfo.setSpacing(false);
            eventInfo.setPadding(false);

            Span eventTitle = new Span(event.getTitre());
            eventTitle.getStyle()
                    .set("font-weight", "500")
                    .set("font-size", "var(--lumo-font-size-s)");

            Span eventDate = new Span(event.getDateDebut()
                    .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
            eventDate.getStyle()
                    .set("font-size", "var(--lumo-font-size-xs)")
                    .set("color", "var(--lumo-secondary-text-color)");

            eventInfo.add(eventTitle, eventDate);
            layout1.add(icon, eventInfo);

            return layout1;
        })).setHeader("Événement").setAutoWidth(true);

        grid.addColumn(event -> {
            int totalPlaces = event.getCapaciteMax();
            int reservedPlaces = reservationService.getConfirmedPlacesCountByEvent(event.getId());
            return reservedPlaces + " / " + totalPlaces;
        }).setHeader("Places").setAutoWidth(true);

        grid.addColumn(event -> {
            double revenue = reservationService.getReservationsByEvent(event.getId()).stream()
                    .filter(r -> r.getStatut().name().equals("CONFIRMEE"))
                    .mapToDouble(r -> r.getMontantTotal() != null ? r.getMontantTotal() : 0)
                    .sum();
            return String.format("%.2f DH", revenue);
        }).setHeader("Revenu").setAutoWidth(true);

        // Charger les événements à venir de l'organisateur
        List<com.eventbooking.entity.Event> upcomingEvents = eventService.getUpcomingEventsByOrganisateur(currentUser.getId());
        grid.setItems(upcomingEvents);

        // Bouton pour voir tous les événements
        Button viewAllButton = new Button("Voir tous les événements",
                new Icon(VaadinIcon.ARROW_RIGHT));
        viewAllButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        viewAllButton.addClickListener(e ->
                UI.getCurrent().navigate("organizer/events"));

        layout.add(title, grid, viewAllButton);
        return layout;
    }

    private VerticalLayout createQuickActions() {
        VerticalLayout layout = new VerticalLayout();
        layout.setPadding(true);
        layout.setSpacing(true);
        layout.getStyle()
                .set("border", "1px solid var(--lumo-contrast-10pct)")
                .set("border-radius", "var(--lumo-border-radius-m)")
                .set("background", "var(--lumo-base-color)");

        H3 title = new H3("⚡ Actions Rapides");
        title.getStyle().set("margin", "0 0 1rem 0");

        HorizontalLayout actions = new HorizontalLayout();
        actions.setSpacing(true);

        actions.add(
                createQuickActionButton("Nouvel Événement",
                        VaadinIcon.PLUS_CIRCLE, "organizer/events/new"),

                createQuickActionButton("Statistiques",
                        VaadinIcon.CHART, "organizer/stats")
        );

        layout.add(title, actions);
        return layout;
    }

    private Button createQuickActionButton(String text, VaadinIcon icon, String route) {
        Button button = new Button(text, icon.create());
        button.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        button.addClickListener(e -> UI.getCurrent().navigate(route));
        return button;
    }
}