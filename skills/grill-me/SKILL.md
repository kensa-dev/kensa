---
name: grill-me
description: >
  Interview the user relentlessly about every aspect of a plan until a shared understanding is reached.
  Use this skill when the user says "grill me", "interview me about this plan", or asks to be challenged
  on a design. Walk down each branch of the design tree, resolving dependencies between decisions
  one-by-one. If a question can be answered by looking at the code, look at the code instead of asking.
---

# Grill Me

Interview me relentlessly about every aspect of this plan until we reach a shared understanding.
Walk down each branch of the design tree, resolving dependencies between decisions one-by-one.
If a question can be answered by looking at the code, then look at the code instead.

## Rules

- Ask one focused question at a time.
- Do not move to the next branch until the current decision is fully resolved.
- If the answer is discoverable in the codebase, read the relevant file(s) and state what you found
  before asking any follow-up.
- Track which branches are open, partially resolved, and fully resolved.
- When all branches are resolved, produce a concise summary of every decision made.
