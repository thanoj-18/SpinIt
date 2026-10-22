# Project Report – SpinIt

<!-- **Date:** 2026‑10‑22  
**Author:** Thanoj
**Version:** 1.0 (plain‑Java, no build tool) -->

---

## 1. Executive Summary
SpinIt is a tiny desktop party game written in **Java 21** using only the standard Swing library.  
Players enter their names, press **Space** (or click **SPIN**) and a virtual bottle rotates on a wooden table.  
When it stops, the player whose badge the neck points to is highlighted and recorded in a “Last 5 Spins” history panel.

The whole project consists of **four `.java` files** (≈ 600 LOC total) and runs with a single `javac`/`java` command – no Maven, Gradle, or external jars.

---

## 2. Current Status
| Item | State |
|------|-------|
| Core gameplay (add/remove players, spin, winner detection) | ✅ Working |
| UI (three‑column layout, wood table, bottle animation, highlight FX) | ✅ Implemented |
| Keyboard shortcuts (`Space`, `Delete`) | ✅ Implemented |
| Compile / run verified on Windows (PowerShell) | ✅ Verified |
| Unit tests | ❌ Not present |
| Documentation (README, this report) | ✅ Added |

---

## 3. Architecture & Module Overview

```
src/
├── Main.java        – program entry point, launches GameFrame on EDT
├── GameFrame.java   – JFrame, three‑column layout, player & history panels, spin button, key bindings
├── GamePanel.java   – JPanel subclass, custom painting:
│                       • wood‑grain table surface
│                       • centre “SPIN” marker
│                       • player badges around the circle
│                       • rotating bottle (green glass, gold cap)
│                       • winner glow + star‑burst particles
│                       • title rendering
└── SpinEngine.java  – Swing Timer animation (≈5 s, quintic ease‑out)
                        • picks random target player
                        • computes total rotation (5‑9 revs + delta)
                        • calls back with winner name
```

All classes are in the **default package** (no `package` statement) and reference each other directly.

---

## 4. Key Algorithms
* **Spin trajectory** – `totalRotation = fullRevs * 2π + delta`, where `fullRevs ∈ [5,9]` and `delta` is the shortest forward angle to the chosen player.
* **Easing** – quintic ease‑out: `eased = 1 - (1 - progress)^5`.
* **Winner resolution** – final bottle angle → player index by normalising to `[0, 2π)`, shifting by `π/2` (player 0 at top), then `round(angle / 2π * n)`.

---

## 5. Code Quality
* **No external dependencies** – only `java.*` and `javax.swing.*`.
* **Clean compilation** with `javac -Xlint:all` – only harmless `serialVersionUID` / `this‑escape` warnings (standard for Swing components).
* **Minimal dead code** – previous sweep removed unused imports, helper method `interpolateColor`, and anonymous `ActionListener` (replaced by lambda).
* **Single‑responsibility** – UI, animation, and geometry are separated into distinct classes.

---

## 6. Testing
| Type | Status |
|------|--------|
| Manual functional testing (add players, spin, history) | ✅ Passed |
| Automated unit tests | ❌ Not yet |
| Headless CI build | ❌ Not configured |

*Recommended next step*: add a small JUnit 5 test module exercising `SpinEngine.computeTargetAngle`, `normalizeAngle`, and `angleToPlayerIndex`.

---

## 7. Build & Run Instructions (recap)

```bash
# 1. Compile
javac -d out src/*.java

# 2. Run
java -cp out Main
```

One‑liners (Java 11+):
```bash
java --source 21 src/Main.java
# or
java src/Main.java
```

---

## 8. Known Limitations / Technical Debt
1. **Default package** – works for tiny projects but prevents IDE auto‑import and module‑system use.
2. **No persistence** – player list and window geometry are lost on exit.
3. **No sound or haptic feedback**.
4. **Hard‑coded colours / dimensions** – theme changes require source edits.
5. **No automated test suite** – regression risk when refactoring.

---

## 9. Future Enhancements (Prioritised)

| Priority | Feature | Effort |
|----------|---------|--------|
| High | Add JUnit 5 tests for `SpinEngine` math | Low |
| High | Persist player list & window size (JSON / Preferences API) | Low |
| Medium | Sound effects (spin, win) | Low |
| Medium | Truth / Dare card deck drawn after each spin | Medium |
| Medium | Settings dialog (spin duration, revolutions, theme) | Medium |
| Low | Player avatars / auto‑assigned colours | Medium |
| Low | `jpackage` native installers (`.exe`, `.dmg`, `.deb`) | Medium |
| Low | Module declaration (`module-info.java`) + move to named package | Low |

---

## 10. Conclusion
The project meets its functional goals: a fun, zero‑dependency Truth‑or‑Dare spinner with smooth animation and a polished UI.  
The codebase is small, readable, and ready for incremental improvements (tests, persistence, packaging) without architectural rewrites.