package com.eventbooking.view.publicview;

import com.eventbooking.entity.Event;
import com.eventbooking.entity.User;
import com.eventbooking.entity.enums.UserRole;
import com.eventbooking.service.EventService;
import com.eventbooking.service.FileStorageService;
import com.eventbooking.service.ReservationService;
import com.eventbooking.service.UserService;
import com.eventbooking.view.components.EventCard;
import com.vaadin.flow.component.*;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.contextmenu.ContextMenu;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
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

import java.util.List;

@Route("")
@PageTitle("Accueil - EventBooking")
public class HomeView extends VerticalLayout {

    private final EventService eventService;
    private final FileStorageService fileStorageService;
    private final ReservationService reservationService;
    private final UserService userService;

    private VerticalLayout eventsContainer;
    private List<Event> allEvents;
    private TextField searchField;
    private Select<String> categoryFilter;
    private Select<String> sortFilter;

    @Autowired
    public HomeView(EventService eventService,
                    FileStorageService fileStorageService,
                    ReservationService reservationService,
                    UserService userService) {
        this.eventService = eventService;
        this.fileStorageService = fileStorageService;
        this.reservationService = reservationService;
        this.userService = userService;

        addClassNames("home-view", LumoUtility.Overflow.HIDDEN);
        setPadding(false);
        setSpacing(false);
        setWidthFull();
        getStyle()
                .set("background", "linear-gradient(to bottom, #f8fafc 0%, #ffffff 100%)")
                .set("min-height", "100vh");

        // Injecter le CSS inline
        injectCustomCSS();

        add(
                createNavbar(),
                createHeroSection(),
                createSearchSection(),
                createEventsSection(),
                createCategoriesSection(),
                createFeaturesSection(),
                createTestimonialsSection(),
                createFooter()
        );

        loadEvents();
        initAnimations();
    }

    private void injectCustomCSS() {
        UI.getCurrent().getPage().addStyleSheet("https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css");


        // Injecter le CSS directement
        UI.getCurrent().getPage().executeJs("""
    const style = document.createElement('style');
    style.textContent = `
        /* ==================== EVENTS CARDS LAYOUT ==================== */
        .events-cards-layout {
            display: flex !important;
            flex-wrap: nowrap !important;
            overflow-x: auto !important;
            gap: 24px !important;
            padding: 20px 0 !important;
            scrollbar-width: thin !important;
            scrollbar-color: #cbd5e1 transparent !important;
        }
        
        .events-cards-layout::-webkit-scrollbar {
            height: 8px !important;
        }
        
        .events-cards-layout::-webkit-scrollbar-track {
            background: #f1f5f9 !important;
            border-radius: 4px !important;
        }
        
        .events-cards-layout::-webkit-scrollbar-thumb {
            background: #cbd5e1 !important;
            border-radius: 4px !important;
        }
        
        .events-cards-layout::-webkit-scrollbar-thumb:hover {
            background: #94a3b8 !important;
        }
        
        .event-card {
            flex: 0 0 auto !important;
            min-width: 320px !important;
            max-width: 350px !important;
            height: 500px !important;
            display: flex !important;
            flex-direction: column !important;
        }
        
        .event-content {
            flex-grow: 1 !important;
            display: flex !important;
            flex-direction: column !important;
        }
        
        /* Responsive pour mobile */
        @media (max-width: 768px) {
            .events-cards-layout {
                padding: 20px 16px !important;
                gap: 16px !important;
            }
            
            .event-card {
                min-width: 280px !important;
                max-width: 300px !important;
                height: 480px !important;
            }
        }
        
        @media (max-width: 480px) {
            .event-card {
                min-width: 260px !important;
                max-width: 280px !important;
                height: 460px !important;
            }
        }
        
        /* Animation de défilement fluide */
        .events-cards-layout {
            scroll-behavior: smooth !important;
        }
    `;
    document.head.appendChild(style);
""");

        // Injecter le CSS directement
        UI.getCurrent().getPage().executeJs("""
            const style = document.createElement('style');
            style.textContent = `
                /* ==================== ANIMATIONS ==================== */
                @keyframes fadeIn {
                    from { opacity: 0; transform: translateY(20px); }
                    to { opacity: 1; transform: translateY(0); }
                }
                
                @keyframes fadeInUp {
                    from { opacity: 0; transform: translateY(40px); }
                    to { opacity: 1; transform: translateY(0); }
                }
                
                @keyframes scaleUp {
                    from { opacity: 0; transform: scale(0.95); }
                    to { opacity: 1; transform: scale(1); }
                }
                
                @keyframes slideInLeft {
                    from { opacity: 0; transform: translateX(-30px); }
                    to { opacity: 1; transform: translateX(0); }
                }
                
                @keyframes float {
                    0%, 100% { transform: translateY(0); }
                    50% { transform: translateY(-10px); }
                }
                
                @keyframes pulse {
                    0%, 100% { opacity: 1; }
                    50% { opacity: 0.5; }
                }
                
                @keyframes gradient {
                    0% { background-position: 0% 50%; }
                    50% { background-position: 100% 50%; }
                    100% { background-position: 0% 50%; }
                }
                
                /* ==================== GLOBAL STYLES ==================== */
                .home-view {
                    overflow-x: hidden;
                    font-family: 'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
                }
                
                /* ==================== NAVBAR STYLES ==================== */
                .navbar {
                    background: rgba(255, 255, 255, 0.95);
                    backdrop-filter: blur(10px);
                    border-bottom: 1px solid #e2e8f0;
                    position: sticky;
                    top: 0;
                    z-index: 1000;
                    box-shadow: 0 1px 3px rgba(0,0,0,0.08);
                    transition: all 0.3s ease;
                }
                
                .logo-section {
                    cursor: pointer;
                    transition: all 0.3s ease;
                }
                
                .logo-section:hover {
                    transform: translateX(-4px);
                }
                
                .brand-name {
                    font-size: 20px;
                    font-weight: 700;
                    background: linear-gradient(135deg, #6366f1 0%, #8b5cf6 100%);
                    -webkit-background-clip: text;
                    -webkit-text-fill-color: transparent;
                    background-size: 200% 200%;
                    animation: gradient 3s ease infinite;
                }
                
                .brand-tagline {
                    font-size: 11px;
                    font-weight: 500;
                    color: #64748b;
                    letter-spacing: 1px;
                    text-transform: uppercase;
                }
                
                .nav-button {
                    position: relative;
                    overflow: hidden;
                    transition: all 0.3s ease;
                    font-weight: 500;
                    font-size: 14px;
                    padding: 8px 16px;
                    border-radius: 8px;
                }
                
                .nav-button:hover {
                    background: #f1f5f9;
                }
                
                .nav-button::after {
                    content: '';
                    position: absolute;
                    bottom: 0;
                    left: 50%;
                    width: 0;
                    height: 2px;
                    background: linear-gradient(135deg, #6366f1, #8b5cf6);
                    transition: all 0.3s ease;
                    transform: translateX(-50%);
                }
                
                .nav-button:hover::after {
                    width: 80%;
                }
                
                .notification-btn {
                    position: relative;
                }
                
                .notification-badge {
                    position: absolute;
                    top: -4px;
                    right: -4px;
                    background: #ef4444;
                    color: white;
                    font-size: 10px;
                    font-weight: 600;
                    width: 16px;
                    height: 16px;
                    border-radius: 50%;
                    display: flex;
                    align-items: center;
                    justify-content: center;
                    animation: pulse 2s infinite;
                }
                
                .user-avatar {
                    cursor: pointer;
                    border: 2px solid white;
                    box-shadow: 0 2px 8px rgba(0,0,0,0.15);
                    transition: all 0.3s ease;
                }
                
                .user-avatar:hover {
                    transform: scale(1.05);
                    box-shadow: 0 4px 12px rgba(99, 102, 241, 0.3);
                }
                
                .login-btn, .register-btn {
                    position: relative;
                    overflow: hidden;
                    transition: all 0.3s ease;
                    font-weight: 500;
                }
                
                .login-btn {
                    padding: 8px 20px;
                }
                
                .register-btn {
                    padding: 8px 24px;
                    border-radius: 8px;
                    border: none;
                    box-shadow: 0 4px 12px rgba(99, 102, 241, 0.25);
                    background: linear-gradient(135deg, #6366f1 0%, #8b5cf6 100%);
                    background-size: 200% 200%;
                    animation: gradient 3s ease infinite;
                }
                
                .register-btn:hover {
                    transform: translateY(-2px);
                    box-shadow: 0 8px 20px rgba(99, 102, 241, 0.4);
                }
                
                /* ==================== HERO SECTION ==================== */
                .hero-section {
                    position: relative;
                    overflow: hidden;
                    padding: 80px 20px;
                    background: linear-gradient(135deg, #f8fafc 0%, #ffffff 100%);
                    animation: fadeIn 0.8s ease-out;
                }
                
                .hero-pattern {
                    position: absolute;
                    top: 0;
                    left: 0;
                    right: 0;
                    bottom: 0;
                    background-image: radial-gradient(#e2e8f0 1px, transparent 1px);
                    background-size: 40px 40px;
                    opacity: 0.3;
                    animation: float 8s ease-in-out infinite;
                }
                
                .hero-glow {
                    position: absolute;
                    width: 600px;
                    height: 600px;
                    background: radial-gradient(circle, rgba(99, 102, 241, 0.15) 0%, transparent 70%);
                    top: 50%;
                    left: 50%;
                    transform: translate(-50%, -50%);
                    animation: float 6s ease-in-out infinite;
                }
                
                .hero-content {
                    position: relative;
                    z-index: 1;
                    max-width: 800px;
                    margin: 0 auto;
                    text-align: center;
                }
                
                .hero-badge {
                    display: inline-flex;
                    align-items: center;
                    gap: 8px;
                    padding: 8px 16px;
                    background: rgba(99, 102, 241, 0.1);
                    border-radius: 20px;
                    margin-bottom: 16px;
                    animation: fadeInUp 0.6s ease-out 0.2s both;
                }
                
                .hero-title {
                    font-size: clamp(36px, 5vw, 48px);
                    font-weight: 800;
                    line-height: 1.2;
                    margin: 0 0 24px 0;
                    background: linear-gradient(135deg, #1e293b 0%, #475569 100%);
                    -webkit-background-clip: text;
                    -webkit-text-fill-color: transparent;
                    animation: fadeInUp 0.6s ease-out 0.4s both;
                }
                
                .hero-subtitle {
                    font-size: clamp(16px, 2vw, 18px);
                    color: #64748b;
                    line-height: 1.6;
                    margin: 0 auto 48px;
                    max-width: 600px;
                    animation: fadeInUp 0.6s ease-out 0.6s both;
                }
                
                .hero-cta-buttons {
                    animation: fadeInUp 0.6s ease-out 0.8s both;
                }
                
                .explore-btn, .create-btn {
                    position: relative;
                    overflow: hidden;
                    transition: all 0.3s ease;
                    padding: 16px 32px;
                    border-radius: 12px;
                    font-weight: 600;
                }
                
                .explore-btn {
                    background: linear-gradient(135deg, #6366f1 0%, #8b5cf6 100%);
                    border: none;
                    box-shadow: 0 8px 24px rgba(99, 102, 241, 0.3);
                    background-size: 200% 200%;
                    animation: gradient 3s ease infinite;
                }
                
                .create-btn {
                    border: 2px solid #e2e8f0;
                }
                
                .explore-btn:hover, .create-btn:hover {
                    transform: translateY(-2px);
                }
                
                .explore-btn:hover {
                    box-shadow: 0 12px 32px rgba(99, 102, 241, 0.4);
                }
                
                .hero-stats {
                    margin-top: 48px;
                    animation: fadeInUp 0.6s ease-out 1s both;
                }
                
                .stat-card {
                    text-align: center;
                    animation: fadeInUp 0.6s ease-out;
                    animation-fill-mode: both;
                }
                
                .stat-card:nth-child(1) { animation-delay: 0.1s; }
                .stat-card:nth-child(2) { animation-delay: 0.2s; }
                .stat-card:nth-child(3) { animation-delay: 0.3s; }
                .stat-card:nth-child(4) { animation-delay: 0.4s; }
                
                .stat-number {
                    margin: 0;
                    font-size: clamp(24px, 3vw, 32px);
                    font-weight: 800;
                    color: #1e293b;
                    background: linear-gradient(135deg, #6366f1, #8b5cf6);
                    -webkit-background-clip: text;
                    -webkit-text-fill-color: transparent;
                    background-size: 200% 200%;
                    animation: gradient 3s ease infinite;
                }
                
                .stat-label {
                    font-size: 14px;
                    color: #64748b;
                    font-weight: 500;
                }
                
                /* ==================== CARDS ANIMATIONS ==================== */
                .category-card, .feature-card, .testimonial-card, .event-card {
                    transition: all 0.3s ease;
                    animation: fadeIn 0.6s ease-out;
                    animation-fill-mode: both;
                }
                
                .category-card:hover, .feature-card:hover, .testimonial-card:hover {
                    transform: translateY(-4px);
                    box-shadow: 0 12px 24px rgba(0,0,0,0.1);
                }
                
                .event-card:hover {
                    transform: translateY(-8px);
                    box-shadow: 0 20px 40px rgba(0,0,0,0.12) !important;
                }
                
                /* ==================== RESPONSIVE ==================== */
                @media (max-width: 768px) {
                    .hero-title {
                        font-size: 32px !important;
                    }
                    
                    .hero-stats {
                        flex-wrap: wrap;
                        justify-content: center;
                        gap: 24px;
                    }
                    
                    .hero-cta-buttons {
                        flex-direction: column;
                        width: 100%;
                    }
                    
                    .explore-btn, .create-btn {
                        width: 100%;
                        margin-bottom: 12px;
                    }
                }
                
                @media (max-width: 480px) {
                    .hero-title {
                        font-size: 28px !important;
                    }
                    
                    .navbar {
                        padding: 12px !important;
                    }
                    
                    .nav-button {
                        padding: 6px 12px;
                        font-size: 12px;
                    }
                }
                
                /* ==================== UTILITY ANIMATIONS ==================== */
                .animate-on-scroll {
                    opacity: 0;
                    transform: translateY(30px);
                    transition: all 0.8s ease;
                }
                
                .animate-on-scroll.animated {
                    opacity: 1;
                    transform: translateY(0);
                }
                
                .fade-in {
                    animation: fadeIn 0.6s ease-out;
                }
                
                .fade-in-up {
                    animation: fadeInUp 0.6s ease-out;
                }
                
                .scale-up {
                    animation: scaleUp 0.6s ease-out;
                }
            `;
            document.head.appendChild(style);
        """);
    }

    private void initAnimations() {
        UI.getCurrent().getPage().executeJs("""
            // Intersection Observer pour les animations au scroll
            const observer = new IntersectionObserver((entries) => {
                entries.forEach(entry => {
                    if (entry.isIntersecting) {
                        entry.target.classList.add('animated');
                    }
                });
            }, {
                threshold: 0.1,
                rootMargin: '0px 0px -50px 0px'
            });
            
            // Observer tous les éléments avec animate-on-scroll
            document.querySelectorAll('.animate-on-scroll').forEach(el => {
                observer.observe(el);
            });
            
            // Animation des boutons au hover
            document.querySelectorAll('.nav-button, .register-btn, .explore-btn, .create-btn').forEach(button => {
                button.addEventListener('mouseenter', () => {
                    button.style.transform = 'translateY(-2px)';
                });
                button.addEventListener('mouseleave', () => {
                    button.style.transform = 'translateY(0)';
                });
            });
            
            // Effet de parallaxe pour la section hero
            window.addEventListener('scroll', () => {
                const scrolled = window.pageYOffset;
                const heroPattern = document.querySelector('.hero-pattern');
                const heroGlow = document.querySelector('.hero-glow');
                
                if (heroPattern) {
                    heroPattern.style.transform = `translateY(${scrolled * 0.5}px)`;
                }
                if (heroGlow) {
                    heroGlow.style.transform = `translate(-50%, calc(-50% + ${scrolled * 0.3}px))`;
                }
            });
        """);
    }

    // ==================== NAVBAR ====================
    private Component createNavbar() {
        HorizontalLayout navbar = new HorizontalLayout();
        navbar.addClassNames("navbar", "animate-on-scroll");
        navbar.setWidthFull();
        navbar.setPadding(true);
        navbar.setSpacing(false);
        navbar.setAlignItems(FlexComponent.Alignment.CENTER);
        navbar.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);

        // Logo section
        HorizontalLayout logoSection = createLogoSection();

        // Navigation menu
        HorizontalLayout menuLayout = createNavigationMenu();

        // Right section
        Component rightSection = createRightSection();

        navbar.add(logoSection, menuLayout, rightSection);
        return navbar;
    }

    private HorizontalLayout createLogoSection() {
        HorizontalLayout logoSection = new HorizontalLayout();
        logoSection.addClassNames("logo-section");
        logoSection.setAlignItems(FlexComponent.Alignment.CENTER);
        logoSection.getStyle().set("cursor", "pointer");

        // Logo avec badge
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

        // Nom
        Div nameContainer = new Div();
        nameContainer.getStyle()
                .set("display", "flex")
                .set("flex-direction", "column");

        Span brandName = new Span("EventBooking");
        brandName.addClassName("brand-name");

        Span tagline = new Span("Events Platform");
        tagline.addClassName("brand-tagline");

        nameContainer.add(brandName, tagline);
        logoSection.add(logoContainer, nameContainer);

        logoSection.addClickListener(e -> getUI().ifPresent(ui -> ui.getPage().reload()));

        return logoSection;
    }

    private HorizontalLayout createNavigationMenu() {
        HorizontalLayout menuLayout = new HorizontalLayout();
        menuLayout.setAlignItems(FlexComponent.Alignment.CENTER);

        String[] menuItems = {"Accueil", "Événements", "Catégories", "À propos"};
        String[] sectionIds = {null, "events-section", "categories-section", null};

        for (int i = 0; i < menuItems.length; i++) {
            Button btn = createNavButton(menuItems[i]);
            if (sectionIds[i] != null) {
                final String sectionId = sectionIds[i];
                btn.addClickListener(e -> scrollToSection(sectionId));
            } else if (i == 0) {
                btn.addClickListener(e -> scrollToTop());
            }
            menuLayout.add(btn);
        }

        return menuLayout;
    }

    private Button createNavButton(String text) {
        Button btn = new Button(text);
        btn.addClassNames("nav-button");
        btn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        return btn;
    }

    private Component createRightSection() {
        User currentUser = getCurrentUser();
        return currentUser != null ? createUserMenuSection(currentUser) : createLoginRegisterButtons();
    }

    private Component createUserMenuSection(User user) {
        HorizontalLayout userSection = new HorizontalLayout();
        userSection.setAlignItems(FlexComponent.Alignment.CENTER);

        // Notification icon
        Button notificationBtn = new Button(VaadinIcon.BELL.create());
        notificationBtn.addClassNames("notification-btn");
        notificationBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        Span badge = new Span("3");
        badge.addClassName("notification-badge");

        // Avatar avec badge
        Avatar avatar = new Avatar(user.getPrenom() + " " + user.getNom());
        avatar.setImage("https://api.dicebear.com/7.x/avataaars/svg?seed=" + user.getEmail());
        avatar.addClassName("user-avatar");

        // Context menu
        ContextMenu contextMenu = new ContextMenu();
        contextMenu.setTarget(avatar);
        contextMenu.setOpenOnClick(true);

        MenuItem profileItem = contextMenu.addItem("Mon Profil", e ->
                getUI().ifPresent(ui -> ui.navigate("client/profile")));
        profileItem.addComponentAsFirst(VaadinIcon.USER.create());

        MenuItem reservationsItem = contextMenu.addItem("Mes Réservations", e ->
                getUI().ifPresent(ui -> ui.navigate("client/reservations")));
        reservationsItem.addComponentAsFirst(VaadinIcon.TICKET.create());

        MenuItem dashboardItem = contextMenu.addItem("Dashboard", e ->
                getUI().ifPresent(ui -> ui.navigate(getDashboardRoute(user.getRole()))));
        dashboardItem.addComponentAsFirst(VaadinIcon.DASHBOARD.create());

        contextMenu.add(new Hr());

        MenuItem logoutItem = contextMenu.addItem("Déconnexion", e -> {
            VaadinSession.getCurrent().setAttribute("currentUser", null);
            VaadinSession.getCurrent().close();
            getUI().ifPresent(ui -> {
                ui.getPage().reload();
                ui.navigate("");
            });
        });
        logoutItem.addComponentAsFirst(VaadinIcon.SIGN_OUT.create());
        logoutItem.getElement().getStyle().set("color", "#ef4444");

        userSection.add(notificationBtn, avatar);
        return userSection;
    }

    private Component createLoginRegisterButtons() {
        HorizontalLayout buttons = new HorizontalLayout();
        buttons.addClassNames("auth-buttons");

        Button loginBtn = new Button("Connexion");
        loginBtn.addClassNames("login-btn");
        loginBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        Button registerBtn = new Button("S'inscrire");
        registerBtn.addClassNames("register-btn");
        registerBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        loginBtn.addClickListener(e -> getUI().ifPresent(ui -> ui.navigate("login")));
        registerBtn.addClickListener(e -> getUI().ifPresent(ui -> ui.navigate("register")));

        buttons.add(loginBtn, registerBtn);
        return buttons;
    }

    // ==================== HERO SECTION ====================
    private Component createHeroSection() {
        Div hero = new Div();
        hero.addClassNames("hero-section", "animate-on-scroll");
        hero.setWidthFull();

        // Background pattern
        Div pattern = new Div();
        pattern.addClassName("hero-pattern");

        // Glow effect
        Div glow = new Div();
        glow.addClassName("hero-glow");

        // Content
        VerticalLayout content = new VerticalLayout();
        content.addClassNames("hero-content");
        content.setAlignItems(FlexComponent.Alignment.CENTER);
        content.getStyle()
                .set("position", "relative")
                .set("z-index", "1")
                .set("max-width", "800px")
                .set("margin", "0 auto")
                .set("text-align", "center");

        // Badge
        Div badge = new Div();
        badge.addClassName("hero-badge");

        Icon starIcon = VaadinIcon.STAR.create();
        starIcon.setSize("16px");
        starIcon.setColor("#6366f1");

        Span badgeText = new Span("Plateforme N°1 au Maroc");
        badgeText.getStyle()
                .set("font-size", "14px")
                .set("font-weight", "600")
                .set("color", "#6366f1");

        badge.add(starIcon, badgeText);

        // Title
        H1 heroTitle = new H1("Découvrez des expériences inoubliables");
        heroTitle.addClassName("hero-title");

        // Subtitle
        Paragraph heroSubtitle = new Paragraph(
                "Réservez vos places pour les meilleurs événements au Maroc. " +
                        "Concerts, conférences, spectacles et plus encore."
        );
        heroSubtitle.addClassName("hero-subtitle");

        // CTA Buttons
        HorizontalLayout ctaButtons = new HorizontalLayout();
        ctaButtons.addClassName("hero-cta-buttons");
        ctaButtons.setAlignItems(FlexComponent.Alignment.CENTER);

        Button exploreBtn = new Button("Explorer les événements", VaadinIcon.ARROW_RIGHT.create());
        exploreBtn.addClassNames("explore-btn");
        exploreBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_LARGE);
        exploreBtn.setIconAfterText(true);

        Button createEventBtn = new Button("Créer un événement", VaadinIcon.PLUS.create());
        createEventBtn.addClassNames("create-btn");
        createEventBtn.addThemeVariants(ButtonVariant.LUMO_CONTRAST);

        exploreBtn.addClickListener(e -> scrollToSection("events-section"));
        createEventBtn.addClickListener(e -> {
            User user = getCurrentUser();
            if (user != null && user.getRole() == UserRole.ORGANIZER) {
                getUI().ifPresent(ui -> ui.navigate("organizer/events/create"));
            } else if (user != null) {
                Notification.show("Seuls les organisateurs peuvent créer des événements", 3000, Notification.Position.MIDDLE);
            } else {
                getUI().ifPresent(ui -> ui.navigate("login"));
            }
        });

        ctaButtons.add(exploreBtn, createEventBtn);

        // Stats
        HorizontalLayout stats = new HorizontalLayout();
        stats.addClassName("hero-stats");

        stats.add(
                createStatCard("500+", "Événements"),
                createStatCard("50K+", "Participants"),
                createStatCard("98%", "Satisfaction"),
                createStatCard("24/7", "Support")
        );

        content.add(badge, heroTitle, heroSubtitle, ctaButtons, stats);
        hero.add(pattern, glow, content);
        return hero;
    }

    private Component createStatCard(String number, String label) {
        VerticalLayout card = new VerticalLayout();
        card.addClassNames("stat-card");
        card.setAlignItems(FlexComponent.Alignment.CENTER);
        card.setPadding(false);

        H3 numberText = new H3(number);
        numberText.addClassName("stat-number");

        Span labelText = new Span(label);
        labelText.addClassName("stat-label");

        card.add(numberText, labelText);
        return card;
    }

    // ==================== SEARCH SECTION ====================
    private Component createSearchSection() {
        VerticalLayout searchSection = new VerticalLayout();
        searchSection.addClassNames("search-section", "animate-on-scroll");
        searchSection.setWidthFull();
        searchSection.setAlignItems(FlexComponent.Alignment.CENTER);
        searchSection.setPadding(true);
        searchSection.getStyle()
                .set("padding", "48px 20px")
                .set("background", "white");

        H3 searchTitle = new H3("Trouvez votre prochain événement");
        searchTitle.getStyle()
                .set("margin", "0 0 24px 0")
                .set("color", "#1e293b")
                .set("font-size", "clamp(28px, 4vw, 32px)")
                .set("font-weight", "700");

        // Search card
        Div searchCard = new Div();
        searchCard.addClassNames("search-card", "fade-in-up");
        searchCard.getStyle()
                .set("width", "100%")
                .set("max-width", "900px")
                .set("background", "white")
                .set("border-radius", "16px")
                .set("box-shadow", "0 10px 40px rgba(0,0,0,0.08)")
                .set("padding", "24px");

        HorizontalLayout searchBar = new HorizontalLayout();
        searchBar.setWidthFull();
        searchBar.setAlignItems(FlexComponent.Alignment.END);

        searchField = new TextField();
        searchField.setPlaceholder("Rechercher concerts, conférences, sports...");
        searchField.setPrefixComponent(VaadinIcon.SEARCH.create());
        searchField.setWidth("100%");
        searchField.getStyle()
                .set("--lumo-border-radius", "12px");

        categoryFilter = new Select<>();
        categoryFilter.setPlaceholder("Toutes catégories");
        categoryFilter.setItems("Toutes", "CONCERT", "THEATRE", "CONFERENCE", "SPORT", "AUTRE");
        categoryFilter.setWidth("180px");
        categoryFilter.getStyle()
                .set("--lumo-border-radius", "12px");

        sortFilter = new Select<>();
        sortFilter.setPlaceholder("Trier par");
        sortFilter.setItems("Date ↑", "Date ↓", "Popularité", "Prix ↑", "Prix ↓");
        sortFilter.setWidth("140px");
        sortFilter.getStyle()
                .set("--lumo-border-radius", "12px");

        Button searchBtn = new Button("Rechercher", VaadinIcon.SEARCH.create());
        searchBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        searchBtn.getStyle()
                .set("background", "linear-gradient(135deg, #6366f1 0%, #8b5cf6 100%)")
                .set("border", "none")
                .set("border-radius", "12px")
                .set("padding", "12px 24px");

        searchBtn.addClickListener(e -> filterEvents());

        searchBar.add(searchField, categoryFilter, sortFilter, searchBtn);
        searchBar.setFlexGrow(1, searchField);

        searchCard.add(searchBar);
        searchSection.add(searchTitle, searchCard);
        return searchSection;
    }

    // ==================== EVENTS SECTION ====================
    private Component createEventsSection() {
        VerticalLayout eventsSection = new VerticalLayout();
        eventsSection.addClassNames("events-section", "animate-on-scroll");
        eventsSection.setWidthFull();
        eventsSection.setPadding(true);
        eventsSection.setId("events-section");
        eventsSection.getStyle()
                .set("padding", "80px 20px")
                .set("background", "#f8fafc");

        // Header
        HorizontalLayout header = new HorizontalLayout();
        header.setWidthFull();
        header.setAlignItems(FlexComponent.Alignment.END);
        header.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);

        Div titleSection = new Div();
        titleSection.getStyle().set("display", "flex").set("flex-direction", "column");

        H2 sectionTitle = new H2("Événements à venir");
        sectionTitle.getStyle()
                .set("margin", "0 0 8px 0")
                .set("color", "#1e293b")
                .set("font-size", "clamp(28px, 4vw, 36px)")
                .set("font-weight", "700");

        Span sectionSubtitle = new Span("Découvrez les meilleurs événements près de chez vous");
        sectionSubtitle.getStyle()
                .set("color", "#64748b")
                .set("font-size", "16px");

        titleSection.add(sectionTitle, sectionSubtitle);

        // View all button
        Button viewAllBtn = new Button("Voir tout", VaadinIcon.ARROW_RIGHT.create());
        viewAllBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        viewAllBtn.setIconAfterText(true);
        viewAllBtn.getStyle()
                .set("font-weight", "600")
                .set("color", "#6366f1");

        viewAllBtn.addClickListener(e -> getUI().ifPresent(ui -> ui.navigate("all-events")));

        header.add(titleSection, viewAllBtn);

        // Events container
        eventsContainer = new VerticalLayout();
        eventsContainer.setWidthFull();
        eventsContainer.setSpacing(false);
        eventsContainer.setPadding(false);

        eventsSection.add(header, eventsContainer);
        return eventsSection;
    }

    // ==================== CATEGORIES SECTION ====================
    private Component createCategoriesSection() {
        VerticalLayout categoriesSection = new VerticalLayout();
        categoriesSection.addClassNames("categories-section");
        categoriesSection.setWidthFull();
        categoriesSection.setAlignItems(FlexComponent.Alignment.CENTER);
        categoriesSection.getStyle()
                .set("padding", "80px 20px")
                .set("background", "white");

        // Header
        VerticalLayout titleSection = new VerticalLayout();
        titleSection.setAlignItems(FlexComponent.Alignment.CENTER);
        titleSection.setPadding(false);
        titleSection.getStyle()
                .set("text-align", "center")
                .set("max-width", "600px")
                .set("margin-bottom", "48px");

        H2 sectionTitle = new H2("Parcourir par catégorie");
        sectionTitle.getStyle()
                .set("margin", "0 0 16px 0")
                .set("color", "#1e293b")
                .set("font-size", "clamp(28px, 4vw, 36px)")
                .set("font-weight", "700");

        Span sectionSubtitle = new Span("Trouvez des événements qui correspondent à vos intérêts");
        sectionSubtitle.getStyle()
                .set("color", "#64748b")
                .set("font-size", "18px");

        titleSection.add(sectionTitle, sectionSubtitle);

        // Categories grid
        Div categoriesGrid = new Div();
        categoriesGrid.getStyle()
                .set("display", "grid")
                .set("grid-template-columns", "repeat(auto-fit, minmax(250px, 1fr))")
                .set("gap", "24px")
                .set("max-width", "1200px")
                .set("margin", "0 auto");

        // Category cards - création directe sans dépendre de méthodes complexes
        categoriesGrid.add(
                createCategoryCard(
                        VaadinIcon.MUSIC.create(),
                        "Concerts",
                        "Vivez des moments magiques",
                        "#8b5cf6"
                ),
                createCategoryCard(
                        VaadinIcon.HOME.create(),
                        "Théâtre",
                        "Plongez dans des histoires",
                        "#6366f1"
                ),
                createCategoryCard(
                        VaadinIcon.COMMENT_ELLIPSIS.create(),
                        "Conférences",
                        "Apprenez des experts",
                        "#10b981"
                ),
                createCategoryCard(
                        VaadinIcon.TROPHY.create(),
                        "Sports",
                        "Vivez l'émotion",
                        "#ef4444"
                ),
                createCategoryCard(
                        VaadinIcon.USERS.create(),
                        "Networking",
                        "Rencontrez des pros",
                        "#f59e0b"
                ),
                createCategoryCard(
                        VaadinIcon.STAR.create(),
                        "Autres",
                        "Découvrez l'inattendu",
                        "#06b6d4"
                )
        );

        categoriesSection.add(titleSection, categoriesGrid);
        return categoriesSection;
    }

    private Component createCategoryCard(Icon icon, String title, String subtitle, String color) {
        Div card = new Div();
        card.addClassNames("category-card");
        card.getStyle()
                .set("background", "white")
                .set("border-radius", "16px")
                .set("padding", "32px")
                .set("transition", "all 0.3s ease")
                .set("cursor", "pointer")
                .set("border", "2px solid transparent")
                .set("box-shadow", "0 4px 12px rgba(0,0,0,0.05)")
                .set("height", "100%");

        // Icon container
        Div iconContainer = new Div();
        iconContainer.getStyle()
                .set("width", "64px")
                .set("height", "64px")
                .set("background", color)
                .set("border-radius", "16px")
                .set("display", "flex")
                .set("align-items", "center")
                .set("justify-content", "center")
                .set("margin-bottom", "24px")
                .set("transition", "all 0.3s ease");

        icon.setSize("28px");
        icon.setColor("white");
        iconContainer.add(icon);

        // Content
        VerticalLayout content = new VerticalLayout();
        content.setPadding(false);
        content.setSpacing(false);

        H4 cardTitle = new H4(title);
        cardTitle.getStyle()
                .set("margin", "0 0 8px 0")
                .set("color", "#1e293b")
                .set("font-size", "20px")
                .set("font-weight", "600");

        Span cardSubtitle = new Span(subtitle);
        cardSubtitle.getStyle()
                .set("color", "#64748b")
                .set("font-size", "14px")
                .set("line-height", "1.5");

        // Explore button
        Button exploreBtn = new Button("Explorer", VaadinIcon.ARROW_RIGHT.create());
        exploreBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        exploreBtn.getStyle()
                .set("padding", "8px 0")
                .set("font-size", "14px")
                .set("color", color)
                .set("margin-top", "12px");
        exploreBtn.setIconAfterText(true);

        content.add(cardTitle, cardSubtitle, exploreBtn);
        card.add(iconContainer, content);

        // Animation au hover
        card.getElement().addEventListener("mouseenter", e -> {
            card.getStyle()
                    .set("transform", "translateY(-8px)")
                    .set("box-shadow", "0 20px 40px rgba(0,0,0,0.12)")
                    .set("border-color", color);
            iconContainer.getStyle()
                    .set("transform", "scale(1.1) rotate(5deg)")
                    .set("box-shadow", "0 8px 16px " + color + "80");
            exploreBtn.getStyle().set("transform", "translateX(4px)");
        });

        card.getElement().addEventListener("mouseleave", e -> {
            card.getStyle()
                    .set("transform", "translateY(0)")
                    .set("box-shadow", "0 4px 12px rgba(0,0,0,0.05)")
                    .remove("border-color");
            iconContainer.getStyle()
                    .set("transform", "scale(1) rotate(0deg)")
                    .set("box-shadow", "none");
            exploreBtn.getStyle().remove("transform");
        });

        // Click handler pour la carte
        card.addClickListener(e -> {
            // Naviguer vers les événements filtrés par catégorie
            String category = title.toUpperCase();
            if (category.equals("CONCERTS")) category = "CONCERT";
            else if (category.equals("THÉÂTRE")) category = "THEATRE";
            else if (category.equals("CONFÉRENCES")) category = "CONFERENCE";
            else if (category.equals("SPORTS")) category = "SPORT";
            else if (category.equals("NETWORKING")) category = "AUTRE";
            else if (category.equals("AUTRES")) category = "AUTRE";

            // Filtrer les événements par cette catégorie
            if (categoryFilter != null) {
                categoryFilter.setValue(category);
                filterEvents();
                scrollToSection("events-section");
            }
        });

        return card;
    }
    // ==================== FEATURES SECTION ====================
    private Component createFeaturesSection() {
        VerticalLayout featuresSection = new VerticalLayout();
        featuresSection.addClassNames("features-section");
        featuresSection.setWidthFull();
        featuresSection.setAlignItems(FlexComponent.Alignment.CENTER);
        featuresSection.getStyle()
                .set("padding", "80px 20px")
                .set("background", "#f8fafc");

        // Header
        VerticalLayout titleSection = new VerticalLayout();
        titleSection.setAlignItems(FlexComponent.Alignment.CENTER);
        titleSection.setPadding(false);
        titleSection.getStyle()
                .set("text-align", "center")
                .set("max-width", "600px")
                .set("margin-bottom", "60px");

        H2 sectionTitle = new H2("Pourquoi choisir EventBooking ?");
        sectionTitle.getStyle()
                .set("margin", "0 0 16px 0")
                .set("color", "#1e293b")
                .set("font-size", "clamp(28px, 4vw, 36px)")
                .set("font-weight", "700");

        Span sectionSubtitle = new Span("La plateforme la plus complète pour vos événements");
        sectionSubtitle.getStyle()
                .set("color", "#64748b")
                .set("font-size", "18px");

        titleSection.add(sectionTitle, sectionSubtitle);

        // Features grid
        Div featuresGrid = new Div();
        featuresGrid.getStyle()
                .set("display", "grid")
                .set("grid-template-columns", "repeat(auto-fit, minmax(300px, 1fr))")
                .set("gap", "32px")
                .set("max-width", "1200px")
                .set("margin", "0 auto");

        // Créer et ajouter les cartes de fonctionnalités
        featuresGrid.add(
                createFeatureCard(
                        VaadinIcon.SHIELD.create(),
                        "Paiement Sécurisé",
                        "Transactions 100% sécurisées avec cryptage SSL",
                        "#10b981"
                ),
                createFeatureCard(
                        VaadinIcon.CLOCK.create(),
                        "Réservation Rapide",
                        "Réservez en 3 clics maximum depuis n'importe quel appareil",
                        "#6366f1"
                ),
                createFeatureCard(
                        VaadinIcon.TICKET.create(),
                        "Billets Digitaux",
                        "Accédez à vos billets directement depuis votre smartphone",
                        "#8b5cf6"
                ),
                createFeatureCard(
                        VaadinIcon.STAR.create(),
                        "Événements Premium",
                        "Accès exclusif aux meilleurs événements de la région",
                        "#f59e0b"
                ),
                createFeatureCard(
                        VaadinIcon.HEADPHONES.create(),
                        "Support 24/7",
                        "Notre équipe est disponible pour vous aider à tout moment",
                        "#ef4444"
                ),
                createFeatureCard(
                        VaadinIcon.CHART.create(),
                        "Analytics Avancés",
                        "Suivez vos événements avec nos outils d'analyse",
                        "#06b6d4"
                )
        );

        featuresSection.add(titleSection, featuresGrid);
        return featuresSection;
    }

    private Component createFeatureCard(Icon icon, String title, String description, String color) {
        Div card = new Div();
        card.addClassNames("feature-card");
        card.getStyle()
                .set("background", "white")
                .set("border-radius", "16px")
                .set("padding", "32px")
                .set("transition", "all 0.3s ease")
                .set("border", "1px solid #e2e8f0")
                .set("height", "100%");

        // Icon container
        Div iconContainer = new Div();
        iconContainer.getStyle()
                .set("width", "64px")
                .set("height", "64px")
                .set("background", "linear-gradient(135deg, " + color + "20 0%, " + color + "10 100%)")
                .set("border-radius", "16px")
                .set("display", "flex")
                .set("align-items", "center")
                .set("justify-content", "center")
                .set("margin-bottom", "24px")
                .set("transition", "all 0.3s ease");

        icon.setSize("28px");
        icon.setColor(color);
        iconContainer.add(icon);

        // Content
        VerticalLayout content = new VerticalLayout();
        content.setPadding(false);
        content.setSpacing(false);

        H4 cardTitle = new H4(title);
        cardTitle.getStyle()
                .set("margin", "0 0 12px 0")
                .set("color", "#1e293b")
                .set("font-size", "20px")
                .set("font-weight", "600");

        Paragraph cardDescription = new Paragraph(description);
        cardDescription.getStyle()
                .set("margin", "0")
                .set("color", "#64748b")
                .set("font-size", "15px")
                .set("line-height", "1.6");

        content.add(cardTitle, cardDescription);

        card.add(iconContainer, content);

        // Animation au hover
        card.getElement().addEventListener("mouseenter", e -> {
            card.getStyle()
                    .set("transform", "translateY(-8px)")
                    .set("box-shadow", "0 20px 40px rgba(0,0,0,0.12)");
            iconContainer.getStyle()
                    .set("transform", "scale(1.1)")
                    .set("box-shadow", "0 8px 16px " + color + "40");
        });

        card.getElement().addEventListener("mouseleave", e -> {
            card.getStyle()
                    .set("transform", "translateY(0)")
                    .set("box-shadow", "none");
            iconContainer.getStyle()
                    .set("transform", "scale(1)")
                    .set("box-shadow", "none");
        });

        return card;
    }


    // ==================== TESTIMONIALS SECTION ====================
    private Component createTestimonialsSection() {
        VerticalLayout testimonialsSection = new VerticalLayout();
        testimonialsSection.addClassNames("testimonials-section");
        testimonialsSection.setWidthFull();
        testimonialsSection.setAlignItems(FlexComponent.Alignment.CENTER);
        testimonialsSection.getStyle()
                .set("padding", "80px 20px")
                .set("background", "white");

        // Header
        VerticalLayout titleSection = new VerticalLayout();
        titleSection.setAlignItems(FlexComponent.Alignment.CENTER);
        titleSection.setPadding(false);
        titleSection.getStyle()
                .set("text-align", "center")
                .set("max-width", "600px")
                .set("margin-bottom", "60px");

        H2 sectionTitle = new H2("Ce que disent nos utilisateurs");
        sectionTitle.getStyle()
                .set("margin", "0 0 16px 0")
                .set("color", "#1e293b")
                .set("font-size", "clamp(28px, 4vw, 36px)")
                .set("font-weight", "700");

        Span sectionSubtitle = new Span("Rejoignez des milliers d'utilisateurs satisfaits");
        sectionSubtitle.getStyle()
                .set("color", "#64748b")
                .set("font-size", "18px");

        titleSection.add(sectionTitle, sectionSubtitle);

        // Testimonials grid
        Div testimonialsGrid = new Div();
        testimonialsGrid.getStyle()
                .set("display", "grid")
                .set("grid-template-columns", "repeat(auto-fit, minmax(350px, 1fr))")
                .set("gap", "32px")
                .set("max-width", "1200px")
                .set("margin", "0 auto");

        // Créer et ajouter les cartes de témoignages
        testimonialsGrid.add(
                createTestimonialCard(
                        "https://api.dicebear.com/7.x/avataaars/svg?seed=Amine",
                        "Amine El Fassi",
                        "Entrepreneur",
                        5,
                        "Une plateforme exceptionnelle ! J'ai organisé plusieurs événements et tout s'est parfaitement déroulé. L'interface est intuitive et le support réactif."
                ),
                createTestimonialCard(
                        "https://api.dicebear.com/7.x/avataaars/svg?seed=Sarah",
                        "Sarah Benkirane",
                        "Responsable Événements",
                        5,
                        "Depuis que j'utilise EventBooking, la gestion de mes événements est devenue un jeu d'enfant. Les statistiques en temps réel sont particulièrement utiles."
                ),
                createTestimonialCard(
                        "https://api.dicebear.com/7.x/avataaars/svg?seed=Karim",
                        "Karim Idrissi",
                        "Participant régulier",
                        5,
                        "Je fréquente des événements depuis 2 ans via cette plateforme. Jamais déçu ! La diversité des événements et la facilité de réservation sont impressionnantes."
                )
        );

        testimonialsSection.add(titleSection, testimonialsGrid);
        return testimonialsSection;
    }

    private Component createTestimonialCard(String avatarUrl, String name, String role, int rating, String text) {
        Div card = new Div();
        card.addClassNames("testimonial-card");
        card.getStyle()
                .set("background", "#f8fafc")
                .set("border-radius", "20px")
                .set("padding", "32px")
                .set("transition", "all 0.3s ease")
                .set("border", "1px solid #e2e8f0")
                .set("height", "100%");

        // Avatar and info
        HorizontalLayout header = new HorizontalLayout();
        header.setAlignItems(FlexComponent.Alignment.CENTER);
        header.setSpacing(true);
        header.getStyle().set("margin-bottom", "16px");

        Avatar avatar = new Avatar(name);
        avatar.setImage(avatarUrl);
        avatar.getStyle()
                .set("width", "56px")
                .set("height", "56px")
                .set("border", "3px solid white")
                .set("box-shadow", "0 4px 12px rgba(0,0,0,0.1)");

        VerticalLayout info = new VerticalLayout();
        info.setPadding(false);
        info.setSpacing(false);

        H4 userName = new H4(name);
        userName.getStyle()
                .set("margin", "0 0 4px 0")
                .set("color", "#1e293b")
                .set("font-size", "18px")
                .set("font-weight", "600");

        Span userRole = new Span(role);
        userRole.getStyle()
                .set("color", "#64748b")
                .set("font-size", "14px");

        info.add(userName, userRole);
        header.add(avatar, info);

        // Stars
        HorizontalLayout starsContainer = new HorizontalLayout();
        starsContainer.setSpacing(false);
        starsContainer.getStyle()
                .set("margin", "0 0 16px 0");

        for (int i = 0; i < 5; i++) {
            Icon star = i < rating ?
                    VaadinIcon.STAR.create() : VaadinIcon.STAR_O.create();
            star.setSize("20px");
            star.setColor("#fbbf24");
            starsContainer.add(star);
        }

        // Quote icon
        Icon quoteIcon = VaadinIcon.QUOTE_RIGHT.create();
        quoteIcon.setSize("32px");
        quoteIcon.setColor("#e2e8f0");
        quoteIcon.getStyle()
                .set("position", "absolute")
                .set("top", "24px")
                .set("right", "24px");

        // Text
        Paragraph testimonialText = new Paragraph(text);
        testimonialText.getStyle()
                .set("margin", "0")
                .set("color", "#475569")
                .set("font-size", "16px")
                .set("line-height", "1.7")
                .set("font-style", "italic");

        // Container pour le contenu
        Div content = new Div();
        content.getStyle()
                .set("position", "relative")
                .set("z-index", "1");

        content.add(quoteIcon, header, starsContainer, testimonialText);
        card.add(content);

        // Animation au hover
        card.getElement().addEventListener("mouseenter", e -> {
            card.getStyle()
                    .set("transform", "translateY(-8px)")
                    .set("box-shadow", "0 20px 40px rgba(0,0,0,0.12)")
                    .set("border-color", "#6366f1");
            quoteIcon.setColor("#6366f1");
            quoteIcon.getStyle().set("opacity", "0.3");
        });

        card.getElement().addEventListener("mouseleave", e -> {
            card.getStyle()
                    .set("transform", "translateY(0)")
                    .set("box-shadow", "none")
                    .set("border-color", "#e2e8f0");
            quoteIcon.setColor("#e2e8f0");
            quoteIcon.getStyle().remove("opacity");
        });

        return card;
    }

    // ==================== FOOTER ====================
    private Component createFooter() {
        Div footer = new Div();
        footer.addClassNames("footer", "animate-on-scroll");
        footer.setWidthFull();
        footer.getStyle()
                .set("background", "linear-gradient(135deg, #1e293b 0%, #0f172a 100%)")
                .set("color", "white")
                .set("padding", "64px 20px 32px 20px");

        Div content = new Div();
        content.getStyle()
                .set("max-width", "1200px")
                .set("margin", "0 auto");

        // Main footer content
        Div mainContent = new Div();
        mainContent.getStyle()
                .set("display", "grid")
                .set("grid-template-columns", "repeat(auto-fit, minmax(250px, 1fr))")
                .set("gap", "48px")
                .set("margin-bottom", "48px");

        // Column 1: About
        Div aboutColumn = new Div();
        aboutColumn.getStyle().set("display", "flex").set("flex-direction", "column");

        HorizontalLayout logo = createLogoSection();
        logo.getStyle().set("margin-bottom", "24px");

        Paragraph aboutText = new Paragraph(
                "La plateforme de référence pour la réservation d'événements au Maroc. " +
                        "Nous connectons les passionnés avec les meilleures expériences."
        );
        aboutText.getStyle()
                .set("color", "#cbd5e1")
                .set("font-size", "15px")
                .set("line-height", "1.6")
                .set("margin", "0 0 24px 0");

        HorizontalLayout socialIcons = new HorizontalLayout();

        String[] socialColors = {"#3b5998", "#1da1f2", "#e1306c", "#ff0000"};
        VaadinIcon[] socialIconsList = {VaadinIcon.FACEBOOK, VaadinIcon.TWITTER, VaadinIcon.GLOBE, VaadinIcon.YOUTUBE};

        for (int i = 0; i < socialIconsList.length; i++) {
            Button socialBtn = new Button(socialIconsList[i].create());
            socialBtn.addClassNames("social-btn");
            socialBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
            socialBtn.getStyle()
                    .set("background", socialColors[i] + "20")
                    .set("color", "white")
                    .set("border-radius", "8px")
                    .set("padding", "8px")
                    .set("transition", "all 0.3s");

            socialBtn.addClickListener(e -> {});
            socialIcons.add(socialBtn);
        }

        aboutColumn.add(logo, aboutText, socialIcons);

        // Column 2: Quick Links
        Div linksColumn = new Div();
        linksColumn.getStyle().set("display", "flex").set("flex-direction", "column");

        H4 linksTitle = new H4("Liens rapides");
        linksTitle.getStyle()
                .set("margin", "0 0 20px 0")
                .set("color", "white")
                .set("font-size", "18px");

        String[] quickLinks = {"Accueil", "Événements", "Catégories", "À propos", "Contact"};
        for (String link : quickLinks) {
            Anchor anchor = new Anchor("#", link);
            anchor.addClassNames("footer-link");
            anchor.getStyle()
                    .set("color", "#cbd5e1")
                    .set("text-decoration", "none")
                    .set("margin-bottom", "12px")
                    .set("display", "block")
                    .set("transition", "color 0.2s");

            anchor.getElement().addEventListener("mouseenter", e ->
                    anchor.getStyle().set("color", "#6366f1"));
            anchor.getElement().addEventListener("mouseleave", e ->
                    anchor.getStyle().set("color", "#cbd5e1"));

            linksColumn.add(anchor);
        }

        // Column 3: Contact
        Div contactColumn = new Div();
        contactColumn.getStyle().set("display", "flex").set("flex-direction", "column");

        H4 contactTitle = new H4("Contactez-nous");
        contactTitle.getStyle()
                .set("margin", "0 0 20px 0")
                .set("color", "white")
                .set("font-size", "18px");

        String[] contacts = {
                "contact@eventbooking.ma",
                "+212 5 22 00 00 00",
                "123 Bd Mohammed V, Casablanca",
                "Lun-Ven: 9h-18h"
        };
        VaadinIcon[] contactIcons = {
                VaadinIcon.ENVELOPE,
                VaadinIcon.PHONE,
                VaadinIcon.MAP_MARKER,
                VaadinIcon.CLOCK
        };

        for (int i = 0; i < contacts.length; i++) {
            HorizontalLayout contactItem = new HorizontalLayout();
            contactItem.setAlignItems(FlexComponent.Alignment.CENTER);
            contactItem.getStyle().set("margin-bottom", "12px");

            Icon icon = contactIcons[i].create();
            icon.setSize("16px");
            icon.setColor("#94a3b8");

            Span text = new Span(contacts[i]);
            text.getStyle()
                    .set("color", "#cbd5e1")
                    .set("font-size", "14px");

            contactItem.add(icon, text);
            contactColumn.add(contactItem);
        }

        mainContent.add(aboutColumn, linksColumn, contactColumn);

        // Divider
        Div divider = new Div();
        divider.getStyle()
                .set("height", "1px")
                .set("background", "rgba(255,255,255,0.1)")
                .set("margin", "32px 0");

        // Copyright
        Div copyright = new Div();
        copyright.getStyle()
                .set("display", "flex")
                .set("justify-content", "space-between")
                .set("align-items", "center")
                .set("padding-top", "32px");

        Span copyrightText = new Span("© 2025 EventBooking. Tous droits réservés.");
        copyrightText.getStyle()
                .set("color", "#94a3b8")
                .set("font-size", "14px");

        HorizontalLayout legalLinks = new HorizontalLayout();

        String[] legal = {"Conditions d'utilisation", "Politique de confidentialité", "Mentions légales"};
        for (String legalText : legal) {
            Anchor legalLink = new Anchor("#", legalText);
            legalLink.getStyle()
                    .set("color", "#94a3b8")
                    .set("font-size", "14px")
                    .set("text-decoration", "none");
            legalLinks.add(legalLink);
        }

        copyright.add(copyrightText, legalLinks);

        content.add(mainContent, divider, copyright);
        footer.add(content);
        return footer;
    }

    // ==================== UTILITY METHODS ====================
    private User getCurrentUser() {
        try {
            Object userObj = VaadinSession.getCurrent().getAttribute("currentUser");
            return userObj instanceof User ? (User) userObj : null;
        } catch (Exception e) {
            return null;
        }
    }

    private String getDashboardRoute(UserRole role) {
        if (role == UserRole.ADMIN) return "admin/dashboard";
        if (role == UserRole.ORGANIZER) return "organizer/dashboard";
        return "client/dashboard";
    }

    private void scrollToTop() {
        getUI().ifPresent(ui -> ui.getPage().executeJs(
                "window.scrollTo({top: 0, behavior: 'smooth'});"
        ));
    }

    private void scrollToSection(String sectionId) {
        getUI().ifPresent(ui -> ui.getPage().executeJs(
                "const element = document.getElementById($0);" +
                        "if (element) {" +
                        "   element.scrollIntoView({" +
                        "       behavior: 'smooth'," +
                        "       block: 'start'" +
                        "   });" +
                        "}",
                sectionId
        ));
    }

    private void loadEvents() {
        allEvents = eventService.getPublishedEvents();
        displayEvents(allEvents);
    }

    private void filterEvents() {
        String keyword = searchField.getValue().toLowerCase();
        String category = categoryFilter.getValue();

        List<Event> filtered = allEvents.stream()
                .filter(e -> keyword.isEmpty() ||
                        e.getTitre().toLowerCase().contains(keyword) ||
                        (e.getDescription() != null && e.getDescription().toLowerCase().contains(keyword)))
                .filter(e -> category == null || category.equals("Toutes") ||
                        e.getCategorie().name().equals(category))
                .toList();

        displayEvents(filtered);
    }

    private void displayEvents(List<Event> events) {
        eventsContainer.removeAll();

        if (events.isEmpty()) {
            Div emptyState = new Div();
            emptyState.getStyle()
                    .set("text-align", "center")
                    .set("padding", "60px 20px")
                    .set("max-width", "500px")
                    .set("margin", "40px auto");

            Icon emptyIcon = VaadinIcon.CALENDAR_CLOCK.create();
            emptyIcon.setSize("64px");
            emptyIcon.setColor("#cbd5e1");

            H3 emptyTitle = new H3("Aucun événement trouvé");
            emptyTitle.getStyle()
                    .set("margin", "24px 0 12px 0")
                    .set("color", "#475569");

            Paragraph emptyText = new Paragraph(
                    "Essayez de modifier vos critères de recherche ou revenez plus tard."
            );
            emptyText.getStyle()
                    .set("color", "#64748b")
                    .set("margin", "0");

            emptyState.add(emptyIcon, emptyTitle, emptyText);
            eventsContainer.add(emptyState);
            return;
        }

        // Container pour les cartes sur la même ligne
        HorizontalLayout cardsLayout = new HorizontalLayout();
        cardsLayout.addClassName("events-cards-layout");
        cardsLayout.setWidthFull();
        cardsLayout.setSpacing(true);
        cardsLayout.setPadding(true);
        cardsLayout.getStyle()
                .set("flex-wrap", "wrap")
                .set("justify-content", "center")
                .set("gap", "24px")
                .set("overflow-x", "auto")
                .set("padding", "20px 0");

        // Ajouter chaque carte
        for (Event event : events) {
            EventCard eventCard = createEnhancedEventCard(event);
            eventCard.addClassNames("event-card", "scale-up");
            eventCard.getStyle()
                    .set("flex-shrink", "0")
                    .set("min-width", "320px")  // Largeur minimale pour chaque carte
                    .set("max-width", "350px"); // Largeur maximale pour chaque carte

            cardsLayout.add(eventCard);
        }

        eventsContainer.add(cardsLayout);
    }


    private EventCard createEnhancedEventCard(Event event) {
        String baseUrl = getBaseUrlFromService();

        EventCard card = new EventCard(event, baseUrl, eventService, reservationService, userService);

        // Style pour l'affichage en ligne
        card.getStyle()
                .set("transition", "all 0.3s")
                .set("border", "1px solid #e2e8f0")
                .set("border-radius", "16px")
                .set("overflow", "hidden")
                .set("width", "100%")
                .set("height", "500px") // Hauteur fixe pour uniformiser
                .set("display", "flex")
                .set("flex-direction", "column");

        // Style pour le contenu interne pour qu'il s'adapte
        card.getChildren().forEach(component -> {
            if (component.getElement().getClassList().contains("event-content")) {
                component.getElement().getStyle()
                        .set("flex-grow", "1")
                        .set("display", "flex")
                        .set("flex-direction", "column");
            }
        });

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