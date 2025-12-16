package com.eventbooking.entity;

import com.eventbooking.entity.enums.EventCategory;
import com.eventbooking.entity.enums.EventStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

@Entity
@Table(name = "events")
public class Event {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(min = 5, max = 100)
    private String titre;

    @Size(max = 1000)
    private String description;

    @Enumerated(EnumType.STRING)
    private EventCategory categorie;

    @Future
    private LocalDateTime dateDebut;

    private LocalDateTime dateFin;
    private String lieu;
    private String ville;
    private Integer capaciteMax;
    private Double prixUnitaire;
    private String imageUrl;

    @ManyToOne
    @JoinColumn(name = "organisateur_id")
    private User organisateur;

    @Enumerated(EnumType.STRING)
    private EventStatus statut = EventStatus.BROUILLON;

    private LocalDateTime dateCreation;
    private LocalDateTime dateModification;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitre() { return titre; }
    public void setTitre(String titre) { this.titre = titre; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public EventCategory getCategorie() { return categorie; }
    public void setCategorie(EventCategory categorie) { this.categorie = categorie; }
    public LocalDateTime getDateDebut() { return dateDebut; }
    public void setDateDebut(LocalDateTime dateDebut) { this.dateDebut = dateDebut; }
    public LocalDateTime getDateFin() { return dateFin; }
    public void setDateFin(LocalDateTime dateFin) { this.dateFin = dateFin; }
    public String getLieu() { return lieu; }
    public void setLieu(String lieu) { this.lieu = lieu; }
    public String getVille() { return ville; }
    public void setVille(String ville) { this.ville = ville; }
    public Integer getCapaciteMax() { return capaciteMax; }
    public void setCapaciteMax(Integer capaciteMax) { this.capaciteMax = capaciteMax; }
    public Double getPrixUnitaire() { return prixUnitaire; }
    public void setPrixUnitaire(Double prixUnitaire) { this.prixUnitaire = prixUnitaire; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public User getOrganisateur() { return organisateur; }
    public void setOrganisateur(User organisateur) { this.organisateur = organisateur; }
    public EventStatus getStatut() { return statut; }
    public void setStatut(EventStatus statut) { this.statut = statut; }
    public LocalDateTime getDateCreation() { return dateCreation; }
    public void setDateCreation(LocalDateTime dateCreation) { this.dateCreation = dateCreation; }
    public LocalDateTime getDateModification() { return dateModification; }
    public void setDateModification(LocalDateTime dateModification) { this.dateModification = dateModification; }
}