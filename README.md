# 🚀 Senior & Staff Android Engineer Mastery Hub & Production Reference

[![Kotlin](https://img.shields.io/badge/Kotlin-2.1.0-7F52FF?logo=kotlin&logoColor=white)](https://kotlinlang.org)
[![Android SDK](https://img.shields.io/badge/Compile%20SDK-35-3DDC84?logo=android&logoColor=white)](https://developer.android.com/about/versions/15)
[![Jetpack Compose](https://img.shields.io/badge/Compose%20BOM-2025.02.00-4285F4?logo=jetpackcompose&logoColor=white)](https://developer.android.com/jetpack/compose)
[![Gradle](https://img.shields.io/badge/Gradle-9.9.1-02303A?logo=gradle&logoColor=white)](https://gradle.org)
[![AGP](https://img.shields.io/badge/AGP-8.8.0-3DDC84?logo=android&logoColor=white)](https://developer.android.com/studio/releases/gradle-plugin)
[![Architecture](https://img.shields.io/badge/Architecture-MVI%20%2F%20Clean%20%2F%20UDF-00C853)](https://developer.android.com/topic/architecture)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](LICENSE)

A comprehensive, production-grade Android engineering repository, framework internals reference, and staff-level interview preparation platform authored by **Satdev Kumar** (Senior Android Developer, IBM Consulting).

This repository bridges high-level architecture with low-level execution mechanics: from Linux kernel boot and ART bytecode compilation to StrongBox HSM hardware security, Macrobenchmark jank elimination, resilient offline-first synchronization, and on-device Generative AI agent orchestration.

---

## 👨‍💻 Candidate & Author Profile

- **Name:** Satdev Kumar
- **Experience:** 8 Years in Native Android & Enterprise Mobile Architecture
- **Organization:** IBM Consulting (Pune, India)
- **Primary Specialization:** Native Android (Kotlin, Java, Jetpack Compose, Coroutines/Flow, Android OS Internals, NDK/JNI)
- **Domain Experience:**
  - **Financial Services & Banking:** PNC Mobile Banking, Card-Free ATM functionality, Security Center, Biometric hardware tokenization.
  - **Healthcare & Regulated Systems:** German Electronic Patient Record (ePA) healthcare architecture, cryptographic signature verification, HIPAA/GDPR compliance.
- **AI Specialization:** On-Device Small Language Models (Gemini Nano, Android AICore, LiteRT), Mobile RAG (Local Vector DBs), Model Context Protocol (MCP), and Enterprise Hybrid Gateways (IBM watsonx, AWS Bedrock, Azure OpenAI).
- **Target Career Tracks:** Senior Android Developer, Lead Android Developer, Android Technical Lead, Staff Android Engineer, AI-Integrated Mobile Application Architect.

---

## 🏛️ Repository Architecture & Ecosystem

```
├── 00_CURRICULUM_AND_ROADMAP/
│   ├── MASTER_CURRICULUM.md             # 25 Master Modules across 6 Core Pillars
│   ├── 30_DAY_PREPARATION_PLAN.md       # Day-by-day 30-day interview roadmap (2.5-3h/day)
│   └── INTERVIEW_EVALUATION_RUBRIC.md   # Tier-1 1-10 scoring rubric for L5/L6/L7 evaluations
│
├── 01_LEARNING_MODULES/                 # Exhaustive chapters adhering to 11-section framework
│   ├── MODULE_02_KOTLIN_INTERNALS/
│   │   ├── 01_KOTLIN_BYTECODE_AND_MEMORY.md
│   │   └── 02_GENERICS_AND_DELEGATION.md
│   ├── MODULE_03_JAVA_FOR_ARCHITECTS/
│   │   ├── 01_JAVA_MEMORY_MODEL.md
│   │   └── 02_JAVA_KOTLIN_INTEROP.md
│   ├── MODULE_04_ANDROID_OS_INTERNALS/
│   │   ├── 01_ZYGOTE_AND_APP_STARTUP.md
│   │   └── 02_BINDER_IPC_INTERNALS.md
│   ├── MODULE_06_COMPOSE_DEEP_INTERNALS/
│   │   ├── 01_SLOT_TABLE_AND_COMPILER.md
│   │   └── 02_STABILITY_AND_SNAPSHOTS.md
│   ├── MODULE_07_ENTERPRISE_ARCHITECTURE/
│   │   └── 01_MVI_AND_UDF.md
│   ├── MODULE_08_COROUTINES_CONCURRENCY/
│   │   ├── 01_CPS_AND_DISPATCHERS.md
│   │   ├── 02_STRUCTURED_CONCURRENCY.md
│   │   └── 03_FLOW_AND_BACKPRESSURE.md
│   ├── MODULE_09_JETPACK_COMPONENTS/
│   │   └── 01_VIEWMODEL_AND_LIFECYCLE_INTERNALS.md
│   ├── MODULE_10_DEPENDENCY_INJECTION/
│   │   └── 01_DAGGER_HILT_INTERNALS.md
│   ├── MODULE_11_NETWORKING_INTEGRATION/
│   │   └── 01_RESILIENT_NETWORKING.md
│   ├── MODULE_12_DATABASE_PERSISTENCE/
│   │   └── 01_ROOM_INTERNALS_AND_MIGRATIONS.md
│   ├── MODULE_13_MODULARIZATION_ARCHITECTURE/
│   │   └── 01_MULTI_MODULE_GRAPH_DESIGN.md
│   ├── MODULE_14_SECURITY_AND_COMPLIANCE/
│   │   └── 01_HARDWARE_KEYSTORE_AND_BIOMETRICS.md
│   ├── MODULE_16_PERFORMANCE_ENGINEERING/
│   │   └── 01_PROFILING_AND_BASELINE_PROFILES.md
│   └── MODULE_25_AI_MOBILE_ARCHITECTURE/
│       └── 01_ON_DEVICE_AI_AND_AGENTS.md
│
├── 02_PRODUCTION_CODEBASE/              # Fully compilable Android SDK 35 Multi-Module Codebase
│   ├── settings.gradle.kts              # Module topology (:core:model, :core:network, :core:security, :feature:banking_atm)
│   ├── build.gradle.kts                 # Root Gradle 9.9.1 / AGP 8.8.0 / Kotlin 2.1.0 DSL
│   ├── core/
│   │   ├── model/                       # Pure Kotlin domain entities & immutable UI models
│   │   ├── network/                     # OkHttp + Retrofit + Mutex token refresh + Idempotency
│   │   └── security/                    # StrongBox Keystore + Biometric CryptoObject manager
│   └── feature/
│       └── banking_atm/                 # Compose MVI ViewModel + UI State + Turbine tests
│
├── 03_SYSTEM_DESIGN_BLUEPRINTS/         # Tier-1 Staff-Level Mobile System Designs
│   ├── 01_WHATSAPP_MESSAGING.md         # Real-time WebSocket/QUIC, E2EE Double Ratchet, Outbox
│   ├── 02_PNC_CARD_FREE_ATM_BANKING.md  # Dynamic QR/NFC HCE ISO 7816-4 APDU, Play Integrity
│   └── 03_OFFLINE_FEED_INFINITE_SCROLL.md # Paging 3 Keyset, Hardware Bitmaps, Media3 Autoplay
│
├── 04_INTERVIEW_PREP_ENGINE/
│   └── MOCK_INTERVIEW_DRILLS.md         # Staff Scenario drills 1-4 with deep model answers
│
└── 05_REPAIR_SCRIPTS_AND_TOOLS/
    └── audit_repairs_guide.sh           # Automated audit remediation script for public repos
```

---

## 🛠️ Compilable Production Codebase Quickstart

The codebase in `02_PRODUCTION_CODEBASE/` is configured with modern Android standards:
- **Build System:** Gradle 9.9.1, Android Gradle Plugin 8.8.0, Kotlin 2.1.0
- **Android SDK:** `compileSdk = 35`, `minSdk = 28`, `targetSdk = 35`
- **Java Target:** Java 17 toolchain

### Build and Run All Test Suites:
```bash
cd "02_PRODUCTION_CODEBASE"

# Run unit tests across all modules
./gradlew test

# Verify Kotlin compilation and dependencies
./gradlew check
```

---

## 📑 Core Technical Deep Dives (Strict 11-Section Framework)

Every chapter in `01_LEARNING_MODULES/` adheres to a strict 11-section framework:
- **A. Fundamentals:** Architectural and runtime foundations
- **B. Technical Definition:** Concrete framework rules, compiler lowerings, and specs
- **C. Internal Working:** Mermaid sequence diagrams, memory models, bytecode
- **D. Production Implementation:** Compilable Kotlin code, dependencies, and tests
- **E. Real-World Scenarios:** Enterprise banking (PNC) and healthcare (ePA) case studies
- **F. Architecture & Trade-offs:** Comparison matrices and decision frameworks
- **G. Common Mistakes:** Anti-patterns and recovery techniques
- **H. Interview Prep:** Basic to Staff-level questions with tricky follow-ups
- **I. Coding Practice:** Real-world algorithmic and architectural coding drills
- **J. Senior Engineer Explanation:** 60-second elevator pitch and 3-minute architectural dive
- **K. Knowledge Validation:** 10 technical MCQs, 5 exercises, and 1 staff architecture challenge

---

## 📱 System Design Blueprints

1. **[Real-Time Messaging Client (WhatsApp / Signal Scale)](03_SYSTEM_DESIGN_BLUEPRINTS/01_WHATSAPP_MESSAGING.md):**
   - Signal Protocol (X3DH & Double Ratchet), Sender Keys for 1,024-member group chats.
   - Cellular Radio Resource Control (RRC) state management and adaptive heartbeat intervals.
   - Transactional Outbox pattern with local SQLite WAL persistence and resumable chunked media pipeline.

2. **[FinTech Card-Free ATM Architecture (PNC Banking Style)](03_SYSTEM_DESIGN_BLUEPRINTS/02_PNC_CARD_FREE_ATM_BANKING.md):**
   - Dual-modal terminal handshake: Dynamic QR challenge-response and NFC Host Card Emulation (`HostApduService`).
   - Hardware StrongBox HSM key generation with `setUserAuthenticationRequired(true)`.
   - RAM zeroization of sensitive arrays (`CharArray`, `ByteArray`) to thwart memory dumps.

3. **[Offline-First Feed & Infinite Scroll Engine (Instagram / Twitter Scale)](03_SYSTEM_DESIGN_BLUEPRINTS/03_OFFLINE_FEED_INFINITE_SCROLL.md):**
   - Keyset/cursor pagination with Jetpack Paging 3 and Room `RemoteMediator`.
   - Hardware Bitmaps (`Bitmap.Config.HARDWARE`) bypassing JVM heap to prevent GC pauses.
   - Media3 ExoPlayer single-player pooling with center-viewport auto-play ($> 60\%$ visibility).

---

## 🤖 On-Device AI Specialization

**[Module 25: AI Mobile Architecture & Local Agents](01_LEARNING_MODULES/MODULE_25_AI_MOBILE_ARCHITECTURE/01_ON_DEVICE_AI_AND_AGENTS.md):**
- **Android AICore & Gemini Nano:** System-level Private Compute Core (PCC) execution with LoRA fine-tuning adapters.
- **Mobile RAG:** Local vector embeddings (MiniLM) and offline vector stores (ObjectBox / SQLite VSS).
- **Model Context Protocol (MCP):** Structured JSON tool calling with mandatory native Android BiometricPrompt security gating.
- **Hybrid Routing:** Dynamic dispatch between on-device SLMs and enterprise cloud gateways (IBM watsonx, AWS Bedrock, Azure OpenAI).

---

## 📄 License
This repository is licensed under the [Apache License 2.0](LICENSE).
