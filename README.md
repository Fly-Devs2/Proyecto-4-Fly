# Fly App - Kotlin Multiplatform Project

Fly App is a cross-platform application built with **Kotlin Multiplatform (KMP)** and **Compose Multiplatform**, targeting Android and Desktop.

## 🚀 Tech Stack
- **UI:** Compose Multiplatform
- **Dependency Injection:** Koin
- **Architecture:** MVVM (Model-View-ViewModel)
- **Concurrency:** Kotlin Coroutines & Flow
- **Authentication:** Google Auth
- **Collaboration:** GitLive

---

## 🎨 Design System (Theme)
Located in `composeApp/src/commonMain/kotlin/.../presentation/theme/`

### Colors
- **Backgrounds:**
  - `BgDarkest` (#141029): Primary background for all screens.
  - `BgDark` (#1A1535): Secondary background, used for Bottom Nav.
  - `BgCard` (#1E1742): Cards, inputs, and sections.
  - `BgSurface` (#262048): Elevated surfaces and headers.
- **Accents (Roles/Status):**
  - `AccentViolet` (#7C5CFF): Primary / User Role / CTA / FAB.
  - `AccentGold` (#FFC23C): Messenger Role / Badges / Section Titles.
  - `AccentRed` (#FF6B6B): Admin Role / Errors / Deletion.
  - `AccentMint` (#4AD8A8): Store Role / Success / "Free" status.
- **Text:**
  - `TextPrimary` (#FFFFFF): Main content.
  - `TextSecondary` (#A8A2CC): Subtitles and labels.
  - `TextMuted` (#6B6690): Placeholders.

### Typography (Inter Font)
- **Titles:** `titleLarge` (19sp Bold), `titleMedium` (16sp ExtraBold), `titleSmall` (12sp Bold Uppercase).
- **Body:** `bodyLarge` (16sp), `bodyMedium` (14sp), `bodySmall` (13sp Medium).
- **Labels:** `labelLarge` (15sp Bold - Buttons), `labelMedium` (11sp Medium), `labelSmall` (11sp Regular).

---

## 🏗️ Architecture & Project Structure

### 1. Layers
- **Views (Compose):** Located in `composeApp`. High-level screens and reusable components.
- **ViewModels:** Located in `shared/.../presentation/`. Use `MutableStateFlow` for UI State and `viewModelScope` for logic.
- **Repositories:** Interfaces in `shared/.../domain/repository/`, implementations in `shared/.../data/`.
- **Domain Models:** Pure Kotlin data classes in `shared/.../domain/model/`.

### 2. Dependency Injection
Managed via Koin in `shared/.../di/modules/`:
- `DataModule.kt`: Repository and Data Source definitions.
- `PresentationModule.kt`: ViewModel definitions.
- `SharedModule.kt`: Main entry point for DI.

---

## 🧩 Default Components
Use these components from `ucenfotec.ac.cr.flydevs.presentation.components` to maintain consistency:
- `PrimaryButton`: Main CTA button.
- `FormField` / `TextField` / `PriceField`: Standardized input fields.
- `TopBar` & `BottomNav`: Layout navigation elements.
- `PhotoUploadZone`: Standardized image picker UI.
- `QuantityStepper`: Numeric input for quantities.

---

## 📝 Guidelines for Development & Prompts
- **GitLive:** Always sync through GitLive for real-time collaboration.
- **View Pattern:** Use a dedicated `UiState` data class for every screen.
- **ViewModel Logic:** Use `runCatching` for repository calls and update `UiState` accordingly.
- **Styling:** Avoid hardcoded colors or sizes. Always reference `MaterialTheme.colorScheme` and `MaterialTheme.typography`.
- **Naming:** Follow `[Feature][Layer]` naming (e.g., `RegisterViewModel`, `GameCardRepository`).
