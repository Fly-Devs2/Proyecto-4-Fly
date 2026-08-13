# ⚙️ Guía de configuración — Fly App

Pasos para levantar el proyecto desde cero en una máquina nueva. Si algo falla, revisa primero la sección [🧯 Problemas comunes](#-problemas-comunes) al final.

## 1. Requisitos previos

| Herramienta | Versión | Notas |
|---|---|---|
| **JDK** | 21 (Amazon Corretto) | Gradle lo auto-descarga vía foojay según `gradle/gradle-daemon-jvm.properties`. El bytecode se compila a **JVM 17**. |
| **Android SDK** | `compileSdk` 36 / `targetSdk` 36 / `minSdk` 24 | Instalable desde Android Studio → SDK Manager. |
| **Android Studio** | Narwhal o superior | Recomendado. Necesita el plugin de Kotlin Multiplatform. |
| **Gradle** | 9.4.1 | **No lo instales**: usa el wrapper (`./gradlew`). |
| **Node.js** | 22 | Solo si vas a tocar `firebase-backend/`. |
| **Firebase CLI** | última | `npm install -g firebase-tools`. Solo para desplegar reglas o functions. |
| **Git** | — | |

> No se requiere macOS ni Xcode: el proyecto **no tiene target iOS**.

## 2. Clonar el repositorio

```bash
git clone https://github.com/Fly-Devs2/Proyecto-4-Fly.git
cd Proyecto-4-Fly
```

La rama de trabajo es **`DEV`**.

## 3. Configurar el Android SDK (`local.properties`)

Este archivo no está versionado. Si abres el proyecto en Android Studio se genera solo; si compilas desde consola, créalo en la raíz:

```properties
# Windows
sdk.dir=C:\\Users\\<tu-usuario>\\AppData\\Local\\Android\\Sdk

# macOS
# sdk.dir=/Users/<tu-usuario>/Library/Android/sdk

# Linux
# sdk.dir=/home/<tu-usuario>/Android/Sdk
```

## 4. Configurar Firebase ⚠️ (obligatorio para Android)

El archivo `androidApp/google-services.json` **está en `.gitignore`**, así que no viene en el clon. Sin él, `:androidApp` no compila.

1. Pide acceso al proyecto **`fly-app-eddaa`** en la [consola de Firebase](https://console.firebase.google.com/).
2. Ve a **Configuración del proyecto → Tus apps → Android** (`ucenfotec.ac.cr.flydevs`) y descarga `google-services.json`.
3. Colócalo en `androidApp/google-services.json`.
4. **Registra tu huella SHA-1 de debug**, o el inicio de sesión con Google fallará silenciosamente en tu máquina:

   ```bash
   # Desde la raíz del proyecto
   ./gradlew :androidApp:signingReport
   ```

   Copia el SHA-1 de la variante `debug` y agrégalo en **Configuración del proyecto → Tus apps → Android → Agregar huella digital**. Luego vuelve a descargar el `google-services.json` actualizado.

> El escritorio (`:desktopApp`) no necesita `google-services.json`, pero sí requiere que el proyecto de Firebase tenga habilitados Firestore y Auth.

## 5. Compilar y ejecutar

```bash
# Verificación rápida de que todo compila (es el comando que usa el equipo)
./gradlew :androidApp:compileDebugKotlin :desktopApp:compileKotlin

# Escritorio
./gradlew :desktopApp:run

# Android — instala en el emulador o dispositivo conectado
./gradlew :androidApp:installDebug

# Solo generar el APK de debug
./gradlew :androidApp:assembleDebug
```

En Windows usa `gradlew.bat` en lugar de `./gradlew`.

Desde Android Studio: selecciona la configuración **androidApp** para móvil, o crea una configuración de Gradle con la tarea `:desktopApp:run` para escritorio.

## 6. Pruebas

```bash
./gradlew :shared:jvmTest        # tests JVM del módulo shared
./gradlew :shared:test           # incluye androidHostTest
```

## 7. Backend (Cloud Functions) — opcional

Solo necesario si vas a modificar la lógica del servidor.

```bash
cd firebase-backend
npm install
npm run build              # compila TypeScript a lib/

firebase login
firebase use fly-app-eddaa

npm run serve              # emulador local de functions
npm run deploy             # firebase deploy --only functions
npm run logs               # ver logs en producción
```

Antes del **primer despliegue** de `generateOrderQr` hay que crear su secreto:

```bash
firebase functions:secrets:set QR_HMAC_SECRET
```

Además, el correo con el código QR depende de la extensión **Trigger Email from Firestore** instalada sobre la colección `mail`.

Para publicar cambios en las reglas de seguridad:

```bash
firebase deploy --only firestore:rules
```

## 8. Empaquetar distribuibles de escritorio

```bash
./gradlew :desktopApp:packageDistributionForCurrentOS   # .msi / .dmg / .deb
```

## 🧯 Problemas comunes

| Síntoma | Causa / solución |
|---|---|
| `File google-services.json is missing` | Falta el paso 4. Descárgalo de la consola de Firebase. |
| `SDK location not found` | Falta `local.properties` (paso 3). |
| El login con Google no hace nada | Tu SHA-1 de debug no está registrado en Firebase (paso 4.4). |
| `Unsupported class file major version` | Estás usando un JDK distinto al 21. Deja que Gradle resuelva el toolchain o apunta `JAVA_HOME` a Corretto 21. |
| Build lentísimo la primera vez | Normal: Gradle descarga el toolchain, el wrapper y las dependencias. Ya están activados el config cache y el build cache. |

---

← Volver al [README](../README.md)
