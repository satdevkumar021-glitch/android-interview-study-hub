# SENIOR & STAFF ANDROID MOCK INTERVIEW DRILL LOGS
## Comprehensive Scenario Questions, Technical Rubrics & Model Architectures

---

### DRILL 01: CONCURRENCY & EXCEPTION RESILIENCE (L6 STAFF LEVEL)

#### The Problem Scenario
In an enterprise Android application with a multi-module architecture, a background sync engine is responsible for syncing 4 independent data streams concurrently:
1. `syncUserProfile()` (critical user identity payload)
2. `syncAccountBalances()` (financial ledger balances)
3. `syncMarketingBanners()` (promotional carousels)
4. `syncSecurityAlerts()` (fraud risk signals)

An engineer implements this using:
```kotlin
suspend fun executeSync() = coroutineScope {
    val profileDeferred = async { syncUserProfile() }
    val balancesDeferred = async { syncAccountBalances() }
    val bannersDeferred = async { syncMarketingBanners() }
    val alertsDeferred = async { syncSecurityAlerts() }
    awaitAll(profileDeferred, balancesDeferred, bannersDeferred, alertsDeferred)
}
```
During a promotional campaign, `syncMarketingBanners()` throws an `IOException: 504 Gateway Timeout`. In production, the entire sync operation aborts, cancelling `syncUserProfile()` and `syncAccountBalances()` mid-flight and logging the user out.

#### The Technical Questions
1. Why does `coroutineScope` cancel all sibling coroutines when a single child fails, and how does structured concurrency propagate exceptions up the Job hierarchy?
2. If the engineer replaces `coroutineScope` with `supervisorScope`, what happens when `async` throws an exception, and why does wrapping `awaitAll()` in a `try/catch` block still fail to prevent app crashes in certain configurations?
3. What is the production-grade architectural solution that guarantees fault tolerance for optional data streams without compromising structured concurrency?

#### The Staff-Level Model Answer
1. **Parent-Child Job Hierarchy & Cancellation Propagation:**
   - Under standard `coroutineScope`, child coroutines inherit a standard `Job` from the scope.
   - When a child coroutine fails with an unhandled exception (other than `CancellationException`), it cancels its parent Job.
   - The parent Job immediately cancels all other active children before re-throwing the exception.
2. **The `supervisorScope` + `async` Trap:**
   - In a `supervisorScope`, failure of a child does not cancel siblings or the supervisor.
   - However, when using `async` inside a `supervisorScope`, if `async` fails, the exception is stored inside the `Deferred` instance and thrown upon invoking `.await()`.
   - Crucially, if an `async` coroutine is launched directly in a scope with an unhandled exception handler, or if the coroutine builder failure occurs before `await()` is called, some coroutine exception handlers will log the failure as uncaught.
3. **The Production Pattern:**
   Use `supervisorScope` and isolate non-critical tasks with individual `runCatching` blocks or an explicit `Result<T>` mapping:
   ```kotlin
   suspend fun executeResilientSync(): SyncSummary = supervisorScope {
       val profileDeferred = async { runCatching { syncUserProfile() } }
       val balancesDeferred = async { runCatching { syncAccountBalances() } }
       val bannersDeferred = async { runCatching { syncMarketingBanners() } }
       val alertsDeferred = async { runCatching { syncSecurityAlerts() } }

       SyncSummary(
           profile = profileDeferred.await().getOrNull(),
           balances = balancesDeferred.await().getOrNull(),
           banners = bannersDeferred.await().getOrDefault(emptyList()),
           alerts = alertsDeferred.await().getOrNull()
       )
   }
   ```

---

### DRILL 02: COMPOSE STABILITY & MULTI-MODULE SKIPPING (L6/L7 LEVEL)

#### The Problem Scenario
In an Android application featuring an immunization timeline (German Healthcare ePA style), an engineer builds a high-frequency LazyColumn list displaying medical records. Each item is rendered using:
```kotlin
@Composable
fun ImmunizationRow(
    record: ImmunizationRecord,
    onItemClick: (ImmunizationRecord) -> Unit,
    modifier: Modifier = Modifier
) { ... }
```
Where `ImmunizationRecord` is defined in `:core:model`:
```kotlin
data class ImmunizationRecord(
    val id: String,
    val vaccineName: String,
    val dateEpoch: Long,
    val tags: List<String>
)
```
During scroll profiling, the Compose Compiler Metrics report reveals:
`restartable skippable fun ImmunizationRow` is marked **restartable only, NOT skippable**! Scrolling drops frames on low-end devices.

#### The Technical Questions
1. Why did the Compose Compiler mark `ImmunizationRow` as unskippable despite `ImmunizationRecord` being a Kotlin `data class` with immutable `val` properties?
2. How does multi-module architecture affect Compose stability inference when model classes reside in an external module that does not apply the Compose Compiler Gradle plugin?
3. Detail three production approaches to restore stability and enable recomposition skipping.

#### The Staff-Level Model Answer
1. **The Interface Stability Trap:**
   - `tags: List<String>` is an interface (`java.util.List`). The Compose compiler cannot guarantee that the underlying runtime implementation is immutable (it could be an instance of `ArrayList` mutated elsewhere).
   - Therefore, the Compose compiler infers `ImmunizationRecord` as **Unstable**. When any parent state changes, `ImmunizationRow` cannot be skipped.
2. **Multi-Module Compiler Boundary:**
   - If `:core:model` is a pure Kotlin library without the Compose Compiler plugin, it produces no Compose stability metadata (`$stability`).
   - Consumer modules (such as `:feature:timeline`) must treat all classes from `:core:model` as **unstable** by default unless explicitly configured.
3. **The Production Remediation:**
   - *Option A (Modern Gradle Compose Configuration):* Add a root `stability_configuration.txt` file and register it in `build.gradle.kts`:
     ```kotlin
     composeCompiler {
         stabilityConfigurationFile = rootProject.file("stability_config.conf")
     }
     ```
     With contents: `java.util.List` or `com.enterprise.model.*`.
   - *Option B (Persistent Collections):* Use `kotlinx.collections.immutable.ImmutableList<String>` which is recognized as stable by Compose.
   - *Option C (UI Presentation Model):* Map domain entities to feature-level UI models annotated with `@Immutable`.

---

### DRILL 03: TRANSACTIONAL OUTBOX & SQLITE WAL (L6/L7 LEVEL)

#### The Problem Scenario
An enterprise banking application writes high-value transactions (ATM staging, wire transfers) to an offline SQLite Transactional Outbox table. Three concurrent actors access SQLite simultaneously:
1. `WorkManager` draining outbox records on `Dispatchers.IO`.
2. UI Thread observing transactions via Room `Flow<List<TransactionRecord>>`.
3. An active WebSocket coroutine writing incoming delta updates.

During network handover in an elevator:
- **Issue A:** UI freezes (150ms–400ms frame drops) occur while the user types or scrolls.
- **Issue B:** A transfer is executed twice on the core backend when the connection switches mid-flight.

#### The Staff-Level Model Answer
1. **Root Cause Analysis:**
   - *Issue A:* SQLite default Rollback Journal mode acquires an exclusive database lock during writes, stalling Room read queries. High-frequency WebSocket writes trigger Room's `InvalidationTracker` repeatedly.
   - *Issue B:* Two Generals Problem. Wi-Fi severed before HTTP 200 OK returned. Client experienced a socket timeout and retried on Cellular. The backend lacked distributed deduplication on `idempotency_key`, executing a double debit.
2. **Architectural Remediation:**
   - Enable SQLite **Write-Ahead Logging (WAL)**:
     `RoomDatabase.Builder.setJournalMode(JournalMode.WRITE_AHEAD_LOGGING)`. Readers and writers run concurrently without blocking.
   - Batch incoming WebSocket updates into a single transaction every 100ms.
   - Enforce client-generated cryptographic `Idempotency-Key: UUIDv4` attached as an HTTP header and validated atomically in the backend Redis cache.
   - Two-phase outbox lease: query server transaction status (`GET /transfers/status/{key}`) before re-dispatching on timeout.

---

### DRILL 04: MEMORY LEAK TRIAGE & COROUTINE LIFECYCLES (L6/L7 LEVEL)

#### The Problem Scenario
After navigating across multiple feature screens, users report severe frame drops and `OutOfMemoryError` (OOM) crashes on 4GB devices:
1. $140\text{MB}$ of `Bitmap` instances remain allocated on the Dashboard.
2. Destroyed `KycDocumentScanActivity` instances are pinned by an active Coroutine `Job`.
3. Legacy code used `GlobalScope.launch` and a `SharedFlow` with `replay = 5`.

#### The Staff-Level Model Answer
1. **Reference Retention Chain:**
   - `GlobalScope` creates a top-level `Job` with no parent bound to the process lifecycle.
   - A coroutine launched in `GlobalScope` captures references to outer variables in its closure.
   - If that closure references an `Activity` callback or View, the entire Activity, its `Window`, `DecorView`, and View hierarchy are retained in heap memory indefinitely.
2. **Bitmap Allocation & GC Pauses:**
   - Prior to Android 8.0, bitmaps lived on the Dalvik/ART heap.
   - On Android 8.0+, bitmaps live on the native C++ heap, but pixel references are tracked by Java `Finalizer` or `Cleaner` objects.
   - Rapid allocation of standard `ARGB_8888` bitmaps causes heavy memory churning and concurrent mark-sweep (CMS) GC pauses.
   - **`Bitmap.Config.HARDWARE`** stores pixel buffers directly in GPU memory (`AHardwareBuffer`). Pixel memory is immutable and inaccessible to the CPU, completely bypassing JVM heap memory management and eliminating GC pauses.
3. **Step-by-Step Remediation Plan:**
   - Deprecate and ban `GlobalScope` via ArchUnit / ktlint rules. Enforce `viewModelScope` and `lifecycleScope`.
   - Audit `SharedFlow` replay caches; replace with `Channel` for single-time events or `StateFlow` for state.
   - Integrate Coil image loader with `Bitmap.Config.HARDWARE` and strict viewport downsampling.
