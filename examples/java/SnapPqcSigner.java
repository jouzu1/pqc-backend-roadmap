package com.bank.snap.crypto;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.util.Base64;

/**
 * Proof-of-Concept Standar Nasional Open API Pembayaran (SNAP) BI
 * Menggunakan Algoritma Asimetris Post-Quantum Cryptography: ML-DSA-65 (NIST FIPS 204).
 *
 * Dependensi Maven:
 *   groupId: org.bouncycastle
 *   artifactId: bcprov-jdk18on
 *   version: 1.80 atau lebih baru
 */
public class SnapPqcSigner {
    static {
        // Registrasikan Bouncy Castle Security Provider
        if (Security.getProvider("BC") == null) {
            Security.addProvider(new BouncyCastleProvider());
        }
    }

    public static void main(String[] args) throws Exception {
        System.out.println("=== SNAP BI Post-Quantum Signature Simulation (ML-DSA-65) ===");

        // 1. Generate KeyPair ML-DSA-65 (NIST Security Category 3 - setara AES-192 / RSA-3072)
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("ML-DSA-65", "BC");
        KeyPair keyPair = kpg.generateKeyPair();
        System.out.println("[1] KeyPair ML-DSA-65 berhasil dibuat.");
        System.out.println("    - Public Key Size  : " + keyPair.getPublic().getEncoded().length + " bytes");
        System.out.println("    - Private Key Size : " + keyPair.getPrivate().getEncoded().length + " bytes");

        // 2. Format stringToSign sesuai Standar Teknis SNAP BI B2B Token
        // Format resmi ASPI: stringToSign = X-CLIENT-KEY + "|" + X-TIMESTAMP
        String clientKey = "MERCHANT-CORP-ID-00129";
        String xTimestamp = "2026-10-29T14:30:00+07:00";
        String stringToSign = clientKey + "|" + xTimestamp;
        System.out.println("\n[2] stringToSign SNAP BI : " + stringToSign);

        // 3. Signing payload menggunakan ML-DSA-65 (FIPS 204)
        Signature signer = Signature.getInstance("ML-DSA-65", "BC");
        signer.initSign(keyPair.getPrivate());
        signer.update(stringToSign.getBytes(StandardCharsets.UTF_8));
        byte[] signatureBytes = signer.sign();

        String xSignature = Base64.getEncoder().encodeToString(signatureBytes);
        System.out.println("\n[3] Hasil Penandatanganan (Signing):");
        System.out.println("    - Raw Signature Size  : " + signatureBytes.length + " bytes"); // ~3.309 bytes
        System.out.println("    - X-SIGNATURE (Base64): " + xSignature.length() + " karakter");  // ~4.412 karakter

        // 4. Verifikasi Signature di sisi API Gateway Bank / Partner
        Signature verifier = Signature.getInstance("ML-DSA-65", "BC");
        verifier.initVerify(keyPair.getPublic());
        verifier.update(stringToSign.getBytes(StandardCharsets.UTF_8));
        boolean isValid = verifier.verify(signatureBytes);

        System.out.println("\n[4] Hasil Verifikasi API Gateway Bank: " + (isValid ? "VALID (SUCCESS)" : "INVALID (FAILED)"));
    }
}
