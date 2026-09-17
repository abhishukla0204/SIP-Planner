# SIP Planner

[![Android Studio](https://img.shields.io/badge/Android%20Studio-2024.2.1-green.svg)](https://developer.android.com/studio)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.2.10-blue.svg)](https://kotlinlang.org)
[![Min SDK](https://img.shields.io/badge/Min%20SDK-26-orange.svg)](https://developer.android.com)
[![Compile SDK](https://img.shields.io/badge/Compile%20SDK-37-brightgreen.svg)](https://developer.android.com)

An **offline-first, privacy-focused Android application** for planning mutual fund SIPs, goal-based wealth tracking, and portfolio performance analysis.

Built entirely on **free, keyless infrastructure** — no paid APIs, no backend cloud servers, no user tracking, and no subscriptions.

---

## App Modules & Screen Previews

### 1. Portfolio Management (`Portfolio` Tab)

<p align="center">
  <img src="images/image1.png" width="32%" alt="Portfolio Dashboard" />
</p>

* **Live Portfolio Valuation:** Tracks overall net portfolio value powered by daily AMFI NAV updates.
* **Gain / Loss Breakdown:** Visualizes capital contributed (Slate) versus compounding gains added (Emerald Green).
* **Scheme Holdings:** Displays all mutual fund holdings with total units accumulated and current NAV prices.
* **`+ Log Investment`:** Quick dialog to log paid SIP instalments with auto-filled NAVs.

---

### 2. Wealth & SIP Calculators (`Plan` Tab)

<p align="center">
  <img src="images/image2.png" width="30%" alt="Monthly SIP Calculator" />
  &nbsp;&nbsp;&nbsp;&nbsp;
  <img src="images/image3.png" width="30%" alt="Lumpsum Calculator" />
  &nbsp;&nbsp;&nbsp;&nbsp;
  <img src="images/image4.png" width="30%" alt="Reverse Target Calculator" />
</p>

* **Monthly SIP Mode (`image 1`):** Projects compounding wealth for regular or yearly step-up SIPs.
* **One-Time Lumpsum Mode (`image 2`):** Projects growth for single lump sum investments over time.
* **Reach a Target Mode (`image4 3`):** Reverse-calculates the exact monthly instalment required to land on any target amount.
* **Dual Control Inputs:** Smooth slider dragging combined with interactive numeric keyboard input boxes.

---

### 3. Goal-Based Planning (`Goals` Tab)

<p align="center">
  <img src="images/image5.png" width="30%" alt="Goal Input & Assumptions" />
  &nbsp;&nbsp;&nbsp;&nbsp;
  <img src="images/image6.png" width="30%" alt="Goal Inflation Verdict" />
  &nbsp;&nbsp;&nbsp;&nbsp;
  <img src="images/image7.png" width="30%" alt="Goals Dashboard" />
</p>

* **New Goal Setup (`image 1`):** Input target cost today, target year, monthly investment capacity, and expected inflation rate.
* **Live Inflation Verdict (`image 2`):** Calculates future inflated target cost and provides an instant verdict (*"This plan gets you there"* or *"Short by ₹X.XX L"*).
* **Goals Dashboard (`image 3`):** Overview of all active goals, monthly commitment total, and on-track status.

---

### 4. Mutual Fund Watchlist & Search (`Funds` Tab)

<p align="center">
  <img src="images/image8.png" width="32%" alt="Mutual Fund Watchlist & Search" />
</p>

* **Keyless AMFI Search:** Search over 10,000+ public Indian mutual fund schemes via `api.mfapi.in`.
* **Following Watchlist:** Pin your favorite mutual funds to the top of the Funds screen with live NAV prices.
* **Historical NAV & Returns:** View historical NAV line charts and annualised 1Y, 3Y, and 5Y trailing returns.

---

## Tech Stack & Architecture

- **UI Framework:** [Jetpack Compose](https://developer.android.com/jetpack/compose) with Material 3 Design System
- **Architecture:** Clean Architecture (Core, Domain, Data, UI) + Unidirectional Data Flow (UDF)
- **Dependency Injection:** [Hilt](https://developer.android.com/training/dependency-injection/hilt-android)
- **Local Database:** [Room Database](https://developer.android.com/training/data-storage/room)
- **Networking:** Retrofit 2 + OkHttp 5 + Kotlinx Serialization
- **Background Work:** WorkManager for local monthly notifications
- **Graphics:** Custom Compose `Canvas` drawing for zero-dependency high-performance charts

### Architecture Diagram & Layer Breakdown

```
ui (Compose + ViewModels)  ──►  domain (Models + Repository Interfaces)  ◄──  data (Room + Retrofit)
                                               ▲
                                     core/finance (Pure Kotlin)
```

| Layer | Responsibility |
| :--- | :--- |
| **`core/finance`** | Pure Kotlin financial math engine (SIP math, Bisection Goal Solver, Newton-Raphson XIRR). Zero Android dependencies, 100% unit tested. |
| **`core/format`** | Indian currency formatting engine (Lakh / Crore Indian Numbering System formatting). |
| **`domain`** | Business entities and repository interfaces. |
| **`data`** | Room DAOs, Retrofit API client (`api.mfapi.in`), and repository implementations. |
| **`ui`** | Jetpack Compose screens, ViewModels, Emerald & Slate theme, Canvas charts. |

---

## Financial Engine: Under the Hood

### 1. Bisection Goal Solver
Standard algebraic formulas break down when incorporating yearly step-ups or existing saved capital. **`GoalSolver`** uses a binary search (bisection method) over the monotonic future-value function to solve the exact monthly base instalment required for any goal in under 20 iterations.

### 2. Newton-Raphson XIRR
CAGR assumes a single initial investment. Because SIPs consist of multiple cash flows on different dates, **SIP Planner** calculates true yield using a **Newton-Raphson XIRR solver**, falling back to bisection if derivatives approach zero.

---

## Setup & Building

### Prerequisites
- **Android Studio:** Ladybug (2024.2.1) or newer
- **JDK:** Version 17
- **Android SDK:** `compileSdk 37`, `minSdk 26`

```bash
# Clone the repository
git clone https://github.com/abhishukla0204/SIP-Planner.git

# Run unit tests
./gradlew testDebugUnitTest
```

