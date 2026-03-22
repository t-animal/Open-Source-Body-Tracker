# Translations & String Resources

## Overview

All user-visible text lives in Android string resources under `app/src/main/res/values/` (default/English), split by feature:

| File | Contents |
|------|----------|
| `strings.xml` | `app_name` only |
| `strings.common.xml` | Shared strings, navigation, menu, metric labels, analysis methods |
| `strings.measurements.xml` | Edit, list, delete, discard, demo, table, validation |
| `strings.analysis.xml` | Chart hints, reorder/expand content descriptions |
| `strings.photos.xml` | Selection, animation, compare, content descriptions |
| `strings.importbackup.xml` | Import flow, progress, errors |
| `strings.export.xml` | Export settings, progress, auto-export errors, notifications |
| `strings.settings.xml` | Settings navigation, visibility |
| `strings.profile.xml` | Profile form, validation |
| `strings.onboarding.xml` | Onboarding flow |
| `strings.reminders.xml` | Reminder settings, notifications |
| `strings.about.xml` | About screen |

A string belongs in the file matching its feature folder. If it is used in multiple features, it goes in `strings.common.xml`.

Translation files follow the standard Android convention: `app/src/main/res/values-<locale>/strings.xml` (e.g. `values-de/strings.xml`). Locale files do **not** need to mirror the split — Android merges all `values/strings.*.xml` into one namespace automatically.

## Rules for Agents

### When adding or changing UI text

1. **Never hardcode strings in Compose or Kotlin files.** Always add an entry to the appropriate `values/strings.*.xml` file and reference it via `stringResource(R.string.…)`.
2. **Use the existing naming convention.** Keys follow the pattern `<feature>_<context>_<purpose>`, e.g. `export_label_password`, `photos_button_compare`, `cd_animation_play`. Browse the target file to match the style before inventing a new key.
3. **Place new entries in the correct file.** Pick the `strings.<feature>.xml` file that matches the feature folder where the string is used. If the string is used in multiple features, add it to `strings.common.xml`.
4. **Add the new key to every existing translation file.** After adding a string, add the same key (with a translated value or, if unsure, the English fallback wrapped in a `TODO` comment) to every `values-<locale>/strings.xml` that exists. To find all translation files, glob for `app/src/main/res/values-*/strings*.xml`.

### When removing UI text

1. **Remove the key from its `values/strings.*.xml` file AND every translation file.** Stale keys in translation files cause unnecessary maintenance burden and confuse future translators.
2. **Grep the codebase for the key name** (`R.string.<key>`) before deleting to confirm it is truly unused.

### When renaming a key

1. Rename in the appropriate `values/strings.*.xml` file and all `values-<locale>/strings*.xml` files simultaneously.
2. Update all Kotlin/Compose references (`R.string.<old>` → `R.string.<new>`).

### Keeping translations in sync

After any string resource change, run this checklist:

1. **Glob** `app/src/main/res/values-*/strings*.xml` to discover all locale files.
2. **Compare key sets.** Every key across the `values/strings.*.xml` files must exist in each locale file and vice versa. Keys present in a locale file but missing from the default files are stale and must be removed.
3. **Flag untranslated strings.** If you add a key but cannot provide a proper translation, use the English text and add an XML comment `<!-- TODO: translate -->` so translators can find it.

### Parameterized strings

- Use positional format specifiers (`%1$s`, `%2$d`) not bare `%s/%d` when there are multiple parameters — translators may need to reorder them.
- Document parameter meaning in an XML comment above the string if not obvious from context.

### Plurals

- Use Android `<plurals>` resources when a string depends on a count. Do not construct plural forms with string concatenation or conditional logic in Kotlin.

### Content descriptions (accessibility)

- Content descriptions (`cd_*` keys) must also be translated. They are read aloud by TalkBack and are just as important as visible text.
