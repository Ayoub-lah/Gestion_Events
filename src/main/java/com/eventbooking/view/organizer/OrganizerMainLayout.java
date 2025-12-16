package com.eventbooking.view.organizer;

import com.eventbooking.entity.User;
import com.eventbooking.entity.enums.UserRole;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Header;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.router.RouterLayout;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.theme.lumo.LumoUtility;

public class OrganizerMainLayout extends AppLayout implements RouterLayout {

    private User currentUser;

    public OrganizerMainLayout() {
        currentUser = (User) VaadinSession.getCurrent().getAttribute("currentUser");

        if (currentUser == null || currentUser.getRole() != UserRole.ORGANIZER) {
            UI.getCurrent().navigate("login");
            return;
        }

        createHeader();
        createDrawer();
    }

    private void createHeader() {
        DrawerToggle toggle = new DrawerToggle();

        H1 title = new H1("📋 Dashboard Organisateur");
        title.getStyle()
                .set("font-size", "var(--lumo-font-size-l)")
                .set("margin", "0");

        // User menu
        Avatar avatar = new Avatar(currentUser.getPrenom() + " " + currentUser.getNom());
        avatar.setThemeName("small");
        avatar.getElement().setAttribute("tabindex", "-1");

        MenuBar userMenu = new MenuBar();
        userMenu.setThemeName("tertiary-inline contrast");

        Div userInfo = new Div();
        userInfo.setText(currentUser.getPrenom() + " " + currentUser.getNom());
        userInfo.getStyle()
                .set("font-size", "var(--lumo-font-size-s)")
                .set("color", "var(--lumo-secondary-text-color)");

        Div roleInfo = new Div();
        roleInfo.setText("Organisateur");
        roleInfo.getStyle()
                .set("font-size", "var(--lumo-font-size-xs)")
                .set("color", "var(--lumo-contrast-50pct)");

        VerticalLayout userLayout = new VerticalLayout(userInfo, roleInfo);
        userLayout.setPadding(false);
        userLayout.setSpacing(false);

        userMenu.addItem(userLayout, e -> showUserMenu(e.getSource()));

        Button logoutButton = new Button("Déconnexion", new Icon(VaadinIcon.SIGN_OUT));
        logoutButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ERROR);
        logoutButton.addClickListener(e -> logout());

        HorizontalLayout header = new HorizontalLayout(toggle, title, new Div(), userMenu, logoutButton);
        header.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        header.expand(title);
        header.setWidthFull();
        header.addClassNames(
                LumoUtility.Padding.Vertical.NONE,
                LumoUtility.Padding.Horizontal.MEDIUM
        );

        addToNavbar(header);
    }

    private void createDrawer() {
        SideNav nav = new SideNav();

        // Dashboard
        nav.addItem(new SideNavItem("Dashboard", OrganizerDashboardView.class));

        // Réservations


        nav.addItem(new SideNavItem("Événements", MyEventsView.class));

        // Statistiques
        nav.addItem(new SideNavItem("Statistiques", OrganizerStatsView.class));

        // Profil
        nav.addItem(new SideNavItem("Mon Profil", OrganizerProfileView.class));

        addToDrawer(new VerticalLayout(
                new Header(createAppName()),
                nav
        ));
    }

    private HorizontalLayout createAppName() {
        HorizontalLayout appName = new HorizontalLayout(
                new Icon(VaadinIcon.CALENDAR),
                new Span("Event Booking")
        );
        appName.setAlignItems(FlexComponent.Alignment.CENTER);
        appName.setPadding(true);
        appName.setSpacing(true);
        return appName;
    }

    private void showUserMenu(MenuItem menuBar) {
        // Menu contextuel pour l'utilisateur
    }

    private void logout() {
        VaadinSession.getCurrent().setAttribute("currentUser", null);
        VaadinSession.getCurrent().close();
        UI.getCurrent().navigate("login");
    }
}