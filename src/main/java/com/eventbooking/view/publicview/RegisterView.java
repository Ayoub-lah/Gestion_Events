package com.eventbooking.view.publicview;

import com.eventbooking.entity.User;
import com.eventbooking.entity.enums.UserRole;
import com.eventbooking.service.UserService;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.InputStream;

@Route("register")
@PageTitle("Inscription | Event Booking")
@AnonymousAllowed
public class RegisterView extends VerticalLayout {

    @Autowired
    private UserService userService;

    private TextField nomField;
    private TextField prenomField;
    private EmailField emailField;
    private TextField telephoneField;
    private PasswordField passwordField;
    private PasswordField confirmPasswordField;
    private ComboBox<UserRole> roleComboBox;

    public RegisterView() {
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);
        setSizeFull();
        setPadding(false);
        setSpacing(false);
        getStyle().set("background-color", "#f8f9fa");
        createUI();
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
                .set("padding", "40px 20px")
                .set("overflow-y", "auto");

        // Conteneur du formulaire
        Div formContainer = new Div();
        formContainer.setWidth("500px");
        formContainer.getStyle()
                .set("max-width", "90%")
                .set("padding", "20px");

        // Titre principal
        H1 mainTitle = new H1("Event Booking");
        mainTitle.getStyle()
                .set("margin", "0 0 10px 0")
                .set("color", "#1976d2")
                .set("text-align", "center")
                .set("font-size", "32px")
                .set("font-weight", "600");

        // Sous-titre
        Paragraph subtitle = new Paragraph("Créer un nouveau compte");
        subtitle.getStyle()
                .set("margin", "0 0 30px 0")
                .set("color", "#666")
                .set("text-align", "center")
                .set("font-size", "16px");

        // Informations personnelles
        Paragraph sectionTitle1 = new Paragraph("Informations personnelles");
        sectionTitle1.getStyle()
                .set("font-weight", "600")
                .set("color", "#333")
                .set("margin", "20px 0 10px 0")
                .set("font-size", "14px");

        // Layout horizontal pour Nom et Prénom
        Div nameContainer = new Div();
        nameContainer.getStyle()
                .set("display", "grid")
                .set("grid-template-columns", "1fr 1fr")
                .set("gap", "15px")
                .set("margin-bottom", "15px");

        prenomField = new TextField("Prénom *");
        prenomField.setWidthFull();
        prenomField.setRequiredIndicatorVisible(true);
        prenomField.getStyle()
                .set("--vaadin-input-field-border-radius", "6px")
                .set("--vaadin-input-field-border-width", "1px");

        nomField = new TextField("Nom *");
        nomField.setWidthFull();
        nomField.setRequiredIndicatorVisible(true);
        nomField.getStyle()
                .set("--vaadin-input-field-border-radius", "6px")
                .set("--vaadin-input-field-border-width", "1px");

        nameContainer.add(prenomField, nomField);

        // Champs email et téléphone
        emailField = new EmailField("Email *");
        emailField.setWidthFull();
        emailField.setPlaceholder("exemple@email.com");
        emailField.setRequiredIndicatorVisible(true);
        emailField.getStyle()
                .set("--vaadin-input-field-border-radius", "6px")
                .set("--vaadin-input-field-border-width", "1px")
                .set("margin-bottom", "15px");

        telephoneField = new TextField("Téléphone");
        telephoneField.setWidthFull();
        telephoneField.setPlaceholder("+212 6XX XXX XXX");
        telephoneField.getStyle()
                .set("--vaadin-input-field-border-radius", "6px")
                .set("--vaadin-input-field-border-width", "1px")
                .set("margin-bottom", "15px");

        // Type de compte
        Paragraph sectionTitle2 = new Paragraph("Type de compte");
        sectionTitle2.getStyle()
                .set("font-weight", "600")
                .set("color", "#333")
                .set("margin", "25px 0 10px 0")
                .set("font-size", "14px");

        roleComboBox = new ComboBox<>("Sélectionnez votre rôle *");
        roleComboBox.setItems(UserRole.CLIENT, UserRole.ORGANIZER);
        roleComboBox.setItemLabelGenerator(role -> {
            switch(role) {
                case CLIENT: return "Client - Réserver des événements";
                case ORGANIZER: return "Organisateur - Créer des événements";
                default: return role.name();
            }
        });
        roleComboBox.setValue(UserRole.CLIENT);
        roleComboBox.setWidthFull();
        roleComboBox.setRequiredIndicatorVisible(true);
        roleComboBox.getStyle()
                .set("--vaadin-input-field-border-radius", "6px")
                .set("--vaadin-input-field-border-width", "1px")
                .set("margin-bottom", "25px");

        // Sécurité
        Paragraph sectionTitle3 = new Paragraph("Sécurité");
        sectionTitle3.getStyle()
                .set("font-weight", "600")
                .set("color", "#333")
                .set("margin", "25px 0 10px 0")
                .set("font-size", "14px");

        passwordField = new PasswordField("Mot de passe *");
        passwordField.setWidthFull();
        passwordField.setPlaceholder("Minimum 8 caractères");
        passwordField.setRequiredIndicatorVisible(true);
        passwordField.getStyle()
                .set("--vaadin-input-field-border-radius", "6px")
                .set("--vaadin-input-field-border-width", "1px")
                .set("margin-bottom", "15px");

        confirmPasswordField = new PasswordField("Confirmer le mot de passe *");
        confirmPasswordField.setWidthFull();
        confirmPasswordField.setRequiredIndicatorVisible(true);
        confirmPasswordField.getStyle()
                .set("--vaadin-input-field-border-radius", "6px")
                .set("--vaadin-input-field-border-width", "1px")
                .set("margin-bottom", "25px");

        // Bouton d'inscription
        Button registerButton = new Button("Créer mon compte");
        registerButton.setWidthFull();
        registerButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        registerButton.getStyle()
                .set("height", "44px")
                .set("font-weight", "600")
                .set("border-radius", "6px")
                .set("font-size", "16px")
                .set("margin-bottom", "20px");
        registerButton.addClickListener(e -> handleRegister());

        // Lien de connexion
        Paragraph loginSection = new Paragraph();
        loginSection.getStyle()
                .set("text-align", "center")
                .set("margin", "0")
                .set("color", "#666")
                .set("font-size", "14px");

        Button loginButton = new Button("Se connecter");
        loginButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        loginButton.getStyle()
                .set("color", "#1976d2")
                .set("font-weight", "500")
                .set("margin-left", "5px");
        loginButton.addClickListener(e -> UI.getCurrent().navigate("login"));

        loginSection.add(new Paragraph("Vous avez déjà un compte ? "));
        loginSection.add(loginButton);



        // Ajout de tous les composants au formulaire
        formContainer.add(
                mainTitle,
                subtitle,
                sectionTitle1,
                nameContainer,
                emailField,
                telephoneField,
                sectionTitle2,
                roleComboBox,
                sectionTitle3,
                passwordField,
                confirmPasswordField,
                registerButton,
                loginSection
        );

        leftPanel.add(formContainer);
        leftPanel.setHorizontalComponentAlignment(Alignment.CENTER, formContainer);

        // Partie droite - Photo/Illustration
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
                .set("background", "rgba(25, 118, 210, 0.85)")
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
        Span icon = new Span("🌟");
        icon.getStyle()
                .set("font-size", "72px")
                .set("margin-bottom", "30px");

        H1 welcomeTitle = new H1("Rejoignez notre communauté !");
        welcomeTitle.getStyle()
                .set("color", "white")
                .set("font-size", "38px")
                .set("font-weight", "bold")
                .set("margin", "0 0 25px 0")
                .set("line-height", "1.3");

        Paragraph welcomeText = new Paragraph(
                "Inscrivez-vous pour commencer votre aventure avec Event Booking. " +
                        "Que vous soyez client ou organisateur, notre plateforme vous offre tous " +
                        "les outils nécessaires pour une expérience événementielle exceptionnelle."
        );
        welcomeText.getStyle()
                .set("color", "rgba(255, 255, 255, 0.95)")
                .set("font-size", "18px")
                .set("max-width", "500px")
                .set("line-height", "1.6")
                .set("margin-bottom", "40px");

        // Avantages selon le rôle
        Div advantages = new Div();
        advantages.getStyle()
                .set("text-align", "left")
                .set("max-width", "500px");

        H3 clientAdvantages = new H3("🎟️  Pour les Clients :");
        clientAdvantages.getStyle()
                .set("color", "white")
                .set("margin", "20px 0 10px 0")
                .set("font-size", "18px");

        String[] clientBenefits = {
                "• Réservation facile en quelques clics",
                "• Gestion centralisée de vos billets",
                "• Recommandations personnalisées",
                "• Alertes pour vos événements favoris"
        };

        for (String benefit : clientBenefits) {
            Paragraph benefitItem = new Paragraph(benefit);
            benefitItem.getStyle()
                    .set("color", "rgba(255, 255, 255, 0.9)")
                    .set("font-size", "16px")
                    .set("margin", "8px 0 8px 15px");
            advantages.add(benefitItem);
        }

        H3 organizerAdvantages = new H3("📅  Pour les Organisateurs :");
        organizerAdvantages.getStyle()
                .set("color", "white")
                .set("margin", "25px 0 10px 0")
                .set("font-size", "18px");

        String[] organizerBenefits = {
                "• Création et gestion d'événements",
                "• Analyse des statistiques de participation",
                "• Outils de promotion intégrés",
                "• Gestion simplifiée des participants"
        };

        for (String benefit : organizerBenefits) {
            Paragraph benefitItem = new Paragraph(benefit);
            benefitItem.getStyle()
                    .set("color", "rgba(255, 255, 255, 0.9)")
                    .set("font-size", "16px")
                    .set("margin", "8px 0 8px 15px");
            advantages.add(benefitItem);
        }

        // Statistiques ou témoignage
        Div testimonial = new Div();
        testimonial.getStyle()
                .set("background", "rgba(255, 255, 255, 0.15)")
                .set("border-radius", "10px")
                .set("padding", "20px")
                .set("margin-top", "40px")
                .set("max-width", "500px")
                .set("text-align", "center");

        Paragraph testimonialText = new Paragraph("\"Event Booking a transformé ma façon d'organiser des événements. Simple, efficace et professionnel !\"");
        testimonialText.getStyle()
                .set("color", "white")
                .set("font-style", "italic")
                .set("font-size", "16px")
                .set("margin", "0 0 10px 0");

        Paragraph testimonialAuthor = new Paragraph("- Sarah M., Organisatrice d'événements");
        testimonialAuthor.getStyle()
                .set("color", "rgba(255, 255, 255, 0.8)")
                .set("font-size", "14px");

        testimonial.add(testimonialText, testimonialAuthor);

        imageContent.add(icon, welcomeTitle, welcomeText, advantages, testimonial);
        rightPanel.add(backgroundImage, overlay, imageContent);

        // Ajout des deux panneaux au layout principal
        mainLayout.add(leftPanel, rightPanel);
        add(mainLayout);
    }

    private Image createBackgroundImage() {
        try {
            // Essayez de charger une image depuis les ressources
            InputStream imageStream = getClass().getResourceAsStream("/images/register-background.jpg");
            if (imageStream != null) {
                StreamResource resource = new StreamResource("register-background.jpg", () -> imageStream);
                return new Image(resource, "Background d'inscription");
            }
        } catch (Exception e) {
            // Si l'image n'existe pas, on utilise une couleur de fond
        }

        // Fallback - Image SVG générée pour l'inscription
        String svgBackground = """
            <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 800 600" preserveAspectRatio="xMidYMid slice">
                <defs>
                    <linearGradient id="gradient" x1="0%" y1="0%" x2="100%" y2="100%">
                        <stop offset="0%" style="stop-color:#1976d2;stop-opacity:1" />
                        <stop offset="100%" style="stop-color:#2196f3;stop-opacity:1" />
                    </linearGradient>
                    <pattern id="pattern" x="0" y="0" width="120" height="120" patternUnits="userSpaceOnUse">
                        <circle cx="60" cy="60" r="40" fill="none" stroke="rgba(255,255,255,0.1)" stroke-width="2"/>
                        <path d="M30,30 L90,90 M90,30 L30,90" stroke="rgba(255,255,255,0.1)" stroke-width="2"/>
                        <circle cx="60" cy="60" r="20" fill="none" stroke="rgba(255,255,255,0.1)" stroke-width="1"/>
                    </pattern>
                </defs>
                <rect width="100%" height="100%" fill="url(#gradient)"/>
                <rect width="100%" height="100%" fill="url(#pattern)" opacity="0.15"/>
                
                <!-- Formes décoratives -->
                <circle cx="150" cy="150" r="100" fill="rgba(255,255,255,0.05)"/>
                <circle cx="650" cy="450" r="150" fill="rgba(255,255,255,0.05)"/>
                <circle cx="400" cy="200" r="80" fill="rgba(255,255,255,0.05)"/>
                <circle cx="300" cy="400" r="60" fill="rgba(255,255,255,0.05)"/>
                <circle cx="500" cy="100" r="70" fill="rgba(255,255,255,0.05)"/>
                
                <!-- Éléments événementiels -->
                <g opacity="0.2">
                    <rect x="200" y="80" width="40" height="60" rx="5" fill="rgba(255,255,255,0.3)" transform="rotate(15 220 110)"/>
                    <rect x="550" y="120" width="40" height="60" rx="5" fill="rgba(255,255,255,0.3)" transform="rotate(-10 570 150)"/>
                    <rect x="350" y="450" width="40" height="60" rx="5" fill="rgba(255,255,255,0.3)" transform="rotate(5 370 480)"/>
                </g>
            </svg>
            """;

        Image image = new Image();
        image.getElement().setProperty("innerHTML", svgBackground);
        return image;
    }

    private void handleRegister() {
        String nom = nomField.getValue();
        String prenom = prenomField.getValue();
        String email = emailField.getValue();
        String telephone = telephoneField.getValue();
        UserRole role = roleComboBox.getValue();
        String password = passwordField.getValue();
        String confirmPassword = confirmPasswordField.getValue();

        // Validations
        if (nom.isEmpty() || prenom.isEmpty() || email.isEmpty() || password.isEmpty()) {
            showNotification("Veuillez remplir tous les champs obligatoires (*)",
                    NotificationVariant.LUMO_ERROR);
            return;
        }

        if (password.length() < 8) {
            showNotification("Le mot de passe doit contenir au moins 8 caractères",
                    NotificationVariant.LUMO_ERROR);
            return;
        }

        if (!password.equals(confirmPassword)) {
            showNotification("Les mots de passe ne correspondent pas",
                    NotificationVariant.LUMO_ERROR);
            return;
        }

        if (role == null) {
            showNotification("Veuillez sélectionner un type de compte",
                    NotificationVariant.LUMO_ERROR);
            return;
        }

        try {
            // Vérifier si l'email existe déjà
            if (userService.existsByEmail(email)) {
                showNotification("Un compte avec cet email existe déjà",
                        NotificationVariant.LUMO_ERROR);
                return;
            }

            // Créer le nouvel utilisateur
            User newUser = userService.registerNewUser(nom, prenom, email, password, telephone, role);

            showNotification("Inscription réussie ! Vous pouvez maintenant vous connecter.",
                    NotificationVariant.LUMO_SUCCESS);

            UI.getCurrent().navigate("login");

        } catch (Exception e) {
            showNotification("Erreur lors de l'inscription: " + e.getMessage(),
                    NotificationVariant.LUMO_ERROR);
        }
    }

    private void showNotification(String message, NotificationVariant variant) {
        Notification notification = new Notification(message, 4000);
        notification.addThemeVariants(variant);
        notification.setPosition(Notification.Position.TOP_CENTER);
        notification.open();
    }
}