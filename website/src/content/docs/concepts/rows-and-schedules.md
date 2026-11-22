---
title: Rows, priorities and schedules
description: How the launcher decides which providers fill the rows of the home screen at any moment.
---

The launcher fills the rows of the active layout from a list of **bindings**. A binding connects a source to the home screen: a recommendation provider, a content rows plugin or a launch source. Nothing in this flow requires a plugin to be Tier 3.

## The pieces

| Term | Meaning |
| --- | --- |
| **Row slot** | Declared by the layout: kind (cards, app tiles, hero banner), height, item size, scroll direction. A layout also declares how many rows it has, which can be unlimited. |
| **Binding** | A source, a display label, a slot kind, a priority schedule, and an optional confidence floor. The same provider can appear in several bindings. |
| **Priority schedule** | A default priority plus rules such as "weekdays, 6:00 to 8:30 am: priority 1". A lower number means a higher priority. A rule can also switch a binding off. |
| **Confidence** | How well a provider thinks its suggestions fit right now, from 0 to 1, for the row and optionally for each title. |
| **Hero slot** | A layout may declare one. It is bound like any row and shows the top title, or rotates the top few. Titles need a backdrop image and a short tagline. |
| **Manual override** | A remote button opens a picker that pins a binding into a row until the next schedule change. |

## The rules

1. **Order by priority.** At any moment the launcher orders bindings by their current priority and fills rows from the top.
2. **Not enough rows means not used.** If there are more bindings than rows, the ones below the last row are not used. They are not asked for anything, so an unused provider receives no data. With an unlimited layout every binding gets a row.
3. **Confidence never outranks priority.** The launcher uses confidence in three ways only: it skips a row whose confidence is below the binding's floor, so the next binding moves up; it breaks ties between equal priorities; and it orders titles inside a row. A provider cannot claim high confidence to push ahead.
4. **Ask in priority order.** The launcher asks bindings in priority order and stops when the rows are full. Rows far below the screen can be asked lazily as the user scrolls.

## Example

A layout with two rows and three recommendation providers. The number is each provider's priority at that time.

| Provider | Weekday mornings | Evenings | All other times |
| --- | --- | --- | --- |
| For the kids (school-day picks) | 1 | 3, not used | 2 |
| For the family (shared picks) | 2 | 2 | 1 |
| For parents (adult picks) | 3, not used | 1 | 3, not used |

So on a weekday morning the rows are the kids' picks and the family picks. In the evening they are the parents' picks and the family picks. At other times they are the family picks and the kids' picks.

## Notes

- Schedules, priorities and confidence floors are launcher data. A plugin never sees the schedule. It is asked for a row only when its turn comes.
- A household writes one recommendation provider per purpose. The launcher has no notion of people. Any per-person tuning lives inside the provider.
- The launcher cannot tell who is watching. A schedule by the clock is a guess, which is why the manual override exists.

## Setting it up with the remote

All configuration can be done with the remote that comes with the TV. The baseline is the D-pad, Select, long-press Select and Back, because Android TV guidance expects apps to work with those buttons. Extra buttons differ by remote and firmware, so nothing depends on them.

- Long-press a row to change its source, edit priorities, set the confidence floor, move or hide it.
- A time scrubber lets you slide a time of day and see which bindings take the rows at that moment.
- The whole setup exports and imports as a file, so labels and long lists can be edited on a PC or phone.
