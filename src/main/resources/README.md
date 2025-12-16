# 🎉 Application de Gestion d'Événements

Une application web complète de gestion d'événements développée avec Spring Boot, Vaadin et Spring Security.

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
- [Développement](#-développement)
- [Dépannage](#-dépannage)
- [Licence](#-licence)

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
git clone https://github.com/<username>/event-management-app.git
cd event-management-app
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

## 🗄️ Configuration

### Fichier `application.properties`
```properties
# ================================
# CONFIGURATION SERVEUR
# ================================
server.port=8080
server.servlet.context-path=/

# ================================
# BASE DE DONNÉES H2
# ================================
spring.datasource.url=jdbc:h2:mem:eventdb
spring.datasource.driverClassName=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=
spring.h2.console.enabled=true
spring.h2.console.path=/h2-console
spring.h2.console.settings.trace=false
spring.h2.console.settings.web-allow-others=false

# ================================
# JPA & HIBERNATE
# ================================
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.properties.hibernate.use_sql_comments=true
spring.sql.init.mode=always
spring.jpa.defer-datasource-initialization=true

# ================================
# VAADIN
# ================================
vaadin.servlet.productionMode=false
vaadin.charts.development-mode=true

# ================================
# UPLOAD DE FICHIERS
# ================================
spring.servlet.multipart.max-file-size=10MB
spring.servlet.multipart.max-request-size=10MB
file.upload-dir=uploads

# ================================
# RESSOURCES STATIQUES
# ================================
spring.web.resources.static-locations=classpath:/static/,file:./uploads/
app.base-url=http://localhost:8080

# ================================
# WEBSOCKETS
# ================================
spring.websocket.enabled=true
```

### Variables d'Environnement (Optionnel)
```bash
# Pour la production, vous pouvez utiliser :
export SERVER_PORT=8080
export DB_URL=jdbc:h2:mem:eventdb
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

### Méthode 3 : Générer un JAR Exécutable
```bash
mvn clean package
java -jar target/event-management-app-1.0.0.jar
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

### Développement Frontend (Vaadin)
```bash
# Mode développement Vaadin
mvn spring-boot:run -Pproduction-mode=false

# Builder le frontend
mvn vaadin:prepare-frontend
mvn vaadin:build-frontend
```

### Débogage
```bash
# Démarrer en mode debug
mvn spring-boot:run -Dspring-boot.run.jvmArguments="-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=5005"

# Connexion depuis IntelliJ :
# Run → Edit Configurations → + → Remote JVM Debug
# Host: localhost, Port: 5005
```

---

## 🔧 Dépannage

### Problèmes Courants et Solutions

| Problème | Solution |
|----------|----------|
| **Port 8080 déjà utilisé** | `server.port=8081` dans `application.properties` ou `netstat -ano \| findstr :8080` puis `taskkill /PID [PID] /F` |
| **Erreurs de dépendances Maven** | `mvn clean install -U` puis recharger le projet dans IntelliJ |
| **H2 Console inaccessible** | Vérifier `spring.h2.console.enabled=true` et l'URL `http://localhost:8080/h2-console` |
| **Upload de fichiers échoue** | Vérifier que le dossier `uploads/` existe et a les permissions d'écriture |
| **Vaadin ne se compile pas** | Supprimer `node_modules/` et `package-lock.json` puis `mvn clean install` |
| **Authentification échoue** | Vérifier les logs Spring Security et les données dans `data.sql` |

### Logs et Surveillance
```bash
# Niveau de logs (dans application.properties)
logging.level.com.eventmanagement=DEBUG
logging.level.org.springframework.security=DEBUG
logging.level.org.hibernate.SQL=DEBUG

# Vérifier les logs de démarrage
tail -f logs/application.log
```

### Tests de Connexion
```bash
# Tester la base de données
echo "SELECT COUNT(*) FROM USERS;" | curl -X POST http://localhost:8080/h2-console

# Tester l'API (avec curl)
curl -X GET http://localhost:8080/api/events
curl -u admin@event.ma:password123 http://localhost:8080/api/users
```

---

## 📈 Déploiement

### Préparation pour la Production
1. Modifier `application.properties` :
   ```properties
   vaadin.servlet.productionMode=true
   spring.h2.console.enabled=false
   spring.jpa.show-sql=false
   ```

2. Configurer une base de données externe (MySQL/PostgreSQL) :
   ```properties
   spring.datasource.url=jdbc:mysql://localhost:3306/eventdb
   spring.datasource.username=root
   spring.datasource.password=votre_mot_de_passe
   ```

### Docker (Optionnel)
```dockerfile
# Dockerfile
FROM openjdk:17-jdk-slim
COPY target/event-management-app-*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

```bash
# Construire et exécuter
docker build -t event-management-app .
docker run -p 8080:8080 event-management-app
```

---

**✨ Développé avec passion pour la gestion d'événements ✨**

*Dernière mise à jour : $(date +%Y-%m-%d)*