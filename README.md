# ♟️ Truthful Checkers

**Truthful Checkers** is a real-time, cross-platform multiplayer board game built entirely with **Kotlin Multiplatform (KMP)**. Developed as a 300-level Computer Science final project at Moravian University, this application merges classic checkers mechanics with dynamic trivia, offline persistence, and cloud-synchronized multiplayer.

![Kotlin](https://img.shields.io/badge/kotlin-%237F52FF.svg?style=for-the-badge&logo=kotlin&logoColor=white)
![Android](https://img.shields.io/badge/Android-3DDC84?style=for-the-badge&logo=android&logoColor=white)
![iOS](https://img.shields.io/badge/iOS-000000?style=for-the-badge&logo=ios&logoColor=white)
![Firebase](https://img.shields.io/badge/firebase-ffca28?style=for-the-badge&logo=firebase&logoColor=black)

---

## 🚀 Core Architecture & 300-Level Features

This application was engineered to demonstrate mastery over modern software architecture, utilizing a decoupled **MVVM/MVI** pattern, reactive `StateFlows`, and rigorous separation of concerns.

* **🌐 Cloud Database Integration (Firebase):** Utilizes Firebase Realtime Database (via GitLive SDK) to power live matchmaking and instantly synchronize board state, trivia answers, and piece movement across separate physical devices.
* **💾 Local Persistent Storage (Room/SQLite):** Implements the new KMP Room library to cache OpenTDB trivia questions for offline play and automatically save local PvE/PvP match histories to the device's physical storage.
* **🎵 Native Hardware Media Access:** Features custom, platform-specific audio engines to prevent OOM crashes. Uses `MediaPlayer` on Android and `AVFoundation` on iOS to seamlessly mix looping background tracks with overlapping piece-capture sound effects.
* **📡 Network API Integration (Ktor):** Asynchronously fetches dynamically generated educational questions from the OpenTDB REST API to populate the trivia overlays.
* **🎨 Custom 2D Graphics & Animation:** Bypasses standard UI widgets in favor of a mathematically rendered Compose Canvas to draw the active game board and animate sprite sheets for loading overlays.
* **💉 Dependency Injection (Koin):** Implements a professional DI framework to safely inject Apple and Android hardware drivers into shared business logic without breaking the multiplatform boundary.

---

## 📂 Project Structure

The codebase is strictly divided to maximize code sharing while maintaining native performance:

* `composeApp/src/commonMain/`
  * **The Core Engine:** Contains 95% of the application code. Includes the Compose UI screens, `GameViewModel`, Ktor network clients, Room DAO interfaces, and the core game logic.
* `composeApp/src/androidMain/`
  * **Android Targets:** Contains the `AndroidSQLiteDriver` injection, `MediaPlayer` hardware bindings, and the `MainActivity` entry point.
* `composeApp/src/iosMain/`
  * **Apple Targets:** Contains the iOS `BundledSQLiteDriver`, strict `AVFoundation` audio session delegates, and the `MainViewController` Swift-to-Kotlin bridge.
* `iosApp/`
  * **Xcode Project:** The native iOS wrapper required to compile the Kotlin framework into an `.ipa` executable.

---

## 🛠️ Build & Run Instructions

Ensure you have the latest versions of **Android Studio** and **Xcode** (for iOS builds) installed.

### Android Setup
To build and run the development version of the Android app, select the `composeApp` configuration in Android Studio and hit Run, or execute via terminal:
```bash
./gradlew :composeApp:assembleDebug

iOS Setup
To build the iOS target, you must compile the Kotlin Native framework. Select the iosApp configuration in Android Studio, or execute the following via terminal (optimized for Apple Silicon): ./gradlew :composeApp:iosSimulatorArm64MainClasses

👨‍💻 Development
UI Framework: Compose Multiplatform

State Management: ViewModels & StateFlow

Navigation: Jetpack Navigation Compose

Networking: Ktor Client

Database: Room (KMP 2.7.0+) & Firebase Realtime Database

DI: Koin
