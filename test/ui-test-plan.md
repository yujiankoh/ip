# UI Test Plan

Text-based UI tests for the Elsa chatbot. Each test case below feeds a list of
commands to the program on standard input and compares the whole console output
against the expected output recorded here.

Run the tests with:

```
python .claude/skills/test-ui/scripts/run-ui-tests.py
```

## How the program is run

The runner starts the program with this command, from the repository root:

```run
java src/main/java/Elsa.java
```

Java 25's source launcher compiles `Task.java` alongside `Elsa.java`, so no
separate build step is needed.

## How outputs are compared

- The **whole** console output is compared, including the banner and greeting.
- Trailing whitespace on each line is ignored, as is any blank line at the very
  end of the output. This matters because the banner lines end in spaces that an
  editor may trim from this file.
- Everything else must match exactly, including leading indentation.

Because the greeting and the border style appear in every expected output,
changing either of them means updating every test case below. The runner prints
a line-by-line difference on failure, which shows exactly what to change.

## How to add a test case

Add a `###` heading, an `**Aim:**` line, an ` ```input ` block, and an
` ```expected ` block. Cases run top to bottom and the session stops at the
first failure.

---

### TC-1 - Greet and exit immediately

**Aim:** Check that the chatbot greets the user on startup and exits on `bye`, with no tasks involved.

```input
bye
```

```expected
    * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
      _____ _
     |  ___| |___  __ _
     | |__ | / __|/ _` |
     |  __|| \__ \ (_| |
     |_____|_|___/\__,_|
     Hello! I'm Elsa.
     Do you want to build a snowman?
    * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *

    * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
     The cold never bother me anyways!
    * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
```

---

### TC-2 - List when nothing has been added

**Aim:** Check that `list` on an empty task list reports the empty-list message instead of printing an empty block.

```input
list
bye
```

```expected
    * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
      _____ _
     |  ___| |___  __ _
     | |__ | / __|/ _` |
     |  __|| \__ \ (_| |
     |_____|_|___/\__,_|
     Hello! I'm Elsa.
     Do you want to build a snowman?
    * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *

    * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
     Into the Unknown.
    * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *

    * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
     The cold never bother me anyways!
    * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
```

---

### TC-3 - Add tasks and list them

**Aim:** Check that entered text is stored, confirmed with `added:`, and listed back in entry order, numbered from 1 and shown as not done.

```input
read book
return book
list
bye
```

```expected
    * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
      _____ _
     |  ___| |___  __ _
     | |__ | / __|/ _` |
     |  __|| \__ \ (_| |
     |_____|_|___/\__,_|
     Hello! I'm Elsa.
     Do you want to build a snowman?
    * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *

    * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
     added: read book
    * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *

    * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
     added: return book
    * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *

    * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
     Here are the tasks in your list:
     1.[ ] read book
     2.[ ] return book
    * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *

    * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
     The cold never bother me anyways!
    * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
```

---

### TC-4 - Mark a task as done

**Aim:** Check that `mark 2` reports the task it changed and that only that task shows `[X]` in a later `list`, confirming the 1-based number maps to the right task.

```input
read book
return book
mark 2
list
bye
```

```expected
    * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
      _____ _
     |  ___| |___  __ _
     | |__ | / __|/ _` |
     |  __|| \__ \ (_| |
     |_____|_|___/\__,_|
     Hello! I'm Elsa.
     Do you want to build a snowman?
    * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *

    * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
     added: read book
    * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *

    * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
     added: return book
    * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *

    * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
     Nice! I've marked this task as done:
       [X] return book
    * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *

    * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
     Here are the tasks in your list:
     1.[ ] read book
     2.[X] return book
    * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *

    * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
     The cold never bother me anyways!
    * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
```

---

### TC-5 - Unmark a task that was marked done

**Aim:** Check that `unmark` reverses a previous `mark`, returning the task to `[ ]` rather than leaving it done or removing it.

```input
read book
mark 1
unmark 1
list
bye
```

```expected
    * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
      _____ _
     |  ___| |___  __ _
     | |__ | / __|/ _` |
     |  __|| \__ \ (_| |
     |_____|_|___/\__,_|
     Hello! I'm Elsa.
     Do you want to build a snowman?
    * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *

    * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
     added: read book
    * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *

    * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
     Nice! I've marked this task as done:
       [X] read book
    * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *

    * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
     OK, I've marked this task as not done yet:
       [ ] read book
    * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *

    * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
     Here are the tasks in your list:
     1.[ ] read book
    * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *

    * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
     The cold never bother me anyways!
    * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
```

---

### TC-6 - Add todos and list them

**Aim:** Check that `todo` adds a task marked `[T]`, confirms it with the running task count, and that the type marker survives being marked done.

```input
todo borrow book
todo read book
mark 2
list
bye
```

```expected
    * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
      _____ _
     |  ___| |___  __ _
     | |__ | / __|/ _` |
     |  __|| \__ \ (_| |
     |_____|_|___/\__,_|
     Hello! I'm Elsa.
     Do you want to build a snowman?
    * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *

    * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
     Got it. I've added this task:
       [T][ ] borrow book
     Now you have 1 task in the list.
    * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *

    * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
     Got it. I've added this task:
       [T][ ] read book
     Now you have 2 tasks in the list.
    * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *

    * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
     Nice! I've marked this task as done:
       [T][X] read book
    * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *

    * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
     Here are the tasks in your list:
     1.[T][ ] borrow book
     2.[T][X] read book
    * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *

    * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
     The cold never bother me anyways!
    * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
```

---

### TC-7 - Add deadlines

**Aim:** Check that `deadline` splits the description from the time given after `/by`, displays the task as `[D]` with the time in brackets, and treats the time as free text rather than a real date.

```input
todo borrow book
deadline return book /by Sunday
deadline do homework /by no idea :-p
list
bye
```

```expected
    * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
      _____ _
     |  ___| |___  __ _
     | |__ | / __|/ _` |
     |  __|| \__ \ (_| |
     |_____|_|___/\__,_|
     Hello! I'm Elsa.
     Do you want to build a snowman?
    * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *

    * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
     Got it. I've added this task:
       [T][ ] borrow book
     Now you have 1 task in the list.
    * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *

    * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
     Got it. I've added this task:
       [D][ ] return book (by: Sunday)
     Now you have 2 tasks in the list.
    * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *

    * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
     Got it. I've added this task:
       [D][ ] do homework (by: no idea :-p)
     Now you have 3 tasks in the list.
    * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *

    * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
     Here are the tasks in your list:
     1.[T][ ] borrow book
     2.[D][ ] return book (by: Sunday)
     3.[D][ ] do homework (by: no idea :-p)
    * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *

    * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
     The cold never bother me anyways!
    * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
```

---

### TC-8 - Add events and list all three task types together

**Aim:** Check that `event` splits the description, start time, and end time apart on `/from` and `/to`, displays the task as `[E]`, and that todos, deadlines, and events coexist in one list and can each be marked done.

```input
todo borrow book
deadline return book /by Sunday
event project meeting /from Mon 2pm /to 4pm
mark 3
list
bye
```

```expected
    * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
      _____ _
     |  ___| |___  __ _
     | |__ | / __|/ _` |
     |  __|| \__ \ (_| |
     |_____|_|___/\__,_|
     Hello! I'm Elsa.
     Do you want to build a snowman?
    * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *

    * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
     Got it. I've added this task:
       [T][ ] borrow book
     Now you have 1 task in the list.
    * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *

    * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
     Got it. I've added this task:
       [D][ ] return book (by: Sunday)
     Now you have 2 tasks in the list.
    * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *

    * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
     Got it. I've added this task:
       [E][ ] project meeting (from: Mon 2pm to: 4pm)
     Now you have 3 tasks in the list.
    * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *

    * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
     Nice! I've marked this task as done:
       [E][X] project meeting (from: Mon 2pm to: 4pm)
    * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *

    * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
     Here are the tasks in your list:
     1.[T][ ] borrow book
     2.[D][ ] return book (by: Sunday)
     3.[E][X] project meeting (from: Mon 2pm to: 4pm)
    * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *

    * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
     The cold never bother me anyways!
    * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
```
