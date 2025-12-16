package com.eventbooking.view.client;

import com.eventbooking.entity.User;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.contextmenu.ContextMenu;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;

@Route("client")
public class ClientMainLayout extends AppLayout {

    private User currentUser;

    public ClientMainLayout() {
        // Récupérer l'utilisateur connecté depuis la session
        Object userObj = VaadinSession.getCurrent().getAttribute("currentUser");
        this.currentUser = (userObj instanceof User) ? (User) userObj : null;

        if (currentUser == null) {
            // Rediriger vers login si pas connecté
            UI.getCurrent().navigate("login");
            return;
        }

        createHeader();
        setPrimarySection(Section.DRAWER);
    }

    private void createHeader() {
        // Navbar principale
        HorizontalLayout navbar = new HorizontalLayout();
        navbar.setWidthFull();
        navbar.setPadding(true);
        navbar.setSpacing(false);
        navbar.setAlignItems(FlexComponent.Alignment.CENTER);
        navbar.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        navbar.getStyle()
                .set("background", "linear-gradient(135deg, #667eea 0%, #764ba2 100%)")
                .set("box-shadow", "0 4px 20px rgba(0, 0, 0, 0.15)")
                .set("position", "sticky")
                .set("top", "0")
                .set("z-index", "1000")
                .set("border-bottom", "1px solid rgba(255, 255, 255, 0.1)");

        // Partie gauche : Logo et menu toggle
        HorizontalLayout leftSection = new HorizontalLayout();
        leftSection.setAlignItems(FlexComponent.Alignment.CENTER);
        leftSection.setSpacing(true);
        leftSection.setPadding(false);

        // Logo avec effet de brillance
        HorizontalLayout logoSection = new HorizontalLayout();
        logoSection.setAlignItems(FlexComponent.Alignment.CENTER);
        logoSection.setSpacing(true);
        logoSection.getStyle()
                .set("cursor", "pointer")
                .set("padding", "8px 16px")
                .set("border-radius", "8px")
                .set("transition", "all 0.3s ease")
                .set("background", "rgba(255, 255, 255, 0.1)")
                .set("backdrop-filter", "blur(10px)");

        logoSection.addClickListener(e -> UI.getCurrent().navigate(""));

        Icon logoIcon = VaadinIcon.CALENDAR_CLOCK.create();
        logoIcon.setSize("28px");
        logoIcon.getStyle()
                .set("color", "#ffffff")
                .set("filter", "drop-shadow(0 2px 4px rgba(0,0,0,0.2))");

        Div logoContainer = new Div();
        logoContainer.getStyle()
                .set("display", "flex")
                .set("align-items", "center")
                .set("gap", "12px");

        H3 logoText = new H3("EventBooking");
        logoText.getStyle()
                .set("color", "white")
                .set("margin", "0")
                .set("font-weight", "700")
                .set("font-size", "22px")
                .set("letter-spacing", "-0.5px")
                .set("text-shadow", "0 2px 4px rgba(0,0,0,0.2)");

        Span premiumBadge = new Span("Pro");
        premiumBadge.getStyle()
                .set("background", "linear-gradient(45deg, #FFD700, #FFA500)")
                .set("color", "#333")
                .set("font-size", "10px")
                .set("font-weight", "bold")
                .set("padding", "2px 8px")
                .set("border-radius", "12px")
                .set("margin-left", "8px")
                .set("box-shadow", "0 2px 4px rgba(0,0,0,0.1)");

        logoContainer.add(logoText, premiumBadge);
        logoSection.add(logoIcon, logoContainer);
        leftSection.add(logoSection);

        // Partie centrale : Navigation
        HorizontalLayout navSection = new HorizontalLayout();
        navSection.setAlignItems(FlexComponent.Alignment.CENTER);
        navSection.getStyle()
                .set("margin-left", "32px")
                .set("background", "rgba(255, 255, 255, 0.08)")
                .set("padding", "4px")
                .set("border-radius", "12px")
                .set("backdrop-filter", "blur(10px)");


        // Partie droite : Menu utilisateur
        HorizontalLayout rightSection = new HorizontalLayout();
        rightSection.setAlignItems(FlexComponent.Alignment.CENTER);

        // Notification badge
        Button notificationBtn = new Button(VaadinIcon.BELL.create());
        notificationBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        notificationBtn.getStyle()
                .set("color", "white")
                .set("position", "relative")
                .set("margin-right", "8px");

        // Badge de notification
        Span notificationBadge = new Span("3");
        notificationBadge.getStyle()
                .set("position", "absolute")
                .set("top", "-4px")
                .set("right", "-4px")
                .set("background", "#ff4757")
                .set("color", "white")
                .set("font-size", "10px")
                .set("font-weight", "bold")
                .set("width", "18px")
                .set("height", "18px")
                .set("border-radius", "50%")
                .set("display", "flex")
                .set("align-items", "center")
                .set("justify-content", "center")
                .set("box-shadow", "0 2px 4px rgba(0,0,0,0.2)");

        notificationBtn.getElement().appendChild(notificationBadge.getElement());

        // Avatar utilisateur avec menu contextuel
        Div avatarContainer = new Div();
        avatarContainer.getStyle()
                .set("position", "relative")
                .set("display", "flex")
                .set("align-items", "center");

        Avatar userAvatar = new Avatar(currentUser.getPrenom() + " " + currentUser.getNom());
        userAvatar.setImage("https://api.dicebear.com/7.x/avataaars/svg?seed=" + currentUser.getEmail());
        userAvatar.getStyle()
                .set("cursor", "pointer")
                .set("border", "3px solid rgba(255, 255, 255, 0.3)")
                .set("box-shadow", "0 4px 12px rgba(0, 0, 0, 0.2)")
                .set("transition", "transform 0.3s ease")
                .set("width", "44px")
                .set("height", "44px");

        // Badge de statut en ligne
        Div onlineStatus = new Div();
        onlineStatus.getStyle()
                .set("position", "absolute")
                .set("bottom", "0")
                .set("right", "0")
                .set("width", "12px")
                .set("height", "12px")
                .set("background", "#4cd964")
                .set("border", "2px solid #667eea")
                .set("border-radius", "50%")
                .set("box-shadow", "0 2px 4px rgba(0,0,0,0.2)");

        avatarContainer.add(userAvatar, onlineStatus);

        // Créer un menu contextuel amélioré
        ContextMenu contextMenu = new ContextMenu();
        contextMenu.setTarget(userAvatar);
        contextMenu.setOpenOnClick(true);
        contextMenu.getStyle()
                .set("border-radius", "12px")
                .set("box-shadow", "0 10px 40px rgba(0, 0, 0, 0.2)")
                .set("border", "1px solid rgba(255, 255, 255, 0.1)")
                .set("overflow", "hidden");

        // Ajouter les items au menu
        addUserMenuItems(contextMenu);

        rightSection.add(notificationBtn, avatarContainer);

        // Assembler la navbar
        navbar.add(leftSection, navSection, rightSection);
        addToNavbar(navbar);
    }

    private Button createNavButton(String text, VaadinIcon icon, String route) {
        Button button = new Button(text, icon.create());
        button.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        button.getStyle()
                .set("color", "rgba(255, 255, 255, 0.9)")
                .set("font-weight", "500")
                .set("font-size", "14px")
                .set("padding", "8px 16px")
                .set("border-radius", "8px")
                .set("transition", "all 0.3s ease")
                .set("--lumo-button-color", "rgba(255, 255, 255, 0.9)")
                .set("--lumo-button-primary-color", "rgba(255, 255, 255, 0.1)");

        button.addClickListener(e -> {
            if (!route.isEmpty()) {
                UI.getCurrent().navigate(route);
            }
        });

        // Effet hover - Séparer les appels addEventListener
        button.getElement().addEventListener("mouseenter", e -> {
            button.getStyle()
                    .set("background", "rgba(255, 255, 255, 0.15)")
                    .set("transform", "translateY(-1px)");
        });

        button.getElement().addEventListener("mouseleave", e -> {
            button.getStyle()
                    .set("background", "transparent")
                    .set("transform", "translateY(0)");
        });

        return button;
    }

    private void addUserMenuItems(ContextMenu menu) {
        // En-tête du menu avec info utilisateur
        Div header = new Div();
        header.getStyle()
                .set("padding", "16px")
                .set("background", "linear-gradient(135deg, #667eea 0%, #764ba2 100%)")
                .set("border-bottom", "1px solid rgba(255, 255, 255, 0.1)")
                .set("margin-bottom", "8px");

        Div userInfo = new Div();
        userInfo.getStyle()
                .set("display", "flex")
                .set("align-items", "center")
                .set("gap", "12px");

        Avatar menuAvatar = new Avatar(currentUser.getPrenom() + " " + currentUser.getNom());
        menuAvatar.setImage("https://api.dicebear.com/7.x/avataaars/svg?seed=" + currentUser.getEmail());
        menuAvatar.getStyle()
                .set("width", "48px")
                .set("height", "48px")
                .set("border", "3px solid white");

        Div userDetails = new Div();
        userDetails.getStyle()
                .set("display", "flex")
                .set("flex-direction", "column");

        Span userName = new Span(currentUser.getPrenom() + " " + currentUser.getNom());
        userName.getStyle()
                .set("font-weight", "bold")
                .set("color", "white")
                .set("font-size", "16px");

        Span userEmail = new Span(currentUser.getEmail());
        userEmail.getStyle()
                .set("color", "rgba(255, 255, 255, 0.8)")
                .set("font-size", "12px");

        userDetails.add(userName, userEmail);
        userInfo.add(menuAvatar, userDetails);
        header.add(userInfo);

        // Ajouter l'en-tête au menu
        menu.add(header);

        // Section principale - utiliser addItem() pour chaque élément
        createMenuItem(menu, "Dashboard", VaadinIcon.DASHBOARD, "client/dashboard");
        createMenuItem(menu, "Mes Réservations", VaadinIcon.TICKET, "client/reservations");
        createMenuItem(menu, "Historique", VaadinIcon.CALENDAR, "client/history");
        createMenuItem(menu, "Mon Profil", VaadinIcon.USER, "client/profile");
        menu.add(new Hr());
        createMenuItem(menu, "Paramètres", VaadinIcon.COG, "client/settings");
        createMenuItem(menu, "Aide & Support", VaadinIcon.QUESTION_CIRCLE, "client/help");
        menu.add(new Hr());

        // Déconnexion
        MenuItem logoutItem = menu.addItem("Déconnexion", e -> {
            VaadinSession.getCurrent().setAttribute("currentUser", null);
            VaadinSession.getCurrent().close();
            UI.getCurrent().getPage().reload();
            UI.getCurrent().navigate("");
        });

        Icon logoutIcon = VaadinIcon.SIGN_OUT.create();
        logoutIcon.getStyle()
                .set("width", "16px")
                .set("height", "16px")
                .set("margin-right", "12px")
                .set("color", "var(--lumo-error-color)");

        logoutItem.addComponentAsFirst(logoutIcon);
        logoutItem.getElement().getStyle()
                .set("color", "var(--lumo-error-color)")
                .set("font-weight", "500")
                .set("padding", "12px 16px")
                .set("transition", "background-color 0.2s")
                .set("border-radius", "8px")
                .set("display", "flex")
                .set("align-items", "center")
                .set("gap", "12px");
    }

    private void createMenuItem(ContextMenu menu, String text, VaadinIcon icon, String route) {
        MenuItem item = menu.addItem("", e -> UI.getCurrent().navigate(route));

        // Créer le contenu personnalisé
        Div content = new Div();
        content.getStyle()
                .set("display", "flex")
                .set("align-items", "center")
                .set("gap", "12px")
                .set("padding", "12px 16px")
                .set("cursor", "pointer")
                .set("border-radius", "8px")
                .set("transition", "background-color 0.2s");

        Icon itemIcon = icon.create();
        itemIcon.getStyle()
                .set("width", "18px")
                .set("height", "18px")
                .set("color", "var(--lumo-secondary-text-color)");

        Span itemText = new Span(text);
        itemText.getStyle()
                .set("font-size", "14px")
                .set("color", "var(--lumo-body-text-color)");

        content.add(itemIcon, itemText);

        // Effet hover - Séparer les appels addEventListener
        content.getElement().addEventListener("mouseenter", e -> {
            content.getStyle().set("background-color", "var(--lumo-contrast-5pct)");
        });

        content.getElement().addEventListener("mouseleave", e -> {
            content.getStyle().set("background-color", "transparent");
        });

        // Ajouter le contenu à l'item
        item.add(content);
    }

    public User getCurrentUser() {
        return currentUser;
    }
}