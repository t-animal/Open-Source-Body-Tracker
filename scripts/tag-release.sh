#!/usr/bin/env bash
set -euo pipefail

YEAR=$(date +%Y)
MONTH=$(date +%m)
PREFIX="v-${YEAR}.${MONTH}"

# Find highest NN for this month across beta and production tags
MAX_NN=0
while IFS= read -r tag; do
    NN=$(echo "$tag" | sed "s/^${PREFIX}-0*\([0-9]*\).*/\1/")
    [ "$NN" -gt "$MAX_NN" ] && MAX_NN=$NN
done < <(git tag -l "${PREFIX}-*" | grep -E "^${PREFIX}-[0-9]+(beta)?$" || true)

NEXT_NN=$(( MAX_NN + 1 ))
NN_PADDED=$(printf '%02d' "$NEXT_NN")
VERSION_CODE=$(( YEAR * 10000 + 10#$MONTH * 100 + NEXT_NN ))

echo "Release type:"
echo "  1) beta"
echo "  2) production"
read -rp "Choice [1/2]: " CHOICE

case "$CHOICE" in
  1) TAG="${PREFIX}-${NN_PADDED}beta" ;;
  2) TAG="${PREFIX}-${NN_PADDED}"     ;;
  *) echo "Invalid choice" >&2; exit 1 ;;
esac

# versionName never includes the track suffix or the v- prefix so that a
# beta build promoted to production has an identical versionName.
VERSION_NAME="${YEAR}.${MONTH}-${NN_PADDED}"

echo ""
echo "  Tag:         $TAG"
echo "  versionCode: $VERSION_CODE"
echo "  versionName: $VERSION_NAME"
echo ""
read -rp "Confirm? [y/N]: " CONFIRM
[[ "$CONFIRM" =~ ^[yY]$ ]] || { echo "Aborted."; exit 0; }

BUILD_GRADLE="app/build.gradle.kts"
sed -i "s/versionCode = [0-9]*/versionCode = ${VERSION_CODE}/" "$BUILD_GRADLE"
sed -i "s/versionName = \"[^\"]*\"/versionName = \"${VERSION_NAME}\"/" "$BUILD_GRADLE"

git add "$BUILD_GRADLE"
git commit -m "Release ${TAG}"
git tag "$TAG"

echo ""
echo "Done. Push with:"
echo "  git push origin HEAD $TAG"
