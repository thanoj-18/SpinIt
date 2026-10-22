# SpinIt

A lightweight desktop party game written in pure Java 21 (Swing).  
Spin a virtual bottle on a wooden table; whoever it points to answers *Truth* or does a *Dare*.

---

## 🎮 How to Play

| Action | How |
|--------|-----|
| **Add a player** | Type a name in the left panel → press **Enter** or click **Add** |
| **Remove a player** | Select a name in the list → press **Delete** or click **Remove Selected** |
| **Spin** | Press **SPACE** or click the **SPIN** button (enabled when ≥ 2 players) |
| **Result** | The winner’s badge pulses gold with a particle burst; the name & timestamp appear in the “Last 5 Spins” panel on the right |

---

## ✨ Features

* Clean three‑column UI – Players / Table / History
* Realistic bottle physics: 5‑9 full revolutions + smooth quintic ease‑out (≈ 5 s)
* Winner highlight animation (gold glow + starburst particles)
* Keyboard shortcuts – `Space` to spin, `Delete` to remove
* No external dependencies – only the JDK

---

## 🛠️ Tech Stack

| Layer | Technology |
|-------|------------|
| Language | Java 21 |
| UI Toolkit | Swing (standard library) |
| Build | None required – plain `javac` / `java` |
| Packaging | Optional: runnable JAR via `jar` command |

---

## 💻 System Requirements

| Requirement | Minimum |
|-------------|---------|
| Java Runtime | **JDK 21** (or any JRE 21+) |
| OS | Windows / macOS / Linux (any platform with a JVM) |
| RAM | < 50 MB |
| Disk | < 1 MB (source) + a few MB for compiled classes |

---

## 📁 Project Structure

```
SpinIt/
├── .gitignore
├── README.md
└── src/
    ├── GameFrame.java   // Main window, layout, player/history panels
    ├── GamePanel.java   // Custom canvas – table, bottle, badges, animations
    ├── Main.java        // Entry point
    └── SpinEngine.java  // Spin animation & winner calculation
```

All source files use the **default package** (no `package` declaration).

---

## 🚀 Installation & Running

### 1️⃣ Clone / copy the project
```bash
git clone <repo-url>        # or just copy the folder
cd SpinIt
```

### 2️⃣ Compile (once)
```bash
javac -d out src/*.java
```

### 3️⃣ Run
```bash
java -cp out Main
```

#### One‑liners (Java 11+)
```bash
# No separate compile step
java --source 21 src/Main.java

# Even shorter
java src/Main.java
```

The window **“SpinIt”** appears immediately.

---

## 🔮 Future Enhancements

| Idea | Description |
|------|-------------|
| **Sound effects** | Spin‑whoosh, winner chime |
| **Truth / Dare cards** | Draw a random prompt after each spin |
| **Player avatars / colours** | Auto‑assign distinct hues for quicker visual identification |
| **Settings dialog** | Adjust spin duration, revolutions, table theme |
| **Persistence** | Save player list & window size between runs |
| **Unit tests** | Headless tests for `SpinEngine` math |
| **Native installer** | `jpackage` to produce .exe / .dmg / .deb |

---

## 📄 License

MIT – feel free to copy, modify, and share.

---

Enjoy the game! 🎉