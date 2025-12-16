package com.eventbooking.view.admin;

import com.eventbooking.entity.User;
import com.eventbooking.entity.enums.UserRole;
import com.eventbooking.service.UserService;
import com.eventbooking.view.admin.components.AdminSidebar;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.data.validator.EmailValidator;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.spring.annotation.UIScope;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.format.DateTimeFormatter;
import java.util.Optional;

@Route(value = "admin/profile")
@PageTitle("Mon Profil")
@UIScope
public class AdminProfile extends HorizontalLayout implements BeforeEnterObserver {

    @Autowired
    private UserService userService;

    private User currentUser;
    private AdminSidebar sidebar;
    private VerticalLayout contentLayout;
    private Binder<User> binder;
    private TextField nomField;
    private TextField prenomField;
    private EmailField emailField;
    private TextField telephoneField;
    private Button saveButton;
    private Button cancelButton;
    private Button changePasswordButton;
    private boolean isEditing = false;

    public AdminProfile(@Autowired UserService userService) {
        this.userService = userService;
        setSizeFull();
        setPadding(false);
        setSpacing(false);

        // Récupérer l'utilisateur courant depuis la session
        currentUser = (User) VaadinSession.getCurrent().getAttribute("currentUser");

        if (currentUser == null) {
            UI.getCurrent().navigate("login");
            return;
        }

        // Vérifier si c'est un admin
        if (!currentUser.getRole().equals(UserRole.ADMIN)) {
            UI.getCurrent().navigate("");
            return;
        }

        initComponents();
    }

    private void initComponents() {
        // Créer la sidebar
        sidebar = new AdminSidebar(currentUser, "admin/profile");

        // Créer le contenu principal
        contentLayout = createContentLayout();

        // Ajouter les composants au layout
        add(sidebar, contentLayout);

        // Configurer les proportions
        setFlexGrow(1, contentLayout);
        sidebar.setWidth("280px");
    }

    private VerticalLayout createContentLayout() {
        VerticalLayout content = new VerticalLayout();
        content.setSizeFull();
        content.setPadding(false);
        content.setSpacing(false);
        content.getStyle()
                .set("background", "#f8fafc")
                .set("overflow-y", "auto");

        // En-tête
        createHeader(content);

        // Contenu principal
        createMainContent(content);

        return content;
    }

    private void createHeader(VerticalLayout parent) {
        HorizontalLayout header = new HorizontalLayout();
        header.setWidthFull();
        header.setPadding(true);
        header.setAlignItems(FlexComponent.Alignment.CENTER);
        header.getStyle()
                .set("background", "white")
                .set("box-shadow", "0 1px 3px rgba(0,0,0,0.1)")
                .set("border-bottom", "1px solid #e2e8f0")
                .set("position", "sticky")
                .set("top", "0")
                .set("z-index", "100");

        // Titre avec icône
        HorizontalLayout titleContainer = new HorizontalLayout();
        titleContainer.setSpacing(true);
        titleContainer.setAlignItems(FlexComponent.Alignment.CENTER);

        Icon profileIcon = VaadinIcon.USER.create();
        profileIcon.setSize("24px");
        profileIcon.setColor("#1e40af");

        H2 title = new H2("Mon Profil");
        title.getStyle()
                .set("margin", "0")
                .set("color", "#1e293b")
                .set("font-size", "24px")
                .set("font-weight", "600");

        titleContainer.add(profileIcon, title);

        // Espaceur
        Div spacer = new Div();
        spacer.getStyle().set("flex-grow", "1");

        header.add(titleContainer, spacer);
        parent.add(header);
    }

    private void createMainContent(VerticalLayout parent) {
        HorizontalLayout mainContent = new HorizontalLayout();
        mainContent.setSizeFull();
        mainContent.setPadding(true);
        mainContent.setSpacing(true);

        // Section gauche - Information du profil (35% de l'espace)
        VerticalLayout profileInfo = createProfileInfo();

        // Section droite - Formulaire d'édition (65% de l'espace)
        VerticalLayout editForm = createEditForm();

        mainContent.add(profileInfo, editForm);
        mainContent.setFlexGrow(0.35, profileInfo);  // 35% de l'espace
        mainContent.setFlexGrow(0.65, editForm);     // 65% de l'espace

        parent.add(mainContent);

        // Initialiser le binder avec les données de l'utilisateur
        binder = new Binder<>(User.class);
        binder.setBean(currentUser);
        bindFields();

        // Désactiver les champs par défaut
        setFieldsEnabled(false);
    }

    private VerticalLayout createProfileInfo() {
        VerticalLayout profileInfo = new VerticalLayout();
        profileInfo.setWidth("100%");
        profileInfo.setMaxWidth("400px");
        profileInfo.setPadding(false);
        profileInfo.setSpacing(false);
        profileInfo.getStyle()
                .set("border-radius", "12px")
                .set("background", "white")
                .set("box-shadow", "0 2px 8px rgba(0,0,0,0.08)")
                .set("border", "1px solid #e2e8f0")
                .set("overflow", "hidden");

        // Header avec dégradé - Pleine largeur
        Div headerSection = new Div();
        headerSection.getStyle()
                .set("background", "linear-gradient(90deg, #1e40af 0%, #3b82f6 100%)")
                .set("padding", "40px 20px")
                .set("text-align", "center")
                .set("display", "flex")
                .set("flex-direction", "column")
                .set("align-items", "center")
                .set("justify-content", "center")
                .set("width", "100%");

        // Conteneur principal pour centrer tout le contenu
        VerticalLayout headerContent = new VerticalLayout();
        headerContent.setSpacing(false);
        headerContent.setPadding(false);
        headerContent.setAlignItems(FlexComponent.Alignment.CENTER);
        headerContent.getStyle()
                .set("width", "100%")
                .set("max-width", "300px");

        // Avatar
        Div avatar = new Div();
        avatar.getStyle()
                .set("width", "100px")
                .set("height", "100px")
                .set("background", "linear-gradient(135deg, #ffffff 0%, rgba(255,255,255,0.8) 100%)")
                .set("border-radius", "50%")
                .set("margin", "0 auto 20px auto")
                .set("display", "flex")
                .set("align-items", "center")
                .set("justify-content", "center")
                .set("border", "4px solid rgba(255,255,255,0.3)");

        Icon userIcon = VaadinIcon.USER.create();
        userIcon.setSize("40px");
        userIcon.setColor("#1e40af");
        avatar.add(userIcon);

        // Nom - Pleine largeur, texte centré
        H4 userName = new H4(currentUser.getPrenom() + " " + currentUser.getNom());
        userName.getStyle()
                .set("margin", "0 0 10px 0")
                .set("color", "white")
                .set("font-weight", "700")
                .set("font-size", "1.5rem")
                .set("text-align", "center")
                .set("width", "100%")
                .set("line-height", "1.2");

        // Conteneur pour le rôle et le badge
        VerticalLayout roleContainer = new VerticalLayout();
        roleContainer.setSpacing(false);
        roleContainer.setPadding(false);
        roleContainer.setAlignItems(FlexComponent.Alignment.CENTER);
        roleContainer.getStyle()
                .set("width", "100%")
                .set("margin-bottom", "5px");

        // Rôle - Administrateur
        Paragraph role = new Paragraph("Administrateur");
        role.getStyle()
                .set("margin", "0 0 8px 0")
                .set("color", "rgba(255,255,255,0.95)")
                .set("font-size", "1.1rem")
                .set("font-weight", "500")
                .set("text-align", "center")
                .set("width", "100%");

        // Badge ADMIN
        Div adminBadge = new Div();
        adminBadge.setText("ADMIN");
        adminBadge.getStyle()
                .set("display", "inline-block")
                .set("background", "rgba(255,255,255,0.2)")
                .set("color", "white")
                .set("padding", "6px 16px")
                .set("border-radius", "20px")
                .set("font-size", "0.8rem")
                .set("font-weight", "700")
                .set("backdrop-filter", "blur(10px)")
                .set("letter-spacing", "1px")
                .set("margin", "0 auto")
                .set("text-transform", "uppercase");

        roleContainer.add(role, adminBadge);
        headerContent.add(avatar, userName, roleContainer);
        headerSection.add(headerContent);

        // Informations détaillées
        VerticalLayout infoSection = new VerticalLayout();
        infoSection.setPadding(true);
        infoSection.setSpacing(true);
        infoSection.getStyle().set("background", "white");





        // Cards de statistiques
        HorizontalLayout statsCards = new HorizontalLayout();
        statsCards.setSpacing(true);
        statsCards.setWidthFull();

        // Calculer les jours depuis l'inscription
        long daysSinceRegistration = java.time.temporal.ChronoUnit.DAYS.between(
                currentUser.getDateInscription().toLocalDate(),
                java.time.LocalDate.now()
        );

        statsCards.add(
                createStatCard("Membre depuis", String.valueOf(daysSinceRegistration), "jours", VaadinIcon.CALENDAR_CLOCK, "#3b82f6"),
                createStatCard("Activité", currentUser.getActif() ? "Actif" : "Inactif", "statut", VaadinIcon.CHART, "#10b981")
        );

        infoSection.add(statsCards);

        profileInfo.add(headerSection, infoSection);
        return profileInfo;
    }

    private Div createInfoRow(String label, Object value, VaadinIcon icon) {
        Div row = new Div();
        row.getStyle()
                .set("display", "flex")
                .set("align-items", "flex-start")
                .set("margin-bottom", "12px");

        Icon rowIcon = icon.create();
        rowIcon.setSize("16px");
        rowIcon.getStyle()
                .set("color", "#64748b")
                .set("margin-right", "12px")
                .set("flex-shrink", "0")
                .set("margin-top", "2px");

        VerticalLayout contentLayout = new VerticalLayout();
        contentLayout.setSpacing(false);
        contentLayout.setPadding(false);

        Div labelDiv = new Div(label);
        labelDiv.getStyle()
                .set("font-weight", "500")
                .set("color", "#475569")
                .set("font-size", "0.85rem")
                .set("margin-bottom", "2px");

        Div valueDiv = new Div();

        // Gérer les différents types de valeur
        if (value instanceof String) {
            valueDiv.setText((String) value);
        } else if (value instanceof Div) {
            // Si c'est un Div (comme un badge), on l'ajoute directement
            valueDiv.removeAll();
            valueDiv.add((Div) value);
        } else {
            valueDiv.setText(String.valueOf(value));
        }

        valueDiv.getStyle()
                .set("color", "#1e293b")
                .set("font-size", "0.95rem")
                .set("font-weight", "400")
                .set("word-break", "break-word");

        contentLayout.add(labelDiv, valueDiv);
        row.add(rowIcon, contentLayout);

        return row;
    }

    private Div createStatusBadge(Boolean active) {
        Div badge = new Div();
        badge.getStyle()
                .set("display", "inline-flex")
                .set("align-items", "center")
                .set("gap", "5px")
                .set("padding", "4px 10px")
                .set("border-radius", "20px")
                .set("font-size", "0.75rem")
                .set("font-weight", "600");

        if (active) {
            badge.setText("Actif");
            badge.getStyle()
                    .set("background", "#d1fae5")
                    .set("color", "#065f46");
        } else {
            badge.setText("Inactif");
            badge.getStyle()
                    .set("background", "#fee2e2")
                    .set("color", "#991b1b");
        }
        return badge;
    }

    private Div createStatCard(String title, String value, String unit, VaadinIcon icon, String color) {
        Div card = new Div();
        card.getStyle()
                .set("background", "white")
                .set("border", "1px solid #e2e8f0")
                .set("border-radius", "8px")
                .set("padding", "15px")
                .set("flex-grow", "1")
                .set("min-width", "120px")
                .set("text-align", "center");

        // Icône
        Div iconContainer = new Div();
        iconContainer.getStyle()
                .set("width", "36px")
                .set("height", "36px")
                .set("background", color + "15")
                .set("border-radius", "8px")
                .set("display", "flex")
                .set("align-items", "center")
                .set("justify-content", "center")
                .set("margin", "0 auto 10px auto");

        Icon cardIcon = icon.create();
        cardIcon.setSize("18px");
        cardIcon.setColor(color);
        iconContainer.add(cardIcon);

        // Valeur
        Div valueDiv = new Div(value);
        valueDiv.getStyle()
                .set("font-size", "1.25rem")
                .set("font-weight", "700")
                .set("color", "#1e293b")
                .set("line-height", "1")
                .set("margin-bottom", "4px");

        // Titre
        Div titleDiv = new Div(title);
        titleDiv.getStyle()
                .set("font-size", "0.8rem")
                .set("color", "#64748b")
                .set("margin-bottom", "2px");

        // Unité
        Div unitDiv = new Div(unit);
        unitDiv.getStyle()
                .set("font-size", "0.75rem")
                .set("color", color)
                .set("font-weight", "600");

        card.add(iconContainer, valueDiv, titleDiv, unitDiv);
        return card;
    }

    private VerticalLayout createEditForm() {
        VerticalLayout editForm = new VerticalLayout();
        editForm.setPadding(true);
        editForm.setSpacing(true);
        editForm.getStyle()
                .set("background", "white")
                .set("border-radius", "12px")
                .set("box-shadow", "0 2px 8px rgba(0,0,0,0.08)")
                .set("border", "1px solid #e2e8f0")
                .set("flex-grow", "1")
                .set("min-height", "500px");

        // Header du formulaire
        HorizontalLayout formHeader = new HorizontalLayout();
        formHeader.setWidthFull();
        formHeader.setAlignItems(FlexComponent.Alignment.CENTER);
        formHeader.getStyle().set("margin-bottom", "10px");

        H4 formTitle = new H4("Modifier mes informations");
        formTitle.getStyle()
                .set("margin", "0")
                .set("color", "#1e293b")
                .set("font-weight", "600")
                .set("font-size", "1.25rem");

        Div spacer = new Div();
        spacer.getStyle().set("flex-grow", "1");

        Button editButton = new Button("Modifier", new Icon(VaadinIcon.EDIT));
        editButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        editButton.addClickListener(e -> toggleEditMode());

        formHeader.add(formTitle, spacer, editButton);

        // Formulaire
        FormLayout formLayout = new FormLayout();
        formLayout.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("500px", 2)
        );
        formLayout.getStyle()
                .set("padding", "20px 0")
                .set("flex-grow", "1");

        // Champs du formulaire
        nomField = new TextField("Nom");
        nomField.setWidthFull();
        nomField.setHeight("56px");

        prenomField = new TextField("Prénom");
        prenomField.setWidthFull();
        prenomField.setHeight("56px");

        emailField = new EmailField("Email");
        emailField.setWidthFull();
        emailField.setHeight("56px");
        emailField.setErrorMessage("Email invalide");

        telephoneField = new TextField("Téléphone");
        telephoneField.setWidthFull();
        telephoneField.setHeight("56px");

        formLayout.add(nomField, prenomField, emailField, telephoneField);

        // Boutons d'action
        HorizontalLayout buttonLayout = new HorizontalLayout();
        buttonLayout.setSpacing(true);
        buttonLayout.setPadding(true);
        buttonLayout.getStyle()
                .set("margin-top", "20px")
                .set("border-top", "1px solid #e2e8f0")
                .set("padding-top", "20px");

        saveButton = new Button("Enregistrer", new Icon(VaadinIcon.CHECK));
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SUCCESS);
        saveButton.setVisible(false);
        saveButton.addClickListener(e -> saveProfile());

        cancelButton = new Button("Annuler", new Icon(VaadinIcon.CLOSE));
        cancelButton.addThemeVariants(ButtonVariant.LUMO_ERROR);
        cancelButton.setVisible(false);
        cancelButton.addClickListener(e -> cancelEdit());

        changePasswordButton = new Button("Changer le mot de passe", new Icon(VaadinIcon.LOCK));
        changePasswordButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        changePasswordButton.addClickListener(e -> openChangePasswordDialog());

        buttonLayout.add(saveButton, cancelButton, changePasswordButton);

        editForm.add(formHeader, formLayout, buttonLayout);
        return editForm;
    }

    private void bindFields() {
        binder.forField(nomField)
                .asRequired("Le nom est requis")
                .bind(User::getNom, User::setNom);

        binder.forField(prenomField)
                .asRequired("Le prénom est requis")
                .bind(User::getPrenom, User::setPrenom);

        binder.forField(emailField)
                .asRequired("L'email est requis")
                .withValidator(new EmailValidator("Email invalide"))
                .bind(User::getEmail, User::setEmail);

        binder.forField(telephoneField)
                .bind(User::getTelephone, User::setTelephone);
    }

    private void setFieldsEnabled(boolean enabled) {
        nomField.setEnabled(enabled);
        prenomField.setEnabled(enabled);
        emailField.setEnabled(enabled);
        telephoneField.setEnabled(enabled);
        saveButton.setVisible(enabled);
        cancelButton.setVisible(enabled);
        isEditing = enabled;
    }

    private void toggleEditMode() {
        if (isEditing) {
            cancelEdit();
        } else {
            setFieldsEnabled(true);
            // Mettre à jour le binder avec les données actuelles
            binder.readBean(currentUser);
        }
    }

    private void saveProfile() {
        try {
            // Valider et écrire les données
            binder.writeBean(currentUser);

            // Vérifier si l'email existe déjà pour un autre utilisateur
            Optional<User> existingUser = userService.getUserByEmail(currentUser.getEmail());
            if (existingUser.isPresent() && !existingUser.get().getId().equals(currentUser.getId())) {
                Notification.show("Cet email est déjà utilisé par un autre compte")
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
                return;
            }

            // Mettre à jour l'utilisateur
            User updatedUser = userService.updateUser(currentUser.getId(), currentUser);

            // Mettre à jour la session
            VaadinSession.getCurrent().setAttribute("currentUser", updatedUser);
            currentUser = updatedUser;

            // Désactiver l'édition
            setFieldsEnabled(false);

            Notification.show("Profil mis à jour avec succès")
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);

        } catch (ValidationException e) {
            Notification.show("Veuillez corriger les erreurs dans le formulaire")
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    private void cancelEdit() {
        // Restaurer les valeurs originales
        binder.readBean(currentUser);
        setFieldsEnabled(false);
    }

    private void openChangePasswordDialog() {
        Dialog dialog = new Dialog();
        dialog.setWidth("400px");
        dialog.setCloseOnEsc(true);
        dialog.setCloseOnOutsideClick(false);

        VerticalLayout dialogLayout = new VerticalLayout();
        dialogLayout.setSpacing(true);
        dialogLayout.setPadding(true);

        H4 dialogTitle = new H4("Changer le mot de passe");
        dialogTitle.getStyle()
                .set("margin", "0 0 20px 0")
                .set("color", "#1e293b");

        // Champs pour le mot de passe
        PasswordField oldPasswordField = new PasswordField("Mot de passe actuel");
        oldPasswordField.setWidthFull();
        oldPasswordField.setPlaceholder("Entrez votre mot de passe actuel");

        PasswordField newPasswordField = new PasswordField("Nouveau mot de passe");
        newPasswordField.setWidthFull();
        newPasswordField.setPlaceholder("Entrez le nouveau mot de passe");

        PasswordField confirmPasswordField = new PasswordField("Confirmer le mot de passe");
        confirmPasswordField.setWidthFull();
        confirmPasswordField.setPlaceholder("Confirmez le nouveau mot de passe");

        // Indicateur de force du mot de passe
        ProgressBar strengthBar = new ProgressBar();
        strengthBar.setWidthFull();
        strengthBar.setVisible(false);
        strengthBar.getStyle()
                .set("height", "6px")
                .set("margin-top", "5px")
                .set("border-radius", "3px");

        Paragraph strengthText = new Paragraph();
        strengthText.getStyle()
                .set("font-size", "12px")
                .set("margin-top", "2px")
                .set("margin-bottom", "0");
        strengthText.setVisible(false);

        // Écouter les changements pour évaluer la force
        newPasswordField.addValueChangeListener(e -> {
            String password = e.getValue();
            if (password != null && !password.isEmpty()) {
                double strength = calculatePasswordStrength(password);
                strengthBar.setValue(strength);
                strengthBar.setVisible(true);
                strengthText.setVisible(true);

                // Mettre à jour la couleur selon la force
                if (strength < 0.4) {
                    strengthBar.getStyle().set("--lumo-primary-color", "#ef4444");
                    strengthText.setText("Faible");
                    strengthText.getStyle().set("color", "#ef4444");
                } else if (strength < 0.7) {
                    strengthBar.getStyle().set("--lumo-primary-color", "#f59e0b");
                    strengthText.setText("Moyen");
                    strengthText.getStyle().set("color", "#f59e0b");
                } else {
                    strengthBar.getStyle().set("--lumo-primary-color", "#10b981");
                    strengthText.setText("Fort");
                    strengthText.getStyle().set("color", "#10b981");
                }
            } else {
                strengthBar.setVisible(false);
                strengthText.setVisible(false);
            }
        });

        // Boutons du dialog
        HorizontalLayout dialogButtons = new HorizontalLayout();
        dialogButtons.setSpacing(true);
        dialogButtons.getStyle().set("margin-top", "20px");

        Button confirmButton = new Button("Confirmer", new Icon(VaadinIcon.CHECK));
        confirmButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button cancelButtonDialog = new Button("Annuler", new Icon(VaadinIcon.CLOSE));
        cancelButtonDialog.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        dialogButtons.add(confirmButton, cancelButtonDialog);

        confirmButton.addClickListener(e -> {
            String oldPassword = oldPasswordField.getValue();
            String newPassword = newPasswordField.getValue();
            String confirmPassword = confirmPasswordField.getValue();

            // Validation
            if (oldPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
                Notification.show("Tous les champs sont requis")
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
                return;
            }

            if (!newPassword.equals(confirmPassword)) {
                Notification.show("Les mots de passe ne correspondent pas")
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
                return;
            }

            if (newPassword.length() < 6) {
                Notification.show("Le mot de passe doit contenir au moins 6 caractères")
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
                return;
            }

            // Changer le mot de passe
            boolean success = userService.changePassword(currentUser.getId(), oldPassword, newPassword);

            if (success) {
                Notification.show("Mot de passe changé avec succès")
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                dialog.close();
            } else {
                Notification.show("Mot de passe actuel incorrect")
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });

        cancelButtonDialog.addClickListener(e -> dialog.close());

        // Ajouter les composants au layout
        VerticalLayout passwordStrengthLayout = new VerticalLayout();
        passwordStrengthLayout.setSpacing(false);
        passwordStrengthLayout.setPadding(false);
        passwordStrengthLayout.add(strengthBar, strengthText);

        dialogLayout.add(dialogTitle, oldPasswordField, newPasswordField,
                confirmPasswordField, passwordStrengthLayout, dialogButtons);
        dialog.add(dialogLayout);
        dialog.open();
    }

    // Méthode pour calculer la force du mot de passe
    private double calculatePasswordStrength(String password) {
        if (password == null || password.isEmpty()) {
            return 0;
        }

        double strength = 0;
        int length = password.length();

        // Points pour la longueur
        strength += Math.min(length / 20.0, 0.3);

        // Points pour la complexité
        if (password.matches(".*[A-Z].*")) strength += 0.2;
        if (password.matches(".*[a-z].*")) strength += 0.1;
        if (password.matches(".*\\d.*")) strength += 0.2;
        if (password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?].*")) strength += 0.2;

        return Math.min(strength, 1.0);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        // Vérifier l'authentification et les autorisations
        if (currentUser == null || !currentUser.getRole().equals(UserRole.ADMIN)) {
            event.forwardTo("login");
        }
    }
}