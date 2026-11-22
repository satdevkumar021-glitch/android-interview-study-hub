# 🚀 Android Interview Study Hub & Architecture Reference

[![Kotlin](https://img.shields.io/badge/Kotlin-2.0%2B-7F52FF?logo=kotlin&logoColor=white)](https://kotlinlang.org)
[![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-BOM%202026-4285F4?logo=jetpackcompose&logoColor=white)](https://developer.android.com/jetpack/compose)
[![Architecture](https://img.shields.io/badge/Architecture-MVI%20%2F%20Clean-00C853)](https://developer.android.com/topic/architecture)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](LICENSE)
[![CI/CD](https://img.shields.io/badge/CI%2FCD-GitHub%20Actions-2088FF?logo=githubactions&logoColor=white)](.github/workflows)

A production-grade Android engineering reference and comprehensive study platform authored by **Satdev Kumar** (Senior Android Developer, IBM India). 

Designed to demonstrate elite architectural patterns, modern Kotlin idioms, performance profiling, and mobile system design principles expected at tier-1 product companies.

---

## 🏛️ System Architecture: Unidirectional Data Flow (MVI)

The project enforces strict separation of concerns across multi-module boundaries:

```mermaid
flowchart TD
    subgraph UI_Layer["UI Layer (Jetpack Compose)"]
        View["Composable Screens\n(Stateful / Stateless)"]
        UIState["Immutable UI State\n(Data Class)"]
        UIIntent["User Actions / Intents\n(Sealed Interface)"]
    end

    subgraph Architecture_Layer["Presentation Layer (ViewModel)"]
        VM["StateViewModel\n(Lifecycle-Aware)"]
        Reducer["State Reducer\n(Coroutines / StateFlow)"]
    end

    subgraph Domain_Layer["Domain Layer (Pure Kotlin)"]
        UseCase["Interactors / UseCases\n(Business Logic)"]
        DomainModel["Domain Models\n(Entities)"]
    end

    subgraph Data_Layer["Data Layer (Repository Pattern)"]
        Repo["Data Repository Implementation"]
        LocalSource["Local Storage\n(Room + SQLCipher)"]
        RemoteSource["Remote Network\n(Retrofit + OkHttp)"]
    end

    UIIntent -->|Dispatches| VM
    VM -->|Executes| UseCase
    UseCase -->|Requests| Repo
    Repo -->|Reads/Writes| LocalSource
    Repo -->|Fetches| RemoteSource
    Repo -->|Streams Data| VM
    VM -->|Reduces| Reducer
    Reducer -->|Emits StateFlow| UIState
    UIState -->|Recomposes| View
```

---

## 🌟 Core Technical Modules

- **`01-kotlin-advanced`:** Coroutines internals, Structured Concurrency, Flow (SharedFlow vs StateFlow), Custom Dispatchers, Memory Management, and Bytecode Inlining.
- **`02-jetpack-compose-internals`:** Snapshot State system, Recomposition optimization, SubcomposeLayout, Custom Layouts, and Compose Modifier chains.
- **`03-clean-architecture-mvi`:** Production multi-module structure (`:core:designsystem`, `:core:database`, `:core:network`, `:feature:dashboard`).
- **`04-mobile-security`:** Hardware-backed Android Keystore, AES-GCM encryption, BiometricPrompt (FIDO), and Google Play Integrity attestation.
- **`05-performance-profiling`:** Baseline Profiles generation, Macrobenchmark tracing, LeakCanary memory leak triage, and ANR elimination.
- **`06-mobile-system-design`:** Offline-first synchronization, high-frequency stock tickers, live location tracking, and resilient caching architectures.

---

## 🛠️ Quick Start & Running Tests

### Prerequisites:
- Android Studio Ladybug | 2024.2+ or Meerkat
- JDK 17 or 21
- Android SDK 35 (Target SDK 35)

### Build & Run Unit Tests:
```bash
# Clone the repository
git clone https://github.com/satdevkumar021-glitch/android-interview-study-hub.git
cd android-interview-study-hub

# Build debug APK
./gradlew assembleDebug

# Run unit tests across all modules with MockK and Turbine
./gradlew testDebugUnitTest
```

---

## 👨‍💻 Author
**Satdev Kumar**  
Senior Android Developer | IBM India  
- [LinkedIn Profile](https://linkedin.com/in/satdevsokhal)  
- [GitHub Profile](https://github.com/satdevkumar021-glitch)
