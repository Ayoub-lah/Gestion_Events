package com.eventbooking.view.publicview;

import com.eventbooking.entity.User;
import com.eventbooking.entity.enums.UserRole;
import com.eventbooking.service.UserService;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.InputStream;
import java.util.Optional;

@Route("login")
@PageTitle("Connexion | Event Booking")
@AnonymousAllowed
public class LoginView extends VerticalLayout implements BeforeEnterObserver {

    @Autowired
    private UserService userService;

    public LoginView() {
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);
        setSizeFull();
        setPadding(false);
        setSpacing(false);
        getStyle().set("background-color", "#f5f5f5");
        createUI();
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        User currentUser = (User) VaadinSession.getCurrent().getAttribute("currentUser");
        if (currentUser != null) {
            event.forwardTo(getDashboardRoute(currentUser.getRole()));
        }
    }

    private void createUI() {
        // Layout horizontal principal
        HorizontalLayout mainLayout = new HorizontalLayout();
        mainLayout.setSizeFull();
        mainLayout.setSpacing(false);
        mainLayout.setAlignItems(Alignment.STRETCH);

        // Partie gauche - Formulaire
        VerticalLayout leftPanel = new VerticalLayout();
        leftPanel.setWidth("50%");
        leftPanel.setHeightFull();
        leftPanel.setJustifyContentMode(JustifyContentMode.CENTER);
        leftPanel.setAlignItems(Alignment.CENTER);
        leftPanel.getStyle()
                .set("background", "white")
                .set("padding", "40px 20px");

        // Conteneur du formulaire
        Div formContainer = new Div();
        formContainer.setWidth("400px");
        formContainer.getStyle()
                .set("max-width", "90%");

        // Titre
        H1 title = new H1("Event Booking");
        title.getStyle()
                .set("margin", "0 0 10px 0")
                .set("color", "#1976d2")
                .set("text-align", "center")
                .set("font-size", "28px");

        H2 subtitle = new H2("Connexion");
        subtitle.getStyle()
                .set("margin", "0 0 30px 0")
                .set("color", "#666")
                .set("text-align", "center")
                .set("font-size", "20px")
                .set("font-weight", "normal");

        // Champs de formulaire
        EmailField emailField = new EmailField("Email");
        emailField.setWidthFull();
        emailField.setPlaceholder("exemple@email.com");
        emailField.setRequiredIndicatorVisible(true);
        emailField.getStyle().set("margin-bottom", "15px");

        PasswordField passwordField = new PasswordField("Mot de passe");
        passwordField.setWidthFull();
        passwordField.setPlaceholder("Votre mot de passe");
        passwordField.setRequiredIndicatorVisible(true);
        passwordField.getStyle().set("margin-bottom", "15px");

        // Bouton de connexion
        Button loginButton = new Button("Se connecter");
        loginButton.setWidthFull();
        loginButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        loginButton.getStyle()
                .set("height", "40px")
                .set("margin-top", "20px")
                .set("font-weight", "bold");
        loginButton.addClickListener(e ->
                handleLogin(emailField.getValue(), passwordField.getValue()));

        // Soumettre avec Enter
        passwordField.addKeyPressListener(event -> {
            if (event.getKey().getKeys().contains("Enter")) {
                handleLogin(emailField.getValue(), passwordField.getValue());
            }
        });

        // Lien d'inscription
        Paragraph registerText = new Paragraph("Pas encore de compte ?");
        registerText.getStyle()
                .set("text-align", "center")
                .set("margin", "20px 0 10px 0")
                .set("color", "#666");

        Button registerButton = new Button("Créer un compte");
        registerButton.setWidthFull();
        registerButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        registerButton.addClickListener(e -> UI.getCurrent().navigate("register"));



        // Ajout des composants au formulaire
        formContainer.add(title, subtitle, emailField, passwordField,
                loginButton, registerText, registerButton);

        leftPanel.add(formContainer);
        leftPanel.setHorizontalComponentAlignment(Alignment.CENTER, formContainer);

        // Partie droite - Photo
        VerticalLayout rightPanel = new VerticalLayout();
        rightPanel.setWidth("50%");
        rightPanel.setHeightFull();
        rightPanel.setJustifyContentMode(JustifyContentMode.CENTER);
        rightPanel.setAlignItems(Alignment.CENTER);
        rightPanel.getStyle()
                .set("background", "linear-gradient(135deg, #1976d2 0%, #2196f3 100%)")
                .set("position", "relative");

        // Image de fond (vous pouvez utiliser votre propre image)
        Image backgroundImage = createBackgroundImage();
        backgroundImage.setWidthFull();
        backgroundImage.setHeightFull();
        backgroundImage.getStyle()
                .set("object-fit", "cover")
                .set("position", "absolute")
                .set("top", "0")
                .set("left", "0");

        // Overlay pour améliorer la lisibilité
        Div overlay = new Div();
        overlay.setWidthFull();
        overlay.setHeightFull();
        overlay.getStyle()
                .set("background", "rgba(25, 118, 210, 0.8)")
                .set("position", "absolute")
                .set("top", "0")
                .set("left", "0");

        // Contenu sur l'image
        VerticalLayout imageContent = new VerticalLayout();
        imageContent.setAlignItems(Alignment.CENTER);
        imageContent.setJustifyContentMode(JustifyContentMode.CENTER);
        imageContent.setHeightFull();
        imageContent.setWidthFull();
        imageContent.getStyle()
                .set("position", "relative")
                .set("z-index", "1")
                .set("color", "white")
                .set("padding", "40px")
                .set("text-align", "center");

        // Icône ou logo
        Span icon = new Span("🎫");
        icon.getStyle()
                .set("font-size", "64px")
                .set("margin-bottom", "20px");

        H1 welcomeTitle = new H1("Bienvenue !");
        welcomeTitle.getStyle()
                .set("color", "white")
                .set("font-size", "36px")
                .set("font-weight", "bold")
                .set("margin", "0 0 20px 0");

        Paragraph welcomeText = new Paragraph(
                "Connectez-vous pour accéder à votre espace personnel " +
                        "et profiter de toutes les fonctionnalités de notre plateforme de réservation d'événements."
        );
        welcomeText.getStyle()
                .set("color", "rgba(255, 255, 255, 0.9)")
                .set("font-size", "18px")
                .set("max-width", "500px")
                .set("line-height", "1.6");

        // Points forts
        Div features = new Div();
        features.getStyle()
                .set("margin-top", "40px")
                .set("text-align", "left");

        String[] featureItems = {
                "✓ Réservation facile et rapide",
                "✓ Gestion de vos événements",
                "✓ Notifications en temps réel",
                "✓ Support client 24/7"
        };

        for (String item : featureItems) {
            Paragraph feature = new Paragraph(item);
            feature.getStyle()
                    .set("color", "rgba(255, 255, 255, 0.9)")
                    .set("font-size", "16px")
                    .set("margin", "10px 0");
            features.add(feature);
        }

        imageContent.add(icon, welcomeTitle, welcomeText, features);
        rightPanel.add(backgroundImage, overlay, imageContent);

        // Ajout des deux panneaux au layout principal
        mainLayout.add(leftPanel, rightPanel);
        add(mainLayout);
    }

    private Image createBackgroundImage() {
        try {
            // Essayez de charger une image depuis les ressources
            InputStream imageStream = getClass().getResourceAsStream("/images/login-background.jpg");
            if (imageStream != null) {
                StreamResource resource = new StreamResource("login-background.jpg", () -> imageStream);
                return new Image(resource, "Background de connexion");
            }
        } catch (Exception e) {
            // Si l'image n'existe pas, on utilise une couleur de fond
        }

        // Fallback - Image SVG générée pour les événements
        String svgBackground = """
            <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 800 600" preserveAspectRatio="xMidYMid slice">
                <defs>
                    <linearGradient id="gradient" x1="0%" y1="0%" x2="100%" y2="100%">
                        <stop offset="0%" style="stop-color:#1976d2;stop-opacity:1" />
                        <stop offset="100%" style="stop-color:#2196f3;stop-opacity:1" />
                    </linearGradient>
                    <pattern id="pattern" x="0" y="0" width="100" height="100" patternUnits="userSpaceOnUse">
                        <circle cx="50" cy="50" r="30" fill="none" stroke="rgba(255,255,255,0.1)" stroke-width="2"/>
                        <path d="M20,20 L80,80 M80,20 L20,80" stroke="rgba(255,255,255,0.1)" stroke-width="2"/>
                    </pattern>
                </defs>
                <rect width="100%" height="100%" fill="url(#gradient)"/>
                <rect width="100%" height="100%" fill="url(#pattern)" opacity="0.2"/>
                <circle cx="200" cy="150" r="80" fill="rgba(255,255,255,0.05)"/>
                <circle cx="600" cy="450" r="120" fill="rgba(255,255,255,0.05)"/>
                <circle cx="400" cy="300" r="60" fill="rgba(255,255,255,0.05)"/>
            </svg>
            """;

        Image image = new Image();
        image.getElement().setProperty("innerHTML", svgBackground);
        return image;
    }

    private void handleLogin(String email, String password) {
        if (email.isEmpty() || password.isEmpty()) {
            showNotification("Veuillez remplir tous les champs", NotificationVariant.LUMO_ERROR);
            return;
        }

        try {
            boolean authenticated = userService.authenticate(email, password);

            if (authenticated) {
                Optional<User> userOpt = userService.getUserByEmail(email);

                if (userOpt.isPresent()) {
                    User user = userOpt.get();
                    VaadinSession.getCurrent().setAttribute("currentUser", user);

                    showNotification("Connexion réussie !", NotificationVariant.LUMO_SUCCESS);

                    UI.getCurrent().navigate(getDashboardRoute(user.getRole()));
                    return;
                }
            }

            showNotification("Email ou mot de passe incorrect", NotificationVariant.LUMO_ERROR);

        } catch (Exception e) {
            showNotification("Erreur: " + e.getMessage(), NotificationVariant.LUMO_ERROR);
        }
    }

    private String getDashboardRoute(UserRole role) {
        switch (role) {
            case ADMIN: return "admin/dashboard";
            case ORGANIZER: return "organizer/dashboard";
            case CLIENT: return "client/dashboard";
            default: return "";
        }
    }

    private void showNotification(String message, NotificationVariant variant) {
        Notification notification = new Notification(message, 3000);
        notification.addThemeVariants(variant);
        notification.setPosition(Notification.Position.TOP_CENTER);
        notification.open();
    }
}