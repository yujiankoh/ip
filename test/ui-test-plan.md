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
