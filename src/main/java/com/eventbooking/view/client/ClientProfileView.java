package com.eventbooking.view.client;

import com.eventbooking.entity.User;
import com.eventbooking.service.UserService;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;
import org.springframework.beans.factory.annotation.Autowired;

@Route(value = "client/profile", layout = ClientMainLayout.class)
@PageTitle("Mon Profil - EventBooking")
public class ClientProfileView extends VerticalLayout {

    private final UserService userService;
    private User currentUser;

    private TextField nomField;
    private TextField prenomField;
    private EmailField emailField;
    private TextField telephoneField;

    @Autowired
    public ClientProfileView(UserService userService) {
        this.userService = userService;

        // Récupérer l'utilisateur connecté
        Object userObj = VaadinSession.getCurrent().getAttribute("currentUser");
        this.currentUser = (userObj instanceof User) ? (User) userObj : null;

        if (currentUser == null) {
            UI.getCurrent().navigate("login");
            return;
        }

        initView();
    }

    private void initView() {
        setSpacing(false);
        setPadding(true);
        setWidthFull();
        getStyle().set("background", "#f8f9fa");

        // Header
        HorizontalLayout header = new HorizontalLayout();
        header.setWidthFull();
        header.setAlignItems(Alignment.CENTER);
        header.setSpacing(true);

        Icon profileIcon = VaadinIcon.USER.create();
        profileIcon.setSize("32px");
        profileIcon.getStyle().set("color", "#667eea");

        H2 title = new H2("Mon Profil");
        title.getStyle()
                .set("margin", "0")
                .set("color", "#333");

        header.add(profileIcon, title);

        // Contenu principal
        VerticalLayout content = new VerticalLayout();
        content.setSpacing(true);
        content.setPadding(false);
        content.setWidthFull();

        // Section d'informations personnelles
        VerticalLayout infoSection = createInfoSection();

        // Section changement de mot de passe
        VerticalLayout passwordSection = createPasswordSection();

        content.add(infoSection, passwordSection);

        add(header, content);
    }

    private VerticalLayout createInfoSection() {
        VerticalLayout section = new VerticalLayout();
        section.setSpacing(true);
        section.setPadding(true);
        section.getStyle()
                .set("background", "white")
                .set("border-radius", "12px")
                .set("box-shadow", "0 2px 8px rgba(0,0,0,0.1)");

        H3 sectionTitle = new H3("Informations Personnelles");
        sectionTitle.getStyle()
                .set("margin", "0 0 20px 0")
                .set("color", "#667eea");

        // Formulaire
        FormLayout form = new FormLayout();
        form.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("500px", 2)
        );

        nomField = new TextField("Nom");
        nomField.setValue(currentUser.getNom());
        nomField.setWidthFull();

        prenomField = new TextField("Prénom");
        prenomField.setValue(currentUser.getPrenom());
        prenomField.setWidthFull();

        emailField = new EmailField("Email");
        emailField.setValue(currentUser.getEmail());
        emailField.setReadOnly(true);
        emailField.setWidthFull();

        telephoneField = new TextField("Téléphone");
        telephoneField.setValue(currentUser.getTelephone() != null ? currentUser.getTelephone() : "");
        telephoneField.setWidthFull();
        telephoneField.setPlaceholder("+212 6 XX XX XX XX");

        form.add(nomField, 1);
        form.add(prenomField, 1);
        form.add(emailField, 2);
        form.add(telephoneField, 1);

        // Boutons d'action
        HorizontalLayout buttons = new HorizontalLayout();
        buttons.setSpacing(true);

        Button saveBtn = new Button("Enregistrer", VaadinIcon.CHECK.create());
        saveBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        saveBtn.addClickListener(e -> saveProfile());

        Button cancelBtn = new Button("Annuler", VaadinIcon.CLOSE.create());
        cancelBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        cancelBtn.addClickListener(e -> resetForm());

        buttons.add(saveBtn, cancelBtn);

        section.add(sectionTitle, form, buttons);
        return section;
    }

    private VerticalLayout createPasswordSection() {
        VerticalLayout section = new VerticalLayout();
        section.setSpacing(true);
        section.setPadding(true);
        section.getStyle()
                .set("background", "white")
                .set("border-radius", "12px")
                .set("box-shadow", "0 2px 8px rgba(0,0,0,0.1)")
                .set("margin-top", "20px");

        H3 sectionTitle = new H3("Changer le mot de passe");
        sectionTitle.getStyle()
                .set("margin", "0 0 20px 0")
                .set("color", "#667eea");

        FormLayout form = new FormLayout();
        form.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 1));

        TextField currentPassword = new TextField("Mot de passe actuel");
        currentPassword.setWidthFull();
        currentPassword.setPlaceholder("Entrez votre mot de passe actuel");

        TextField newPassword = new TextField("Nouveau mot de passe");
        newPassword.setWidthFull();
        newPassword.setPlaceholder("Entrez le nouveau mot de passe");

        TextField confirmPassword = new TextField("Confirmer le mot de passe");
        confirmPassword.setWidthFull();
        confirmPassword.setPlaceholder("Confirmez le nouveau mot de passe");

        form.add(currentPassword, newPassword, confirmPassword);

        // Informations sur les exigences de mot de passe
        Paragraph passwordInfo = new Paragraph(
                "Le mot de passe doit contenir au moins 8 caractères, " +
                        "incluant une majuscule, une minuscule, un chiffre et un caractère spécial."
        );
        passwordInfo.getStyle()
                .set("color", "#666")
                .set("font-size", "12px")
                .set("margin", "10px 0");

        Button changePasswordBtn = new Button("Changer le mot de passe", VaadinIcon.LOCK.create());
        changePasswordBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SUCCESS);
        changePasswordBtn.addClickListener(e -> {
            if (validatePasswordChange(currentPassword.getValue(),
                    newPassword.getValue(),
                    confirmPassword.getValue())) {
                boolean changed = userService.changePassword(
                        currentUser.getId(),
                        currentPassword.getValue(),
                        newPassword.getValue()
                );

                if (changed) {
                    Notification.show("Mot de passe changé avec succès!", 3000, Notification.Position.MIDDLE);
                    currentPassword.clear();
                    newPassword.clear();
                    confirmPassword.clear();
                } else {
                    Notification.show("Mot de passe actuel incorrect!", 3000, Notification.Position.MIDDLE);
                }
            }
        });

        section.add(sectionTitle, form, passwordInfo, changePasswordBtn);
        return section;
    }

    private boolean validatePasswordChange(String current, String newPass, String confirm) {
        if (current == null || current.trim().isEmpty()) {
            Notification.show("Veuillez entrer votre mot de passe actuel", 3000, Notification.Position.MIDDLE);
            return false;
        }

        if (newPass == null || newPass.trim().isEmpty()) {
            Notification.show("Veuillez entrer un nouveau mot de passe", 3000, Notification.Position.MIDDLE);
            return false;
        }

        if (newPass.length() < 8) {
            Notification.show("Le mot de passe doit contenir au moins 8 caractères", 3000, Notification.Position.MIDDLE);
            return false;
        }

        if (!newPass.equals(confirm)) {
            Notification.show("Les mots de passe ne correspondent pas", 3000, Notification.Position.MIDDLE);
            return false;
        }

        return true;
    }

    private void saveProfile() {
        try {
            // Mettre à jour l'utilisateur
            currentUser.setNom(nomField.getValue());
            currentUser.setPrenom(prenomField.getValue());
            currentUser.setTelephone(telephoneField.getValue());

            // Sauvegarder via le service
            User updatedUser = userService.updateUser(currentUser.getId(), currentUser);

            // Mettre à jour la session
            VaadinSession.getCurrent().setAttribute("currentUser", updatedUser);

            Notification.show("Profil mis à jour avec succès!", 3000, Notification.Position.MIDDLE);
        } catch (Exception e) {
            Notification.show("Erreur lors de la mise à jour du profil: " + e.getMessage(),
                    5000, Notification.Position.MIDDLE);
        }
    }

    private void resetForm() {
        nomField.setValue(currentUser.getNom());
        prenomField.setValue(currentUser.getPrenom());
        telephoneField.setValue(currentUser.getTelephone() != null ? currentUser.getTelephone() : "");
    }
}