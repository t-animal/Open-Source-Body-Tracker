#!/usr/bin/env bash

set -euo pipefail

usage() {
        cat <<'EOF'
Generate measurement guidance drawables from assets/body-drawing.svg.

Usage:
    bash scripts/generate-measurement-guidance.sh
    bash scripts/generate-measurement-guidance.sh <svg-path> <light-dir> <night-dir>

Defaults:
    svg-path  = assets/body-drawing.svg
    light-dir = app/src/main/res/drawable
    night-dir = app/src/main/res/drawable-night

Optional environment variables:
    MEASUREMENT_GUIDANCE_NIGHT_FILL
        Hex fill color used for pure-black filled shapes in night mode.
        Default: #F2F2F2

Examples:
    bash scripts/generate-measurement-guidance.sh
    MEASUREMENT_GUIDANCE_NIGHT_FILL=#EDEDED bash scripts/generate-measurement-guidance.sh
    bash scripts/generate-measurement-guidance.sh \
        assets/body-drawing.svg \
        app/src/main/res/drawable \
        app/src/main/res/drawable-night
EOF
}

if [[ "${1:-}" == "-h" || "${1:-}" == "--help" ]]; then
        usage
        exit 0
fi

SCRIPT_DIR="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd -- "${SCRIPT_DIR}/.." && pwd)"

SVG_PATH="${1:-${REPO_ROOT}/assets/body-drawing.svg}"
LIGHT_DIR="${2:-${REPO_ROOT}/app/src/main/res/drawable}"
NIGHT_DIR="${3:-${REPO_ROOT}/app/src/main/res/drawable-night}"
NIGHT_FILL="${MEASUREMENT_GUIDANCE_NIGHT_FILL:-#F2F2F2}"

python3 "${SCRIPT_DIR}/generate_measurement_guidance.py" \
    --svg "${SVG_PATH}" \
    --light-dir "${LIGHT_DIR}" \
    --night-dir "${NIGHT_DIR}" \
    --night-fill "${NIGHT_FILL}"
