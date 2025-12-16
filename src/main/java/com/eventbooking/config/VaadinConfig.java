package com.eventbooking.config;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.server.ServiceInitEvent;
import com.vaadin.flow.server.VaadinServiceInitListener;
import com.vaadin.flow.server.VaadinSession;
import com.eventbooking.entity.User;
import com.eventbooking.entity.enums.UserRole;
import org.springframework.stereotype.Component;

@Component
public class VaadinConfig implements VaadinServiceInitListener {

    @Override
    public void serviceInit(ServiceInitEvent event) {
        event.getSource().addUIInitListener(uiEvent -> {
            UI ui = uiEvent.getUI();
            ui.addBeforeEnterListener(this::beforeEnter);
        });
    }

    private void beforeEnter(BeforeEnterEvent event) {
        String targetRoute = getCleanRoute(event.getLocation().getPath());

        User currentUser = getCurrentUser();

        System.out.println("=== NAVIGATION == Route: " + targetRoute + ", User: " +
                (currentUser != null ? currentUser.getRole() : "null"));

        // 1. Si route publique → LAISSER PASSER
        if (isPublicRoute(targetRoute)) {
            System.out.println("→ Route publique, laisser passer");
            return;
        }

        // 2. Si utilisateur NON connecté sur route protégée → LOGIN
        if (currentUser == null && isProtectedRoute(targetRoute)) {
            System.out.println("→ Non connecté sur route protégée → login");
            event.rerouteTo("login");
            return;
        }

        // 3. Si utilisateur connecté sur login/register → DASHBOARD
        if (currentUser != null && (targetRoute.equals("login") || targetRoute.equals("register"))) {
            String dashboardRoute = getDashboardRoute(currentUser.getRole());
            System.out.println("→ Connecté sur login/register → " + dashboardRoute);
            event.rerouteTo(dashboardRoute);
            return;
        }

        // 4. Si utilisateur connecté mais mauvais rôle → ACCÈS REFUSÉ
        if (currentUser != null && !hasAccess(currentUser, targetRoute)) {
            System.out.println("→ Mauvais rôle → access-denied");
            event.rerouteTo("access-denied");
            return;
        }

        // 5. Sinon → LAISSER PASSER (utilisateur connecté avec bon rôle)
        System.out.println("→ Accès autorisé");
    }

    private String getCleanRoute(String path) {
        if (path == null || path.isEmpty() || path.equals("/")) {
            return "";
        }
        // Enlever le slash initial s'il existe
        return path.startsWith("/") ? path.substring(1) : path;
    }

    private boolean isPublicRoute(String route) {
        return route.isEmpty() ||
                route.equals("") ||
                route.equals("login") ||
                route.equals("register") ||
                route.equals("events") ||
                route.equals("event") ||
                route.startsWith("event/") || // Permettre l'accès aux détails d'événements
                route.equals("access-denied") ||
                route.contains("h2-console") ||
                route.contains("VAADIN") || // Resources Vaadin
                route.contains("uploads") || // Fichiers uploadés
                route.startsWith("public/");
    }

    private boolean isProtectedRoute(String route) {
        return route.startsWith("admin/") ||
                route.startsWith("organizer/") ||
                route.startsWith("client/");
    }

    private boolean hasAccess(User user, String route) {
        UserRole role = user.getRole();

        if (route.startsWith("admin/")) {
            return role == UserRole.ADMIN;
        } else if (route.startsWith("organizer/")) {
            return role == UserRole.ORGANIZER;
        } else if (route.startsWith("client/")) {
            return role == UserRole.CLIENT;
        }

        return true; // Routes non protégées
    }

    private String getDashboardRoute(UserRole role) {
        switch (role) {
            case ADMIN:
                return "admin/dashboard";
            case ORGANIZER:
                return "organizer/dashboard";
            case CLIENT:
                return "client/dashboard";
            default:
                return "";
        }
    }

    private User getCurrentUser() {
        VaadinSession session = VaadinSession.getCurrent();
        if (session != null && session.getAttribute("currentUser") instanceof User) {
            return (User) session.getAttribute("currentUser");
        }
        return null;
    }
}