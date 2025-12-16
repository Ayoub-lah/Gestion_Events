    package com.eventbooking.entity;

    import com.eventbooking.entity.enums.UserRole;
    import jakarta.persistence.*;
    import jakarta.validation.constraints.Email;
    import jakarta.validation.constraints.NotBlank;
    import java.time.LocalDateTime;

    @Entity
    @Table(name = "users")
    public class User {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @NotBlank
        private String nom;

        @NotBlank
        private String prenom;

        @Email
        @Column(unique = true)
        private String email;

        private String password;

        @Enumerated(EnumType.STRING)
        private UserRole role;

        private LocalDateTime dateInscription;
        private Boolean actif = true;
        private String telephone;

        // Getters et Setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getNom() { return nom; }
        public void setNom(String nom) { this.nom = nom; }
        public String getPrenom() { return prenom; }
        public void setPrenom(String prenom) { this.prenom = prenom; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
        public UserRole getRole() { return role; }
        public void setRole(UserRole role) { this.role = role; }
        public LocalDateTime getDateInscription() { return dateInscription; }
        public void setDateInscription(LocalDateTime dateInscription) { this.dateInscription = dateInscription; }
        public Boolean getActif() { return actif; }
        public void setActif(Boolean actif) { this.actif = actif; }
        public String getTelephone() { return telephone; }
        public void setTelephone(String telephone) { this.telephone = telephone; }
    }