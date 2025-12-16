-- Utilisateurs avec mot de passe: "password123"
-- Hash BCrypt généré correctement
INSERT INTO users (nom, prenom, email, password, role, date_inscription, actif, telephone) VALUES
('Admin', 'System', 'admin@event.ma', '$2a$10$rbafrAtFe5cSVXGtNI0hxuOSGImlaw/UiQcA3YZA.aFuSvVYyK1yu', 'ADMIN', CURRENT_TIMESTAMP, true, '+212 6 11 11 11 11'),
('Alami', 'Hassan', 'organizer1@event.ma', '$2a$10$rbafrAtFe5cSVXGtNI0hxuOSGImlaw/UiQcA3YZA.aFuSvVYyK1yu', 'ORGANIZER', CURRENT_TIMESTAMP, true, '+212 6 22 22 22 22'),
('Bennani', 'Fatima', 'organizer2@event.ma', '$2a$10$rbafrAtFe5cSVXGtNI0hxuOSGImlaw/UiQcA3YZA.aFuSvVYyK1yu', 'ORGANIZER', CURRENT_TIMESTAMP, true, '+212 6 33 33 33 33'),
('Idrissi', 'Karim', 'client1@event.ma', '$2a$10$rbafrAtFe5cSVXGtNI0hxuOSGImlaw/UiQcA3YZA.aFuSvVYyK1yu', 'CLIENT', CURRENT_TIMESTAMP, true, '+212 6 44 44 44 44'),
('Tazi', 'Amina', 'client2@event.ma', '$2a$10$rbafrAtFe5cSVXGtNI0hxuOSGImlaw/UiQcA3YZA.aFuSvVYyK1yu', 'CLIENT', CURRENT_TIMESTAMP, true, '+212 6 55 55 55 55');

-- Événements
INSERT INTO events (titre, description, categorie, date_debut, date_fin, lieu, ville, capacite_max, prix_unitaire, organisateur_id, statut, date_creation, date_modification) VALUES
('Concert Rock International', 'Grand concert de rock avec des groupes internationaux renommés. Une soirée inoubliable!', 'CONCERT', '2025-02-15 20:00:00', '2025-02-15 23:30:00', 'Salle Olympia', 'Casablanca', 500, 250.0, 2, 'PUBLIE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Festival Jazz Sous Les Étoiles', 'Festival de jazz en plein air avec les meilleurs artistes marocains et internationaux.', 'CONCERT', '2025-03-20 19:00:00', '2025-03-20 23:00:00', 'Jardin Majorelle', 'Marrakech', 300, 180.0, 2, 'PUBLIE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Pièce de Théâtre: Les Misérables', 'Adaptation moderne du chef-d''œuvre de Victor Hugo', 'THEATRE', '2025-03-10 19:00:00', '2025-03-10 21:30:00', 'Théâtre National Mohammed V', 'Rabat', 200, 150.0, 3, 'PUBLIE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Comédie: Rire en Scène', 'Soirée stand-up avec les meilleurs humoristes marocains', 'THEATRE', '2025-02-25 20:00:00', '2025-02-25 22:00:00', 'Théâtre Megarama', 'Casablanca', 150, 120.0, 3, 'PUBLIE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Conférence Tech Summit 2025', 'Conférence sur les dernières innovations technologiques et l''IA', 'CONFERENCE', '2025-04-05 09:00:00', '2025-04-05 18:00:00', 'Centre de Congrès', 'Casablanca', 400, 300.0, 2, 'PUBLIE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Match de Football: WAC vs Raja', 'Derby casablancais tant attendu', 'SPORT', '2025-02-28 18:00:00', '2025-02-28 20:00:00', 'Stade Mohammed V', 'Casablanca', 45000, 100.0, 3, 'PUBLIE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Marathon de Rabat 2025', 'Course internationale - 10km, semi-marathon et marathon', 'SPORT', '2025-05-15 07:00:00', '2025-05-15 13:00:00', 'Corniche de Rabat', 'Rabat', 5000, 50.0, 2, 'PUBLIE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Salon du Livre de Casablanca', 'Rencontres avec auteurs, dédicaces et expositions', 'AUTRE', '2025-06-10 10:00:00', '2025-06-15 20:00:00', 'OFEC', 'Casablanca', 10000, 0.0, 3, 'PUBLIE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Réservations
INSERT INTO reservations (utilisateur_id, evenement_id, nombre_places, montant_total, date_reservation, statut, code_reservation, commentaire) VALUES
(4, 1, 2, 500.0, CURRENT_TIMESTAMP, 'CONFIRMEE', 'RES-001-2025', 'Réservation VIP'),
(4, 3, 1, 150.0, CURRENT_TIMESTAMP, 'CONFIRMEE', 'RES-002-2025', NULL),
(5, 1, 3, 750.0, CURRENT_TIMESTAMP, 'CONFIRMEE', 'RES-003-2025', 'Réservation groupe'),
(5, 2, 2, 360.0, CURRENT_TIMESTAMP, 'EN_ATTENTE', 'RES-004-2025', NULL),
(4, 5, 1, 300.0, CURRENT_TIMESTAMP, 'CONFIRMEE', 'RES-005-2025', 'Billet professionnel');