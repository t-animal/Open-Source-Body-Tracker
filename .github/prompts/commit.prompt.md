---
agent: 'agent'
description: 'Commit changes with a concise and meaningful message'
---

Commit all changes that you have made to git.

Create a meaningful but concise commit message with:
- a subject line (max 50 chars)
- a blank line
- a body with wrapped lines (max 72 chars per line)

IMPORTANT:
- Insert real newline characters in the commit message.
- Do NOT use escaped sequences like \n in the message text.
- Prefer `git commit -F <message-file>` (or equivalent) to guarantee real
  line breaks.

After committing, verify formatting with:
`git log -1 --pretty=%B`

Return:
- commit hash
- subject
- full body as created