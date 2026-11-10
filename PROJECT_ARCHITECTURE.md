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
3. **Top Row Primary Workspace & Left Rail Editor Tools**:
   - **Top Row (`SketchTopBarWithFourPills`)**: `Explain`, `Files`, `>_ Terminal` (opens the bottom Terminal/Debugger/Problems drawer), `Run ▶`, `AI Guide`, `⌘P`, and `Settings`.
   - **Left Rail (`SketchLeftCircularIdeRail`)**: `{ }` Format, `!` Diagnostics/Quick-Fix, `🔍` Structure/Find, `⌘/` Line Actions, `🎨` Color Palette, `✨` AI Tools, `📤` Export/Share, `+` New File, and `↶` Undo.
4. **MIUI / HyperOS Floating Windows**: Smooth spring-animated windows with discrete presets (`PORTRAIT` 340×520dp, `LANDSCAPE` 560×360dp, `MINI` 240×170dp, `FULL`).
5. **Desktop-IDE Sliding Bottom Drawer**: Slide-up panel (`>_ Terminal`, `🪲 Debugger`, `⚠ Problems`) toggled from the Top Row `>_ Terminal` pill.
6. **Guarded Language Switching & Extension Lock**: Language/extension changes are exclusively performed inside `FileSettingsDialog` with a mandatory warning confirmation dialog that preserves existing code as-is while updating the file extension.

---

## 2. Directory Structure & Component Responsibilities

```
app/src/main/java/com/example/
├── model/
│   └── EditorModels.kt           # All domain models, LanguageType, WindowPresetTier, BottomDrawerTab, rename validator
├── viewmodel/
│   └── EditorViewModel.kt        # Central state holder (EditorUiState), file I/O, showStatusToast, updateSectionLanguage
├── code/
│   ├── CodeLanguageEngine.kt     # Multi-language tokenizer, snippets, syntax checks & debug simulator
│   └── KotlinCodeEngine.kt       # Deep Kotlin lexer, syntax diagnostics (diagnoseKotlinSyntax), symbols
├── ui/
│   ├── components/
│   │   ├── KotlinCodeSectionCard.kt   # Core code editor, TextLayoutResult-locked gutter synchronization, caret-anchored suggestions (+28dp)
│   │   ├── IdeBottomDrawer.kt         # Sliding bottom drawer (Terminal, Debugger, Problems)
│   │   ├── MacFloatingIslandComponents.kt # Top bar pills, Left circular IDE tool rail, and MIUI floating windows
│   │   ├── FileSettingsDialog.kt      # File settings (confirmation-guarded language switcher, Android Share/Copy export, theme overrides)
│   │   └── AnimatedBackground.kt      # Particle/ambient animated background
│   ├── screens/
│   │   ├── DocumentListScreen.kt      # Project gallery, search, trichrome logo, new folder creation
│   │   └── DocumentEditorScreen.kt    # Main workspace assembling top bar, left rail, editor card, bottom drawer & modals
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
| `viewmodel/EditorViewModel.kt` | Manages `EditorUiState`, undo/redo, auto-save timers, terminal outputs, bottom drawer visibility, `updateSectionLanguage` (keeps code as-is), and `showStatusToast`. | Adding new state mutations, actions, or document commands. |
| `code/CodeLanguageEngine.kt` | Multi-language syntax parsing (Python colons, Java semicolons, C++ headers, Dart/Rust brackets), autocomplete dictionary, and `executeAndDebug`. | Adding snippets, syntax rules, or compiler diagnostic logic. |
| `code/KotlinCodeEngine.kt` | Tokenizer and Kotlin-specific syntax rules, bracket counting, and diagnostic generation (`diagnoseKotlinSyntax`). | Improving Kotlin syntax analysis or symbol detection. |
| `ui/components/KotlinCodeSectionCard.kt` | Text editing surface with `BasicTextField`, `TextLayoutResult`-synchronized line numbers gutter, read-only language chip (opens File Settings), and floating suggestion overlay. | Adjusting font metrics, cursor positions, or gutter alignment. |
| `ui/components/FileSettingsDialog.kt` | File Settings modal with confirmation-guarded language switching (`pendingLanguageChange`) and `exportOrCopyCodePayload` (Android Share chooser + Clipboard + Toast). | Updating per-file settings, export formats, or language switch warnings. |
| `ui/components/IdeBottomDrawer.kt` | Sliding bottom drawer containing Terminal, Debugger, and Problems tabs. | Adding debugger features (breakpoints, step execution) or terminal tools. |
| `ui/components/MacFloatingIslandComponents.kt` | Top Bar primary pills (`SketchTopBarWithFourPills`), Left Circular IDE Rail (`SketchLeftCircularIdeRail`), and draggable floating windows. | Tweaking top bar pills, left rail tools, or floating window geometry. |
| `ui/screens/DocumentEditorScreen.kt` | The master workspace composable hosting the top bar, tab switcher, left rail, code card, drawer, and alerts. | Changing overall screen layout, adding dialogs, or keyboard shortcuts. |
| `IDE.md` | Feature registry tracking implemented and upcoming IDE features. | **Must be updated every time IDE features are added or planned.** |

---

## 4. Critical Invariants (DO NOT BREAK)
1. **Gutter-to-`TextLayoutResult` Synchronization**: In `KotlinCodeSectionCard.kt`, each gutter row height (`rowHeightDp`) is derived directly from `codeLayoutResult.getLineTop(startVis)` and `codeLayoutResult.getLineBottom(endVis)`, and both gutter `Text` and `BasicTextField` share `fontSize = 13.sp`, `lineHeight = 22.sp`, `PlatformTextStyle(includeFontPadding = false)`, and `LineHeightStyle(alignment = Alignment.Center, trim = LineHeightStyle.Trim.None)`.
2. **Guarded Language Switching**: Do not place a single-click language dropdown on the main editor canvas. Language changes must go through `FileSettingsDialog` with the confirmation `AlertDialog`, and `EditorViewModel.updateSectionLanguage` must preserve existing `codeContent` as-is.
3. **Autocomplete Offset**: Autocomplete popup is offset to `cursorRect.right + 28.dp` so code directly under the cursor is never covered.
4. **Compile Diagnostics Method**: In `CodeLanguageEngine.kt`, Kotlin diagnostics MUST call `KotlinCodeEngine.diagnoseKotlinSyntax(code)`.
5. **Metadata Capability**: Never remove `MAJOR_CAPABILITY_SERVER_SIDE_GEMINI_API` from root `metadata.json`.

---

## 5. Verification Commands
- **Compiler Check**: `compile_applet`
- **Unit & Robolectric Tests**: `gradle :app:testDebugUnitTest` (via `run_command`)
