package com.eventbooking.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * Utility class pour générer des mots de passe BCrypt
 * Exécutez cette classe pour générer les hash à utiliser dans data.sql
 */
public class PasswordGenerator {

    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        // Mot de passe à encoder
        String password = "password123";

        // Génère le hash
        String hash = encoder.encode(password);

        System.out.println("╔════════════════════════════════════════════════════════════╗");
        System.out.println("║          GÉNÉRATEUR DE HASH BCRYPT                       ║");
        System.out.println("╚════════════════════════════════════════════════════════════╝");
        System.out.println();
        System.out.println("Mot de passe en clair : " + password);
        System.out.println();
        System.out.println("Hash BCrypt généré :");
        System.out.println(hash);
        System.out.println();

        // Vérification
        boolean matches = encoder.matches(password, hash);
        System.out.println("Vérification : " + (matches ? "✓ Hash valide" : "✗ Erreur"));
        System.out.println();
        System.out.println("Copiez ce hash dans votre fichier data.sql");
        System.out.println("Remplacez tous les hash par celui-ci pour utiliser 'password123'");
        System.out.println();

        // Exemple d'insertion SQL
        System.out.println("Exemple d'utilisation dans data.sql :");
        System.out.println("INSERT INTO users (email, password, ...) VALUES");
        System.out.println("('client1@event.ma', '" + hash + "', ...);");
    }
}