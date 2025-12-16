package com.eventbooking.service.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class DashboardFilter {
    private LocalDate dateFrom;
    private LocalDate dateTo;
    private String period;
    private String filterType; // "ALL", "USER", "EVENT", "RESERVATION"
    private Long userId; // Pour filtrer par utilisateur spécifique
    private Long eventId; // Pour filtrer par événement spécifique

    // Constructeurs
    public DashboardFilter() {}

    public DashboardFilter(LocalDate dateFrom, LocalDate dateTo) {
        this.dateFrom = dateFrom;
        this.dateTo = dateTo;
        this.period = "CUSTOM";
    }

    // Getters et Setters
    public LocalDate getDateFrom() { return dateFrom; }
    public void setDateFrom(LocalDate dateFrom) { this.dateFrom = dateFrom; }

    public LocalDate getDateTo() { return dateTo; }
    public void setDateTo(LocalDate dateTo) { this.dateTo = dateTo; }

    public String getPeriod() { return period; }
    public void setPeriod(String period) { this.period = period; }

    public String getFilterType() { return filterType; }
    public void setFilterType(String filterType) { this.filterType = filterType; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public Long getEventId() { return eventId; }
    public void setEventId(Long eventId) { this.eventId = eventId; }

    // Méthodes utilitaires
    public LocalDateTime getDateFromAsDateTime() {
        return dateFrom != null ? dateFrom.atStartOfDay() : null;
    }

    public LocalDateTime getDateToAsDateTime() {
        return dateTo != null ? dateTo.atTime(23, 59, 59) : null;
    }

    public boolean hasDateRange() {
        return dateFrom != null && dateTo != null;
    }
}