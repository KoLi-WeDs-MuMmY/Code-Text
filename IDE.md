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

1. **Interactive Call-Graph & Memory Allocation Flame Chart**:
   - Visual timeline of function entry/exit depth and heap object allocations produced by `KtBytecodeVm`.
2. **Gradle / Kotlin Multiplatform Project Export (.zip)**:
   - Export multi-file `.xScribe` project folders as ready-to-build Gradle Kotlin projects.
3. **Snippet & Live-Template Cloud/File Sharing**:
   - Import/export custom user-defined live templates and syntax themes via `.json` via the Android Photo/File Picker.


---

## 9. CodeAssist + iOS Glass UI Overhaul, Bottom Terminal Drawer & SDK Toolchain Manager

- **Bottom-Anchored Build/Terminal/Debugger/Logcat Drawer**: Fixed `IdeBottomDrawer` alignment in `DocumentEditorScreen.kt` using `Modifier.align(Alignment.BottomCenter)` so the drawer always slides up from the bottom of the screen. Added CodeAssist `Logcat` tab, `All` / `Warnings` / `Errors` filter pills, `Filter log...` search field, and interactive Step Over / Step Into / Call Stack debugging.
- **Decluttered CodeAssist + iOS Glass Workspace**: Consolidated redundant headers into a single CodeAssist Top Bar, Slim Left Activity Rail, Collapsible `Files` Project Tree (`app/src/main/<lang>`), Editor File Tab Bar (`K Main.kt • ×`), Breadcrumb Bar (`app > example > myapp > Main.kt`), and "No file open" Zero-State Screen.
- **Real-Time Caret & Keyboard Auto-Scroll**: Updated `KotlinCodeSectionCard.kt` so `bringActiveCodeLineIntoView` runs on every character typed, cursor change, and soft-keyboard (`IME`) height change using `TextLayoutResult.getCursorRect()`.
- **SDK & Plugin Toolchain Manager + iOS Grouped Settings**: Added downloadable language SDKs & debuggers (Kotlin, Java, Python, C++, Dart, Rust) and IDE extensions in `ShortcutAndSettingsDialog.kt` and the workspace `Plugins` panel, and restructured both Main Studio Settings and File Settings into categorized iOS-style grouped cards.


---

## 10. CodeAssist Deep IDE & Debugger Integration + Rounded UI Redesign

- **Interactive Gutter Breakpoints & Expression Watch Debugger (`KotlinCodeSectionCard.kt`, `IdeBottomDrawer.kt`, `CodeLanguageEngine.kt`)**:
  - Clickable line-number gutter (`gutter_line_${orderIndex}_$lineNum`) toggles red **Breakpoint dots (`●`)** with crimson active-line tinting while strictly preserving 1:1 `TextLayoutResult` vertical synchronization.
  - Debugger drawer supports **Resume to Next Breakpoint**, **Step Over (`F8`)**, **Step Into (`F7`)**, **Reset**, active breakpoint pill chips, and an **Interactive Expression Watch Evaluator (`+ Watch`)** that evaluates arithmetic, boolean comparisons, and string/collection properties (`.length`, `.size`, `.uppercase()`, `.lowercase()`) against live stack variables.
- **Auto-Import Quick Fixes & Optimize Imports (`CodeLanguageEngine.kt`, `KotlinCodeSectionCard.kt`, `IdeBottomDrawer.kt`)**:
  - Detects unresolved standard library & Android/JDK/Python/C++/Dart/Rust symbols (`UUID`, `LocalDate`, `File`, `AtomicInteger`, `Dispatchers`, `Flow`, `Json`, `ArrayList`, `HashMap`, `Scanner`, `Math`, etc.) and surfaces 1-tap **`+ Import <package>`** pills inline on the error line and in the `Problems` drawer.
  - **Optimize Imports (`optimize_imports_button_${orderIndex}`)** deduplicates, alphabetically sorts, and removes unused `import`, `#include`, and `use` statements.
- **Code Folding & Go-to-Definition Symbol Navigation (`KotlinCodeSectionCard.kt`, `DocumentEditorScreen.kt`)**:
  - Detects multi-line classes, functions, control blocks, and import blocks across all 6 languages with gutter fold chevrons (`▾`/`▸`) and expandable folded-region pills.
  - Indexes all project symbols (`extractSymbolsForSection` & `findDefinitionAtCursor`) for 1-tap **`⌘B Go to Def`** jumping across files and inside the `Structure` side panel.
- **CodeAssist Rounded Project Home & Spring-Physics Navigation (`DocumentListScreen.kt`, `MainActivity.kt`)**:
  - Redesigned Project Home with warm Darcula-Trench pill-rounded cards (`22.dp`), live SDK Toolchain readiness bar (`X/6 SDKs Ready`), 1-tap Quick Start Language Templates, a New Project Template Wizard modal, and spring-physics screen transitions (`AnimatedContent`).

---

## 11. Zero-Server On-Device AST Engine, Interactive `stdin` Pipe, Line Jumping & Local Snapshots

- **100% On-Device Zero-Server AST Engine (`AiSectionNamingService.kt`)**:
  - Enabled instant local AST execution (`preferOfflineLocalEngine = true`, `<5ms` latency, `$0` server cost) for Smart File Naming, 3-Part Deep-Dive Explanations, Idiomatic Refactor & Auto-Fix, and Documentation Generation across all 6 languages (**PEP-257 `"""` docstrings** for Python, **`///` Rustdoc/Dartdoc** for Rust/Dart, and **`/** ... */` KDoc/Javadoc/Doxygen** for Kotlin/Java/C++).
- **End-to-End Line Jumping Across All IDE Panels (`DocumentEditorScreen.kt`, `KotlinCodeSectionCard.kt`, `EditorViewModel.kt`)**:
  - Fixed the previously unwired `onJumpToLine` callback in `IdeBottomDrawer`. Clicking a diagnostic in `Problems`, stepping through `Step Over`/`Step Into`/`Resume` in `Debugger`, clicking a symbol in `Structure` or `⌘B Go to Def`, or clicking a search match in `Find in Files` now moves the editor caret to the exact 1-based line, unfolds any collapsed region containing that line, focuses the editor, and smoothly scrolls the line into view.
- **Full Multi-Language Formatter, Comment Toggle & Live Templates (`CodeLanguageEngine.kt`, `KotlinCodeSectionCard.kt`)**:
  - Added `formatCodeForLanguage` (Python 4-space colon-block indentation, Dart 2-space brace indentation, and Java/C++/Rust/Kotlin 4-space brace indentation).
  - Added `toggleLineCommentForLanguage` (`# ` for Python, `// ` for C-style languages) and `getLiveTemplatesForLanguage` with native snippets for all 6 languages.
  - Fixed Markdown export (`exportActiveDocumentAsMarkdown`) to use the section's real language fence (```` ```${sec.language.id} ````) and Floating/Split `RUN_OUTPUT` windows to execute with the active language engine instead of hardcoded Kotlin.
- **Interactive Terminal `stdin` Input Pipe & Variable Interpolation (`IdeBottomDrawer.kt`, `CodeLanguageEngine.kt`)**:
  - Added a live **`stdin >` Console Input Bar** inside the Terminal drawer that feeds user input into `readln()`, `Scanner`, `input()`, `std::cin >>`, and `stdin.readLineSync()` with full variable interpolation across `println`, `print`, `System.out.println`, and `std::cout`.
- **On-Device Local Version History & 1-Tap Rollback (`KotlinCodeSectionCard.kt`, `EditorViewModel.kt`)**:
  - Added a **`⏱ History` pill** in the editor breadcrumb bar and a collapsible **Local Snapshots** drawer for manual checkpoints (`+ Save Snapshot`), automatic pre-refactor/pre-docstring checkpoints, and 1-tap rollback.

---

## 12. Real On-Device AST Statement & Expression Interpreter + Termux Bridge

- **Real On-Device Multi-Language Interpreter (`OnDeviceInterpreter.kt`)**:
  - Replaced regex-only output simulation with a real statement & expression AST interpreter supporting Kotlin, Java, Python, C++, Dart, and Rust.
  - Evaluates arithmetic/boolean/string expressions, `if`/`elif`/`else` branches, `for` ranges/iterables/C-style `for (init; cond; upd)`, `while` loops (with step safety guard), `break`/`continue`, user-defined functions with recursion & `return`, lists/indexing, string templates, and interactive `stdin` consumption while recording live variables and call stack frames.
- **Honest Built-In Engine Toggles (`EditorModels.kt`, `EditorViewModel.kt`, `ShortcutAndSettingsDialog.kt`)**:
  - Removed the fake 360ms download simulation and misleading desktop binary sizes. All 6 language engines (`On-Device`) and 4 IDE extensions (`Built-in`) are active out of the box and toggle instantaneously.
  - Added an explicit technical notice in Settings explaining Android SELinux/`W^X` and Google Play Dynamic Code Loading restrictions.
- **1-Tap `Termux Cmd` Native Compiler Bridge (`CodeLanguageEngine.kt`, `IdeBottomDrawer.kt`)**:
  - Added a `Termux Cmd` pill in the Terminal drawer header that copies a ready-to-paste shell heredoc (`cat << 'KTSCRIBE_EOF' > ...` + `kotlinc`/`javac`/`python3`/`g++`/`dart`/`rustc`) for users who want bare-metal Linux compilation in Termux without Play-restricted permissions.

---

## 13. Built-In Kotlin Compiler Plugin, KtBytecodeVm, M3 Tonal UI & Adaptive Screen-Ratio Themes

- **Built-In On-Device Kotlin Compiler Plugin & Stack Bytecode VM (`compiler/KotlinCompilerPlugin.kt`, `compiler/KtBytecodeVm.kt`, `compiler/CompilerModels.kt`)**:
  - `sdk_kotlin` (`Kotlin Compiler Plugin`) is bundled directly in the app source (`isInstalled = true`, permanently active, zero download or server calls).
  - Real 4-stage pipeline: Lexer (`Token`) → Recursive-Descent Parser (`ProgramAst`) → Semantic Analyzer (`SymbolEntry`, diagnostics) → Stack Bytecode Emitter (`BytecodeInstruction`, constant pool, function/class metadata).
  - `KtBytecodeVm.DebugSession` executes compiled artifacts on-device with breakpoint pause (`runUntilBreakpoint`), `stepOver()`, trace scrubbing (`resetTrace()`), call frame snapshots, operand stack preview, and heap object tracking.
- **Material 3 Tonal Dark UI & Elevation Overhaul (`Theme.kt`, `AppThemeId.kt`, `DocumentListScreen.kt`, `DocumentEditorScreen.kt`, `IdeBottomDrawer.kt`)**:
  - Migrated workspace surfaces to Material 3 tonal hierarchy (`surfaceContainerLowest`..`surfaceContainerHighest`, `outlineVariant`), elevation shadows, spring transitions, and single-line (`softWrap = false`) status/action pills across all 6 `AppThemeId` palettes.
- **Dedicated Screen-Size Theme Files & Zero-Permission Ratio Adaptation (`ScreenProfile.kt`, `CompactPortraitTheme.kt`, `CompactLandscapeTheme.kt`, `MediumFoldableTheme.kt`, `ExpandedTabletTheme.kt`)**:
  - `ScreenRatioClassifier` reads `WindowMetrics`/`DisplayMetrics` + `BoxWithConstraints` with **zero extra Android permissions** and dispatches tier-specific theme files (`CompactPortraitTheme.kt`, `CompactLandscapeTheme.kt`, `MediumFoldableTheme.kt`, `ExpandedTabletTheme.kt`).
  - Narrow portrait phones use a compact single-row top bar + `⋮` overflow menu + `ModalBottomSheet` side tools; landscape phones, foldables, and tablets use M3 `NavigationRail`, multi-column project grids, and inline side panels.
  - Fixed top bar pill wrapping (`✓ Indexed`, `Settings` clipping), breadcrumb vs `Ln/Col` overlap in `KotlinCodeSectionCard`, and floating window `Full` label wrapping & viewport-clamped dragging in `MacFloatingIslandComponents`.
- **4 Newly Shipped IDE Features**:
  - **Bracket Pair Colorizer & Fuzzy Command Palette (`BracketPairColorizer.kt`, `DocumentEditorScreen.kt`)**: Depth-aware rainbow coloring for `()`, `{}`, `[]` (skipping strings/comments) with cursor-adjacent pair highlights & unmatched red errors; `⌘P` Command Palette with subsequence fuzzy scoring and matched-character highlights.
  - **Live Kotlin REPL Console (`IdeBottomDrawer.kt`, `KtBytecodeVm.ReplSession`)**: Persistent interactive Kotlin REPL drawer tab (`λ REPL`) retaining variables, functions, and data classes across evaluations with inferred return types and compiled op counts.
  - **AST & Bytecode Inspector (`IdeBottomDrawer.kt`)**: Inspects compiled `KtVM` instructions (`pc`, `OpCode`, operand, line), constant pool, `ProgramAst` summary, and typed symbol table with live `activePc` highlighting and click-to-jump-line navigation.
  - **Multi-File Reorderable Tabs & Visual Git Diff History (`DocumentEditorScreen.kt`, `IdeBottomDrawer.kt`, `DiffEngine.kt`)**: Tab bar supports `◀`/`▶` reordering and `+` language quick-picker; `± Git Diff` tab runs LCS line diffing (`DiffEngine.computeLineDiff`) between `LocalCodeSnapshot` revisions and the active editor buffer with 1-tap snapshot creation and restore.

