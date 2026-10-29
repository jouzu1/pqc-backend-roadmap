# Panduan Eksekusi Portofolio 7 Hari: "SNAP BI Hybrid PQC Payment Gateway"

> **Tujuan Proyek:** Membangun *proof-of-concept* (PoC) production-grade payment gateway yang mengimplementasikan standar **SNAP BI (Standar Nasional Open API Pembayaran)** dengan kapabilitas **Hybrid Post-Quantum Cryptography (FIPS 204 ML-DSA & RSA-2048)**, proteksi token **DPoP (RFC 9449)**, mitigasi buffer **32KB NGINX**, dan orkestrasi transaksi tangguh (SAGA/Outbox).  
> **Navigasi:** [← Studi Kasus: BIS Project Leap](./case-studies/bis-project-leap.md) | [🏠 Beranda](../README.md)

---

## 🗓️ Ringkasan Jadwal & Deliverable

| Hari | Fase & Fokus | Komponen & Hands-On Deliverables |
| :---: | :--- | :--- |
| **Hari 1–2** | **Cryptographic Benchmark Lab** | • Setup Java 21+ / Spring Boot dengan Bouncy Castle 1.80+ (`bcprov-jdk18on`)<br/>• Implementasikan JMH (Java Microbenchmark Harness) untuk mengukur latensi CPU & alokasi memori: `RSA-2048` vs `ML-DSA-44` vs `ML-DSA-65` vs `ML-DSA-87`<br/>• Buat grafik komparasi: throughput (ops/sec), byte signature, dan Base64 header length |
| **Hari 3–4** | **Crypto-Agile Provider Pattern** *(SNAP BI Header Integration)* | • Rancang arsitektur Strategy Pattern dengan interface `SnapSignatureEngine`:<br/>&nbsp;&nbsp;– `RsaSignatureEngine` (Legacy SHA256withRSA standar SNAP BI saat ini)<br/>&nbsp;&nbsp;– `MlDsaSignatureEngine` (PQC FIPS 204)<br/>&nbsp;&nbsp;– `HybridCompositeSignatureEngine` (Dual signing: RSA + ML-DSA)<br/>• Konfigurasikan engine via Spring `@ConditionalOnProperty` / Environment Variable<br/>• Format `stringToSign` sesuai aturan ASPI: `stringToSign = X-CLIENT-KEY + "\|" + X-TIMESTAMP` |
| **Hari 5–6** | **Gateway Buffer Tuning, DPoP, & Network Testing** | • Tempatkan NGINX reverse proxy di depan backend Spring Boot<br/>• Konfigurasikan buffer header (32KB) dan simulasi stress-test pengiriman header PQC<br/>• Implementasikan proteksi OAuth 2.0 DPoP (RFC 9449) pada endpoint `/v1.0/access-token/b2b`<br/>• Uji coba untuk memastikan tidak terjadi error HTTP 431 atau TCP timeout |
| **Hari 7** | **Integrasi Resilient Engine & Dokumentasi GitHub** | • Hubungkan modul verifikasi hybrid ini ke orchestrator pembayaran BI-FAST<br/>• Buat skenario failover: jika BI-FAST down, alihkan transaksi otomatis ke Jaringan Switching / Fast Payment Industri (bukan BI-RTGS)<br/>• Publikasikan kode ke GitHub dengan diagram arsitektur enterprise, penjelasan mitigasi MTU, serta referensi ke roadmap BSPI 2030 Bank Indonesia |

---

## 🛠️ Rincian Teknis Harian

### Hari 1–2: Cryptographic Benchmark Lab
1. Inisialisasi proyek Spring Boot 3.3+ dengan Java 21.
2. Tambahkan dependensi Bouncy Castle:
   ```xml
   <dependency>
       <groupId>org.bouncycastle</groupId>
       <artifactId>bcprov-jdk18on</artifactId>
       <version>1.80</version>
   </dependency>
   ```
3. Susun pengujian JMH (*Java Microbenchmark Harness*) untuk membandingkan performa signing dan verifikasi antara `SHA256withRSA-2048` dengan `ML-DSA-65`.

### Hari 3–4: Crypto-Agile Provider Pattern
1. Bangun abstraksi `SnapSignatureEngine` agar engine penandatanganan tidak terikat pada satu algoritma:
   ```java
   public interface SnapSignatureEngine {
       String sign(String stringToSign, PrivateKey key) throws Exception;
       boolean verify(String stringToSign, String signature, PublicKey key) throws Exception;
       String getAlgorithmName();
   }
   ```
2. Implementasikan `HybridCompositeSignatureEngine` yang menggabungkan signature RSA dan ML-DSA dalam satu format envelope komposit.

### Hari 5–6: Gateway Buffer Tuning & DPoP
1. Jalankan NGINX reverse proxy di depan backend menggunakan konfigurasi buffer 32 KB:
   ```nginx
   client_header_buffer_size 4k;
   large_client_header_buffers 4 32k;
   ```
2. Uji kirim header `X-SIGNATURE` berukuran 4,4 KB dari client curl/Postman dan pastikan request lolos tanpa error `HTTP 431`.
3. Implementasikan validasi header `DPoP` pada token otentikasi B2B untuk memastikan token terikat pada public key pengirim.

### Hari 7: Resilient Engine & Dokumentasi
1. Integrasikan engine verifikasi tanda tangan ke pipeline pembayaran.
2. Rancang skenario failover: jika rail transfer utama (BI-FAST) mengalami degradasi/maintenance, orkestrator secara otomatis mengalihkan rute ke Switching Partner domestik (Artajasa / Rintis / Alto / Jalin).
3. Dokumentasikan arsitektur dan hasil benchmark secara visual di README GitHub.

---

> [← Kembali ke Beranda](../README.md)
