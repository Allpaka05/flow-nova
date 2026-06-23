# FlowNova

FlowNova is a desktop application designed for efficient creation and management of flowcharts. It provides a flexible canvas for designing algorithms, system processes, and logical structures with customizable styling and export capabilities.

## Key Features

* **Visual Diagramming:** Intuitive canvas interface for drawing and arranging flowchart nodes.
* **Drag-and-Drop Editor:** Seamless workflow creation using a drag-and-drop mechanism with real-time node previews.
* **Customizable Styling:** Full control over visual properties, including node border colors, fill colors, text fonts, and line styles.
* **Theme Support:** Built-in support for different UI themes to improve accessibility and user experience.
* **Node Management:** Comprehensive system for adding, managing, and connecting nodes to form complex logical chains.
* **Cross-Platform:** Built with Java Swing, ensuring compatibility across desktop environments.

## Usage

* Creating Nodes: Select a component from the toolbar and drag it onto the canvas.
* Customizing Styles: Use the sidebar property panel to adjust border thickness, colors, and font settings for selected or default nodes.
* Connecting Nodes: Click and drag between nodes to create directional flow lines.

## Getting Started

### Prerequisites

* Java Runtime Environment (JRE) 17 or higher.

### Installation

1. Download the latest executable (JAR, EXE, or AppImage) from the [Releases](https://github.com/Allpaka05/flow-nova/releases) page.
2. For the JAR version, run the following command in your terminal:
   ```bash
   java -jar FlowNova.jar
   ```
3. Alternatively, use the provided EXE file on Windows or the AppImage on Linux for a more convenient desktop launch.

## Build on Linux

FlowNova can be built on Linux in two ways:

- as a regular JAR file for running with Java,
- as an AppImage for a more convenient desktop launch.

### Requirements

Before building, make sure you have:

- Linux x86_64;
- Java 17 or newer;
- `bash`, `wget`, `tar`, `rsync`.

### Build the JAR

To build the JAR version, run:

```bash
./package-linux.sh
```

This creates `FlowNova.jar`.

You can start it from the terminal with:

```bash
java -jar FlowNova.jar
```

### Build the AppImage

To build the AppImage version, run:

```bash
./package-AppImage.sh
```

This creates `FlowNova-x86_64.AppImage`.

You can start it in two ways:

- from the terminal:
  ```bash
  chmod +x FlowNova-x86_64.AppImage
  ./FlowNova-x86_64.AppImage
  ```
- from a file manager:
  - open the file manager;
  - find `FlowNova-x86_64.AppImage`;
  - double-click it;
  - if the system asks for permission, allow execution.

### Which one to use

- Use the **JAR** if you already have Java installed and want to run the app manually.
- Use the **AppImage** if you want a portable desktop version that runs like a normal Linux app.

## Built With

* **Java Swing** — core framework for the graphical user interface.
* **Launch4j** — used for creating the Windows executable wrapper.
* **AppImageTool** — used to package FlowNova as a portable Linux desktop application.
* **Eclipse Temurin 17** — bundled in the AppImage as the Java runtime for Linux.

## License

This project is licensed under the GNU General Public License v3.0 (GPL-3.0). This is a copyleft license that requires any derivative work to be distributed under the same license terms and to make the source code publicly available. See the [LICENSE](LICENSE) file for the full text.
