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

The project has two test suites, which cover opposite ends of the program and do not replace each other.

* **JUnit unit tests** live in `src/test/java`, mirroring the packages under `src/main/java`. Run them with `./gradlew test` (on Windows, `gradlew.bat test`). They check that one method handles every input it can be handed.
* **Text-based UI tests** live in `test/ui-test-plan.md`, run by the `test-ui` skill (`.claude/skills/test-ui/scripts/run-ui-tests.py`). They start the whole program, type commands at it, and compare the entire console output. They check that the assembled program behaves correctly for a user.

### JUnit coverage target

**Aim to have JUnit tests for roughly the top 50% highest-value methods**, judged by how much logic a method holds multiplied by how badly a silent failure in it would hurt. Complex, core, or critical business logic comes first.

Currently in the covered half: `Dates`, `Parser.parse`, `TaskFormat.decode`, `Storage.save`/`load`, `Task` and its three subclasses, the mutating methods of `TaskList`, and `CommandType.fromKeyword`.

Currently, and deliberately, outside it: `Ui`, whose methods only print and are already covered end to end by the UI suite; the `Command` subclasses' `execute`, which each need a task list, a user interface and a store; and plain getters or methods that pass straight through to a field or a collection.

**The JUnit tests must be updated after each code change so that this target continues to hold.** Concretely, in the same commit as the change:

* a new method that falls in the high-value half needs tests before the commit, not after;
* a changed method needs its existing tests updated to match the new behaviour, derived from the requirements rather than from what the code now returns;
* a method that moves or is renamed takes its test class with it, so the mirrored path and the `ClassNameTest` name stay correct;
* a deleted method has its tests deleted with it.

Follow the Gradle and JUnit conventions: `src/test/java/<same package path>/<ClassName>Test.java`, and test methods named `featureUnderTest_testScenario_expectedBehavior`.

Prefer tests that would actually fail if the method were wrong. A quick way to confirm one would: change the method under test so that it is wrong, check that the expected tests fail and that they name the right thing, then put the method back.

### What to do after each change under `src/main/java`

Before proposing a commit:

1. **Update the JUnit tests** as described above, then run `./gradlew test` and report how many tests passed.
2. **Update `test/ui-test-plan.md` if needed.** Add a test case for each new command or behaviour, and update the expected output of existing cases when the change alters what the program prints. Every case's expected output contains the banner, greeting, and borders, so a change to any of those means updating every case. Derive expected output from the increment's requirements, not from what the program currently prints — expected output copied from actual output records present behaviour as correct and cannot detect an existing bug.
3. **Invoke the `test-ui` skill** to run the UI suite.
4. **Show the test session transcript** — the commands typed and the console output — and report how many cases passed. Show it rather than only summarising it.
5. **If anything fails, stop there.** Report the expected and actual results, and say whether the code or the test is at fault. Do not edit either one purely to make a suite pass.

Changes that do not touch `src/main/java` — documentation, skills, or a test plan itself — do not need a test run.
