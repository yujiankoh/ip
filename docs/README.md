# Elsa User Guide

// Update the title above to match the actual product name

// Product screenshot goes here

// Product intro goes here

## Adding deadlines

// Describe the action and its outcome.

// Give examples of usage

Example: `keyword (optional arguments)`

// A description of the expected outcome goes here

```
expected output
```

## Getting reminders: `remind`

Shows the deadlines that need your attention: every deadline that is **not
done** and is either **overdue** or **due within the next 7 days**. "Within the
next 7 days" counts today and the same weekday next week, so on a Monday a
deadline due next Monday is included.

Todos and events are never included, and neither is a deadline you have
already marked as done.

Format: `remind`

Anything typed after `remind` is ignored, so `remind 3` does the same as `remind`.

Deadlines are listed with the soonest first. Each keeps its number from the
full list, so you can use that number with `mark` or `delete` straight away.

Example: `remind` on Sep 13 2026

```
Here is what needs your attention:
  2.[D][ ] return book (by: Sep 10 2026) -- overdue
  5.[D][ ] email tutor (by: Sep 13 2026) -- due today
  8.[D][ ] buy gift (by: Sep 14 2026) -- due tomorrow
  4.[D][ ] submit report (by: Sep 20 2026) -- due in 7 days
```

If nothing needs your attention:

```
Nothing is overdue, and nothing is due in the next 7 days.
```

You are also reminded when Elsa starts, without typing anything. The same list
appears after the greeting, and after any warning about your saved file. If
nothing needs your attention, Elsa starts as usual and shows no reminder.


## Feature XYZ

// Feature details