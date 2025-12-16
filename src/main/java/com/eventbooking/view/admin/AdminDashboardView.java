package com.eventbooking.view.admin;

import com.eventbooking.entity.Event;
import com.eventbooking.entity.Reservation;
import com.eventbooking.entity.User;
import com.eventbooking.entity.enums.*;
import com.eventbooking.service.EventService;
import com.eventbooking.service.ReservationService;
import com.eventbooking.service.UserService;
import com.eventbooking.service.dto.DashboardFilter;
import com.eventbooking.view.admin.components.AdminSidebar;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.TabSheet;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;
import jakarta.annotation.security.PermitAll;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Route("admin/dashboard")
@PageTitle("Dashboard Admin | Event Booking")
@PermitAll
public class AdminDashboardView extends HorizontalLayout {

    @Autowired
    private UserService userService;

    @Autowired
    private EventService eventService;

    @Autowired
    private ReservationService reservationService;

    private DashboardFilter currentFilter;

    private User currentUser;
    private TabSheet dashboardTabs;
    private DatePicker dateFromPicker;
    private DatePicker dateToPicker;
    private ComboBox<String> periodComboBox;

    public AdminDashboardView(UserService userService,
                              EventService eventService,
                              ReservationService reservationService) {
        this.userService = userService;
        this.eventService = eventService;
        this.reservationService = reservationService;


        setSizeFull();
        setPadding(false);
        setSpacing(false);
        getStyle().set("overflow", "hidden");

        // Charger l'utilisateur
        loadCurrentUser();

        // ⭐ Vérifier le rôle et créer l'UI
        if (currentUser != null && currentUser.getRole() == UserRole.ADMIN) {
            createUI();
        } else if (currentUser != null) {
            getUI().ifPresent(ui -> ui.navigate("access-denied"));
        }
    }

    private void loadCurrentUser() {
        currentUser = (User) VaadinSession.getCurrent().getAttribute("currentUser");
        if (currentUser == null) {
            System.out.println("❌ Aucun utilisateur connecté - Redirection vers login");
            getUI().ifPresent(ui -> ui.getPage().executeJs("window.location.href = 'login'"));
        } else {
            System.out.println("✅ Utilisateur connecté: " + currentUser.getEmail() + " - Rôle: " + currentUser.getRole());
        }
    }

    private void createUI() {
        // Créer la sidebar
        AdminSidebar sidebar = new AdminSidebar(currentUser, "admin/dashboard");

        // Créer la zone de contenu principale
        VerticalLayout contentArea = createContentArea();

        add(sidebar, contentArea);
    }

    private VerticalLayout createContentArea() {
        VerticalLayout contentArea = new VerticalLayout();
        contentArea.setSizeFull();
        contentArea.setPadding(true);
        contentArea.setSpacing(true);
        contentArea.getStyle()
                .set("background", "#f8fafc")
                .set("overflow-y", "auto");

        // Header du contenu
        HorizontalLayout header = createContentHeader();

        // Filtres pour le dashboard
        HorizontalLayout filters = createDashboardFilters();

        // Onglets du dashboard
        dashboardTabs = createDashboardTabs();

        contentArea.add(header, filters, dashboardTabs);
        return contentArea;
    }

    private HorizontalLayout createContentHeader() {
        HorizontalLayout header = new HorizontalLayout();
        header.setWidthFull();
        header.setJustifyContentMode(JustifyContentMode.BETWEEN);
        header.setAlignItems(Alignment.CENTER);

        // Titre avec icône
        HorizontalLayout titleLayout = new HorizontalLayout();
        titleLayout.setSpacing(true);
        titleLayout.setAlignItems(Alignment.CENTER);

        Icon dashboardIcon = VaadinIcon.DASHBOARD.create();
        dashboardIcon.setSize("24px");
        dashboardIcon.setColor("#1e293b");

        H1 title = new H1("Tableau de Bord Administrateur");
        title.getStyle()
                .set("margin", "0")
                .set("color", "#1e293b")
                .set("font-weight", "600");

        titleLayout.add(dashboardIcon, title);

        // Actions rapides
        HorizontalLayout actions = new HorizontalLayout();
        actions.setSpacing(true);
        actions.setAlignItems(Alignment.CENTER);

        // Bouton Rapports
        Button reportsBtn = new Button("Rapports détaillés",
                VaadinIcon.CHART.create());
        reportsBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        reportsBtn.addClickListener(e ->
                UI.getCurrent().navigate("admin/reports"));

        // Bouton Actualiser
        Button refreshBtn = new Button("Actualiser",
                VaadinIcon.REFRESH.create());
        refreshBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        refreshBtn.addClickListener(e -> refreshDashboard());

        actions.add(reportsBtn, refreshBtn);
        header.add(titleLayout, actions);
        return header;
    }

    private HorizontalLayout createDashboardFilters() {
        HorizontalLayout filters = new HorizontalLayout();
        filters.setWidthFull();
        filters.setSpacing(true);
        filters.setAlignItems(Alignment.END);

        // Sélecteur de période
        periodComboBox = new ComboBox<>("Période");
        periodComboBox.setItems(
                "Aujourd'hui",
                "Cette semaine",
                "Ce mois",
                "Mois dernier",
                "Cette année",
                "Toute période"
        );
        periodComboBox.setValue("Ce mois");
        periodComboBox.setWidth("200px");
        periodComboBox.addValueChangeListener(e -> updateDateRange());

        // Date de début
        dateFromPicker = new DatePicker("Du");
        dateFromPicker.setWidth("150px");

        // Date de fin
        dateToPicker = new DatePicker("Au");
        dateToPicker.setWidth("150px");

        updateDateRange();

        // Bouton d'application des filtres
        Button applyBtn = new Button("Appliquer", VaadinIcon.FILTER.create());
        applyBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SMALL);
        applyBtn.addClickListener(e -> applyFilters());

        filters.add(periodComboBox, dateFromPicker, dateToPicker, applyBtn);
        return filters;
    }

    private void updateDateRange() {
        LocalDate today = LocalDate.now();
        String period = periodComboBox.getValue();

        if (period == null) return;

        switch (period) {
            case "Aujourd'hui":
                dateFromPicker.setValue(today);
                dateToPicker.setValue(today);
                break;
            case "Cette semaine":
                LocalDate startOfWeek = today.minusDays(today.getDayOfWeek().getValue() - 1);
                dateFromPicker.setValue(startOfWeek);
                dateToPicker.setValue(today);
                break;
            case "Ce mois":
                LocalDate startOfMonth = today.withDayOfMonth(1);
                dateFromPicker.setValue(startOfMonth);
                dateToPicker.setValue(today);
                break;
            case "Mois dernier":
                LocalDate lastMonth = today.minusMonths(1);
                dateFromPicker.setValue(lastMonth.withDayOfMonth(1));
                dateToPicker.setValue(lastMonth.withDayOfMonth(lastMonth.lengthOfMonth()));
                break;
            case "Cette année":
                dateFromPicker.setValue(today.withDayOfYear(1));
                dateToPicker.setValue(today);
                break;
            case "Toute période":
                dateFromPicker.setValue(LocalDate.of(2024, 1, 1));
                dateToPicker.setValue(today);
                break;
        }
    }

    private void applyFilters() {
        Notification.show("Filtres appliqués", 1000, Notification.Position.TOP_CENTER);
        refreshDashboard();
    }

    private TabSheet createDashboardTabs() {
        TabSheet tabs = new TabSheet();
        tabs.setWidthFull();
        tabs.setHeight("700px");

        // Onglet Vue d'ensemble (principal)
        VerticalLayout overviewTab = createOverviewTab();
        tabs.add("🏠 Vue d'ensemble", overviewTab);

        // Onglet Activité récente
        VerticalLayout activityTab = createActivityTab();
        tabs.add("📈 Activité récente", activityTab);

        // Onglet Événements
        VerticalLayout eventsTab = createEventsDashboardTab();
        tabs.add("🎪 Événements", eventsTab);

        // Onglet Réservations
        VerticalLayout reservationsTab = createReservationsDashboardTab();
        tabs.add("📋 Réservations", reservationsTab);

        return tabs;
    }

    private VerticalLayout createOverviewTab() {
        VerticalLayout layout = new VerticalLayout();
        layout.setPadding(true);
        layout.setSpacing(true);

        // Cartes de statistiques principales
        HorizontalLayout mainStats = createMainStatsCards();

        // Cartes de statistiques secondaires
        HorizontalLayout secondaryStats = createSecondaryStatsCards();

        // Actions rapides admin
        HorizontalLayout quickActions = createQuickActions();

        // Résumé visuel
        VerticalLayout visualSummary = createVisualSummary();

        layout.add(mainStats, secondaryStats, quickActions, visualSummary);
        return layout;
    }

    private HorizontalLayout createMainStatsCards() {
        HorizontalLayout stats = new HorizontalLayout();
        stats.setWidthFull();
        stats.setSpacing(true);

        try {
            long totalUsers = userService.getTotalUsersCount();
            long totalEvents = eventService.getTotalEventsCount();
            long totalReservations = reservationService.getTotalReservationsCount();
            double totalRevenue = reservationService.getTotalRevenue();
            long clientCount = userService.getUsersByRole(UserRole.CLIENT).size();


            stats.add(
                    createStatCard(VaadinIcon.USERS, "Utilisateurs",
                            String.valueOf(clientCount), "#7b1fa2", "admin/users?role=CLIENT"),
                    createStatCard(VaadinIcon.CALENDAR, "Événements",
                            String.valueOf(totalEvents), "#1976d2", "admin/events"),
                    createStatCard(VaadinIcon.TICKET, "Réservations",
                            String.valueOf(totalReservations), "#2e7d32", "admin/reservations"),
                    createStatCard(VaadinIcon.MONEY, "Revenu Total",
                            String.format("%.2f MAD", totalRevenue), "#d32f2f", null)
            );
        } catch (Exception e) {
            Paragraph error = new Paragraph("Erreur: " + e.getMessage());
            error.getStyle().set("color", "#d32f2f");
            stats.add(error);
        }

        return stats;
    }

    private HorizontalLayout createSecondaryStatsCards() {
        HorizontalLayout stats = new HorizontalLayout();
        stats.setWidthFull();
        stats.setSpacing(true);

        try {
            long upcomingEvents = eventService.getUpcomingEventsCount();
            long confirmedReservations = reservationService.getReservationsByStatus(ReservationStatus.CONFIRMEE).size();
            long activeUsers = userService.getAllUsers().stream().filter(User::getActif).count();
            long organizerCount = userService.getUsersByRole(UserRole.ORGANIZER).size();


            stats.add(
                    createStatCard(VaadinIcon.CLOCK, "Événements à venir",
                            String.valueOf(upcomingEvents), "#ed6c02", "admin/events"),
                    createStatCard(VaadinIcon.CHECK, "Réservations confirmées",
                            String.valueOf(confirmedReservations), "#2e7d32", "admin/reservations"),
                    createStatCard(VaadinIcon.STAR, "Organisateurs",
                            String.valueOf(organizerCount), "#7b1fa2", "admin/users?role=ORGANIZER")
            );
        } catch (Exception e) {
            stats.add(new Paragraph("Erreur: " + e.getMessage()));
        }

        return stats;
    }

    private HorizontalLayout createQuickActions() {
        HorizontalLayout actions = new HorizontalLayout();
        actions.setWidthFull();
        actions.setSpacing(true);

        actions.add(
                createActionCard(VaadinIcon.PLUS_CIRCLE, "Nouvel Événement",
                        "Créer un nouvel événement", "#1976d2", "admin/events"),
                createActionCard(VaadinIcon.USERS, "Ajouter Utilisateur",
                        "Ajouter un nouvel utilisateur", "#7b1fa2", "admin/users"),
                createActionCard(VaadinIcon.CHART, "Rapports détaillés",
                        "Voir les rapports complets", "#2e7d32", "admin/reports")

        );

        return actions;
    }

    private VerticalLayout createVisualSummary() {
        VerticalLayout section = new VerticalLayout();
        section.setWidthFull();
        section.setPadding(true);
        section.setSpacing(true);
        section.getStyle()
                .set("border", "1px solid #e0e0e0")
                .set("border-radius", "8px")
                .set("background", "white");

        H3 title = new H3("📊 Résumé Visuel");
        title.getStyle().set("margin", "0").set("color", "#1e293b");

        HorizontalLayout charts = new HorizontalLayout();
        charts.setWidthFull();
        charts.setSpacing(true);

        // Distribution des événements par catégorie
        Div categoriesDiv = createCategoryDistributionVisual();

        // Distribution des réservations par statut
        Div statusDiv = createStatusDistributionVisual();

        charts.add(categoriesDiv, statusDiv);
        section.add(title, charts);
        return section;
    }

    private VerticalLayout createActivityTab() {
        VerticalLayout layout = new VerticalLayout();
        layout.setPadding(true);
        layout.setSpacing(true);

        // Activité récente en deux colonnes
        HorizontalLayout activityColumns = new HorizontalLayout();
        activityColumns.setWidthFull();
        activityColumns.setSpacing(true);

        // Événements récents
        VerticalLayout recentEvents = createRecentEventsSection();

        // Réservations récentes
        VerticalLayout recentReservations = createRecentReservationsSection();

        activityColumns.add(recentEvents, recentReservations);

        // Top 5
        HorizontalLayout topSections = new HorizontalLayout();
        topSections.setWidthFull();
        topSections.setSpacing(true);

        // Top événements
        VerticalLayout topEvents = createPopularEventsSection();

        // Top clients
        VerticalLayout topClients = createTopClientsSection();

        topSections.add(topEvents, topClients);

        layout.add(activityColumns, topSections);
        return layout;
    }

    private VerticalLayout createEventsDashboardTab() {
        VerticalLayout layout = new VerticalLayout();
        layout.setPadding(true);
        layout.setSpacing(true);

        // Statistiques événements
        HorizontalLayout eventStats = new HorizontalLayout();
        eventStats.setWidthFull();
        eventStats.setSpacing(true);

        try {
            long publishedEvents = eventService.getAllEvents().stream()
                    .filter(e -> e.getStatut() == EventStatus.PUBLIE)
                    .count();
            long upcomingEvents = eventService.getUpcomingEventsCount();
            long draftEvents = eventService.getAllEvents().stream()
                    .filter(e -> e.getStatut() == EventStatus.BROUILLON)
                    .count();
            long cancelledEvents = eventService.getAllEvents().stream()
                    .filter(e -> e.getStatut() == EventStatus.ANNULE)
                    .count();

            eventStats.add(
                    createStatCard(VaadinIcon.BULLETS, "Publiés", String.valueOf(publishedEvents), "#1976d2", null),
                    createStatCard(VaadinIcon.CLOCK, "À venir", String.valueOf(upcomingEvents), "#2e7d32", null),
                    createStatCard(VaadinIcon.EDIT, "Brouillons", String.valueOf(draftEvents), "#666", null),
                    createStatCard(VaadinIcon.CLOSE_CIRCLE, "Annulés", String.valueOf(cancelledEvents), "#d32f2f", null)
            );
        } catch (Exception e) {
            eventStats.add(new Paragraph("Erreur: " + e.getMessage()));
        }

        // Liste complète des événements
        VerticalLayout eventsList = createEventsListSection();

        layout.add(eventStats, eventsList);
        return layout;
    }

    private VerticalLayout createReservationsDashboardTab() {
        VerticalLayout layout = new VerticalLayout();
        layout.setPadding(true);
        layout.setSpacing(true);

        // Statistiques réservations
        HorizontalLayout reservationStats = new HorizontalLayout();
        reservationStats.setWidthFull();
        reservationStats.setSpacing(true);

        try {
            long confirmedReservations = reservationService.getReservationsByStatus(ReservationStatus.CONFIRMEE).size();
            long pendingReservations = reservationService.getReservationsByStatus(ReservationStatus.EN_ATTENTE).size();
            long cancelledReservations = reservationService.getReservationsByStatus(ReservationStatus.ANNULEE).size();
            long totalReservations = reservationService.getTotalReservationsCount();

            reservationStats.add(
                    createStatCard(VaadinIcon.CHECK, "Confirmées", String.valueOf(confirmedReservations), "#2e7d32", null),
                    createStatCard(VaadinIcon.HOURGLASS, "En attente", String.valueOf(pendingReservations), "#ed6c02", null),
                    createStatCard(VaadinIcon.CLOSE, "Annulées", String.valueOf(cancelledReservations), "#d32f2f", null),
                    createStatCard(VaadinIcon.TICKET, "Total", String.valueOf(totalReservations), "#1976d2", null)
            );
        } catch (Exception e) {
            reservationStats.add(new Paragraph("Erreur: " + e.getMessage()));
        }

        // Liste complète des réservations
        VerticalLayout reservationsList = createReservationsListSection();

        layout.add(reservationStats, reservationsList);
        return layout;
    }

    // ==================== COMPOSANTS RÉUTILISABLES ====================

    private VerticalLayout createStatCard(VaadinIcon icon, String title, String value, String color, String route) {
        VerticalLayout card = new VerticalLayout();
        card.setWidth("100%");
        card.setPadding(true);
        card.setSpacing(false);
        card.getStyle()
                .set("border", "1px solid #e0e0e0")
                .set("border-radius", "8px")
                .set("box-shadow", "0 2px 4px rgba(0,0,0,0.1)")
                .set("background", "white")
                .set("text-align", "center")
                .set("cursor", route != null ? "pointer" : "default")
                .set("transition", "all 0.3s ease");

        if (route != null) {
            card.addClickListener(e -> getUI().ifPresent(ui -> ui.navigate(route)));

            card.getElement().addEventListener("mouseenter", e -> {
                card.getStyle()
                        .set("box-shadow", "0 4px 8px rgba(0,0,0,0.2)")
                        .set("transform", "translateY(-2px)");
            });

            card.getElement().addEventListener("mouseleave", e -> {
                card.getStyle()
                        .set("box-shadow", "0 2px 4px rgba(0,0,0,0.1)")
                        .set("transform", "translateY(0)");
            });
        }

        Icon iconComponent = icon.create();
        iconComponent.setSize("32px");
        iconComponent.setColor(color);

        H2 valueH2 = new H2(value);
        valueH2.getStyle()
                .set("margin", "10px 0 5px 0")
                .set("color", color)
                .set("font-size", "24px");

        Paragraph titlePara = new Paragraph(title);
        titlePara.getStyle()
                .set("margin", "0")
                .set("color", "#666")
                .set("font-size", "14px");

        card.add(iconComponent, valueH2, titlePara);
        return card;
    }

    private VerticalLayout createActionCard(VaadinIcon icon, String title, String description, String color, String route) {
        VerticalLayout card = new VerticalLayout();
        card.setWidth("100%");
        card.setPadding(true);
        card.setSpacing(true);
        card.getStyle()
                .set("border", "1px solid #e0e0e0")
                .set("border-radius", "8px")
                .set("box-shadow", "0 2px 4px rgba(0,0,0,0.1)")
                .set("background", "white")
                .set("cursor", "pointer")
                .set("transition", "all 0.3s ease");

        card.addClickListener(e -> getUI().ifPresent(ui -> ui.navigate(route)));

        card.getElement().addEventListener("mouseenter", e -> {
            card.getStyle()
                    .set("box-shadow", "0 4px 8px rgba(0,0,0,0.2)")
                    .set("border-color", color)
                    .set("transform", "translateY(-2px)");
        });

        card.getElement().addEventListener("mouseleave", e -> {
            card.getStyle()
                    .set("box-shadow", "0 2px 4px rgba(0,0,0,0.1)")
                    .set("border-color", "#e0e0e0")
                    .set("transform", "translateY(0)");
        });

        Icon iconComponent = icon.create();
        iconComponent.setSize("48px");
        iconComponent.setColor(color);

        H2 titleH2 = new H2(title);
        titleH2.getStyle()
                .set("margin", "10px 0")
                .set("color", color)
                .set("font-size", "18px");

        Paragraph descPara = new Paragraph(description);
        descPara.getStyle()
                .set("margin", "0")
                .set("color", "#666")
                .set("font-size", "14px");

        card.add(iconComponent, titleH2, descPara);
        return card;
    }

    private Div createCategoryDistributionVisual() {
        Div div = new Div();
        div.getStyle()
                .set("width", "100%")
                .set("padding", "15px")
                .set("background", "#f9f9f9")
                .set("border-radius", "6px");

        H4 title = new H4("🎪 Événements par Catégorie");
        title.getStyle().set("margin-top", "0").set("color", "#1e293b");

        Map<EventCategory, Long> categoryCounts = eventService.getAllEvents().stream()
                .collect(Collectors.groupingBy(Event::getCategorie, Collectors.counting()));

        long totalEvents = eventService.getTotalEventsCount();

        VerticalLayout bars = new VerticalLayout();
        bars.setSpacing(true);
        bars.setPadding(false);

        for (Map.Entry<EventCategory, Long> entry : categoryCounts.entrySet()) {
            String category = getCategoryLabel(entry.getKey());
            long count = entry.getValue();
            double percentage = totalEvents > 0 ? (count * 100.0 / totalEvents) : 0;

            Div barContainer = new Div();
            barContainer.getStyle()
                    .set("width", "100%")
                    .set("margin-bottom", "10px");

            HorizontalLayout labelRow = new HorizontalLayout();
            labelRow.setWidthFull();
            labelRow.setJustifyContentMode(JustifyContentMode.BETWEEN);

            Span label = new Span(category);
            Span perc = new Span(String.format("%.1f%% (%d)", percentage, count));
            perc.getStyle().set("color", "#666").set("font-size", "12px");

            labelRow.add(label, perc);

            Div progressBar = new Div();
            progressBar.getStyle()
                    .set("width", "100%")
                    .set("height", "8px")
                    .set("background", "#e0e0e0")
                    .set("border-radius", "4px")
                    .set("overflow", "hidden");

            Div progressFill = new Div();
            progressFill.getStyle()
                    .set("width", percentage + "%")
                    .set("height", "100%")
                    .set("background", "#1976d2")
                    .set("border-radius", "4px");

            progressBar.add(progressFill);
            barContainer.add(labelRow, progressBar);
            bars.add(barContainer);
        }

        div.add(title, bars);
        return div;
    }

    private Div createStatusDistributionVisual() {
        Div div = new Div();
        div.getStyle()
                .set("width", "100%")
                .set("padding", "15px")
                .set("background", "#f9f9f9")
                .set("border-radius", "6px");

        H4 title = new H4("📋 Réservations par Statut");
        title.getStyle().set("margin-top", "0").set("color", "#1e293b");

        Map<ReservationStatus, Long> statusCounts = reservationService.getAllReservations().stream()
                .collect(Collectors.groupingBy(Reservation::getStatut, Collectors.counting()));

        long totalReservations = reservationService.getTotalReservationsCount();

        VerticalLayout bars = new VerticalLayout();
        bars.setSpacing(true);
        bars.setPadding(false);

        for (Map.Entry<ReservationStatus, Long> entry : statusCounts.entrySet()) {
            String status = getReservationStatusLabel(entry.getKey());
            long count = entry.getValue();
            double percentage = totalReservations > 0 ? (count * 100.0 / totalReservations) : 0;

            Div barContainer = new Div();
            barContainer.getStyle()
                    .set("width", "100%")
                    .set("margin-bottom", "10px");

            HorizontalLayout labelRow = new HorizontalLayout();
            labelRow.setWidthFull();
            labelRow.setJustifyContentMode(JustifyContentMode.BETWEEN);

            Span label = new Span(status);
            Span perc = new Span(String.format("%.1f%% (%d)", percentage, count));
            perc.getStyle().set("color", "#666").set("font-size", "12px");

            labelRow.add(label, perc);

            Div progressBar = new Div();
            progressBar.getStyle()
                    .set("width", "100%")
                    .set("height", "8px")
                    .set("background", "#e0e0e0")
                    .set("border-radius", "4px")
                    .set("overflow", "hidden");

            Div progressFill = new Div();
            String color = getReservationStatusColor(entry.getKey());
            progressFill.getStyle()
                    .set("width", percentage + "%")
                    .set("height", "100%")
                    .set("background", color)
                    .set("border-radius", "4px");

            progressBar.add(progressFill);
            barContainer.add(labelRow, progressBar);
            bars.add(barContainer);
        }

        div.add(title, bars);
        return div;
    }

    private VerticalLayout createRecentEventsSection() {
        VerticalLayout section = new VerticalLayout();
        section.setWidthFull();
        section.setPadding(true);
        section.setSpacing(true);
        section.getStyle()
                .set("border", "1px solid #e0e0e0")
                .set("border-radius", "8px")
                .set("background", "white");

        HorizontalLayout header = new HorizontalLayout();
        header.setWidthFull();
        header.setJustifyContentMode(JustifyContentMode.BETWEEN);
        header.setAlignItems(Alignment.CENTER);

        H3 title = new H3("📅 Événements Récents");
        title.getStyle().set("margin", "0");

        Button viewAll = new Button("Voir tout", VaadinIcon.ARROW_RIGHT.create());
        viewAll.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL);
        viewAll.addClickListener(e -> getUI().ifPresent(ui -> ui.navigate("admin/events")));

        header.add(title, viewAll);

        Grid<Event> grid = new Grid<>(Event.class, false);
        grid.setWidthFull();
        grid.setHeight("300px");

        grid.addColumn(Event::getTitre)
                .setHeader("Titre")
                .setWidth("200px");

        grid.addColumn(event -> getCategoryLabel(event.getCategorie()))
                .setHeader("Catégorie");

        grid.addColumn(event -> event.getDateDebut() != null
                        ? event.getDateDebut().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                        : "")
                .setHeader("Date")
                .setWidth("120px");

        grid.addColumn(event -> getStatusLabel(event.getStatut()))
                .setHeader("Statut");

        List<Event> recentEvents = eventService.getAllEvents().stream()
                .sorted((e1, e2) -> {
                    if (e1.getDateCreation() == null) return 1;
                    if (e2.getDateCreation() == null) return -1;
                    return e2.getDateCreation().compareTo(e1.getDateCreation());
                })
                .limit(10)
                .collect(Collectors.toList());

        grid.setItems(recentEvents);
        section.add(header, grid);
        return section;
    }

    private VerticalLayout createRecentReservationsSection() {
        VerticalLayout section = new VerticalLayout();
        section.setWidthFull();
        section.setPadding(true);
        section.setSpacing(true);
        section.getStyle()
                .set("border", "1px solid #e0e0e0")
                .set("border-radius", "8px")
                .set("background", "white");

        HorizontalLayout header = new HorizontalLayout();
        header.setWidthFull();
        header.setJustifyContentMode(JustifyContentMode.BETWEEN);
        header.setAlignItems(Alignment.CENTER);

        H3 title = new H3("📋 Réservations Récentes");
        title.getStyle().set("margin", "0");

        Button viewAll = new Button("Voir tout", VaadinIcon.ARROW_RIGHT.create());
        viewAll.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL);
        viewAll.addClickListener(e -> getUI().ifPresent(ui -> ui.navigate("admin/reservations")));

        header.add(title, viewAll);

        Grid<Reservation> grid = new Grid<>(Reservation.class, false);
        grid.setWidthFull();
        grid.setHeight("300px");

        grid.addColumn(reservation -> reservation.getUtilisateur().getPrenom() + " " +
                        reservation.getUtilisateur().getNom())
                .setHeader("Client");

        grid.addColumn(reservation -> reservation.getEvenement().getTitre())
                .setHeader("Événement");

        grid.addColumn(Reservation::getNombrePlaces)
                .setHeader("Places");

        grid.addColumn(reservation -> reservation.getMontantTotal() + " MAD")
                .setHeader("Montant");

        grid.addColumn(reservation -> getReservationStatusLabel(reservation.getStatut()))
                .setHeader("Statut");

        List<Reservation> recentReservations = reservationService.getAllReservations().stream()
                .sorted((r1, r2) -> {
                    if (r1.getDateReservation() == null) return 1;
                    if (r2.getDateReservation() == null) return -1;
                    return r2.getDateReservation().compareTo(r1.getDateReservation());
                })
                .limit(10)
                .collect(Collectors.toList());

        grid.setItems(recentReservations);
        section.add(header, grid);
        return section;
    }

    private VerticalLayout createPopularEventsSection() {
        VerticalLayout section = new VerticalLayout();
        section.setWidthFull();
        section.setPadding(true);
        section.setSpacing(true);
        section.getStyle()
                .set("border", "1px solid #e0e0e0")
                .set("border-radius", "8px")
                .set("background", "white");

        H3 title = new H3("🏆 Top 5 Événements Populaires");
        title.getStyle().set("margin", "0").set("color", "#1e293b");

        List<Event> popularEvents = eventService.getAllEvents().stream()
                .filter(e -> e.getStatut() == EventStatus.PUBLIE)
                .sorted((e1, e2) -> {
                    int r1 = reservationService.getReservationsByEvent(e1.getId()).size();
                    int r2 = reservationService.getReservationsByEvent(e2.getId()).size();
                    return Integer.compare(r2, r1);
                })
                .limit(5)
                .collect(Collectors.toList());

        Div list = new Div();
        list.getStyle().set("width", "100%");

        int rank = 1;
        for (Event event : popularEvents) {
            int reservationCount = reservationService.getReservationsByEvent(event.getId()).size();
            double fillRate = event.getCapaciteMax() > 0 ?
                    (reservationCount * 100.0 / event.getCapaciteMax()) : 0;

            Div item = new Div();
            item.getStyle()
                    .set("display", "flex")
                    .set("justify-content", "space-between")
                    .set("align-items", "center")
                    .set("padding", "10px")
                    .set("border-bottom", "1px solid #eee");

            Div left = new Div();
            Span rankSpan = new Span(rank + ". ");
            rankSpan.getStyle().set("font-weight", "bold").set("color", "#1976d2");
            Span nameSpan = new Span(event.getTitre());
            nameSpan.getStyle().set("margin-left", "5px");
            left.add(rankSpan, nameSpan);

            Div right = new Div();
            Span countSpan = new Span(reservationCount + " rés.");
            countSpan.getStyle().set("margin-right", "10px").set("color", "#666");
            Span rateSpan = new Span(String.format("%.0f%%", fillRate));
            rateSpan.getStyle().set("color", fillRate > 80 ? "#2e7d32" : "#ed6c02");
            right.add(countSpan, rateSpan);

            item.add(left, right);
            list.add(item);
            rank++;
        }

        section.add(title, list);
        return section;
    }

    private VerticalLayout createTopClientsSection() {
        VerticalLayout section = new VerticalLayout();
        section.setWidthFull();
        section.setPadding(true);
        section.setSpacing(true);
        section.getStyle()
                .set("border", "1px solid #e0e0e0")
                .set("border-radius", "8px")
                .set("background", "white");

        H3 title = new H3("👑 Top 5 Clients");
        title.getStyle().set("margin", "0").set("color", "#1e293b");

        List<User> topClients = userService.getAllUsers().stream()
                .filter(u -> u.getRole() == UserRole.CLIENT)
                .sorted((u1, u2) -> {
                    long r1 = reservationService.getUserReservationsCount(u1.getId());
                    long r2 = reservationService.getUserReservationsCount(u2.getId());
                    return Long.compare(r2, r1);
                })
                .limit(5)
                .collect(Collectors.toList());

        Div list = new Div();
        list.getStyle().set("width", "100%");

        int rank = 1;
        for (User user : topClients) {
            long reservationCount = reservationService.getUserReservationsCount(user.getId());

            Div item = new Div();
            item.getStyle()
                    .set("display", "flex")
                    .set("justify-content", "space-between")
                    .set("align-items", "center")
                    .set("padding", "10px")
                    .set("border-bottom", "1px solid #eee");

            Div left = new Div();
            Span rankSpan = new Span(rank + ". ");
            rankSpan.getStyle().set("font-weight", "bold").set("color", "#1976d2");
            Span nameSpan = new Span(user.getPrenom() + " " + user.getNom());
            nameSpan.getStyle().set("margin-left", "5px");
            left.add(rankSpan, nameSpan);

            Div right = new Div();
            Span countSpan = new Span(reservationCount + " réservations");
            countSpan.getStyle().set("color", "#666");
            right.add(countSpan);

            item.add(left, right);
            list.add(item);
            rank++;
        }

        section.add(title, list);
        return section;
    }

    private VerticalLayout createEventsListSection() {
        VerticalLayout section = new VerticalLayout();
        section.setWidthFull();
        section.setPadding(true);
        section.setSpacing(true);
        section.getStyle()
                .set("border", "1px solid #e0e0e0")
                .set("border-radius", "8px")
                .set("background", "white");

        HorizontalLayout header = new HorizontalLayout();
        header.setWidthFull();
        header.setJustifyContentMode(JustifyContentMode.BETWEEN);
        header.setAlignItems(Alignment.CENTER);

        H3 title = new H3("🎪 Tous les Événements");
        title.getStyle().set("margin", "0");

        Button newEvent = new Button("Nouvel événement", VaadinIcon.PLUS.create());
        newEvent.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        newEvent.addClickListener(e -> getUI().ifPresent(ui -> ui.navigate("admin/events")));

        header.add(title, newEvent);

        Grid<Event> grid = new Grid<>(Event.class, false);
        grid.setWidthFull();
        grid.setHeight("500px");

        grid.addColumn(Event::getTitre)
                .setHeader("Titre")
                .setWidth("250px");

        grid.addColumn(event -> getCategoryLabel(event.getCategorie()))
                .setHeader("Catégorie");

        grid.addColumn(event -> event.getDateDebut() != null
                        ? event.getDateDebut().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
                        : "")
                .setHeader("Date début");

        grid.addColumn(event -> getStatusLabel(event.getStatut()))
                .setHeader("Statut");

        grid.addColumn(Event::getCapaciteMax)
                .setHeader("Capacité");

        grid.addColumn(new ComponentRenderer<>(event -> {
            int reservationCount = reservationService.getReservationsByEvent(event.getId()).size();
            double fillRate = event.getCapaciteMax() > 0 ?
                    (reservationCount * 100.0 / event.getCapaciteMax()) : 0;

            Div div = new Div();
            div.getStyle().set("display", "flex").set("align-items", "center");

            Span count = new Span(reservationCount + "/" + event.getCapaciteMax());
            count.getStyle().set("margin-right", "10px");

            Div progress = new Div();
            progress.getStyle()
                    .set("width", "60px")
                    .set("height", "8px")
                    .set("background", "#e0e0e0")
                    .set("border-radius", "4px")
                    .set("overflow", "hidden");

            Div fill = new Div();
            fill.getStyle()
                    .set("width", fillRate + "%")
                    .set("height", "100%")
                    .set("background", fillRate > 80 ? "#2e7d32" : "#ed6c02")
                    .set("border-radius", "4px");

            progress.add(fill);
            div.add(count, progress);
            return div;
        })).setHeader("Remplissage");

        List<Event> allEvents = eventService.getAllEvents();
        grid.setItems(allEvents);

        section.add(header, grid);
        return section;
    }

    private VerticalLayout createReservationsListSection() {
        VerticalLayout section = new VerticalLayout();
        section.setWidthFull();
        section.setPadding(true);
        section.setSpacing(true);
        section.getStyle()
                .set("border", "1px solid #e0e0e0")
                .set("border-radius", "8px")
                .set("background", "white");

        H3 title = new H3("📋 Toutes les Réservations");
        title.getStyle().set("margin", "0");

        Grid<Reservation> grid = new Grid<>(Reservation.class, false);
        grid.setWidthFull();
        grid.setHeight("500px");

        grid.addColumn(reservation -> reservation.getUtilisateur().getPrenom() + " " +
                        reservation.getUtilisateur().getNom())
                .setHeader("Client");

        grid.addColumn(reservation -> reservation.getEvenement().getTitre())
                .setHeader("Événement")
                .setWidth("200px");

        grid.addColumn(Reservation::getNombrePlaces)
                .setHeader("Places");

        grid.addColumn(reservation -> reservation.getMontantTotal() + " MAD")
                .setHeader("Montant");

        grid.addColumn(new ComponentRenderer<>(reservation -> {
            Span badge = new Span(getReservationStatusLabel(reservation.getStatut()));
            badge.getStyle()
                    .set("background", getReservationStatusColor(reservation.getStatut()))
                    .set("color", "white")
                    .set("padding", "3px 10px")
                    .set("border-radius", "12px")
                    .set("font-size", "12px");
            return badge;
        })).setHeader("Statut");

        grid.addColumn(reservation -> reservation.getDateReservation() != null
                        ? reservation.getDateReservation().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
                        : "")
                .setHeader("Date réservation");

        List<Reservation> allReservations = reservationService.getAllReservations();
        grid.setItems(allReservations);

        section.add(title, grid);
        return section;
    }

    // ==================== MÉTHODES UTILITAIRES ====================

    private void refreshDashboard() {
        Notification.show("Dashboard actualisé", 1000, Notification.Position.TOP_CENTER);
        // On pourrait recharger les données ici si nécessaire
    }

    private String getCategoryLabel(EventCategory category) {
        return switch (category) {
            case CONCERT -> "🎵 Concert";
            case THEATRE -> "🎭 Théâtre";
            case CONFERENCE -> "🎤 Conférence";
            case SPORT -> "⚽ Sport";
            case AUTRE -> "📌 Autre";
        };
    }

    private String getStatusLabel(EventStatus status) {
        return switch (status) {
            case BROUILLON -> "📝 Brouillon";
            case PUBLIE -> "✅ Publié";
            case ANNULE -> "❌ Annulé";
            case TERMINE -> "🏁 Terminé";
        };
    }

    private String getReservationStatusLabel(ReservationStatus status) {
        return switch (status) {
            case EN_ATTENTE -> "⏳ En attente";
            case CONFIRMEE -> "✅ Confirmée";
            case ANNULEE -> "❌ Annulée";
        };
    }

    private String getReservationStatusColor(ReservationStatus status) {
        return switch (status) {
            case EN_ATTENTE -> "#ed6c02";
            case CONFIRMEE -> "#2e7d32";
            case ANNULEE -> "#d32f2f";
        };
    }

    private String getRoleLabel(UserRole role) {
        return switch (role) {
            case ADMIN -> "👑 Admin";
            case ORGANIZER -> "🎪 Organisateur";
            case CLIENT -> "👤 Client";
        };
    }
}