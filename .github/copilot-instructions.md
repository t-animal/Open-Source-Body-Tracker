# Agent Instructions

## Planning

When planning take the [AGENTS.md](../documents/AGENTS.md) file into account

## Build Command Waiting Policy

When running any build command (for example `assembleDebug`, `build`, or `:app:compileDebugKotlin`), follow this waiting policy:

1. Wait until output contains either:
   - `BUILD SUCCESSFUL`, or
   - `BUILD FAILED`
2. Do not wait for the whole timeout in one call.
3. Poll in **2-second increments**.
4. Stop waiting after a **maximum of 60 seconds total**.

### Required behavior

- Use repeated waits of `2000` ms each.
- Re-check output after every increment.
- End early as soon as success/failure is detected.
- If neither status appears within 60 seconds, report timeout clearly.

### Pseudocode

```text
elapsed = 0
while elapsed < 60000:
  wait 2000 ms
  read latest output
  if "BUILD SUCCESSFUL" in output or "BUILD FAILED" in output:
    stop and report result
  elapsed += 2000
if elapsed >= 60000:
  report timeout (no final build status observed)
```
