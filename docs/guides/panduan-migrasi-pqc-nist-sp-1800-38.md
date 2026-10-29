# Panduan teknis migrasi kriptografi pasca-kuantum: NIST SP 1800-38 (Volume A, B, dan C)

Dokumen ini merangkum dan menyusun ulang materi dari tiga publikasi National Institute of Standards and Technology (NIST) Special Publication 1800-38:
* **NIST SP 1800-38A:** Executive Summary
* **NIST SP 1800-38B:** Approach, Architecture, and Security Characteristics of Public Key Application Discovery Tools
* **NIST SP 1800-38C:** Quantum-Resistant Cryptography Technology Interoperability and Performance Report

Berkas PDF asli untuk ketiga volume di atas dapat diakses melalui folder repositori referensi:
* Sumber PDF: [Post Quantum Cryptography - Google Drive](https://drive.google.com/drive/u/0/folders/1cYg1miKm8vfst9E355slB-pz1Hgd1mEs)

Fokus utama dokumen ini adalah memberikan panduan teknis yang lugas bagi arsitek sistem, manajer keamanan, dan pengembang backend mengenai cara mengidentifikasi aset kriptografi yang rentan, mengukur risiko migrasi, serta menerapkan algoritma pasca-kuantum pada protokol jaringan dan modul perangkat keras.

---

## 1. Ringkasan eksekutif dan konteks ancaman (NIST SP 1800-38A)

Perkembangan komputer kuantum skala besar (Cryptographically Relevant Quantum Computer atau CRQC) akan mematahkan sistem kriptografi asimetris yang dipakai saat ini. Penggantian algoritma pada infrastruktur teknologi membutuhkan waktu bertahun-tahun. Organisasi yang menyimpan data berharga jangka panjang harus mulai memetakan sistem dan menyiapkan transisi sekarang agar data terlindung dari ancaman masa depan.

National Cybersecurity Center of Excellence (NCCoE) di NIST membentuk konsorsium bersama industri untuk mempercepat kesiapan migrasi. Konsorsium ini mencakup penyedia teknologi seperti AWS, Cisco, IBM, Microsoft, SandboxAQ, Thales, wolfSSL, DigiCert, Keyfactor, Samsung SDS, dan institusi finansial seperti JPMorgan Chase.

### Dua ancaman kuantum utama terhadap kriptografi klasik

Kriptografi modern terbagi menjadi dua ranah utama, dan keduanya menghadapi dampak yang berbeda dari algoritma kuantum:

1. **Algoritma Shor:** Algoritma kuantum yang dapat menyelesaikan masalah faktorisasi bilangan bulat dan logaritma diskrit pada grup bilangan maupun kurva eliptik dalam waktu polinomial. Algoritma ini mematahkan seluruh kriptografi kunci publik standar yang digunakan saat ini:
   * RSA (faktorisasi)
   * Diffie-Hellman dan DSA (logaritma diskrit)
   * ECDH dan ECDSA (logaritma diskrit kurva eliptik)
   Dampaknya mencakup kegagalan pertukaran kunci rahasia, pemalsuan tanda tangan digital, dan hilangnya kemampuan verifikasi identitas pada protokol komunikasi.

2. **Algoritma Grover:** Algoritma kuantum yang memberikan percepatan kuadratik untuk pencarian pada ruang kunci yang tidak terstruktur (*brute force*). 
   * Efeknya setara dengan membagi dua panjang kunci efektif.
   * Kunci simetris AES-128 turun tingkat keamanannya menjadi setara 64-bit, sehingga tidak lagi aman.
   * Kunci simetris AES-256 turun tingkat keamanannya menjadi setara 128-bit, yang secara teknis masih sangat aman dari komputasi kuantum.
   * Fungsi hash seperti SHA-256 dan SHA-3 memerlukan ukuran digest minimal 256 atau 384 bit agar tahan terhadap serangan tabrakan (*collision*) dan *preimage*.

### Ruang lingkup tantangan organisasi

Organisasi menghadapi beberapa kendala praktis selama fase transisi:
* Sebagian besar aplikasi tidak memiliki visibilitas lengkap terhadap pustaka, sertifikat, dan protokol yang mereka gunakan.
* Kriptografi sering kali terpasang secara kaku di dalam kode program (*hardcoded*) atau tersembunyi di dalam dependensi pihak ketiga.
* Algoritma pasca-kuantum baru bukan merupakan pengganti langsung (*drop-in replacement*). Algoritma ini memiliki ukuran kunci dan tanda tangan yang jauh lebih besar, membutuhkan alokasi memori yang berbeda, dan mempengaruhi latensi jaringan.

---

## 2. Kesiapan kuantum dan penemuan kriptografi (NIST SP 1800-38B)

Volume B menitikberatkan pada tahap awal migrasi: cara menemukan, menginventarisasi, dan menilai risiko dari seluruh algoritma kriptografi yang berjalan di lingkungan perusahaan (*cryptographic discovery*).

### 2.1 Metodologi penilaian risiko migrasi

NIST SP 1800-38B membandingkan beberapa metodologi penilaian risiko untuk menentukan prioritas migrasi:

#### A. Teorema Mosca
Teorema ini diperkenalkan oleh Michele Mosca dan menjadi rumus dasar perhitungan urgensi migrasi kuantum:

$$\text{Jika } (X + Y) > Z \text{, maka organisasi berada dalam bahaya.}$$

Definisi parameter:
* **$X$ (Shelf Life):** Berapa lama data harus tetap terjaga kerahasiaannya. Contohnya, data rekam medis, rahasia dagang, atau data transaksi perbankan sering kali memiliki kewajiban retensi kerahasiaan selama 10 hingga 30 tahun.
* **$Y$ (Migration Time):** Waktu yang dibutuhkan organisasi untuk memperbarui seluruh sistem, aplikasi, dan perangkat keras agar siap pasca-kuantum.
* **$Z$ (Collapse Time):** Estimasi waktu sampai aktor ancaman memiliki akses ke komputer kuantum yang mampu memecahkan kriptografi klasik.

Jika jumlah masa simpan data ($X$) dan durasi migrasi ($Y$) melebihi waktu kedatangan komputer kuantum ($Z$), musuh dapat merekam lalu lintas data yang terenkripsi saat ini dan mendekripsinya nanti saat komputer kuantum tersedia. Pola serangan ini dikenal sebagai *Store Now, Decrypt Later* (SNDL) atau *Harvest Now, Decrypt Later* (HNDL).

Berdasarkan studi RAND Corporation yang dikutip dalam dokumen ini, dari 55 fungsi infrastruktur kritis nasional (National Critical Functions), 6 fungsi berstatus prioritas tinggi untuk segera dibantu migrasinya, 15 fungsi berstatus prioritas menengah, dan 34 fungsi berstatus prioritas rendah.

#### B. Crypto Agility Risk Assessment Framework (CARAF)
CARAF memperluas Teorema Mosca dengan fokus pada *cryptographic agility* (kemampuan sistem untuk berganti algoritma tanpa perombakan besar pada arsitektur perangkat lunak). Framework ini terdiri dari lima tahap:
1. Identifikasi ancaman.
2. Inventarisasi aset kriptografi.
3. Estimasi risiko berbasis probabilitas waktu.
4. Pengamanan aset melalui mitigasi risiko teknis.
5. Penyusunan peta jalan organisasi.

#### C. Model Risiko FS-ISAC (Sektor Finansial)
Financial Services Information Sharing and Analysis Center (FS-ISAC) menyusun konsep Cryptographic Agility Index (CAI). Pendekatan ini memberi bobot khusus pada:
* Apakah pustaka kriptografi dibangun sendiri di internal atau dibeli dari vendor pihak ketiga. Ketergantungan pada vendor pihak ketiga meningkatkan risiko keterlambatan pembaruan.
* Jaringan pembayaran dan kliring antar-lembaga finansial (seperti jaringan SWIFT, BI-FAST, atau switching lokal) yang membutuhkan konsensus standar industri sebelum salah satu pihak dapat memperbarui algoritmanya.

#### D. Faktor Evaluasi CISA
Cybersecurity and Infrastructure Security Agency (CISA) menyarankan tujuh pertanyaan penilai risiko bagi pemilik sistem:
1. Apakah sistem tergolong aset bernilai tinggi bagi organisasi?
2. Aset apa yang dilindungi (kunci penandatanganan, master key, token sesi, data pribadi)?
3. Sistem mana saja yang terhubung secara langsung?
4. Sejauh mana sistem bertukar data dengan regulator atau entitas pemerintah?
5. Sejauh mana sistem bertukar informasi dengan pihak luar organisasi?
6. Apakah sistem mendukung layanan infrastruktur kritis?
7. Berapa lama data yang lewat harus dilindungi kerahasiaannya?

---

### 2.2 Arsitektur penemuan kriptografi di lingkungan enterprise

Untuk menyusun Cryptographic Bill of Materials (CBOM), dokumen ini mendemonstrasikan tiga metode penemuan yang saling melengkapi:

```
+---------------------------------------------------------------------------------------+
|                   ARSITEKTUR PENEMUAN KRIPTOGRAFI ENTERPRISE (SP 1800-38B)            |
+---------------------------------------------------------------------------------------+

  1. Pipeline CI/CD (Kode Sumber & Build)
     [Kode Sumber] ---> [SAST / Semgrep / cdxgen] ---> Format SARIF / CBOM JSON
                                                               |
  2. Sistem Operasional & Host (Server / VM / EDR)             v
     [File Sistem & Memori] ---> [Agen Endpoint] ------> [Normalisasi Data]
                                                               |
  3. Lalu Lintas Jaringan (Network Traffic)                    v
     [Passive Sniffing / Active TLS Scanner] ---------> [Dashboard Risiko Enterprise]
```

1. **Pemindaian Kode Sumber pada Pipeline CI/CD:**
   * Pemeriksaan statis (SAST) dijalankan pada setiap *pull request* atau tahapan *build*.
   * Alat memindai pemanggilan API kriptografi (misalnya `Cipher.getInstance("RSA")` atau `MessageDigest.getInstance("SHA1")`), impor dependensi eksternal, dan konfigurasi skrip.
   * Hasil temuan dikeluarkan dalam format standar SARIF (Static Analysis Results Interchange Format) agar dapat diagregasi secara otomatis.

2. **Pemindaian Host dan Sistem Operasi:**
   * Agen yang terpasang pada peladen memindai sertifikat digital di penyimpanan sistem (misalnya Java KeyStore, Windows Certificate Store, direktori `/etc/ssl/certs`).
   * Pemeriksaan pustaka bersama (*shared libraries* seperti `libssl.so`, `crypto.dll`) dan modul kernel yang dimuat ke dalam memori.

3. **Pemindaian dan Pemantauan Jaringan:**
   * **Pemindaian aktif:** Mengirimkan *handshake probe* ke setiap port terbuka untuk mencatat versi TLS, cipher suite yang didukung, dan rantai sertifikat X.509 yang disajikan peladen.
   * **Pencatatan pasif:** Merekam salinan lalu lintas jaringan (*packet mirror*) untuk menemukan klien lama yang masih menegosiasikan algoritma rentan tanpa mengganggu ketersediaan layanan.

### 2.3 Standar data output dan normalisasi CBOM

NIST SP 1800-38B menetapkan normalisasi data hasil penemuan ke format terstruktur machine-readable. Format yang digunakan adalah **CycloneDX v1.6** dengan ekstensi properti kriptografi (`cryptoProperties`) dan **SARIF v2.1.0**.

Contoh struktur data JSON yang dinormalisasi untuk aset jaringan:
```json
{
  "bomFormat": "CycloneDX",
  "specVersion": "1.6",
  "components": [
    {
      "type": "cryptographic-asset",
      "name": "TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256",
      "properties": [
        { "name": "endpoint:ip", "value": "192.168.10.50" },
        { "name": "endpoint:port", "value": "443" },
        { "name": "protocol:version", "value": "TLSv1.2" },
        { "name": "crypto:key_exchange", "value": "ECDHE-P256" },
        { "name": "crypto:signature", "value": "RSA-2048" },
        { "name": "crypto:quantum_vulnerable", "value": "true" }
      ]
    }
  ]
}
```

---

## 3. Laporan interoperabilitas dan performa (NIST SP 1800-38C)

Volume C menyajikan hasil pengujian laboratorium terhadap algoritma pasca-kuantum yang diterapkan pada protokol komunikasi, format sertifikat digital, dan modul perangkat keras (HSM).

### 3.1 Algoritma yang diuji

Pengujian menggunakan kandidat standar NIST yang kini telah dibakukan:
* **ML-KEM (FIPS 203 / CRYSTALS-Kyber):** Digunakan untuk pertukaran kunci rahasia (*Key Encapsulation Mechanism*). Varian: Kyber-512, Kyber-768, Kyber-1024.
* **ML-DSA (FIPS 204 / CRYSTALS-Dilithium):** Digunakan untuk tanda tangan digital berbasis kisi (*lattice-based signature*). Varian: Dilithium2 (ML-DSA-44), Dilithium3 (ML-DSA-65), Dilithium5 (ML-DSA-87).
* **Falcon:** Tanda tangan digital berbasis kisi dengan ukuran signature lebih ringkas tetapi implementasi floating-point lebih kompleks.
* **SLH-DSA (FIPS 205 / SPHINCS+):** Tanda tangan digital berbasis hash *stateless*.

---

### 3.2 Pengujian protokol: SSH, TLS 1.3, dan QUIC

Pengujian dilakukan menggunakan OpenSSL dengan OQS Provider, Samsung SDS PQC-TLS (`s-pqc-tls`), dan AWS `s2n-tls`.

#### A. Transport Layer Security (TLS 1.3)
Pengujian berfokus pada mode hibrida: menggabungkan algoritma pertukaran kunci klasik dengan algoritma pasca-kuantum dalam satu jabat tangan (*handshake*).
* **Kombinasi standar:** `X25519 + Kyber-768` (sekarang dibakukan dalam RFC 10024 dengan codepoint `0x11EC`).
* **Temuan performa Samsung SDS (s-pqc-tls):**
  * Handshake per detik untuk kurva klasik `ECDHE P-256 + ECDSA P-256`: 333,62 handshake/detik.
  * Handshake per detik untuk hibrida `P256-Kyber-512 + ECDSA P-256`: 301,70 handshake/detik.
  * Handshake per detik untuk hibrida `X25519-Kyber-768 + ECDSA P-384`: 242,59 handshake/detik.
* **Kesimpulan:** Waktu komputasi ML-KEM sangat efisien. Penurunan *throughput* pada mode hibrida relatif kecil dan tidak menjadi kendala berarti untuk beban kerja peladen web modern.

#### B. QUIC dan UDP
Pengujian QUIC menghadapi batasan ukuran paket UDP awal (*Initial Packet*). Spesifikasi QUIC (RFC 9000) membatasi ukuran paket UDP pertama maksimal 1200 byte untuk mencegah serangan amplifikasi DDoS.
* Ketika kunci publik dan sertifikat pasca-kuantum melebihi batas 1200 byte, paket ClientHello terpaksa dipecah menjadi beberapa datagram UDP.
* Pengujian membuktikan bahwa tumpukan jaringan yang belum diperbarui sering kali mengalami kegagalan jabat tangan atau membuang paket yang terfragmentasi.

---

### 3.3 Enam format sertifikat digital X.509 hibrida

Salah satu kontribusi teknis paling penting dalam SP 1800-38C Bagian 8 adalah dokumentasi enam format sertifikat X.509 untuk menjembatani sistem lama dengan sistem baru:

```
+--------------------------------------------------------------------------------------------------+
| ENAM FORMAT SERTIFIKAT X.509 UNTUK TRANSISI PASCA-KUANTUM (SP 1800-38C BAGIAN 8.1.2)             |
+--------------------------------------------------------------------------------------------------+

1. PURE PQC:
   [X.509 Standard ASN.1] ---> Berisi HANYA Public Key ML-DSA & Signature ML-DSA.
   (Tidak kompatibel dengan parser klasik lama).

2. HYBRID CONCATENATED:
   [Single Cert] ---> Public Key = concat(RSA_Key, PQC_Key) | Signature = concat(RSA_Sig, PQC_Sig).
   (Menggunakan OID gabungan kustom).

3. HYBRID BOUND:
   [Cert 1: RSA Murni] <======== Ekstensi Tautan Timbal-Balik ========> [Cert 2: PQC Murni]
   (Dua sertifikat terpisah yang saling menunjuk melalui ekstensi multi-auth).

4. HYBRID COMPOSITE (Draft IETF LAMPS):
   [Single Cert] ---> SubjectPublicKeyInfo berisi Sequence ASN.1 dari beberapa kunci dan algoritma.
   Verifikasi mewajibkan kedua tanda tangan valid secara bersamaan.

5. HYBRID USING EXTENSIONS (Catalyst):
   [Sertifikat Utama: RSA Standar] + [Ekstensi Non-Kritis: Public Key PQC & Signature PQC]
   Parser lama memverifikasi RSA dan mengabaikan ekstensi; parser baru memverifikasi keduanya.

6. HYBRID DELTA EXTENSIONS (Chameleon):
   [Base Certificate: RSA] + [Delta Extension yang memuat perbedaan untuk membangun Cert PQC]
   Menghemat ruang transmisi dengan hanya menyertakan perbedaan (*delta*) di dalam ekstensi.
```

1. **Pure PQC:**
   Menggunakan struktur X.509 standar (RFC 5280) tetapi mengganti materi kunci dan signature secara penuh dengan algoritma pasca-kuantum. Format ini hanya bisa dibaca oleh aplikasi yang sudah diperbarui.

2. **Hybrid Concatenated:**
   Menggabungkan byte kunci klasik dan kunci kuantum secara langsung menjadi satu string byte tanpa mengubah pohon sintaks ASN.1. Menggunakan OID khusus untuk kombinasi pasangan algoritma.

3. **Hybrid Bound:**
   Menggunakan dua sertifikat terpisah: satu sertifikat klasik dan satu sertifikat pasca-kuantum murni. Masing-masing sertifikat menyertakan ekstensi tautan yang memuat digest dari sertifikat pasangannya (berdasarkan `draft-becker-guthrie-cert-binding-for-multi-auth`).

4. **Hybrid Composite:**
   Sertifikat tunggal yang membungkus beberapa kunci publik dan beberapa tanda tangan di dalam elemen sequence ASN.1 terstruktur. Ini adalah format yang didorong oleh kelompok kerja IETF LAMPS (`draft-ounsworth-pq-composite-sigs`). Transaksi dianggap sah hanya jika verifikasi klasik dan verifikasi pasca-kuantum keduanya sukses.

5. **Hybrid Using Extensions (Pendekatan Catalyst):**
   Sertifikat tampak seperti sertifikat klasik biasa bagi parser lama. Kunci publik dan tanda tangan pasca-kuantum disimpan di dalam X.509 V3 Extension non-kritis. Sistem lama membaca tanda tangan RSA/ECDSA tanpa terganggu, sementara sistem baru membaca ekstensi untuk memvalidasi tanda tangan pasca-kuantum.

6. **Hybrid Delta Extensions (Pendekatan Chameleon):**
   Sertifikat dasar memuat data klasik, sementara ekstensi khusus memuat perbedaan (*delta*) yang dibutuhkan untuk menyusun ulang sertifikat sekunder pasca-kuantum. Pola ini menghemat ruang sertifikat jika dibandingkan dengan membawa dua sertifikat utuh.

---

### 3.4 Hardware Security Modules (HSM) dan dilema *Digest-then-Sign*

Pengujian pada HSM fisik (termasuk modul pada peladen IBM z16 enterprise) mengungkap tantangan mendasar pada arsitektur antarmuka PKCS#11 saat mengadopsi tanda tangan pasca-kuantum.

#### Masalah teknis pada PKCS#11 v2.40
Antarmuka HSM standar saat ini (PKCS#11 v2.40) dirancang untuk algoritma klasik:
* Pada RSA atau ECDSA, aplikasi klien menghitung hash dari pesan terlebih dahulu di luar HSM, kemudian hanya mengirimkan digest berukuran kecil (32 atau 64 byte) ke dalam HSM untuk ditandatangani (`CKM_ECDSA` atau `CKM_RSA_PKCS`).
* Pola ini disebut **Digest-then-Sign** atau **Hash-then-Sign**.

#### Karakteristik intrinsik algoritma tanda tangan pasca-kuantum
Algoritma seperti ML-DSA (Dilithium) dan Falcon tidak memisahkan proses hashing dari proses komputasi kisi secara bebas:
* ML-DSA membutuhkan seluruh isi pesan $M$ untuk digabungkan dengan representasi kunci publik $tr$ melalui fungsi `SHAKE256(tr || M)` di dalam putaran penandatanganan.
* Falcon memetakan pesan langsung ke titik kisi (*HashToPoint*).
* Sifat ini memberikan perlindungan matematika yang lebih kuat terhadap tabrakan hash (*collision resistance*), tetapi menuntut modul penandatangan menerima seluruh pesan utuh (*Pure Mode*).

#### Dampak performa pada modul penandatangan perangkat keras
Jika sebuah aplikasi menandatangani dokumen atau payload berukuran besar (misalnya file beberapa puluh megabyte):
1. Mengirimkan seluruh payload pesan melalui bus jaringan ke HSM via fungsi `C_SignUpdate` dan `C_SignFinal` menciptakan *bottleneck* transmisi yang parah.
2. Memori internal HSM terbatas dan tidak dirancang untuk menyimpan buffer pesan biner berukuran besar.

#### Solusi standarisasi: Mode Prehash vs Pure Signature
Untuk mengatasi tantangan ini, dokumen merekomendasikan dua jalur implementasi:
* **Pure Mode (Tanpa Prehash):** Digunakan untuk data berukuran kecil hingga menengah (seperti token akses, header otentikasi API, dan pesan transaksi pembayaran). Seluruh payload dikirim langsung ke mesin penandatangan.
* **Prehash Mode (HashML-DSA / Hash-then-Sign):** Digunakan untuk payload besar atau dokumen arsip. Klien menghitung hash awal pesan dengan fungsi SHAKE-256, lalu memasukkannya ke dalam struktur pembungkus (*wrapper context*) yang ditandatangani oleh algoritma pasca-kuantum. Pola ini mempertahankan kompatibilitas dengan batasan memori perangkat keras HSM.

---

## 4. Relevansi praktis untuk arsitektur perbankan dan API SNAP BI

Temuan dalam NIST SP 1800-38 berkaitan langsung dengan modernisasi sistem perbankan nasional, termasuk implementasi Standar Nasional Open API Pembayaran (SNAP BI) dan visi Bank Indonesia BSPI 2030:

```
+---------------------------------------------------------------------------------------------------+
|               PEMETAAN TEMUAN NIST KE SISTEM PEMBAYARAN & SNAP BI                                 |
+---------------------------------------------------------------------------------------------------+
| Temuan NIST SP 1800-38          | Dampak Arsitektur Sistem Finansial                              |
+---------------------------------+-----------------------------------------------------------------+
| Ukuran signature ML-DSA-65      | Header HTTP `X-SIGNATURE` membengkak dari 344 karakter          |
| membengkak menjadi 3.309 byte   | (RSA-2048) menjadi sekitar 4.412 karakter Base64.              |
| (12x lipat RSA-2048).           | Memerlukan penyesuaian buffer web server (minimal 32 KB).       |
+---------------------------------+-----------------------------------------------------------------+
| Fragmentasi MTU paket jaringan  | Header HTTP PQC melebihi batas 1.500 byte Ethernet, memecah     |
| pada antarmuka TCP.             | paket IP menjadi beberapa fragmen. Solusi: kompresi header      |
|                                 | HTTP/2 HPACK atau migrasi ke gRPC binary framing.               |
+---------------------------------+-----------------------------------------------------------------+
| Rekomendasi sertifikat hibrida  | Memungkinkan API Gateway perbankan melakukan validasi ganda     |
| Composite (IETF LAMPS).         | (RSA + ML-DSA) selama masa transisi sebelum RSA dihentikan.     |
+---------------------------------+-----------------------------------------------------------------+
| Format penemuan aset CBOM       | Bank dapat memetakan cipher suite di seluruh microservice       |
| CycloneDX v1.6 & SARIF.         | secara otomatis untuk memenuhi audit kepatuhan regulasi.        |
+---------------------------------+-----------------------------------------------------------------+
```

### Langkah adaptasi teknis yang perlu diambil tim pengembang

1. **Konfigurasi Buffer Peladen Web:**
   Konfigurasi default web server seperti Tomcat atau NGINX (biasanya 8 KB) rawan memicu galat `HTTP 431 Request Header Fields Too Large` saat menerima request SNAP BI dengan header tanda tangan ML-DSA dan rantai sertifikat hibrida. Nilai `max-http-request-header-size` harus dinaikkan ke minimal 32 KB.

2. **Desain Kode Kripto-Agil (Strategy Pattern):**
   Aplikasi backend tidak boleh mengikat logika verifikasi ke algoritma tunggal. Antarmuka penandatangan harus diabstraksikan sehingga sistem dapat berpindah mode antara `LEGACY_RSA`, `PQC_ML_DSA`, atau `HYBRID_COMPOSITE` cukup melalui konfigurasi variabel lingkungan.

3. **Penyusunan CBOM di Pipeline CI/CD:**
   Mengintegrasikan alat pemindai statis pada proses *build* aplikasi pembayaran untuk mencatat seluruh pemanggilan pustaka kriptografi ke dalam format CycloneDX v1.6. Langkah ini memberikan bukti kesiapan audit saat regulasi mewajibkan pelaporan kesiapan kuantum.

---
*Dokumen ini disusun sebagai panduan referensi arsitektur teknis dengan merujuk langsung pada publikasi resmi NIST Special Publication 1800-38A, 1800-38B, dan 1800-38C. Berkas PDF asli dapat diakses di [Post Quantum Cryptography - Google Drive](https://drive.google.com/drive/u/0/folders/1cYg1miKm8vfst9E355slB-pz1Hgd1mEs).*