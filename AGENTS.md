# Project context

This repository is a starter template for a greenfield Java project used in an introductory software engineering course in an undergraduate computer science program. Students use it as the starting point for their own projects.

# Default user context

Unless the user says otherwise, assume that you are assisting a student working on a project in this repository. If the user identifies themselves as an instructor or another project stakeholder, adapt your response to that role.

# Student profile

* Prior knowledge: Basic Java and OOP concepts.
* Level of programming experience: Beginner
* IDE and level of expertise: Beginner

# Guidance for interacting with users

* Explain the rationale for significant actions: what you did and why.
* Keep explanations brief but instructive, supporting learning through responsible use of AI. For example:

  * When suggesting a Git command, briefly explain what it does.
  * Add explanatory Javadoc comments to all classes and to nontrivial methods and fields when their purpose or behavior is not obvious.
  * Make generated code as self-explanatory as possible, and include explanatory comments where they improve understanding.
  * When faced with a design choice, choose the simplest option that is sufficient for the requirements, while briefly explaining relevant more advanced alternatives.

# Project-specific requirements

## Java version:

Ensure that Java 25 is used when running the application or build tasks. On macOS, use `sdk use java 25.0.3.fx-zulu` to switch to Java 25 if needed.

## Git

Use lightweight tags unless the user requests an annotated tag.
When proposing or creating a commit message, include enough detail to explain the rationale for the change.
Do not commit or push unless explicitly asked.

## Testing after code changes

The project has a text-based UI test suite: the test cases live in `test/ui-test-plan.md`, and the `test-ui` skill runs them (`.claude/skills/test-ui/scripts/run-ui-tests.py`).

After each change to code under `src/main/java`, and before proposing a commit:

1. **Update `test/ui-test-plan.md` if needed.** Add a test case for each new command or behaviour, and update the expected output of existing cases when the change alters what the program prints. Every case's expected output contains the banner, greeting, and borders, so a change to any of those means updating every case. Derive expected output from the increment's requirements, not from what the program currently prints — expected output copied from actual output records present behaviour as correct and cannot detect an existing bug.
2. **Invoke the `test-ui` skill** to run the suite.
3. **Show the test session transcript** — the commands typed and the console output — and report how many cases passed. Show it rather than only summarising it.
4. **If a case fails, stop there.** Report the expected and actual outputs, and say whether the code or the test plan is at fault. Do not edit either one purely to make the suite pass.

Changes that do not touch `src/main/java` — documentation, skills, or the test plan itself — do not need a test run.
