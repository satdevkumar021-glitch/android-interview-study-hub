# SENIOR & STAFF ANDROID INTERVIEW EVALUATION RUBRIC

This rubric is modeled after Tier-1 product-based company interview standards (Google, Meta, Uber, Netflix, Stripe) and will be used to score every mock interview and assessment.

---

## 🎯 SCORING SCALE (1 to 10)

- **1–3: Unsatisfactory / Junior** — Answers recite high-level textbook definitions without knowing runtime behavior. Cannot write idiomatic, compilable Kotlin. Fails to consider edge cases or memory overhead.
- **4–5: Developing / Mid-Level (L4)** — Writes functional code. Understands standard Android Jetpack APIs. Struggles when asked *why* an API works the way it does under the hood. Relies on standard library defaults without analyzing trade-offs.
- **6–7: Competent / Senior Developer (L5)** — Strong command of Kotlin idioms and Android architecture. Able to explain compiler mechanics (inlining, CPS in coroutines). Proactively addresses memory leaks, thread safety, and error handling.
- **8–9: Advanced / Technical Lead & Staff Architect (L6)** — Demonstrates deep mastery of OS internals (Binder, ART GC, Zygote, Linux cgroups). Evaluates architectural decisions from cost, security, battery, network, and team scalability angles. Defends trade-offs with data.
- **10: Exceptional / Principal Engineer** — Flawless system design. Proactively anticipates distributed systems edge cases, hardware security boundaries, and compiler bytecode optimizations. Teaches and articulates complex concepts with effortless clarity.

---

## 📊 EVALUATION PILLARS

```
┌────────────────────────────────────────────────────────────────────────┐
│                          EVALUATION PILLARS                            │
├────────────────────┬────────────────────┬──────────────────────────────┤
│ 1. Core Depth      │ 2. System Design   │ 3. Engineering Rigor         │
│ (Internals/Bytecode│ (Scalability/Edge  │ (Testing/Clean Architecture/ │
│  & Memory Runtimes)│  Cases/Trade-offs) │  Security/Observability)     │
└────────────────────┴────────────────────┴──────────────────────────────┘
```

### Pillar 1: Language & Framework Depth
- Does the candidate understand how Kotlin compiles to JVM bytecode?
- Can they trace execution down to the OS kernel, Binder driver, and ART garbage collector?
- Do they understand memory layout (stack vs heap, object headers, primitive boxing)?

### Pillar 2: Architectural Trade-Offs & Scalability
- Can the candidate defend why they chose MVI over MVVM for a given domain?
- Do they understand the trade-offs of premature modularization and over-abstracted Clean Architecture?
- Can they design systems that gracefully handle offline states, network latency, and memory pressure?

### Pillar 3: Production Security & Compliance
- Does the candidate treat security as a first-class architectural requirement?
- Do they know how to securely use Android Keystore, StrongBox, biometric authentication, and Play Integrity?
- Can they protect sensitive data in memory and prevent reverse-engineering exploits?

### Pillar 4: Communication & Senior Demeanor
- Can the candidate deliver a crisp 60-second summary before diving into technical details?
- Do they actively clarify ambiguous requirements before jumping into solutions?
- Do they demonstrate technical empathy, mentoring instincts, and pragmatic ownership?
