# Clash of Clankers

[![Build and Release](https://github.com/UQcsse3200/2025-studio-1/actions/workflows/game_release.yaml/badge.svg)](https://github.com/UQcsse3200/2025-studio-1/actions/workflows/game_release.yaml)
[![Game Unit Tests](https://github.com/UQcsse3200/2025-studio-1/actions/workflows/game_tests.yaml/badge.svg?branch=main)](https://github.com/UQcsse3200/2025-studio-1/actions/workflows/game_tests.yaml)
[![Javadoc Test Build](https://github.com/UQcsse3200/2025-studio-1/actions/workflows/javadoc_build.yaml/badge.svg)](https://github.com/UQcsse3200/2025-studio-1/actions/workflows/javadoc_build.yaml)

## About the Project

**Clash of Clankers** is a role-playing game developed by 42 students in **CSSE3200 Studio 1** at the **University of Queensland (UQ)**. Over the course of the semester, seven teams worked collaboratively to transform a bare-bones custom engine (built on **libGDX**) into a fully featured and engaging RPG, centered on the conflict between humanity and AI.

In a near-future world, a lone operative must battle waves of rogue machines and powerful bosses to ensure humanity’s survival.

## Course Description

Over 8 weeks, our team completed 4 sprints, each focused on building and refining the game:

* **Sprint 1:** HUD, new map, items, weapons, enemies, and first boss
* **Sprint 2:** Shop, tutorial, bigger map, more enemies and bosses, cheat codes, and storyline
* **Sprint 3:** Sound effects, casino, saving/loading, leaderboard, equipment, avatar changes, mini-map, balancing, teleportation, NPCs, armor, perks, and easter eggs
* **Sprint 4:** Visual and gameplay polish, audio integration, final balancing, secrets, and demo build

By the end, we delivered a complete playable experience with progression, combat, exploration, and polish.

---

## Play the game

### Option A: Download a build (recommended)

Go to **GitHub → Releases** on this repository and download the desktop build for your platform. Double-click the executable to play.

### Option B: Run from source

```bash
# Clone and run with the Gradle wrapper
git clone https://github.com/UQcsse3200/2025-studio-1.git
cd 2025-studio-1
./gradlew desktop:run   # Windows: gradlew.bat desktop:run
```

**Prerequisites**

* **Java 21 (LTS)**. We recommend Temurin builds from Adoptium.
* **No IDE required**. Any editor works. The Gradle wrapper will fetch dependencies.

> Building a JAR is not required here since a release build is already provided on GitHub.

---

## How to play (short)

* **Goal**: Complete objectives in the level while managing resources and avoiding hazards. Explore, interact, and progress through tasks set by the scenario.
* **Flow**: Start in the main menu, choose Play, then follow on-screen prompts and quest log. Interact with terminals or objects to advance.

### Default keybinds

| Action         | Key                           |
|----------------|-------------------------------|
| Move           | **A / S / D**                 |
| Jump           | **Space**                     |
| Sneak          | **Left Shift**                |
| Interact       | **E**                         |
| Dash           | **Double-tap A or D**         |
| Primary Action | **Left Mouse** or **F**       |
| Pause/Menu     | **Esc**                       |


## Features
- Wave-based survival with scaling machine enemies and unique bosses
- Unlockable doors, weapons, stats, armor, perks, and upgrades
- Endless survival mode with an optional escape objective
- Multi-room map with teleporters, mini-map, and casino
- Shops, friendly NPCs, cheat codes, and hidden easter eggs

---

## Documentation

- **[Wiki Home](https://github.com/UQcsse3200/2025-studio-1/wiki)**
    - Getting Started
    - Walkthrough
    - Storyline and Quests
    - Design Notes and Architecture

- **[JavaDoc](https://uqcsse3200.github.io/2025-studio-1/)**

- **[SonarCloud](https://sonarcloud.io/project/overview?id=UQcsse3200_2025-studio-1)**

---

## Screenshots

<div style="text-align: center;">
  <img src="assets/wiki/readme/mainmenu.png" alt="Main Menu" width="2056" />
  <img src="assets/wiki/readme/lobby.png" alt="Gameplay" width="2056" />
  <img src="assets/wiki/readme/demo.gif" alt="Gameplay Demo" width="1280" />
</div>

---

## Testing

```bash
./gradlew test
./gradlew jacocoTestReport   # coverage
```

Reports are available in the Gradle build directory and on CI.

---

## License

Released under the **MIT License**. If you reuse the engine or game code, please include attribution to the **Clash of Clankers team** and the **UQ Studio 1 course**.

---

## References
Details of AI usage for this project can be found in the [references/](references/) directory.
