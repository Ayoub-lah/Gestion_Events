package com.eventbooking.view.publicview;

import com.eventbooking.entity.Event;
import com.eventbooking.service.EventService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.*;

import java.time.format.DateTimeFormatter;

@Route("event/:id")
@PageTitle("Détails de l'événement - EventBooking")
public class EventDetailView extends VerticalLayout implements HasUrlParameter<String> {

    private final EventService eventService;
    private Event event;

    public EventDetailView(EventService eventService) {
        this.eventService = eventService;
        setPadding(false);
        setSpacing(false);
        setWidthFull();
    }

    @Override
    public void setParameter(BeforeEvent event, String parameter) {
        try {
            Long eventId = Long.parseLong(parameter);
            this.event = eventService.getEventById(eventId).orElse(null);

            if (this.event == null) {
                showNotFound();
            } else {
                showEventDetails();
            }
        } catch (NumberFormatException e) {
            showNotFound();
        }
    }

    private void showNotFound() {
        removeAll();
        add(new H1("Événement non trouvé"));
    }

    private void showEventDetails() {
        removeAll();

        // Header avec image
        Div header = new Div();
        header.getStyle()
                .set("width", "100%")
                .set("height", "400px")
                .set("position", "relative")
                .set("background-image", getEventImageUrl(event))
                .set("background-size", "cover")
                .set("background-position", "center");

        // Overlay sombre
        Div overlay = new Div();
        overlay.getStyle()
                .set("position", "absolute")
                .set("top", "0")
                .set("left", "0")
                .set("width", "100%")
                .set("height", "100%")
                .set("background", "linear-gradient(to bottom, rgba(0,0,0,0.3), rgba(0,0,0,0.7))");

        // Contenu de l'en-tête
        VerticalLayout headerContent = new VerticalLayout();
        headerContent.getStyle()
                .set("position", "absolute")
                .set("bottom", "40px")
                .set("left", "40px")
                .set("color", "white");

        H1 title = new H1(event.getTitre());
        title.getStyle()
                .set("color", "white")
                .set("font-size", "48px")
                .set("margin-bottom", "10px")
                .set("text-shadow", "2px 2px 4px rgba(0,0,0,0.5)");

        Paragraph date = new Paragraph(
                event.getDateDebut().format(DateTimeFormatter.ofPattern("EEEE dd MMMM yyyy 'à' HH:mm"))
        );
        date.getStyle()
                .set("color", "rgba(255,255,255,0.9)")
                .set("font-size", "20px");

        headerContent.add(title, date);
        header.add(overlay, headerContent);

        // Contenu principal
        VerticalLayout mainContent = new VerticalLayout();
        mainContent.setWidth("800px");
        mainContent.setPadding(true);
        mainContent.getStyle().set("margin", "40px auto");

        // Description
        if (event.getDescription() != null) {
            Div descriptionSection = new Div();
            descriptionSection.getStyle()
                    .set("background", "white")
                    .set("padding", "30px")
                    .set("border-radius", "12px")
                    .set("box-shadow", "0 4px 12px rgba(0,0,0,0.1)")
                    .set("margin-bottom", "30px");

            H2 descTitle = new H2("Description");
            descTitle.getStyle().set("margin-bottom", "20px");

            Paragraph description = new Paragraph(event.getDescription());
            description.getStyle()
                    .set("font-size", "16px")
                    .set("line-height", "1.6")
                    .set("color", "#555");

            descriptionSection.add(descTitle, description);
            mainContent.add(descriptionSection);
        }

        // Informations détaillées
        Div infoSection = new Div();
        infoSection.getStyle()
                .set("background", "white")
                .set("padding", "30px")
                .set("border-radius", "12px")
                .set("box-shadow", "0 4px 12px rgba(0,0,0,0.1)");

        H2 infoTitle = new H2("Informations pratiques");
        infoTitle.getStyle().set("margin-bottom", "20px");

        VerticalLayout infoList = new VerticalLayout();
        infoList.setSpacing(true);

        // Lieu
        HorizontalLayout locationInfo = new HorizontalLayout();
        locationInfo.setAlignItems(Alignment.CENTER);
        locationInfo.setSpacing(true);

        Icon locationIcon = VaadinIcon.MAP_MARKER.create();
        locationIcon.setColor("#667eea");

        Div locationText = new Div();
        locationText.setText(event.getLieu() + ", " + event.getVille());
        locationText.getStyle().set("font-size", "16px");

        locationInfo.add(locationIcon, locationText);

        // Date et heure
        HorizontalLayout timeInfo = new HorizontalLayout();
        timeInfo.setAlignItems(Alignment.CENTER);
        timeInfo.setSpacing(true);

        Icon timeIcon = VaadinIcon.CLOCK.create();
        timeIcon.setColor("#667eea");

        Div timeText = new Div();
        timeText.setText(event.getDateDebut().format(DateTimeFormatter.ofPattern("dd/MM/yyyy à HH:mm")));
        timeText.getStyle().set("font-size", "16px");

        timeInfo.add(timeIcon, timeText);

        // Prix
        HorizontalLayout priceInfo = new HorizontalLayout();
        priceInfo.setAlignItems(Alignment.CENTER);
        priceInfo.setSpacing(true);

        Icon priceIcon = VaadinIcon.MONEY.create();
        priceIcon.setColor("#667eea");

        Div priceText = new Div();
        priceText.setText(event.getPrixUnitaire() > 0 ?
                event.getPrixUnitaire() + " DH" : "Gratuit");
        priceText.getStyle().set("font-size", "16px").set("font-weight", "bold");

        priceInfo.add(priceIcon, priceText);

        infoList.add(locationInfo, timeInfo, priceInfo);
        infoSection.add(infoTitle, infoList);
        mainContent.add(infoSection);

        // Bouton de réservation
        Button reserveButton = new Button("Réserver maintenant", VaadinIcon.TICKET.create());
        reserveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_LARGE);
        reserveButton.getStyle()
                .set("margin", "40px auto")
                .set("display", "block")
                .set("background", "linear-gradient(135deg, #667eea 0%, #764ba2 100%)")
                .set("padding", "15px 40px");

        reserveButton.addClickListener(e -> {
            // Navigation vers la réservation
            getUI().ifPresent(ui -> ui.navigate("client/reservation/" + event.getId()));
        });

        add(header, mainContent, reserveButton);
    }

    private String getEventImageUrl(Event event) {
        if (event.getImageUrl() == null || event.getImageUrl().isEmpty()) {
            return "url('https://images.unsplash.com/photo-1501281668745-f6f2610a4ab0?w=1200&h=400&fit=crop&auto=format')";
        }

        String imageUrl = event.getImageUrl();
        if (!imageUrl.startsWith("http") && !imageUrl.startsWith("/uploads/")) {
            imageUrl = "/uploads/" + imageUrl;
        }

        return "url('" + imageUrl + "')";
    }
}