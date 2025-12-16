package com.eventbooking.view.admin;

import com.eventbooking.entity.Event;
import com.eventbooking.entity.User;
import com.eventbooking.entity.enums.EventCategory;
import com.eventbooking.entity.enums.EventStatus;
import com.eventbooking.entity.enums.UserRole;
import com.eventbooking.service.EventService;
import com.eventbooking.service.FileStorageService;
import com.eventbooking.service.UserService;
import com.eventbooking.view.admin.components.AdminSidebar;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.datetimepicker.DateTimePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;
import jakarta.annotation.security.PermitAll;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.InputStream;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Route("admin/events")
@PageTitle("Gestion Événements | Event Booking Admin")
@PermitAll
public class AllEventsManagementView extends HorizontalLayout {

    @Autowired
    private EventService eventService;

    @Autowired
    private UserService userService;

    @Autowired
    private FileStorageService fileStorageService;

    private Grid<Event> eventGrid;
    private ComboBox<EventCategory> categoryFilter;
    private ComboBox<EventStatus> statusFilter;
    private TextField searchField;
    private User currentUser;

    // CORRECTION: Variables d'instance pour gérer l'image
    private String tempImageBase64;
    private String tempImageUrl;
    private Image previewImage;
    private Div previewContainer;
    private Button removeImageBtn;

    public AllEventsManagementView(EventService eventService, UserService userService, FileStorageService fileStorageService) {
        this.eventService = eventService;
        this.userService = userService;
        this.fileStorageService = fileStorageService;

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
        loadEvents();
    }

    private void createUI() {
        AdminSidebar sidebar = new AdminSidebar(currentUser, "admin/events");
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

        HorizontalLayout header = createHeader();
        HorizontalLayout filters = createFilters();
        eventGrid = createEventGrid();

        content.add(header, filters, eventGrid);
        return content;
    }

    private void showEventDetailsDialog(Event event) {
        Dialog dialog = new Dialog();
        dialog.setModal(true);
        dialog.setDraggable(false);
        dialog.setWidth("900px");
        dialog.setMaxHeight("90vh");
        // CORRECTION: Supprimer les styles directement sur la Dialog

        // Créer un conteneur principal avec les styles
        VerticalLayout dialogLayout = new VerticalLayout();
        dialogLayout.setPadding(false);
        dialogLayout.setSpacing(false);
        dialogLayout.setSizeFull();
        dialogLayout.getStyle()
                .set("border-radius", "12px")
                .set("box-shadow", "0 10px 40px rgba(0,0,0,0.2)")
                .set("overflow", "hidden");

        // Header amélioré
        HorizontalLayout header = new HorizontalLayout();
        header.setWidthFull();
        header.setJustifyContentMode(JustifyContentMode.BETWEEN);
        header.setAlignItems(Alignment.CENTER);
        header.getStyle()
                .set("background", "linear-gradient(135deg, #1976d2 0%, #0d47a1 100%)")
                .set("padding", "20px 24px")
                .set("border-radius", "12px 12px 0 0");

        H2 dialogTitle = new H2("📋 Détails de l'Événement");
        dialogTitle.getStyle()
                .set("margin", "0")
                .set("color", "white")
                .set("font-size", "1.5rem")
                .set("font-weight", "600");

        Button closeBtn = new Button(new Icon(VaadinIcon.CLOSE));
        closeBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        closeBtn.getStyle()
                .set("color", "white")
                .set("background", "rgba(255,255,255,0.2)")
                .set("border-radius", "50%")
                .set("min-width", "44px")
                .set("min-height", "44px");
        closeBtn.addClickListener(e -> dialog.close());

        header.add(dialogTitle, closeBtn);

        VerticalLayout content = new VerticalLayout();
        content.setSpacing(true);
        content.setPadding(false);
        content.getStyle()
                .set("overflow-y", "auto")
                .set("padding", "0 24px 24px 24px")
                .set("background", "#ffffff");

        // Section Image améliorée
        if (event.getImageUrl() != null && !event.getImageUrl().isEmpty()) {
            try {
                String imageUrl = event.getImageUrl();
                if (!imageUrl.startsWith("http")) {
                    imageUrl = "/api/files" + imageUrl;
                }

                Div imageContainer = new Div();
                imageContainer.getStyle()
                        .set("width", "100%")
                        .set("height", "280px")
                        .set("margin-top", "20px")
                        .set("border-radius", "10px")
                        .set("overflow", "hidden")
                        .set("box-shadow", "0 4px 12px rgba(0,0,0,0.1)");

                Image image = new Image(imageUrl, event.getTitre());
                image.setWidth("100%");
                image.setHeight("100%");
                image.getStyle()
                        .set("object-fit", "cover")
                        .set("transition", "transform 0.3s ease");

                imageContainer.add(image);
                content.add(imageContainer);
            } catch (Exception e) {
                // Image non chargée
                Div placeholder = new Div();
                placeholder.getStyle()
                        .set("width", "100%")
                        .set("height", "200px")
                        .set("background", "linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%)")
                        .set("border-radius", "10px")
                        .set("display", "flex")
                        .set("align-items", "center")
                        .set("justify-content", "center")
                        .set("margin-top", "20px");

                Icon imageIcon = VaadinIcon.PICTURE.create();
                imageIcon.setSize("64px");
                imageIcon.setColor("#64748b");
                placeholder.add(imageIcon);
                content.add(placeholder);
            }
        }

        // En-tête d'informations
        Div headerInfo = new Div();
        headerInfo.getStyle()
                .set("background", "#f8fafc")
                .set("padding", "20px")
                .set("border-radius", "10px")
                .set("margin-top", "20px")
                .set("border", "1px solid #e2e8f0");

        HorizontalLayout titleRow = new HorizontalLayout();
        titleRow.setWidthFull();
        titleRow.setJustifyContentMode(JustifyContentMode.BETWEEN);
        titleRow.setAlignItems(Alignment.CENTER);

        H2 eventTitle = new H2(event.getTitre());
        eventTitle.getStyle()
                .set("margin", "0")
                .set("color", "#1e293b")
                .set("font-size", "1.8rem");

        // Badge de statut amélioré
        Span statusBadge = new Span(getStatusLabel(event.getStatut()));
        statusBadge.getStyle()
                .set("background", getStatusColor(event.getStatut()))
                .set("color", "white")
                .set("padding", "6px 16px")
                .set("border-radius", "20px")
                .set("font-size", "14px")
                .set("font-weight", "600")
                .set("letter-spacing", "0.3px")
                .set("box-shadow", "0 2px 6px rgba(0,0,0,0.1)");

        titleRow.add(eventTitle, statusBadge);
        headerInfo.add(titleRow);

        // Catégorie
        Span categoryBadge = new Span(getCategoryLabel(event.getCategorie()));
        categoryBadge.getStyle()
                .set("background", "#e3f2fd")
                .set("color", "#1976d2")
                .set("padding", "4px 12px")
                .set("border-radius", "16px")
                .set("font-size", "13px")
                .set("margin-top", "12px")
                .set("display", "inline-block");

        headerInfo.add(categoryBadge);
        content.add(headerInfo);

        // Grille d'informations améliorée
        FormLayout infoLayout = new FormLayout();
        infoLayout.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("500px", 2)
        );
        infoLayout.setWidthFull();
        infoLayout.getStyle()
                .set("margin-top", "20px")
                .set("padding", "20px")
                .set("background", "white")
                .set("border-radius", "10px")
                .set("border", "1px solid #e2e8f0");

        // Dates avec icônes
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE dd MMMM yyyy 'à' HH:mm");

        // Créer les champs d'information
        Div dateDebutField = createStyledInfoField("Date de début",
                event.getDateDebut().format(formatter), VaadinIcon.CALENDAR);

        Div dateFinField = createStyledInfoField("Date de fin",
                event.getDateFin().format(formatter), VaadinIcon.CLOCK);

        Div lieuField = createStyledInfoField("Lieu", event.getLieu(), VaadinIcon.MAP_MARKER);

        Div villeField = createStyledInfoField("Ville", event.getVille(), VaadinIcon.BUILDING);

        int availablePlaces = eventService.getAvailablePlaces(event.getId());
        Div capaciteField = createStyledInfoField("Capacité",
                availablePlaces + " / " + event.getCapaciteMax() + " places disponibles",
                VaadinIcon.USERS);

        Div prixField = createStyledInfoField("Prix",
                event.getPrixUnitaire() + " MAD", VaadinIcon.MONEY);

        String organizerText = event.getOrganisateur() != null ?
                event.getOrganisateur().getPrenom() + " " + event.getOrganisateur().getNom() +
                        " (" + event.getOrganisateur().getEmail() + ")" : "Non assigné";
        Div organisateurField = createStyledInfoField("Organisateur", organizerText, VaadinIcon.USER);

        String createdText = event.getDateCreation() != null ?
                event.getDateCreation().format(DateTimeFormatter.ofPattern("dd/MM/yyyy 'à' HH:mm")) : "N/A";
        Div createdField = createStyledInfoField("Créé le", createdText, VaadinIcon.CALENDAR_CLOCK);

        // Ajouter les champs au layout
        infoLayout.add(dateDebutField, dateFinField);
        infoLayout.add(lieuField, villeField);
        infoLayout.add(capaciteField, prixField);
        infoLayout.add(organisateurField, createdField);

        content.add(infoLayout);

        // Description stylisée
        if (event.getDescription() != null && !event.getDescription().isEmpty()) {
            Div descContainer = new Div();
            descContainer.getStyle()
                    .set("margin-top", "20px")
                    .set("padding", "20px")
                    .set("background", "white")
                    .set("border-radius", "10px")
                    .set("border", "1px solid #e2e8f0");

            HorizontalLayout descHeader = new HorizontalLayout();
            descHeader.setSpacing(true);
            descHeader.setAlignItems(Alignment.CENTER);

            Icon descIcon = VaadinIcon.FILE_TEXT.create();
            descIcon.setSize("20px");
            descIcon.setColor("#1976d2");

            Span descTitle = new Span("Description");
            descTitle.getStyle()
                    .set("font-weight", "600")
                    .set("color", "#1e293b")
                    .set("font-size", "1.1rem");

            descHeader.add(descIcon, descTitle);
            descContainer.add(descHeader);

            Span descContent = new Span(event.getDescription());
            descContent.getStyle()
                    .set("margin-top", "12px")
                    .set("display", "block")
                    .set("line-height", "1.6")
                    .set("color", "#475569")
                    .set("white-space", "pre-wrap");

            descContainer.add(descContent);
            content.add(descContainer);
        }

        // Boutons d'action améliorés
        HorizontalLayout actionButtons = new HorizontalLayout();
        actionButtons.setSpacing(true);
        actionButtons.getStyle()
                .set("margin-top", "30px")
                .set("padding", "20px")
                .set("background", "#f8fafc")
                .set("border-radius", "10px")
                .set("border-top", "1px solid #e2e8f0")
                .set("justify-content", "flex-end");

        Button editBtn = new Button("✏️ Modifier", new Icon(VaadinIcon.EDIT));
        editBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        editBtn.getStyle()
                .set("border-radius", "8px")
                .set("padding", "10px 20px")
                .set("font-weight", "600");
        editBtn.addClickListener(e -> {
            dialog.close();
            openEventDialog(event);
        });

        Button closeDialogBtn = new Button("Fermer", new Icon(VaadinIcon.CLOSE));
        closeDialogBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        closeDialogBtn.getStyle()
                .set("border-radius", "8px")
                .set("padding", "10px 20px");
        closeDialogBtn.addClickListener(e -> dialog.close());

        actionButtons.add(editBtn, closeDialogBtn);
        content.add(actionButtons);

        // Ajouter le header et le contenu au layout principal
        dialogLayout.add(header, content);

        // Ajouter le layout principal à la dialog
        dialog.add(dialogLayout);
        dialog.open();
    }

    private Div createStyledInfoField(String label, String value, VaadinIcon icon) {
        Div field = new Div();
        field.getStyle()
                .set("padding", "12px")
                .set("background", "#f8fafc")
                .set("border-radius", "8px")
                .set("border", "1px solid #f1f5f9");

        HorizontalLayout fieldContent = new HorizontalLayout();
        fieldContent.setSpacing(true);
        fieldContent.setAlignItems(Alignment.CENTER);
        fieldContent.getStyle().set("margin-bottom", "4px");

        if (icon != null) {
            Icon fieldIcon = icon.create();
            fieldIcon.setSize("16px");
            fieldIcon.setColor("#64748b");
            fieldContent.add(fieldIcon);
        }

        Span labelSpan = new Span(label + ":");
        labelSpan.getStyle()
                .set("font-weight", "600")
                .set("color", "#475569")
                .set("font-size", "13px");
        fieldContent.add(labelSpan);

        field.add(fieldContent);

        Span valueSpan = new Span(value);
        valueSpan.getStyle()
                .set("font-size", "15px")
                .set("color", "#1e293b")
                .set("margin-left", icon != null ? "28px" : "0")
                .set("display", "block");
        field.add(valueSpan);

        return field;
    }


    // Ajoutez cette méthode helper en tant que méthode privée de la classe
    private Div createInfoField(String label, String value, VaadinIcon icon) {
        Div field = new Div();
        field.getStyle()
                .set("padding", "12px")
                .set("background", "#f8fafc")
                .set("border-radius", "8px")
                .set("border", "1px solid #f1f5f9");

        HorizontalLayout fieldContent = new HorizontalLayout();
        fieldContent.setSpacing(true);
        fieldContent.setAlignItems(Alignment.CENTER);
        fieldContent.getStyle().set("margin-bottom", "4px");

        if (icon != null) {
            Icon fieldIcon = icon.create();
            fieldIcon.setSize("16px");
            fieldIcon.setColor("#64748b");
            fieldContent.add(fieldIcon);
        }

        Span labelSpan = new Span(label + ":");
        labelSpan.getStyle()
                .set("font-weight", "600")
                .set("color", "#475569")
                .set("font-size", "13px");
        fieldContent.add(labelSpan);

        field.add(fieldContent);

        Span valueSpan = new Span(value);
        valueSpan.getStyle()
                .set("font-size", "15px")
                .set("color", "#1e293b")
                .set("margin-left", icon != null ? "28px" : "0")
                .set("display", "block");
        field.add(valueSpan);

        return field;
    }


    private HorizontalLayout createHeader() {
        HorizontalLayout header = new HorizontalLayout();
        header.setWidthFull();
        header.setJustifyContentMode(JustifyContentMode.BETWEEN);
        header.setAlignItems(Alignment.CENTER);

        H1 title = new H1("🎪 Gestion des Événements");
        title.getStyle().set("margin", "0").set("color", "#1976d2");

        Button addEventBtn = new Button("Créer Événement", new Icon(VaadinIcon.PLUS));
        addEventBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        addEventBtn.addClickListener(e -> openEventDialog(null));

        header.add(title, addEventBtn);
        return header;
    }

    private HorizontalLayout createFilters() {
        HorizontalLayout filters = new HorizontalLayout();
        filters.setWidthFull();
        filters.setSpacing(true);
        filters.setAlignItems(Alignment.END);

        searchField = new TextField("Rechercher");
        searchField.setPlaceholder("Titre, lieu, ville...");
        searchField.setPrefixComponent(new Icon(VaadinIcon.SEARCH));
        searchField.setWidth("300px");
        searchField.addValueChangeListener(e -> filterEvents());

        categoryFilter = new ComboBox<>("Catégorie");
        categoryFilter.setItems(EventCategory.values());
        categoryFilter.setItemLabelGenerator(this::getCategoryLabel);
        categoryFilter.setPlaceholder("Toutes les catégories");
        categoryFilter.setClearButtonVisible(true);
        categoryFilter.addValueChangeListener(e -> filterEvents());

        statusFilter = new ComboBox<>("Statut");
        statusFilter.setItems(EventStatus.values());
        statusFilter.setItemLabelGenerator(this::getStatusLabel);
        statusFilter.setPlaceholder("Tous les statuts");
        statusFilter.setClearButtonVisible(true);
        statusFilter.addValueChangeListener(e -> filterEvents());

        Button refreshBtn = new Button("🔄 Actualiser");
        refreshBtn.addClickListener(e -> loadEvents());

        filters.add(searchField, categoryFilter, statusFilter, refreshBtn);
        return filters;
    }

    private Grid<Event> createEventGrid() {
        Grid<Event> grid = new Grid<>(Event.class, false);
        grid.setHeight("600px");

        grid.addColumn(Event::getId)
                .setHeader("ID")
                .setWidth("70px")
                .setFlexGrow(0);

        // CORRECTION: Colonne avec image - URL corrigée
        grid.addColumn(new ComponentRenderer<>(event -> {
            HorizontalLayout cell = new HorizontalLayout();
            cell.setSpacing(true);
            cell.setAlignItems(Alignment.CENTER);

            if (event.getImageUrl() != null && !event.getImageUrl().isEmpty()) {
                try {
                    String imageUrl = event.getImageUrl();
                    if (!imageUrl.startsWith("http")) {
                        imageUrl = "/api/files" + imageUrl;
                    }

                    Image image = new Image(imageUrl, event.getTitre());
                    image.setHeight("40px");
                    image.setWidth("40px");
                    image.getStyle()
                            .set("border-radius", "5px")
                            .set("object-fit", "cover")
                            .set("border", "1px solid #ddd");
                    cell.add(image);
                } catch (Exception e) {
                    Icon imageIcon = VaadinIcon.PICTURE.create();
                    imageIcon.setSize("20px");
                    imageIcon.setColor("#666");
                    cell.add(imageIcon);
                }
            } else {
                Icon imageIcon = VaadinIcon.PICTURE.create();
                imageIcon.setSize("20px");
                imageIcon.setColor("#ccc");
                cell.add(imageIcon);
            }

            Span titleSpan = new Span(event.getTitre());
            titleSpan.getStyle().set("font-weight", "500");
            cell.add(titleSpan);

            return cell;
        })).setHeader("Événement").setSortable(true).setWidth("300px");

        grid.addColumn(new ComponentRenderer<>(event -> {
            Span badge = new Span(getCategoryLabel(event.getCategorie()));
            badge.getStyle()
                    .set("background", "#e3f2fd")
                    .set("color", "#1976d2")
                    .set("padding", "4px 10px")
                    .set("border-radius", "12px")
                    .set("font-size", "12px")
                    .set("font-weight", "500");
            return badge;
        })).setHeader("Catégorie").setWidth("150px");

        grid.addColumn(event -> event.getDateDebut().format(
                        DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")))
                .setHeader("Date Début")
                .setSortable(true)
                .setWidth("170px");

        grid.addColumn(Event::getVille).setHeader("Ville").setWidth("120px");

        grid.addColumn(event -> event.getOrganisateur() != null
                        ? event.getOrganisateur().getPrenom() + " " + event.getOrganisateur().getNom()
                        : "N/A")
                .setHeader("Organisateur")
                .setWidth("180px");

        grid.addColumn(new ComponentRenderer<>(event -> {
            Span statusBadge = new Span(getStatusLabel(event.getStatut()));
            statusBadge.getStyle()
                    .set("background", getStatusColor(event.getStatut()))
                    .set("color", "white")
                    .set("padding", "5px 10px")
                    .set("border-radius", "12px")
                    .set("font-size", "12px")
                    .set("font-weight", "bold");
            return statusBadge;
        })).setHeader("Statut").setWidth("140px");

        grid.addColumn(event -> {
            int available = eventService.getAvailablePlaces(event.getId());
            return available + "/" + event.getCapaciteMax();
        }).setHeader("Places").setWidth("100px");

        grid.addColumn(event -> event.getPrixUnitaire() + " MAD")
                .setHeader("Prix")
                .setWidth("100px");

        grid.addComponentColumn(event -> {
            HorizontalLayout actions = new HorizontalLayout();
            actions.setSpacing(true);

            Button viewBtn = new Button(new Icon(VaadinIcon.EYE));
            viewBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
            viewBtn.getElement().setAttribute("title", "Voir détails");
            viewBtn.addClickListener(e -> showEventDetailsDialog(event));

            Button editBtn = new Button(new Icon(VaadinIcon.EDIT));
            editBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_PRIMARY);
            editBtn.getElement().setAttribute("title", "Modifier");
            editBtn.addClickListener(e -> openEventDialog(event));

            Button publishBtn = new Button(new Icon(
                    event.getStatut() == EventStatus.PUBLIE ? VaadinIcon.CLOSE : VaadinIcon.CHECK));
            publishBtn.addThemeVariants(ButtonVariant.LUMO_SMALL,
                    event.getStatut() == EventStatus.PUBLIE
                            ? ButtonVariant.LUMO_ERROR
                            : ButtonVariant.LUMO_SUCCESS);
            publishBtn.getElement().setAttribute("title",
                    event.getStatut() == EventStatus.PUBLIE ? "Annuler" : "Publier");
            publishBtn.addClickListener(e -> toggleEventStatus(event));

            Button deleteBtn = new Button(new Icon(VaadinIcon.TRASH));
            deleteBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_ERROR);
            deleteBtn.getElement().setAttribute("title", "Supprimer");
            deleteBtn.addClickListener(e -> confirmDeleteEvent(event));

            actions.add(viewBtn, editBtn, publishBtn, deleteBtn);
            return actions;
        }).setHeader("Actions").setWidth("220px").setFlexGrow(0);

        return grid;
    }

    private void loadEvents() {
        try {
            List<Event> events = eventService.getAllEvents();
            eventGrid.setItems(events);
        } catch (Exception e) {
            showNotification("Erreur de chargement: " + e.getMessage(), NotificationVariant.LUMO_ERROR);
        }
    }

    private void filterEvents() {
        String searchTerm = searchField.getValue().toLowerCase();
        EventCategory selectedCategory = categoryFilter.getValue();
        EventStatus selectedStatus = statusFilter.getValue();

        List<Event> filteredEvents = eventService.getAllEvents().stream()
                .filter(event -> {
                    boolean matchesSearch = searchTerm.isEmpty() ||
                            event.getTitre().toLowerCase().contains(searchTerm) ||
                            event.getLieu().toLowerCase().contains(searchTerm) ||
                            event.getVille().toLowerCase().contains(searchTerm);

                    boolean matchesCategory = selectedCategory == null ||
                            event.getCategorie() == selectedCategory;

                    boolean matchesStatus = selectedStatus == null ||
                            event.getStatut() == selectedStatus;

                    return matchesSearch && matchesCategory && matchesStatus;
                })
                .toList();

        eventGrid.setItems(filteredEvents);
    }

    private void openEventDialog(Event event) {
        Dialog dialog = new Dialog();
        dialog.setModal(true);
        dialog.setDraggable(false);
        dialog.setWidth("850px");
        dialog.setMaxHeight("90vh");

        // CORRECTION: Réinitialiser correctement les variables
        tempImageBase64 = null;
        tempImageUrl = event != null ? event.getImageUrl() : null;

        H2 dialogTitle = new H2(event == null ? "➕ Nouvel Événement" : "✏️ Modifier Événement");
        dialogTitle.getStyle().set("margin", "0 0 20px 0");

        HorizontalLayout mainLayout = new HorizontalLayout();
        mainLayout.setSizeFull();
        mainLayout.setSpacing(true);

        VerticalLayout imageSection = createImageSection(event, dialog);
        imageSection.setWidth("300px");
        imageSection.setMinWidth("300px");

        VerticalLayout formSection = createFormSection(event, dialog);
        formSection.setFlexGrow(1);

        mainLayout.add(imageSection, formSection);

        VerticalLayout dialogContent = new VerticalLayout(dialogTitle, mainLayout);
        dialogContent.setPadding(true);
        dialog.add(dialogContent);
        dialog.open();
    }

    private VerticalLayout createImageSection(Event event, Dialog dialog) {
        VerticalLayout imageSection = new VerticalLayout();
        imageSection.setSpacing(true);
        imageSection.setPadding(false);
        imageSection.getStyle()
                .set("background", "#f9fafb")
                .set("border-radius", "10px")
                .set("padding", "15px")
                .set("border", "2px dashed #d1d5db");

        Div imageTitle = new Div("🖼️ Image de l'événement");
        imageTitle.getStyle()
                .set("font-weight", "600")
                .set("color", "#374151")
                .set("margin-bottom", "10px");

        // CORRECTION: Initialiser previewContainer comme variable d'instance
        previewContainer = new Div();
        previewContainer.getStyle()
                .set("width", "100%")
                .set("height", "180px")
                .set("border-radius", "8px")
                .set("overflow", "hidden")
                .set("margin-bottom", "10px")
                .set("background", "#f3f4f6")
                .set("display", "flex")
                .set("align-items", "center")
                .set("justify-content", "center");

        // CORRECTION: Initialiser previewImage comme variable d'instance
        previewImage = new Image();
        previewImage.setHeight("100%");
        previewImage.setWidth("100%");
        previewImage.getStyle().set("object-fit", "cover");

        // CORRECTION: Afficher l'image existante correctement
        if (event != null && event.getImageUrl() != null && !event.getImageUrl().isEmpty()) {
            String imageUrl = event.getImageUrl();
            if (!imageUrl.startsWith("http")) {
                imageUrl = "/api/files" + imageUrl;
            }
            previewImage.setSrc(imageUrl);
            previewImage.setVisible(true);
            previewContainer.add(previewImage);
        } else {
            Icon defaultIcon = VaadinIcon.PICTURE.create();
            defaultIcon.setSize("60px");
            defaultIcon.setColor("#9ca3af");
            previewContainer.add(defaultIcon);
        }

        MemoryBuffer buffer = new MemoryBuffer();
        Upload upload = new Upload(buffer);
        upload.setAcceptedFileTypes("image/jpeg", "image/png", "image/gif", "image/webp");
        upload.setMaxFileSize(5 * 1024 * 1024);
        upload.setMaxFiles(1);
        upload.setDropAllowed(true);
        upload.setWidthFull();

        Button uploadButton = new Button("📁 Choisir une image", new Icon(VaadinIcon.UPLOAD));
        uploadButton.setWidthFull();
        uploadButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        uploadButton.getStyle()
                .set("border", "1px solid #d1d5db")
                .set("border-radius", "8px")
                .set("padding", "10px");

        upload.setUploadButton(uploadButton);

        // CORRECTION: Gestionnaire d'upload amélioré
        upload.addSucceededListener(e -> {
            try {
                InputStream fileData = buffer.getInputStream();
                byte[] imageBytes = fileData.readAllBytes();

                String base64Image = java.util.Base64.getEncoder().encodeToString(imageBytes);
                String mimeType = getMimeType(e.getFileName());
                String dataUrl = "data:" + mimeType + ";base64," + base64Image;

                tempImageBase64 = dataUrl;
                tempImageUrl = null;

                previewContainer.removeAll();
                previewImage.setSrc(dataUrl);
                previewImage.setVisible(true);
                previewContainer.add(previewImage);

                if (removeImageBtn != null) {
                    removeImageBtn.setVisible(true);
                }

                showNotification("✓ Image chargée avec succès", NotificationVariant.LUMO_SUCCESS);

            } catch (Exception ex) {
                showNotification("Erreur lors du traitement de l'image: " + ex.getMessage(),
                        NotificationVariant.LUMO_ERROR);
            }
        });

        upload.addFileRejectedListener(e -> {
            showNotification("Erreur: " + e.getErrorMessage(), NotificationVariant.LUMO_ERROR);
        });

        // CORRECTION: Bouton de suppression amélioré
        removeImageBtn = new Button("🗑️ Supprimer l'image", new Icon(VaadinIcon.TRASH));
        removeImageBtn.setWidthFull();
        removeImageBtn.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_SMALL);
        removeImageBtn.setVisible((event != null && event.getImageUrl() != null) || tempImageBase64 != null);
        removeImageBtn.addClickListener(e -> {
            tempImageUrl = null;
            tempImageBase64 = null;
            previewContainer.removeAll();
            Icon defaultIcon = VaadinIcon.PICTURE.create();
            defaultIcon.setSize("60px");
            defaultIcon.setColor("#9ca3af");
            previewContainer.add(defaultIcon);
            removeImageBtn.setVisible(false);
        });

        imageSection.add(imageTitle, previewContainer, upload, removeImageBtn);
        return imageSection;
    }

    private String getMimeType(String fileName) {
        String lowerFileName = fileName.toLowerCase();
        if (lowerFileName.endsWith(".jpg") || lowerFileName.endsWith(".jpeg")) {
            return "image/jpeg";
        } else if (lowerFileName.endsWith(".png")) {
            return "image/png";
        } else if (lowerFileName.endsWith(".gif")) {
            return "image/gif";
        } else if (lowerFileName.endsWith(".webp")) {
            return "image/webp";
        }
        return "application/octet-stream";
    }

    private VerticalLayout createFormSection(Event event, Dialog dialog) {
        VerticalLayout formSection = new VerticalLayout();
        formSection.setSpacing(true);
        formSection.setPadding(false);

        FormLayout formLayout = new FormLayout();
        formLayout.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("500px", 2)
        );

        TextField titreField = new TextField("Titre");
        titreField.setRequired(true);
        titreField.setMinLength(5);
        titreField.setMaxLength(100);
        if (event != null) titreField.setValue(event.getTitre());

        TextArea descriptionField = new TextArea("Description");
        descriptionField.setMaxLength(1000);
        descriptionField.setHeight("100px");
        if (event != null && event.getDescription() != null)
            descriptionField.setValue(event.getDescription());

        ComboBox<EventCategory> categoryCombo = new ComboBox<>("Catégorie");
        categoryCombo.setItems(EventCategory.values());
        categoryCombo.setItemLabelGenerator(this::getCategoryLabel);
        categoryCombo.setRequired(true);
        if (event != null) categoryCombo.setValue(event.getCategorie());
        else categoryCombo.setValue(EventCategory.AUTRE);

        DateTimePicker dateDebutPicker = new DateTimePicker("Date Début");
        dateDebutPicker.setRequiredIndicatorVisible(true);
        if (event != null) dateDebutPicker.setValue(event.getDateDebut());

        DateTimePicker dateFinPicker = new DateTimePicker("Date Fin");
        dateFinPicker.setRequiredIndicatorVisible(true);
        if (event != null) dateFinPicker.setValue(event.getDateFin());

        TextField lieuField = new TextField("Lieu");
        lieuField.setRequired(true);
        if (event != null) lieuField.setValue(event.getLieu());

        TextField villeField = new TextField("Ville");
        villeField.setRequired(true);
        if (event != null) villeField.setValue(event.getVille());

        IntegerField capaciteField = new IntegerField("Capacité Maximale");
        capaciteField.setRequired(true);
        capaciteField.setMin(1);
        capaciteField.setStep(1);
        if (event != null) capaciteField.setValue(event.getCapaciteMax());
        else capaciteField.setValue(100);

        NumberField prixField = new NumberField("Prix Unitaire (MAD)");
        prixField.setRequired(true);
        prixField.setMin(0);
        prixField.setStep(0.01);
        if (event != null) prixField.setValue(event.getPrixUnitaire());
        else prixField.setValue(50.0);

        ComboBox<User> organizerCombo = new ComboBox<>("Organisateur");
        List<User> organizers = userService.getUsersByRole(UserRole.ORGANIZER);
        organizerCombo.setItems(organizers);
        organizerCombo.setItemLabelGenerator(user ->
                user.getPrenom() + " " + user.getNom() + " (" + user.getEmail() + ")"
        );
        organizerCombo.setRequired(true);
        if (event != null && event.getOrganisateur() != null) {
            organizerCombo.setValue(event.getOrganisateur());
        } else if (!organizers.isEmpty()) {
            organizerCombo.setValue(organizers.get(0));
        }

        ComboBox<EventStatus> statusCombo = new ComboBox<>("Statut");
        statusCombo.setItems(EventStatus.values());
        statusCombo.setItemLabelGenerator(this::getStatusLabel);
        statusCombo.setRequired(true);
        if (event != null) statusCombo.setValue(event.getStatut());
        else statusCombo.setValue(EventStatus.BROUILLON);

        formLayout.add(titreField, 2);
        formLayout.add(descriptionField, 2);
        formLayout.add(categoryCombo, statusCombo);
        formLayout.add(dateDebutPicker, dateFinPicker);
        formLayout.add(lieuField, villeField);
        formLayout.add(capaciteField, prixField);
        formLayout.add(organizerCombo, 2);

        HorizontalLayout buttonLayout = new HorizontalLayout();
        buttonLayout.setSpacing(true);
        buttonLayout.getStyle().set("margin-top", "20px");

        Button saveBtn = new Button("💾 Enregistrer", new Icon(VaadinIcon.CHECK));
        saveBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        saveBtn.addClickListener(e -> {
            if (validateEventForm(titreField, dateDebutPicker, dateFinPicker,
                    lieuField, villeField, capaciteField, prixField, organizerCombo)) {

                try {
                    Event newEvent = new Event();
                    newEvent.setTitre(titreField.getValue());
                    newEvent.setDescription(descriptionField.getValue());
                    newEvent.setCategorie(categoryCombo.getValue());
                    newEvent.setDateDebut(dateDebutPicker.getValue());
                    newEvent.setDateFin(dateFinPicker.getValue());
                    newEvent.setLieu(lieuField.getValue());
                    newEvent.setVille(villeField.getValue());
                    newEvent.setCapaciteMax(capaciteField.getValue());
                    newEvent.setPrixUnitaire(prixField.getValue());
                    newEvent.setOrganisateur(organizerCombo.getValue());
                    newEvent.setStatut(statusCombo.getValue());

                    // CORRECTION: Logique de sauvegarde améliorée
                    if (event == null) {
                        // Création d'un nouvel événement
                        if (tempImageBase64 != null) {
                            eventService.createEventWithImage(newEvent, tempImageBase64);
                            showNotification("✓ Événement créé avec image", NotificationVariant.LUMO_SUCCESS);
                        } else {
                            eventService.createEvent(newEvent);
                            showNotification("✓ Événement créé", NotificationVariant.LUMO_SUCCESS);
                        }
                    } else {
                        // Mise à jour d'un événement existant
                        if (tempImageBase64 != null) {
                            // Nouvelle image uploadée
                            eventService.updateEventWithImage(event.getId(), newEvent, tempImageBase64);
                            showNotification("✓ Événement modifié avec nouvelle image", NotificationVariant.LUMO_SUCCESS);
                        } else if (tempImageUrl == null && event.getImageUrl() != null) {
                            // Image supprimée
                            newEvent.setImageUrl(null);
                            eventService.updateEvent(event.getId(), newEvent);
                            showNotification("✓ Événement modifié (image supprimée)", NotificationVariant.LUMO_SUCCESS);
                        } else {
                            // Pas de changement d'image
                            newEvent.setImageUrl(event.getImageUrl());
                            eventService.updateEvent(event.getId(), newEvent);
                            showNotification("✓ Événement modifié", NotificationVariant.LUMO_SUCCESS);
                        }
                    }

                    dialog.close();
                    loadEvents();

                } catch (Exception ex) {
                    showNotification("Erreur: " + ex.getMessage(), NotificationVariant.LUMO_ERROR);
                    ex.printStackTrace();
                }
            }
        });

        Button cancelBtn = new Button("Annuler", new Icon(VaadinIcon.CLOSE));
        cancelBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        cancelBtn.addClickListener(e -> dialog.close());

        buttonLayout.add(saveBtn, cancelBtn);
        formSection.add(formLayout, buttonLayout);
        return formSection;
    }

    private boolean validateEventForm(TextField titreField, DateTimePicker dateDebutPicker,
                                      DateTimePicker dateFinPicker, TextField lieuField,
                                      TextField villeField, IntegerField capaciteField,
                                      NumberField prixField, ComboBox<User> organizerCombo) {
        if (titreField.isEmpty() || lieuField.isEmpty() || villeField.isEmpty()) {
            showNotification("Veuillez remplir tous les champs obligatoires",
                    NotificationVariant.LUMO_ERROR);
            return false;
        }

        if (dateDebutPicker.isEmpty() || dateFinPicker.isEmpty()) {
            showNotification("Les dates sont obligatoires", NotificationVariant.LUMO_ERROR);
            return false;
        }

        if (dateFinPicker.getValue().isBefore(dateDebutPicker.getValue())) {
            showNotification("La date de fin doit être après la date de début",
                    NotificationVariant.LUMO_ERROR);
            return false;
        }

        if (capaciteField.isEmpty() || capaciteField.getValue() <= 0) {
            showNotification("La capacité doit être supérieure à 0",
                    NotificationVariant.LUMO_ERROR);
            return false;
        }

        if (prixField.isEmpty() || prixField.getValue() < 0) {
            showNotification("Le prix ne peut pas être négatif",
                    NotificationVariant.LUMO_ERROR);
            return false;
        }

        if (organizerCombo.isEmpty()) {
            showNotification("Veuillez sélectionner un organisateur",
                    NotificationVariant.LUMO_ERROR);
            return false;
        }

        return true;
    }

    private void toggleEventStatus(Event event) {
        try {
            if (event.getStatut() == EventStatus.PUBLIE) {
                eventService.cancelEvent(event.getId());
                showNotification("✓ Événement annulé", NotificationVariant.LUMO_SUCCESS);
            } else {
                eventService.publishEvent(event.getId());
                showNotification("✓ Événement publié", NotificationVariant.LUMO_SUCCESS);
            }
            loadEvents();
        } catch (Exception e) {
            showNotification("Erreur: " + e.getMessage(), NotificationVariant.LUMO_ERROR);
        }
    }

    private void confirmDeleteEvent(Event event) {
        ConfirmDialog dialog = new ConfirmDialog();
        dialog.setHeader("⚠️ Confirmer la suppression");
        dialog.setText("Êtes-vous sûr de vouloir supprimer l'événement \"" +
                event.getTitre() + "\" ? Cette action est irréversible.");

        dialog.setCancelable(true);
        dialog.setCancelText("Annuler");

        dialog.setConfirmText("Supprimer");
        dialog.setConfirmButtonTheme("error primary");
        dialog.addConfirmListener(e -> deleteEvent(event));

        dialog.open();
    }

    private void deleteEvent(Event event) {
        try {
            eventService.deleteEventWithImage(event.getId());
            showNotification("✓ Événement supprimé", NotificationVariant.LUMO_SUCCESS);
            loadEvents();
        } catch (Exception e) {
            showNotification("Erreur: " + e.getMessage(), NotificationVariant.LUMO_ERROR);
        }
    }

    private String getCategoryLabel(EventCategory category) {
        return switch (category) {
            case CONCERT -> "🎵 Concert";
            case THEATRE -> "🎭 Théâtre";
            case CONFERENCE -> "🎤 Conférence";
            case SPORT -> "⚽ Sport";
            case AUTRE -> "📌 Autre";
        };
    }

    private String getStatusLabel(EventStatus status) {
        return switch (status) {
            case BROUILLON -> "📝 Brouillon";
            case PUBLIE -> "✅ Publié";
            case ANNULE -> "❌ Annulé";
            case TERMINE -> "🏁 Terminé";
        };
    }

    private String getStatusColor(EventStatus status) {
        return switch (status) {
            case BROUILLON -> "#666";
            case PUBLIE -> "#2e7d32";
            case ANNULE -> "#d32f2f";
            case TERMINE -> "#1976d2";
        };
    }

    private void showNotification(String message, NotificationVariant variant) {
        Notification notification = new Notification(message, 3000);
        notification.addThemeVariants(variant);
        notification.setPosition(Notification.Position.TOP_CENTER);
        notification.open();
    }
}