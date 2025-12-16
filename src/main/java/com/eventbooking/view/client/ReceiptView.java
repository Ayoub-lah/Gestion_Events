package com.eventbooking.view.client;

import com.eventbooking.entity.Reservation;
import com.eventbooking.entity.User;
import com.eventbooking.service.HtmlReceiptService;
import com.eventbooking.service.ReservationService;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.router.*;
import com.vaadin.flow.server.VaadinSession;
import org.springframework.beans.factory.annotation.Autowired;

@Route("receipt/:id")
@PageTitle("Reçu de Réservation")
public class ReceiptView extends Div implements BeforeEnterObserver {

    private final ReservationService reservationService;
    private final HtmlReceiptService htmlReceiptService;

    @Autowired
    public ReceiptView(ReservationService reservationService,
                       HtmlReceiptService htmlReceiptService) {
        this.reservationService = reservationService;
        this.htmlReceiptService = htmlReceiptService;

        setSizeFull();
        getStyle().set("overflow", "auto");
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        try {
            // Récupérer l'ID depuis les paramètres
            String idParam = event.getRouteParameters().get("id").orElse(null);
            if (idParam == null) {
                showError("ID de réservation manquant");
                return;
            }

            Long reservationId = Long.parseLong(idParam);

            // Vérifier l'authentification
            Object userObj = VaadinSession.getCurrent().getAttribute("currentUser");
            if (!(userObj instanceof User)) {
                getUI().ifPresent(ui -> ui.navigate("login"));
                return;
            }

            User currentUser = (User) userObj;

            // Récupérer la réservation
            var reservationOpt = reservationService.getReservationById(reservationId);
            if (reservationOpt.isEmpty()) {
                showError("Réservation non trouvée");
                return;
            }

            Reservation reservation = reservationOpt.get();

            // Vérifier que l'utilisateur est propriétaire
            if (!reservation.getUtilisateur().getId().equals(currentUser.getId())) {
                showError("Accès non autorisé");
                return;
            }

            // Générer et afficher le HTML
            String htmlContent = htmlReceiptService.generateReceiptHtml(reservation);

            // Créer un iframe pour afficher le reçu
            getElement().executeJs("""
                const iframe = document.createElement('iframe');
                iframe.style.width = '100%';
                iframe.style.height = '100vh';
                iframe.style.border = 'none';
                iframe.srcdoc = $0;
                this.appendChild(iframe);
                
                // Bouton d'impression en dehors de l'iframe
                const printBtn = document.createElement('button');
                printBtn.textContent = '🖨️ Imprimer le reçu';
                printBtn.style.position = 'fixed';
                printBtn.style.top = '10px';
                printBtn.style.right = '10px';
                printBtn.style.zIndex = '1000';
                printBtn.style.padding = '10px 20px';
                printBtn.style.background = '#667eea';
                printBtn.style.color = 'white';
                printBtn.style.border = 'none';
                printBtn.style.borderRadius = '5px';
                printBtn.style.cursor = 'pointer';
                printBtn.style.boxShadow = '0 2px 10px rgba(0,0,0,0.2)';
                printBtn.onclick = function() {
                    iframe.contentWindow.print();
                };
                this.appendChild(printBtn);
                
                // Bouton retour
                const backBtn = document.createElement('button');
                backBtn.textContent = '← Retour';
                backBtn.style.position = 'fixed';
                backBtn.style.top = '10px';
                backBtn.style.left = '10px';
                backBtn.style.zIndex = '1000';
                backBtn.style.padding = '10px 20px';
                backBtn.style.background = '#f8f9fa';
                backBtn.style.color = '#333';
                backBtn.style.border = '1px solid #ddd';
                backBtn.style.borderRadius = '5px';
                backBtn.style.cursor = 'pointer';
                backBtn.onclick = function() {
                    window.history.back();
                };
                this.appendChild(backBtn);
            """, htmlContent);

        } catch (NumberFormatException e) {
            showError("ID de réservation invalide");
        } catch (Exception e) {
            e.printStackTrace();
            showError("Erreur lors du chargement du reçu");
        }
    }

    private void showError(String message) {
        getElement().setProperty("innerHTML",
                "<div style='padding: 40px; text-align: center;'>" +
                        "<h3 style='color: #e53e3e;'>Erreur</h3>" +
                        "<p>" + message + "</p>" +
                        "<button onclick='window.history.back()' style='padding: 10px 20px; background: #667eea; color: white; border: none; border-radius: 5px; cursor: pointer;'>Retour</button>" +
                        "</div>");
    }
}