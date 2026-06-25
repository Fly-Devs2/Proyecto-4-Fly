# AI AGENT INSTRUCTIONS - Fly App

You are an expert Kotlin Multiplatform (KMP) developer working on the Fly App. Follow these rules strictly to maintain architectural integrity and design consistency.

## 🎯 High-Level Context
- **Framework:** Compose Multiplatform (Android & Desktop).
- **DI:** Koin (Modules in `shared/.../di/modules/`).
- **Architecture:** Strict MVVM with logic in the `shared` module.
- **Collaboration:** Always assume real-time sync via GitLive.

---

## 🎨 Design System (Tokens)
DO NOT use hardcoded hex codes. ALWAYS reference `MaterialTheme.colorScheme` or the project-specific palette from `ucenfotec.ac.cr.flydevs.presentation.theme`.

### Color Mapping
- **Primary BG:** `BgDarkest` (#141029)
- **Secondary/Nav BG:** `BgDark` (#1A1535)
- **Cards/Inputs:** `BgCard` (#1E1742)
- **Primary Accent/CTA:** `AccentViolet` (#7C5CFF)
- **Messenger/Gold:** `AccentGold` (#FFC23C)
- **Error/Admin:** `AccentRed` (#FF6B6B)
- **Success/Store:** `AccentMint` (#4AD8A8)

### Typography Scale
- **Screen Titles:** `titleLarge` (19sp Bold)
- **Subheaders:** `titleMedium` (16sp ExtraBold)
- **Labels (Small/Gold):** `titleSmall` (12sp Bold Uppercase)
- **Primary Body:** `bodyLarge` (16sp)
- **Button Labels:** `labelLarge` (15sp Bold)

---

## 🏗️ Architectural Patterns

### 1. The "UiState" Pattern (Mandatory)
Every screen MUST have a corresponding `UiState` data class in the `shared` module.