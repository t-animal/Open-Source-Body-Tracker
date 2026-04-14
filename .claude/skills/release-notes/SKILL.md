---
name: release-notes
description: Generate release notes from commit messages since the last beta tag. Use when preparing notes for an upcoming beta or production release.
allowed-tools: Bash, Read
argument-hint: "[optional: base tag for diff, e.g. v2026.04-01beta]"
---

Generate release notes for the upcoming release.
Follow every step exactly — do not invent, assume, or embellish anything not present in the git output.

## Step 1: Find the baseline tag

If `$ARGUMENTS` is non-empty, use it as the baseline tag. Otherwise run:

```bash
git tag -l "*beta" --sort=-version:refname | head -1
```

Record the result as LAST_BETA_TAG. If nothing is returned, use the first commit as the baseline:

```bash
git rev-list --max-parents=0 HEAD
```

Tell the user which baseline you are using before continuing.

## Step 2: Collect all commits since that tag

Run **exactly** this command, substituting LAST_BETA_TAG with the actual value:

```bash
git log --pretty=format:"COMMIT %H%nSUBJECT: %s%nBODY:%n%b%n<<<END>>>" LAST_BETA_TAG..HEAD
```

Read every line of the output. Do not skip any commit. If the output is empty, tell the user there are no commits since the baseline and stop.

## Step 3: Filter out non-user-facing commits

Exclude any commit whose subject line matches any of these patterns:
- Starts with `Release ` — version bump commits
- Contains only tooling/CI changes with no user impact (e.g. changes only to `.github/`, `scripts/`, `gradle/`, `*.yml` files) — only exclude these if the subject line itself makes the scope clear

When in doubt, **keep the commit** and let Step 4 decide.

## Step 4: Derive user-facing change descriptions

For each remaining commit, derive one change description:
- Base it on the **subject line** and any **body text** from the commit.
- Write it in plain language an end user would understand (not developer jargon).
- If the commit is purely internal refactoring with no visible user effect and that is clear from the message, **omit it** — do not guess at impact.
- **Never add detail that is not stated in the commit message.** If something is unclear, describe it at the level of abstraction the commit message uses, no more.

## Step 5: Write the release notes

Read the template from @template.md. Fill it in:

- **Intro line**: one to three honest sentences summarizing the theme of the changes and how the user is affected or how the app was improved **for them**.
  Use the actual changes to derive the theme. Do not use marketing language.
  If there is no clear unifying theme, write: "This release includes several improvements and fixes."

- **Change list**: one entry per user-facing change derived in Step 4, in the same order as the git log (newest first).

Output the completed release notes in a code block so they can be copied directly.
