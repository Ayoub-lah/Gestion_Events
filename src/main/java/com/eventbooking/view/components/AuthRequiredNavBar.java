package com.eventbooking.view.components;

import com.eventbooking.entity.User;
import com.eventbooking.entity.enums.UserRole;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.contextmenu.ContextMenu;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.server.VaadinSession;

public class AuthRequiredNavBar extends HorizontalLayout {

    public AuthRequiredNavBar() {
        setWidthFull();
        setPadding(true);
        setSpacing(true);
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.END);

        // Vérifier si l'utilisateur est connecté
        Object userObj = VaadinSession.getCurrent().getAttribute("currentUser");

        if (userObj instanceof User) {
            User user = (User) userObj;
            createLoggedInView(user);
        } else {
            createLoggedOutView();
        }
    }

    private void createLoggedInView(User user) {
        // Avatar avec menu contextuel
        Avatar avatar = new Avatar(user.getPrenom() + " " + user.getNom());
        avatar.getStyle().set("cursor", "pointer");

        // Créer un menu contextuel
        ContextMenu contextMenu = new ContextMenu();
        contextMenu.setTarget(avatar);
        contextMenu.setOpenOnClick(true);

        // Ajouter les items au menu
        addMenuItems(contextMenu, user);

        add(avatar);
    }

    private void addMenuItems(ContextMenu menu, User user) {
        // Dashboard selon le rôle
        String dashboardRoute = getDashboardRoute(user.getRole());

        // CORRECTION: Ne pas chaîner les méthodes qui retournent void
        var dashboardItem = menu.addItem("Mon Dashboard", e ->
                UI.getCurrent().navigate(dashboardRoute)
        );
        dashboardItem.addComponentAsFirst(VaadinIcon.DASHBOARD.create());

        // Réservations
        var reservationsItem = menu.addItem("Mes Réservations", e ->
                UI.getCurrent().navigate("client/reservations")
        );
        reservationsItem.addComponentAsFirst(VaadinIcon.TICKET.create());

        // Historique
        var historyItem = menu.addItem("Historique", e ->
                UI.getCurrent().navigate("client/history")
        );
        historyItem.addComponentAsFirst(VaadinIcon.CALENDAR.create());

        // Profil
        var profileItem = menu.addItem("Mon Profil", e ->
                UI.getCurrent().navigate("client/profile")
        );
        profileItem.addComponentAsFirst(VaadinIcon.USER.create());

        // Séparateur
        menu.add(new Hr());

        // Déconnexion
        var logoutItem = menu.addItem("Déconnexion", e -> {
            VaadinSession.getCurrent().setAttribute("currentUser", null);
            VaadinSession.getCurrent().close();
            UI.getCurrent().getPage().reload();
        });
        logoutItem.addComponentAsFirst(VaadinIcon.SIGN_OUT.create());
        logoutItem.getElement().getStyle().set("color", "var(--lumo-error-color)");
    }

    private String getDashboardRoute(UserRole role) {
        if (role == UserRole.ADMIN) return "admin/dashboard";
        if (role == UserRole.ORGANIZER) return "organizer/dashboard";
        return "client/dashboard";
    }

    private void createLoggedOutView() {
        Button loginBtn = new Button("Connexion", VaadinIcon.SIGN_IN.create());
        loginBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        loginBtn.addClickListener(e -> UI.getCurrent().navigate("login"));

        Button registerBtn = new Button("S'inscrire", VaadinIcon.USER_CHECK.create());
        registerBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        registerBtn.addClickListener(e -> UI.getCurrent().navigate("register"));

        add(loginBtn, registerBtn);
    }
}