package com.eventbooking.service;

import com.eventbooking.entity.Reservation;
import com.eventbooking.entity.User;
import com.eventbooking.entity.Event;
import com.eventbooking.entity.enums.ReservationStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public interface ReservationService {

    // Créer une nouvelle réservation
    Reservation createReservation(Reservation reservation);

    // Créer une réservation avec validation
    Reservation createReservation(Long eventId, Long userId, Integer numberOfPlaces, String comment);

    // Mettre à jour une réservation
    Reservation updateReservation(Long id, Reservation reservation);

    // Supprimer une réservation
    void deleteReservation(Long id);

    // Obtenir une réservation par son ID
    Optional<Reservation> getReservationById(Long id);

    // Obtenir une réservation par son code
    Optional<Reservation> getReservationByCode(String codeReservation);

    // Obtenir toutes les réservations
    List<Reservation> getAllReservations();

    // Obtenir les réservations d'un utilisateur
    List<Reservation> getReservationsByUser(Long userId);

    // Obtenir les réservations d'un événement
    List<Reservation> getReservationsByEvent(Long eventId);

    // Obtenir les réservations par statut
    List<Reservation> getReservationsByStatus(ReservationStatus status);

    // Confirmer une réservation
    Reservation confirmReservation(Long reservationId);

    // Annuler une réservation
    Reservation cancelReservation(Long reservationId, String reason);

    // Vérifier la disponibilité des places pour un événement
    boolean checkAvailability(Long eventId, Integer numberOfPlaces);

    // Calculer le montant total d'une réservation
    Double calculateTotalAmount(Long eventId, Integer numberOfPlaces);

    // Générer un code de réservation unique
    String generateReservationCode();

    // Méthodes de statistiques pour les dashboards
    long getTotalReservationsCount();

    long getUserReservationsCount(Long userId);

    long getUserConfirmedReservationsCount(Long userId);

    long getOrganizerReservationsCount(Long organizerId);

    double getTotalRevenue();

    double getOrganizerRevenue(Long organizerId);

    // Obtenir le nombre de réservations confirmées pour un événement
    Integer getConfirmedPlacesCountByEvent(Long eventId);

    // Vérifier si l'utilisateur a déjà réservé cet événement
    boolean hasUserReservedEvent(Long userId, Long eventId);

    // Obtenir les réservations à venir d'un utilisateur
    List<Reservation> getUpcomingReservationsByUser(Long userId);

    // Obtenir l'historique des réservations d'un utilisateur
    List<Reservation> getReservationHistoryByUser(Long userId);
}
