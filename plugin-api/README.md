# FireEscape plugin API

Versioned AIDL contracts between the launcher and plugin APKs (ADR 0004). Each plugin
class has one bound service, found by the intent action `org.fireescape.plugin.<CLASS>`.
The AIDL sources live in `src/main/aidl`.

## Stability rules

- A contract's major version changes only for breaking edits. The launcher supports two
  majors of a contract at once.
- Within a major, methods are only added at the end of the AIDL interface.
- A plugin declares its contract in service metadata `fireescape.api` (for example
  `spike/1`). A major the launcher does not support is listed as incompatible and never
  bound.
- Calls run off the launcher's main thread under the contract's timeout; a plugin that
  crashes, hangs or answers late never freezes the launcher.

## Published contracts

| Contract | Intent action | Major | Call timeout |
| --- | --- | --- | --- |
| `spike/1` — host-skeleton test contract (M0.5); real classes arrive with M1.x | `org.fireescape.plugin.SPIKE` | 1 | 500 ms |
