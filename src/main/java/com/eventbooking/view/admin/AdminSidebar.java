package com.eventbooking.view.admin.components;

import com.eventbooking.entity.User;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.server.VaadinSession;

public class AdminSidebar extends VerticalLayout {

    private User currentUser;
    private String currentRoute;

    // Stocker les références aux boutons pour pouvoir les mettre à jour
    private Button dashboardBtn;
    private Button usersBtn;
    private Button eventsBtn;
    private Button reservationsBtn;
    private Button reportsBtn;
    private Button profileBtn;


    public AdminSidebar(User currentUser, String currentRoute) {
        this.currentUser = currentUser;
        this.currentRoute = currentRoute;

        setWidth("280px");
        setHeightFull();
        setPadding(true);
        setSpacing(false);
        getStyle()
                .set("background", "linear-gradient(180deg, #1e3a8a 0%, #111827 100%)")
                .set("color", "white")
                .set("box-shadow", "2px 0 10px rgba(0,0,0,0.1)");

        createSidebar();
    }

    private void createSidebar() {
        // Logo et titre
        Div logoSection = createLogoSection();

        // Section utilisateur

        // Menu de navigation
        VerticalLayout menu = createMenu();

        add(logoSection, menu);
    }

    private Div createLogoSection() {
        Div logoSection = new Div();
        logoSection.getStyle()
                .set("padding", "20px 15px")
                .set("border-bottom", "1px solid rgba(255,255,255,0.1)")
                .set("margin-bottom", "20px");

        H1 logo = new H1("Admin");
        logo.getStyle()
                .set("margin", "0")
                .set("font-size", "24px")
                .set("color", "white");

        Paragraph subtitle = new Paragraph("Event Booking System");
        subtitle.getStyle()
                .set("margin", "5px 0 0 0")
                .set("color", "#94a3b8")
                .set("font-size", "12px");

        logoSection.add(logo, subtitle);
        return logoSection;
    }



    private VerticalLayout createMenu() {
        VerticalLayout menu = new VerticalLayout();
        menu.setPadding(false);
        menu.setSpacing(false);
        menu.getStyle().set("flex-grow", "1");

        // Déterminer quel bouton est actif
        boolean isAccueilActive = currentRoute.contains("/") && !currentRoute.contains("admin");
        boolean isDashboardActive = currentRoute.contains("dashboard");
        boolean isUsersActive = currentRoute.contains("users");
        boolean isEventsActive = currentRoute.contains("events");
        boolean isReservationsActive = currentRoute.contains("reservations");
        boolean isReportsActive = currentRoute.contains("reports");
        boolean isProfileActive = currentRoute.contains("profile");



        dashboardBtn = createNavButton(
                "Tableau de Bord",
                VaadinIcon.DASHBOARD,
                "admin/dashboard",
                isDashboardActive
        );

        usersBtn = createNavButton(
                "Gestion Utilisateurs",
                VaadinIcon.USERS,
                "admin/users",
                isUsersActive
        );

        eventsBtn = createNavButton(
                "Gestion Événements",
                VaadinIcon.CALENDAR,
                "admin/events",
                isEventsActive
        );

        reservationsBtn = createNavButton(
                "Gestion Réservations",
                VaadinIcon.LIST,
                "admin/reservations",
                isReservationsActive
        );

        reportsBtn = createNavButton(
                "Rapports Système",
                VaadinIcon.CHART,
                "admin/reports",
                isReportsActive
        );

        profileBtn = createNavButton(
                "Profile",
                VaadinIcon.USER,
                "admin/profile",
                isProfileActive
        );

        menu.add(dashboardBtn, usersBtn, eventsBtn, reservationsBtn, reportsBtn, profileBtn);

        // Espaceur
        Div spacer = new Div();
        spacer.getStyle().set("flex-grow", "1");
        menu.add(spacer);

        // Déconnexion
        Button logoutBtn = createLogoutButton();
        menu.add(logoutBtn);

        return menu;
    }

    private Button createNavButton(String text, VaadinIcon icon, String route, boolean active) {
        Button button = new Button(text, new Icon(icon));
        button.setWidthFull();
        button.setHeight("50px");

        // Style de base
        updateButtonStyle(button, active);

        button.getElement().getStyle().set("gap", "10px");

        // Effet hover
        button.getElement().addEventListener("mouseenter", e -> {
            if (!active) {
                button.getStyle()
                        .set("background", "rgba(59, 130, 246, 0.1)")
                        .set("color", "#93c5fd")
                        .set("border", "1px solid rgba(59, 130, 246, 0.3)")
                        .set("transform", "translateX(2px)");
            }
        });

        button.getElement().addEventListener("mouseleave", e -> {
            if (!active) {
                updateButtonStyle(button, false);
            }
        });

        // Navigation avec route
        if (route != null) {
            button.addClickListener(e -> {
                // Animation de clic
                button.getStyle().set("transform", "scale(0.98)");
                UI.getCurrent().getPage().executeJs(
                        "setTimeout(() => $0.style.transform = '', 150)",
                        button.getElement()
                );

                // Mettre à jour tous les boutons pour désactiver les autres
                updateAllButtonStates(button);

                // Navigation
                UI.getCurrent().navigate(route);
            });
        }

        return button;
    }

    private void updateButtonStyle(Button button, boolean active) {
        button.getStyle()
                .set("justify-content", "flex-start")
                .set("padding", "0 15px")
                .set("margin", "2px 0")
                .set("color", active ? "#93c5fd" : "#e2e8f0")
                .set("background", active ? "rgba(59, 130, 246, 0.2)" : "transparent")
                .set("border", active ? "1px solid rgba(59, 130, 246, 0.5)" : "1px solid transparent")
                .set("border-radius", "8px")
                .set("font-size", "14px")
                .set("font-weight", active ? "600" : "400")
                .set("transition", "all 0.2s ease-in-out");
    }

    private void updateAllButtonStates(Button clickedButton) {
        // Liste de tous les boutons
        Button[] allButtons = {dashboardBtn, usersBtn, eventsBtn, reservationsBtn, reportsBtn, profileBtn};

        for (Button button : allButtons) {
            if (button != null) {
                boolean isActive = button == clickedButton;
                updateButtonStyle(button, isActive);
            }
        }
    }

    private Button createLogoutButton() {
        Button logoutBtn = new Button("Déconnexion", new Icon(VaadinIcon.SIGN_OUT));
        logoutBtn.setWidthFull();
        logoutBtn.setHeight("50px");
        logoutBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        logoutBtn.getStyle()
                .set("justify-content", "flex-start")
                .set("padding", "0 15px")
                .set("margin", "10px 0 0 0")
                .set("color", "#fca5a5")
                .set("background", "transparent")
                .set("border", "1px solid rgba(248, 113, 113, 0.3)")
                .set("border-radius", "8px")
                .set("font-size", "14px")
                .set("transition", "all 0.2s ease-in-out");

        logoutBtn.getElement().getStyle().set("gap", "10px");

        // Effet hover
        logoutBtn.getElement().addEventListener("mouseenter", e -> {
            logoutBtn.getStyle()
                    .set("background", "rgba(248, 113, 113, 0.1)")
                    .set("color", "#fca5a5")
                    .set("border", "1px solid rgba(248, 113, 113, 0.5)")
                    .set("transform", "translateX(2px)");
        });

        logoutBtn.getElement().addEventListener("mouseleave", e -> {
            logoutBtn.getStyle()
                    .set("background", "transparent")
                    .set("color", "#fca5a5")
                    .set("border", "1px solid rgba(248, 113, 113, 0.3)")
                    .set("transform", "translateX(0)");
        });

        // Animation de clic
        logoutBtn.addClickListener(e -> {
            logoutBtn.getStyle().set("transform", "scale(0.98)");
            UI.getCurrent().getPage().executeJs(
                    "setTimeout(() => $0.style.transform = '', 150)",
                    logoutBtn.getElement()
            );

            // Petit délai pour l'animation
            UI.getCurrent().getPage().executeJs(
                    "setTimeout(() => { $0.click(); }, 200)",
                    logoutBtn.getElement()
            );
        });

        logoutBtn.addClickListener(e -> logout());
        return logoutBtn;
    }

    private void logout() {
        VaadinSession.getCurrent().setAttribute("currentUser", null);
        VaadinSession.getCurrent().close();
        UI.getCurrent().getPage().executeJs("window.location.href = 'login'");
    }

    // Méthode pour mettre à jour le bouton actif
    public void setActiveRoute(String newRoute) {
        this.currentRoute = newRoute;

        // Mettre à jour l'état de tous les boutons
        boolean isAccueilActive = newRoute.contains("/") && !newRoute.contains("admin");
        boolean isDashboardActive = newRoute.contains("dashboard");
        boolean isUsersActive = newRoute.contains("users");
        boolean isEventsActive = newRoute.contains("events");
        boolean isReservationsActive = newRoute.contains("reservations");
        boolean isReportsActive = newRoute.contains("reports");
        boolean isProfileActive = newRoute.contains("Profile");


        if (dashboardBtn != null) updateButtonStyle(dashboardBtn, isDashboardActive);
        if (usersBtn != null) updateButtonStyle(usersBtn, isUsersActive);
        if (eventsBtn != null) updateButtonStyle(eventsBtn, isEventsActive);
        if (reservationsBtn != null) updateButtonStyle(reservationsBtn, isReservationsActive);
        if (reportsBtn != null) updateButtonStyle(reportsBtn, isReportsActive);
        if (profileBtn != null) updateButtonStyle(profileBtn, isProfileActive);

    }
}