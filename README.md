# 🏰 Mystic Maze

> A Java dungeon exploration game with procedurally generated worlds, shortest-path navigation, NPC dialogue, coin collection, and full save/load support. Project Completed with Leo Du and Nitin Rao.

---

## ✨ Features

### 🌍 Procedural World Generation
- Generates unique worlds from a numeric seed.
- Randomized rectangular rooms with varying sizes.
- L-shaped hallways connecting all rooms.
- Guaranteed full connectivity between all floor tiles.
- Deterministic output: the same seed always produces the same world.

### 🕹️ Avatar Movement
- Keyboard movement using `W`, `A`, `S`, and `D`.
- Mouse click pathfinding using Breadth-First Search (BFS).
- Animated shortest-path movement to any reachable tile.

### 🧙 Interactive NPCs
- Deterministic NPC placement based on the world seed.
- Multi-stage dialogue system with player response choices.
- Timed dialogue rendering and hidden easter eggs.

### 🪙 Coin Collection
- Coins spawn deterministically in random floor locations.
- HUD displays progress (`Coins Collected / Total Coins`).
- Collecting all coins returns the player to the main menu.

### 💾 Save and Load
- Save progress with `:Q`.
- Load the most recent save from the main menu.
- Restores:
  - Seed
  - Avatar position
  - Movement history
  - Coin progress
  - NPC state

### 🖥️ Heads-Up Display (HUD)
- Displays the tile description under the mouse cursor.
- Shows current coin progress.

---

## 📁 Project Structure

```text
proj5/
└── src/
    └── core/
        ├── Main.java
        ├── Game.java
        ├── GameState.java
        ├── World.java
        ├── Room.java
        ├── Position.java
        ├── Pathfinder.java
        ├── SaveManager.java
        └── FeatureState.java
