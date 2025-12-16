package com.eventbooking.view.admin;

import com.eventbooking.entity.User;
import com.eventbooking.entity.enums.UserRole;
import com.eventbooking.service.UserService;
import com.eventbooking.view.admin.components.AdminSidebar;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;
import jakarta.annotation.security.PermitAll;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Route("admin/users")
@PageTitle("Gestion Utilisateurs | Event Booking Admin")
@PermitAll
public class UserManagementView extends HorizontalLayout {

    @Autowired
    private UserService userService;

    private Grid<User> userGrid;
    private ComboBox<UserRole> roleFilter;
    private TextField searchField;
    private User currentUser;

    public UserManagementView(UserService userService) {
        this.userService = userService;

        setSizeFull();
        setPadding(false);
        setSpacing(false);
        getStyle().set("overflow", "hidden");

        currentUser = (User) VaadinSession.getCurrent().getAttribute("currentUser");

        if (currentUser == null || currentUser.getRole() != UserRole.ADMIN) {
            add(new H2("Accès refusé - Administrateurs uniquement"));
            return;
        }

        createUI();
        loadUsers();
    }

    private void createUI() {
        // Ajouter la sidebar
        AdminSidebar sidebar = new AdminSidebar(currentUser, "admin/users");

        // Créer la zone de contenu
        VerticalLayout contentArea = createContentArea();

        add(sidebar, contentArea);
    }

    private VerticalLayout createContentArea() {
        VerticalLayout content = new VerticalLayout();
        content.setSizeFull();
        content.setPadding(true);
        content.setSpacing(true);
        content.getStyle()
                .set("background", "#f8fafc")
                .set("overflow-y", "auto");

        // Header
        HorizontalLayout header = createHeader();

        // Filters
        HorizontalLayout filters = createFilters();

        // Grid
        userGrid = createUserGrid();

        content.add(header, filters, userGrid);
        return content;
    }

    private HorizontalLayout createHeader() {
        HorizontalLayout header = new HorizontalLayout();
        header.setWidthFull();
        header.setJustifyContentMode(JustifyContentMode.BETWEEN);
        header.setAlignItems(Alignment.CENTER);

        H1 title = new H1("👥 Gestion des Utilisateurs");
        title.getStyle().set("margin", "0").set("color", "#1976d2");

        HorizontalLayout actions = new HorizontalLayout();
        actions.setSpacing(true);

        Button addUserBtn = new Button("Ajouter Utilisateur", new Icon(VaadinIcon.PLUS));
        addUserBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        addUserBtn.addClickListener(e -> openUserDialog(null));

        actions.add(addUserBtn);
        header.add(title, actions);

        return header;
    }

    private HorizontalLayout createFilters() {
        HorizontalLayout filters = new HorizontalLayout();
        filters.setWidthFull();
        filters.setSpacing(true);
        filters.setAlignItems(Alignment.END);

        searchField = new TextField("Rechercher");
        searchField.setPlaceholder("Nom, prénom ou email...");
        searchField.setPrefixComponent(new Icon(VaadinIcon.SEARCH));
        searchField.setWidth("300px");
        searchField.addValueChangeListener(e -> filterUsers());

        roleFilter = new ComboBox<>("Rôle");
        roleFilter.setItems(UserRole.values());
        roleFilter.setItemLabelGenerator(role -> getRoleLabel(role));
        roleFilter.setPlaceholder("Tous les rôles");
        roleFilter.setClearButtonVisible(true);
        roleFilter.addValueChangeListener(e -> filterUsers());

        Button refreshBtn = new Button("🔄 Actualiser");
        refreshBtn.addClickListener(e -> loadUsers());

        filters.add(searchField, roleFilter, refreshBtn);
        return filters;
    }

    private Grid<User> createUserGrid() {
        Grid<User> grid = new Grid<>(User.class, false);
        grid.setHeight("600px");

        grid.addColumn(User::getId)
                .setHeader("ID")
                .setWidth("80px")
                .setFlexGrow(0);

        grid.addColumn(user -> user.getNom() + " " + user.getPrenom())
                .setHeader("Nom Complet")
                .setSortable(true);

        grid.addColumn(User::getEmail)
                .setHeader("Email")
                .setSortable(true);

        grid.addColumn(User::getTelephone)
                .setHeader("Téléphone");

        grid.addColumn(new ComponentRenderer<>(user -> {
            Span badge = new Span(getRoleLabel(user.getRole()));
            badge.getElement().getThemeList().add("badge");
            badge.getStyle()
                    .set("background", getRoleColor(user.getRole()))
                    .set("color", "white")
                    .set("padding", "5px 10px")
                    .set("border-radius", "12px")
                    .set("font-size", "12px")
                    .set("font-weight", "bold");
            return badge;
        })).setHeader("Rôle").setWidth("150px");

        grid.addColumn(new ComponentRenderer<>(user -> {
            Span statusBadge = new Span(user.getActif() ? "✓ Actif" : "✗ Inactif");
            statusBadge.getStyle()
                    .set("color", user.getActif() ? "#2e7d32" : "#d32f2f")
                    .set("font-weight", "bold");
            return statusBadge;
        })).setHeader("Statut").setWidth("120px");

        grid.addColumn(user -> user.getDateInscription() != null
                        ? user.getDateInscription().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                        : "-")
                .setHeader("Date Inscription")
                .setWidth("150px");

        grid.addComponentColumn(user -> {
            HorizontalLayout actions = new HorizontalLayout();
            actions.setSpacing(true);

            Button editBtn = new Button(new Icon(VaadinIcon.EDIT));
            editBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
            editBtn.getElement().setAttribute("title", "Modifier");
            editBtn.addClickListener(e -> openUserDialog(user));

            Button toggleBtn = new Button(new Icon(
                    user.getActif() ? VaadinIcon.BAN : VaadinIcon.CHECK_CIRCLE
            ));
            toggleBtn.addThemeVariants(ButtonVariant.LUMO_SMALL,
                    user.getActif() ? ButtonVariant.LUMO_ERROR : ButtonVariant.LUMO_SUCCESS);
            toggleBtn.getElement().setAttribute("title",
                    user.getActif() ? "Désactiver" : "Activer");
            toggleBtn.addClickListener(e -> toggleUserStatus(user));

            Button deleteBtn = new Button(new Icon(VaadinIcon.TRASH));
            deleteBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_ERROR);
            deleteBtn.getElement().setAttribute("title", "Supprimer");
            deleteBtn.addClickListener(e -> confirmDeleteUser(user));

            if (user.getId().equals(currentUser.getId())) {
                deleteBtn.setEnabled(false);
                toggleBtn.setEnabled(false);
            }

            actions.add(editBtn, toggleBtn, deleteBtn);
            return actions;
        }).setHeader("Actions").setWidth("200px").setFlexGrow(0);

        return grid;
    }

    private void loadUsers() {
        try {
            List<User> users = userService.getAllUsers();
            userGrid.setItems(users);
        } catch (Exception e) {
            showNotification("Erreur de chargement: " + e.getMessage(), NotificationVariant.LUMO_ERROR);
        }
    }

    private void filterUsers() {
        String searchTerm = searchField.getValue().toLowerCase();
        UserRole selectedRole = roleFilter.getValue();

        List<User> filteredUsers = userService.getAllUsers().stream()
                .filter(user -> {
                    boolean matchesSearch = searchTerm.isEmpty() ||
                            user.getNom().toLowerCase().contains(searchTerm) ||
                            user.getPrenom().toLowerCase().contains(searchTerm) ||
                            user.getEmail().toLowerCase().contains(searchTerm);

                    boolean matchesRole = selectedRole == null || user.getRole() == selectedRole;

                    return matchesSearch && matchesRole;
                })
                .toList();

        userGrid.setItems(filteredUsers);
    }

    private void openUserDialog(User user) {
        Dialog dialog = new Dialog();
        dialog.setModal(true);
        dialog.setDraggable(false);
        dialog.setWidth("500px");

        H2 dialogTitle = new H2(user == null ? "➕ Nouvel Utilisateur" : "✏️ Modifier Utilisateur");
        dialogTitle.getStyle().set("margin", "0 0 20px 0");

        FormLayout formLayout = new FormLayout();
        formLayout.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 1));

        TextField nomField = new TextField("Nom");
        nomField.setRequired(true);
        if (user != null) nomField.setValue(user.getNom());

        TextField prenomField = new TextField("Prénom");
        prenomField.setRequired(true);
        if (user != null) prenomField.setValue(user.getPrenom());

        EmailField emailField = new EmailField("Email");
        emailField.setRequired(true);
        if (user != null) emailField.setValue(user.getEmail());

        TextField telephoneField = new TextField("Téléphone");
        if (user != null && user.getTelephone() != null) telephoneField.setValue(user.getTelephone());

        ComboBox<UserRole> roleComboBox = new ComboBox<>("Rôle");
        roleComboBox.setItems(UserRole.values());
        roleComboBox.setItemLabelGenerator(this::getRoleLabel);
        roleComboBox.setRequired(true);
        if (user != null) roleComboBox.setValue(user.getRole());
        else roleComboBox.setValue(UserRole.CLIENT);

        PasswordField passwordField = new PasswordField("Mot de passe");
        if (user == null) {
            passwordField.setRequired(true);
            passwordField.setHelperText("Minimum 8 caractères");
        } else {
            passwordField.setHelperText("Laisser vide pour conserver le mot de passe actuel");
        }

        formLayout.add(nomField, prenomField, emailField, telephoneField, roleComboBox, passwordField);

        HorizontalLayout buttonLayout = new HorizontalLayout();
        buttonLayout.setSpacing(true);
        buttonLayout.getStyle().set("margin-top", "20px");

        Button saveBtn = new Button("💾 Enregistrer");
        saveBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        saveBtn.addClickListener(e -> {
            if (validateUserForm(nomField, prenomField, emailField, passwordField, user)) {
                saveUser(user, nomField.getValue(), prenomField.getValue(),
                        emailField.getValue(), telephoneField.getValue(),
                        roleComboBox.getValue(), passwordField.getValue());
                dialog.close();
            }
        });

        Button cancelBtn = new Button("Annuler");
        cancelBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        cancelBtn.addClickListener(e -> dialog.close());

        buttonLayout.add(saveBtn, cancelBtn);

        VerticalLayout dialogContent = new VerticalLayout(dialogTitle, formLayout, buttonLayout);
        dialogContent.setPadding(true);
        dialog.add(dialogContent);
        dialog.open();
    }

    private boolean validateUserForm(TextField nomField, TextField prenomField,
                                     EmailField emailField, PasswordField passwordField, User user) {
        if (nomField.isEmpty() || prenomField.isEmpty() || emailField.isEmpty()) {
            showNotification("Veuillez remplir tous les champs obligatoires", NotificationVariant.LUMO_ERROR);
            return false;
        }

        if (user == null && passwordField.isEmpty()) {
            showNotification("Le mot de passe est obligatoire", NotificationVariant.LUMO_ERROR);
            return false;
        }

        if (!passwordField.isEmpty() && passwordField.getValue().length() < 8) {
            showNotification("Le mot de passe doit contenir au moins 8 caractères", NotificationVariant.LUMO_ERROR);
            return false;
        }

        return true;
    }

    private void saveUser(User existingUser, String nom, String prenom, String email,
                          String telephone, UserRole role, String password) {
        try {
            if (existingUser == null) {
                if (userService.existsByEmail(email)) {
                    showNotification("Un utilisateur avec cet email existe déjà", NotificationVariant.LUMO_ERROR);
                    return;
                }

                userService.registerNewUser(nom, prenom, email, password, telephone, role);
                showNotification("✓ Utilisateur créé avec succès", NotificationVariant.LUMO_SUCCESS);
            } else {
                existingUser.setNom(nom);
                existingUser.setPrenom(prenom);
                existingUser.setEmail(email);
                existingUser.setTelephone(telephone);
                existingUser.setRole(role);

                userService.updateUser(existingUser.getId(), existingUser);

                if (!password.isEmpty()) {
                    userService.resetPassword(email, password);
                }

                showNotification("✓ Utilisateur modifié avec succès", NotificationVariant.LUMO_SUCCESS);
            }

            loadUsers();
        } catch (Exception e) {
            showNotification("Erreur: " + e.getMessage(), NotificationVariant.LUMO_ERROR);
        }
    }

    private void toggleUserStatus(User user) {
        try {
            // ✅ CORRECTION: Recharger l'utilisateur depuis la BDD
            User freshUser = userService.getUserByEmail(user.getEmail())
                    .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

            if (freshUser.getActif()) {
                userService.deactivateUser(freshUser.getId());
                showNotification("✓ Utilisateur désactivé", NotificationVariant.LUMO_SUCCESS);
            } else {
                // ✅ ACTIVER l'utilisateur
                userService.activateUser(freshUser.getId());
                showNotification("✓ Utilisateur activé", NotificationVariant.LUMO_SUCCESS);
            }
            loadUsers();
        } catch (Exception e) {
            showNotification("Erreur: " + e.getMessage(), NotificationVariant.LUMO_ERROR);
        }
    }

    private void confirmDeleteUser(User user) {
        // Vérifier d'abord s'il y a des dépendances
        try {
            userService.deleteUser(user.getId());
            // Si aucune exception n'est lancée, on peut supprimer
            ConfirmDialog dialog = new ConfirmDialog();
            dialog.setHeader("⚠️ Confirmer la suppression");
            dialog.setText("Êtes-vous sûr de vouloir supprimer l'utilisateur " +
                    user.getPrenom() + " " + user.getNom() + " ?");

            dialog.setCancelable(true);
            dialog.setCancelText("Annuler");

            dialog.setConfirmText("Supprimer");
            dialog.setConfirmButtonTheme("error primary");
            dialog.addConfirmListener(e -> {
                try {
                    userService.deleteUser(user.getId());
                    showNotification("✓ Utilisateur supprimé avec succès", NotificationVariant.LUMO_SUCCESS);
                    loadUsers();
                } catch (Exception ex) {
                    showNotification("❌ Erreur: " + ex.getMessage(), NotificationVariant.LUMO_ERROR);
                }
            });

            dialog.open();
        } catch (IllegalStateException e) {
            // Si des dépendances existent, afficher directement le message
            if (e.getMessage().contains("réservation") || e.getMessage().contains("événement")) {
                showDetailedErrorDialog(user, e.getMessage());
            } else if (e.getMessage().contains("propre compte")) {
                showNotification("❌ " + e.getMessage(), NotificationVariant.LUMO_ERROR);
            } else {
                showNotification("❌ Erreur: " + e.getMessage(), NotificationVariant.LUMO_ERROR);
            }
        }
    }

    private void showDetailedErrorDialog(User user, String errorMessage) {
        ConfirmDialog dialog = new ConfirmDialog();
        dialog.setHeader("⚠️ Impossible de supprimer l'utilisateur");

        // Construire un message plus détaillé
        StringBuilder details = new StringBuilder();
        details.append("L'utilisateur ")
                .append(user.getPrenom())
                .append(" ")
                .append(user.getNom())
                .append(" ne peut pas être supprimé car :\n\n");

        if (errorMessage.contains("réservation")) {
            details.append("• Des réservations sont associées à son compte\n");
        }

        if (errorMessage.contains("événement")) {
            details.append("• Des événements sont associés à son compte\n");
        }

        details.append("\nVous pouvez :\n");
        details.append("• Désactiver son compte à la place\n");
        details.append("• Supprimer d'abord les dépendances\n");

        dialog.setText(details.toString());

        dialog.setCancelable(true);
        dialog.setCancelText("Fermer");

        // Ajouter un bouton pour désactiver à la place
        dialog.setConfirmText("Désactiver le compte");
        dialog.addConfirmListener(e -> {
            userService.deactivateUser(user.getId());
            showNotification("✓ Compte désactivé avec succès", NotificationVariant.LUMO_SUCCESS);
            loadUsers();
        });

        dialog.open();
    }

    private String getRoleLabel(UserRole role) {
        return switch (role) {
            case ADMIN -> "👑 Administrateur";
            case ORGANIZER -> "🎪 Organisateur";
            case CLIENT -> "👤 Client";
        };
    }

    private String getRoleColor(UserRole role) {
        return switch (role) {
            case ADMIN -> "#d32f2f";
            case ORGANIZER -> "#ed6c02";
            case CLIENT -> "#1976d2";
        };
    }

    private void showNotification(String message, NotificationVariant variant) {
        Notification notification = new Notification(message, 3000);
        notification.addThemeVariants(variant);
        notification.setPosition(Notification.Position.TOP_CENTER);
        notification.open();
    }
}