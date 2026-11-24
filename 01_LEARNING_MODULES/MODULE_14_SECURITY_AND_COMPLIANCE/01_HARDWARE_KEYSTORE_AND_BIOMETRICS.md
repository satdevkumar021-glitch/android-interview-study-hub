# MODULE 14: MOBILE SECURITY & BANKING COMPLIANCE
## CHAPTER 01: HARDWARE-BACKED ANDROID KEYSTORE, STRONGBOX & BIOMETRIC CRYPTO-OBJECT BINDING

---

### A. FUNDAMENTALS

#### 1. What is the Concept?
Mobile security in enterprise and banking applications relies on hardware-enforced trust boundaries. The Android platform provides:
- **Android Keystore Provider:** A dedicated cryptographic service where private and secret keys are generated, stored, and operated on inside isolated hardware modules: either a **Trusted Execution Environment (TEE)** running on the main SoC, or a dedicated Hardware Security Module (HSM) called **StrongBox**.
- **Cryptographic Key Attestation:** Proving to a remote banking server that a key was genuinely generated inside hardware and is protected by Android Keystore.
- **Biometric Crypto-Object Binding:** Binding a cryptographic operation (`Cipher`, `Signature`, or `Mac`) directly to user authentication via `BiometricPrompt`, ensuring that a cryptographic transaction signature can **only** be executed if the user physically authorizes it with biometric verification (fingerprint or 3D face unlock).

#### 2. Why Does It Exist?
If private cryptographic keys or session tokens are stored in standard application memory (RAM) or in files on disk (even in private storage `/data/data/com.app/`), they are vulnerable to:
1. **Root Exploits:** A rooted Android device or an attacker with physical USB debugging access (`adb pull`, Frida, Xposed) can dump process RAM or read local storage files.
2. **Key Extraction Attacks:** Traditional software cryptography decrypts keys in regular user-space RAM, making keys readable via heap inspection.

The Android Keystore ensures that **the private key material NEVER enters the Android OS user space or RAM**. The key is generated inside the secure hardware chip; encryption and signing operations are executed inside the chip; and only the resulting cipher text or digital signature is returned to the app.

#### 3. What Problem Does It Solve?
- **Root-Resilient Cryptography:** Even if the Android OS kernel is fully compromised by root malware, an attacker cannot extract private keys protected by TEE or StrongBox.
- **Biometric Spoofing Elimination:** Bypassing biometric UI prompts in software (e.g., hooking `onAuthenticationSucceeded()` with Frida) is useless because the backend transaction requires a cryptographic signature that **only the hardware chip can compute**, and the hardware chip only unlocks the key upon physical sensor verification.
- **Malicious Biometric Enrolment Prevention:** If an attacker learns the device PIN and adds their own fingerprint in device settings, keys flagged with `setInvalidatedByBiometricEnrollment(true)` are automatically rendered permanently unusable.

#### 4. Where is It Used?
- Banking & FinTech: Card-Free ATM cash withdrawals, contactless NFC payments, credit card tokenization, and high-value wire transfers.
- Healthcare (German ePA): Digital health identity signing, patient record decryption, and Passkey / FIDO2 authentication.

#### 5. What Happens If We Do Not Use / Understand It?
- **PCI-DSS Compliance Violations:** Failing mobile banking security audits by leaving plaintext cryptographic keys or un-zeroized PIN arrays in heap memory.
- **GCM Nonce-Reuse Disaster:** Reusing an Initialization Vector (IV) in AES-GCM encryption completely destroys the security of AES-GCM, allowing attackers to recover plaintext without knowing the key.
- **Replay Attacks:** Generating signatures without backend-provided server nonces or challenges.

---

### B. TECHNICAL DEFINITION

> **Hardware-Backed Keystore & Biometric Cryptographic Binding:**
> The **Android Keystore** provides cryptographic isolation by delegating key management to hardware-backed Keymaster / Keymint HAL implementations. 
> In **StrongBox** (introduced in Android 9 / API 28), keys are housed on a dedicated tamper-resistant hardware microcontroller with its own isolated CPU, RAM, and flash storage (e.g., Titan M chip).
> When configuring `KeyGenParameterSpec.setUserAuthenticationRequired(true)`, the private key is locked by the Keymaster daemon. When the user successfully authenticates via `BiometricPrompt`, the biometric daemon passes an authenticated **Auth Token** to Keymaster, unlocking the private key for a single cryptographic operation inside the hardware boundary.

---

### C. INTERNAL WORKING & ARCHITECTURE

```mermaid
sequenceDiagram
    autonumber
    actor User as User Fingerprint
    participant BioPrompt as BiometricPrompt Framework
    participant AppProcess as Banking App (User Space)
    participant KeystoreDaemon as Keystore Daemon (keystored)
    participant TEE as TEE / StrongBox Hardware Chip
    participant BankServer as Remote Banking Server

    BankServer->>AppProcess: Sends unique server nonce challenge
    AppProcess->>KeystoreDaemon: Requests Cipher initialization for alias "atm_private_key"
    KeystoreDaemon->>TEE: Checks key policy
    Note over TEE: Key policy requires physical biometric authentication!
    TEE-->>KeystoreDaemon: Returns Locked CryptoObject handle
    KeystoreDaemon-->>AppProcess: Wraps in BiometricPrompt.CryptoObject(cipher)
    
    AppProcess->>BioPrompt: Calls authenticate(cryptoObject)
    User->>BioPrompt: Scans Fingerprint on physical sensor
    Note over BioPrompt: Hardware biometric sensor validates minutiae
    BioPrompt->>TEE: Sends hardware AuthToken (HMAC-SHA256 signed by sensor)
    Note over TEE: TEE verifies AuthToken; UNLOCKS key inside hardware!
    
    BioPrompt->>AppProcess: onAuthenticationSucceeded(result)
    AppProcess->>AppProcess: cryptoObject.cipher.doFinal(nonceBytes)
    Note over AppProcess,TEE: Cipher execution happens inside TEE! Key never leaves chip!
    AppProcess->>BankServer: Submits signed transaction payload
    BankServer->>BankServer: Verifies signature with stored Public Key
```

#### 1. TEE vs StrongBox: The Hardware Distinction
- **Trusted Execution Environment (TEE):**
  - An isolated execution environment implemented directly on the main SoC (ARM TrustZone).
  - Shares physical silicon and memory buses with the Android OS, but memory is partitioned and cryptographically encrypted at the hardware level.
  - Fast execution; supports large key sizes.
- **StrongBox Keymaster (Dedicated HSM):**
  - A physically separate, discrete microcontroller (e.g., Google Titan M, Samsung Knox Vault).
  - Completely separate package, separate CPU, separate RAM, and physical tamper-resistant sensors (detecting laser fault injection, voltage glitching, and temperature attacks).
  - True hardware security boundary. Limited storage and clock speed, but maximum defense against sophisticated physical laboratory attacks.

#### 2. AES-256 GCM: The Nonce-Reuse Catastrophe
AES-GCM (Galois/Counter Mode) is an **Authenticated Encryption with Associated Data (AEAD)** cipher. It provides both confidentiality (encryption) and integrity (authentication tag).
- **The Rule:** An Initialization Vector (IV) / Nonce must **NEVER be used more than once with the same key**.
- *The Danger:* If two distinct plaintexts are encrypted with the same AES-GCM key and the exact same IV, an attacker can XOR the ciphertexts together to eliminate the keystream and completely recover the authentication hash subkey, breaking the encryption entirely.
- *The Defense:* **Never supply a hardcoded IV.** Let Android Keystore or `SecureRandom` generate a cryptographically secure 12-byte (96-bit) IV on every single encryption pass:
```kotlin
val cipher = Cipher.getInstance("AES/GCM/NoPadding")
cipher.init(Cipher.ENCRYPT_MODE, secretKey) // Android Keystore auto-generates secure random IV!
val iv = cipher.iv // Extract 12-byte IV generated by Keystore
```

#### 3. RAM Zeroization (Defense in Depth)
Garbage collection does not overwrite object memory; it simply marks memory blocks as available for future allocations. Sensitive data stored in `String` (which is immutable and interned on the heap) can persist in RAM dumps for hours.
*Enterprise Standard:* Store sensitive credentials (PINs, raw entropy, private key bytes) in primitive `CharArray` or `ByteArray`, and overwrite them with zeros immediately after use:
```kotlin
val pin = charArrayOf('4', '8', '2', '9')
try {
    processPin(pin)
} finally {
    pin.fill('0') // ZEROIZATION: Completely clears data from RAM!
}
```

---

### D. PRODUCTION IMPLEMENTATION

#### Production-Grade Hardware Keystore Manager with StrongBox & Biometric Binding
Below is a compilable, production-grade security manager demonstrating:
1. Hardware Keystore key generation with StrongBox fallback.
2. Invalidation on new biometric enrollment (`setInvalidatedByBiometricEnrollment(true)`).
3. Secure biometric transaction signing with payload zeroization.

```kotlin
package com.ibm.banking.security.keystore

import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import androidx.biometric.BiometricPrompt
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.PrivateKey
import java.security.PublicKey
import java.security.Signature
import java.security.spec.ECGenParameterSpec

/**
 * Enterprise Hardware-Backed Security Engine for Card-Free ATM Authorizations.
 * Operates inside TEE or StrongBox Keymaster.
 */
class HardwareKeystoreManager(
    private val keyAlias: String = "banking_atm_auth_key"
) {

    private val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }

    init {
        ensureHardwareKeyPairGenerated()
    }

    /**
     * Generates a Secp256r1 Elliptic Curve (EC) key pair inside hardware.
     * Enforces:
     * - Physical user authentication via BiometricPrompt.
     * - Automatic key revocation if device user registers a new fingerprint.
     * - StrongBox HSM backing where supported by device hardware.
     */
    private fun ensureHardwareKeyPairGenerated() {
        if (keyStore.containsAlias(keyAlias)) return

        val keyPairGenerator = KeyPairGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_EC,
            ANDROID_KEYSTORE
        )

        val builder = KeyGenParameterSpec.Builder(
            keyAlias,
            KeyProperties.PURPOSE_SIGN or KeyProperties.PURPOSE_VERIFY
        )
            .setAlgorithmParameterSpec(ECGenParameterSpec("secp256r1"))
            .setDigests(KeyProperties.DIGEST_SHA256)
            // Mandates physical biometric authentication for every signature operation!
            .setUserAuthenticationRequired(true)
            // Permanently invalidates the key if a new fingerprint is enrolled in device settings!
            .setInvalidatedByBiometricEnrollment(true)

        // Attempt StrongBox hardware allocation on supported devices (Android 9+ / API 28)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            try {
                builder.setIsStrongBoxBacked(true)
                keyPairGenerator.initialize(builder.build())
                keyPairGenerator.generateKeyPair()
                return
            } catch (e: Exception) {
                // StrongBox not present on device: Fall back to standard TEE TrustZone
                builder.setIsStrongBoxBacked(false)
            }
        }

        keyPairGenerator.initialize(builder.build())
        keyPairGenerator.generateKeyPair()
    }

    /**
     * Initializes an authenticated Signature object for BiometricPrompt.
     */
    fun createBiometricCryptoObject(): BiometricPrompt.CryptoObject {
        val privateKey = (keyStore.getEntry(keyAlias, null) as KeyStore.PrivateKeyEntry).privateKey
        val signature = Signature.getInstance("SHA256withECDSA")
        signature.initSign(privateKey)
        return BiometricPrompt.CryptoObject(signature)
    }

    /**
     * Signs the transaction payload using the unlocked hardware Signature.
     * Memory zeroization is applied to input bytes.
     */
    fun signTransactionPayload(
        cryptoObject: BiometricPrompt.CryptoObject,
        challengePayload: ByteArray
    ): ByteArray {
        val signature = cryptoObject.signature 
            ?: throw IllegalStateException("CryptoObject does not contain an authenticated Signature")

        return try {
            signature.update(challengePayload)
            signature.sign() // Executed inside TEE hardware!
        } finally {
            // Defensive RAM zeroization: Overwrite challenge payload buffer
            challengePayload.fill(0.toByte())
        }
    }

    fun getPublicKey(): PublicKey {
        return keyStore.getCertificate(keyAlias).publicKey
    }

    fun deleteKey() {
        if (keyStore.containsAlias(keyAlias)) {
            keyStore.deleteEntry(keyAlias)
        }
    }

    companion object {
        private const val ANDROID_KEYSTORE = "AndroidKeyStore"
    }
}
```

---

### E. REAL-WORLD SCENARIOS

1. **FinTech (ATM Card-Free Withdrawal Hardware Challenge):**
   When withdrawing cash at an ATM terminal, the bank backend provides a cryptographic challenge nonce (`sha256_challenge`). The Android app passes this nonce to `HardwareKeystoreManager`, prompting the user for biometric authorization. The resulting ECDSA digital signature is submitted to the ATM terminal via QR scan. The bank server verifies the signature using the device's registered public key. An attacker running Frida on the phone cannot spoof the signature because the private key is held in physical silicon.
2. **Healthcare (German ePA Smartcard & Biometric Binding):**
   In German digital health applications, patient medical timeline access requires proof of possession of the registered device. Biometric authentication unlocks a TEE-backed private key to decrypt patient FHIR records. Setting `setInvalidatedByBiometricEnrollment(true)` guarantees that if the device is stolen and unlocked with a newly added fingerprint, all medical decryption keys are instantly destroyed by the hardware chip.
3. **Defense Against Evil Maid / Physical Stolen Device Attacks:**
   If a device is stolen while powered on, an attacker might freeze the RAM chip with liquid nitrogen to preserve volatile memory states (Cold Boot Attack). Because keys stored in StrongBox Keymaster reside on an independent silicon die with internal power monitors, the key material is physically unreachable from the main memory bus.

---

### F. ARCHITECTURE AND TRADE-OFFS

| Security Storage Level | Hardware Location | Resistance to Root Malware | Resistance to Physical Chip Probing | Speed / Latency |
| :--- | :--- | :--- | :--- | :--- |
| **`EncryptedSharedPreferences`** | Software disk (`/data/data/`) | Low (Root can extract master key from memory) | None | Fastest (~microseconds) |
| **TEE Keymaster (TrustZone)** | Main SoC Secure World | **Complete** (Isolated from Android kernel) | Moderate (Vulnerable to laser fault injection) | Fast (~1–3 milliseconds) |
| **StrongBox Keymaster (HSM)** | Discrete Physical Microcontroller | **Complete** (Separate CPU/RAM/Bus) | **Maximum** (Tamper-resistant silicon) | Slower (~10–25 milliseconds) |

---

### G. COMMON MISTAKES & ANTI-PATTERNS

#### Anti-Pattern 1: Hardcoding or Static Reuse of AES-GCM IVs
```kotlin
// CATASTROPHIC SECURITY BUG: GCM NONCE REUSE!
val staticIv = ByteArray(12) { 0 } // HARDCODED ALL-ZEROS IV!
val spec = GCMParameterSpec(128, staticIv)
cipher.init(Cipher.ENCRYPT_MODE, key, spec) // DISASTER!
```
*Why it fails:* In AES-GCM, reusing an IV across two messages encrypted with the same key exposes the underlying XOR difference of the plaintexts and leaks the authentication subkey ($H$), completely destroying both confidentiality and tamper protection.
*Correction:* Always allow `cipher.init(Cipher.ENCRYPT_MODE, key)` to generate a randomized 12-byte IV using hardware entropy, and prepend the 12-byte IV to the stored ciphertext.

#### Anti-Pattern 2: Relying on Software Biometric Callbacks Without CryptoObject
```kotlin
// FLAWED IMPLEMENTATION: Easy to bypass with Frida / Hooking
biometricPrompt.authenticate(promptInfo) // No CryptoObject passed!

override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
    // Attackers hook this method with 3 lines of JavaScript in Frida and bypass auth!
    authorizePayment() 
}
```
*Why it fails:* Without a `CryptoObject`, the biometric prompt is pure UI. Any attacker running Frida or Xposed can hook `onAuthenticationSucceeded()` and invoke `authorizePayment()` without ever touching the fingerprint sensor.
*Correction:* Always pass a `BiometricPrompt.CryptoObject` holding an initialized Keystore `Cipher` or `Signature`. The backend transaction must require the resulting cryptographic signature.

---

### H. INTERVIEW PREPARATION (BASIC TO STAFF LEVEL)

#### Q1 (Basic): What is the Android Keystore, and how does it differ from storing an encrypted file in internal storage?
**Answer:**
Storing an encrypted file in internal storage still requires storing or deriving the decryption key in software memory (RAM). An attacker with root privileges can dump the app's heap or reverse engineer the key derivation algorithm.
The **Android Keystore** stores keys in dedicated hardware (TEE or StrongBox). Key material never enters the application's process memory. All cryptographic operations (signing, decryption) are executed inside the secure hardware chip; the app only receives the final output.

#### Q2 (Intermediate): What does `setInvalidatedByBiometricEnrollment(true)` do, and why is it mandatory for banking apps?
**Answer:**
When a key is generated with `setInvalidatedByBiometricEnrollment(true)`, Android Keystore links the key to the current biometric hardware state (the enrolled fingerprint/face templates).
If the device user (or an attacker who knows the device lockscreen PIN) goes into Android Settings and adds a new fingerprint, the Keystore permanently and irreversibly invalidates the key. Any future attempt to use that key throws `KeyPermanentlyInvalidatedException`. This prevents unauthorized users from adding their biometric profile to access an existing banking session.

#### Q3 (Advanced): How does Key Attestation prove that a key is hardware-backed to a remote banking backend?
**Answer:**
When an app generates a key pair with an attestation challenge (`setAttestationChallenge(serverNonce)`), the secure hardware generates an **X.509 Certificate Chain**.
The leaf certificate contains:
- The public key.
- An attestation extension containing the security level (`TrustedEnvironment` or `StrongBox`).
- Verified properties (package name, app signature hash, whether biometric authentication is required).
The root of this certificate chain is signed by the **Google Root CA** private key embedded in the hardware during factory manufacturing. The app sends the certificate chain to the banking backend; the backend verifies the chain against Google's public Root CA certificates, mathematically proving that the key resides in genuine hardware and has not been tampered with.

#### Q4 (Senior Scenario): Your banking security audit flags that sensitive user PINs are held in `String` variables in memory, violating PCI-DSS standards. How do you re-architect data handling to achieve zeroization?
**Answer:**
1. **Eliminate `String` for Credentials:** Strings in Java/Kotlin are immutable and stored in the heap's string pool. They cannot be overwritten and persist until garbage collection compaction (which can take hours).
2. **Migrate to `CharArray` / `ByteArray`:** Represent all PINs, passwords, and cryptographic keys as `CharArray` or `ByteArray`.
3. **Enforce Immediate Zeroization:** Wrap credential consumption in a `try/finally` block. In the `finally` block, overwrite the array with zeros using `Arrays.fill(pinArray, '0')` or `pinArray.fill(0.toByte())`.
4. **Custom Compose TextField:** Use custom Compose text input filters that store input in mutable character arrays rather than returning immutable `String` state emissions.

#### Q5 (Staff Architecture): What is StrongBox Keymaster, and what are the architectural trade-offs of enabling it in an enterprise mobile application?
**Answer:**
StrongBox is a discrete Hardware Security Module (HSM)—a physically separate chip from the main application processor (SoC), with its own CPU, memory, and physical tamper protection against hardware fault injection.
- **Architectural Benefits:** Maximum resistance against physical attacks and cold boot attacks. Complies with the highest federal and financial cryptographic standards (FIPS 140-2 Level 3).
- **Architectural Trade-offs:**
  1. **Latency:** Communicating with a physically separate chip over SPI/I2C buses is significantly slower than on-SoC TEE TrustZone. RSA key generation can take up to 2–5 seconds; signing takes 15–30ms.
  2. **Storage Constraints:** StrongBox microcontrollers have very small secure flash storage (often under 64KB), limiting the number of keys that can be stored simultaneously.
  3. **Hardware Availability:** Only available on high-end devices running Android 9+ (Pixel 3+, Samsung Galaxy S20+ with Knox Vault). Apps must implement graceful fallback to standard TEE.

---

### I. CODING PRACTICE

#### Problem: Secure PIN Zeroization Buffer
**Requirement:** Build a thread-safe `SecurePinBuffer` class in Kotlin that accepts single-digit inputs, validates PIN length, computes an HMAC-SHA256 hash using a hardware key, and guarantees complete memory zeroization of raw PIN digits immediately upon completion or cancellation.

```kotlin
package com.ibm.banking.security.keystore

import java.util.Arrays
import javax.crypto.Mac

class SecurePinBuffer(private val requiredLength: Int = 6) {

    private val pinStorage = CharArray(requiredLength)
    private var currentLength = 0

    @Synchronized
    fun appendDigit(digit: Char): Boolean {
        require(digit in '0'..'9') { "Digit must be numeric" }
        if (currentLength >= requiredLength) return false
        pinStorage[currentLength++] = digit
        return true
    }

    @Synchronized
    fun isComplete(): Boolean = currentLength == requiredLength

    /**
     * Executes cryptographic operation and immediately zeroizes internal RAM buffers.
     */
    @Synchronized
    fun <T> executeWithZeroization(action: (CharArray) -> T): T {
        return try {
            action(pinStorage)
        } finally {
            clear() // Memory zeroization guaranteed!
        }
    }

    @Synchronized
    fun clear() {
        Arrays.fill(pinStorage, '\u0000') // Overwrite physical RAM slots with null characters
        currentLength = 0
    }
}
```

*Complexity Analysis:*
- **Time Complexity:** $O(N)$ zeroization pass over character buffer.
- **Space Complexity:** $O(1)$ fixed memory footprint. Zero heap retention of plaintext credentials.

---

### J. SENIOR ENGINEER INTERVIEW SCRIPTS

#### 60-Second Explanation (Executive Summary)
> *"Enterprise mobile security relies on hardware-enforced cryptographic boundaries. The Android Keystore ensures that private key material is generated and stored inside isolated hardware—either on-SoC TEE TrustZone or dedicated StrongBox HSM chips—meaning private keys never enter application RAM, even on rooted devices. For high-value transactions like Card-Free ATM withdrawals, we bind cryptographic operations directly to user biometrics using `BiometricPrompt.CryptoObject`. This mandates that the hardware chip will only execute digital signatures when a physical biometric authentication token is validated, while `setInvalidatedByBiometricEnrollment(true)` protects against unauthorized new fingerprint enrollments."*

#### 3-Minute Deep Dive (Staff Architecture Level)
> *"In enterprise banking platforms, security must be designed under the assumption that the host operating system could be compromised by root malware, malicious instrumentation frameworks like Frida, or physical device theft.
>
> If an application stores private keys or session tokens in standard internal storage or in-memory strings, a rooted user or compromised process can easily dump the heap and exfiltrate the keys. In our architectures at IBM, we mandate that all cryptographic keys are generated within the Android Keystore with hardware backing.
>
> When configuring `KeyGenParameterSpec`, we target StrongBox Keymaster on supported hardware (like Samsung Knox Vault or Google Titan M). Unlike standard TEE TrustZone—which shares the silicon die with the Android OS—StrongBox is an independent microcontroller with isolated RAM, CPU, and physical tamper sensors.
>
> To secure transactions, we eliminate pure software biometric checks. A common vulnerability is calling `biometricPrompt.authenticate()` and authorizing payments inside `onAuthenticationSucceeded()`. An attacker can bypass this in seconds using a Frida hook. We enforce cryptographic binding: we pass a `BiometricPrompt.CryptoObject` initialized with an authenticated Keystore `Signature`.
>
> When the user touches the fingerprint sensor, the hardware biometric sensor transmits an HMAC-signed AuthToken directly to the Keymaster hardware, unlocking the private key for a single signature operation. The resulting digital signature is submitted to the bank server for verification against the device's public key.
>
> Finally, we enforce strict compliance with PCI-DSS and OWASP MASVS v2 standards:
> 1. **Zeroization:** We never use `String` for PINs or passwords. We use `CharArray` and overwrite the buffer with zeros in `finally` blocks to eliminate residual heap traces.
> 2. **Key Invalidation:** We enable `setInvalidatedByBiometricEnrollment(true)`. If a thief learns the device lockscreen PIN and enrolls their own fingerprint in Android Settings, the hardware chip instantly and irreversibly destroys our cryptographic keys, completely preventing unauthorized account draining."*

---

### K. KNOWLEDGE VALIDATION & SELF-ASSESSMENT

#### 10 Senior Interview Questions
1. Why does `KeyGenParameterSpec.Builder.setUserAuthenticationRequired(true)` prevent key usage without biometric authentication?
2. What is the difference between AES-GCM and AES-CBC in terms of integrity protection?
3. How does `setAttestationChallenge()` protect against man-in-the-middle public key replacement?
4. What happens when an app attempts to use a key that was invalidated by a new biometric enrollment? *(Answer: Throws `KeyPermanentlyInvalidatedException`)*.
5. What is the purpose of Associated Data (AAD) in `Cipher.updateAAD()` during AES-GCM encryption?
6. Why does StrongBox key generation take significantly longer than TEE key generation?
7. What are the limitations of client-side root detection libraries (like RootBeer)?
8. How does SafetyNet / Google Play Integrity API provide server-attested device integrity?
9. Why should you avoid using `SecureRandom` without proper system seeding on older Android versions?
10. How does Android 14 enforce tighter restrictions on Screenshot detection and Screen capture protection (`FLAG_SECURE`)?

#### Practical Self-Assessment Checklist
- [ ] Generated an EC key pair inside Android Keystore with `setUserAuthenticationRequired(true)`.
- [ ] Bound a `BiometricPrompt.CryptoObject` to an authenticated digital signature.
- [ ] Implemented memory zeroization on a sensitive cryptographic buffer using `fill(0)`.
- [ ] Verified that generating an AES-GCM cipher auto-generates a unique 12-byte IV.
- [ ] Verified that adding a new fingerprint triggers `KeyPermanentlyInvalidatedException`.
