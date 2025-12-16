package com.eventbooking.entity;

import com.eventbooking.entity.enums.ReservationStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.time.LocalDateTime;

@Entity
@Table(name = "reservations")
public class Reservation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "utilisateur_id")
    private User utilisateur;

    @ManyToOne
    @JoinColumn(name = "evenement_id")
    private Event evenement;

    @Min(1)
    @Max(10)
    private Integer nombrePlaces;

    private Double montantTotal;
    private LocalDateTime dateReservation;

    @Enumerated(EnumType.STRING)
    private ReservationStatus statut = ReservationStatus.EN_ATTENTE;

    @Column(unique = true)
    private String codeReservation;

    private String commentaire;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public User getUtilisateur() { return utilisateur; }
    public void setUtilisateur(User utilisateur) { this.utilisateur = utilisateur; }
    public Event getEvenement() { return evenement; }
    public void setEvenement(Event evenement) { this.evenement = evenement; }
    public Integer getNombrePlaces() { return nombrePlaces; }
    public void setNombrePlaces(Integer nombrePlaces) { this.nombrePlaces = nombrePlaces; }
    public Double getMontantTotal() { return montantTotal; }
    public void setMontantTotal(Double montantTotal) { this.montantTotal = montantTotal; }
    public LocalDateTime getDateReservation() { return dateReservation; }
    public void setDateReservation(LocalDateTime dateReservation) { this.dateReservation = dateReservation; }
    public ReservationStatus getStatut() { return statut; }
    public void setStatut(ReservationStatus statut) { this.statut = statut; }
    public String getCodeReservation() { return codeReservation; }
    public void setCodeReservation(String codeReservation) { this.codeReservation = codeReservation; }
    public String getCommentaire() { return commentaire; }
    public void setCommentaire(String commentaire) { this.commentaire = commentaire; }
}