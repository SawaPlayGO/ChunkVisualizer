# ChunkVisualizer

**ChunkVisualizer** is a lightweight and powerful Minecraft plugin that allows players to visualize chunk boundaries in real-time without using the debug menu (`F3 + G`). 

It is a perfect companion for **Towny** or other land-claim systems, as it allows players to see specific chunk edges clearly without cluttering the screen with a global grid.

---

## 🎥 Demonstration

### 1. Dynamic Chunk Tracking
The visualization automatically follows the player when crossing chunk borders, ensuring seamless territory control.

![Chunk Movement](https://i.imgur.com/RJEOXLg.gif)

### 2. Full GUI Customization
Use the `/cv settings` command to open an intuitive interface that allows you to:
* **Toggle** visibility with a single click.
* **Adjust height** of the boundary blocks relative to your current position.
* **Change materials** used for the visualization.
* **Reset** all settings to server defaults.

![GUI Settings](https://i.imgur.com/ffgOyiV.gif)

### 3. Non-Intrusive "Ghost" Blocks
Built using modern packets and **Display Entities**, these blocks have no collision. They **do not interfere with gameplay**: you can mine ores or interact with items directly through the visualization.

![World Interaction](https://i.imgur.com/pZJKeiJ.gif)

---

## 🛠 Commands & Permissions

| Command | Description | Permission |
| :--- | :--- | :--- |
| `/cv` | Main command (Help menu) | `chunkvisualizer.use` |
| `/cv settings` | Open the customization GUI | `chunkvisualizer.use` |
| `/cv reload` | Reload configuration files | `chunkvisualizer.admin` |

---

## ⚙️ Installation
1. Download the latest `.jar` file from the [Releases](https://github.com/your-repo/releases) page.
2. Place it into your server's `plugins` folder.
3. Restart the server or load it using a plugin manager.

---

## 📝 Roadmap
* [ ] Persistent user settings storage (Database support).
* [ ] Color support for boundary block outlines/highlights.

---

## 📄 License
This project is licensed under the MIT License - see the LICENSE file for details.
