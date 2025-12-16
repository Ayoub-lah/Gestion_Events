package com.eventbooking.repository;

import com.eventbooking.entity.Event;
import com.eventbooking.entity.enums.EventCategory;
import com.eventbooking.entity.enums.EventStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {

    // Trouver les événements par organisateur
    List<Event> findByOrganisateurId(Long organisateurId);

    // Trouver les événements par statut
    List<Event> findByStatut(EventStatus statut);

    // Trouver les événements par catégorie
    List<Event> findByCategorie(EventCategory categorie);

    // Trouver les événements par ville
    List<Event> findByVille(String ville);

    // Trouver les événements publiés et à venir
    @Query("SELECT e FROM Event e WHERE e.statut = 'PUBLIE' AND e.dateDebut > :currentDate ORDER BY e.dateDebut ASC")
    List<Event> findUpcomingPublishedEvents(@Param("currentDate") LocalDateTime currentDate);

    // Trouver les événements publiés par catégorie
    @Query("SELECT e FROM Event e WHERE e.statut = 'PUBLIE' AND e.categorie = :category AND e.dateDebut > :currentDate ORDER BY e.dateDebut ASC")
    List<Event> findPublishedEventsByCategory(@Param("category") EventCategory category,
                                              @Param("currentDate") LocalDateTime currentDate);

    // Trouver les événements par titre (recherche)
    @Query("SELECT e FROM Event e WHERE e.statut = 'PUBLIE' AND LOWER(e.titre) LIKE LOWER(CONCAT('%', :titre, '%'))")
    List<Event> findByTitreContainingIgnoreCase(@Param("titre") String titre);

    // Compter les événements par statut
    Long countByStatut(EventStatus statut);

    // Trouver les événements entre deux dates
    @Query("SELECT e FROM Event e WHERE e.dateDebut BETWEEN :startDate AND :endDate AND e.statut = 'PUBLIE'")
    List<Event> findEventsBetweenDates(@Param("startDate") LocalDateTime startDate,
                                       @Param("endDate") LocalDateTime endDate);

    // Vérifier si un événement existe avec le même titre et date
    @Query("SELECT COUNT(e) > 0 FROM Event e WHERE e.titre = :titre AND e.dateDebut = :dateDebut")
    boolean existsByTitreAndDateDebut(@Param("titre") String titre,
                                      @Param("dateDebut") LocalDateTime dateDebut);
}