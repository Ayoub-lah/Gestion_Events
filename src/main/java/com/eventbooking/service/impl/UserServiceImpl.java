package com.eventbooking.service.impl;

import com.eventbooking.entity.User;
import com.eventbooking.entity.enums.UserRole;
import com.eventbooking.repository.UserRepository;
import com.eventbooking.repository.ReservationRepository;
import com.eventbooking.repository.EventRepository;
import com.eventbooking.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private EventRepository eventRepository;

    private BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public long getUserReservationsCount(Long userId) {
        return reservationRepository.countByUtilisateurId(userId);
    }

    @Override
    public long getUserConfirmedReservationsCount(Long userId) {
        return reservationRepository.countByUtilisateurIdAndStatut(
                userId,
                com.eventbooking.entity.enums.ReservationStatus.CONFIRMEE
        );
    }

    @Override
    public long getTotalUsersCount() {
        return userRepository.count();
    }

    @Override
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        // Vérifier si c'est l'utilisateur actuellement connecté
        User currentUser = getCurrentUser();
        if (currentUser != null && currentUser.getId().equals(id)) {
            throw new IllegalStateException("Vous ne pouvez pas supprimer votre propre compte");
        }

        // Vérifier les réservations associées (pour tous les utilisateurs)
        long reservationCount = reservationRepository.countByUtilisateurId(id);

        // Vérifier les événements associés (pour les organisateurs)
        long eventCount = eventRepository.findByOrganisateurId(id).size();

        // Construire un message d'erreur personnalisé
        StringBuilder errorMessage = new StringBuilder();

        if (reservationCount > 0) {
            errorMessage.append("Impossible de supprimer cet utilisateur car ")
                    .append(reservationCount)
                    .append(" réservation(s) lui sont associée(s). ");
        }

        if (eventCount > 0) {
            errorMessage.append("Impossible de supprimer cet utilisateur car ")
                    .append(eventCount)
                    .append(" événement(s) lui sont associé(s). ");
        }

        // Si des dépendances existent, lancer une exception avec le message détaillé
        if (errorMessage.length() > 0) {
            throw new IllegalStateException(errorMessage.toString());
        }

        // Si aucune dépendance, supprimer l'utilisateur
        userRepository.deleteById(id);
    }

    // Méthode pour récupérer l'utilisateur connecté
    private User getCurrentUser() {
        // Implémentez cette méthode selon votre système d'authentification
        // Par exemple, si vous utilisez VaadinSession :
        try {
            com.vaadin.flow.server.VaadinSession session = com.vaadin.flow.server.VaadinSession.getCurrent();
            if (session != null && session.getAttribute("currentUser") instanceof User) {
                return (User) session.getAttribute("currentUser");
            }
        } catch (Exception e) {
            // Gérer l'exception si VaadinSession n'est pas disponible
        }
        return null;
    }


    @Override
    public boolean authenticate(String email, String password) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            return user.getActif() && passwordEncoder.matches(password, user.getPassword());
        }
        return false;
    }

    @Override
    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Override
    public User registerNewUser(String nom, String prenom, String email, String password, String telephone, UserRole role) {
        User user = new User();
        user.setNom(nom);
        user.setPrenom(prenom);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setTelephone(telephone);
        user.setRole(role);
        user.setDateInscription(LocalDateTime.now());
        user.setActif(true);

        return userRepository.save(user);
    }

    @Override
    public User updateUser(Long id, User user) {
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        existingUser.setNom(user.getNom());
        existingUser.setPrenom(user.getPrenom());
        existingUser.setEmail(user.getEmail());
        existingUser.setTelephone(user.getTelephone());

        return userRepository.save(existingUser);
    }

    @Override
    public void deactivateUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
        user.setActif(false);
        userRepository.save(user);
    }

    @Override
    public void activateUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
        user.setActif(true);
        userRepository.save(user);
    }

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public List<User> getUsersByRole(UserRole role) {
        return userRepository.findByRole(role);
    }

    @Override
    public List<User> getActiveUsers() {
        return userRepository.findByActif(true);
    }

    @Override
    public boolean changePassword(Long userId, String oldPassword, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        if (passwordEncoder.matches(oldPassword, user.getPassword())) {
            user.setPassword(passwordEncoder.encode(newPassword));
            userRepository.save(user);
            return true;
        }
        return false;
    }

    @Override
    public boolean resetPassword(String email, String newPassword) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setPassword(passwordEncoder.encode(newPassword));
            userRepository.save(user);
            return true;
        }
        return false;
    }






}