# DriveEasy – Backend (Spring Boot / Spring Cloud)

Plateforme de location de véhicules basée sur une **architecture microservices**.
Projet du module *Développement d'applications web et multimédia* – Licence Informatique et Multimédia (LIM3), SESAME.

## Architecture

```
            Frontend Angular (4200)  /  Postman
                        │
                        ▼
              api-gateway (8080)  ── CORS, routage, filtre X-Request-Id
             /          │           \
            ▼           ▼            ▼
 vehicule-service   client-service   location-service
      (8081)            (8082)           (8083)
                                    appelle vehicule + client
            \           │           /
             ▼          ▼          ▼
   service-registry (Eureka, 8761)   config-server (8888)   Zipkin (9411)
```

| Composant | Port | Rôle |
|---|---|---|
| `service-registry` | 8761 | Eureka Server : annuaire des services |
| `config-server` | 8888 | Spring Cloud Config : configuration centralisée (`config-repo/`) |
| `vehicule-service` | 8081 | Parc automobile, catégories, disponibilité |
| `client-service` | 8082 | Clients, permis, éligibilité, fidélité |
| `location-service` | 8083 | Cycle de vie des locations, prix, pénalités, statistiques |
| `api-gateway` | 8080 | Point d'entrée unique (`/api/**`), load balancing, CORS |
| Zipkin (Docker) | 9411 | Traçage distribué |

Les données sont stockées **en mémoire** (aucune base de données) et des jeux de test sont chargés au démarrage.

## Technologies

Java 21 · Spring Boot 3.5.6 · Spring Cloud 2025.0.0 (Eureka, Config, Gateway, LoadBalancer) · Micrometer Tracing + Brave · Zipkin · Maven · Docker

## Prérequis

- **JDK 21** (`java -version` doit afficher 21)
- **Maven 3.9+**
- **Docker Desktop** (pour Zipkin)
- Ports libres : 8080–8083, 8761, 8888, 9411

## Installation et lancement

### 1. Cloner et compiler

```bash
git clone https://github.com/<utilisateur>/driveeasy-backend.git
cd driveeasy-backend
mvn clean install
```

### 2. Démarrer Zipkin

```bash
docker compose up -d
```

### 3. Démarrer les services **dans cet ordre**

Chaque service lit sa configuration au démarrage : le **config-server doit être prêt** avant les autres.

| Ordre | Commande (depuis la racine) |
|---|---|
| 1 | `cd service-registry && mvn spring-boot:run` |
| 2 | `cd config-server && mvn spring-boot:run` |
| 3 | `cd vehicule-service && mvn spring-boot:run` |
| 4 | `cd client-service && mvn spring-boot:run` |
| 5 | `cd location-service && mvn spring-boot:run` |
| 6 | `cd api-gateway && mvn spring-boot:run` |

**Sous Windows** : double-cliquer sur `start-all.bat` (lance tout dans l'ordre), ou sur les scripts `0-build.bat` à `6-start-gateway.bat`.
Les scripts `.bat` utilisent le JDK `C:\Program Files\Eclipse Adoptium\jdk-21...` : adapter le chemin si nécessaire.

### 4. Vérifier

- Eureka : http://localhost:8761 → 5 instances UP
- Gateway : http://localhost:8080/api/vehicules
- Zipkin : http://localhost:9411

## Configuration centralisée

Les fichiers de configuration sont dans [`config-repo/`](config-repo) (un fichier par service + `application.yml` commun).
Le config-server propose deux profils :

| Profil | Source | Utilisation |
|---|---|---|
| `git` | dépôt GitHub (dossier `config-repo/`) | mode final |
| `native` | dossier local `../config-repo/` | développement hors ligne |

Changer de profil : variable d'environnement `CONFIG_PROFILE=native` (ou `git`).

Propriétés métier externalisées : âge minimum (21), remises FIDELE 5 % / VIP 10 %, coefficient de pénalité 1,5, seuils de fidélité 5 / 15.

## API (via la gateway, préfixe `/api`)

### vehicule-service

| Méthode | URI | Description |
|---|---|---|
| GET | `/api/vehicules?categorie=&carburant=&statut=&prixMax=` | Liste filtrée |
| GET | `/api/vehicules/{id}` | Détail |
| POST | `/api/vehicules` | Ajout (201, 409 si immatriculation existante) |
| PUT | `/api/vehicules/{id}` | Modification |
| DELETE | `/api/vehicules/{id}` | Suppression (409 si LOUE) |
| PATCH | `/api/vehicules/{id}/statut` | Changement de statut |
| GET | `/api/vehicules/disponibles?dateDebut=&dateFin=` | Véhicules disponibles |

### client-service

| Méthode | URI | Description |
|---|---|---|
| GET | `/api/clients?nom=&cin=&email=` | Recherche |
| GET | `/api/clients/{id}` | Détail |
| POST | `/api/clients` | Création (409 si CIN / email / permis existant) |
| PUT | `/api/clients/{id}` | Modification |
| DELETE | `/api/clients/{id}` | Suppression (409 si location active) |
| GET | `/api/clients/{id}/eligibilite?dateFin=` | Âge ≥ 21 et permis valide |
| PATCH | `/api/clients/{id}/categorie` | Catégorie de fidélité |

### location-service

| Méthode | URI | Description |
|---|---|---|
| GET | `/api/locations?statut=&clientId=&vehiculeId=&dateDebut=&dateFin=&page=&size=` | Liste paginée |
| GET | `/api/locations/{id}` | Détail enrichi (client + véhicule) |
| POST | `/api/locations` | Réservation (vérifications inter-services) |
| POST | `/api/locations/simulation` | Simulation du prix |
| PATCH | `/api/locations/{id}/depart` | Départ → EN_COURS, véhicule LOUE |
| PATCH | `/api/locations/{id}/retour` | Retour → TERMINEE, pénalités, fidélité |
| PATCH | `/api/locations/{id}/annulation` | Annulation |
| GET | `/api/locations/statistiques` | CA, taux d'occupation, top véhicules |

**Règles de calcul**
- Prix = nombre de jours × prix journalier × coefficient catégorie − remise fidélité (FIDELE 5 %, VIP 10 %)
- Coefficients : ECONOMIQUE 1.0 · COMPACTE 1.1 · SUV 1.3 · LUXE 1.5 · UTILITAIRE 1.2
- Pénalité de retard = 1,5 × prix journalier × jours de retard
- Passage FIDELE après 5 locations terminées, VIP après 15

Chaque module contient un fichier `requests.http` (extension *REST Client* de VS Code ou IntelliJ) avec des requêtes prêtes à l'emploi, y compris les cas d'erreur.

## Jeux de données

| Véhicules | Clients | Locations |
|---|---|---|
| 7 véhicules (id 6 en MAINTENANCE) | 1–2 éligibles, 3 FIDELE, 4 VIP, **5 trop jeune**, **6 permis expiré** | 12 terminées, 1 réservée (client 2) ; le client 1 a 4 locations terminées → FIDELE au prochain retour |

## Codes HTTP

200 OK · 201 Created · 204 No Content · 400 données invalides / règle métier · 404 introuvable · 409 conflit · 500 erreur interne · 503 service distant indisponible.
Toutes les erreurs ont le même format JSON : `timestamp, status, error, message, path, details`.

## Observabilité

- **Actuator** : `/actuator/health`, `/actuator/info` sur chaque service ; `/actuator/gateway/routes` sur la gateway
- **Zipkin** : probabilité d'échantillonnage 1.0 ; une requête `GET /api/locations/1` produit une trace gateway → location-service → client-service + vehicule-service
- **Filtre gateway** : en-têtes `X-Request-Id` et `X-Response-Time` sur chaque réponse

## Captures d'écran

*(à compléter : Eureka, Zipkin, Postman)*
