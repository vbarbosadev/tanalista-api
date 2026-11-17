#!/usr/bin/env bash
set -euo pipefail

cd "$(dirname "$0")/.."
version="2026.9.5"
case "$(uname -m)" in
  x86_64) arch="x64" ;;
  aarch64|arm64) arch="arm64" ;;
  *) echo "Arquitetura não suportada pelo setup" >&2; exit 1 ;;
esac
mkdir -p "$HOME/.local/bin"
tmp="$(mktemp -d)"
trap 'rm -rf "$tmp"' EXIT
asset="mise-v${version}-linux-${arch}.tar.gz"
release="https://github.com/jdx/mise/releases/download/v${version}"
curl --fail --location --retry 3 "$release/$asset" -o "$tmp/$asset"
curl --fail --location --retry 3 "$release/SHASUMS256.txt" -o "$tmp/SHASUMS256.txt"
(
  cd "$tmp"
  awk -v asset="$asset" '$2 == asset || $2 == "./" asset { print }' SHASUMS256.txt > selected.sha256
  if [ "$(wc -l < selected.sha256)" -ne 1 ]; then
    echo "Esperado exatamente um checksum SHA256 para $asset" >&2
    exit 1
  fi
  sha256sum --check selected.sha256
)
tar -xzf "$tmp/$asset" -C "$tmp"
install -m 0755 "$tmp/mise/bin/mise" "$HOME/.local/bin/mise"
export PATH="$HOME/.local/bin:$PATH"
mise trust mise.toml
mise install --yes
mise reshim
activation='eval "$(~/.local/bin/mise activate bash)"'
grep -Fqx "$activation" "$HOME/.bashrc" || printf '\n%s\n' "$activation" >> "$HOME/.bashrc"
echo "Toolchain pronta. Use: mise run ci ou docker compose up --build"
echo "A API usa identidade local: mantenha a porta 8081 privada no Codespaces."
