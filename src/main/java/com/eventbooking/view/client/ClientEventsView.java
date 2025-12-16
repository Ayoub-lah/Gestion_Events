package com.eventbooking.view.client;

import com.eventbooking.entity.Event;
import com.eventbooking.entity.User;
import com.eventbooking.service.EventService;
import com.eventbooking.service.ReservationService;
import com.eventbooking.service.UserService;
import com.eventbooking.view.components.EventCard;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.util.List;

@Route("client/events")
@PageTitle("Événements - Client")
public class ClientEventsView extends VerticalLayout {

    private final EventService eventService;
    private final ReservationService reservationService;
    private final UserService userService;
    private final User currentUser;

    private List<Event> allEvents;
    private Div eventsContainer;
    private TextField searchField;
    private Select<String> categoryFilter;
    private Select<String> cityFilter;
    private DatePicker dateFromFilter;
    private DatePicker dateToFilter;
    private Checkbox freeEventsOnly;

    @Autowired
    public ClientEventsView(EventService eventService,
                            ReservationService reservationService,
                            UserService userService) {
        this.eventService = eventService;
        this.reservationService = reservationService;
        this.userService = userService;
        this.currentUser = (User) VaadinSession.getCurrent().getAttribute("currentUser");

        setPadding(false);
        setSpacing(false);
        setWidthFull();
        getStyle().set("background", "#f8f9fa");

        add(
                createHeader(),
                createFiltersSection(),
                createEventsGrid()
        );

        loadEvents();
    }

    private Component createHeader() {
        HorizontalLayout header = new HorizontalLayout();
        header.setWidthFull();
        header.setPadding(true);
        header.setAlignItems(Alignment.CENTER);
        header.setJustifyContentMode(JustifyContentMode.BETWEEN);
        header.getStyle()
                .set("background", "white")
                .set("box-shadow", "0 2px 4px rgba(0,0,0,0.1)")
                .set("margin-bottom", "20px");

        H2 title = new H2("Rechercher des événements");
        title.getStyle()
                .set("margin", "0")
                .set("color", "#333");

        Span subtitle = new Span("Découvrez et réservez des événements passionnants");
        subtitle.getStyle()
                .set("color", "#666")
                .set("font-size", "14px");

        header.add(title, subtitle);
        return header;
    }

    private Component createFiltersSection() {
        VerticalLayout filtersSection = new VerticalLayout();
        filtersSection.setWidthFull();
        filtersSection.setPadding(true);
        filtersSection.getStyle()
                .set("background", "white")
                .set("border-radius", "12px")
                .set("box-shadow", "0 2px 8px rgba(0,0,0,0.1)")
                .set("margin", "0 20px 20px 20px");

        // Recherche principale
        HorizontalLayout mainSearch = new HorizontalLayout();
        mainSearch.setWidthFull();
        mainSearch.setAlignItems(Alignment.END);
        mainSearch.setSpacing(true);

        searchField = new TextField("Rechercher");
        searchField.setPlaceholder("Rechercher un événement...");
        searchField.setPrefixComponent(VaadinIcon.SEARCH.create());
        searchField.setWidth("400px");
        searchField.addValueChangeListener(e -> filterEvents());

        Button searchBtn = new Button("Rechercher", VaadinIcon.SEARCH.create());
        searchBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        searchBtn.addClickListener(e -> filterEvents());

        mainSearch.add(searchField, searchBtn);
        mainSearch.setFlexGrow(1, searchField);

        // Filtres avancés
        HorizontalLayout advancedFilters = new HorizontalLayout();
        advancedFilters.setWidthFull();
        advancedFilters.setSpacing(true);

        categoryFilter = new Select<>();
        categoryFilter.setLabel("Catégorie");
        categoryFilter.setItems("Toutes", "CONCERT", "THEATRE", "CONFERENCE", "SPORT", "AUTRE");
        categoryFilter.setValue("Toutes");
        categoryFilter.addValueChangeListener(e -> filterEvents());

        cityFilter = new Select<>();
        cityFilter.setLabel("Ville");
        cityFilter.setItems("Toutes", "Casablanca", "Rabat", "Marrakech", "Fès", "Tanger");
        cityFilter.setValue("Toutes");
        cityFilter.addValueChangeListener(e -> filterEvents());

        dateFromFilter = new DatePicker("Date début");
        dateFromFilter.addValueChangeListener(e -> filterEvents());

        dateToFilter = new DatePicker("Date fin");
        dateToFilter.addValueChangeListener(e -> filterEvents());

        freeEventsOnly = new Checkbox("Événements gratuits uniquement");
        freeEventsOnly.addValueChangeListener(e -> filterEvents());

        advancedFilters.add(categoryFilter, cityFilter, dateFromFilter, dateToFilter, freeEventsOnly);

        // Boutons de contrôle
        HorizontalLayout controlButtons = new HorizontalLayout();
        controlButtons.setSpacing(true);

        Button resetBtn = new Button("Réinitialiser", VaadinIcon.REFRESH.create());
        resetBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        resetBtn.addClickListener(e -> resetFilters());

        Button saveSearchBtn = new Button("Sauvegarder recherche", VaadinIcon.BOOKMARK.create());
        saveSearchBtn.addThemeVariants(ButtonVariant.LUMO_CONTRAST);
        saveSearchBtn.addClickListener(e -> saveSearchCriteria());

        controlButtons.add(resetBtn, saveSearchBtn);

        filtersSection.add(mainSearch, advancedFilters, controlButtons);
        return filtersSection;
    }

    private Component createEventsGrid() {
        VerticalLayout eventsSection = new VerticalLayout();
        eventsSection.setWidthFull();
        eventsSection.setPadding(true);
        eventsSection.getStyle()
                .set("background", "white")
                .set("border-radius", "12px")
                .set("box-shadow", "0 2px 8px rgba(0,0,0,0.1)")
                .set("margin", "0 20px");

        // En-tête avec compteur
        HorizontalLayout header = new HorizontalLayout();
        header.setWidthFull();
        header.setJustifyContentMode(JustifyContentMode.BETWEEN);
        header.setAlignItems(Alignment.CENTER);

        H3 sectionTitle = new H3("Événements disponibles");
        sectionTitle.getStyle().set("margin", "0");

        Span countSpan = new Span("0 événements");
        countSpan.setId("events-count");
        countSpan.getStyle().set("color", "#666");

        header.add(sectionTitle, countSpan);

        // Conteneur des événements
        eventsContainer = new Div();
        eventsContainer.getStyle()
                .set("display", "grid")
                .set("grid-template-columns", "repeat(auto-fill, minmax(320px, 1fr))")
                .set("gap", "30px")
                .set("margin-top", "20px");

        eventsSection.add(header, eventsContainer);
        return eventsSection;
    }

    private void loadEvents() {
        allEvents = eventService.getPublishedEvents();
        displayEvents(allEvents);
    }

    private void filterEvents() {
        String searchTerm = searchField.getValue().toLowerCase();
        String category = categoryFilter.getValue();
        String city = cityFilter.getValue();
        LocalDate dateFrom = dateFromFilter.getValue();
        LocalDate dateTo = dateToFilter.getValue();
        boolean freeOnly = freeEventsOnly.getValue();

        List<Event> filtered = allEvents.stream()
                .filter(e -> searchTerm.isEmpty() || e.getTitre().toLowerCase().contains(searchTerm))
                .filter(e -> category.equals("Toutes") || e.getCategorie().name().equals(category))
                .filter(e -> city.equals("Toutes") || e.getVille().equalsIgnoreCase(city))
                .filter(e -> dateFrom == null || !e.getDateDebut().toLocalDate().isBefore(dateFrom))
                .filter(e -> dateTo == null || !e.getDateDebut().toLocalDate().isAfter(dateTo))
                .filter(e -> !freeOnly || (e.getPrixUnitaire() == null || e.getPrixUnitaire() <= 0))
                .toList();

        displayEvents(filtered);
    }

    private void displayEvents(List<Event> events) {
        eventsContainer.removeAll();

        if (events.isEmpty()) {
            Div emptyState = new Div();
            emptyState.getStyle()
                    .set("text-align", "center")
                    .set("padding", "40px")
                    .set("grid-column", "1 / -1");

            Icon icon = VaadinIcon.SEARCH.create();
            icon.setSize("48px");
            icon.getStyle().set("color", "#cbd5e0").set("margin-bottom", "20px");

            H3 message = new H3("Aucun événement trouvé");
            message.getStyle()
                    .set("color", "#4a5568")
                    .set("margin", "0 0 10px 0");

            Span suggestion = new Span("Essayez de modifier vos critères de recherche");
            suggestion.getStyle().set("color", "#718096");

            emptyState.add(icon, message, suggestion);
            eventsContainer.add(emptyState);
        } else {
            events.forEach(event -> {
                EventCard eventCard = new EventCard(event, "http://localhost:8080",
                        eventService, reservationService, userService);
                eventsContainer.add(eventCard);
            });
        }

        // Mettre à jour le compteur
        getElement().executeJs("""
            const countSpan = document.getElementById('events-count');
            if (countSpan) {
                countSpan.textContent = $0 + ' événement(s)';
            }
        """, events.size());
    }

    private void resetFilters() {
        searchField.clear();
        categoryFilter.setValue("Toutes");
        cityFilter.setValue("Toutes");
        dateFromFilter.clear();
        dateToFilter.clear();
        freeEventsOnly.setValue(false);
        loadEvents();
    }

    private void saveSearchCriteria() {
        // Implémenter la sauvegarde des critères de recherche
        com.vaadin.flow.component.notification.Notification.show(
                "Recherche sauvegardée dans vos favoris",
                3000,
                com.vaadin.flow.component.notification.Notification.Position.TOP_END
        );
    }
}