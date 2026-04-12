---
agent: 'agent'
model: 'GPT-5 mini'
description: 'Commit changes with a concise and meaningful message'
---

Commit all changes that you have made to git. Identify which
files belong together semantically and add them to the commit.
If the use asks you to commit the staged changes, then do not 
stage any new files.

Create a meaningful but concise commit message with:
- a subject line (max 50 chars)
- a blank line
- a body with wrapped lines (max 72 chars per line)

Include all relevant information about the changes. Especially:
- what was changed (briefly - but don't just list the files)
- the reason for the change
- any relevant details that would help others understand the change

IMPORTANT:
- Insert real newline characters in the commit message.
- Do NOT use escaped sequences like \n in the message text.
- Prefer `git commit -F <message-file>` (or equivalent) to guarantee real
  line breaks.

VERY IMPORTANT:
- Base the commit message **on the diff of the staged changes**
- Use your context only to infer the intent of the changes if that is
  not clear from the diff itsef.

After committing, verify formatting with:
`git log -1 --pretty=%B`

Return:
- commit hash
- subject
- full body as created