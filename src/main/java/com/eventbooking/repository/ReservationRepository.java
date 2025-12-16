package com.eventbooking.repository;

import com.eventbooking.entity.Reservation;
import com.eventbooking.entity.enums.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    List<Reservation> findByUtilisateurId(Long userId);

    List<Reservation> findByEvenementId(Long eventId);

    List<Reservation> findByStatut(ReservationStatus statut);

    Optional<Reservation> findByCodeReservation(String codeReservation);

    // Méthodes de comptage
    Long countByUtilisateurId(Long userId);

    Long countByUtilisateurIdAndStatut(Long userId, ReservationStatus statut);

    Long countByEvenementOrganisateurId(Long organizerId);

    // Méthodes de somme pour les statistiques
    @Query("SELECT SUM(r.montantTotal) FROM Reservation r WHERE r.statut = :statut")
    Double sumMontantTotalByStatut(@Param("statut") ReservationStatus statut);

    @Query("SELECT SUM(r.montantTotal) FROM Reservation r WHERE r.evenement.organisateur.id = :organizerId AND r.statut = :statut")
    Double sumMontantTotalByOrganizerAndStatut(@Param("organizerId") Long organizerId,
                                               @Param("statut") ReservationStatus statut);

    @Query("SELECT SUM(r.nombrePlaces) FROM Reservation r WHERE r.evenement.id = :eventId AND r.statut = :statut")
    Integer sumNombrePlacesByEventAndStatut(@Param("eventId") Long eventId,
                                            @Param("statut") ReservationStatus statut);

    // Vérifier l'existence d'une réservation
    @Query("SELECT COUNT(r) > 0 FROM Reservation r WHERE r.utilisateur.id = :userId AND r.evenement.id = :eventId AND r.statut IN :statusList")
    boolean existsByUtilisateurIdAndEvenementIdAndStatutIn(@Param("userId") Long userId,
                                                           @Param("eventId") Long eventId,
                                                           @Param("statusList") List<ReservationStatus> statusList);

    // Réservations à venir d'un utilisateur
    @Query("SELECT r FROM Reservation r WHERE r.utilisateur.id = :userId AND r.evenement.dateDebut > :currentDate ORDER BY r.evenement.dateDebut ASC")
    List<Reservation> findUpcomingReservationsByUser(@Param("userId") Long userId,
                                                     @Param("currentDate") LocalDateTime currentDate);

    // Historique des réservations d'un utilisateur
    @Query("SELECT r FROM Reservation r WHERE r.utilisateur.id = :userId AND r.evenement.dateDebut <= :currentDate ORDER BY r.evenement.dateDebut DESC")
    List<Reservation> findPastReservationsByUser(@Param("userId") Long userId,
                                                 @Param("currentDate") LocalDateTime currentDate);

    // Réservations confirmées pour un événement (méthode existante)
    @Query("SELECT r FROM Reservation r WHERE r.evenement.id = :eventId AND r.statut = 'CONFIRMEE'")
    List<Reservation> findConfirmedReservationsByEvent(@Param("eventId") Long eventId);

    // Nombre de places confirmées pour un événement (méthode existante)
    @Query("SELECT SUM(r.nombrePlaces) FROM Reservation r WHERE r.evenement.id = :eventId AND r.statut = 'CONFIRMEE'")
    Integer countConfirmedPlacesByEvent(@Param("eventId") Long eventId);
}