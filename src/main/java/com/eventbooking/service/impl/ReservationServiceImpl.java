package com.eventbooking.service.impl;

import com.eventbooking.entity.Reservation;
import com.eventbooking.entity.User;
import com.eventbooking.entity.Event;
import com.eventbooking.entity.enums.ReservationStatus;
import com.eventbooking.entity.enums.EventStatus;
import com.eventbooking.repository.ReservationRepository;
import com.eventbooking.repository.UserRepository;
import com.eventbooking.repository.EventRepository;
import com.eventbooking.service.ReservationService;
import com.eventbooking.util.ReservationCodeGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Random;

@Service
@Transactional
public class ReservationServiceImpl implements ReservationService {

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private ReservationCodeGenerator codeGenerator;

    private Random random = new Random();

    @Override
    public Reservation createReservation(Reservation reservation) {
        if (reservation.getEvenement() == null || reservation.getUtilisateur() == null) {
            throw new IllegalArgumentException("L'événement et l'utilisateur sont obligatoires");
        }

        if (reservation.getNombrePlaces() == null || reservation.getNombrePlaces() < 1) {
            throw new IllegalArgumentException("Le nombre de places doit être au moins 1");
        }

        if (!checkAvailability(reservation.getEvenement().getId(), reservation.getNombrePlaces())) {
            throw new IllegalStateException("Places insuffisantes pour cet événement");
        }

        if (reservation.getEvenement().getStatut() != EventStatus.PUBLIE) {
            throw new IllegalStateException("Impossible de réserver un événement non publié");
        }

        if (reservation.getEvenement().getDateDebut().isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("Impossible de réserver un événement déjà passé");
        }

        if (hasUserReservedEvent(reservation.getUtilisateur().getId(), reservation.getEvenement().getId())) {
            throw new IllegalStateException("Vous avez déjà réservé cet événement");
        }

        Double totalAmount = calculateTotalAmount(
                reservation.getEvenement().getId(),
                reservation.getNombrePlaces()
        );
        reservation.setMontantTotal(totalAmount);
        reservation.setCodeReservation(generateReservationCode());
        reservation.setDateReservation(LocalDateTime.now());
        reservation.setStatut(ReservationStatus.EN_ATTENTE);

        return reservationRepository.save(reservation);
    }

    @Override
    public Reservation createReservation(Long eventId, Long userId, Integer numberOfPlaces, String comment) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Événement non trouvé"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur non trouvé"));

        Reservation reservation = new Reservation();
        reservation.setEvenement(event);
        reservation.setUtilisateur(user);
        reservation.setNombrePlaces(numberOfPlaces);
        reservation.setCommentaire(comment);

        return createReservation(reservation);
    }

    @Override
    public Reservation updateReservation(Long id, Reservation reservation) {
        Reservation existingReservation = reservationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Réservation non trouvée"));

        if (reservation.getCommentaire() != null) {
            existingReservation.setCommentaire(reservation.getCommentaire());
        }

        return reservationRepository.save(existingReservation);
    }

    @Override
    public void deleteReservation(Long id) {
        // ✅ CORRECTION: Permettre la suppression de n'importe quelle réservation (admin)
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Réservation non trouvée"));

        reservationRepository.deleteById(id);
    }

    @Override
    public Optional<Reservation> getReservationById(Long id) {
        return reservationRepository.findById(id);
    }

    @Override
    public Optional<Reservation> getReservationByCode(String codeReservation) {
        return reservationRepository.findByCodeReservation(codeReservation);
    }

    @Override
    public List<Reservation> getAllReservations() {
        return reservationRepository.findAll();
    }

    @Override
    public List<Reservation> getReservationsByUser(Long userId) {
        return reservationRepository.findByUtilisateurId(userId);
    }

    @Override
    public List<Reservation> getReservationsByEvent(Long eventId) {
        return reservationRepository.findByEvenementId(eventId);
    }

    @Override
    public List<Reservation> getReservationsByStatus(ReservationStatus status) {
        return reservationRepository.findByStatut(status);
    }

    @Override
    public Reservation confirmReservation(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("Réservation non trouvée"));

        if (!checkAvailability(reservation.getEvenement().getId(), reservation.getNombrePlaces())) {
            throw new IllegalStateException("Places insuffisantes pour confirmer cette réservation");
        }

        reservation.setStatut(ReservationStatus.CONFIRMEE);
        return reservationRepository.save(reservation);
    }

    @Override
    public Reservation cancelReservation(Long reservationId, String reason) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("Réservation non trouvée"));

        reservation.setStatut(ReservationStatus.ANNULEE);
        if (reason != null && !reason.trim().isEmpty()) {
            reservation.setCommentaire((reservation.getCommentaire() != null ?
                    reservation.getCommentaire() + " | " : "") + "Annulation: " + reason);
        }

        return reservationRepository.save(reservation);
    }

    @Override
    public boolean checkAvailability(Long eventId, Integer numberOfPlaces) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Événement non trouvé"));

        Integer reservedPlaces = getConfirmedPlacesCountByEvent(eventId);
        int availablePlaces = event.getCapaciteMax() - (reservedPlaces != null ? reservedPlaces : 0);

        return availablePlaces >= numberOfPlaces;
    }

    @Override
    public Double calculateTotalAmount(Long eventId, Integer numberOfPlaces) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Événement non trouvé"));

        return event.getPrixUnitaire() * numberOfPlaces;
    }

    @Override
    public String generateReservationCode() {
        String code;
        do {
            code = codeGenerator.generateCode();
        } while (reservationRepository.findByCodeReservation(code).isPresent());

        return code;
    }

    @Override
    public long getTotalReservationsCount() {
        return reservationRepository.count();
    }

    @Override
    public long getUserReservationsCount(Long userId) {
        return reservationRepository.countByUtilisateurId(userId);
    }

    @Override
    public long getUserConfirmedReservationsCount(Long userId) {
        return reservationRepository.countByUtilisateurIdAndStatut(userId, ReservationStatus.CONFIRMEE);
    }

    @Override
    public long getOrganizerReservationsCount(Long organizerId) {
        return reservationRepository.countByEvenementOrganisateurId(organizerId);
    }

    @Override
    public double getTotalRevenue() {
        Double revenue = reservationRepository.sumMontantTotalByStatut(ReservationStatus.CONFIRMEE);
        return revenue != null ? revenue : 0.0;
    }

    @Override
    public double getOrganizerRevenue(Long organizerId) {
        Double revenue = reservationRepository.sumMontantTotalByOrganizerAndStatut(organizerId, ReservationStatus.CONFIRMEE);
        return revenue != null ? revenue : 0.0;
    }

    @Override
    public Integer getConfirmedPlacesCountByEvent(Long eventId) {
        Integer count = reservationRepository.sumNombrePlacesByEventAndStatut(eventId, ReservationStatus.CONFIRMEE);
        return count != null ? count : 0;
    }

    @Override
    public boolean hasUserReservedEvent(Long userId, Long eventId) {
        return reservationRepository.existsByUtilisateurIdAndEvenementIdAndStatutIn(
                userId, eventId, List.of(ReservationStatus.EN_ATTENTE, ReservationStatus.CONFIRMEE));
    }

    @Override
    public List<Reservation> getUpcomingReservationsByUser(Long userId) {
        return reservationRepository.findUpcomingReservationsByUser(userId, LocalDateTime.now());
    }

    @Override
    public List<Reservation> getReservationHistoryByUser(Long userId) {
        return reservationRepository.findPastReservationsByUser(userId, LocalDateTime.now());
    }
}