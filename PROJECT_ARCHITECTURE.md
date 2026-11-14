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
├── compiler/
│   ├── CompilerModels.kt         # Lexer tokens, AST nodes, KtVM opcodes, CompilationArtifact & ReplHistoryEntry
│   ├── KotlinCompilerPlugin.kt   # Bundled 4-stage Kotlin compiler (Lexer -> Parser -> Semantic -> Bytecode Emitter)
│   └── KtBytecodeVm.kt           # Stack-based KtVM, DebugSession (breakpoints/step/trace), & persistent ReplSession
├── model/
│   └── EditorModels.kt           # Domain models, LanguageType, WindowPresetTier, BottomDrawerTab, LocalCodeSnapshot
├── viewmodel/
│   └── EditorViewModel.kt        # Central state holder (EditorUiState), KtVM debug/REPL/diff actions, tab reorder
├── code/
│   ├── CodeLanguageEngine.kt     # Multi-language diagnostics, formatting, snippets & AST/VM execution bridge
│   ├── KotlinCodeEngine.kt       # Deep Kotlin lexer, syntax diagnostics (diagnoseKotlinSyntax), symbols
│   ├── BracketPairColorizer.kt   # Depth-aware rainbow bracket colorizer + cursor pair & unmatched highlights
│   ├── DiffEngine.kt             # Pure Kotlin LCS line diff engine powering Visual Git Diff & History
│   └── OnDeviceInterpreter.kt    # Companion multi-language AST statement/expression interpreter
├── ui/
│   ├── components/
│   │   ├── KotlinCodeSectionCard.kt   # Core code editor, TextLayoutResult-locked gutter, bracket coloring, autocomplete
│   │   ├── IdeBottomDrawer.kt         # Bottom drawer (Terminal, Problems, Debugger, Logcat, REPL, Bytecode/AST, Git Diff)
│   │   ├── MacFloatingIslandComponents.kt # Top bar pills, Left circular IDE rail, viewport-clamped MIUI floating windows
│   │   ├── FileSettingsDialog.kt      # File settings (confirmation-guarded language switcher, Android Share/Copy export)
│   │   ├── ShortcutAndSettingsDialog.kt # Categorized M3 Settings, Built-in Kotlin Compiler Plugin status, Display Profile
│   │   └── ModernFloatingMeshBackground.kt # Adaptive ambient background
│   ├── screens/
│   │   ├── DocumentListScreen.kt      # Adaptive Project Home (M3 NavigationRail on wide screens, ModalBottomSheet/grid)
│   │   └── DocumentEditorScreen.kt    # Main workspace, portrait overflow/sheet adaptation, Fuzzy Command Palette
│   └── theme/
│       ├── ScreenProfile.kt           # Zero-permission WindowMetrics/DisplayMetrics classifier & AdaptiveThemeSpec
│       ├── CompactPortraitTheme.kt    # Narrow portrait phone theme specs
│       ├── CompactLandscapeTheme.kt   # Landscape phone theme specs
│       ├── MediumFoldableTheme.kt     # Foldable / 600dp+ medium theme specs
│       ├── ExpandedTabletTheme.kt     # Tablet / 840dp+ expanded theme specs
│       ├── Theme.kt, AppThemeId.kt, Color.kt & Type.kt # M3 tonal dark/light schemes + 6 IDE palettes
└── MainActivity.kt                    # Entry point, BoxWithConstraints ScreenRatioClassifier, Compose navigation
```

---

## 3. Key Source Files Quick-Reference

| File | What It Does | When to Modify |
|---|---|---|
| `compiler/KotlinCompilerPlugin.kt` & `KtBytecodeVm.kt` | Bundled 4-stage Kotlin compiler (`compile`) + stack VM (`DebugSession`, `ReplSession`). | Extending Kotlin syntax parsing, bytecode opcodes, or VM debugging/REPL. |
| `code/BracketPairColorizer.kt` & `DiffEngine.kt` | Depth-aware bracket pair highlighting & LCS line diffing (`computeLineDiff`). | Adjusting bracket depth colors or snapshot diff visualization. |
| `ui/theme/ScreenProfile.kt` & `*Theme.kt` | Zero-permission aspect-ratio classifier + 4 screen-size theme files (`CompactPortrait`, `CompactLandscape`, `MediumFoldable`, `ExpandedTablet`). | Tuning responsive padding, rail vs bottom sheet behavior, or typography scaling per screen tier. |
| `model/EditorModels.kt` | Defines `CodeSectionUiModel`, `DocumentDetailUiModel`, `LanguageType`, `WindowPresetTier`, `BottomDrawerTab`, `LocalCodeSnapshot`. | Adding new languages, extensions, window presets, or bundle formats. |
| `viewmodel/EditorViewModel.kt` | Manages `EditorUiState`, undo/redo, KtVM debug/REPL/compile state, snapshot restore, tab reordering, and status toasts. | Adding new state mutations, actions, or document commands. |
| `code/CodeLanguageEngine.kt` | Multi-language syntax parsing, autocomplete, formatting, and AST/VM execution bridge. | Adding snippets, syntax rules, or compiler diagnostic logic. |
| `code/KotlinCodeEngine.kt` | Tokenizer and Kotlin-specific syntax rules, bracket counting, and diagnostic generation (`diagnoseKotlinSyntax`). | Improving Kotlin syntax analysis or symbol detection. |
| `ui/components/KotlinCodeSectionCard.kt` | Text editing surface with `BasicTextField`, `TextLayoutResult`-synchronized gutter, bracket colorizer, inline breadcrumb + `Ln/Col`, and suggestions. | Adjusting font metrics, cursor positions, or gutter alignment. |
| `ui/components/FileSettingsDialog.kt` | File Settings modal with confirmation-guarded language switching (`pendingLanguageChange`) and `exportOrCopyCodePayload`. | Updating per-file settings, export formats, or language switch warnings. |
| `ui/components/IdeBottomDrawer.kt` | Sliding bottom drawer containing Terminal, Problems, Debugger, Logcat, REPL, Bytecode/AST, and Git Diff tabs. | Adding debugger features, bytecode views, or REPL/diff controls. |
| `ui/components/MacFloatingIslandComponents.kt` | Top Bar primary pills, Left Circular IDE Rail, and viewport-clamped MIUI floating windows. | Tweaking top bar pills, left rail tools, or floating window geometry. |
| `ui/screens/DocumentEditorScreen.kt` | Adaptive workspace with portrait overflow menu + `ModalBottomSheet`, reorderable tabs, Fuzzy Command Palette, and bottom drawer. | Changing overall screen layout, adding dialogs, or keyboard shortcuts. |
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

---

## 12. Real On-Device Interpreter, Honest Engine State & Termux Export

- `code/OnDeviceInterpreter.kt`: Real multi-language statement/expression interpreter supporting recursion, loops, conditionals, arrays/lists, string interpolation, and interactive `stdin` across Kotlin, Java, Python, C++, Dart, and Rust. Wired through `CodeLanguageEngine.executeAndDebug`.
- `EditorViewModel.kt` / `ToolchainPluginItem`: Zero fake download delays; all built-in AST engines & IDE plugins are enabled out-of-the-box and persist toggle state immediately in `SharedPreferences`.
- `CodeLanguageEngine.buildTermuxNativeCommand` & `IdeBottomDrawer.kt` (`terminal_copy_termux_cmd_button`): Zero-permission `Termux Cmd` clipboard bridge generating heredoc + native compiler execution commands (`kotlinc`, `javac`, `python3`, `g++`, `dart`, `rustc`) for external bare-metal compilation in Termux.

---

## 13. Built-In Kotlin Compiler Plugin, KtBytecodeVm, M3 Tonal UI & Screen-Ratio Theme Dispatch

### 13.1 Built-In Kotlin Compiler Plugin & Stack Bytecode VM (`compiler/`)
- `KotlinCompilerPlugin.compile(code, language)` runs a deterministic 4-stage pipeline in-app: Lexer → Recursive-Descent Parser (`ProgramAst`) → Semantic Analyzer (`SymbolEntry`, `KotlinDiagnostic`) → Stack Bytecode Emitter (`BytecodeInstruction`, constant pool, function/class maps).
- `sdk_kotlin` (`Kotlin Compiler Plugin`) is pre-bundled inside app source (`isInstalled = true`, permanently active, zero server roundtrips). Companion languages (Java, Python, C++, Dart, Rust) also transpile into the unified `ProgramAst` for symbol & bytecode inspection.
- `KtBytecodeVm.DebugSession` drives real breakpoint pausing (`runUntilBreakpoint`), `stepOver()`, trace stepping (`resetTrace()`), live operand stack, heap object map, and call-frame snapshots directly from compiled artifacts.
- `KtBytecodeVm.ReplSession` powers the persistent Kotlin REPL drawer tab (`BottomDrawerTab.REPL`).

### 13.2 Material 3 Tonal Dark UI & Separate Screen-Size Themes (`ui/theme/`)
- `ScreenRatioClassifier` computes device/window aspect ratio & tier from `WindowMetrics`/`DisplayMetrics` + `BoxWithConstraints` with **zero extra Android permissions**, providing `LocalAdaptiveThemeSpec` via `CompositionLocalProvider`.
- Four dedicated theme files (`CompactPortraitTheme.kt`, `CompactLandscapeTheme.kt`, `MediumFoldableTheme.kt`, `ExpandedTabletTheme.kt`) govern padding, spacing, card elevation, `useNavigationRail`, `useCompactTopActions`, grid columns, and `maxContentWidth`.
- M3 tonal surface layering (`surfaceContainerLowest`..`surfaceContainerHighest`), single-line badges, portrait `ModalBottomSheet` side tools, and portrait `"⋮"` overflow menu resolve narrow-screen clipping & vertical-wrap overlaps cleanly.

### 13.3 Bracket Colorizer, Fuzzy Command Palette, Bytecode Inspector & Git Diff
- `BracketPairColorizer`: Rainbow depth styling + cursor pair highlight + unmatched red highlighting while preserving 1:1 `TextLayoutResult` gutter synchronization.
- `DocumentEditorScreen`: Subsequence fuzzy `⌘P` Command Palette + reorderable multi-file project tabs (`◀`/`▶` + `+` language menu).
- `IdeBottomDrawer`: `REPL`, `AST_BYTECODE` (Bytecode vs AST/Symbols segmented viewer), and `GIT_DIFF` (`DiffEngine.computeLineDiff` LCS visual diff against `LocalCodeSnapshot`s with create/restore controls).

