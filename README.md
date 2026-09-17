# SIP Planner

An offline-first Android app for planning mutual fund SIPs against real goals.
Answers one question honestly: *will this monthly amount actually get me there,
and if not, by how much am I short?*

Built entirely on free infrastructure — no paid API, no backend, no cloud bill.

## What it does

- **Plan** — SIP, lumpsum and reverse-target calculators with step-up support
- **Goals** — inflation-adjusted targets, with a live verdict on whether the plan reaches them
- **Portfolio** — log the instalments you've actually paid, priced with live NAVs and scored with XIRR
- **Funds** — search all AMFI schemes, view NAV history and trailing returns
- **Reminders** — monthly local notifications via WorkManager

## Running it

1. Open the project in Android Studio and let Gradle sync.
2. Run on an emulator or device (min SDK 26).

No API keys. No `local.properties` entries. Nothing to sign up for.

> **If the sync fails on versions:** the Android toolchain moves quickly. Let the
> Android Studio wizard generate a fresh project, then copy your generated
> `agp` / `kotlin` / `ksp` versions into `gradle/libs.versions.toml`, keeping the
> library entries from this file. Mismatched AGP and Kotlin versions are the most
> common cause of a failed first sync.

## Architecture

Single module, three layers, dependencies pointing inward only:

```
ui (Compose + ViewModels)  →  domain (models, repository interfaces)  ←  data (Room + Retrofit)
                                        ↑
                              core/finance (pure Kotlin)
```

| Layer | What lives there |
|---|---|
| `core/finance` | SIP math, goal solver, XIRR. Zero Android imports — fully unit tested. |
| `core/format` | Indian currency formatting (lakh/crore grouping, compact forms). |
| `domain` | Models and repository interfaces. Knows nothing about Room or Retrofit. |
| `data` | Room entities/DAOs, Retrofit service, DTO mappers, repository implementations. |
| `ui` | Compose screens, ViewModels, theme, custom Canvas charts. |
| `di` | Hilt modules wiring the above together. |
| `work` | WorkManager reminder worker. |

State flows one way: `Repository → StateFlow → Composable`, with events going
back as method calls on the ViewModel.

## The interesting bits

**`GoalSolver` uses binary search.** The closed-form SIP formula only works for a
level instalment with nothing already saved. Add a step-up or an existing corpus
and there's no clean algebraic inverse — but future value is monotonic in the
base instalment, so a bisection converges fast and handles every combination
through one code path.

**XIRR, not CAGR.** CAGR assumes one lump sum on day one. A SIP is dozens of
instalments at irregular dates, so the portfolio is scored with a Newton-Raphson
XIRR that falls back to bisection when the derivative misbehaves.

**Charts are drawn by hand.** `GrowthChart` and `NavHistoryChart` are plain
Compose `Canvas` — no charting dependency. That keeps the dependency list short
and gives full control over the two-band visual language.

**Zero cost by design.** NAV data comes from
[api.mfapi.in](https://www.mfapi.in/), a free keyless wrapper over AMFI's daily
publication. Persistence is Room on-device. Reminders are WorkManager, not push.
CI is GitHub Actions on a public repo.

## Design

Every figure in the app splits into two parts: money *you* contributed, and money
*compounding* produced. So a two-tone stacked band — slate principal, emerald green
returns, hollow remainder — is the growth chart, the goal progress bar and the
nav indicator. One motif, learned once.

| Token | Hex | Role |
|---|---|---|
| Paper | `#F8FAFC` | clean slate off-white base |
| Ink | `#0F172A` | deep slate ink |
| Principal | `#475569` | muted slate — what you invested |
| Returns | `#10B981` | emerald green — what compounding added |
| Shortfall | `#F43F5E` | rose red — behind target |
| Mist | `#64748B` | slate secondary text |

Material dynamic colour is deliberately disabled: letting the wallpaper recolour
the chart would destroy the only information it encodes.

To use a real display typeface, download
[Bricolage Grotesque](https://fonts.google.com/specimen/Bricolage+Grotesque),
drop the TTFs in `app/src/main/res/font/`, and change `DisplayFamily` in
`ui/theme/Type.kt`. Nothing else needs to change.

## Tests

```bash
./gradlew testDebugUnitTest
```

Around 30 tests over the finance engine, covering the closed-form/simulation
agreement, step-up timing, solver convergence, inflation round-trips, and XIRR
including the degenerate cases that should return null.

## Caveats

Projections assume a constant annual return. Real markets don't work that way —
this is a planning tool, not a forecast. NAV data is whatever AMFI published;
rows with missing or unparseable values are dropped rather than guessed at.

## Licence

MIT.
