package com.eventbooking.view.organizer;

import com.eventbooking.entity.Event;
import com.eventbooking.entity.User;
import com.eventbooking.entity.enums.EventCategory;
import com.eventbooking.entity.enums.EventStatus;
import com.eventbooking.entity.enums.UserRole;
import com.eventbooking.service.EventService;
import com.eventbooking.service.FileStorageService;
import com.vaadin.flow.component.UI;
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
import jakarta.annotation.security.RolesAllowed;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Route(value = "organizer/events", layout = OrganizerMainLayout.class)
@PageTitle("Mes Événements | Event Booking")
@RolesAllowed("ORGANIZER")
public class MyEventsView extends VerticalLayout {

    private final EventService eventService;
    private final FileStorageService fileStorageService;
    private User currentUser;
    private Grid<Event> eventGrid;
    private ComboBox<EventStatus> statusFilter;
    private TextField searchField;

    // Variables pour la gestion de l'image
    private String tempImageBase64;
    private String tempImageUrl;
    private Image previewImage;
    private Div previewContainer;
    private Button removeImageBtn;

    @Autowired
    public MyEventsView(EventService eventService, FileStorageService fileStorageService) {
        this.eventService = eventService;
        this.fileStorageService = fileStorageService;

        currentUser = (User) VaadinSession.getCurrent().getAttribute("currentUser");
        if (currentUser == null || currentUser.getRole() != UserRole.ORGANIZER) {
            UI.getCurrent().navigate("login");
            return;
        }

        setSizeFull();
        setPadding(false);
        setSpacing(false);
        getStyle().set("background", "#f8fafc");

        createUI();
        loadEvents();
    }

    private void createUI() {
        setPadding(true);
        setSpacing(true);

        // Header
        HorizontalLayout header = createHeader();

        // Filtres
        HorizontalLayout filters = createFilters();

        // Grid
        eventGrid = createEventGrid();

        add(header, filters, eventGrid);
        expand(eventGrid);
    }

    private HorizontalLayout createHeader() {
        HorizontalLayout header = new HorizontalLayout();
        header.setWidthFull();
        header.setJustifyContentMode(JustifyContentMode.BETWEEN);
        header.setAlignItems(Alignment.CENTER);

        H1 title = new H1("🎉 Mes Événements");
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

        statusFilter = new ComboBox<>("Statut");
        statusFilter.setItems(EventStatus.values());
        statusFilter.setItemLabelGenerator(this::getStatusLabel);
        statusFilter.setPlaceholder("Tous les statuts");
        statusFilter.setClearButtonVisible(true);
        statusFilter.addValueChangeListener(e -> filterEvents());

        Button refreshBtn = new Button("🔄 Actualiser");
        refreshBtn.addClickListener(e -> loadEvents());

        filters.add(searchField, statusFilter, refreshBtn);
        return filters;
    }

    private Grid<Event> createEventGrid() {
        Grid<Event> grid = new Grid<>(Event.class, false);
        grid.setHeight("600px");

        grid.addColumn(Event::getId)
                .setHeader("ID")
                .setWidth("70px")
                .setFlexGrow(0);

        // CORRECTION: Colonne avec image - spécifier explicitement les types
        grid.addColumn(new ComponentRenderer<HorizontalLayout, Event>(event -> {
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

        // CORRECTION: Colonne catégorie - spécifier explicitement les types
        grid.addColumn(new ComponentRenderer<Span, Event>(event -> {
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

        grid.addColumn(event -> {
            int available = eventService.getAvailablePlaces(event.getId());
            return available + "/" + event.getCapaciteMax();
        }).setHeader("Places").setWidth("100px");

        grid.addColumn(event -> event.getPrixUnitaire() + " MAD")
                .setHeader("Prix")
                .setWidth("100px");

        // CORRECTION: Colonne statut - spécifier explicitement les types
        grid.addColumn(new ComponentRenderer<Span, Event>(event -> {
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

        // CORRECTION: Colonne actions - spécifier explicitement les types
        grid.addColumn(new ComponentRenderer<HorizontalLayout, Event>(event -> {
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
        })).setHeader("Actions").setWidth("250px").setFlexGrow(0);

        return grid;
    }

    private void loadEvents() {
        try {
            List<Event> events = eventService.getEventsByOrganizer(currentUser.getId());
            eventGrid.setItems(events);
        } catch (Exception e) {
            showNotification("Erreur de chargement: " + e.getMessage(), NotificationVariant.LUMO_ERROR);
        }
    }

    private void filterEvents() {
        String searchTerm = searchField.getValue().toLowerCase();
        EventStatus selectedStatus = statusFilter.getValue();

        List<Event> filteredEvents = eventService.getEventsByOrganizer(currentUser.getId()).stream()
                .filter(event -> {
                    boolean matchesSearch = searchTerm.isEmpty() ||
                            event.getTitre().toLowerCase().contains(searchTerm) ||
                            event.getLieu().toLowerCase().contains(searchTerm) ||
                            event.getVille().toLowerCase().contains(searchTerm);

                    boolean matchesStatus = selectedStatus == null ||
                            event.getStatut() == selectedStatus;

                    return matchesSearch && matchesStatus;
                })
                .toList();

        eventGrid.setItems(filteredEvents);
    }

    private void showEventDetailsDialog(Event event) {
        Dialog dialog = new Dialog();
        dialog.setModal(true);
        dialog.setDraggable(false);
        dialog.setWidth("800px");
        dialog.setMaxHeight("90vh");

        HorizontalLayout header = new HorizontalLayout();
        header.setWidthFull();
        header.setJustifyContentMode(JustifyContentMode.BETWEEN);
        header.setAlignItems(Alignment.CENTER);

        H2 dialogTitle = new H2("🔍 Détails de l'Événement");
        dialogTitle.getStyle().set("margin", "0");

        Button closeBtn = new Button(new Icon(VaadinIcon.CLOSE));
        closeBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        closeBtn.addClickListener(e -> dialog.close());

        header.add(dialogTitle, closeBtn);

        VerticalLayout content = new VerticalLayout();
        content.setSpacing(true);
        content.setPadding(false);
        content.getStyle().set("overflow-y", "auto");

        // Afficher l'image
        if (event.getImageUrl() != null && !event.getImageUrl().isEmpty()) {
            try {
                String imageUrl = event.getImageUrl();
                if (!imageUrl.startsWith("http")) {
                    imageUrl = "/api/files" + imageUrl;
                }
                Image image = new Image(imageUrl, event.getTitre());
                image.setWidth("100%");
                image.setHeight("250px");
                image.getStyle()
                        .set("border-radius", "8px")
                        .set("object-fit", "cover");
                content.add(image);
            } catch (Exception e) {
                // Image non chargée
            }
        }

        FormLayout infoLayout = new FormLayout();
        infoLayout.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("500px", 2)
        );
        infoLayout.setWidthFull();

        Span titleLabel = new Span("Titre:");
        titleLabel.getStyle().set("font-weight", "600").set("color", "#374151");
        Span titleValue = new Span(event.getTitre());
        titleValue.getStyle().set("font-size", "16px");

        Span categoryLabel = new Span("Catégorie:");
        categoryLabel.getStyle().set("font-weight", "600").set("color", "#374151");
        Span categoryValue = new Span(getCategoryLabel(event.getCategorie()));

        Span statusLabel = new Span("Statut:");
        statusLabel.getStyle().set("font-weight", "600").set("color", "#374151");
        Span statusBadge = new Span(getStatusLabel(event.getStatut()));
        statusBadge.getStyle()
                .set("background", getStatusColor(event.getStatut()))
                .set("color", "white")
                .set("padding", "3px 10px")
                .set("border-radius", "12px")
                .set("font-size", "12px");

        Span descLabel = new Span("Description:");
        descLabel.getStyle().set("font-weight", "600").set("color", "#374151");
        Span descValue = new Span(event.getDescription() != null ? event.getDescription() : "Non renseignée");
        descValue.getStyle().set("white-space", "pre-wrap");

        Span startDateLabel = new Span("Date de début:");
        startDateLabel.getStyle().set("font-weight", "600").set("color", "#374151");
        Span startDateValue = new Span(event.getDateDebut().format(
                DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));

        Span endDateLabel = new Span("Date de fin:");
        endDateLabel.getStyle().set("font-weight", "600").set("color", "#374151");
        Span endDateValue = new Span(event.getDateFin().format(
                DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));

        Span locationLabel = new Span("Lieu:");
        locationLabel.getStyle().set("font-weight", "600").set("color", "#374151");
        Span locationValue = new Span(event.getLieu());

        Span cityLabel = new Span("Ville:");
        cityLabel.getStyle().set("font-weight", "600").set("color", "#374151");
        Span cityValue = new Span(event.getVille());

        Span capacityLabel = new Span("Capacité:");
        capacityLabel.getStyle().set("font-weight", "600").set("color", "#374151");
        int availablePlaces = eventService.getAvailablePlaces(event.getId());
        Span capacityValue = new Span(availablePlaces + " / " + event.getCapaciteMax() + " places");

        Span priceLabel = new Span("Prix:");
        priceLabel.getStyle().set("font-weight", "600").set("color", "#374151");
        Span priceValue = new Span(event.getPrixUnitaire() + " MAD");

        Span createdLabel = new Span("Créé le:");
        createdLabel.getStyle().set("font-weight", "600").set("color", "#374151");
        Span createdValue = new Span(event.getDateCreation() != null ?
                event.getDateCreation().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) : "N/A");

        infoLayout.add(titleLabel, titleValue);
        infoLayout.add(categoryLabel, categoryValue);
        infoLayout.add(statusLabel, statusBadge);
        infoLayout.add(startDateLabel, startDateValue);
        infoLayout.add(endDateLabel, endDateValue);
        infoLayout.add(locationLabel, locationValue);
        infoLayout.add(cityLabel, cityValue);
        infoLayout.add(capacityLabel, capacityValue);
        infoLayout.add(priceLabel, priceValue);
        infoLayout.add(createdLabel, createdValue);
        infoLayout.add(descLabel, 2);
        infoLayout.add(descValue, 2);

        content.add(infoLayout);

        HorizontalLayout actionButtons = new HorizontalLayout();
        actionButtons.setSpacing(true);
        actionButtons.getStyle().set("margin-top", "20px");

        Button editBtn = new Button("✏️ Modifier", new Icon(VaadinIcon.EDIT));
        editBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        editBtn.addClickListener(e -> {
            dialog.close();
            openEventDialog(event);
        });



        Button closeDialogBtn = new Button("Fermer", new Icon(VaadinIcon.CLOSE));
        closeDialogBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        closeDialogBtn.addClickListener(e -> dialog.close());

        actionButtons.add(editBtn, closeDialogBtn);
        content.add(actionButtons);

        VerticalLayout dialogLayout = new VerticalLayout(header, content);
        dialogLayout.setPadding(true);
        dialogLayout.setSpacing(true);
        dialogLayout.setSizeFull();

        dialog.add(dialogLayout);
        dialog.open();
    }

    private void openEventDialog(Event event) {
        Dialog dialog = new Dialog();
        dialog.setModal(true);
        dialog.setDraggable(false);
        dialog.setWidth("850px");
        dialog.setMaxHeight("90vh");

        // Réinitialiser les variables d'image
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

        previewImage = new Image();
        previewImage.setHeight("100%");
        previewImage.setWidth("100%");
        previewImage.getStyle().set("object-fit", "cover");

        // Afficher l'image existante
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

        HorizontalLayout buttonLayout = new HorizontalLayout();
        buttonLayout.setSpacing(true);
        buttonLayout.getStyle().set("margin-top", "20px");

        Button saveBtn = new Button("💾 Enregistrer", new Icon(VaadinIcon.CHECK));
        saveBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        saveBtn.addClickListener(e -> {
            if (validateEventForm(titreField, dateDebutPicker, dateFinPicker,
                    lieuField, villeField, capaciteField, prixField)) {

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
                    newEvent.setOrganisateur(currentUser);
                    newEvent.setStatut(statusCombo.getValue());

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
                                      NumberField prixField) {
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
            if (event.getImageUrl() != null && !event.getImageUrl().isEmpty()) {
                eventService.deleteEventWithImage(event.getId());
            } else {
                eventService.deleteEvent(event.getId());
            }
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