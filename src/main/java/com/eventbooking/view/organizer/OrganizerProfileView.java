package com.eventbooking.view.organizer;

import com.eventbooking.entity.User;
import com.eventbooking.entity.enums.UserRole;
import com.eventbooking.service.UserService;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.format.DateTimeFormatter;

@Route(value = "organizer/profile", layout = OrganizerMainLayout.class)
@PageTitle("Mon Profil | Event Booking")
@RolesAllowed("ORGANIZER")
public class OrganizerProfileView extends VerticalLayout {

    private final UserService userService;
    private User currentUser;

    @Autowired
    public OrganizerProfileView(UserService userService) {
        this.userService = userService;

        currentUser = (User) VaadinSession.getCurrent().getAttribute("currentUser");
        if (currentUser == null || currentUser.getRole() != UserRole.ORGANIZER) {
            UI.getCurrent().navigate("login");
            return;
        }

        setSizeFull();
        setPadding(false);
        setSpacing(false);

        createUI();
    }

    private void createUI() {
        // Header
        H1 title = new H1("👤 Mon Profil");
        title.getStyle()
                .set("margin", "1rem")
                .set("color", "var(--lumo-primary-text-color)");

        // Layout principal
        HorizontalLayout mainLayout = new HorizontalLayout();
        mainLayout.setSizeFull();
        mainLayout.setPadding(true);
        mainLayout.setSpacing(true);

        // Section gauche - Informations
        VerticalLayout infoSection = createInfoSection();
        infoSection.setWidth("40%");

        // Section droite - Édition
        VerticalLayout editSection = createEditSection();
        editSection.setWidth("60%");

        mainLayout.add(infoSection, editSection);
        add(title, mainLayout);
    }

    private VerticalLayout createInfoSection() {
        VerticalLayout layout = new VerticalLayout();
        layout.setPadding(true);
        layout.setSpacing(true);
        layout.getStyle()
                .set("border", "1px solid var(--lumo-contrast-10pct)")
                .set("border-radius", "var(--lumo-border-radius-m)")
                .set("background", "var(--lumo-base-color)");

        H2 title = new H2("📋 Informations Personnelles");
        title.getStyle().set("margin", "0 0 1rem 0");

        // Avatar
        HorizontalLayout avatarSection = new HorizontalLayout();
        avatarSection.setSpacing(true);
        avatarSection.setAlignItems(Alignment.CENTER);

        Icon avatar = new Icon(VaadinIcon.USER);
        avatar.setSize("64px");
        avatar.getStyle()
                .set("border-radius", "50%")
                .set("background", "var(--lumo-primary-color-10pct)")
                .set("color", "var(--lumo-primary-color)")
                .set("padding", "16px");

        VerticalLayout userInfo = new VerticalLayout();
        userInfo.setSpacing(false);
        userInfo.setPadding(false);

        H2 userName = new H2(currentUser.getPrenom() + " " + currentUser.getNom());
        userName.getStyle()
                .set("margin", "0")
                .set("font-size", "var(--lumo-font-size-xl)");

        Span role = new Span("Organisateur");
        role.getElement().getThemeList().add("badge success");

        userInfo.add(userName, role);
        avatarSection.add(avatar, userInfo);

        // Informations détaillées
        VerticalLayout details = new VerticalLayout();
        details.setSpacing(true);
        details.setPadding(true);
        details.getStyle()
                .set("border", "1px solid var(--lumo-contrast-10pct)")
                .set("border-radius", "var(--lumo-border-radius-s)")
                .set("background", "var(--lumo-contrast-5pct)");

        addDetailRow(details, "📧 Email", currentUser.getEmail());
        addDetailRow(details, "📱 Téléphone",
                currentUser.getTelephone() != null ?
                        currentUser.getTelephone() : "Non renseigné");
        addDetailRow(details, "📅 Date d'inscription",
                currentUser.getDateInscription().format(
                        DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
        addDetailRow(details, "✅ Statut",
                currentUser.getActif() ? "Actif" : "Inactif");

        layout.add(title, avatarSection, details);
        return layout;
    }

    private void addDetailRow(VerticalLayout layout, String label, String value) {
        HorizontalLayout row = new HorizontalLayout();
        row.setWidthFull();
        row.setJustifyContentMode(JustifyContentMode.BETWEEN);

        Span labelSpan = new Span(label);
        labelSpan.getStyle()
                .set("font-weight", "500")
                .set("color", "var(--lumo-secondary-text-color)");

        Span valueSpan = new Span(value);
        valueSpan.getStyle()
                .set("font-weight", "400");

        row.add(labelSpan, valueSpan);
        layout.add(row);
    }

    private VerticalLayout createEditSection() {
        VerticalLayout layout = new VerticalLayout();
        layout.setPadding(true);
        layout.setSpacing(true);
        layout.getStyle()
                .set("border", "1px solid var(--lumo-contrast-10pct)")
                .set("border-radius", "var(--lumo-border-radius-m)")
                .set("background", "var(--lumo-base-color)");

        H2 title = new H2("✏️ Modifier mes informations");
        title.getStyle().set("margin", "0 0 1rem 0");

        // Formulaire
        FormLayout form = new FormLayout();
        form.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("500px", 2)
        );

        TextField nomField = new TextField("Nom");
        nomField.setValue(currentUser.getNom());
        nomField.setWidthFull();

        TextField prenomField = new TextField("Prénom");
        prenomField.setValue(currentUser.getPrenom());
        prenomField.setWidthFull();

        TextField emailField = new TextField("Email");
        emailField.setValue(currentUser.getEmail());
        emailField.setWidthFull();

        TextField telephoneField = new TextField("Téléphone");
        telephoneField.setValue(currentUser.getTelephone() != null ?
                currentUser.getTelephone() : "");
        telephoneField.setWidthFull();

        form.add(nomField, 1);
        form.add(prenomField, 1);
        form.add(emailField, 2);
        form.add(telephoneField, 2);

        // Bouton de sauvegarde
        Button saveButton = new Button("Enregistrer les modifications",
                new Icon(VaadinIcon.CHECK));
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        saveButton.addClickListener(e -> {
            if (validateProfileForm(nomField, prenomField, emailField)) {
                updateProfile(nomField.getValue(), prenomField.getValue(),
                        emailField.getValue(), telephoneField.getValue());
            }
        });

        // Bouton changement de mot de passe
        Button changePasswordButton = new Button("Changer le mot de passe",
                new Icon(VaadinIcon.LOCK));
        changePasswordButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        changePasswordButton.addClickListener(e -> showChangePasswordDialog());

        HorizontalLayout buttons = new HorizontalLayout(saveButton, changePasswordButton);
        buttons.setSpacing(true);

        layout.add(title, form, buttons);
        return layout;
    }

    private boolean validateProfileForm(TextField nom, TextField prenom, TextField email) {
        if (nom.getValue() == null || nom.getValue().trim().isEmpty()) {
            Notification.show("Le nom est obligatoire", 3000,
                            Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            return false;
        }

        if (prenom.getValue() == null || prenom.getValue().trim().isEmpty()) {
            Notification.show("Le prénom est obligatoire", 3000,
                            Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            return false;
        }

        if (email.getValue() == null || email.getValue().trim().isEmpty()) {
            Notification.show("L'email est obligatoire", 3000,
                            Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            return false;
        }

        if (!email.getValue().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            Notification.show("Format d'email invalide", 3000,
                            Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            return false;
        }

        return true;
    }

    private void updateProfile(String nom, String prenom, String email, String telephone) {
        try {
            User updatedUser = new User();
            updatedUser.setNom(nom);
            updatedUser.setPrenom(prenom);
            updatedUser.setEmail(email);
            updatedUser.setTelephone(telephone);

            userService.updateUser(currentUser.getId(), updatedUser);

            // Mettre à jour l'utilisateur dans la session
            currentUser.setNom(nom);
            currentUser.setPrenom(prenom);
            currentUser.setEmail(email);
            currentUser.setTelephone(telephone);
            VaadinSession.getCurrent().setAttribute("currentUser", currentUser);

            Notification.show("Profil mis à jour avec succès", 3000,
                            Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);

            // Rafraîchir la page
            UI.getCurrent().getPage().reload();
        } catch (Exception e) {
            Notification.show("Erreur: " + e.getMessage(), 3000,
                            Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    private void showChangePasswordDialog() {
        Dialog dialog = new Dialog();
        dialog.setWidth("400px");
        dialog.setCloseOnOutsideClick(false);

        H2 title = new H2("🔒 Changer le mot de passe");
        title.getStyle().set("margin", "0 0 1rem 0");

        VerticalLayout form = new VerticalLayout();
        form.setSpacing(true);

        PasswordField currentPasswordField = new PasswordField("Mot de passe actuel");
        currentPasswordField.setRequired(true);
        currentPasswordField.setWidthFull();

        PasswordField newPasswordField = new PasswordField("Nouveau mot de passe");
        newPasswordField.setRequired(true);
        newPasswordField.setWidthFull();

        PasswordField confirmPasswordField = new PasswordField("Confirmer le nouveau mot de passe");
        confirmPasswordField.setRequired(true);
        confirmPasswordField.setWidthFull();

        HorizontalLayout buttons = new HorizontalLayout();
        buttons.setSpacing(true);

        Button saveButton = new Button("Changer", new Icon(VaadinIcon.CHECK));
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        saveButton.addClickListener(e -> {
            if (validatePasswordForm(currentPasswordField, newPasswordField, confirmPasswordField)) {
                changePassword(currentPasswordField.getValue(), newPasswordField.getValue(), dialog);
            }
        });

        Button cancelButton = new Button("Annuler", new Icon(VaadinIcon.CLOSE));
        cancelButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        cancelButton.addClickListener(e -> dialog.close());

        buttons.add(saveButton, cancelButton);

        form.add(title, currentPasswordField, newPasswordField, confirmPasswordField, buttons);
        dialog.add(form);
        dialog.open();
    }

    private boolean validatePasswordForm(PasswordField currentPassword,
                                         PasswordField newPassword,
                                         PasswordField confirmPassword) {
        if (currentPassword.getValue() == null || currentPassword.getValue().trim().isEmpty()) {
            Notification.show("Le mot de passe actuel est obligatoire", 3000,
                            Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            return false;
        }

        if (newPassword.getValue() == null || newPassword.getValue().trim().isEmpty()) {
            Notification.show("Le nouveau mot de passe est obligatoire", 3000,
                            Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            return false;
        }

        if (newPassword.getValue().length() < 6) {
            Notification.show("Le nouveau mot de passe doit contenir au moins 6 caractères", 3000,
                            Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            return false;
        }

        if (!newPassword.getValue().equals(confirmPassword.getValue())) {
            Notification.show("Les mots de passe ne correspondent pas", 3000,
                            Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            return false;
        }

        return true;
    }

    private void changePassword(String currentPassword, String newPassword, Dialog dialog) {
        try {
            boolean success = userService.changePassword(
                    currentUser.getId(), currentPassword, newPassword);

            if (success) {
                Notification.show("Mot de passe changé avec succès", 3000,
                                Notification.Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                dialog.close();
            } else {
                Notification.show("Mot de passe actuel incorrect", 3000,
                                Notification.Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        } catch (Exception e) {
            Notification.show("Erreur: " + e.getMessage(), 3000,
                            Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }
}