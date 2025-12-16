package com.eventbooking.view.client;

import com.eventbooking.entity.Reservation;
import com.eventbooking.entity.User;
import com.eventbooking.service.ReservationService;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.charts.Chart;
import com.vaadin.flow.component.charts.model.*;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Route(value = "client/history", layout = ClientMainLayout.class)
@PageTitle("Historique - EventBooking")
public class ClientHistoryView extends VerticalLayout {

    private final ReservationService reservationService;
    private User currentUser;

    private Grid<Reservation> historyGrid;
    private Tabs filterTabs;
    private Chart reservationChart;

    @Autowired
    public ClientHistoryView(ReservationService reservationService) {
        this.reservationService = reservationService;

        Object userObj = VaadinSession.getCurrent().getAttribute("currentUser");
        this.currentUser = (userObj instanceof User) ? (User) userObj : null;

        if (currentUser == null) {
            UI.getCurrent().navigate("login");
            return;
        }

        initView();
        loadAllReservations();
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
        header.setSpacing(true);

        Icon historyIcon = VaadinIcon.CALENDAR.create();
        historyIcon.setSize("32px");
        historyIcon.getStyle().set("color", "#667eea");

        H2 title = new H2("Historique des Réservations");
        title.getStyle()
                .set("margin", "0")
                .set("color", "#333");

        header.add(historyIcon, title);

        // Tabs de filtrage
        filterTabs = new Tabs();
        filterTabs.setWidthFull();

        Tab allTab = new Tab("Toutes");
        Tab confirmedTab = new Tab("Confirmées");
        Tab cancelledTab = new Tab("Annulées");
        Tab upcomingTab = new Tab("À venir");
        Tab pastTab = new Tab("Passées");

        filterTabs.add(allTab, confirmedTab, cancelledTab, upcomingTab, pastTab);

        // Ajouter les listeners
        filterTabs.addSelectedChangeListener(e -> {
            Tab selected = e.getSelectedTab();
            if (selected == allTab) {
                loadAllReservations();
            } else if (selected == confirmedTab) {
                loadConfirmedReservations();
            } else if (selected == cancelledTab) {
                loadCancelledReservations();
            } else if (selected == upcomingTab) {
                loadUpcomingReservations();
            } else if (selected == pastTab) {
                loadPastReservations();
            }
        });

        // Graphique
        reservationChart = createReservationChart();

        // Grille
        historyGrid = createHistoryGrid();

        add(header, filterTabs, reservationChart, historyGrid);
    }

    private Chart createReservationChart() {
        Chart chart = new Chart(ChartType.PIE);
        chart.setWidthFull();
        chart.setHeight("300px");

        Configuration configuration = chart.getConfiguration();
        configuration.setTitle("Répartition des réservations");

        PlotOptionsPie plotOptions = new PlotOptionsPie();
        plotOptions.setAllowPointSelect(true);
        plotOptions.setCursor(Cursor.POINTER);
        plotOptions.setShowInLegend(true);

        DataSeries series = new DataSeries();

        List<Reservation> allReservations = reservationService.getReservationsByUser(currentUser.getId());

        long confirmed = allReservations.stream()
                .filter(r -> r.getStatut().toString().equals("CONFIRMEE"))
                .count();

        long cancelled = allReservations.stream()
                .filter(r -> r.getStatut().toString().equals("ANNULEE"))
                .count();

        long pending = allReservations.stream()
                .filter(r -> r.getStatut().toString().equals("EN_ATTENTE"))
                .count();

        series.add(new DataSeriesItem("Confirmées", confirmed));
        series.add(new DataSeriesItem("Annulées", cancelled));
        series.add(new DataSeriesItem("En attente", pending));

        configuration.setSeries(series);

        return chart;
    }

    private Grid<Reservation> createHistoryGrid() {
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
                .setHeader("Date de l'événement")
                .setAutoWidth(true)
                .setSortable(true);

        grid.addColumn(reservation -> reservation.getDateReservation()
                        .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")))
                .setHeader("Date réservation")
                .setAutoWidth(true)
                .setSortable(true);

        grid.addColumn(Reservation::getNombrePlaces)
                .setHeader("Places")
                .setAutoWidth(true);

        grid.addColumn(reservation -> reservation.getMontantTotal() + " MAD")
                .setHeader("Montant")
                .setAutoWidth(true);

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

        return grid;
    }

    private void loadAllReservations() {
        List<Reservation> reservations = reservationService.getReservationsByUser(currentUser.getId());
        historyGrid.setItems(reservations);
        updateChart(reservations);
    }

    private void loadConfirmedReservations() {
        List<Reservation> allReservations = reservationService.getReservationsByUser(currentUser.getId());
        List<Reservation> confirmed = allReservations.stream()
                .filter(r -> r.getStatut().toString().equals("CONFIRMEE"))
                .toList();
        historyGrid.setItems(confirmed);
    }

    private void loadCancelledReservations() {
        List<Reservation> allReservations = reservationService.getReservationsByUser(currentUser.getId());
        List<Reservation> cancelled = allReservations.stream()
                .filter(r -> r.getStatut().toString().equals("ANNULEE"))
                .toList();
        historyGrid.setItems(cancelled);
    }

    private void loadUpcomingReservations() {
        List<Reservation> upcoming = reservationService.getUpcomingReservationsByUser(currentUser.getId());
        historyGrid.setItems(upcoming);
    }

    private void loadPastReservations() {
        List<Reservation> past = reservationService.getReservationHistoryByUser(currentUser.getId());
        historyGrid.setItems(past);
    }

    private void updateChart(List<Reservation> reservations) {
        // Mettre à jour le graphique
        long confirmed = reservations.stream()
                .filter(r -> r.getStatut().toString().equals("CONFIRMEE"))
                .count();

        long cancelled = reservations.stream()
                .filter(r -> r.getStatut().toString().equals("ANNULEE"))
                .count();

        long pending = reservations.stream()
                .filter(r -> r.getStatut().toString().equals("EN_ATTENTE"))
                .count();

        Configuration configuration = reservationChart.getConfiguration();
        DataSeries series = new DataSeries();
        series.add(new DataSeriesItem("Confirmées", confirmed));
        series.add(new DataSeriesItem("Annulées", cancelled));
        series.add(new DataSeriesItem("En attente", pending));

        configuration.setSeries(series);
        reservationChart.drawChart();
    }
}