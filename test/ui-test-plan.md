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

The chatbot saves its tasks to `./data/elsa.txt` under the repository root and
reads them back at startup, so the runner puts that file into a known state
before every case:

- a case with a ```` ```data ```` block starts with exactly those lines saved;
- a case without one starts with no data file at all, as on a first ever run.

Cases are therefore independent of each other and of anything left behind by an
earlier run. Saving itself is still not checked directly, because it prints
nothing to the console; loading is, because what was loaded shows up in `list`.

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

A case may also carry an ` ```data ` block, placed before the input block. Its
lines are written to `./data/elsa.txt` before the program starts, which is how a
case sets up tasks that an earlier run is meant to have saved.

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

### TC-3 - Reject an unknown command and an empty todo description

**Aim:** Check the two errors from the Level-5 examples are reported with the OLAF!!! prefix and say how to correct the input, and that rejecting an input leaves stored tasks untouched.

```input
todo borrow book
todo
blah
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
     OLAF!!! The description of a todo cannot be empty. Use: todo <description>
    * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *

    * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
     OLAF!!! I'm sorry, but I don't know what that means :-(
    * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *

    * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
     Here are the tasks in your list:
     1.[T][ ] borrow book
    * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *

    * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
     The cold never bother me anyways!
    * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
```

---

### TC-4 - Mark a task as done

**Aim:** Check that `mark 2` reports the task it changed and that only that task shows `[X]` in a later `list`, confirming the 1-based number maps to the right task.

```input
todo read book
todo return book
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
       [T][ ] read book
     Now you have 1 task in the list.
    * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *

    * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
     Got it. I've added this task:
       [T][ ] return book
     Now you have 2 tasks in the list.
    * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *

    * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
     Nice! I've marked this task as done:
       [T][X] return book
    * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *

    * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
     Here are the tasks in your list:
     1.[T][ ] read book
     2.[T][X] return book
    * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *

    * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
     The cold never bother me anyways!
    * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
```

---

### TC-5 - Unmark a task that was marked done

**Aim:** Check that `unmark` reverses a previous `mark`, returning the task to `[ ]` rather than leaving it done or removing it.

```input
todo read book
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
     Got it. I've added this task:
       [T][ ] read book
     Now you have 1 task in the list.
    * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *

    * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
     Nice! I've marked this task as done:
       [T][X] read book
    * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *

    * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
     OK, I've marked this task as not done yet:
       [T][ ] read book
    * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *

    * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
     Here are the tasks in your list:
     1.[T][ ] read book
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

---

### TC-9 - Reject empty deadline and event descriptions between valid adds

**Aim:** Check that each kind of task names itself in the error, with the correct article, and that a rejected add does not consume a slot or shift the numbering of the tasks after it.

```input
deadline
deadline return book /by Sunday
event
event project meeting /from Mon 2pm /to 4pm
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
     OLAF!!! The description of a deadline cannot be empty. Use: deadline <description> /by <when>
    * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *

    * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
     Got it. I've added this task:
       [D][ ] return book (by: Sunday)
     Now you have 1 task in the list.
    * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *

    * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
     OLAF!!! The description of an event cannot be empty. Use: event <description> /from <start> /to <end>
    * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *

    * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
     Got it. I've added this task:
       [E][ ] project meeting (from: Mon 2pm to: 4pm)
     Now you have 2 tasks in the list.
    * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *

    * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
     Here are the tasks in your list:
     1.[D][ ] return book (by: Sunday)
     2.[E][ ] project meeting (from: Mon 2pm to: 4pm)
    * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *

    * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
     The cold never bother me anyways!
    * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
```

---

### TC-10 - Handle a blank line, surrounding spaces, and the wrong letter case

**Aim:** Check that an empty line is reported rather than silently ignored, that spaces around a command and its description are trimmed away, and that commands are case sensitive so `BYE` does not end the session.

```input

   todo   read book   
BYE
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
     OLAF!!! You did not type anything. Try "todo <description>", or "list" to see what you have.
    * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *

    * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
     Got it. I've added this task:
       [T][ ] read book
     Now you have 1 task in the list.
    * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *

    * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
     OLAF!!! I'm sorry, but I don't know what that means :-(
    * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *

    * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
     Here are the tasks in your list:
     1.[T][ ] read book
    * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *

    * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
     The cold never bother me anyways!
    * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
```

---

### TC-11 - Reject task numbers that are missing, not numbers, or out of range

**Aim:** Check that each way of getting a task number wrong is reported differently rather than ending the session, and that a valid mark still works afterwards, showing the rejected commands changed nothing.

```input
mark 1
todo read book
mark abc
mark 99
mark 0
unmark
mark 1
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
     OLAF!!! There are no tasks yet, so there is nothing to mark. Add one with "todo <description>" first.
    * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *

    * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
     Got it. I've added this task:
       [T][ ] read book
     Now you have 1 task in the list.
    * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *

    * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
     OLAF!!! "abc" is not a task number. Use a whole number, for example: mark 2.
    * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *

    * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
     OLAF!!! There is no task 99. You have 1 task, so use a number from 1 to 1.
    * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *

    * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
     OLAF!!! There is no task 0. You have 1 task, so use a number from 1 to 1.
    * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *

    * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
     OLAF!!! Which task? Use: unmark <task number>, for example: unmark 2.
    * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *

    * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
     Nice! I've marked this task as done:
       [T][X] read book
    * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *

    * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
     Here are the tasks in your list:
     1.[T][X] read book
    * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *

    * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
     The cold never bother me anyways!
    * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
```

---

### TC-12 - Reject deadlines and events with missing or empty parts

**Aim:** Check that a missing separator, an empty description and an empty time are each reported differently with the correct usage, and that valid adds in between still number correctly.

```input
deadline x
deadline /by Sunday
deadline homework /by
deadline submit report /by Sunday
event meeting /from Mon
event project meeting /from Mon 2pm /to 4pm
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
     OLAF!!! I could not find "/by" in that. Use: deadline <description> /by <when>
    * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *

    * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
     OLAF!!! The description of a deadline cannot be empty. Use: deadline <description> /by <when>
    * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *

    * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
     OLAF!!! The due time after /by cannot be empty. Use: deadline <description> /by <when>
    * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *

    * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
     Got it. I've added this task:
       [D][ ] submit report (by: Sunday)
     Now you have 1 task in the list.
    * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *

    * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
     OLAF!!! I could not find "/to" in that. Use: event <description> /from <start> /to <end>
    * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *

    * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
     Got it. I've added this task:
       [E][ ] project meeting (from: Mon 2pm to: 4pm)
     Now you have 2 tasks in the list.
    * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *

    * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
     Here are the tasks in your list:
     1.[D][ ] submit report (by: Sunday)
     2.[E][ ] project meeting (from: Mon 2pm to: 4pm)
    * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *

    * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
     The cold never bother me anyways!
    * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
```

---

### TC-13 - Reject events with a missing separator or an empty part

**Aim:** Check all four ways an event's arguments can be wrong are reported separately, mirroring the deadline coverage in TC-12, since event parsing splits twice and a fault in either split would otherwise go unnoticed. The valid add at the end confirms the four rejections left the list empty rather than half filled.

```input
event meeting /to 4pm
event /from Mon /to 4pm
event meeting /from /to 4pm
event meeting /from Mon /to
event standup /from Mon /to Tue
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
     OLAF!!! I could not find "/from" in that. Use: event <description> /from <start> /to <end>
    * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *

    * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
     OLAF!!! The description of an event cannot be empty. Use: event <description> /from <start> /to <end>
    * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *

    * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
     OLAF!!! The start time after /from cannot be empty. Use: event <description> /from <start> /to <end>
    * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *

    * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
     OLAF!!! The end time after /to cannot be empty. Use: event <description> /from <start> /to <end>
    * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *

    * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
     Got it. I've added this task:
       [E][ ] standup (from: Mon to: Tue)
     Now you have 1 task in the list.
    * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *

    * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
     Here are the tasks in your list:
     1.[E][ ] standup (from: Mon to: Tue)
    * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *

    * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
     The cold never bother me anyways!
    * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
```

---

### TC-14 - Delete a task from the middle of the list

**Aim:** Check that `delete` reports the task it removed and the remaining count, and that the tasks after it are renumbered. The `mark 3` afterwards targets the task that moved up into position 3, proving the renumbering applies to later commands and is not just how `list` happens to print.

```input
todo read book
deadline return book /by June 6th
event project meeting /from Aug 6th 2pm /to 4pm
todo join sports club
delete 3
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
       [T][ ] read book
     Now you have 1 task in the list.
    * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *

    * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
     Got it. I've added this task:
       [D][ ] return book (by: June 6th)
     Now you have 2 tasks in the list.
    * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *

    * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
     Got it. I've added this task:
       [E][ ] project meeting (from: Aug 6th 2pm to: 4pm)
     Now you have 3 tasks in the list.
    * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *

    * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
     Got it. I've added this task:
       [T][ ] join sports club
     Now you have 4 tasks in the list.
    * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *

    * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
     Noted. I've removed this task:
       [E][ ] project meeting (from: Aug 6th 2pm to: 4pm)
     Now you have 3 tasks in the list.
    * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *

    * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
     Nice! I've marked this task as done:
       [T][X] join sports club
    * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *

    * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
     Here are the tasks in your list:
     1.[T][ ] read book
     2.[D][ ] return book (by: June 6th)
     3.[T][X] join sports club
    * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *

    * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
     The cold never bother me anyways!
    * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
```

---

### TC-15 - Reject bad delete numbers, then delete the last task

**Aim:** Check that `delete` rejects a missing, non-numeric and out-of-range number the same way `mark` does, that the rejections leave the task in place, and that deleting the only task empties the list rather than leaving a stale entry behind.

```input
delete 1
todo read book
delete abc
delete 99
delete
delete 1
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
     OLAF!!! There are no tasks yet, so there is nothing to delete. Add one with "todo <description>" first.
    * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *

    * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
     Got it. I've added this task:
       [T][ ] read book
     Now you have 1 task in the list.
    * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *

    * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
     OLAF!!! "abc" is not a task number. Use a whole number, for example: delete 2.
    * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *

    * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
     OLAF!!! There is no task 99. You have 1 task, so use a number from 1 to 1.
    * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *

    * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
     OLAF!!! Which task? Use: delete <task number>, for example: delete 2.
    * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *

    * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
     Noted. I've removed this task:
       [T][ ] read book
     Now you have 0 tasks in the list.
    * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *

    * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
     Into the Unknown.
    * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *

    * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
     The cold never bother me anyways!
    * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
```

---

### TC-16 - Change the list in every way that triggers a save

**Aim:** Saving to `./data/elsa.txt` happens whenever the task list changes, and it must be invisible from the console: it may not print anything of its own, and it may not stop a command from finishing. This case runs every command that changes the list -- the three adds, `mark`, `unmark` and `delete` -- so a save that printed a stray line, or failed and turned a confirmation into an `OLAF!!!` error, would show up as a difference here. The closing `list` confirms the six changes left the task list in the state the saved file should mirror.

```input
todo read book
deadline return book /by June 6th
event project meeting /from Aug 6th 2pm /to 4pm
mark 1
unmark 1
delete 2
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
       [T][ ] read book
     Now you have 1 task in the list.
    * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *

    * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
     Got it. I've added this task:
       [D][ ] return book (by: June 6th)
     Now you have 2 tasks in the list.
    * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *

    * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
     Got it. I've added this task:
       [E][ ] project meeting (from: Aug 6th 2pm to: 4pm)
     Now you have 3 tasks in the list.
    * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *

    * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
     Nice! I've marked this task as done:
       [T][X] read book
    * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *

    * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
     OK, I've marked this task as not done yet:
       [T][ ] read book
    * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *

    * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
     Noted. I've removed this task:
       [D][ ] return book (by: June 6th)
     Now you have 2 tasks in the list.
    * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *

    * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
     Here are the tasks in your list:
     1.[T][ ] read book
     2.[E][ ] project meeting (from: Aug 6th 2pm to: 4pm)
    * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *

    * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
     The cold never bother me anyways!
    * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
```

---

### TC-17 - Start with tasks saved by an earlier run

**Aim:** Level-7 requires the tasks to be read back from the hard disk at startup. This case starts with a data file holding one of each kind of task, so `list` must show all three with the descriptions, times and done markers they were saved with, rather than the empty-list message. The `delete 2` and `unmark 1` afterwards check that loaded tasks are ordinary members of the list: they can be numbered, changed and removed exactly like tasks typed in this session.

```data
T | 1 | read book
D | 0 | return book | June 6th
E | 0 | project meeting | Aug 6th 2pm | 4pm
```

```input
list
delete 2
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
     Here are the tasks in your list:
     1.[T][X] read book
     2.[D][ ] return book (by: June 6th)
     3.[E][ ] project meeting (from: Aug 6th 2pm to: 4pm)
    * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *

    * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
     Noted. I've removed this task:
       [D][ ] return book (by: June 6th)
     Now you have 2 tasks in the list.
    * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *

    * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
     OK, I've marked this task as not done yet:
       [T][ ] read book
    * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *

    * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
     Here are the tasks in your list:
     1.[T][ ] read book
     2.[E][ ] project meeting (from: Aug 6th 2pm to: 4pm)
    * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *

    * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
     The cold never bother me anyways!
    * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
```

---

### TC-18 - Keep the good tasks when some lines cannot be understood

**Aim:** A hand-edited data file can go wrong in several ways at once, and one bad line must not cost the user the tasks on every other line. This file has a done marker that is neither 1 nor 0, a deadline with no due time, an unknown type letter, and a blank line. The three faulty lines must each be reported with their line number and what is wrong, the blank line must pass without comment, and the two sound tasks must load and be usable. The warning has to say that saving rewrites the file, because the `todo` at the end does exactly that and the skipped lines are then gone.

```data
T | 1 | read book
T | 7 | bad marker
D | 0 | return book

X | 0 | mystery
T | 0 | buy milk
```

```input
list
todo water plants
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
     OLAF!!! I could not understand 3 lines of data/elsa.txt, so I have left them out:
       Line 2: "7" is not a done marker; it should be 1 or 0
       Line 3: a D task needs 4 fields, but this line has 3
       Line 5: "X" is not a task type; it should be T, D or E
     Your other tasks loaded normally. Saving will rewrite the file without the lines above, so edit the file now if you want to keep them.
    * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *

    * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
     Here are the tasks in your list:
     1.[T][X] read book
     2.[T][ ] buy milk
    * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *

    * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
     Got it. I've added this task:
       [T][ ] water plants
     Now you have 3 tasks in the list.
    * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *

    * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
     The cold never bother me anyways!
    * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
```

---

### TC-19 - Refuse text that would break the saved file apart

**Aim:** Tasks are stored with their fields separated by a bar with a space on each side, so a description or a time containing that text would be read back as extra fields and the task would return changed or not at all. The chatbot must refuse it when it is typed, where the user can still fix it, rather than accepting it and losing part of the task at the next startup. Every part the user supplies is checked, not only descriptions, so a time is tried here too. The valid `todo` at the end shows the four rejections changed nothing.

```input
todo a | b
deadline a | b /by Sunday
deadline return book /by Sun | day
event project meeting /from 2pm | 3pm /to 4pm
todo read book
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
     OLAF!!! The description of a todo cannot contain "|" with a space on each side, because that is how data/elsa.txt separates the parts of a task. Use: todo <description>
    * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *

    * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
     OLAF!!! The description of a deadline cannot contain "|" with a space on each side, because that is how data/elsa.txt separates the parts of a task. Use: deadline <description> /by <when>
    * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *

    * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
     OLAF!!! The due time after /by cannot contain "|" with a space on each side, because that is how data/elsa.txt separates the parts of a task. Use: deadline <description> /by <when>
    * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *

    * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
     OLAF!!! The start time after /from cannot contain "|" with a space on each side, because that is how data/elsa.txt separates the parts of a task. Use: event <description> /from <start> /to <end>
    * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *

    * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
     Got it. I've added this task:
       [T][ ] read book
     Now you have 1 task in the list.
    * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *

    * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
     Here are the tasks in your list:
     1.[T][ ] read book
    * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *

    * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
     The cold never bother me anyways!
    * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
```

---

### TC-20 - Separate a command from its argument with a tab

**Aim:** A user who types or pastes a tab instead of a space has still named a command and given it an argument, so the chatbot should read it the same way rather than rejecting the whole line as an unknown command. Note for anyone editing this file: the two input lines below contain real tab characters, and replacing them with spaces would leave the case passing without testing anything.

```input
todo	read book
mark	1
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
       [T][ ] read book
     Now you have 1 task in the list.
    * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *

    * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
     Nice! I've marked this task as done:
       [T][X] read book
    * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *

    * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
     Here are the tasks in your list:
     1.[T][X] read book
    * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *

    * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
     The cold never bother me anyways!
    * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
```
