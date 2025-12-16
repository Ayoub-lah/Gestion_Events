package com.eventbooking.service;

import com.eventbooking.entity.Reservation;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;

@Service
public class HtmlReceiptService {

    public String generateReceiptHtml(Reservation reservation) {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        String lieu = reservation.getEvenement().getLieu() != null ?
                reservation.getEvenement().getLieu() + ", " + reservation.getEvenement().getVille() :
                reservation.getEvenement().getVille();

        double prixUnitaire = reservation.getMontantTotal() / reservation.getNombrePlaces();

        String telephone = reservation.getUtilisateur().getTelephone() != null ?
                reservation.getUtilisateur().getTelephone() : "Non renseigné";

        String commentSection = reservation.getCommentaire() != null && !reservation.getCommentaire().isEmpty() ?
                "<p><strong>Commentaire:</strong> " + reservation.getCommentaire() + "</p>" : "";

        String html = "<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head>\n" +
                "    <meta charset='UTF-8'>\n" +
                "    <title>Reçu de Réservation - " + reservation.getCodeReservation() + "</title>\n" +
                "    <style>\n" +
                "        @media print {\n" +
                "            .no-print { display: none !important; }\n" +
                "            body { margin: 0; padding: 0; }\n" +
                "            .receipt { box-shadow: none; }\n" +
                "            .print-button { display: none; }\n" +
                "        }\n" +
                "        body { font-family: 'Arial', sans-serif; margin: 0; padding: 20px; background: #f5f5f5; }\n" +
                "        .receipt { max-width: 800px; margin: 0 auto; background: white; padding: 40px; border-radius: 10px; box-shadow: 0 4px 20px rgba(0,0,0,0.1); }\n" +
                "        .header { text-align: center; border-bottom: 2px solid #667eea; padding-bottom: 20px; margin-bottom: 30px; }\n" +
                "        .header h1 { color: #667eea; margin: 0 0 10px 0; font-size: 32px; }\n" +
                "        .company-info { color: #666; line-height: 1.6; }\n" +
                "        .company-name { font-weight: bold; color: #333; font-size: 18px; }\n" +
                "        .section { margin-bottom: 30px; }\n" +
                "        .section-title { color: #667eea; border-bottom: 2px solid #e0e0e0; padding-bottom: 10px; margin-bottom: 20px; font-size: 20px; }\n" +
                "        table { width: 100%; border-collapse: collapse; margin-bottom: 20px; }\n" +
                "        th { background: #f8f9fa; padding: 12px 15px; text-align: left; font-weight: bold; border: 1px solid #ddd; color: #555; }\n" +
                "        td { padding: 12px 15px; border: 1px solid #ddd; color: #333; }\n" +
                "        .total-row { background: #667eea; color: white; }\n" +
                "        .total-row th, .total-row td { border-color: #5566bb; font-weight: bold; font-size: 16px; }\n" +
                "        .footer { text-align: center; margin-top: 50px; padding-top: 30px; border-top: 2px solid #e0e0e0; color: #666; }\n" +
                "        .print-button { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; padding: 12px 30px; border: none; border-radius: 8px; cursor: pointer; margin: 20px 0; font-size: 16px; font-weight: bold; transition: all 0.3s; }\n" +
                "        .print-button:hover { transform: translateY(-2px); box-shadow: 0 6px 12px rgba(102, 126, 234, 0.3); }\n" +
                "        .conditions { background: #f8f9fa; padding: 20px; border-radius: 8px; border-left: 4px solid #667eea; }\n" +
                "        .conditions ul { margin: 10px 0; padding-left: 20px; }\n" +
                "        .conditions li { margin-bottom: 8px; color: #555; }\n" +
                "        .highlight { background: #fff8e1; padding: 15px; border-radius: 8px; border: 1px solid #ffd54f; margin: 20px 0; }\n" +
                "        .highlight-code { font-family: 'Courier New', monospace; font-size: 24px; font-weight: bold; color: #667eea; letter-spacing: 2px; }\n" +
                "        .generation-date { color: #999; font-size: 12px; text-align: center; margin-top: 20px; }\n" +
                "        .watermark { position: fixed; top: 50%; left: 50%; transform: translate(-50%, -50%) rotate(-45deg); font-size: 100px; color: rgba(102, 126, 234, 0.1); pointer-events: none; z-index: -1; font-weight: bold; }\n" +
                "    </style>\n" +
                "</head>\n" +
                "<body>\n" +
                "    <div class='watermark'>EventBooking</div>\n" +
                "    \n" +
                "    <div class='receipt'>\n" +
                "        <div class='header'>\n" +
                "            <h1>REÇU DE RÉSERVATION</h1>\n" +
                "            <p class='company-name'>EventBooking</p>\n" +
                "            <p class='company-info'>\n" +
                "                123 Avenue Mohammed V, Casablanca<br>\n" +
                "                Tél: +212 5 22 00 00 00 | Email: contact@eventbooking.ma<br>\n" +
                "                Site web: www.eventbooking.ma\n" +
                "            </p>\n" +
                "        </div>\n" +
                "        \n" +
                "        <div class='highlight'>\n" +
                "            <p style='margin: 0 0 10px 0; color: #555;'><strong>Code de réservation:</strong></p>\n" +
                "            <p class='highlight-code'>" + reservation.getCodeReservation() + "</p>\n" +
                "        </div>\n" +
                "        \n" +
                "        <div class='section'>\n" +
                "            <h3 class='section-title'>INFORMATIONS DE LA RÉSERVATION</h3>\n" +
                "            <table>\n" +
                "                <tr>\n" +
                "                    <th style='width: 30%;'>Date de réservation:</th>\n" +
                "                    <td>" + reservation.getDateReservation().format(dateFormatter) + "</td>\n" +
                "                </tr>\n" +
                "                <tr>\n" +
                "                    <th>Statut:</th>\n" +
                "                    <td><span style='color: #38a169; font-weight: bold;'>✓ " + reservation.getStatut() + "</span></td>\n" +
                "                </tr>\n" +
                "            </table>\n" +
                "        </div>\n" +
                "        \n" +
                "        <div class='section'>\n" +
                "            <h3 class='section-title'>DÉTAILS DE L'ÉVÉNEMENT</h3>\n" +
                "            <table>\n" +
                "                <tr>\n" +
                "                    <th style='width: 30%;'>Événement:</th>\n" +
                "                    <td>" + reservation.getEvenement().getTitre() + "</td>\n" +
                "                </tr>\n" +
                "                <tr>\n" +
                "                    <th>Date et heure:</th>\n" +
                "                    <td>" + reservation.getEvenement().getDateDebut().format(dateFormatter) + "</td>\n" +
                "                </tr>\n" +
                "                <tr>\n" +
                "                    <th>Lieu:</th>\n" +
                "                    <td>" + lieu + "</td>\n" +
                "                </tr>\n" +
                "                <tr>\n" +
                "                    <th>Catégorie:</th>\n" +
                "                    <td><span style='background: #e0e7ff; color: #4f46e5; padding: 4px 10px; border-radius: 12px; font-size: 12px;'>" +
                reservation.getEvenement().getCategorie() + "</span></td>\n" +
                "                </tr>\n" +
                "                <tr>\n" +
                "                    <th>Organisateur:</th>\n" +
                "                    <td>" + (reservation.getEvenement().getOrganisateur() != null ?
                reservation.getEvenement().getOrganisateur().getNom() + " " +
                        reservation.getEvenement().getOrganisateur().getPrenom() : "Non spécifié") + "</td>\n" +
                "                </tr>\n" +
                "            </table>\n" +
                "        </div>\n" +
                "        \n" +
                "        <div class='section'>\n" +
                "            <h3 class='section-title'>DÉTAILS DE PAIEMENT</h3>\n" +
                "            <table>\n" +
                "                <tr>\n" +
                "                    <th style='width: 30%;'>Nombre de places:</th>\n" +
                "                    <td>" + reservation.getNombrePlaces() + " place" + (reservation.getNombrePlaces() > 1 ? "s" : "") + "</td>\n" +
                "                </tr>\n" +
                "                <tr>\n" +
                "                    <th>Prix unitaire:</th>\n" +
                "                    <td>" + String.format("%.2f", prixUnitaire) + " MAD</td>\n" +
                "                </tr>\n" +
                "                <tr class='total-row'>\n" +
                "                    <th>TOTAL:</th>\n" +
                "                    <td style='text-align: right; font-size: 20px;'>" + String.format("%.2f", reservation.getMontantTotal()) + " MAD</td>\n" +
                "                </tr>\n" +
                "            </table>\n" +
                "            <p style='color: #666; font-size: 13px; text-align: right; margin-top: -10px;'>\n" +
                "                Toutes taxes comprises\n" +
                "            </p>\n" +
                "        </div>\n" +
                "        \n" +
                "        <div class='section'>\n" +
                "            <h3 class='section-title'>INFORMATIONS DU CLIENT</h3>\n" +
                "            <table>\n" +
                "                <tr>\n" +
                "                    <th style='width: 30%;'>Nom complet:</th>\n" +
                "                    <td>" + reservation.getUtilisateur().getPrenom() + " " + reservation.getUtilisateur().getNom() + "</td>\n" +
                "                </tr>\n" +
                "                <tr>\n" +
                "                    <th>Email:</th>\n" +
                "                    <td>" + reservation.getUtilisateur().getEmail() + "</td>\n" +
                "                </tr>\n" +
                "                <tr>\n" +
                "                    <th>Téléphone:</th>\n" +
                "                    <td>" + telephone + "</td>\n" +
                "                </tr>\n" +
                "                <tr>\n" +
                "                    <th>ID Client:</th>\n" +
                "                    <td>" + reservation.getUtilisateur().getId() + "</td>\n" +
                "                </tr>\n" +
                "            </table>\n" +
                "        </div>\n" +
                "        \n" +
                "        <div class='section'>\n" +
                "            <h3 class='section-title'>CONDITIONS ET REMARQUES</h3>\n" +
                "            <div class='conditions'>\n" +
                "                <ul>\n" +
                "                    <li>Ce reçu est valable comme justificatif de paiement.</li>\n" +
                "                    <li>Présentez ce reçu à l'entrée de l'événement.</li>\n" +
                "                    <li>Les réservations sont confirmées après paiement.</li>\n" +
                "                    <li>Les annulations sont acceptées jusqu'à 48h avant l'événement.</li>\n" +
                "                    <li>En cas d'annulation, veuillez contacter notre service client.</li>\n" +
                "                    <li>Le non-respect des horaires peut entraîner le refus d'entrée.</li>\n" +
                "                </ul>\n" +
                "                " + commentSection + "\n" +
                "                <p style='margin-top: 15px; color: #666; font-style: italic;'>\n" +
                "                    <strong>Note:</strong> Conservez ce reçu jusqu'à la fin de l'événement.\n" +
                "                </p>\n" +
                "            </div>\n" +
                "        </div>\n" +
                "        \n" +
                "        <div class='footer'>\n" +
                "            <button class='print-button no-print' onclick='window.print()'>📄 Imprimer le reçu</button>\n" +
                "            <p style='font-size: 18px; color: #667eea; font-weight: bold; margin: 20px 0;'>\n" +
                "                Merci pour votre confiance !\n" +
                "            </p>\n" +
                "            <p style='color: #666; line-height: 1.6;'>\n" +
                "                EventBooking - Votre plateforme d'événements préférée<br>\n" +
                "                Service client disponible du lundi au vendredi, 9h-18h\n" +
                "            </p>\n" +
                "            <p class='generation-date'>\n" +
                "                Document généré le " + java.time.LocalDateTime.now().format(dateFormatter) + "<br>\n" +
                "                ID du document: REC-" + reservation.getId() + "-" + System.currentTimeMillis() + "\n" +
                "            </p>\n" +
                "        </div>\n" +
                "    </div>\n" +
                "    \n" +
                "    <script>\n" +
                "        // Auto-print option (uncomment if needed)\n" +
                "        // window.onload = function() {\n" +
                "        //     window.print();\n" +
                "        // }\n" +
                "        \n" +
                "        // Copy to clipboard function\n" +
                "        function copyReservationCode() {\n" +
                "            const code = '" + reservation.getCodeReservation() + "';\n" +
                "            navigator.clipboard.writeText(code).then(function() {\n" +
                "                alert('Code copié dans le presse-papier: ' + code);\n" +
                "            }, function(err) {\n" +
                "                console.error('Erreur lors de la copie: ', err);\n" +
                "            });\n" +
                "        }\n" +
                "    </script>\n" +
                "</body>\n" +
                "</html>";

        return html;
    }
}