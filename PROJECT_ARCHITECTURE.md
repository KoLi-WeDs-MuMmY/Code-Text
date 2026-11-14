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


---

## 9. CodeAssist + iOS Glass Workspace Overhaul & SDK Toolchain Manager

### 9.1 CodeAssist + iOS Glass Workspace Architecture (`DocumentEditorScreen.kt`)
- **Single Bezel-Safe CodeAssist Top Bar**: Consolidates project navigation (`←`), collapsible `Files` sidebar toggle (`◧`), folder badge & title, `✓ Indexed` / `● Sync` vault status pill, Save (`💾`), Undo/Redo, primary `▶ Run` pill, `>_ Terminal` bottom drawer toggle, `Explain`, `Files`, `AI Guide`, `⌘P`, and `Settings`.
- **Slim Left Activity Bar + Collapsible Side Panel**:
  - `FILES`: Hierarchical project explorer (`▾ 📁 app` → `▾ 📁 app/src/main/<lang>` → package node → language files `K Main.kt`, `module.toml`, and quick `+ext` language file creators).
  - `SEARCH`: Live search across all files, code, and explanations in the active folder.
  - `STRUCTURE`: Real-time AST symbol & variable outline (`entryPointSignature` + line-mapped variables).
  - `EXPLAIN`: Inline rich-text explanation & concept tag editor.
  - `PLUGINS`: Inline SDK & Plugin Toolchain Manager for one-tap compiler/debugger installation.
- **CodeAssist Editor File Tab Bar (`K Main.kt • ×`)**: Pill-shaped file tabs with circular language badge, modified indicator dot, individual tab close button (`×`), and a **"No file open" Zero-State Screen** (`Double Shift`, `Ctrl + Shift + N`, `Ctrl + E`, `Ctrl + Shift + F`, `Ctrl + N`, `Alt + 1`) when all tabs are closed.

### 9.2 Bottom-Anchored IDE Drawer (`IdeBottomDrawer.kt`)
- Explicitly anchored to the bottom of the workspace (`Modifier.align(Alignment.BottomCenter)`) so it always slides up from the bottom.
- Includes **4 CodeAssist tabs**: `Build & Run (>_ )`, `Problems`, `Debugger`, and `Logcat` with `All` / `Warnings` / `Errors` filter pills and a `Filter log...` search box.
- **Interactive Step-Through Debugger**: Provides `Step Over`, `Step Into`, `Reset`, Call Stack frame selection, and watched local variable inspection.

### 9.3 Real-Time Caret Auto-Scroll & Parameter Hints (`KotlinCodeSectionCard.kt`)
- Calls `codeLayoutResult.getCursorRect(cursorIdx)` + `codeBringIntoViewRequester.bringIntoView(paddedRect)` on **every** character typed, cursor movement, and soft-keyboard (`IME`) viewport resize (`onSizeChanged`) so the active line is never hidden behind the keyboard.
- Displays inline CodeAssist diagnostic pills (`⊗ Unresolved reference...`), floating parameter signature hints (`< 1/20 > println(message: Any?)`), and side quick-documentation cards next to the autocomplete popup.

### 9.4 SDK & Plugin Toolchain Manager & iOS Grouped Settings (`ShortcutAndSettingsDialog.kt` & `FileSettingsDialog.kt`)
- Both Main Studio Settings and File Inspector are organized into **iOS-style grouped inset cards** with a top segmented category filter bar (`All`, `SDK & Plugins`, `Appearance`, `Editor & Caret`, `Shortcuts`, `Vault & AI`).
- Users can download, install, and manage 6 Language SDKs & Debuggers (Kotlin, Java, Python, C/C++, Dart, Rust) and 4 IDE Extensions with persisted state in `SharedPreferences`.


---

## 10. CodeAssist Deep IDE & Debugger Integration + Rounded Project Manager UI

### 10.1 Gutter Breakpoints & Expression Watch Debugger (`KotlinCodeSectionCard.kt` & `IdeBottomDrawer.kt`)
- Each gutter row in `KotlinCodeSectionCard.kt` (`gutter_line_${orderIndex}_$lineNum`) is clickable to toggle a line breakpoint (`EditorViewModel.toggleLineBreakpoint`), rendering a red dot (`●`) and crimson row highlight without altering `rowHeightDp` synchronization.
- `IdeBottomDrawer.kt` (`DebuggerTabContent`) integrates breakpoint pause/resume (`Resume` jumps directly to the next active breakpoint line), active breakpoint management chips, and an interactive **Expression Watch Evaluator** (`CodeLanguageEngine.evaluateWatchExpression`) with live preview (`= result`) and custom watch pills.

### 10.2 Auto-Import Quick Fixes, Optimize Imports, Code Folding & Go-to-Definition (`CodeLanguageEngine.kt`)
- `getMissingImportsForLanguage` & `applyAutoImport`: Automatically detects unimported symbols across Kotlin, Java, Python, C++, Dart, and Rust and attaches 1-tap `+ Import <symbol>` quick fixes to both inline error pills and the `Problems` tab.
- `optimizeImportsForLanguage`: Deduplicates, sorts, and prunes unused imports in the active file (`optimize_imports_button_${orderIndex}`).
- `detectFoldableRegions`: Identifies foldable functions, classes, and import blocks with gutter `▾`/`▸` chevrons and a folded-regions summary bar.
- `extractSymbolsForSection` & `findDefinitionAtCursor`: Powers `⌘B Go to Def` (`goto_definition_button_${orderIndex}`) and the project-wide symbol outline in `LeftActivityTab.STRUCTURE`.

### 10.3 CodeAssist Rounded Project Home (`DocumentListScreen.kt` & `MainActivity.kt`)
- Features a Live SDK Toolchain Status Bar (`X/6 SDKs Ready`), 1-tap Quick Start Project Template pills (`+ Kotlin`, `+ Java`, `+ Python`, `+ C++`, `+ Dart`, `+ Rust`), a New Project Template Wizard modal (`open_template_wizard_button`), pill-rounded search/filter bars, and spring-animated screen transitions in `MainActivity.kt`.

---

## 11. Zero-Server On-Device Architecture, End-to-End Line Jumping, Interactive `stdin` & Local Snapshots

### 11.1 100% On-Device Zero-Server AST Engine (`AiSectionNamingService.kt`)
- Runs with `preferOfflineLocalEngine = true` by default so Smart Naming, 3-Part Explanations, Idiomatic Refactoring, and Docstring/KDoc generation execute in `<5ms` on-device with zero paid server requirement and zero network timeout failures.
- Supports language-specific documentation conventions (`"""` PEP-257 docstrings for Python, `///` for Rust/Dart, `/** ... */` for Kotlin/Java/C++) and multi-language concept tag inference (`CodeSectionUiModel.inferSmartConceptTagFromCode`).

### 11.2 End-to-End Line Jumping (`EditorViewModel.jumpToCodeLine` → `KotlinCodeSectionCard`)
- `EditorUiState.requestedJumpLine` and `jumpLineTrigger` propagate line-jump requests from `IdeBottomDrawer` (`Problems`, `Debugger` Step Over/Into/Resume, Call Stack), `CodeAssistSidePanel` (`Structure` symbols, foldable regions, and `Search` matches), and `⌘B Go to Def` into `KotlinCodeSectionCard`, which unfolds any enclosing folded block, positions the caret on the target line, requests focus, and calls `bringActiveCodeLineIntoView`.

### 11.3 Interactive `stdin` Pipe, Multi-Language Formatter & Local Version Snapshots
- `IdeBottomDrawer.kt` (`TerminalTabContent`) includes an interactive `stdin >` bar (`terminal_stdin_input`, `terminal_send_stdin_button`) wired to `CodeLanguageEngine.executeAndDebug(..., stdinInput)` with live variable interpolation across all 6 languages.
- `CodeLanguageEngine.formatCodeForLanguage`, `toggleLineCommentForLanguage`, and `getLiveTemplatesForLanguage` provide native formatting, commenting (`#` vs `//`), and live templates per language.
- `EditorUiState.localSnapshotsBySectionId` powers the `⏱ History` pill (`local_history_button_${orderIndex}`) for instant on-device checkpoints and 1-tap rollback.

