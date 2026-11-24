# 30-DAY SENIOR & STAFF ANDROID PREPARATION ROADMAP

**Candidate:** Satdev Kumar | 8 YOE | Senior Android Developer (IBM)  
**Pace:** 2.5 to 3.0 Hours / Day  
**Completion Rule:** A topic is marked complete **ONLY** after writing/reading compilable code, solving the daily practical challenge, and passing the senior interview defense questions.

---

## 📅 PHASE-WISE CALENDAR OVERVIEW

```
WEEK 1: Language Internals & Android OS Plumbing (Days 1–7)
WEEK 2: Reactive UI, Internals & Concurrency Systems (Days 8–14)
WEEK 3: Enterprise Architecture, Security & Performance (Days 15–21)
WEEK 4: Staff System Design, Coding & Mock Interview Drills (Days 22–30)
```

---

## 📖 DAILY EXECUTION SCHEDULE

### WEEK 1: LANGUAGE RUNTIMES & ANDROID OS PLUMBING

#### Day 01: Kotlin 2.0 Compiler, Bytecode & Memory Architecture
- **Topics:** Kotlin 2.0 K2 compiler frontend (FIR), IR backend, Bytecode disassembly analysis (`javap -c -v`), Lambda allocation overhead (`FunctionN` objects), `inline`, `noinline`, `crossinline`, Non-local returns, Value classes (`@JvmInline`), Boxing and name mangling.
- **Reading:** `01_LEARNING_MODULES/MODULE_02_KOTLIN_INTERNALS/01_KOTLIN_BYTECODE_AND_MEMORY.md`
- **Hands-On Code:** Write an inline benchmark verifying heap allocations before and after inlining; implement a Value class for strongly-typed banking Account IDs and inspect its de-sugared bytecode.
- **Interview Defense:** *"Why does `inline` increase binary size if overused? What does `crossinline` prevent at the bytecode level?"*
- **Estimated Time:** 2.5 Hours

#### Day 02: Generics, Variance & Delegation Mechanics
- **Topics:** Subtyping, Covariance (`out`), Contravariance (`in`), Invariant collections, Type projections, Reified type parameters, Property delegation mechanics (`KProperty`, `$$delegate_0`), `by lazy` thread safety modes (SYNCHRONIZED, PUBLICATION, NONE).
- **Reading:** `01_LEARNING_MODULES/MODULE_02_KOTLIN_INTERNALS/02_GENERICS_AND_DELEGATION.md`
- **Hands-On Code:** Implement a thread-safe encrypted SharedPreferences property delegate using AES-GCM; build a generic covariant Result wrapper.
- **Interview Defense:** *"Why is `Array<T>` invariant while `List<T>` is covariant? How does `by lazy` ensure synchronization under the hood?"*
- **Estimated Time:** 2.5 Hours

#### Day 03: Java Memory Model & Low-Level Concurrency
- **Topics:** Shared memory, CPU cache coherency, Store buffers, Memory barriers, `volatile` visibility and instruction reordering guarantees, Happens-before relationships, Atomic variables (CAS), `synchronized` monitor locks.
- **Reading:** `01_LEARNING_MODULES/MODULE_03_JAVA_FOR_ARCHITECTS/01_JAVA_MEMORY_MODEL.md`
- **Hands-On Code:** Construct a lock-free thread-safe bounded FIFO queue using `AtomicReference` and CAS loops.
- **Interview Defense:** *"Can two threads see stale data even if a boolean flag is updated? What does `volatile` guarantee and what does it NOT guarantee?"*
- **Estimated Time:** 2.5 Hours

#### Day 04: Java-Kotlin Interoperability & Legacy Modernization
- **Topics:** Interop bytecode contracts: `@JvmStatic`, `@JvmOverloads`, `@JvmField`, Platform types (`T!`), Nullability annotation enforcement (`@NonNull`, `@Nullable`), Exception handling (`@Throws`), Calling Kotlin coroutines and suspending functions from legacy Java.
- **Reading:** `01_LEARNING_MODULES/MODULE_03_JAVA_FOR_ARCHITECTS/02_JAVA_KOTLIN_INTEROP.md`
- **Hands-On Code:** Create a dual-language module demonstrating safe interop across Java legacy business classes and modern Kotlin coroutine repositories.
- **Interview Defense:** *"What are platform types and how do they lead to unexpected NPEs in Kotlin? How does `@JvmOverloads` generate constructor bytecode?"*
- **Estimated Time:** 2.0 Hours

#### Day 05: Android OS Architecture, Zygote & App Startup
- **Topics:** Linux kernel sandbox, Init process, Zygote initialization, Pre-loaded classes & resources, Copy-on-Write (CoW), Forking model, `ActivityThread.main()`, `LoadedApk`, Application creation, Context hierarchy (`ContextImpl` vs `ContextWrapper`).
- **Reading:** `01_LEARNING_MODULES/MODULE_04_ANDROID_OS_INTERNALS/01_ZYGOTE_AND_APP_STARTUP.md`
- **Hands-On Code:** Instrument app startup using `Trace.beginSection` to measure time spent across `Application.attachBaseContext`, `Application.onCreate`, and first frame rendering.
- **Interview Defense:** *"Explain the exact sequence of events from when the user taps an app icon on the home screen until the first frame is rendered."*
- **Estimated Time:** 3.0 Hours

#### Day 06: Binder IPC Architecture & System Services
- **Topics:** Linux kernel Binder driver (`/dev/binder`), `ioctl` system calls, 1MB transaction buffer limitation, AIDL, Parcel marshaling, Binder thread pool exhaustion (16-thread ceiling), `TransactionTooLargeException`.
- **Reading:** `01_LEARNING_MODULES/MODULE_04_ANDROID_OS_INTERNALS/02_BINDER_IPC_INTERNALS.md`
- **Hands-On Code:** Implement a multi-process AIDL service exchanging structured banking telemetry data; handle transaction size overflow gracefully.
- **Interview Defense:** *"Why does IPC via Binder use a 1MB transaction buffer? How does the kernel map memory between two isolated application processes?"*
- **Estimated Time:** 3.0 Hours

#### Day 07: Process Lifecycle, LMK & SavedState Restoration
- **Topics:** Linux Low Memory Killer (LMK), `oom_adj_score`, Process priority buckets (Foreground, Visible, Service, Cached), System process death vs Configuration changes, `SavedStateRegistryOwner`, `SavedStateHandle`.
- **Reading:** `01_LEARNING_MODULES/MODULE_04_ANDROID_OS_INTERNALS/03_PROCESS_DEATH_AND_LIFECYCLE.md`
- **Hands-On Code:** Build a production multi-step banking form that survives process death caused by developer options ("Don't keep activities") and low memory kills.
- **Interview Defense:** *"What is the difference between a process killed by the OS and an Activity destroyed during screen rotation? How does `SavedStateHandle` recover state?"*
- **Estimated Time:** 2.5 Hours

---

### WEEK 2: REACTIVE UI & CONCURRENCY SYSTEMS

#### Day 08: Jetpack Compose Compiler & Slot Table Internals
- **Topics:** Compose Compiler plugin, AST manipulation, Synthetic parameters (`$composer`, `$changed`, `$default`), Slot Table structure (Gap Buffer), Groups (RestartGroup, MovableGroup, ReplaceGroup), Positional memoization.
- **Reading:** `01_LEARNING_MODULES/MODULE_06_COMPOSE_DEEP_INTERNALS/01_SLOT_TABLE_AND_COMPILER.md`
- **Hands-On Code:** Inspect Compose compiler metrics output; analyze slot table allocations across recompositions.
- **Interview Defense:** *"How does the Compose runtime know whether to re-execute a Composable function or skip it entirely?"*
- **Estimated Time:** 3.0 Hours

#### Day 09: Compose State System, Stability & Recomposition Skipping
- **Topics:** Snapshot State system, Multi-Version Concurrency Control (MVCC) in UI state, `@Stable` and `@Immutable` contracts, Stability inference of external/standard library types, Compose compiler metrics (`app_debug-composables.txt`), `derivedStateOf`.
- **Reading:** `01_LEARNING_MODULES/MODULE_06_COMPOSE_DEEP_INTERNALS/02_STABILITY_AND_SNAPSHOTS.md`
- **Hands-On Code:** Fix an unstable list data structure causing unintended whole-screen recomposition loops using `@Immutable` wrapper and custom stability configuration.
- **Interview Defense:** *"Why does passing a `List<String>` to a Composable cause it to become unstable? How do you fix it?"*
- **Estimated Time:** 2.5 Hours

#### Day 10: Custom Layouts, SubcomposeLayout & Modifier.Node
- **Topics:** The 3 phases of Compose: Composition, Layout (Measure & Place), Drawing. `Layout` composable, `SubcomposeLayout` performance costs (delayed composition), `Modifier.Node` architecture vs legacy `composed {}`.
- **Reading:** `01_LEARNING_MODULES/MODULE_06_COMPOSE_DEEP_INTERNALS/03_CUSTOM_LAYOUTS_AND_MODIFIERS.md`
- **Hands-On Code:** Build a custom high-performance Staggered Grid Layout using `Modifier.Node` to minimize garbage collection allocations.
- **Interview Defense:** *"Why is `Modifier.composed {}` discouraged in modern Compose? How does `Modifier.Node` improve performance?"*
- **Estimated Time:** 2.5 Hours

#### Day 11: Coroutines Internals & CPS Transformation
- **Topics:** Suspending functions, Continuation-Passing Style (CPS), State machine transformation, `invokeSuspend` label tracking, Coroutine dispatchers (`Dispatchers.Default` vs `Dispatchers.IO`), Worker threads, Context switching.
- **Reading:** `01_LEARNING_MODULES/MODULE_08_COROUTINES_CONCURRENCY/01_CPS_AND_DISPATCHERS.md`
- **Hands-On Code:** Manually write a state-machine simulator mimicking the compiler's generated `$continuation` bytecode for 3 sequential network calls.
- **Interview Defense:** *"Explain step-by-step what happens at the thread and bytecode level when a coroutine hits a suspension point."*
- **Estimated Time:** 3.0 Hours

#### Day 12: Structured Concurrency & Advanced Exception Propagation
- **Topics:** `CoroutineScope`, `CoroutineContext`, `Job` hierarchy, `SupervisorJob` fault isolation, Cancellation propagation, `ensureActive()`, `NonCancellable`, Exception bubbling, `CoroutineExceptionHandler`.
- **Reading:** `01_LEARNING_MODULES/MODULE_08_COROUTINES_CONCURRENCY/02_STRUCTURED_CONCURRENCY.md`
- **Hands-On Code:** Implement an enterprise batch sync worker running 10 concurrent requests where individual failures do not cancel sibling tasks.
- **Interview Defense:** *"What is the exact behavioral difference between `coroutineScope {}` and `supervisorScope {}` when a child throws an exception?"*
- **Estimated Time:** 2.5 Hours

#### Day 13: Kotlin Flow Deep Dive, Backpressure & Hot Streams
- **Topics:** Cold Flow execution model, Hot streams (`StateFlow` vs `SharedFlow`), Backpressure handling strategies (`buffer`, `conflate`, `DROP_OLDEST`), High-frequency operators (`debounce`, `distinctUntilChanged`, `flatMapLatest`).
- **Reading:** `01_LEARNING_MODULES/MODULE_08_COROUTINES_CONCURRENCY/03_FLOW_AND_BACKPRESSURE.md`
- **Hands-On Code:** Build a real-time banking transaction search filter with debouncing, query cancellation, and backpressure conflation.
- **Interview Defense:** *"Why should you collect Flows using `repeatOnLifecycle` instead of `lifecycleScope.launch` in UI layers?"*
- **Estimated Time:** 2.5 Hours

#### Day 14: Enterprise MVI Architecture & State Reducers
- **Topics:** Unidirectional Data Flow (UDF), Model-View-Intent (MVI), Immutable UI State modeling, Reducer state machines, One-time side-effects (Channel vs SharedFlow), Clean Architecture layer isolation.
- **Reading:** `01_LEARNING_MODULES/MODULE_07_ENTERPRISE_ARCHITECTURE/01_MVI_AND_UDF.md`
- **Hands-On Code:** Implement a production-grade MVI BaseViewModel with thread-safe atomic state transitions and side-effect dispatching.
- **Interview Defense:** *"How do you handle one-time UI events (like showing a snackbar or navigation) cleanly in MVI without event loss or duplicate execution?"*
- **Estimated Time:** 3.0 Hours

---

### WEEK 3: ARCHITECTURE, SECURITY & PERFORMANCE

#### Day 15: Dependency Injection at Scale: Dagger 2 & Hilt
- **Topics:** Inversion of Control, Dagger compiler generation (`_Factory`, `_MembersInjector`, `DoubleCheck` provider), Component tree lifecycle, Scope caching mechanics, Multibindings (`@IntoMap`, `@IntoSet`), `@AssistedInject`.
- **Reading:** `01_LEARNING_MODULES/MODULE_10_DEPENDENCY_INJECTION/01_DAGGER_HILT_INTERNALS.md`
- **Hands-On Code:** Implement dynamic plugin loading via Dagger Multibindings; trace the generated Java classes created by the Dagger KSP processor.
- **Interview Defense:** *"How does Dagger ensure zero reflection at runtime? What is the role of the `DoubleCheck` provider?"*
- **Estimated Time:** 3.0 Hours

#### Day 16: Resilient Networking & Concurrent Token Refresh
- **Topics:** OkHttp connection pooling, HTTP/2 multiplexing, OkHttp Interceptors (`RealInterceptorChain`), Authenticator, Mutex-synchronized token refresh (preventing stampeding herds during 401 token expiration), Idempotency headers.
- **Reading:** `01_LEARNING_MODULES/MODULE_11_NETWORKING_INTEGRATION/01_RESILIENT_NETWORKING.md`
- **Hands-On Code:** Build a production-grade `AuthInterceptor` and `TokenAuthenticator` using Kotlin `Mutex` to refresh expired OAuth2 tokens concurrently.
- **Interview Defense:** *"If 20 network requests fail simultaneously with HTTP 401, how do you ensure only ONE token refresh call is executed while holding the other 19?"*
- **Estimated Time:** 3.0 Hours

#### Day 17: Offline-First Persistence, Room & Database Performance
- **Topics:** SQLite Write-Ahead Logging (WAL), Room compile-time schema verification, Database migrations (`Migration` vs `AutoMigration`), Indexing strategies, SQLCipher encryption overhead.
- **Reading:** `01_LEARNING_MODULES/MODULE_12_DATABASE_PERSISTENCE/01_ROOM_INTERNALS_AND_MIGRATIONS.md`
- **Hands-On Code:** Write a multi-version Room migration test suite using `MigrationTestHelper` verifying schema upgrade integrity from v1 to v3.
- **Interview Defense:** *"How does SQLite Write-Ahead Logging (WAL) improve read/write concurrency? What happens if an automated Room migration encounters dropped columns?"*
- **Estimated Time:** 2.5 Hours

#### Day 18: Background Execution & WorkManager Architecture
- **Topics:** Doze mode, App Standby Buckets, Android 14/15/16 Foreground Service restrictions (explicit types), WorkManager architecture, JobScheduler integration, Expedited work, Unique work chains.
- **Reading:** `01_LEARNING_MODULES/MODULE_13_BACKGROUND_PROCESSING/01_WORKMANAGER_INTERNALS.md`
- **Hands-On Code:** Implement an expedited background sync worker with exponential backoff, network constraints, and custom Hilt `WorkerFactory`.
- **Interview Defense:** *"When would you choose WorkManager over a Foreground Service or AlarmManager for a periodic syncing task?"*
- **Estimated Time:** 2.5 Hours

#### Day 19: Hardware-Backed Security, Keystore & Biometrics
- **Topics:** Android Keystore Provider, Trusted Execution Environment (TEE), StrongBox Keymaster, Hardware key attestation, AES-256 GCM authenticated encryption, BiometricPrompt `CryptoObject` binding.
- **Reading:** `01_LEARNING_MODULES/MODULE_14_SECURITY_AND_COMPLIANCE/01_HARDWARE_KEYSTORE_AND_BIOMETRICS.md`
- **Hands-On Code:** Build a biometric authentication manager that signs a transaction payload using an authenticated hardware-backed private key.
- **Interview Defense:** *"How does Android Keystore ensure that private keys can never be extracted from the device memory, even on a rooted device?"*
- **Estimated Time:** 3.0 Hours

#### Day 20: App Hardening, Play Integrity & Reverse Engineering Defense
- **Topics:** Google Play Integrity API (Device, App, Account licensing), R8 full-mode obfuscation, ProGuard rule optimization, SSL certificate pinning with backup pins, Mitigating Frida hooking and root exploits.
- **Reading:** `01_LEARNING_MODULES/MODULE_14_SECURITY_AND_COMPLIANCE/02_APP_HARDENING_INTEGRITY.md`
- **Hands-On Code:** Implement client-side SSL pinning with fallback certificate rotation and network security configuration XML.
- **Interview Defense:** *"Why is local client-side root detection fundamentally flawed? How does Play Integrity provide cryptographic server-side attestation?"*
- **Estimated Time:** 2.5 Hours

#### Day 21: Performance Engineering, Startup Profiling & Memory Leaks
- **Topics:** Startup profiling: TTID vs TTFD, App Startup initializer graph, Memory leaks triage (LeakCanary internals, reference queues), ANR root cause analysis via traces.txt, Perfetto custom trace slices.
- **Reading:** `01_LEARNING_MODULES/MODULE_16_PERFORMANCE_ENGINEERING/01_PROFILING_AND_LEAK_TRIAGE.md`
- **Hands-On Code:** Triage a deliberate memory leak in a ViewModel using Android Studio Memory Profiler; extract and analyze the heap dump path.
- **Interview Defense:** *"How do you diagnose an ANR in production when the crash stack points to a Handler queue or Choreographer frame callback?"*
- **Estimated Time:** 3.0 Hours

---

### WEEK 4: STAFF SYSTEM DESIGN, CODING & MOCK INTERVIEWS

#### Day 22: Baseline Profiles & Frame Jank Elimination
- **Topics:** Ahead-of-Time (AOT) vs Just-in-Time (JIT) compilation, ART Cloud Profiles, Jetpack Macrobenchmark, Generating Baseline Profiles, Choreographer 120Hz frame deadlines (8.33ms).
- **Reading:** `01_LEARNING_MODULES/MODULE_16_PERFORMANCE_ENGINEERING/02_BASELINE_PROFILES_AND_JANK.md`
- **Hands-On Code:** Write a Macrobenchmark test to generate Baseline Profiles for cold app startup; measure startup reduction percentage.
- **Interview Defense:** *"What is the exact mechanism by which Baseline Profiles accelerate app cold startup by up to 30%?"*
- **Estimated Time:** 2.5 Hours

#### Day 23: Multi-Module Architecture & Build Performance
- **Topics:** Modularization paradigms: Feature-by-layer vs Feature-by-feature, Core modules (`:core:network`, `:core:database`), API vs Implementation modules (`:api` and `:impl`), Gradle Configuration Cache, Convention plugins (`build-logic`).
- **Reading:** `01_LEARNING_MODULES/MODULE_19_MULTIMODULE_ARCHITECTURE/01_ENTERPRISE_MODULARIZATION.md`
- **Hands-On Code:** Configure a standalone Gradle convention plugin in `build-logic` applying common compiler and lint configurations.
- **Interview Defense:** *"How do you structure dependencies in a 60-module app to avoid circular dependencies and prevent transitive classpath leaks?"*
- **Estimated Time:** 3.0 Hours

#### Day 24: Staff System Design: Real-Time Chat System (WhatsApp/Signal)
- **Topics:** WebSocket lifecycle, SQLite storage schema for chats/messages, Offline outbox pattern, Delivery ACK state machine, End-to-end encryption key exchange, Push notification wakeups.
- **Reading:** `03_SYSTEM_DESIGN_BLUEPRINTS/01_WHATSAPP_MESSAGING.md`
- **Deliverable:** Complete architecture diagram, database schema, sequence diagrams, failure recovery, and trade-off defense.
- **Interview Defense:** *"Design an offline-first real-time chat application handling guaranteed message delivery under volatile network conditions."*
- **Estimated Time:** 3.5 Hours

#### Day 25: Staff System Design: FinTech & Card-Free ATM Architecture
- **Topics:** PNC-style mobile banking: Zero-trust architecture, Dynamic device attestation, TOTP tokenization, Secure NFC/QR handshake with ATM hardware, RAM data zeroization, PCI-DSS compliance.
- **Reading:** `03_SYSTEM_DESIGN_BLUEPRINTS/02_PNC_CARD_FREE_ATM_BANKING.md`
- **Deliverable:** Full FinTech system design covering client, secure gateway, ATM terminal protocol, and failure rollbacks.
- **Interview Defense:** *"Design a secure Card-Free ATM cash withdrawal feature from an Android banking app, ensuring zero replay attacks and physical proximity validation."*
- **Estimated Time:** 3.5 Hours

#### Day 26: Staff System Design: Adaptive Video Streaming Feed (TikTok/YouTube)
- **Topics:** Media3 ExoPlayer integration, Chunked pre-fetching, LRU disk/memory caching of video segments, Adaptive Bitrate (ABR) switching, SurfaceView lifecycle, Frame drop elimination.
- **Reading:** `03_SYSTEM_DESIGN_BLUEPRINTS/03_YOUTUBE_VIDEO_STREAMING.md`
- **Deliverable:** Multi-module architecture for seamless vertical video feed with instant playback.
- **Interview Defense:** *"Design a video feed that pre-fetches the next 3 videos without causing memory exhaustion or network contention during active playback."*
- **Estimated Time:** 3.5 Hours

#### Day 27: High-Yield Coding & DSA in Kotlin
- **Topics:** Two Pointers, Sliding Window, Monotonic Stack, LRU Cache from scratch in Kotlin, Tree BFS/DFS, Top K Frequent Elements, Interval Scheduling.
- **Reading:** `01_LEARNING_MODULES/MODULE_22_DATA_STRUCTURES_KOTLIN/01_HIGH_YIELD_PATTERNS.md`
- **Coding Practice:** Solve 3 interview problems under 30-minute timers with edge-case and complexity analysis.
- **Estimated Time:** 2.5 Hours

#### Day 28: On-Device AI & Mobile-Agent Architectures
- **Topics:** Google LiteRT (TFLite) runtime, MediaPipe GenAI SDK, Android AICore on-device Gemini Nano, Secure Mobile-to-Agent Gateway (LangGraph/watsonx), Streaming token responses, Tool calling.
- **Reading:** `01_LEARNING_MODULES/MODULE_25_AI_MOBILE_ARCHITECTURE/01_ON_DEVICE_AI_AND_AGENTS.md`
- **Hands-On Code:** Build a streaming LLM response client using Ktor SSE (Server-Sent Events) with structured JSON function calling.
- **Interview Defense:** *"How do you design a mobile-to-AI agent gateway that prevents client API key exposure while supporting low-latency token streaming?"*
- **Estimated Time:** 3.0 Hours

#### Day 29: Leadership, Behavioral & STAR Scenarios
- **Topics:** Behavioral frameworks for Senior/Staff roles: Architecture disagreements, Managing production outages, Cross-team technical ownership, Refactoring legacy technical debt without business downtime.
- **Reading:** `04_INTERVIEW_PREP_ENGINE/BEHAVIORAL_STAR_PLAYBOOK.md`
- **Interview Practice:** Record and polish 5 STAR-format case studies based on your IBM Consulting background (German ePA, PNC Mobile Banking).
- **Estimated Time:** 2.5 Hours

#### Day 30: Comprehensive Mock Interview Simulation & Evaluation
- **Simulation:** Full 90-minute technical interview covering:
  1. Kotlin & Android OS internals (30 mins)
  2. Live coding challenge in Kotlin (20 mins)
  3. Staff-level Android System Design (30 mins)
  4. Behavioral & Architecture Defense (10 mins)
- **Outcome:** Technical scoring out of 10, identified blind spots, and final interview readiness verdict.
- **Estimated Time:** 3.0 Hours

---
*Next: Proceed to [01_LEARNING_MODULES/MODULE_02_KOTLIN_INTERNALS/01_KOTLIN_BYTECODE_AND_MEMORY.md](file:///Users/satdevkumar/Desktop/Android%20Pre%20Main/01_LEARNING_MODULES/MODULE_02_KOTLIN_INTERNALS/01_KOTLIN_BYTECODE_AND_MEMORY.md).*
