package com.eventbooking.view.admin;

import com.eventbooking.entity.Event;
import com.eventbooking.entity.Reservation;
import com.eventbooking.entity.User;
import com.eventbooking.entity.enums.*;
import com.eventbooking.service.EventService;
import com.eventbooking.service.ReservationService;
import com.eventbooking.service.UserService;
import com.eventbooking.view.admin.components.AdminSidebar;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.charts.Chart;
import com.vaadin.flow.component.charts.model.*;
import com.vaadin.flow.component.charts.model.style.*;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.TabSheet;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;
import jakarta.annotation.security.PermitAll;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Route("admin/reports")
@PageTitle("Rapports & Statistiques | Event Booking Admin")
@PermitAll
public class AdminReportsView extends HorizontalLayout {

    @Autowired
    private EventService eventService;

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private UserService userService;

    private User currentUser;
    private DatePicker dateFromPicker;
    private DatePicker dateToPicker;
    private ComboBox<String> periodComboBox;
    private TabSheet tabs;
    private LocalDate filterStartDate;
    private LocalDate filterEndDate;

    // Couleurs pour les graphiques
    private static final Color[] CHART_COLORS = {
            new SolidColor("#1976d2"),
            new SolidColor("#2e7d32"),
            new SolidColor("#ed6c02"),
            new SolidColor("#7b1fa2"),
            new SolidColor("#d32f2f"),
            new SolidColor("#666"),
            new SolidColor("#0288d1"),
            new SolidColor("#388e3c"),
            new SolidColor("#f57c00"),
            new SolidColor("#5d4037"),
            new SolidColor("#455a64"),
            new SolidColor("#d81b60")
    };

    public AdminReportsView(EventService eventService,
                            ReservationService reservationService,
                            UserService userService) {
        this.eventService = eventService;
        this.reservationService = reservationService;
        this.userService = userService;

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
    }

    private void createUI() {
        AdminSidebar sidebar = new AdminSidebar(currentUser, "admin/reports");
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
        tabs = createTabSheet();

        content.add(header, filters, tabs);
        return content;
    }

    private HorizontalLayout createHeader() {
        HorizontalLayout header = new HorizontalLayout();
        header.setWidthFull();
        header.setJustifyContentMode(JustifyContentMode.BETWEEN);
        header.setAlignItems(Alignment.CENTER);

        H1 title = new H1("📊 Rapports & Statistiques");
        title.getStyle().set("margin", "0").set("color", "#1976d2");

        HorizontalLayout actions = new HorizontalLayout();
        actions.setSpacing(true);

        Button backBtn = new Button("← Dashboard");
        backBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        backBtn.addClickListener(e -> UI.getCurrent().navigate("admin/dashboard"));

        Button refreshBtn = new Button("🔄 Actualiser");
        refreshBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        refreshBtn.addClickListener(e -> refreshData());

        actions.add(backBtn, refreshBtn);
        header.add(title, actions);

        return header;
    }

    private HorizontalLayout createFilters() {
        HorizontalLayout filters = new HorizontalLayout();
        filters.setWidthFull();
        filters.setSpacing(true);
        filters.setAlignItems(Alignment.END);

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

        dateFromPicker = new DatePicker("Du");
        dateFromPicker.setWidth("150px");

        dateToPicker = new DatePicker("Au");
        dateToPicker.setWidth("150px");

        updateDateRange();

        Button applyBtn = new Button("Appliquer");
        applyBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
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

    private TabSheet createTabSheet() {
        TabSheet tabs = new TabSheet();
        tabs.setWidthFull();
        tabs.setHeight("700px");

        tabs.add("📈 Vue d'ensemble", createOverviewTab());
        tabs.add("🎪 Événements", createEventsTab());
        tabs.add("📋 Réservations", createReservationsTab());
        tabs.add("👥 Utilisateurs", createUsersTab());
        tabs.add("💰 Revenus", createRevenueTab());

        return tabs;
    }

    // ==================== MÉTHODES POUR LES ONGLETS ====================

    private VerticalLayout createOverviewTab() {
        VerticalLayout layout = new VerticalLayout();
        layout.setPadding(true);
        layout.setSpacing(true);

        HorizontalLayout mainStats = createMainStatsCards();

        HorizontalLayout chartsRow1 = new HorizontalLayout();
        chartsRow1.setWidthFull();
        chartsRow1.setSpacing(true);

        VerticalLayout chartContainer1 = new VerticalLayout();
        chartContainer1.setPadding(false);
        chartContainer1.setSpacing(false);
        chartContainer1.setWidth("50%");
        chartContainer1.add(createBarChart("🎪 Événements par Catégorie", getEventsByCategoryData()));

        VerticalLayout chartContainer2 = new VerticalLayout();
        chartContainer2.setPadding(false);
        chartContainer2.setSpacing(false);
        chartContainer2.setWidth("50%");
        chartContainer2.add(createPieChart("📋 Réservations par Statut", getReservationsByStatusData()));

        chartsRow1.add(chartContainer1, chartContainer2);

        VerticalLayout chartContainer3 = new VerticalLayout();
        chartContainer3.setPadding(false);
        chartContainer3.setSpacing(false);
        chartContainer3.setWidthFull();
        chartContainer3.add(createLineChart("📈 Évolution des Inscriptions", getUserGrowthData()));

        layout.add(mainStats, chartsRow1, chartContainer3);
        return layout;
    }

    private VerticalLayout createEventsTab() {
        VerticalLayout layout = new VerticalLayout();
        layout.setPadding(true);
        layout.setSpacing(true);

        HorizontalLayout eventStats = createEventStatsCards();

        HorizontalLayout eventCharts = new HorizontalLayout();
        eventCharts.setWidthFull();
        eventCharts.setSpacing(true);

        VerticalLayout chartContainer1 = new VerticalLayout();
        chartContainer1.setPadding(false);
        chartContainer1.setSpacing(false);
        chartContainer1.setWidth("50%");
        chartContainer1.add(createPieChart("📊 Événements par Statut", getEventsByStatusData()));

        VerticalLayout chartContainer2 = new VerticalLayout();
        chartContainer2.setPadding(false);
        chartContainer2.setSpacing(false);
        chartContainer2.setWidth("50%");
        chartContainer2.add(createBarChart("📅 Événements par Mois", getEventsByMonthData()));

        eventCharts.add(chartContainer1, chartContainer2);
        layout.add(eventStats, eventCharts);
        return layout;
    }

    private VerticalLayout createReservationsTab() {
        VerticalLayout layout = new VerticalLayout();
        layout.setPadding(true);
        layout.setSpacing(true);

        HorizontalLayout reservationStats = createReservationStatsCards();

        HorizontalLayout reservationCharts = new HorizontalLayout();
        reservationCharts.setWidthFull();
        reservationCharts.setSpacing(true);

        VerticalLayout chartContainer1 = new VerticalLayout();
        chartContainer1.setPadding(false);
        chartContainer1.setSpacing(false);
        chartContainer1.setWidth("50%");
        chartContainer1.add(createBarChart("📅 Réservations par Jour", getReservationsByDayData()));

        VerticalLayout chartContainer2 = new VerticalLayout();
        chartContainer2.setPadding(false);
        chartContainer2.setSpacing(false);
        chartContainer2.setWidth("50%");
        chartContainer2.add(createLineChart("📈 Tendance des Réservations", getReservationTrendData()));

        reservationCharts.add(chartContainer1, chartContainer2);
        layout.add(reservationStats, reservationCharts);
        return layout;
    }

    private VerticalLayout createUsersTab() {
        VerticalLayout layout = new VerticalLayout();
        layout.setPadding(true);
        layout.setSpacing(true);

        HorizontalLayout userStats = createUserStatsCards();

        HorizontalLayout userCharts = new HorizontalLayout();
        userCharts.setWidthFull();
        userCharts.setSpacing(true);

        VerticalLayout chartContainer1 = new VerticalLayout();
        chartContainer1.setPadding(false);
        chartContainer1.setSpacing(false);
        chartContainer1.setWidth("50%");
        chartContainer1.add(createPieChart("👥 Distribution par Rôle", getRoleDistributionData()));

        VerticalLayout chartContainer2 = new VerticalLayout();
        chartContainer2.setPadding(false);
        chartContainer2.setSpacing(false);
        chartContainer2.setWidth("50%");
        chartContainer2.add(createBarChart("📈 Nouveaux Utilisateurs", getNewUsersData()));

        userCharts.add(chartContainer1, chartContainer2);
        layout.add(userStats, userCharts);
        return layout;
    }

    private VerticalLayout createRevenueTab() {
        VerticalLayout layout = new VerticalLayout();
        layout.setPadding(true);
        layout.setSpacing(true);

        HorizontalLayout revenueStats = createRevenueStatsCards();

        HorizontalLayout revenueCharts = new HorizontalLayout();
        revenueCharts.setWidthFull();
        revenueCharts.setSpacing(true);

        VerticalLayout chartContainer1 = new VerticalLayout();
        chartContainer1.setPadding(false);
        chartContainer1.setSpacing(false);
        chartContainer1.setWidth("50%");
        chartContainer1.add(createBarChart("💰 Revenus par Catégorie (MAD)", getRevenueByCategoryData()));

        VerticalLayout chartContainer2 = new VerticalLayout();
        chartContainer2.setPadding(false);
        chartContainer2.setSpacing(false);
        chartContainer2.setWidth("50%");
        chartContainer2.add(createLineChart("📈 Évolution des Revenus (MAD)", getRevenueTrendData()));

        revenueCharts.add(chartContainer1, chartContainer2);
        layout.add(revenueStats, revenueCharts);
        return layout;
    }

    // ==================== MÉTHODES POUR LES GRAPHIQUES VAADIN ====================

    private Chart createBarChart(String title, Map<String, Number> data) {
        Chart chart = new Chart(ChartType.COLUMN);
        chart.setWidthFull();
        chart.setHeight("300px");

        Configuration conf = chart.getConfiguration();
        conf.setTitle(title);

        XAxis xAxis = new XAxis();
        xAxis.setCategories(data.keySet().toArray(new String[0]));
        xAxis.setCrosshair(new Crosshair());
        conf.addxAxis(xAxis);

        YAxis yAxis = new YAxis();
        yAxis.setTitle("Nombre");
        yAxis.setMin(0);
        conf.addyAxis(yAxis);

        ListSeries series = new ListSeries("Données");

        for (Map.Entry<String, Number> entry : data.entrySet()) {
            series.addData(entry.getValue());
        }

        conf.addSeries(series);

        // Configuration des barres
        PlotOptionsColumn plotOptions = new PlotOptionsColumn();
        plotOptions.setBorderRadius(4);
        plotOptions.setGroupPadding(0.1);
        plotOptions.setPointPadding(0.2);

        // Définir les couleurs pour chaque point
        series.setPlotOptions(plotOptions);

        // Ajouter des tooltips
        Tooltip tooltip = new Tooltip();
        tooltip.setHeaderFormat("<span style=\"font-size:10px\">{point.key}</span><table>");
        tooltip.setPointFormat("<tr><td style=\"color:{series.color};padding:0\">{series.name}: </td>" +
                "<td style=\"padding:0\"><b>{point.y}</b></td></tr>");
        tooltip.setFooterFormat("</table>");
        tooltip.setShared(true);
        tooltip.setUseHTML(true);
        conf.setTooltip(tooltip);

        // Personnaliser l'apparence
        conf.getChart().setStyledMode(false);

        return chart;
    }

    private Chart createPieChart(String title, Map<String, Number> data) {
        Chart chart = new Chart(ChartType.PIE);
        chart.setWidthFull();
        chart.setHeight("300px");

        Configuration conf = chart.getConfiguration();
        conf.setTitle(title);

        PlotOptionsPie plotOptions = new PlotOptionsPie();
        plotOptions.setAllowPointSelect(true);
        plotOptions.setCursor(Cursor.POINTER);
        plotOptions.setShowInLegend(true);

        DataSeries series = new DataSeries();
        int colorIndex = 0;
        for (Map.Entry<String, Number> entry : data.entrySet()) {
            DataSeriesItem item = new DataSeriesItem(entry.getKey(), entry.getValue());
            item.setColor(CHART_COLORS[colorIndex % CHART_COLORS.length]);
            series.add(item);
            colorIndex++;
        }

        series.setPlotOptions(plotOptions);
        conf.addSeries(series);

        // Ajouter des tooltips
        Tooltip tooltip = new Tooltip();
        tooltip.setPointFormat("{point.name}: <b>{point.y}</b> ({point.percentage:.1f}%)");
        conf.setTooltip(tooltip);

        // Personnaliser l'apparence
        conf.getChart().setStyledMode(false);

        return chart;
    }

    private Chart createLineChart(String title, Map<String, Number> data) {
        Chart chart = new Chart(ChartType.LINE);
        chart.setWidthFull();
        chart.setHeight("300px");

        Configuration conf = chart.getConfiguration();
        conf.setTitle(title);

        XAxis xAxis = new XAxis();
        xAxis.setCategories(data.keySet().toArray(new String[0]));
        xAxis.setCrosshair(new Crosshair());
        conf.addxAxis(xAxis);

        YAxis yAxis = new YAxis();
        yAxis.setTitle("Valeur");
        yAxis.setMin(0);
        conf.addyAxis(yAxis);

        ListSeries series = new ListSeries("Évolution");
        for (Map.Entry<String, Number> entry : data.entrySet()) {
            series.addData(entry.getValue());
        }

        // Style de la ligne
        PlotOptionsLine plotOptions = new PlotOptionsLine();
        plotOptions.setMarker(new Marker(true));
        plotOptions.setColor(new SolidColor("#1976d2"));
        series.setPlotOptions(plotOptions);

        conf.addSeries(series);

        // Ajouter des tooltips
        Tooltip tooltip = new Tooltip();
        tooltip.setShared(true);
        conf.setTooltip(tooltip);

        // Supprimer la légende
        Legend legend = new Legend();
        legend.setEnabled(false);
        conf.setLegend(legend);

        // Personnaliser l'apparence
        conf.getChart().setStyledMode(false);

        return chart;
    }

    // ==================== DONNÉES POUR LES GRAPHIQUES ====================

    private Map<String, Number> getEventsByCategoryData() {
        Map<EventCategory, Long> categoryCounts = eventService.getAllEvents().stream()
                .filter(e -> isEventInDateRange(e))
                .collect(Collectors.groupingBy(Event::getCategorie, Collectors.counting()));

        Map<String, Number> result = new LinkedHashMap<>();
        for (Map.Entry<EventCategory, Long> entry : categoryCounts.entrySet()) {
            result.put(getCategoryLabel(entry.getKey()), entry.getValue());
        }
        return result;
    }

    private Map<String, Number> getReservationsByStatusData() {
        Map<ReservationStatus, Long> statusCounts = reservationService.getAllReservations().stream()
                .filter(r -> isReservationInDateRange(r))
                .collect(Collectors.groupingBy(Reservation::getStatut, Collectors.counting()));

        Map<String, Number> result = new LinkedHashMap<>();
        for (Map.Entry<ReservationStatus, Long> entry : statusCounts.entrySet()) {
            result.put(getReservationStatusLabel(entry.getKey()), entry.getValue());
        }
        return result;
    }

    private Map<String, Number> getUserGrowthData() {
        Map<String, Number> growthData = new LinkedHashMap<>();
        LocalDate today = LocalDate.now();
        for (int i = 5; i >= 0; i--) {
            LocalDate month = today.minusMonths(i);
            String monthKey = month.getMonth().toString().substring(0, 3);

            long count = userService.getAllUsers().stream()
                    .filter(u -> {
                        if (u.getDateInscription() == null) return false;
                        LocalDate inscriptionDate = u.getDateInscription().toLocalDate();
                        return inscriptionDate.getMonth() == month.getMonth() &&
                                inscriptionDate.getYear() == month.getYear();
                    })
                    .count();

            growthData.put(monthKey, count);
        }
        return growthData;
    }

    private Map<String, Number> getEventsByStatusData() {
        Map<EventStatus, Long> statusCounts = eventService.getAllEvents().stream()
                .filter(e -> isEventInDateRange(e))
                .collect(Collectors.groupingBy(Event::getStatut, Collectors.counting()));

        Map<String, Number> result = new LinkedHashMap<>();
        for (Map.Entry<EventStatus, Long> entry : statusCounts.entrySet()) {
            result.put(getStatusLabel(entry.getKey()), entry.getValue());
        }
        return result;
    }

    private Map<String, Number> getEventsByMonthData() {
        Map<String, Number> result = new LinkedHashMap<>();
        LocalDate today = LocalDate.now();

        for (int i = 5; i >= 0; i--) {
            LocalDate month = today.minusMonths(i);
            String monthKey = month.getMonth().toString().substring(0, 3);

            long count = eventService.getAllEvents().stream()
                    .filter(e -> {
                        if (e.getDateCreation() == null) return false;
                        LocalDate creationDate = e.getDateCreation().toLocalDate();
                        return creationDate.getMonth() == month.getMonth() &&
                                creationDate.getYear() == month.getYear();
                    })
                    .count();

            result.put(monthKey, count);
        }
        return result;
    }

    private Map<String, Number> getReservationsByDayData() {
        Map<String, Number> result = new LinkedHashMap<>();
        String[] days = {"Lun", "Mar", "Mer", "Jeu", "Ven", "Sam", "Dim"};

        List<Reservation> filteredReservations = reservationService.getAllReservations().stream()
                .filter(r -> isReservationInDateRange(r))
                .toList();

        // Initialiser toutes les journées à 0
        for (String day : days) {
            result.put(day, 0L);
        }

        // Compter par jour
        for (Reservation reservation : filteredReservations) {
            if (reservation.getDateReservation() != null) {
                String dayOfWeek = reservation.getDateReservation().getDayOfWeek().toString();
                String frenchDay = getFrenchDay(dayOfWeek);
                if (result.containsKey(frenchDay)) {
                    result.put(frenchDay, result.get(frenchDay).longValue() + 1);
                }
            }
        }

        return result;
    }

    private Map<String, Number> getReservationTrendData() {
        Map<String, Number> result = new LinkedHashMap<>();
        LocalDate today = LocalDate.now();

        for (int i = 5; i >= 0; i--) {
            LocalDate weekStart = today.minusWeeks(i);
            String weekKey = "Sem " + (6 - i);

            long count = reservationService.getAllReservations().stream()
                    .filter(r -> {
                        if (r.getDateReservation() == null) return false;
                        LocalDate reservationDate = r.getDateReservation().toLocalDate();
                        return !reservationDate.isBefore(weekStart.minusDays(6)) &&
                                !reservationDate.isAfter(weekStart);
                    })
                    .count();

            result.put(weekKey, count);
        }
        return result;
    }

    private Map<String, Number> getRoleDistributionData() {
        Map<UserRole, Long> roleCounts = userService.getAllUsers().stream()
                .filter(u -> isUserInDateRange(u))
                .collect(Collectors.groupingBy(User::getRole, Collectors.counting()));

        Map<String, Number> result = new LinkedHashMap<>();
        for (Map.Entry<UserRole, Long> entry : roleCounts.entrySet()) {
            result.put(getRoleLabel(entry.getKey()), entry.getValue());
        }
        return result;
    }

    private Map<String, Number> getNewUsersData() {
        Map<String, Number> result = new LinkedHashMap<>();
        LocalDate today = LocalDate.now();

        for (int i = 5; i >= 0; i--) {
            LocalDate month = today.minusMonths(i);
            String monthKey = month.getMonth().toString().substring(0, 3);

            long count = userService.getAllUsers().stream()
                    .filter(u -> {
                        if (u.getDateInscription() == null) return false;
                        LocalDate inscriptionDate = u.getDateInscription().toLocalDate();
                        return inscriptionDate.getMonth() == month.getMonth() &&
                                inscriptionDate.getYear() == month.getYear();
                    })
                    .count();

            result.put(monthKey, count);
        }
        return result;
    }

    private Map<String, Number> getRevenueByCategoryData() {
        Map<String, Number> result = new LinkedHashMap<>();

        List<Reservation> confirmedReservations = reservationService.getReservationsByStatus(ReservationStatus.CONFIRMEE);

        for (EventCategory category : EventCategory.values()) {
            double categoryRevenue = confirmedReservations.stream()
                    .filter(r -> r.getEvenement() != null &&
                            r.getEvenement().getCategorie() == category &&
                            isReservationInDateRange(r))
                    .mapToDouble(r -> r.getMontantTotal() != null ? r.getMontantTotal() : 0)
                    .sum();

            result.put(getCategoryLabel(category), Math.round(categoryRevenue * 100.0) / 100.0);
        }

        return result;
    }

    private Map<String, Number> getRevenueTrendData() {
        Map<String, Number> result = new LinkedHashMap<>();
        LocalDate today = LocalDate.now();

        List<Reservation> confirmedReservations = reservationService.getReservationsByStatus(ReservationStatus.CONFIRMEE);

        for (int i = 5; i >= 0; i--) {
            LocalDate month = today.minusMonths(i);
            String monthKey = month.getMonth().toString().substring(0, 3);

            double monthlyRevenue = confirmedReservations.stream()
                    .filter(r -> {
                        if (r.getDateReservation() == null) return false;
                        LocalDate reservationDate = r.getDateReservation().toLocalDate();
                        return reservationDate.getMonth() == month.getMonth() &&
                                reservationDate.getYear() == month.getYear();
                    })
                    .mapToDouble(r -> r.getMontantTotal() != null ? r.getMontantTotal() : 0)
                    .sum();

            result.put(monthKey, Math.round(monthlyRevenue * 100.0) / 100.0);
        }

        return result;
    }

    // ==================== MÉTHODES UTILITAIRES ====================

    private boolean isEventInDateRange(Event event) {
        if (filterStartDate == null || filterEndDate == null || event.getDateCreation() == null) {
            return true;
        }
        LocalDate eventDate = event.getDateCreation().toLocalDate();
        return !eventDate.isBefore(filterStartDate) && !eventDate.isAfter(filterEndDate);
    }

    private boolean isReservationInDateRange(Reservation reservation) {
        if (filterStartDate == null || filterEndDate == null || reservation.getDateReservation() == null) {
            return true;
        }
        LocalDate reservationDate = reservation.getDateReservation().toLocalDate();
        return !reservationDate.isBefore(filterStartDate) && !reservationDate.isAfter(filterEndDate);
    }

    private boolean isUserInDateRange(User user) {
        if (filterStartDate == null || filterEndDate == null || user.getDateInscription() == null) {
            return true;
        }
        LocalDate inscriptionDate = user.getDateInscription().toLocalDate();
        return !inscriptionDate.isBefore(filterStartDate) && !inscriptionDate.isAfter(filterEndDate);
    }

    private String getFrenchDay(String englishDay) {
        return switch (englishDay) {
            case "MONDAY" -> "Lun";
            case "TUESDAY" -> "Mar";
            case "WEDNESDAY" -> "Mer";
            case "THURSDAY" -> "Jeu";
            case "FRIDAY" -> "Ven";
            case "SATURDAY" -> "Sam";
            case "SUNDAY" -> "Dim";
            default -> englishDay;
        };
    }

    private void applyFilters() {
        filterStartDate = dateFromPicker.getValue();
        filterEndDate = dateToPicker.getValue();
        refreshData();
        Notification.show("Filtres appliqués", 1000, Notification.Position.TOP_CENTER);
    }

    private void refreshData() {
        int selectedIndex = tabs.getSelectedIndex();
        TabSheet newTabs = createTabSheet();

        getChildren().filter(child -> child == tabs).findFirst()
                .ifPresent(oldTabs -> {
                    int index = getChildren().collect(Collectors.toList()).indexOf(oldTabs);
                    remove(oldTabs);
                    addComponentAtIndex(index, newTabs);
                });

        tabs = newTabs;
        tabs.setSelectedIndex(selectedIndex);

        Notification.show("Données actualisées", 2000, Notification.Position.TOP_CENTER);
    }

    // ==================== MÉTHODES DE STATISTIQUES ====================

    private HorizontalLayout createMainStatsCards() {
        HorizontalLayout stats = new HorizontalLayout();
        stats.setWidthFull();
        stats.setSpacing(true);

        try {
            long totalUsers = userService.getAllUsers().size();
            long totalEvents = eventService.getTotalEventsCount();
            long totalReservations = reservationService.getTotalReservationsCount();
            double totalRevenue = reservationService.getTotalRevenue();

            stats.add(
                    createStatCard("👥", "Utilisateurs", String.valueOf(totalUsers), "#7b1fa2"),
                    createStatCard("🎪", "Événements", String.valueOf(totalEvents), "#1976d2"),
                    createStatCard("📋", "Réservations", String.valueOf(totalReservations), "#2e7d32"),
                    createStatCard("💰", "Revenu Total", String.format("%.2f MAD", totalRevenue), "#d32f2f")
            );
        } catch (Exception e) {
            Paragraph error = new Paragraph("Erreur de chargement des statistiques: " + e.getMessage());
            error.getStyle().set("color", "#d32f2f");
            stats.add(error);
        }

        return stats;
    }

    private HorizontalLayout createEventStatsCards() {
        HorizontalLayout stats = new HorizontalLayout();
        stats.setWidthFull();
        stats.setSpacing(true);

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

            stats.add(
                    createStatCard("📢", "Publiés", String.valueOf(publishedEvents), "#1976d2"),
                    createStatCard("📅", "À venir", String.valueOf(upcomingEvents), "#2e7d32"),
                    createStatCard("📝", "Brouillons", String.valueOf(draftEvents), "#666"),
                    createStatCard("❌", "Annulés", String.valueOf(cancelledEvents), "#d32f2f")
            );
        } catch (Exception e) {
            stats.add(new Paragraph("Erreur: " + e.getMessage()));
        }

        return stats;
    }

    private HorizontalLayout createReservationStatsCards() {
        HorizontalLayout stats = new HorizontalLayout();
        stats.setWidthFull();
        stats.setSpacing(true);

        try {
            long confirmedReservations = reservationService.getReservationsByStatus(ReservationStatus.CONFIRMEE).size();
            long pendingReservations = reservationService.getReservationsByStatus(ReservationStatus.EN_ATTENTE).size();
            long cancelledReservations = reservationService.getReservationsByStatus(ReservationStatus.ANNULEE).size();
            long totalReservations = reservationService.getTotalReservationsCount();

            stats.add(
                    createStatCard("✅", "Confirmées", String.valueOf(confirmedReservations), "#2e7d32"),
                    createStatCard("⏳", "En attente", String.valueOf(pendingReservations), "#ed6c02"),
                    createStatCard("❌", "Annulées", String.valueOf(cancelledReservations), "#d32f2f"),
                    createStatCard("📋", "Total", String.valueOf(totalReservations), "#1976d2")
            );
        } catch (Exception e) {
            stats.add(new Paragraph("Erreur: " + e.getMessage()));
        }

        return stats;
    }

    private HorizontalLayout createUserStatsCards() {
        HorizontalLayout stats = new HorizontalLayout();
        stats.setWidthFull();
        stats.setSpacing(true);

        try {
            List<User> allUsers = userService.getAllUsers();
            long activeUsers = allUsers.stream().filter(User::getActif).count();
            long organizers = userService.getUsersByRole(UserRole.ORGANIZER).size();
            long clients = userService.getUsersByRole(UserRole.CLIENT).size();

            stats.add(
                    createStatCard("👥", "Total", String.valueOf(allUsers.size()), "#7b1fa2"),
                    createStatCard("✅", "Actifs", String.valueOf(activeUsers), "#2e7d32"),
                    createStatCard("🎪", "Organisateurs", String.valueOf(organizers), "#ed6c02"),
                    createStatCard("👤", "Clients", String.valueOf(clients), "#1976d2")
            );
        } catch (Exception e) {
            stats.add(new Paragraph("Erreur: " + e.getMessage()));
        }

        return stats;
    }

    private HorizontalLayout createRevenueStatsCards() {
        HorizontalLayout stats = new HorizontalLayout();
        stats.setWidthFull();
        stats.setSpacing(true);

        try {
            double totalRevenue = reservationService.getTotalRevenue();
            double confirmedRevenue = reservationService.getAllReservations().stream()
                    .filter(r -> r.getStatut() == ReservationStatus.CONFIRMEE)
                    .mapToDouble(r -> r.getMontantTotal() != null ? r.getMontantTotal() : 0)
                    .sum();
            long totalTickets = reservationService.getTotalReservationsCount();
            double avgRevenue = totalTickets > 0 ? totalRevenue / totalTickets : 0;

            stats.add(
                    createStatCard("💰", "Revenu Total", String.format("%.2f MAD", totalRevenue), "#2e7d32"),
                    createStatCard("✅", "Confirmé", String.format("%.2f MAD", confirmedRevenue), "#1976d2"),
                    createStatCard("🎫", "Tickets vendus", String.valueOf(totalTickets), "#ed6c02"),
                    createStatCard("📊", "Moyenne/ticket", String.format("%.2f MAD", avgRevenue), "#7b1fa2")
            );
        } catch (Exception e) {
            stats.add(new Paragraph("Erreur: " + e.getMessage()));
        }

        return stats;
    }

    private VerticalLayout createStatCard(String icon, String title, String value, String color) {
        VerticalLayout card = new VerticalLayout();
        card.setWidth("100%");
        card.setPadding(true);
        card.setSpacing(false);
        card.getStyle()
                .set("border", "1px solid #e0e0e0")
                .set("border-radius", "8px")
                .set("box-shadow", "0 2px 4px rgba(0,0,0,0.1)")
                .set("background", "white")
                .set("text-align", "center");

        Paragraph iconPara = new Paragraph(icon);
        iconPara.getStyle().set("font-size", "32px").set("margin", "0");

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

        card.add(iconPara, valueH2, titlePara);
        return card;
    }

    // ==================== MÉTHODES D'ÉTIQUETTES ====================

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

    private String getRoleLabel(UserRole role) {
        return switch (role) {
            case ADMIN -> "👑 Admin";
            case ORGANIZER -> "🎪 Organisateur";
            case CLIENT -> "👤 Client";
        };
    }
}