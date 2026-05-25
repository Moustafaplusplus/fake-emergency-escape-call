#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
KEYSTORE_DIR="$ROOT/keystore"
KEYSTORE_FILE="$KEYSTORE_DIR/release.keystore"
PROPS_FILE="$KEYSTORE_DIR/keystore.properties"
ALIAS="fake-emergency-escape-call"

if [[ -f "$KEYSTORE_FILE" ]]; then
  echo "Keystore already exists: $KEYSTORE_FILE" >&2
  echo "Delete it first if you want to create a new one." >&2
  exit 1
fi

mkdir -p "$KEYSTORE_DIR"
STORE_PASS="$(openssl rand -base64 24 | tr -d '/+=' | head -c 24)"

keytool -genkeypair -v \
  -keystore "$KEYSTORE_FILE" \
  -alias "$ALIAS" \
  -keyalg RSA \
  -keysize 2048 \
  -validity 10000 \
  -storepass "$STORE_PASS" \
  -keypass "$STORE_PASS" \
  -dname "CN=Fake Emergency Escape Call, OU=Mobile, O=Fake Emergency Escape Call, L=Unknown, ST=Unknown, C=US"

cat > "$PROPS_FILE" <<EOF
storeFile=keystore/release.keystore
storePassword=$STORE_PASS
keyAlias=$ALIAS
keyPassword=$STORE_PASS
EOF

chmod 600 "$PROPS_FILE" 2>/dev/null || true

echo "Created $KEYSTORE_FILE"
echo "Credentials saved to $PROPS_FILE (gitignored)"
echo "Back up the keystore and passwords securely — you need them for all future Play Store updates."
