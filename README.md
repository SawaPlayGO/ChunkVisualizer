# ChunkVisualizer

**ChunkVisualizer** is a lightweight and powerful Minecraft plugin that allows players to visualize chunk boundaries in real-time without using the debug menu (`F3 + G`).

It is a perfect companion for **Towny** or other land-claim systems, as it allows players to see specific chunk edges clearly without cluttering the screen with a global grid.

---

## 🎥 Demonstration

### 1. Dynamic Chunk Tracking
The visualization automatically follows the player when crossing chunk borders, ensuring seamless territory control. It works with both highlight modes (corner blocks and walls) and is refreshed after teleports and respawns.

<video src="https://github.com/user-attachments/assets/cb20d246-9a1f-4386-9fc9-e4cfc821f9fe" autoplay loop muted playsinline width="100%"></video>
<video src="https://github.com/user-attachments/assets/fec8db0d-280a-4284-b6c4-a06e495c7048" autoplay loop muted playsinline width="100%"></video>

### 2. Full GUI Customization
Use the `/cv settings` command to open an intuitive interface that allows you to:
* **Toggle** visibility with a single click (`Shift + Click` in Blocks mode).
* **Switch highlight mode** between corner blocks and chunk walls.
* **Adjust height** of the boundary blocks relative to your current position.
* **Change materials** used for the visualization: click any block in your inventory while the menu is open.
* **Change outline color** of the corner blocks (red, orange, yellow, green, cyan, blue, purple or white).
* **Customize walls** (requires `chunkvisualizer.use.display`):
    * pick the wall color,
    * adjust opacity from 5% to 100%,
    * toggle the glow effect.
* **Reset the block material** to the server default with a right click on the material slot.

All settings are saved per player and restored on the next join.

<video src="https://github.com/user-attachments/assets/61dedf24-954d-4cf6-b2c6-2c204fe4b0c6" autoplay loop muted playsinline width="100%"></video>
<video src="https://github.com/user-attachments/assets/8879c05a-d4b9-49ea-a828-76fc4d1768e4" autoplay loop muted playsinline width="100%"></video>

### 3. Non-Intrusive "Ghost" Blocks
Built using modern packets and **Display Entities**, these blocks have no collision. They **do not interfere with gameplay**: you can mine ores or interact with items directly through the visualization. Everything is client-side and visible only to the player who enabled it.

<video src="https://github.com/user-attachments/assets/aaa02b17-89bb-414d-a703-321bd537e738" autoplay loop muted playsinline width="100%"></video>
<video src="https://github.com/user-attachments/assets/223e76b4-c3f6-4500-8e3a-e50621f1ce37" autoplay loop muted playsinline width="100%"></video>

### 4. Two Highlight Modes
* **Corner blocks**: ghost blocks at the four chunk corners with a colored outline. Height, material and outline color are configurable.
* **Walls**: semi-transparent colored walls along the chunk edges (`text_display`), with adjustable opacity and an optional glow effect. Requires the `chunkvisualizer.use.display` permission.

---

## 🛠 Commands & Permissions

| Command | Description | Permission |
| :--- | :--- | :--- |
| `/cv` | Main command (help) | — |
| `/cv settings` | Open the customization GUI | `chunkvisualizer.settings` |
| — | Use the **Walls** highlight mode in the GUI | `chunkvisualizer.use.display` |
| `/cv reload` | Reload `config.yml` and `messages.yml` | `chunkvisualizer.admin` |

> Grant `chunkvisualizer.settings` to your players (e.g. via LuckPerms) so they can open the menu. Without `chunkvisualizer.use.display`, players are always shown the corner blocks mode.

---

## 🔧 Configuration

Defaults for new players are set in `plugins/ChunkVisualizer/config.yml`:

| Option | Description |
| :--- | :--- |
| `default-enabled` | Visualization on/off on first join |
| `default-mode` | `BLOCKS` or `WALLS` |
| `default-height` | Height of the corner blocks relative to the player |
| `default-material` | Material of the corner blocks (any valid Bukkit `Material`) |
| `default-block-glow-color` | Outline color of the corner blocks |
| `default-wall-color` | Wall color (`RED`, `ORANGE`, `YELLOW`, `GREEN`, `CYAN`, `BLUE`, `PURPLE`, `WHITE`) |
| `default-wall-alpha` | Wall opacity in percent (5-100) |
| `default-wall-glow` | Wall glow on/off |

Changes to these defaults affect only players who join for the first time. Existing players keep their own saved settings.

All texts live in `messages.yml`. Use `/cv reload` to apply config and message changes.

Player settings are stored in SQLite (`plugins/ChunkVisualizer/data.db`) and are tied to the player name.

---

## 📋 Requirements
* Paper 1.21+
* Java 21
* No external dependencies: PacketEvents is bundled inside the jar.

---

## ⚙️ Installation
1. Download the latest `.jar` file from the [Releases](https://github.com/SawaPlayGO/ChunkVisualizer/releases) page.
2. Place it into your server's `plugins` folder.
3. Restart the server or load it using a plugin manager.
4. Give your players the `chunkvisualizer.settings` permission.