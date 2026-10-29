#!/usr/bin/env bash
set -euo pipefail

# =============================================================================
# Open Quantum Safe (OQS) Docker Lab
# Menguji coba TLS 1.3 Hybrid Key Exchange (X25519 + ML-KEM-768)
# =============================================================================

echo "[1/3] Menjalankan NGINX Reverse Proxy berkemampuan PQC (Open Quantum Safe)..."
docker run --name oqs-nginx -p 4433:4433 -d openquantumsafe/nginx || {
    echo "Container oqs-nginx sudah ada atau sedang berjalan. Menggunakan container yang ada..."
}

echo ""
echo "[2/3] Menunggu NGINX PQC siap melayani koneksi..."
sleep 3

echo ""
echo "[3/3] Melakukan handshake TLS 1.3 PQC menggunakan client curl OQS..."
echo "Negosiasi kurva hybrid: x25519_mlkem768 (Codepoint IANA 0x11EC)"
echo "-------------------------------------------------------------------------"

docker run --rm -it openquantumsafe/curl curl -k -v --tlsv1.3 \
  --curves x25519_mlkem768 https://host.docker.internal:4433

echo "-------------------------------------------------------------------------"
echo "Pengujian selesai! Cek baris 'SSL connection using TLSv1.3 / x25519_mlkem768'."
