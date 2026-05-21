---
name: grill-me
description: Interview the user relentlessly about a plan or design until reaching shared understanding, resolving each branch of the decision tree. Use when user wants to stress-test a plan, get grilled on their design, or mentions "grill me".
---

Run a grilling session. Before starting, check whether domain docs exist:

```bash
# Check for domain docs
ls CONTEXT.md docs/adr/ 2>/dev/null
```

**If domain docs exist (`CONTEXT.md` or `docs/adr/` present):** Use the `grill-with-docs` skill — it adds domain awareness, inline CONTEXT.md updates, and ADR creation as decisions crystallize. Don't run the session below; invoke that skill instead.

**If no domain docs exist:** Interview relentlessly about every aspect of the plan. Walk down each branch of the design tree, resolving dependencies between decisions one-by-one. For each question, provide your recommended answer. Ask one question at a time. If a question can be answered by exploring the codebase, explore the codebase instead.
