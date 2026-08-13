<h1 align="center">Fly App 🇨🇷</h1>
<p align="center">
  <a href="https://es.wikipedia.org/wiki/Costa_Rica"><img alt="Made in Costa Rica" src="https://img.shields.io/badge/Made%20in-%20Costa%20Rica-blue.svg?logo=data:image/svg%2bxml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIGlkPSJmbGFnLWljb25zLWNyIiB2aWV3Qm94PSIwIDAgNjQwIDQ4MCI+CiAgPGcgZmlsbC1ydWxlPSJldmVub2RkIiBzdHJva2Utd2lkdGg9IjFwdCI+CiAgICA8cGF0aCBmaWxsPSIjMDAwMGI0IiBkPSJNMCAwaDY0MHY0ODBIMHoiLz4KICAgIDxwYXRoIGZpbGw9IiNmZmYiIGQ9Ik0wIDc1LjRoNjQwdjMyMi4zSDB6Ii8+CiAgICA8cGF0aCBmaWxsPSIjZDkwMDAwIiBkPSJNMCAxNTcuN2g2NDB2MTU3LjdIMHoiLz4KICA8L2c+Cjwvc3ZnPgo="/></a>
  <a href="https://kotlinlang.org/"><img alt="Kotlin Version" src="https://img.shields.io/badge/Kotlin-2.4.0-%237F52FF.svg?logo=kotlin"/></a><!-- gradle/libs.versions.toml -->
  <a href="https://www.jetbrains.com/lp/compose-multiplatform/"><img alt="Compose Multiplatform" src="https://img.shields.io/badge/Compose%20Multiplatform-1.11.1-%237F52FF"/></a><!-- gradle/libs.versions.toml --> <br>
  <a href="https://gradle.org/"><img alt="Gradle" src="https://img.shields.io/badge/Gradle-9.4.1-%2302303A.svg?logo=gradle"/></a><!-- gradle/wrapper/gradle-wrapper.properties -->
  <a href="https://firebase.google.com/"><img alt="Firebase" src="https://img.shields.io/badge/Firebase-Firestore%20%7C%20Auth%20%7C%20Functions-%23FFCA28.svg?logo=firebase&logoColor=black"/></a>
  <img alt="Platforms" src="https://img.shields.io/badge/Platforms-Android%20%7C%20Desktop-%233DDC84.svg?logo=android&logoColor=white"/>
</p>

<p align="center">
Marketplace de cartas coleccionables (TCG) para Costa Rica, con pago por SINPE Móvil y entrega física a través de una red de tiendas y mensajeros.
</p>

## 📌 Descripción

**Fly App** es una aplicación multiplataforma (Android y Desktop) construida con **Kotlin Multiplatform** y **Compose Multiplatform** que conecta a coleccionistas de cartas de juegos de mesa y TCG (**Magic**, **Pokémon**, **One Piece**) en Costa Rica.

Cualquier usuario puede **publicar** sus cartas y **comprar** las de otros. Como el pago se hace por **SINPE Móvil** —una transferencia irreversible entre personas— y la entrega es física, la app agrega la capa de confianza que le falta a una venta por redes sociales: la carta se entrega en una **tienda aliada**, el pago se valida con comprobante, un **mensajero** transporta el lote hacia la tienda de destino y el comprador retira con un **código QR firmado** enviado a su correo.

Proyecto académico desarrollado en la **Universidad Cenfotec** por el equipo **Fly Devs**.

## 🧩 Problemática

La compraventa de cartas coleccionables en Costa Rica ocurre hoy en grupos de Facebook, WhatsApp y ferias presenciales, donde:

- **No hay garantía de pago ni de entrega.** El SINPE Móvil es irreversible: quien transfiere primero asume todo el riesgo.
- **No existe reputación verificable.** Nadie puede comprobar si una contraparte cumplió en transacciones anteriores.
- **La logística es manual.** Las partes deben coordinar encuentros presenciales, lo que limita el mercado a una misma zona geográfica.
- **No hay trazabilidad.** Si algo se pierde o llega dañado, no queda evidencia de quién tuvo la carta y cuándo.

## 🎯 Objetivo

Ofrecer un marketplace donde comprar y vender cartas sea seguro y trazable:

- **Custodia física en tiendas aliadas**: el vendedor entrega antes de que el comprador pague.
- **Validación del pago SINPE** con comprobante y evidencia fotográfica en cada paso.
- **Retiro con QR firmado** (HMAC-SHA256) para que solo el comprador legítimo reciba la carta.
- **Reputación por rol** construida a partir de transacciones reales completadas.
- **Red de mensajería** que agrupa órdenes en lotes y amplía el alcance a todo el país.

## 🛠️ Funcionalidades

### 👥 Usuarios (comprador / vendedor)
- Registro e inicio de sesión con **Google**.
- Publicación de cartas con múltiples fotos (cámara o galería), juego, expansión, rareza, condición, idioma, precio y cantidad.
- Catálogo con búsqueda y filtros; detalle de carta con **precios de referencia de Scryfall** y reputación del vendedor.
- **Sobres** (carrito) por vendedor → generación de la orden.
- Pago por **SINPE Móvil** con carga del comprobante.
- Seguimiento del pedido, historial de compras y **código QR** de retiro por correo.
- Calificación mutua al completar la transacción y **reporte de incidencias**.
- Notificaciones push y preferencias de notificación configurables.

### 🚚 Mensajeros
- Lotes de entrega asignados agrupados por tienda origen y destino.
- **Escaneo de QR** para recoger y entregar.
- Evidencia fotográfica de recogida y de entrega.
- Detalle del envío y recompensa por lote.

### 🏪 Tiendas
- Recepción de cartas entregadas por vendedores y de lotes entrantes.
- **Validación de retiros** escaneando el QR del comprador.
- Panel de pendientes y retiros por vencer.

### 🛡️ Administradores
- Dashboard, gestión y bloqueo de usuarios.
- Backoffice de **incidencias** con estado, prioridad y notas internas.
- **Trazabilidad** de la actividad por usuario (pedidos, ventas, calificaciones).
- Configuración de **roles y permisos**.

## 🏗️ Arquitectura

Kotlin Multiplatform con **MVVM** y separación por capas. Cuatro módulos Gradle más el backend:

| Módulo | Rol |
|---|---|
| `:shared` | Librería KMP: `domain/` (modelos y contratos de repositorio), `data/` (implementaciones Firebase, Ktor, Storage), `presentation/` (ViewModels + `UiState`), `di/` (módulos Koin). |
| `:composeApp` | Librería KMP con toda la UI: `App.kt`, `navigation/`, `presentation/screens/`, `presentation/components/`, `presentation/theme/`. |
| `:androidApp` | Aplicación Android (`applicationId` `ucenfotec.ac.cr.flydevs`). |
| `:desktopApp` | Aplicación de escritorio JVM (`MainKt`). |
| `firebase-backend/` | Cloud Functions en TypeScript (Node 22): QR firmado, crons, notificaciones, reputación. |

- **Inyección de dependencias:** Koin (`shared/.../di/modules/`: `DataModule`, `PresentationModule`, `SharedModule`).
- **Concurrencia:** Coroutines y Flow. Cada pantalla expone un `StateFlow<UiState>`.
- **Backend:** acceso directo a Firestore/Auth/Storage desde `shared` con el SDK **GitLive Firebase**; la lógica que no puede vivir en el cliente (firma de QR, crons, notificaciones) está en Cloud Functions.
- **API externa:** [Scryfall](https://scryfall.com/docs/api) para precios y versiones de carta.

### Colecciones de Firestore

`users` · `game_cards` · `envelopes` · `orders` · `batches` · `batch_group` · `stores` · `expansions` · `rarities` · `incidents` (+ `updates`) · `reviews` · `user_ratings` · `notifications` · `notifications_preferences` · `rols` · `mail` (cola de la extensión Trigger Email).

### Cloud Functions

| Función | Tipo | Qué hace |
|---|---|---|
| `generateOrderQr` | callable | Genera el QR de retiro firmado con HMAC-SHA256 y lo sube a Storage. |
| `releaseUnpaidExchanges` | cron (jue. 12:00 md CR) | Cancela órdenes sin comprobante y libera las cartas. |
| `scheduledSinpeReminder` | cron (jue. 8:00 am CR) | Recuerda al comprador subir el comprobante SINPE. |
| `groupOrdersIntoBatches` / `triggerGroupOrdersIntoBatches` | cron + callable | Agrupa órdenes pagadas en lotes `L-XXXX` por par de tiendas. |
| `onOrderStatusChange` | trigger Firestore | Notifica a comprador y vendedor y envía el correo con el QR. |
| `updateUserRatingSummary` / `updateOrderReputation` | trigger Firestore | Recalculan la reputación por rol y ventana temporal. |

## 🎨 Design System

Definido en `composeApp/src/commonMain/kotlin/.../presentation/theme/`. **No se deben escribir colores ni tamaños en duro**: siempre `MaterialTheme.colorScheme` y `MaterialTheme.typography`.

### Colores

| Token | Hex | Uso |
|---|---|---|
| `BgDarkest` | `#141029` | Fondo principal de todas las pantallas |
| `BgDark` | `#1A1535` | Fondo secundario, Bottom Nav |
| `BgCard` | `#1E1742` | Tarjetas, inputs y secciones |
| `BgSurface` | `#262048` | Superficies elevadas y headers |
| `AccentViolet` | `#7C5CFF` | Primario / rol Usuario / CTA / FAB |
| `AccentGold` | `#FFC23C` | Rol Mensajero / badges / títulos de sección |
| `AccentRed` | `#FF6B6B` | Rol Admin / errores / eliminación |
| `AccentMint` | `#4AD8A8` | Rol Tienda / éxito / estado "Gratis" |
| `TextPrimary` | `#FFFFFF` | Contenido principal |
| `TextSecondary` | `#A8A2CC` | Subtítulos y etiquetas |
| `TextMuted` | `#6B6690` | Placeholders |

### Tipografía (Inter)

- **Títulos:** `titleLarge` (19sp Bold) · `titleMedium` (16sp ExtraBold) · `titleSmall` (12sp Bold Uppercase)
- **Cuerpo:** `bodyLarge` (16sp) · `bodyMedium` (14sp) · `bodySmall` (13sp Medium)
- **Etiquetas:** `labelLarge` (15sp Bold, botones) · `labelMedium` (11sp Medium) · `labelSmall` (11sp Regular)

### Componentes por defecto

Reutiliza siempre lo que ya existe en `ucenfotec.ac.cr.flydevs.presentation.components`: `PrimaryButton`, `FormField` / `TextField` / `PriceField`, `TopBar`, `BottomNav`, `PhotoUploadZone`, `QuantityStepper`, `CameraCapture`, `QrScannerCamera`, `GalleryPicker`, `DraggablePhotoGrid`, `ImageCarousel`, `SearchableDropdown`, `PaginationBar`, `EvidenceCard`, `OrdersList`.

## ⚙️ Setup del proyecto

Para configurar el entorno de desarrollo y ejecutar el proyecto, sigue las instrucciones detalladas de nuestra guía de configuración:

**➡️ [Guía de Configuración (SETUP.md)](docs/SETUP.md)**

Resumen rápido para quien ya tiene el entorno listo:

```bash
git clone https://github.com/Fly-Devs2/Proyecto-4-Fly.git && cd Proyecto-4-Fly
# coloca androidApp/google-services.json (no viene en el repo — ver SETUP.md §4)
./gradlew :desktopApp:run          # escritorio
./gradlew :androidApp:installDebug # Android
```

## 📝 Convenciones de desarrollo

- **Patrón de vista:** toda pantalla tiene su `UiState` (data class) y su `ViewModel` en `shared/.../presentation/<feature>/`.
- **Lógica en `shared`:** los `@Composable` no llaman repositorios ni Firebase directamente.
- **ViewModels:** usa `runCatching` para las llamadas a repositorio y actualiza el `UiState` con el resultado.
- **Estilos:** nunca hardcodees hex ni sp; usa los tokens del tema.
- **Nomenclatura:** `[Feature][Capa]` — `RegisterViewModel`, `GameCardRepository`, `OrderDetailScreen`.
- **Contrato de estados:** el enum de Kotlin (`OrderStatus.kt`) es la fuente de verdad; `firebase-backend/src/contract.ts` lo replica y debe mantenerse sincronizado a mano.

## 🧪 Metodología

Desarrollado con **Scrum**, en sprints con backlog priorizado en el GitHub Project del repositorio y diseño de interfaces en **Figma**.

---

**Fly App 🇨🇷 — comprar y vender cartas sin desconfiar de la otra persona.**
