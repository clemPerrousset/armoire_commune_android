# L'Armoire Commune - Android App

Application Android native pour "L'Armoire Commune", développée en Kotlin / XML avec Material Design.

## Fonctionnalités

- **Authentification** : Inscription et Connexion (JWT).
- **Bibliothèque d'objets** : Liste des objets, filtres, disponibilité.
- **Réservations** : Calendrier, sélection de lieu de retrait sur carte (OpenStreetMap), historique.
- **Administration** : Création d'objets, de lieux, et gestion des retours.

## Architecture Technique

- **Langage** : Kotlin
- **Architecture** : MVVM (Model-View-ViewModel)
- **Réseau** : Ktor Client (Coroutines)
- **Carte** : OSMDroid (OpenStreetMap)
- **UI** : XML Layouts, Material Design Components
- **Compatibilité** : minSdk 24, targetSdk 34

## Configuration du Backend

L'application est configurée pour se connecter à un backend local.
Par défaut, elle utilise `http://10.0.2.2:8000` qui correspond au `localhost` de la machine hôte depuis l'émulateur Android.

Si vous testez sur un appareil physique, changez `BASE_URL` dans `app/src/main/java/fr/larmoirecommune/app/network/ApiClient.kt` par l'IP de votre serveur.

## Instructions de Build

1. Ouvrir Android Studio.
2. Sélectionner **Open** et choisir le dossier racine du projet.
3. Laisser Gradle se synchroniser (nécessite Internet).
4. Exécuter l'application sur un émulateur ou un device.

## Dépendances Clés

- `io.ktor:ktor-client-*` : Communication API
- `org.osmdroid:osmdroid-android` : Cartographie
- `org.jetbrains.kotlinx:kotlinx-serialization-json` : Parsing JSON

## Notes

Ce projet a été généré automatiquement. Assurez-vous d'avoir le JDK 17+ et le SDK Android installé.
