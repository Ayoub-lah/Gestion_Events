package com.eventbooking.view.publicview;

import com.eventbooking.entity.Event;
import com.eventbooking.entity.User;
import com.eventbooking.entity.enums.EventCategory;
import com.eventbooking.service.EventService;
import com.eventbooking.service.FileStorageService;
import com.eventbooking.service.ReservationService;
import com.eventbooking.service.UserService;
import com.eventbooking.view.components.EventCard;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.theme.lumo.LumoUtility;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.List;

@Route("all-events")
@PageTitle("Tous les Événements - EventBooking")
public class AllEventsView extends VerticalLayout {

    private final EventService eventService;
    private final FileStorageService fileStorageService;
    private final ReservationService reservationService;
    private final UserService userService;

    private VerticalLayout eventsContainer;
    private List<Event> allEvents;
    private TextField searchField;
    private Select<String> categoryFilter;
    private Select<String> sortFilter;
    private Div resultsCount;

    @Autowired
    public AllEventsView(EventService eventService,
                         FileStorageService fileStorageService,
                         ReservationService reservationService,
                         UserService userService) {
        this.eventService = eventService;
        this.fileStorageService = fileStorageService;
        this.reservationService = reservationService;
        this.userService = userService;

        addClassNames("all-events-view");
        setPadding(false);
        setSpacing(false);
        setWidthFull();
        getStyle()
                .set("background", "#f8fafc")
                .set("min-height", "100vh");

        // Injecter le CSS
        injectCustomCSS();

        add(
                createNavbar(),
                createHeaderSection(),
                createFiltersSection(),
                createEventsSection()
        );

        loadAllEvents();
    }

    private void injectCustomCSS() {
        UI.getCurrent().getPage().addStyleSheet("https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css");

        // Ajoutez ceci dans injectCustomCSS(), dans la section du style
        UI.getCurrent().getPage().executeJs("""
    const style = document.createElement('style');
    style.textContent = `
        /* ... vos styles existants ... */
        
        /* Styles ajoutés pour le centrage */
        .events-container {
            width: 100%;
            display: flex;
            justify-content: center;
        }
        
        .events-grid {
            display: grid;
            grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
            gap: 32px;
            padding: 40px 20px;
            max-width: 1200px;
            margin: 0 auto !important; /* Force le centrage */
            width: 100%;
        }
        
        .empty-state-container {
            display: flex !important;
            align-items: center !important;
            justify-content: center !important;
            min-height: 50vh !important;
            width: 100% !important;
        }
        
        .empty-state {
            text-align: center !important;
            padding: 80px 20px !important;
            max-width: 500px !important;
            margin: 0 auto !important;
            display: flex !important;
            flex-direction: column !important;
            align-items: center !important;
        }
        
        .empty-state h3 {
            color: #475569 !important;
            margin: 24px 0 12px 0 !important;
            text-align: center !important;
        }
        
        .empty-state p {
            color: #64748b !important;
            margin: 0 !important;
            text-align: center !important;
        }
    `;
    document.head.appendChild(style);
""");

        UI.getCurrent().getPage().executeJs("""
            const style = document.createElement('style');
            style.textContent = `
                .all-events-view {
                    font-family: 'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
                }
                
                .page-header {
                    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
                    color: white;
                    padding: 60px 20px;
                    text-align: center;
                }
                
                .page-title {
                    font-size: clamp(32px, 5vw, 48px);
                    font-weight: 800;
                    margin-bottom: 16px;
                    text-shadow: 0 2px 4px rgba(0,0,0,0.1);
                }
                
                .page-subtitle {
                    font-size: 18px;
                    opacity: 0.9;
                    max-width: 600px;
                    margin: 0 auto;
                }
                
                .filters-section {
                    background: white;
                    border-bottom: 1px solid #e2e8f0;
                    padding: 24px 20px;
                    position: sticky;
                    top: 0;
                    z-index: 100;
                    box-shadow: 0 2px 8px rgba(0,0,0,0.05);
                }
                
                .filters-card {
                    max-width: 1200px;
                    margin: 0 auto;
                }
                
                .results-count {
                    font-size: 14px;
                    color: #64748b;
                    font-weight: 500;
                    display: flex;
                    align-items: center;
                    gap: 8px;
                }
                
                .events-grid {
                    display: grid;
                    grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
                    gap: 32px;
                    padding: 40px 20px;
                    max-width: 1200px;
                    margin: 0 auto;
                    width: 100%;
                }
                
                .event-card {
                    transition: all 0.3s ease;
                    animation: fadeIn 0.6s ease-out;
                }
                
                .event-card:hover {
                    transform: translateY(-8px);
                    box-shadow: 0 20px 40px rgba(0,0,0,0.12) !important;
                }
                
                .empty-state {
                    text-align: center;
                    padding: 80px 20px;
                    max-width: 500px;
                    margin: 0 auto;
                }
                
                .empty-state h3 {
                    color: #475569;
                    margin: 24px 0 12px 0;
                }
                
                .empty-state p {
                    color: #64748b;
                    margin: 0;
                }
                
                .category-chip {
                    display: inline-flex;
                    align-items: center;
                    padding: 4px 12px;
                    background: rgba(99, 102, 241, 0.1);
                    border-radius: 20px;
                    font-size: 12px;
                    font-weight: 500;
                    color: #6366f1;
                    cursor: pointer;
                    transition: all 0.2s;
                }
                
                .category-chip:hover {
                    background: rgba(99, 102, 241, 0.2);
                    transform: scale(1.05);
                }
                
                .category-chip.active {
                    background: #6366f1;
                    color: white;
                }
                
                @keyframes fadeIn {
                    from { opacity: 0; transform: translateY(20px); }
                    to { opacity: 1; transform: translateY(0); }
                }
                
                @media (max-width: 768px) {
                    .events-grid {
                        grid-template-columns: 1fr;
                        padding: 20px;
                    }
                    
                    .page-header {
                        padding: 40px 20px;
                    }
                }
            `;
            document.head.appendChild(style);
        """);
    }

    private Component createNavbar() {
        HorizontalLayout navbar = new HorizontalLayout();
        navbar.addClassNames("navbar");
        navbar.setWidthFull();
        navbar.setPadding(true);
        navbar.setSpacing(false);
        navbar.setAlignItems(FlexComponent.Alignment.CENTER);
        navbar.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        navbar.getStyle()
                .set("background", "white")
                .set("border-bottom", "1px solid #e2e8f0")
                .set("position", "sticky")
                .set("top", "0")
                .set("z-index", "1000");

        // Logo section
        HorizontalLayout logoSection = new HorizontalLayout();
        logoSection.setAlignItems(FlexComponent.Alignment.CENTER);
        logoSection.getStyle().set("cursor", "pointer");

        Div logoContainer = new Div();
        logoContainer.getStyle()
                .set("width", "40px")
                .set("height", "40px")
                .set("background", "linear-gradient(135deg, #6366f1 0%, #8b5cf6 100%)")
                .set("border-radius", "12px")
                .set("display", "flex")
                .set("align-items", "center")
                .set("justify-content", "center")
                .set("box-shadow", "0 4px 12px rgba(99, 102, 241, 0.25)");

        Icon logoIcon = VaadinIcon.CALENDAR_CLOCK.create();
        logoIcon.setSize("20px");
        logoIcon.setColor("white");
        logoContainer.add(logoIcon);

        Div nameContainer = new Div();
        nameContainer.getStyle().set("display", "flex").set("flex-direction", "column");

        Span brandName = new Span("EventBooking");
        brandName.getStyle()
                .set("font-size", "20px")
                .set("font-weight", "700")
                .set("color", "#1e293b");

        Span tagline = new Span("Tous les événements");
        tagline.getStyle()
                .set("font-size", "11px")
                .set("font-weight", "500")
                .set("color", "#64748b")
                .set("letter-spacing", "1px")
                .set("text-transform", "uppercase");

        nameContainer.add(brandName, tagline);
        logoSection.add(logoContainer, nameContainer);

        logoSection.addClickListener(e -> getUI().ifPresent(ui -> ui.navigate("")));

        // Back button
        Button backButton = new Button("Retour à l'accueil", VaadinIcon.ARROW_LEFT.create());
        backButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        backButton.addClickListener(e -> getUI().ifPresent(ui -> ui.navigate("")));

        navbar.add(logoSection, backButton);
        return navbar;
    }

    private Component createHeaderSection() {
        Div header = new Div();
        header.addClassNames("page-header");

        // Prendre toute la largeur disponible
        header.setWidthFull();
        header.getStyle()
                .set("display", "flex")
                .set("align-items", "center")
                .set("justify-content", "center")
                .set("min-height", "350px")
                .set("width", "100%") // Force 100% de largeur
                .set("box-sizing", "border-box");

        // Conteneur qui prend toute la largeur
        Div fullWidthContainer = new Div();
        fullWidthContainer.setWidthFull();
        fullWidthContainer.getStyle()
                .set("display", "flex")
                .set("flex-direction", "column")
                .set("align-items", "center")
                .set("justify-content", "center")
                .set("width", "100%")
                .set("height", "100%");

        H1 title = new H1("ÉVÉNEMENTS");
        title.addClassNames("page-title");
        title.getStyle()
                .set("margin-bottom", "20px")
                .set("text-align", "center")
                .set("width", "100%");

        Paragraph subtitle = new Paragraph(
                "Explorez notre sélection d'événements exclusifs. Réservez vos places en quelques clics."
        );
        subtitle.addClassNames("page-subtitle");
        subtitle.getStyle()
                .set("text-align", "center")
                .set("max-width", "800px") // Limite la largeur du texte mais pas du conteneur
                .set("width", "100%")
                .set("padding", "0 20px") // Ajoute du padding sur les côtés
                .set("box-sizing", "border-box");

        fullWidthContainer.add(title, subtitle);
        header.add(fullWidthContainer);
        return header;
    }
    private Component createFiltersSection() {
        VerticalLayout filtersSection = new VerticalLayout();
        filtersSection.addClassNames("filters-section");

        Div filtersCard = new Div();
        filtersCard.addClassNames("filters-card");

        // Top row with search and results count
        HorizontalLayout topRow = new HorizontalLayout();
        topRow.setWidthFull();
        topRow.setAlignItems(FlexComponent.Alignment.CENTER);
        topRow.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);

        // Results count
        resultsCount = new Div();
        resultsCount.addClassNames("results-count");
        resultsCount.setText("Chargement...");

        // Search field
        HorizontalLayout searchLayout = new HorizontalLayout();
        searchLayout.setSpacing(true);
        searchLayout.setAlignItems(FlexComponent.Alignment.END);

        searchField = new TextField();
        searchField.setPlaceholder("Rechercher un événement...");
        searchField.setPrefixComponent(VaadinIcon.SEARCH.create());
        searchField.setWidth("300px");
        searchField.getStyle()
                .set("--lumo-border-radius", "8px");

        searchField.addValueChangeListener(e -> filterEvents());

        // Filter and sort
        categoryFilter = new Select<>();
        categoryFilter.setPlaceholder("Toutes catégories");
        categoryFilter.setItems("Toutes", "CONCERT", "THEATRE", "CONFERENCE", "SPORT", "AUTRE");
        categoryFilter.setWidth("180px");
        categoryFilter.addValueChangeListener(e -> filterEvents());

        sortFilter = new Select<>();
        sortFilter.setPlaceholder("Trier par");
        sortFilter.setItems("Date (plus proche)", "Date (plus lointain)", "Prix (croissant)", "Prix (décroissant)", "Popularité");
        sortFilter.setWidth("200px");
        sortFilter.addValueChangeListener(e -> filterEvents());

        Button clearFilters = new Button("Effacer les filtres", VaadinIcon.CLOSE.create());
        clearFilters.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        clearFilters.addClickListener(e -> {
            searchField.clear();
            categoryFilter.clear();
            sortFilter.clear();
            filterEvents();
        });

        searchLayout.add(searchField, categoryFilter, sortFilter, clearFilters);
        topRow.add(resultsCount, searchLayout);

        // Quick filter chips
        HorizontalLayout chipsRow = new HorizontalLayout();
        chipsRow.setSpacing(true);
        chipsRow.getStyle()
                .set("margin-top", "16px")
                .set("flex-wrap", "wrap");

        String[] categories = {"Tous", "Concerts", "Théâtre", "Conférences", "Sports", "Gratuits", "Prochains 7 jours"};
        for (String category : categories) {
            Div chip = new Div();
            chip.addClassNames("category-chip");
            chip.setText(category);

            chip.addClickListener(e -> {
                // Retirer la classe active de tous les chips
                chipsRow.getChildren()
                        .filter(Component.class::isInstance)
                        .map(Component.class::cast)
                        .forEach(c -> c.getElement().getClassList().remove("active"));

                // Ajouter la classe active au chip cliqué
                chip.getElement().getClassList().add("active");

                // Appliquer le filtre
                applyChipFilter(category);
            });

            chipsRow.add(chip);
        }

        filtersCard.add(topRow, chipsRow);
        filtersSection.add(filtersCard);
        return filtersSection;
    }

    private void applyChipFilter(String category) {
        switch (category) {
            case "Tous":
                searchField.clear();
                categoryFilter.clear();
                break;
            case "Concerts":
                categoryFilter.setValue("CONCERT");
                break;
            case "Théâtre":
                categoryFilter.setValue("THEATRE");
                break;
            case "Conférences":
                categoryFilter.setValue("CONFERENCE");
                break;
            case "Sports":
                categoryFilter.setValue("SPORT");
                break;
            case "Gratuits":
                // Filtrer les événements gratuits
                searchField.setValue("gratuit");
                break;
            case "Prochains 7 jours":
                // Filtrer les événements dans les 7 prochains jours
                LocalDateTime now = LocalDateTime.now();
                LocalDateTime nextWeek = now.plusDays(7);
                // Cette logique sera appliquée dans filterEvents()
                break;
        }
        filterEvents();
    }

    private Component createEventsSection() {
        VerticalLayout eventsSection = new VerticalLayout();
        eventsSection.setWidthFull();
        eventsSection.setPadding(false);
        eventsSection.setSpacing(false);

        eventsContainer = new VerticalLayout();
        eventsContainer.setWidthFull();
        eventsContainer.setSpacing(false);
        eventsContainer.setPadding(false);
        eventsContainer.getStyle().set("background", "#f8fafc");

        eventsSection.add(eventsContainer);
        return eventsSection;
    }

    private void loadAllEvents() {
        allEvents = eventService.getPublishedEvents();
        displayEvents(allEvents);
        updateResultsCount(allEvents.size());
    }

    private void filterEvents() {
        String keyword = searchField.getValue() != null ? searchField.getValue().toLowerCase() : "";
        String category = categoryFilter.getValue();
        String sort = sortFilter.getValue();

        List<Event> filtered = allEvents.stream()
                .filter(e -> keyword.isEmpty() ||
                        e.getTitre().toLowerCase().contains(keyword) ||
                        (e.getDescription() != null && e.getDescription().toLowerCase().contains(keyword)) ||
                        (e.getVille() != null && e.getVille().toLowerCase().contains(keyword)))
                .filter(e -> category == null || category.equals("Toutes") ||
                        e.getCategorie().name().equals(category))
                .toList();

        // Appliquer le tri
        List<Event> sorted = applySorting(filtered, sort);

        displayEvents(sorted);
        updateResultsCount(sorted.size());
    }

    private List<Event> applySorting(List<Event> events, String sortCriteria) {
        if (sortCriteria == null) {
            return events;
        }

        return switch (sortCriteria) {
            case "Date (plus proche)" -> events.stream()
                    .sorted((e1, e2) -> e1.getDateDebut().compareTo(e2.getDateDebut()))
                    .toList();
            case "Date (plus lointain)" -> events.stream()
                    .sorted((e1, e2) -> e2.getDateDebut().compareTo(e1.getDateDebut()))
                    .toList();
            case "Prix (croissant)" -> events.stream()
                    .sorted((e1, e2) -> {
                        Double p1 = e1.getPrixUnitaire() != null ? e1.getPrixUnitaire() : 0.0;
                        Double p2 = e2.getPrixUnitaire() != null ? e2.getPrixUnitaire() : 0.0;
                        return p1.compareTo(p2);
                    })
                    .toList();
            case "Prix (décroissant)" -> events.stream()
                    .sorted((e1, e2) -> {
                        Double p1 = e1.getPrixUnitaire() != null ? e1.getPrixUnitaire() : 0.0;
                        Double p2 = e2.getPrixUnitaire() != null ? e2.getPrixUnitaire() : 0.0;
                        return p2.compareTo(p1);
                    })
                    .toList();
            default -> events; // Par défaut ou "Popularité"
        };
    }

    private void displayEvents(List<Event> events) {
        eventsContainer.removeAll();
        eventsContainer.addClassNames("events-container");

        if (events.isEmpty()) {
            // Conteneur centré
            Div centerContainer = new Div();
            centerContainer.addClassNames("empty-state-container");

            Div emptyState = new Div();
            emptyState.addClassNames("empty-state");

            Icon emptyIcon = VaadinIcon.CALENDAR_CLOCK.create();
            emptyIcon.setSize("64px");
            emptyIcon.setColor("#cbd5e1");

            H3 emptyTitle = new H3("Aucun événement trouvé");

            Paragraph emptyText = new Paragraph(
                    "Aucun événement ne correspond à vos critères de recherche. " +
                            "Essayez de modifier vos filtres ou revenez plus tard."
            );

            Button clearAll = new Button("Effacer tous les filtres", VaadinIcon.REFRESH.create());
            clearAll.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
            clearAll.addClickListener(e -> {
                searchField.clear();
                categoryFilter.clear();
                sortFilter.clear();
                filterEvents();
            });

            emptyState.add(emptyIcon, emptyTitle, emptyText, clearAll);
            centerContainer.add(emptyState);
            eventsContainer.add(centerContainer);
            return;
        }

        // Grille d'événements
        Div grid = new Div();
        grid.addClassNames("events-grid");

        for (Event event : events) {
            EventCard eventCard = createEnhancedEventCard(event);
            eventCard.addClassNames("event-card");
            grid.add(eventCard);
        }

        eventsContainer.add(grid);
    }


    private EventCard createEnhancedEventCard(Event event) {
        String baseUrl = getBaseUrlFromService();

        EventCard card = new EventCard(event, baseUrl, eventService, reservationService, userService);

        // Style moderne pour la carte
        card.getStyle()
                .set("transition", "all 0.3s")
                .set("border", "1px solid #e2e8f0")
                .set("border-radius", "16px")
                .set("overflow", "hidden")
                .set("background", "white");

        card.getElement().addEventListener("mouseenter", e -> {
            card.getStyle()
                    .set("transform", "translateY(-8px)")
                    .set("box-shadow", "0 20px 40px rgba(0,0,0,0.12)");
        });

        card.getElement().addEventListener("mouseleave", e -> {
            card.getStyle()
                    .set("transform", "translateY(0)")
                    .remove("box-shadow");
        });

        return card;
    }

    private void updateResultsCount(int count) {
        String text = count + " événement" + (count > 1 ? "s" : "") + " trouvé" + (count > 1 ? "s" : "");
        resultsCount.setText(text);

        // Ajouter une icône
        resultsCount.removeAll();

        Icon countIcon = VaadinIcon.CALENDAR.create();
        countIcon.setSize("16px");
        countIcon.setColor("#6366f1");

        Span textSpan = new Span(text);

        resultsCount.add(countIcon, textSpan);
    }

    private String getBaseUrlFromService() {
        try {
            java.lang.reflect.Method method = fileStorageService.getClass().getMethod("getFullUrl", String.class);
            if (method != null) {
                String testUrl = (String) method.invoke(fileStorageService, "");
                if (testUrl != null && testUrl.contains("localhost")) {
                    return "http://localhost:8080";
                }
            }
        } catch (Exception e) {
            System.out.println("Impossible d'obtenir l'URL de base: " + e.getMessage());
        }
        return "http://localhost:8080";
    }
}