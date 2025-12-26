# 🎉 Application de Gestion d'Événements

Une application web complète de gestion d'événements développée avec Spring Boot, Vaadin et Spring Security.

<img width="1366" height="768" alt="image" src="https://github.com/user-attachments/assets/7b0b67f7-6f9a-4066-8778-b26f11eccc66" />


---

## 📋 Table des Matières

- [Fonctionnalités](#fonctionnalités-)
- [Technologies Utilisées](#-technologies-utilisées)
- [Prérequis](#-prérequis)
- [Installation](#-installation)
- [Configuration](#-configuration)
- [Lancement de l'Application](#-lancer-lapplication)
- [Accès aux Interfaces](#-accès-aux-interfaces)
- [Sécurité & Rôles](#-sécurité--rôles)
- [Structure du Projet](#-structure-du-projet)

---

## ✨ Fonctionnalités

### 🔐 Sécurité & Authentification
- Authentification sécurisée avec Spring Security
- Gestion des rôles : ADMIN, USER, ORGANIZER
- Protection des endpoints selon les permissions

### 📅 Gestion des Événements
- Création, modification et suppression d'événements
- Consultation de la liste des événements
- Recherche et filtrage des événements
- Gestion des participants

### 📊 Interface Utilisateur
- Interface moderne avec Vaadin
- Composants interactifs et réactifs
- Graphiques et statistiques avec Vaadin Charts
- Upload de fichiers (jusqu'à 10MB)
- Mises à jour en temps réel via WebSockets

### 🗄️ Base de Données
- Base H2 en mémoire intégrée
- Console H2 pour l'administration des données
- Initialisation automatique des données de test

---

## 🛠️ Technologies Utilisées

| Technologie | Version | Description |
|------------|---------|-------------|
| Java | 17 | Langage de programmation |
| Spring Boot | 3.x | Framework backend |
| Vaadin | 24.x | Framework d'interface utilisateur |
| Spring Security | 6.x | Sécurité et authentification |
| H2 Database | 2.x | Base de données en mémoire |
| Maven | 3.8+ | Gestion des dépendances |
| Spring Data JPA | 3.x | Persistance des données |
| WebSockets | | Communication en temps réel |

---

## ✅ Prérequis

### 📦 Logiciels Requis
- **Java JDK 17** ou supérieur
- **IntelliJ IDEA Ultimate** (recommandé)
- **Maven 3.8+**
- **Git**
- **Navigateur Web moderne**

### 🔧 Configuration Système
- 4GB RAM minimum
- 2GB d'espace disque libre
- Connexion Internet pour télécharger les dépendances

---

## ⚙️ Installation

### 1. Cloner le Repository
```bash
https://github.com/Ayoub-lah/Gestion_Events
cd Gestion_Events
```

### 2. Ouvrir avec IntelliJ IDEA
- Ouvrir IntelliJ IDEA Ultimate
- Sélectionner "Open" et choisir le dossier du projet
- Attendre l'indexation et la résolution des dépendances Maven

### 3. Installer les Dépendances
```bash
# Depuis le terminal dans le dossier du projet
mvn clean install

# Ou depuis IntelliJ :
# 1. Ouvrir le panneau Maven (généralement à droite)
# 2. Cliquer sur l'icône "Reload All Maven Projects"
```
---

## ▶️ Lancer l'Application

### Méthode 1 : Depuis IntelliJ IDEA
1. Localiser la classe principale `EventBookingApplication.java`
2. Cliquer avec le bouton droit → `Run 'EventBookingApplication'`
3. Ou utiliser le bouton ▶️ vert dans la barre d'outils

### Méthode 2 : Via Terminal Maven
```bash
# Dans le dossier du projet
mvn spring-boot:run

# Avec un profil spécifique (si configuré)
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

### Vérifier le Démarrage
```bash
# Vérifier que l'application est en cours d'exécution
curl http://localhost:8080

# Les logs doivent afficher :
# Started EventManagementApplication in X.XXX seconds
```

---

## 🔗 Accès aux Interfaces

### 🌐 Application Web
- **URL principale** : [http://localhost:8080](http://localhost:8080)
- **Page de login** : [http://localhost:8080/login](http://localhost:8080/login)

### 🗃️ Console H2 (Base de Données)
- **URL** : [http://localhost:8080/h2-console](http://localhost:8080/h2-console)
- **JDBC URL** : `jdbc:h2:mem:eventdb`
- **Username** : `sa`
- **Password** : (laisser vide)

---

## 🔐 Sécurité & Rôles

### Comptes Préconfigurés
| Rôle | Email | Mot de passe | Permissions |
|------|-------|-------------|-------------|
| **ADMIN** | `admin@event.ma` | `password123` | Accès complet à toutes les fonctionnalités |
| **USER** | `client1@event.ma` | `password123` | Consultation des événements, inscription |
| **ORGANIZER** | `organizer1@event.ma` | `password123` | Gestion de ses propres événements |

### Matrice des Permissions
| Action | ADMIN | ORGANIZER | USER |
|--------|-------|-----------|------|
| Voir tous les événements | ✅ | ✅ | ✅ |
| Créer un événement | ✅ | ✅ | ❌ |
| Modifier tout événement | ✅ | ❌ | ❌ |
| Modifier ses événements | ✅ | ✅ | ❌ |
| Supprimer des événements | ✅ | ❌ | ❌ |
| Gérer les utilisateurs | ✅ | ❌ | ❌ |
| Accès H2 Console | ✅ | ❌ | ❌ |



---

## 🛠️ Développement

### Commandes Maven Utiles
```bash
# Nettoyer et compiler
mvn clean compile

# Exécuter les tests
mvn test

# Générer un rapport de couverture de tests
mvn jacoco:report

# Vérifier les dépendances obsolètes
mvn versions:display-dependency-updates

# Formatter le code
mvn spotless:apply
```

---

**✨ Développé avec passion pour la gestion d'événements ✨**
