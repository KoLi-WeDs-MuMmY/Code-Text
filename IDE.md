# KtScribe IDE Features Registry

> [!CRITICAL]
> **MAINTENANCE DIRECTIVE**:
> This file MUST be updated every time a prompt is given of any kind (unless or until it is a trivial non-functional prompt).
> When new IDE features are implemented, move them from the "Upcoming Features Queue" to the "Implemented IDE Features" section, and add new planned features to the queue.

---

## 🛠 Implemented IDE Features

### 1. Code Editor Core & Typography
- **`TextLayoutResult`-Locked Gutter & Code Line Synchronization**: Each gutter number row (`1.`, `2.`, `3.`) dynamically measures its exact vertical bounds (`getLineTop(startVis)` to `getLineBottom(endVis)`) directly from `BasicTextField`'s `TextLayoutResult` alongside identical `13.sp`/`22.sp` `JetBrainsMonoFontFamily` metrics (`PlatformTextStyle(includeFontPadding = false)`, `LineHeightStyle(Alignment.Center, Trim.None)`), eliminating cumulative vertical drift between gutter numbers and actual code lines across all system font scales and wrapped lines.
- **Syntax Highlighting & Lexical Tokenizer**: Dynamic syntax coloring for Kotlin, Python, Java, C++, Dart, and Rust with keyword, string literal, comment, type, and punctuation styling.
- **Contextual Caret-Anchored Autocomplete**: Desktop-IDE floating autocomplete popup positioned to the right of the caret (`cursorRect.right + 28.dp`), keeping the typing area unobstructed with JetBrains-style badges (`{}`, `K`, `PY`, `JAVA`, `C++`, `DART`, `RS`).
- **Smart Indentation Engine**: Automatic 4-space auto-indentation and brace completion (`{}`, `()`, `""`) without duplicate line injections.
- **Find & Replace**: In-editor regex and case-sensitive find-and-replace strip with navigation.
- **Color Literal Palette**: Inline visual color picker for hex/RGB literals in code.

### 2. File Architecture, Safe Language Switching & Export
- **Native File Extensions**: Code blocks are treated as real native code files (`main.kt`, `main.py`, `Main.java`, `main.cpp`, `main.dart`, `main.rs`), allowing the compiler and debugger to identify them accurately.
- **xScribe Container Model**: Every project is structured as an `.xScribe` container bundle containing:
  - Native Code File (e.g. `main.py`, `main.kt`)
  - Explanation File (`explanation.txt`)
  - Project configuration & AST tags
- **File Settings Exclusive Language Switcher with Warning Confirmation**: Removed the single-click language dropdown from the main editor canvas header. Language/extension switching is housed exclusively inside **File Settings (`⚙`)** and requires confirming a warning dialog explaining that existing file contents are preserved as-is and may not compile or run under the new language extension.
- **Extension Rename Protection**: Prevents changing the file extension during inline title rename with an active warning alert modal explaining compiler incompatibility.
- **Android Share/Save Dialog & Clipboard Export**: Export File and Copy File actions copy the payload to the Android clipboard, display an immediate visual Toast and status banner, and open the native Android Share/Save chooser (`Intent.ACTION_SEND`) for saving `.kt`, `.py`, `.java`, `.ktscribe`, or `.md` files to disk or external apps.

### 3. Workspace Layout: Top Row Primary Controls & Left Rail Editor Tools
- **Top Row Primary Bar**: Houses primary workspace views and actions in a single bezel-safe top bar: `Explain`, `Files`, `>_ Terminal` (toggles the sliding bottom Terminal/Debugger/Problems drawer), `Run ▶`, `AI Guide`, `⌘P`, and `Settings`. Removed the tiny bottom-right status bar terminal trigger.
- **Left Circular IDE Tool Rail**: Houses code editing and file tools on the left side: `{ }` Format Code, `!` Quick-Fix / Diagnostics, `🔍` Structure & Find/Replace, `⌘/` Line Actions & Live Templates, `🎨` Color Picker, `✨` AI Studio Actions, `📤` Export / Copy File, `+` New File, and `↶` Undo.

### 4. Multi-Language Debugger & Compiler Engine
- **Multi-Language Syntax Parser**:
  - **Python**: Real indentation validation, colon checks for `def`/`if`/`for`/`while`/`class`, unclosed string literals.
  - **Java**: Class declaration checking, main method signature, semicolon detection, bracket matching.
  - **Kotlin**: Unmatched braces, keyword syntax, variable declaration validation.
  - **C++**: Preprocessor `#include` check, main return, semicolons.
  - **Dart**: `void main()`, type signatures, semicolons.
  - **Rust**: `fn main()`, syntax validation, braces.
- **Interactive Diagnostics**: Inline error pills, squiggle indicators, and quick-fix recommendations.
- **Execution Simulation**: Standard output (`stdout`), standard error (`stderr`), execution timing, and process return codes (`exit code 0/1`).

### 5. Sliding IDE Bottom Drawer
- **Downside Slide-Up Panel**: Docked bottom drawer matching VS Code / IntelliJ desktop layout, triggered directly from the Top Row `>_ Terminal` pill.
- **Multi-Tab Architecture**:
  - `>_ Terminal`: Build output, interactive commands, and process status.
  - `🪲 Debugger`: Variable inspector, call stack, and step execution.
  - `⚠ Problems`: Real-time aggregated errors and warnings with click-to-line jumping.
- **Height Controls**: Compact height, Half height (40%), and Maximized height (72%).

### 6. MIUI / HyperOS Floating Window Presets
- **Discrete Preset Tiers**:
  - **Vertical Preset**: 340dp × 520dp (9:16 portrait ratio, ideal for notes/AI chat).
  - **Horizontal Preset**: 560dp × 360dp (16:9 landscape ratio, ideal for consoles/side-by-side inspection).
  - **Mini Preset**: 240dp × 170dp compact preview.
  - **Full Preset**: 92% × 88% maximized window.
- **Smooth Physics**: Spring animation with aspect-ratio auto-snap on drag release (`spring(dampingRatio = 0.82f, stiffness = 380f)`).
- **Header Switcher**: Direct `⬍ Vertical` / `⬌ Horizontal` toggle button.

---

## 🔮 Upcoming Features Queue (Next to Implement)

1. **Gutter Breakpoint Toggles & Step-Over Debugging**:
   - Clickable gutter line markers (red dot breakpoints) enabling line-by-line pause, Step Over (`F8`), and Step Into (`F7`) in the Debugger tab.
2. **Multi-File Project Tree with Tab Switching**:
   - Ability to add multiple native code files (e.g., `utils.py`, `models.py`) inside the same `.xScribe` project bundle with draggable editor tabs.
3. **Integrated Git Diff & History Bar**:
   - File revision history tracker with visual side-by-side diffing and restore capabilities.
