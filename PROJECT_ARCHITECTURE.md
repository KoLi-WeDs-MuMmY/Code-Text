# Project Architecture & Developer Guide: KtScribe IDE

> **Purpose**: This concise, high-density guide explains the complete architecture, data flow, and file responsibilities of KtScribe. Read this file when starting a new session to understand 100% of the project with minimal token usage.

---

## 1. What is KtScribe?
KtScribe is a desktop-grade mobile IDE and dual-layer code editor built with Jetpack Compose and Kotlin. It combines:
1. **Native Multi-Language Coding**: Real syntax diagnostics, compiler checks, and code snippets for **Kotlin (.kt)**, **Python (.py)**, **Java (.java)**, **C++ (.cpp)**, **Dart (.dart)**, and **Rust (.rs)**.
2. **xScribe Container Bundles**: Each project is an `.xScribe` container (e.g. `.ktscribe`, `.pyscribe`) encapsulating:
   - An actual native code file (`main.kt`, `main.py`, `Main.java`, etc.) for real compilation and debugging.
   - An associated explanation text file (`explanation.txt`).
   - Bundled concept tags and AST metadata.
3. **MIUI / HyperOS Floating Windows**: Smooth spring-animated windows with discrete presets:
   - `PORTRAIT` (340 × 520 dp)
   - `LANDSCAPE` (560 × 360 dp)
   - `MINI` (240 × 170 dp)
   - `FULL` (maximized)
4. **Desktop-IDE Sliding Bottom Drawer**: VS Code / Android Studio style slide-up panel with:
   - `>_ Terminal`: Execution logs, build output, process exit codes.
   - `🪲 Debugger`: Live variable inspector, call stack, execution state.
   - `⚠ Problems`: Real-time aggregated errors/warnings with click-to-line navigation.
   - `IdeBottomStatusBar`: Compact bottom edge bar displaying active file, error counts, and quick-toggle drawer buttons.
5. **Extension Lock Protection**: Prevents accidental language modification on rename, warning the user if syntax/compiler compatibility would break.

---

## 2. Directory Structure & Component Responsibilities

```
app/src/main/java/com/example/
├── model/
│   └── EditorModels.kt           # All domain models, LanguageType, WindowPresetTier, BottomDrawerTab, rename validator
├── viewmodel/
│   └── EditorViewModel.kt        # Central state holder (EditorUiState), file I/O, drawer & floating window actions
├── code/
│   ├── CodeLanguageEngine.kt     # Multi-language tokenizer, snippets, syntax checks & debug simulator
│   └── KotlinCodeEngine.kt       # Deep Kotlin lexer, syntax diagnostics (diagnoseKotlinSyntax), symbols
├── ui/
│   ├── components/
│   │   ├── KotlinCodeSectionCard.kt   # Core code editor, gutter synchronization (22sp), caret-anchored suggestions (+28dp)
│   │   ├── IdeBottomDrawer.kt         # Sliding bottom drawer (Terminal, Debugger, Problems) & IdeBottomStatusBar
│   │   ├── MacFloatingIslandComponents.kt # Floating window host with drag handles, spring physics & MIUI presets
│   │   ├── FileSettingsDialog.kt      # Intra-file settings (theme overrides, export, naming, language selector)
│   │   ├── SketchTopBarWithFourPills.kt # Mac-style top bar with pill actions and status indicators
│   │   └── AnimatedBackground.kt      # Particle/ambient animated background
│   ├── screens/
│   │   ├── HomeScreen.kt              # Project gallery, search, trichrome logo, new folder creation
│   │   └── DocumentEditorScreen.kt    # Main workspace assembling top bar, tab switcher, editor card, drawer & modals
│   └── theme/
│       ├── Theme.kt & Color.kt        # 6 Curated IDE Themes (Darcula, Monokai Pro, GitHub Dark, Nord Frost, etc.)
│       └── Type.kt                    # Typography using JetBrains Mono font
└── MainActivity.kt                    # Application entry point, system bars, permission handling & screen switching
```

---

## 3. Key Source Files Quick-Reference

| File | What It Does | When to Modify |
|---|---|---|
| `model/EditorModels.kt` | Defines `CodeSectionUiModel`, `DocumentDetailUiModel`, `LanguageType` enum, `WindowPresetTier`, `BottomDrawerTab`, `validateFileExtensionRename`, and file serialization. | Adding new languages, extensions, window presets, or bundle formats. |
| `viewmodel/EditorViewModel.kt` | Manages `EditorUiState`, undo/redo, auto-save timers, terminal outputs, bottom drawer visibility, and section CRUD. | Adding new state mutations, actions, or document commands. |
| `code/CodeLanguageEngine.kt` | Multi-language syntax parsing (Python colons, Java semicolons, C++ headers, Dart/Rust brackets), autocomplete dictionary, and `executeAndDebug`. | Adding snippets, syntax rules, or compiler diagnostic logic. |
| `code/KotlinCodeEngine.kt` | Tokenizer and Kotlin-specific syntax rules, bracket counting, and diagnostic generation (`diagnoseKotlinSyntax`). | Improving Kotlin syntax analysis or symbol detection. |
| `ui/components/KotlinCodeSectionCard.kt` | Text editing surface with `BasicTextField`, line numbers gutter, and floating suggestion overlay. | Adjusting font metrics, cursor positions, or gutter alignment. |
| `ui/components/IdeBottomDrawer.kt` | Sliding bottom drawer containing Terminal, Debugger, and Problems tabs, plus `IdeBottomStatusBar`. | Adding debugger features (breakpoints, step execution) or terminal tools. |
| `ui/components/MacFloatingIslandComponents.kt` | Draggable floating windows with snap-to-preset logic and MIUI/iOS dimensions. | Tweaking window geometry, drag constraints, or transitions. |
| `ui/screens/DocumentEditorScreen.kt` | The master workspace composable hosting the top bar, tab switcher, code card, drawer, and alerts. | Changing overall screen layout, adding dialogs, or keyboard shortcuts. |
| `ui/screens/HomeScreen.kt` | Starting dashboard with search, project cards, and background animation. | Updating home page visual styling, project cards, or logo. |
| `IDE.md` | Feature registry tracking implemented and upcoming IDE features. | **Must be updated every time IDE features are added or planned.** |

---

## 4. Critical Invariants (DO NOT BREAK)
1. **Line-Height Synchronization**: In `KotlinCodeSectionCard.kt`, `fontSize = 13.sp`, `lineHeight = 22.sp`, with `PlatformTextStyle(includeFontPadding = false)` and `LineHeightStyle(alignment = Alignment.Center, trim = LineHeightStyle.Trim.None)` on BOTH gutter numbers and `BasicTextField`. Changing one without the other causes cursor drift.
2. **Autocomplete Offset**: Autocomplete popup is offset to `cursorRect.right + 28.dp` so code directly under the cursor is never covered.
3. **Compile Diagnostics Method**: In `CodeLanguageEngine.kt`, Kotlin diagnostics MUST call `KotlinCodeEngine.diagnoseKotlinSyntax(code)` (not `getDiagnostics`).
4. **Badge Property**: In `LanguageType`, the badge label is `.badgeLabel` (e.g. `"KT"`, `"PY"`), not `badgeChar`.
5. **Metadata Capability**: Never remove `MAJOR_CAPABILITY_SERVER_SIDE_GEMINI_API` from root `metadata.json`.

---

## 5. Verification Commands
- **Compiler Check**: `compile_applet`
- **Unit & Robolectric Tests**: `gradle :app:testDebugUnitTest` (via `run_command`)
