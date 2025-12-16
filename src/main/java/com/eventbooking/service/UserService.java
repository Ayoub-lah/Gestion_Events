package com.eventbooking.service;

import com.eventbooking.entity.User;
import com.eventbooking.entity.enums.UserRole;
import java.util.List;
import java.util.Optional;

public interface UserService {

    boolean authenticate(String email, String password);

    Optional<User> getUserByEmail(String email);

    boolean existsByEmail(String email);

    User registerNewUser(String nom, String prenom, String email, String password, String telephone, UserRole role);

    User updateUser(Long id, User user);

    void deactivateUser(Long id);

    void activateUser(Long id);

    void deleteUser(Long id);

    List<User> getAllUsers();

    List<User> getUsersByRole(UserRole role);

    List<User> getActiveUsers();

    boolean changePassword(Long userId, String oldPassword, String newPassword);

    boolean resetPassword(String email, String newPassword);

    long getTotalUsersCount();

    // AJOUTER CES MÉTHODES :
    long getUserReservationsCount(Long userId);

    long getUserConfirmedReservationsCount(Long userId);
}