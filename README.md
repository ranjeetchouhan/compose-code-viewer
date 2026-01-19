# Compose Viewer

[![GitHub stars](https://img.shields.io/github/stars/ranjeetchouhan/compose-code-viewer?style=social)](https://github.com/ranjeetchouhan/compose-code-viewer/stargazers)
[![GitHub issues](https://img.shields.io/github/issues/ranjeetchouhan/compose-code-viewer)](https://github.com/ranjeetchouhan/compose-code-viewer/issues)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

**Compose Viewer** is a real-time Jetpack Compose Desktop code editor and previewer. Write Kotlin Compose UI code and see the results instantly - perfect for prototyping, learning, and experimenting with Compose Multiplatform.

![Compose Viewer Demo](demo.png)

## ✨ Features

### Core Features
- **Live Preview**: Instant compilation and preview as you type
- **Advanced Editor**:
  - Syntax highlighting for Kotlin/Compose
  - Auto-indentation (maintains indentation on Enter, adds indent after `{`)
  - Bracket matching and code folding
  - Search & Replace (Cmd+F / Ctrl+F)
  - Line numbers
- **Code Samples Library**: 11 ready-to-use examples across 4 categories
  - Basics: Hello World, Button Click
  - Layouts: Card Layout, LazyColumn List
  - Animation: Animated Counter, Kinetic Radar, Quantum Wave Field
  - Graphics: Color Gradient
  - Advanced: Network Image, Progress Indicators, Live Stock Market
- **Code Formatting**: Integrated ktfmt for clean code
- **Cross-Platform**: Native support for macOS, Windows, and Linux
- **Network Images**: Coil integration for `AsyncImage` support
- **Helper Utilities**: Built-in `maxWidth`, `maxHeight` variables

### UI/UX
- Professional dark theme (IntelliJ-inspired)
- Resizable split view
- Version display and update checker
- Platform-specific keyboard shortcuts

## 📦 Installation

### Prerequisites
- JDK 17 or higher

### Windows
```powershell
git clone https://github.com/ranjeetchouhan/compose-code-viewer.git
cd compose-code-viewer
gradlew.bat run
```

### macOS / Linux
```bash
git clone https://github.com/ranjeetchouhan/compose-code-viewer.git
cd compose-code-viewer
./gradlew run
```

## 🚀 Usage

1. **Browse Samples**: Click the 📚 **Samples** button to explore pre-built examples
2. **Write Code**: Type your Compose UI code in the left editor
3. **Run**: Click the ▶️ **Run** button to compile and preview
4. **Format**: Click the **</>** icon to auto-format your code
5. **Reload**: Click 🔄 to refresh the preview
6. **Search**: Press `Cmd+F` (Mac) or `Ctrl+F` (Windows)

### Keyboard Shortcuts
- **Search**: `Cmd+F` (Mac) / `Ctrl+F` (Windows)
- **Next Match**: `Enter`
- **Previous Match**: `Shift+Enter`
- **Navigate Search**: `Arrow Up/Down`

## 🔮 Coming Soon

Features planned for future releases:

- **v1.1.0**:
  - More code samples (Material 3 components, complex animations)
  - Code snippets and templates
  - Export preview as image
  - Dark/Light theme toggle

- **v1.2.0**:
  - Multi-file project support
  - Import external dependencies
  - Code completion and suggestions
  - Debugging tools

- **Future**:
  - Collaborative editing
  - Cloud sync for code snippets
  - Plugin system
  - Mobile preview (Android/iOS)

## 🤝 Contributing

We welcome contributions! Here's how you can help:

- ⭐ **[Star this repository](https://github.com/ranjeetchouhan/compose-code-viewer)** if you find it useful
- 🐛 **[Report issues](https://github.com/ranjeetchouhan/compose-code-viewer/issues/new)** for bugs or feature requests
- 🔧 Submit pull requests to improve the code
- 📖 Help improve documentation
- 💡 Suggest new features or samples

---

⭐ **[Star this repository](https://github.com/ranjeetchouhan/compose-code-viewer)** if you find it useful!  
🐛 **[Report issues](https://github.com/ranjeetchouhan/compose-code-viewer/issues/new)** for bugs or feature requests

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details. You are free to use, modify, and distribute this software.
