package com.eventbooking.view.components;

import com.eventbooking.entity.Event;
import com.eventbooking.entity.Reservation;
import com.eventbooking.entity.User;
import com.eventbooking.entity.enums.EventCategory;
import com.eventbooking.service.EventService;
import com.eventbooking.service.ReservationService;
import com.eventbooking.service.UserService;
import com.eventbooking.view.publicview.LoginView;
import com.eventbooking.view.publicview.RegisterView;
import com.vaadin.flow.component.*;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.theme.lumo.LumoUtility;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;
import java.time.Duration;

public class EventCard extends VerticalLayout {

    private final Event event;
    private final String baseUrl;
    private Button reserveButton;
    private Button detailsButton;
    private Div imageContainer;

    // Services
    private EventService eventService;
    private ReservationService reservationService;
    private UserService userService;

    private User currentUser;

    // Constructeur pour injection manuelle de services
    public EventCard(Event event, String baseUrl,
                     EventService eventService,
                     ReservationService reservationService,
                     UserService userService) {
        this.event = event;
        this.baseUrl = baseUrl != null ? baseUrl : "http://localhost:8080";

        // Injecter les services
        this.eventService = eventService;
        this.reservationService = reservationService;
        this.userService = userService;

        // Récupérer l'utilisateur connecté depuis la session
        Object userObj = VaadinSession.getCurrent().getAttribute("currentUser");
        this.currentUser = (userObj instanceof User) ? (User) userObj : null;

        initLayout();
    }

    // Constructeur sans services (pour compatibilité)
    public EventCard(Event event, String baseUrl) {
        this.event = event;
        this.baseUrl = baseUrl != null ? baseUrl : "http://localhost:8080";

        // Récupérer l'utilisateur connecté depuis la session
        Object userObj = VaadinSession.getCurrent().getAttribute("currentUser");
        this.currentUser = (userObj instanceof User) ? (User) userObj : null;

        initLayout();
    }

    private void initLayout() {
        setPadding(false);
        setSpacing(false);
        setWidth("100%"); // Utilise 100% au lieu de largeur fixe
        setHeight("500px"); // Hauteur fixe pour uniformiser

        addClassNames(
                LumoUtility.BorderRadius.LARGE,
                LumoUtility.Overflow.HIDDEN,
                LumoUtility.BoxShadow.MEDIUM,
                "event-card"
        );

        getStyle()
                .set("background", "white")
                .set("border", "1px solid #e9ecef")
                .set("cursor", "pointer")
                .set("transition", "transform 0.3s ease, box-shadow 0.3s ease")
                .set("position", "relative")
                .set("display", "flex")
                .set("flex-direction", "column");

        // Effet hover
        getElement().addEventListener("mouseenter", e -> {
            getStyle().set("transform", "translateY(-8px)")
                    .set("box-shadow", "0 12px 28px rgba(0,0,0,0.15)");
        });

        getElement().addEventListener("mouseleave", e -> {
            getStyle().set("transform", "translateY(0)")
                    .set("box-shadow", "0 4px 12px rgba(0,0,0,0.1)");
        });

        createLayout();
    }

    private void createLayout() {
        // Image section
        imageContainer = createImageSection();

        // Content section
        VerticalLayout contentSection = createContentSection();

        // Main layout
        add(imageContainer, contentSection);
    }

    private Div createImageSection() {
        Div imageDiv = new Div();
        imageDiv.addClassNames("event-image-container");
        imageDiv.getStyle()
                .set("height", "200px") // Plus haute
                .set("width", "100%")
                .set("position", "relative")
                .set("overflow", "hidden")
                .set("border-radius", "12px 12px 0 0");

        // Overlay gradient pour améliorer la lisibilité des badges
        Div overlay = new Div();
        overlay.getStyle()
                .set("position", "absolute")
                .set("top", "0")
                .set("left", "0")
                .set("right", "0")
                .set("height", "100px")
                .set("background", "linear-gradient(to bottom, rgba(0,0,0,0.3), transparent)")
                .set("pointer-events", "none");

        // Créer l'image avec la même logique que votre Grid
        createImageContent(imageDiv);

        // Category badge - redesigned
        Span categoryBadge = new Span(getCategoryLabel(event.getCategorie()));
        categoryBadge.addClassNames(
                LumoUtility.Padding.Horizontal.MEDIUM,
                LumoUtility.Padding.Vertical.XSMALL,
                LumoUtility.BorderRadius.LARGE,
                LumoUtility.FontSize.SMALL,
                LumoUtility.FontWeight.BOLD
        );
        categoryBadge.getStyle()
                .set("background", getCategoryColor(event.getCategorie()))
                .set("color", "white")
                .set("position", "absolute")
                .set("top", "15px")
                .set("left", "15px")
                .set("box-shadow", "0 4px 8px rgba(0,0,0,0.2)")
                .set("z-index", "10");

        // Date badge - redesigned
        Span dateBadge = new Span(formatDate(event.getDateDebut()));
        dateBadge.addClassNames(
                LumoUtility.Padding.Horizontal.MEDIUM,
                LumoUtility.Padding.Vertical.XSMALL,
                LumoUtility.BorderRadius.LARGE,
                LumoUtility.FontSize.SMALL,
                LumoUtility.FontWeight.BOLD
        );
        dateBadge.getStyle()
                .set("background", "rgba(0, 0, 0, 0.85)")
                .set("color", "white")
                .set("position", "absolute")
                .set("bottom", "15px")
                .set("right", "15px")
                .set("box-shadow", "0 4px 8px rgba(0,0,0,0.2)")
                .set("z-index", "10");

        imageDiv.add(overlay, categoryBadge, dateBadge);

        return imageDiv;
    }

    private void createImageContent(Div imageDiv) {
        String imageUrl = getEventImageUrl(event);

        if (imageUrl != null && !imageUrl.isEmpty() && !imageUrl.startsWith("https://images.unsplash.com")) {
            try {
                String finalUrl = imageUrl;
                if (!imageUrl.startsWith("http") && !imageUrl.startsWith("data:image")) {
                    if (imageUrl.startsWith("/uploads/")) {
                        finalUrl = "/api/files" + imageUrl;
                    } else {
                        finalUrl = "/api/files/uploads/" + imageUrl;
                    }

                    if (!finalUrl.startsWith("http")) {
                        finalUrl = baseUrl + finalUrl;
                    }
                }

                Image image = new Image(finalUrl, event.getTitre());
                image.setWidth("100%");
                image.setHeight("100%");
                image.getStyle()
                        .set("object-fit", "cover")
                        .set("object-position", "center")
                        .set("transition", "transform 0.5s ease");

                // Zoom effect on hover
                image.getElement().addEventListener("mouseenter", e -> {
                    image.getStyle().set("transform", "scale(1.1)");
                });
                image.getElement().addEventListener("mouseleave", e -> {
                    image.getStyle().set("transform", "scale(1)");
                });

                imageDiv.add(image);

            } catch (Exception e) {
                imageDiv.getStyle()
                        .set("background", getCategoryGradient(event.getCategorie()))
                        .set("display", "flex")
                        .set("align-items", "center")
                        .set("justify-content", "center");

                Icon defaultIcon = getCategoryIcon(event.getCategorie());
                defaultIcon.setSize("64px");
                defaultIcon.setColor("rgba(255,255,255,0.8)");
                imageDiv.add(defaultIcon);
            }
        } else {
            imageDiv.getStyle()
                    .set("background", getCategoryGradient(event.getCategorie()))
                    .set("display", "flex")
                    .set("align-items", "center")
                    .set("justify-content", "center");

            Icon defaultIcon = getCategoryIcon(event.getCategorie());
            defaultIcon.setSize("64px");
            defaultIcon.setColor("rgba(255,255,255,0.8)");
            imageDiv.add(defaultIcon);
        }
    }

    private VerticalLayout createContentSection() {
        VerticalLayout content = new VerticalLayout();
        content.addClassNames(
                LumoUtility.Padding.LARGE,
                "event-content"
        );
        content.setSpacing(true);
        content.setPadding(true);

        // Title with truncation
        H3 title = new H3(event.getTitre());
        title.addClassNames(
                LumoUtility.FontSize.LARGE,
                LumoUtility.FontWeight.BOLD,
                LumoUtility.Margin.Bottom.SMALL
        );
        title.getStyle()
                .set("flex-grow", "1") // Prend tout l'espace disponible
                .set("display", "flex")
                .set("flex-direction", "column")
                .set("color", "#2d3748")
                .set("height", "56px")
                .set("overflow", "hidden")
                .set("line-height", "1.4")
                .set("display", "-webkit-box")
                .set("-webkit-line-clamp", "2")
                .set("-webkit-box-orient", "vertical");

        // Description (truncated)
        String description = event.getDescription();
        if (description != null && description.length() > 120) {
            description = description.substring(0, 120) + "...";
        }

        Paragraph desc = new Paragraph(description != null ? description : "Aucune description disponible");
        desc.addClassNames(
                LumoUtility.FontSize.SMALL,
                LumoUtility.TextColor.SECONDARY,
                LumoUtility.Margin.Bottom.MEDIUM
        );
        desc.getStyle()
                .set("height", "48px")
                .set("overflow", "hidden")
                .set("line-height", "1.5")
                .set("display", "-webkit-box")
                .set("-webkit-line-clamp", "2")
                .set("-webkit-box-orient", "vertical");

        // Info icons with improved styling
        VerticalLayout infoLayout = new VerticalLayout();
        infoLayout.setSpacing(true);
        infoLayout.setPadding(false);
        infoLayout.getStyle().set("margin-bottom", "16px");

        // Date and time with icon
        Div dateTimeContainer = createInfoItem(
                VaadinIcon.CALENDAR,
                formatDateTime(event.getDateDebut()),
                "#667eea"
        );

        // Location with icon
        String locationText = event.getVille();
        if (event.getLieu() != null && !event.getLieu().isEmpty()) {
            locationText = event.getLieu() + ", " + locationText;
        }
        Div locationContainer = createInfoItem(
                VaadinIcon.MAP_MARKER,
                locationText,
                "#ed8936"
        );

        // Available places with dynamic color
        int availablePlaces = calculateAvailablePlaces();
        String placesText = availablePlaces + " places disponibles";
        String placesColor = availablePlaces > 10 ? "#38a169" :
                availablePlaces > 0 ? "#d69e2e" : "#e53e3e";
        Div placesContainer = createInfoItem(
                VaadinIcon.USERS,
                placesText,
                placesColor
        );

        infoLayout.add(dateTimeContainer, locationContainer, placesContainer);

        // Price and action container
        HorizontalLayout bottomContainer = new HorizontalLayout();
        bottomContainer.setWidthFull();
        bottomContainer.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        bottomContainer.setAlignItems(Alignment.END);
        bottomContainer.setSpacing(true);

        // Price section
        Div priceSection = new Div();
        if (event.getPrixUnitaire() != null && event.getPrixUnitaire() > 0) {
            // Currency symbol
            Span currency = new Span("MAD");
            currency.addClassNames(LumoUtility.FontSize.SMALL, LumoUtility.TextColor.SECONDARY);
            currency.getStyle().set("margin-right", "4px");

            // Price value
            H3 price = new H3(String.valueOf(event.getPrixUnitaire()));
            price.addClassNames(
                    LumoUtility.FontSize.XLARGE,
                    LumoUtility.FontWeight.BOLD,
                    "price-text"
            );
            price.getStyle()
                    .set("color", "#667eea")
                    .set("margin", "0");

            priceSection.add(currency, price);
        } else {
            Span freeBadge = new Span("GRATUIT");
            freeBadge.addClassNames(
                    LumoUtility.Padding.Horizontal.MEDIUM,
                    LumoUtility.Padding.Vertical.XSMALL,
                    LumoUtility.BorderRadius.LARGE,
                    LumoUtility.FontSize.MEDIUM,
                    LumoUtility.FontWeight.BOLD
            );
            freeBadge.getStyle()
                    .set("background", "linear-gradient(135deg, #38a169, #48bb78)")
                    .set("color", "white")
                    .set("box-shadow", "0 2px 8px rgba(56, 161, 105, 0.3)");

            priceSection.add(freeBadge);
        }

        // Buttons container
        HorizontalLayout buttonsContainer = new HorizontalLayout();
        buttonsContainer.setSpacing(true);
        buttonsContainer.getStyle().set("flex-shrink", "0");

        // Details button
        detailsButton = new Button(new Icon(VaadinIcon.EYE));
        detailsButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ICON);
        detailsButton.getElement().setAttribute("aria-label", "Voir les détails");
        detailsButton.getStyle()
                .set("border", "1px solid #e2e8f0")
                .set("border-radius", "8px")
                .set("padding", "8px")
                .set("cursor", "pointer")
                .set("background", "white");
        detailsButton.addClickListener(e -> {
            e.getSource().getElement().executeJs("event.stopPropagation()");
            showEventDetailsDialog();
        });

        // Reserve button
        reserveButton = new Button("Réserver", new Icon(VaadinIcon.TICKET));
        reserveButton.addThemeVariants(
                ButtonVariant.LUMO_PRIMARY,
                ButtonVariant.LUMO_SMALL
        );
        reserveButton.getStyle()
                .set("background", "linear-gradient(135deg, #667eea 0%, #764ba2 100%)")
                .set("border", "none")
                .set("border-radius", "8px")
                .set("padding", "8px 16px")
                .set("cursor", "pointer")
                .set("color", "white")
                .set("font-weight", "600")
                .set("letter-spacing", "0.3px");

        // Désactiver le bouton si pas de places ou événement passé
        boolean isEventPast = event.getDateDebut().isBefore(LocalDateTime.now());
        boolean isEventFull = availablePlaces <= 0;

        if (isEventPast || isEventFull) {
            reserveButton.setEnabled(false);
            if (isEventPast) {
                reserveButton.setText("Terminé");
                reserveButton.addThemeVariants(ButtonVariant.LUMO_CONTRAST);
                reserveButton.getStyle()
                        .set("background", "#a0aec0")
                        .set("cursor", "not-allowed");
            } else if (isEventFull) {
                reserveButton.setText("Complet");
                reserveButton.addThemeVariants(ButtonVariant.LUMO_ERROR);
                reserveButton.getStyle()
                        .set("background", "#fc8181")
                        .set("cursor", "not-allowed");
            }
        } else {
            // Add hover effect
            reserveButton.getElement().addEventListener("mouseenter", e -> {
                reserveButton.getStyle().set("transform", "translateY(-2px)");
            });
            reserveButton.getElement().addEventListener("mouseleave", e -> {
                reserveButton.getStyle().set("transform", "translateY(0)");
            });

            reserveButton.addClickListener(e -> {
                e.getSource().getElement().executeJs("event.stopPropagation()");
                showReservationDialog();
            });
        }

        buttonsContainer.add(detailsButton, reserveButton);
        bottomContainer.add(priceSection, buttonsContainer);
        content.setFlexGrow(1, title);
        content.setFlexGrow(1, desc);
        content.setFlexGrow(1, infoLayout);

        content.add(title, desc, infoLayout, bottomContainer);

        return content;
    }

    private Div createInfoItem(VaadinIcon vaadinIcon, String text, String iconColor) {
        Div container = new Div();
        container.getStyle()
                .set("display", "flex")
                .set("align-items", "center")
                .set("gap", "10px");

        // Icon container
        Div iconContainer = new Div();
        iconContainer.getStyle()
                .set("width", "32px")
                .set("height", "32px")
                .set("border-radius", "8px")
                .set("background", iconColor + "15")
                .set("display", "flex")
                .set("align-items", "center")
                .set("justify-content", "center")
                .set("flex-shrink", "0");

        Icon rowIcon = new Icon(vaadinIcon);
        rowIcon.setSize("16px");
        rowIcon.setColor(iconColor);
        iconContainer.add(rowIcon);

        // Text
        Span rowText = new Span(text);
        rowText.addClassNames(
                LumoUtility.FontSize.SMALL,
                LumoUtility.FontWeight.MEDIUM
        );
        rowText.getStyle().set("color", "#4a5568");

        container.add(iconContainer, rowText);
        return container;
    }

    private int calculateAvailablePlaces() {
        if (event.getCapaciteMax() == null) {
            return 0;
        }

        if (eventService != null) {
            try {
                return eventService.getAvailablePlaces(event.getId());
            } catch (Exception e) {
                return event.getCapaciteMax();
            }
        }
        return event.getCapaciteMax();
    }

    // ==================== MODAL DE DÉTAILS DE L'ÉVÉNEMENT ====================
    private void showEventDetailsDialog() {
        Dialog detailsDialog = new Dialog();
        detailsDialog.setCloseOnOutsideClick(true);
        detailsDialog.setCloseOnEsc(true);
        detailsDialog.setWidth("900px");
        detailsDialog.setMaxWidth("95vw");
        detailsDialog.setHeight("90vh");
        detailsDialog.setMaxHeight("95vh");
        detailsDialog.addClassName("event-details-dialog");

        // Header avec glass effect
        Div header = new Div();
        header.getStyle()
                .set("background", "rgba(255, 255, 255, 0.95)")
                .set("backdrop-filter", "blur(10px)")
                .set("padding", "20px 24px")
                .set("border-bottom", "1px solid #e9ecef")
                .set("position", "sticky")
                .set("top", "0")
                .set("z-index", "100");

        HorizontalLayout headerContent = new HorizontalLayout();
        headerContent.setWidthFull();
        headerContent.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        headerContent.setAlignItems(Alignment.CENTER);

        H2 title = new H2(event.getTitre());
        title.getStyle()
                .set("margin", "0")
                .set("color", "#2d3748")
                .set("font-size", "1.75rem")
                .set("font-weight", "700");

        Button closeButton = new Button(new Icon(VaadinIcon.CLOSE));
        closeButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        closeButton.addClassName("close-button");
        closeButton.addClickListener(e -> detailsDialog.close());

        headerContent.add(title, closeButton);
        header.add(headerContent);

        // Contenu principal avec scroll
        VerticalLayout content = new VerticalLayout();
        content.setSpacing(false);
        content.setPadding(false);
        content.getStyle()
                .set("overflow-y", "auto")
                .set("height", "calc(100% - 80px)");

        // Section image avec overlay
        Div imageSection = new Div();
        imageSection.getStyle()
                .set("width", "100%")
                .set("height", "350px")
                .set("position", "relative")
                .set("overflow", "hidden");

        // Image container
        Div imageContainer = new Div();
        imageContainer.getStyle()
                .set("width", "100%")
                .set("height", "100%")
                .set("background", getCategoryGradient(event.getCategorie()));

        String imageUrl = getEventImageUrl(event);
        if (imageUrl != null && !imageUrl.isEmpty() && !imageUrl.startsWith("https://images.unsplash.com")) {
            try {
                String finalUrl = imageUrl;
                if (!imageUrl.startsWith("http") && !imageUrl.startsWith("data:image")) {
                    if (imageUrl.startsWith("/uploads/")) {
                        finalUrl = "/api/files" + imageUrl;
                    } else {
                        finalUrl = "/api/files/uploads/" + imageUrl;
                    }
                    if (!finalUrl.startsWith("http")) {
                        finalUrl = baseUrl + finalUrl;
                    }
                }
                Image image = new Image(finalUrl, event.getTitre());
                image.setWidth("100%");
                image.setHeight("100%");
                image.getStyle().set("object-fit", "cover");
                imageContainer.add(image);
            } catch (Exception e) {
                Icon defaultIcon = getCategoryIcon(event.getCategorie());
                defaultIcon.setSize("80px");
                defaultIcon.setColor("rgba(255,255,255,0.8)");
                imageContainer.getStyle().set("display", "flex")
                        .set("align-items", "center")
                        .set("justify-content", "center");
                imageContainer.add(defaultIcon);
            }
        }

        // Gradient overlay
        Div imageOverlay = new Div();
        imageOverlay.getStyle()
                .set("position", "absolute")
                .set("bottom", "0")
                .set("left", "0")
                .set("right", "0")
                .set("height", "50%")
                .set("background", "linear-gradient(to top, rgba(0,0,0,0.8), transparent)")
                .set("pointer-events", "none");

        // Badges flottants
        Div floatingBadges = new Div();
        floatingBadges.getStyle()
                .set("position", "absolute")
                .set("top", "20px")
                .set("left", "20px")
                .set("right", "20px")
                .set("display", "flex")
                .set("gap", "12px")
                .set("flex-wrap", "wrap");

        // Badge catégorie
        Span categoryBadge = new Span(getCategoryLabel(event.getCategorie()));
        categoryBadge.addClassNames(
                LumoUtility.Padding.Horizontal.MEDIUM,
                LumoUtility.Padding.Vertical.SMALL,
                LumoUtility.BorderRadius.LARGE,
                LumoUtility.FontSize.SMALL,
                LumoUtility.FontWeight.BOLD
        );
        categoryBadge.getStyle()
                .set("background", getCategoryColor(event.getCategorie()))
                .set("color", "white")
                .set("box-shadow", "0 4px 12px rgba(0,0,0,0.2)");

        // Badge prix
        String priceText = event.getPrixUnitaire() != null && event.getPrixUnitaire() > 0
                ? event.getPrixUnitaire() + " MAD"
                : "GRATUIT";
        Span priceBadge = new Span(priceText);
        priceBadge.addClassNames(
                LumoUtility.Padding.Horizontal.MEDIUM,
                LumoUtility.Padding.Vertical.SMALL,
                LumoUtility.BorderRadius.LARGE,
                LumoUtility.FontSize.SMALL,
                LumoUtility.FontWeight.BOLD
        );
        priceBadge.getStyle()
                .set("background", event.getPrixUnitaire() != null && event.getPrixUnitaire() > 0
                        ? "#667eea"
                        : "#38a169")
                .set("color", "white")
                .set("box-shadow", "0 4px 12px rgba(0,0,0,0.2)");

        // Badge disponibilité
        int availablePlaces = calculateAvailablePlaces();
        String availabilityText = availablePlaces > 0 ? availablePlaces + " places" : "Complet";
        Span availabilityBadge = new Span(availabilityText);
        availabilityBadge.addClassNames(
                LumoUtility.Padding.Horizontal.MEDIUM,
                LumoUtility.Padding.Vertical.SMALL,
                LumoUtility.BorderRadius.LARGE,
                LumoUtility.FontSize.SMALL,
                LumoUtility.FontWeight.BOLD
        );
        availabilityBadge.getStyle()
                .set("background", availablePlaces > 0 ? "#38a169" : "#e53e3e")
                .set("color", "white")
                .set("box-shadow", "0 4px 12px rgba(0,0,0,0.2)");

        floatingBadges.add(categoryBadge, priceBadge, availabilityBadge);
        imageSection.add(imageContainer, imageOverlay, floatingBadges);

        // Contenu principal
        VerticalLayout mainContent = new VerticalLayout();
        mainContent.setSpacing(true);
        mainContent.setPadding(true);
        mainContent.getStyle().set("background", "white");

        // Grid d'informations
        Div infoGrid = new Div();
        infoGrid.getStyle()
                .set("display", "grid")
                .set("grid-template-columns", "repeat(auto-fit, minmax(280px, 1fr))")
                .set("gap", "20px")
                .set("margin-bottom", "30px");

        // Cartes d'information améliorées
        Div dateTimeCard = createDetailCard(
                new Icon(VaadinIcon.CALENDAR),
                "Date & Heure",
                formatDateTime(event.getDateDebut()),
                event.getDateDebut().isBefore(LocalDateTime.now()) ? "Événement terminé" : "Événement à venir",
                "#667eea"
        );

        Div locationCard = createDetailCard(
                new Icon(VaadinIcon.MAP_MARKER),
                "Lieu",
                event.getLieu() != null ? event.getLieu() + ", " + event.getVille() : event.getVille(),
                event.getVille(),
                "#ed8936"
        );

        Div placesCard = createDetailCard(
                new Icon(VaadinIcon.USERS),
                "Disponibilité",
                availablePlaces + " / " + event.getCapaciteMax() + " places",
                availablePlaces > 0 ? "Inscriptions ouvertes" : "Complet",
                availablePlaces > 0 ? "#38a169" : "#e53e3e"
        );

        Div organizerCard = createDetailCard(
                new Icon(VaadinIcon.USER),
                "Organisateur",
                event.getOrganisateur() != null ? event.getOrganisateur().getNom() : "Non spécifié",
                event.getOrganisateur() != null ? event.getOrganisateur().getEmail() : "N/A",
                "#805ad5"
        );

        infoGrid.add(dateTimeCard, locationCard, placesCard, organizerCard);

        // Section description
        Div descriptionSection = new Div();
        descriptionSection.getStyle()
                .set("background", "#f8fafc")
                .set("border-radius", "16px")
                .set("padding", "28px")
                .set("margin-bottom", "30px");

        H3 descriptionTitle = new H3("Description");
        descriptionTitle.getStyle()
                .set("margin", "0 0 20px 0")
                .set("color", "#2d3748")
                .set("font-size", "1.5rem");

        Paragraph descriptionText = new Paragraph(
                event.getDescription() != null ? event.getDescription() : "Aucune description disponible."
        );
        descriptionText.getStyle()
                .set("color", "#4a5568")
                .set("line-height", "1.7")
                .set("margin", "0")
                .set("font-size", "1.1rem");

        descriptionSection.add(descriptionTitle, descriptionText);

        // Informations supplémentaires (si dateFin existe)
        if (event.getDateFin() != null) {
            Div additionalSection = new Div();
            additionalSection.getStyle()
                    .set("background", "#ebf8ff")
                    .set("border-radius", "16px")
                    .set("padding", "28px")
                    .set("margin-bottom", "30px")
                    .set("border", "1px solid #bee3f8");

            H3 additionalTitle = new H3("Informations supplémentaires");
            additionalTitle.getStyle()
                    .set("margin", "0 0 20px 0")
                    .set("color", "#2b6cb0")
                    .set("font-size", "1.5rem");

            Div additionalGrid = new Div();
            additionalGrid.getStyle()
                    .set("display", "grid")
                    .set("grid-template-columns", "repeat(auto-fit, minmax(200px, 1fr))")
                    .set("gap", "20px");

            // Date de fin
            additionalGrid.add(createAdditionalInfoItem(
                    "Date de fin",
                    event.getDateFin().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
            ));

            // Durée
            Duration duration = Duration.between(event.getDateDebut(), event.getDateFin());
            long hours = duration.toHours();
            long minutes = duration.toMinutes() % 60;
            String durationText = hours + "h" + (minutes > 0 ? " " + minutes + "min" : "");
            additionalGrid.add(createAdditionalInfoItem("Durée", durationText));

            // Temps restant
            if (event.getDateDebut().isAfter(LocalDateTime.now())) {
                Duration timeLeft = Duration.between(LocalDateTime.now(), event.getDateDebut());
                long days = timeLeft.toDays();
                long remainingHours = timeLeft.toHours() % 24;
                String timeLeftText = days > 0 ? days + " jour" + (days > 1 ? "s" : "") :
                        remainingHours > 0 ? remainingHours + " heure" + (remainingHours > 1 ? "s" : "") :
                                "Moins d'une heure";
                additionalGrid.add(createAdditionalInfoItem("Commence dans", timeLeftText));
            }

            additionalSection.add(additionalTitle, additionalGrid);
            mainContent.add(additionalSection);
        }

        // Boutons d'action
        HorizontalLayout actionButtons = new HorizontalLayout();
        actionButtons.setWidthFull();
        actionButtons.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        actionButtons.setSpacing(true);
        actionButtons.getStyle()
                .set("margin-top", "30px")
                .set("padding-top", "30px")
                .set("border-top", "1px solid #e9ecef");

        // Bouton Réserver
        Button reserveInModalButton = new Button("Réserver maintenant", new Icon(VaadinIcon.TICKET));
        reserveInModalButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_LARGE);
        reserveInModalButton.getStyle()
                .set("background", "linear-gradient(135deg, #667eea 0%, #764ba2 100%)")
                .set("border", "none")
                .set("border-radius", "12px")
                .set("padding", "16px 40px")
                .set("font-weight", "600")
                .set("letter-spacing", "0.5px")
                .set("box-shadow", "0 8px 24px rgba(102, 126, 234, 0.3)")
                .set("transition", "all 0.3s ease");

        // Hover effect
        reserveInModalButton.getElement().addEventListener("mouseenter", e -> {
            reserveInModalButton.getStyle()
                    .set("transform", "translateY(-3px)")
                    .set("box-shadow", "0 12px 28px rgba(102, 126, 234, 0.4)");
        });
        reserveInModalButton.getElement().addEventListener("mouseleave", e -> {
            reserveInModalButton.getStyle()
                    .set("transform", "translateY(0)")
                    .set("box-shadow", "0 8px 24px rgba(102, 126, 234, 0.3)");
        });

        // Désactiver si pas de places ou événement passé
        boolean isEventPast = event.getDateDebut().isBefore(LocalDateTime.now());
        boolean isEventFull = availablePlaces <= 0;

        if (isEventPast || isEventFull) {
            reserveInModalButton.setEnabled(false);
            if (isEventPast) {
                reserveInModalButton.setText("Événement terminé");
                reserveInModalButton.addThemeVariants(ButtonVariant.LUMO_CONTRAST);
                reserveInModalButton.getStyle()
                        .set("background", "#a0aec0")
                        .set("box-shadow", "none");
            } else if (isEventFull) {
                reserveInModalButton.setText("Complet");
                reserveInModalButton.addThemeVariants(ButtonVariant.LUMO_ERROR);
                reserveInModalButton.getStyle()
                        .set("background", "#fc8181")
                        .set("box-shadow", "none");
            }
        } else {
            reserveInModalButton.addClickListener(e -> {
                detailsDialog.close();
                showReservationDialog();
            });
        }

        // Bouton Partager
        Button shareButton = new Button("Partager", new Icon(VaadinIcon.SHARE));
        shareButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        shareButton.getStyle()
                .set("border-radius", "12px")
                .set("padding", "12px 24px");

        // Bouton Fermer
        Button closeDetailsButton = new Button("Fermer", new Icon(VaadinIcon.CLOSE));
        closeDetailsButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        closeDetailsButton.addClickListener(e -> detailsDialog.close());

        actionButtons.add(reserveInModalButton, shareButton, closeDetailsButton);

        // Assembler le contenu
        mainContent.add(infoGrid, descriptionSection, actionButtons);
        content.add(imageSection, mainContent);

        // Ajouter tout au dialog
        VerticalLayout dialogContent = new VerticalLayout();
        dialogContent.setSpacing(false);
        dialogContent.setPadding(false);
        dialogContent.setSizeFull();
        dialogContent.add(header, content);

        detailsDialog.add(dialogContent);
        detailsDialog.open();
    }

    private Div createDetailCard(Icon icon, String title, String value, String subtitle, String iconColor) {
        Div card = new Div();
        card.getStyle()
                .set("background", "white")
                .set("border-radius", "16px")
                .set("padding", "24px")
                .set("border", "1px solid #e9ecef")
                .set("box-shadow", "0 4px 12px rgba(0,0,0,0.05)")
                .set("transition", "all 0.3s ease")
                .set("position", "relative")
                .set("overflow", "hidden");

        // Hover effect
        card.getElement().addEventListener("mouseenter", e -> {
            card.getStyle()
                    .set("transform", "translateY(-4px)")
                    .set("box-shadow", "0 8px 24px rgba(0,0,0,0.1)")
                    .set("border-color", iconColor + "40");
        });
        card.getElement().addEventListener("mouseleave", e -> {
            card.getStyle()
                    .set("transform", "translateY(0)")
                    .set("box-shadow", "0 4px 12px rgba(0,0,0,0.05)")
                    .set("border-color", "#e9ecef");
        });

        // Icon circle
        Div iconCircle = new Div();
        iconCircle.getStyle()
                .set("width", "56px")
                .set("height", "56px")
                .set("border-radius", "50%")
                .set("background", iconColor + "15")
                .set("display", "flex")
                .set("align-items", "center")
                .set("justify-content", "center")
                .set("margin-bottom", "16px");

        icon.setSize("24px");
        icon.setColor(iconColor);
        iconCircle.add(icon);

        // Title
        H4 cardTitle = new H4(title);
        cardTitle.getStyle()
                .set("margin", "0 0 8px 0")
                .set("color", "#4a5568")
                .set("font-size", "0.9rem")
                .set("font-weight", "600")
                .set("text-transform", "uppercase")
                .set("letter-spacing", "0.5px");

        // Value
        H3 cardValue = new H3(value);
        cardValue.getStyle()
                .set("margin", "0 0 4px 0")
                .set("color", "#2d3748")
                .set("font-size", "1.5rem")
                .set("font-weight", "700");

        // Subtitle
        Span cardSubtitle = new Span(subtitle);
        cardSubtitle.getStyle()
                .set("color", "#a0aec0")
                .set("font-size", "0.875rem")
                .set("font-weight", "500");

        card.add(iconCircle, cardTitle, cardValue, cardSubtitle);
        return card;
    }

    private Div createAdditionalInfoItem(String label, String value) {
        Div item = new Div();
        item.getStyle()
                .set("background", "white")
                .set("border-radius", "12px")
                .set("padding", "20px")
                .set("border", "1px solid #e9ecef");

        Span itemLabel = new Span(label);
        itemLabel.getStyle()
                .set("display", "block")
                .set("color", "#718096")
                .set("font-size", "0.875rem")
                .set("font-weight", "500")
                .set("margin-bottom", "8px");

        Span itemValue = new Span(value);
        itemValue.getStyle()
                .set("display", "block")
                .set("color", "#2d3748")
                .set("font-size", "1.25rem")
                .set("font-weight", "600");

        item.add(itemLabel, itemValue);
        return item;
    }

    // ==================== MODAL DE RÉSERVATION ====================
    private void showReservationDialog() {
        // Vérifier si les services sont disponibles
        if (eventService == null || reservationService == null) {
            Notification.show("Erreur: Services non disponibles. Veuillez réessayer.", 3000, Notification.Position.MIDDLE);
            return;
        }

        // Vérifier si l'utilisateur est connecté
        if (currentUser == null) {
            // Afficher un dialog pour choisir entre login et register
            showAuthRequiredDialog();
            return;
        }

        // Vérifier si l'utilisateur a déjà réservé cet événement
        if (reservationService.hasUserReservedEvent(currentUser.getId(), event.getId())) {
            Notification.show("Vous avez déjà réservé cet événement!", 3000, Notification.Position.MIDDLE);
            return;
        }

        // Vérifier la disponibilité
        int availablePlaces = eventService.getAvailablePlaces(event.getId());
        if (availablePlaces <= 0) {
            Notification.show("Désolé, plus de places disponibles pour cet événement!", 3000, Notification.Position.MIDDLE);
            return;
        }

        // Vérifier si l'événement est passé
        if (event.getDateDebut().isBefore(LocalDateTime.now())) {
            Notification.show("Cet événement est déjà passé!", 3000, Notification.Position.MIDDLE);
            return;
        }

        // Créer le dialog de réservation
        Dialog reservationDialog = new Dialog();
        reservationDialog.setCloseOnOutsideClick(true);
        reservationDialog.setWidth("600px");
        reservationDialog.setMaxWidth("90vw");
        reservationDialog.setCloseOnEsc(true);
        reservationDialog.addClassName("reservation-dialog");

        // Header avec style moderne
        Div header = new Div();
        header.getStyle()
                .set("background", "linear-gradient(135deg, #667eea 0%, #764ba2 100%)")
                .set("padding", "24px")
                .set("border-radius", "16px 16px 0 0");

        HorizontalLayout headerContent = new HorizontalLayout();
        headerContent.setWidthFull();
        headerContent.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        headerContent.setAlignItems(Alignment.CENTER);

        H3 title = new H3("Réserver votre place");
        title.getStyle()
                .set("margin", "0")
                .set("color", "white")
                .set("font-size", "1.5rem");

        Button closeButton = new Button(new Icon(VaadinIcon.CLOSE));
        closeButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        closeButton.getStyle().set("color", "white");
        closeButton.addClickListener(e -> reservationDialog.close());

        headerContent.add(title, closeButton);
        header.add(headerContent);

        // Contenu du dialog
        VerticalLayout content = new VerticalLayout();
        content.setSpacing(true);
        content.setPadding(true);
        content.getStyle().set("background", "white");

        // Carte d'information de l'événement
        Div eventCard = new Div();
        eventCard.getStyle()
                .set("background", "#f8fafc")
                .set("border-radius", "12px")
                .set("padding", "20px")
                .set("margin-bottom", "20px")
                .set("border", "1px solid #e2e8f0");

        VerticalLayout eventInfo = new VerticalLayout();
        eventInfo.setSpacing(true);
        eventInfo.setPadding(false);

        // Titre de l'événement
        H4 eventTitle = new H4(event.getTitre());
        eventTitle.getStyle()
                .set("margin", "0")
                .set("color", "#2d3748")
                .set("font-size", "1.25rem");

        // Détails en grille
        Div detailsGrid = new Div();
        detailsGrid.getStyle()
                .set("display", "grid")
                .set("grid-template-columns", "repeat(auto-fit, minmax(200px, 1fr))")
                .set("gap", "16px")
                .set("margin-top", "16px");

        // Date
        Div dateItem = createReservationDetailItem(
                VaadinIcon.CALENDAR,
                "Date",
                formatDateTime(event.getDateDebut()),
                "#667eea"
        );

        // Lieu
        String locationDisplay = event.getLieu() != null && !event.getLieu().isEmpty()
                ? event.getLieu() + ", " + event.getVille()
                : event.getVille();
        Div locationItem = createReservationDetailItem(
                VaadinIcon.MAP_MARKER,
                "Lieu",
                locationDisplay,
                "#ed8936"
        );

        // Places disponibles avec indicateur visuel
        String availabilityStatus = availablePlaces > 10 ? "Disponible" :
                availablePlaces > 0 ? "Peu de places" : "Complet";
        String availabilityColor = availablePlaces > 10 ? "#38a169" :
                availablePlaces > 0 ? "#d69e2e" : "#e53e3e";
        Div availabilityItem = createReservationDetailItem(
                VaadinIcon.USERS,
                "Places",
                availablePlaces + " disponibles",
                availabilityColor
        );

        // Indicateur de disponibilité
        Div availabilityIndicator = new Div();
        availabilityIndicator.getStyle()
                .set("height", "8px")
                .set("background", availabilityColor + "30")
                .set("border-radius", "4px")
                .set("margin-top", "8px")
                .set("overflow", "hidden");

        Div availabilityFill = new Div();
        int fillPercentage = Math.min(100, (availablePlaces * 100) / Math.max(1, event.getCapaciteMax()));
        availabilityFill.getStyle()
                .set("width", fillPercentage + "%")
                .set("height", "100%")
                .set("background", availabilityColor)
                .set("border-radius", "4px")
                .set("transition", "width 0.3s ease");

        availabilityIndicator.add(availabilityFill);
        availabilityItem.add(availabilityIndicator);

        detailsGrid.add(dateItem, locationItem, availabilityItem);
        eventInfo.add(eventTitle, detailsGrid);
        eventCard.add(eventInfo);

        // Formulaire de réservation
        Div formCard = new Div();
        formCard.getStyle()
                .set("background", "white")
                .set("border", "1px solid #e2e8f0")
                .set("border-radius", "12px")
                .set("padding", "24px");

        H4 formTitle = new H4("Détails de la réservation");
        formTitle.getStyle()
                .set("margin", "0 0 20px 0")
                .set("color", "#2d3748");

        FormLayout form = new FormLayout();
        form.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("500px", 2)
        );

        // Nombre de places avec style amélioré
        IntegerField numberOfPlaces = new IntegerField("Nombre de places");
        numberOfPlaces.setMin(1);
        numberOfPlaces.setMax(Math.min(10, availablePlaces));
        numberOfPlaces.setValue(1);
        numberOfPlaces.setStepButtonsVisible(true);
        numberOfPlaces.setWidthFull();
        numberOfPlaces.getStyle()
                .set("--vaadin-input-field-border-radius", "8px")
                .set("--vaadin-input-field-border-color", "#e2e8f0");

        // Prix unitaire
        Div priceDisplay = new Div();
        String priceText = event.getPrixUnitaire() != null && event.getPrixUnitaire() > 0
                ? event.getPrixUnitaire() + " MAD / place"
                : "GRATUIT";
        H4 priceTitle = new H4("Prix unitaire");
        priceTitle.getStyle()
                .set("margin", "0 0 8px 0")
                .set("color", "#4a5568")
                .set("font-size", "0.9rem");

        H3 priceValue = new H3(priceText);
        priceValue.getStyle()
                .set("margin", "0")
                .set("color", event.getPrixUnitaire() != null && event.getPrixUnitaire() > 0 ? "#667eea" : "#38a169");

        priceDisplay.add(priceTitle, priceValue);

        // Total dynamique avec animation
        Div totalContainer = new Div();
        totalContainer.getStyle()
                .set("grid-column", "span 2")
                .set("background", "#f0f9ff")
                .set("border-radius", "12px")
                .set("padding", "20px")
                .set("margin-top", "10px")
                .set("border", "1px solid #bee3f8")
                .set("transition", "all 0.3s ease");

        Div totalHeader = new Div();
        totalHeader.getStyle()
                .set("display", "flex")
                .set("justify-content", "space-between")
                .set("align-items", "center")
                .set("margin-bottom", "8px");

        H4 totalLabel = new H4("Total à payer");
        totalLabel.getStyle()
                .set("margin", "0")
                .set("color", "#2b6cb0");

        H2 totalValue = new H2("Total: " + calculateTotal(1) + " MAD");
        totalValue.getStyle()
                .set("margin", "0")
                .set("color", "#2b6cb0")
                .set("font-weight", "700");

        totalHeader.add(totalLabel, totalValue);

        // Détail du calcul
        Div calculationDetail = new Div();
        calculationDetail.getStyle()
                .set("color", "#718096")
                .set("font-size", "0.9rem")
                .set("margin-top", "8px");

        Span calculationText = new Span("1 place × " +
                (event.getPrixUnitaire() != null ? event.getPrixUnitaire() + " MAD" : "0 MAD"));
        calculationDetail.add(calculationText);

        totalContainer.add(totalHeader, calculationDetail);

        // Commentaire
        TextArea comment = new TextArea("Commentaire (optionnel)");
        comment.setWidthFull();
        comment.setMaxLength(500);
        comment.setPlaceholder("Ajoutez un commentaire pour l'organisateur...");
        comment.getStyle()
                .set("--vaadin-input-field-border-radius", "8px")
                .set("--vaadin-input-field-border-color", "#e2e8f0");

        // Mettre à jour le total quand le nombre de places change
        numberOfPlaces.addValueChangeListener(e -> {
            int places = e.getValue() != null ? e.getValue() : 1;
            double total = calculateTotal(places);

            // Animation du changement de total
            totalValue.getElement().executeJs("""
                const element = this;
                element.style.transform = 'scale(1.05)';
                element.style.color = '#48bb78';
                setTimeout(() => {
                    element.style.transform = 'scale(1)';
                    element.style.color = '#2b6cb0';
                }, 300);
            """);

            if (event.getPrixUnitaire() != null && event.getPrixUnitaire() > 0) {
                totalValue.setText("Total: " + total + " MAD");
                calculationText.setText(places + " place" + (places > 1 ? "s" : "") +
                        " × " + event.getPrixUnitaire() + " MAD");
            } else {
                totalValue.setText("GRATUIT");
                calculationText.setText(places + " place" + (places > 1 ? "s" : "") + " gratuite" + (places > 1 ? "s" : ""));
            }
        });

        form.add(numberOfPlaces, 1);
        form.add(priceDisplay, 1);
        form.add(totalContainer);
        form.add(comment, 2);

        // Informations utilisateur
        Div userCard = new Div();
        userCard.getStyle()
                .set("background", "#f8fafc")
                .set("border-radius", "12px")
                .set("padding", "20px")
                .set("margin-top", "20px")
                .set("border", "1px solid #e2e8f0");

        VerticalLayout userInfo = new VerticalLayout();
        userInfo.setSpacing(true);
        userInfo.setPadding(false);

        H4 userTitle = new H4("Informations personnelles");
        userTitle.getStyle()
                .set("margin", "0 0 16px 0")
                .set("color", "#2d3748");

        HorizontalLayout userDetails = new HorizontalLayout();
        userDetails.setSpacing(true);
        userDetails.setAlignItems(Alignment.CENTER);

        // Avatar utilisateur
        Div avatar = new Div();
        avatar.getStyle()
                .set("width", "48px")
                .set("height", "48px")
                .set("border-radius", "50%")
                .set("background", "linear-gradient(135deg, #667eea 0%, #764ba2 100%)")
                .set("display", "flex")
                .set("align-items", "center")
                .set("justify-content", "center")
                .set("color", "white")
                .set("font-weight", "bold")
                .set("font-size", "1.2rem");

        String initials = (currentUser.getPrenom().charAt(0) + "" + currentUser.getNom().charAt(0)).toUpperCase();
        avatar.setText(initials);

        // Détails utilisateur
        VerticalLayout userText = new VerticalLayout();
        userText.setSpacing(false);
        userText.setPadding(false);

        Span userName = new Span(currentUser.getPrenom() + " " + currentUser.getNom());
        userName.getStyle()
                .set("font-weight", "600")
                .set("color", "#2d3748");

        Span userEmail = new Span(currentUser.getEmail());
        userEmail.getStyle()
                .set("color", "#718096")
                .set("font-size", "0.9rem");

        userText.add(userName, userEmail);
        userDetails.add(avatar, userText);
        userInfo.add(userTitle, userDetails);
        userCard.add(userInfo);

        // Boutons d'action
        HorizontalLayout buttons = new HorizontalLayout();
        buttons.setWidthFull();
        buttons.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
        buttons.setSpacing(true);
        buttons.getStyle()
                .set("margin-top", "30px")
                .set("padding-top", "24px")
                .set("border-top", "1px solid #e9ecef");

        Button cancelButton = new Button("Annuler", new Icon(VaadinIcon.CLOSE));
        cancelButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        cancelButton.addClickListener(e -> reservationDialog.close());

        Button confirmButton = new Button("Confirmer la réservation", new Icon(VaadinIcon.CHECK));
        confirmButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SUCCESS);
        confirmButton.getStyle()
                .set("background", "linear-gradient(135deg, #48bb78 0%, #38a169 100%)")
                .set("border", "none")
                .set("border-radius", "12px")
                .set("padding", "12px 32px")
                .set("font-weight", "600")
                .set("letter-spacing", "0.3px")
                .set("box-shadow", "0 8px 20px rgba(56, 161, 105, 0.3)")
                .set("transition", "all 0.3s ease");

        // Hover effect
        confirmButton.getElement().addEventListener("mouseenter", e -> {
            confirmButton.getStyle()
                    .set("transform", "translateY(-2px)")
                    .set("box-shadow", "0 12px 24px rgba(56, 161, 105, 0.4)");
        });
        confirmButton.getElement().addEventListener("mouseleave", e -> {
            confirmButton.getStyle()
                    .set("transform", "translateY(0)")
                    .set("box-shadow", "0 8px 20px rgba(56, 161, 105, 0.3)");
        });

        confirmButton.addClickListener(e -> {
            // Validation
            if (numberOfPlaces.getValue() == null || numberOfPlaces.getValue() < 1) {
                Notification.show("Veuillez sélectionner un nombre de places valide", 3000, Notification.Position.MIDDLE);
                return;
            }

            if (numberOfPlaces.getValue() > availablePlaces) {
                Notification.show("Pas assez de places disponibles", 3000, Notification.Position.MIDDLE);
                return;
            }

            try {
                // Créer la réservation
                Reservation reservation = reservationService.createReservation(
                        event.getId(),
                        currentUser.getId(),
                        numberOfPlaces.getValue(),
                        comment.getValue()
                );

                // Confirmer la réservation
                reservationService.confirmReservation(reservation.getId());

                // Afficher le dialog de succès
                reservationDialog.close();
                showReservationSuccessDialog(reservation);

                // Mettre à jour l'affichage de la carte
                refreshCardAfterReservation();

            } catch (Exception ex) {
                Notification.show(
                        "Erreur lors de la réservation: " + ex.getMessage(),
                        5000,
                        Notification.Position.MIDDLE
                );
            }
        });

        buttons.add(cancelButton, confirmButton);

        // Assembler le contenu
        formCard.add(formTitle, form);
        content.add(eventCard, formCard, userCard, buttons);

        // Dialog layout
        VerticalLayout dialogContent = new VerticalLayout();
        dialogContent.setSpacing(false);
        dialogContent.setPadding(false);
        dialogContent.add(header, content);

        reservationDialog.add(dialogContent);
        reservationDialog.open();
    }

    private Div createReservationDetailItem(VaadinIcon vaadinIcon, String label, String value, String color) {
        Div item = new Div();
        item.getStyle()
                .set("display", "flex")
                .set("align-items", "center")
                .set("gap", "12px");

        // Icon circle
        Div iconCircle = new Div();
        iconCircle.getStyle()
                .set("width", "40px")
                .set("height", "40px")
                .set("border-radius", "50%")
                .set("background", color + "20")
                .set("display", "flex")
                .set("align-items", "center")
                .set("justify-content", "center")
                .set("flex-shrink", "0");

        Icon itemIcon = new Icon(vaadinIcon);
        itemIcon.setSize("20px");
        itemIcon.setColor(color);
        iconCircle.add(itemIcon);

        // Text content
        VerticalLayout textContent = new VerticalLayout();
        textContent.setSpacing(false);
        textContent.setPadding(false);

        Span itemLabel = new Span(label);
        itemLabel.getStyle()
                .set("font-size", "0.8rem")
                .set("color", "#718096")
                .set("font-weight", "500");

        Span itemValue = new Span(value);
        itemValue.getStyle()
                .set("font-size", "1rem")
                .set("color", "#2d3748")
                .set("font-weight", "600");

        textContent.add(itemLabel, itemValue);
        item.add(iconCircle, textContent);
        return item;
    }

    private void refreshCardAfterReservation() {
        // Animation de confirmation
        getStyle().set("border", "2px solid #38a169");

        // Mettre à jour le bouton
        if (reserveButton != null) {
            reserveButton.setEnabled(false);
            reserveButton.setText("✓ Réservé");
            reserveButton.removeThemeVariants(ButtonVariant.LUMO_PRIMARY);
            reserveButton.addThemeVariants(ButtonVariant.LUMO_SUCCESS);
            reserveButton.getStyle()
                    .set("background", "#38a169")
                    .set("box-shadow", "0 4px 12px rgba(56, 161, 105, 0.3)");
        }
    }

    private double calculateTotal(int places) {
        if (event.getPrixUnitaire() == null || event.getPrixUnitaire() <= 0) {
            return 0;
        }
        return event.getPrixUnitaire() * places;
    }

    private void showReservationSuccessDialog(Reservation reservation) {
        Dialog successDialog = new Dialog();
        successDialog.setCloseOnOutsideClick(true);
        successDialog.setWidth("500px");
        successDialog.setMaxWidth("90vw");
        successDialog.setCloseOnEsc(true);
        successDialog.addClassName("success-dialog");

        VerticalLayout content = new VerticalLayout();
        content.setSpacing(true);
        content.setPadding(true);
        content.setAlignItems(Alignment.CENTER);
        content.getStyle()
                .set("text-align", "center")
                .set("background", "white");

        // Animation de succès
        Div successAnimation = new Div();
        successAnimation.getStyle()
                .set("width", "80px")
                .set("height", "80px")
                .set("border-radius", "50%")
                .set("background", "linear-gradient(135deg, #48bb78 0%, #38a169 100%)")
                .set("display", "flex")
                .set("align-items", "center")
                .set("justify-content", "center")
                .set("margin", "0 auto 20px")
                .set("position", "relative");

        Icon checkIcon = new Icon(VaadinIcon.CHECK);
        checkIcon.setSize("40px");
        checkIcon.setColor("white");
        successAnimation.add(checkIcon);

        // Cercle d'animation
        Div pulseCircle = new Div();
        pulseCircle.getStyle()
                .set("position", "absolute")
                .set("top", "0")
                .set("left", "0")
                .set("right", "0")
                .set("bottom", "0")
                .set("border-radius", "50%")
                .set("border", "2px solid #48bb78")
                .set("animation", "pulse 2s infinite");

        successAnimation.add(pulseCircle);

        // Titre
        H3 title = new H3("Réservation confirmée!");
        title.getStyle()
                .set("color", "#2d3748")
                .set("margin", "0")
                .set("font-size", "1.75rem");

        // Message
        Paragraph message = new Paragraph(
                "Votre réservation a été enregistrée avec succès. " +
                        "Vous recevrez un email de confirmation avec tous les détails."
        );
        message.getStyle()
                .set("text-align", "center")
                .set("color", "#718096")
                .set("margin", "20px 0")
                .set("font-size", "1rem");

        // Détails de la réservation dans une carte
        Div detailsCard = new Div();
        detailsCard.getStyle()
                .set("background", "#f8fafc")
                .set("border-radius", "12px")
                .set("padding", "24px")
                .set("width", "100%")
                .set("border", "1px solid #e2e8f0");

        VerticalLayout detailsContent = new VerticalLayout();
        detailsContent.setSpacing(true);
        detailsContent.setPadding(false);

        // Code de réservation en gros
        Div codeContainer = new Div();
        codeContainer.getStyle()
                .set("background", "white")
                .set("border-radius", "8px")
                .set("padding", "16px")
                .set("margin-bottom", "16px")
                .set("border", "2px dashed #cbd5e0");

        H4 codeLabel = new H4("Votre code de réservation");
        codeLabel.getStyle()
                .set("margin", "0 0 8px 0")
                .set("color", "#4a5568")
                .set("font-size", "1rem");

        H2 codeValue = new H2(reservation.getCodeReservation());
        codeValue.getStyle()
                .set("margin", "0")
                .set("color", "#667eea")
                .set("font-family", "'Courier New', monospace")
                .set("letter-spacing", "2px")
                .set("font-weight", "700");

        codeContainer.add(codeLabel, codeValue);

        // Autres détails
        Div detailsGrid = new Div();
        detailsGrid.getStyle()
                .set("display", "grid")
                .set("grid-template-columns", "1fr 1fr")
                .set("gap", "12px");

        detailsGrid.add(
                createSuccessDetail("Événement", event.getTitre()),
                createSuccessDetail("Date", event.getDateDebut().format(DateTimeFormatter.ofPattern("dd/MM/yy"))),
                createSuccessDetail("Places", reservation.getNombrePlaces().toString()),
                createSuccessDetail("Montant",
                        reservation.getMontantTotal() != null ? reservation.getMontantTotal() + " MAD" : "GRATUIT")
        );

        detailsContent.add(codeContainer, detailsGrid);
        detailsCard.add(detailsContent);

        // Boutons
        HorizontalLayout buttons = new HorizontalLayout();
        buttons.setSpacing(true);
        buttons.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);

        // Bouton pour copier le code
        Button copyButton = new Button("Copier le code", new Icon(VaadinIcon.COPY));
        copyButton.addThemeVariants(ButtonVariant.LUMO_CONTRAST);
        copyButton.getStyle()
                .set("border-radius", "8px")
                .set("padding", "10px 20px");
        copyButton.addClickListener(e -> {
            getElement().executeJs("""
                navigator.clipboard.writeText($0).then(() => {
                    const button = $1;
                    button.textContent = "Copié!";
                    button.style.background = '#38a169';
                    setTimeout(() => {
                        button.textContent = "Copier le code";
                        button.style.background = '';
                    }, 2000);
                });
            """, reservation.getCodeReservation(), copyButton.getElement());
        });

        // Bouton Fermer
        Button closeButton = new Button("Fermer", new Icon(VaadinIcon.CHECK));
        closeButton.addThemeVariants(ButtonVariant.LUMO_SUCCESS);
        closeButton.getStyle()
                .set("border-radius", "8px")
                .set("padding", "10px 30px");
        closeButton.addClickListener(e -> successDialog.close());

        buttons.add(copyButton, closeButton);

        content.add(successAnimation, title, message, detailsCard, buttons);
        successDialog.add(content);

        // Ajouter l'animation CSS
        successDialog.getElement().executeJs("""
            const style = document.createElement('style');
            style.textContent = `
                @keyframes pulse {
                    0% { transform: scale(1); opacity: 1; }
                    50% { transform: scale(1.1); opacity: 0.5; }
                    100% { transform: scale(1); opacity: 1; }
                }
                .success-dialog {
                    animation: dialogAppear 0.3s ease;
                }
                @keyframes dialogAppear {
                    from { opacity: 0; transform: translateY(-20px); }
                    to { opacity: 1; transform: translateY(0); }
                }
            `;
            document.head.appendChild(style);
        """);

        successDialog.open();
    }

    private Div createSuccessDetail(String label, String value) {
        Div detail = new Div();
        detail.getStyle()
                .set("background", "white")
                .set("border-radius", "8px")
                .set("padding", "12px");

        Span detailLabel = new Span(label);
        detailLabel.getStyle()
                .set("display", "block")
                .set("font-size", "0.8rem")
                .set("color", "#718096")
                .set("margin-bottom", "4px");

        Span detailValue = new Span(value);
        detailValue.getStyle()
                .set("display", "block")
                .set("font-size", "1rem")
                .set("color", "#2d3748")
                .set("font-weight", "600");

        detail.add(detailLabel, detailValue);
        return detail;
    }

    // ==================== DIALOG POUR AUTHENTIFICATION REQUISE ====================
    private void showAuthRequiredDialog() {
        Dialog authDialog = new Dialog();
        authDialog.setCloseOnOutsideClick(true);
        authDialog.setWidth("500px");
        authDialog.setCloseOnEsc(true);
        authDialog.addClassName("auth-dialog");

        VerticalLayout content = new VerticalLayout();
        content.setSpacing(true);
        content.setPadding(true);
        content.setAlignItems(Alignment.CENTER);
        content.getStyle()
                .set("text-align", "center")
                .set("background", "white");

        // Icon avec animation
        Div iconContainer = new Div();
        iconContainer.getStyle()
                .set("width", "80px")
                .set("height", "80px")
                .set("border-radius", "50%")
                .set("background", "linear-gradient(135deg, #667eea 0%, #764ba2 100%)")
                .set("display", "flex")
                .set("align-items", "center")
                .set("justify-content", "center")
                .set("margin", "0 auto 20px");

        Icon lockIcon = new Icon(VaadinIcon.LOCK);
        lockIcon.setSize("40px");
        lockIcon.setColor("white");
        iconContainer.add(lockIcon);

        // Titre
        H3 title = new H3("Connexion requise");
        title.getStyle()
                .set("text-align", "center")
                .set("color", "#2d3748")
                .set("margin", "0")
                .set("font-size", "1.75rem");

        // Message
        Paragraph message = new Paragraph(
                "Vous devez être connecté pour réserver cet événement. " +
                        "Connectez-vous à votre compte ou créez-en un nouveau en quelques secondes."
        );
        message.getStyle()
                .set("text-align", "center")
                .set("color", "#718096")
                .set("margin", "20px 0")
                .set("font-size", "1rem");

        // Information sur l'événement
        Div eventCard = new Div();
        eventCard.getStyle()
                .set("background", "#f8fafc")
                .set("border-radius", "12px")
                .set("padding", "20px")
                .set("width", "100%")
                .set("border", "1px solid #e2e8f0")
                .set("margin-bottom", "20px");

        H4 eventTitle = new H4(event.getTitre());
        eventTitle.getStyle()
                .set("margin", "0 0 12px 0")
                .set("color", "#667eea");

        HorizontalLayout eventDetails = new HorizontalLayout();
        eventDetails.setSpacing(true);
        eventDetails.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        eventDetails.setAlignItems(Alignment.CENTER);

        Span eventDate = new Span(formatDate(event.getDateDebut()));
        eventDate.getStyle()
                .set("color", "#718096")
                .set("font-size", "0.9rem");

        Icon dotIcon = new Icon(VaadinIcon.CIRCLE);
        dotIcon.setSize("4px");
        dotIcon.setColor("#cbd5e0");

        Span eventLocation = new Span(event.getVille());
        eventLocation.getStyle()
                .set("color", "#718096")
                .set("font-size", "0.9rem");

        eventDetails.add(eventDate, dotIcon, eventLocation);
        eventCard.add(eventTitle, eventDetails);

        // Boutons d'action
        VerticalLayout buttons = new VerticalLayout();
        buttons.setSpacing(true);
        buttons.setWidth("100%");
        buttons.setAlignItems(Alignment.STRETCH);

        Button loginButton = new Button("Se connecter", new Icon(VaadinIcon.SIGN_IN));
        loginButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        loginButton.getStyle()
                .set("border-radius", "12px")
                .set("padding", "14px")
                .set("font-weight", "600")
                .set("font-size", "1rem");
        loginButton.addClickListener(e -> {
            authDialog.close();
            getUI().ifPresent(ui -> ui.navigate(LoginView.class));
        });

        Button registerButton = new Button("Créer un compte", new Icon(VaadinIcon.USER));
        registerButton.addThemeVariants(ButtonVariant.LUMO_SUCCESS);
        registerButton.getStyle()
                .set("border-radius", "12px")
                .set("padding", "14px")
                .set("font-weight", "600")
                .set("font-size", "1rem");
        registerButton.addClickListener(e -> {
            authDialog.close();
            getUI().ifPresent(ui -> ui.navigate(RegisterView.class));
        });

        Button cancelButton = new Button("Plus tard", new Icon(VaadinIcon.CLOSE));
        cancelButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        cancelButton.addClickListener(e -> authDialog.close());

        buttons.add(loginButton, registerButton, cancelButton);

        content.add(iconContainer, title, message, eventCard, buttons);
        authDialog.add(content);

        authDialog.open();
    }

    private HorizontalLayout createInfoRow(VaadinIcon icon, String text) {
        HorizontalLayout row = new HorizontalLayout();
        row.setSpacing(true);
        row.setAlignItems(Alignment.CENTER);
        row.setPadding(false);

        Icon rowIcon = new Icon(icon);
        rowIcon.setSize("16px");
        rowIcon.setColor("var(--lumo-contrast-60pct)");

        Span rowText = new Span(text);
        rowText.addClassNames(
                LumoUtility.FontSize.SMALL,
                LumoUtility.TextColor.SECONDARY
        );

        row.add(rowIcon, rowText);
        return row;
    }

    private String getEventImageUrl(Event event) {
        if (event == null) {
            return null;
        }

        String imageUrl = event.getImageUrl();

        if (imageUrl == null || imageUrl.isEmpty() || imageUrl.trim().isEmpty()) {
            return getDefaultImageByCategory(event.getCategorie());
        }

        return imageUrl;
    }

    private Icon getCategoryIcon(EventCategory category) {
        return switch (category) {
            case CONCERT -> new Icon(VaadinIcon.MUSIC);
            case THEATRE -> new Icon(VaadinIcon.CALENDAR);
            case CONFERENCE -> new Icon(VaadinIcon.COMMENT);
            case SPORT -> new Icon(VaadinIcon.DIPLOMA);
            case AUTRE -> new Icon(VaadinIcon.STAR);
        };
    }

    private String getDefaultImageByCategory(EventCategory category) {
        return switch (category) {
            case CONCERT -> "https://images.unsplash.com/photo-1493225457124-a3eb161ffa5f?w=400&h=200&fit=crop&auto=format";
            case THEATRE -> "https://images.unsplash.com/photo-1514306191717-452ec28c7814?w=400&h=200&fit=crop&auto=format";
            case CONFERENCE -> "https://images.unsplash.com/photo-1540575467063-178a50c2df87?w=400&h=200&fit=crop&auto=format";
            case SPORT -> "https://images.unsplash.com/photo-1461896836934-ffe607ba8211?w=400&h=200&fit=crop&auto=format";
            case AUTRE -> "https://images.unsplash.com/photo-1505373877841-8d25f7d46678?w=400&h=200&fit=crop&auto=format";
        };
    }

    private String getCategoryGradient(EventCategory category) {
        return switch (category) {
            case CONCERT -> "linear-gradient(135deg, #f56565 0%, #ed64a6 100%)";
            case THEATRE -> "linear-gradient(135deg, #4299e1 0%, #667eea 100%)";
            case SPORT -> "linear-gradient(135deg, #48bb78 0%, #38a169 100%)";
            case CONFERENCE -> "linear-gradient(135deg, #ed8936 0%, #dd6b20 100%)";
            case AUTRE -> "linear-gradient(135deg, #9f7aea 0%, #805ad5 100%)";
        };
    }

    private String getCategoryLabel(EventCategory category) {
        return switch (category) {
            case CONCERT -> "Concert";
            case THEATRE -> "Théâtre";
            case CONFERENCE -> "Conférence";
            case SPORT -> "Sport";
            case AUTRE -> "Autre";
        };
    }

    private String getCategoryColor(EventCategory category) {
        return switch (category) {
            case CONCERT -> "#e53e3e";
            case THEATRE -> "#3182ce";
            case SPORT -> "#38a169";
            case CONFERENCE -> "#dd6b20";
            case AUTRE -> "#805ad5";
        };
    }

    private String formatDate(LocalDateTime date) {
        return date.format(DateTimeFormatter.ofPattern("dd MMM"));
    }

    private String formatDateTime(LocalDateTime date) {
        return date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy • HH:mm"));
    }

    // ==================== GETTERS ====================
    public Event getEvent() {
        return event;
    }

    public Button getReserveButton() {
        return reserveButton;
    }

    public Button getDetailsButton() {
        return detailsButton;
    }

    public Div getImageContainer() {
        return imageContainer;
    }

    // ==================== SETTERS ====================
    public void setEventService(EventService eventService) {
        this.eventService = eventService;
    }

    public void setReservationService(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    // Méthode pour rafraîchir l'image si elle change
    public void refreshImage() {
        imageContainer.removeAll();
        createImageContent(imageContainer);
    }
}