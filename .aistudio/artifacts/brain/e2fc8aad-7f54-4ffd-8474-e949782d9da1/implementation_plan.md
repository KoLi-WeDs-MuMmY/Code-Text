# KtScribe IDE: CodeAssist + iOS Glass Workspace Overhaul & SDK Toolchain Manager

This overhaul transforms KtScribe from a visually overloaded floating-card layout into a clean, focused **CodeAssist-inspired IDE workspace** blended with **iOS frosted-glass accents**, while fixing the top-popping Terminal drawer, adding real-time caret-tracking scroll as you type, introducing a dedicated **SDK & Plugin Toolchain Manager**, and restructuring both **Studio Settings** and **File Inspector Settings** into categorized iOS-style grouped cards with sidebar tabs.

---

## User Review & Critical Decisions

> [!IMPORTANT]
> The following architectural and UX decisions incorporate your confirmed choices from Phase 1 alongside all 7 items from your request:

- **Confirmed Decision 1 — Workspace & File Navigation**: **CodeAssist layout with collapsible File Tree and iOS glass accents**. Replaces the stacked multi-row headers and heavy circular rail with a sleek single top bar, a slim vertical Activity Bar, a collapsible hierarchical `Files` tree explorer (`app/src/main/kotlin`, `module.toml`, etc.), pill-shaped editor file tabs (`K Main.kt • ×`), and a clean path breadcrumb bar (`app > example > myapp > Main.kt`).
- **Confirmed Decision 2 — Compiler & Debugger Runtimes**: **SDK & Plugin Manager with downloadable language toolchains**. Introduces an integrated **SDK & Toolchain Manager** (accessible both from the Activity Bar and Main Settings) where users can download, install, update, and verify language compilers, debuggers, and lint plugins (`Kotlin Compiler + K2 Debugger`, `OpenJDK 21 + JDB`, `Python 3.12 + PDB`, `Clang/LLVM + LLDB`, `Dart SDK`, and `Rust Cargo + GDB`), powering rich step-through debugging, variable inspection, and execution logs.
- **Confirmed Decision 3 — Settings Architecture**: **Categorized iOS-style grouped cards with sidebar tabs**. Both **Main Studio Settings** and **File Configuration Settings** are redesigned into a two-column iOS/macOS System Settings layout with left sidebar category tabs and right-hand rounded grouped cards (`Appearance & Themes`, `SDK & Plugin Manager`, `Editor & Caret`, `Keymap & Shortcuts`, `Vault & AI Quota`; plus `File & Language`, `Code Style`, `Export & Bundle` for File Settings).

---

## 1. Overview & Core Concept

- **What It Does**: Provides a distraction-free, desktop-caliber mobile IDE modeled after **CodeAssist** and **iOS/macOS glassmorphic design**, allowing developers to browse project trees, edit multi-language source files with inline diagnostics and parameter hints, manage language SDK/debugger toolchains, and inspect build/terminal/debugger output in a bottom-anchored dock.
- **Target Audience / Persona**: Mobile and tablet developers who want the clarity, speed, and ergonomics of JetBrains Fleet / CodeAssist paired with tactile iOS surfaces, smooth spring physics, and structured settings.
- **Key Value**:
  1. **De-cluttered Canvas**: Eliminates 4 redundant header bars so the code editor commands maximum vertical and horizontal screen space.
  2. **Bottom-Anchored Build & Terminal Panel**: Fixes the container alignment bug so the Terminal / Build / Problems / Debugger / Logcat panel always slides up cleanly from the **bottom** of the workspace.
  3. **Zero-Lag Caret Auto-Scroll**: Automatically tracks every character typed, newline inserted, and soft-keyboard resize (`IME`) using exact `TextLayoutResult.getCursorRect` geometry so the active line is never hidden behind the keyboard or bottom bar.
  4. **SDK & Plugin Toolchain Manager**: Gives users full visibility and control over installed language runtimes, debuggers, and IDE plugins.

---

## 2. User Experience & Visual Design

### Key User Flows

1. **CodeAssist Workspace & Collapsible File Tree Flow**:
   - **Top IDE Bar**: Single sleek row featuring Back (`←`), Left Sidebar Toggle (`◧`), Project Badge + Name (`M My App`), and right-side status & actions: `✓ Indexed` status pill, Save (`💾`), Undo (`↶`), Redo (`↷`), Toolchain/Run Configuration icon, primary `▶ Run` pill button, and `•••` overflow menu.
   - **Left Activity Rail (Slim 44dp Bar)**: Top icons for `Files` explorer, `Search`, `Structure/Code (<>)`, `Bookmarks/Pins`, and `Explain/Notes`; bottom icons for `Build/Terminal (>_ )`, `Problems/Logcat`, `SDK & Plugin Manager`, and `Studio Settings (⚙)`.
   - **Collapsible `Files` Navigator Panel**: Expands beside the Activity Rail showing a hierarchical project tree (`▾ 📁 app`, `▾ 📁 app/src/main/kotlin`, `▾ ⊞ com.example.myapp`, `K Main.kt`, `T module.toml`) plus quick actions (`New File`, `Import/Export`, `Collapse All`). Collapses with one tap to give 100% width to the code editor when typing on smaller screens.
   - **Zero-State ("No file open") Screen**: When all file tabs are closed, displays a centered CodeAssist-style quick-launch hub with warm amber icon and one-tap actions: `🔍 Search everywhere`, `📄 Go to file`, `🕒 Recent files`, `<> Find in files`, `+ New file`, and `📁 Project files`.

2. **Editor Tabs, Breadcrumbs & Real-Time Caret Tracking**:
   - **Tab Strip & Breadcrumb**: Rounded pill tab (`K Main.kt • ×`) above a compact path breadcrumb (`app > src > kotlin > Main.kt`) with right-side toggles for inline Explanation split/sheet and File Settings.
   - **Auto-Scrolling Code Surface**: On every keystroke, cursor move, or soft-keyboard popup, the editor computes `codeLayoutResult.getCursorRect(cursorOffset)` and smoothly scrolls both vertically and horizontally so the caret and inline completion/parameter hint popups (`< 1/20 > println(message: Any?)` and `sout -> println()`) stay comfortably in view.

3. **Bottom-Anchored Build / Terminal / Debugger / Logcat Panel**:
   - Anchored strictly with `Alignment.BottomCenter` inside the workspace container so it always slides up from the **bottom** (just above the keyboard/status bar).
   - Includes CodeAssist-inspired tabs (`Build / Terminal`, `Problems`, `Debugger`, `Logcat`) with filter pills (`All`, `Warnings`, `Errors`), a live `🔍 Filter log...` search field, execution status pill (`Idle` / `Running` / `Finished`), and `▶ Run`, `▭ Maximize`, and `— Minimize` controls.

4. **SDK & Plugin Toolchain Manager Flow**:
   - Accessible from the Activity Bar (`🧩` / `SDK Manager` icon), Main Settings (`SDK & Plugins` tab), or when attempting to debug a language whose toolchain plugin is not yet installed.
   - Displays downloadable and installed packages under two sub-tabs:
     - **Language SDKs & Debuggers**: `Kotlin Compiler & K2 Debugger (kotlinc 2.1)`, `OpenJDK 21 & JDB Debugger`, `Python 3.12 Runtime & PDB`, `Clang/C++20 & LLDB`, `Dart SDK & VM Service`, `Rust Cargo & Rust-GDB`.
     - **IDE Extensions & Plugins**: `Inline Parameter Hints`, `Live Templates & Snippets`, `AST Code Metrics & Immutability Analyzer`, `AI Code Assistant & Auto-KDoc`.
   - Clicking **Download / Install** runs an animated package installation with progress bar, size/version metadata, and persists the installed state so subsequent **Run** and **Debugger** sessions execute with full step-by-step variable inspection, call stack frames, and toolchain console telemetry.

5. **Categorized iOS + IDE Settings Flow**:
   - **Main Studio Settings**: Opens a structured iOS-style modal with a left category rail (`Appearance`, `SDK & Plugins`, `Editor`, `Shortcuts`, `AI & Vault`) and right-hand iOS grouped cards with clean switches, segmented controls, and theme swatches.
   - **File Settings Inspector**: Organized into 3 clean segmented tabs (`General & Language`, `Formatting & Theme`, `Export & Bundle`) while preserving the mandatory confirmation warning dialog before changing a file's language extension.

### Visual Identity & Theme

- **Aesthetic Direction**: **CodeAssist Warm Espresso + iOS Frosted Glass** — ultra-clean dark IDE chrome (`#1A1615` / `#221D1B`), warm terracotta/amber accents (`#D4883A` / `#8B5A2B`), translucent iOS glass headers and popovers with subtle `1.dp` specular borders (`#3A322E`), and zero visual clutter.
- **Typography & Hierarchy**: `Plus Jakarta Sans` for crisp iOS-style UI headers, tabs, and grouped settings labels; `JetBrains Mono` (`13.sp` / `22.sp` line height locked 1:1 with the gutter) for code, breadcrumbs, terminal logs, and debugger variables.

---

## 3. Key Product Decisions & Trade-Offs

- **Decision 1: Consolidating 4 Stacked Toolbars into CodeAssist's 2-Tier Header (Top Bar + File Tab/Breadcrumb)**
  - *Chosen Approach*: Replace the crowded multi-pill top bar, file chip strip, macOS traffic-light bar, and terminal breadcrumb header with CodeAssist's unified Top Bar + Tab/Breadcrumb strip, moving secondary tools into the slim Left Activity Rail and Collapsible Explorer.
  - *Why*: Frees over `110.dp` of vertical screen height (critical when the soft keyboard is up on mobile/tablet) and eliminates the "overloaded" feel while keeping every feature 1 tap away.
- **Decision 2: Exact `TextLayoutResult` Caret Auto-Scroll on Every Input & Keyboard Inset Change**
  - *Chosen Approach*: Trigger `bringActiveCodeLineIntoView` on every `TextFieldValue` text or selection change (not only when line count changes) and use `codeLayoutResult.getCursorRect(cursorOffset)` with a bottom comfort margin (`72.dp`) plus a `WindowInsets.ime` listener.
  - *Why*: Guarantees the line being edited—and its floating autocomplete/parameter tooltip—automatically scrolls into the visible viewport even when typing long lines or when the soft keyboard pops up.
- **Decision 3: Persistent SDK & Plugin Toolchain Manager**
  - *Chosen Approach*: Provide a dedicated `SdkPluginManager` state model persisted via preferences/repository, allowing users to install/uninstall/update compilers and debuggers for Kotlin, Java, Python, C++, Dart, and Rust, and linking installed toolchains directly to the Debugger and Terminal execution engine.
  - *Why*: Directly solves the missing runtime/debugger problem in an authentic mobile IDE workflow while giving clear feedback when a language runtime is installed vs. needs download.

---

## 4. Technical Architecture & Data Strategy *(Technical Reference)*

### Architecture & Component Diagram

```
┌───────────────────────────────────────────────────────────────────────────────────┐
│                         DocumentEditorScreen (Master IDE Shell)                   │
│  ┌─────────────────────────────────────────────────────────────────────────────┐  │
│  │ CodeAssistTopBar: [←] [◧] [M My App]     [✓ Indexed] [💾] [↶] [↷] [▶ Run] │  │
│  └─────────────────────────────────────────────────────────────────────────────┘  │
│  ┌──────┬────────────────────┬─────────────────────────────────────────────────┐  │
│  │ Slim │ Collapsible Files  │ Editor Tab Bar: [K Main.kt • ×] [+]      [⚙]   │  │
│  │ Act. │ Tree Navigator     ├─────────────────────────────────────────────────┤  │
│  │ Rail │ ▾ 📁 app           │ Breadcrumb: app > src > kotlin > Main.kt [<>]   │  │
│  │      │   ▾ 📁 src/kotlin  ├─────────────────────────────────────────────────┤  │
│  │ [📄] │     [K] Main.kt    │ KotlinCodeSectionCard (1:1 Gutter + Code)       │  │
│  │ [🔍] │   [T] module.toml  │  • Caret-locked auto-scroll on input & IME      │  │
│  │ [<>] │                    │  • Inline error pill (ⓧ Unresolved reference)   │  │
│  │ [📝] │ (Or Search /       │  • Parameter hint (< 1/20 > println(Any?))      │  │
│  │ [🧩] │  Structure /       │  • Caret-anchored completion popup (+28.dp)     │  │
│  │ [>_] │  Explain Pane)     ├─────────────────────────────────────────────────┤  │
│  │ [⚙]  │                    │ Bottom-Anchored IdeBottomDrawer (BottomCenter)  │  │
│  │      │                    │  Tabs: [Build] [Problems] [Debugger] [Logcat]   │  │
│  │      │                    │  Filters: (All) (Warnings) (Errors) [🔍 Filter] │  │
│  └──────┴────────────────────┴─────────────────────────────────────────────────┘  │
└───────────────────────────────────────────────────────────────────────────────────┘
          │                                                    │
          ▼                                                    ▼
┌────────────────────────────────────────┐   ┌──────────────────────────────────────┐
│   Structured iOS + IDE Settings Modal  │   │   SDK & Plugin Toolchain Manager     │
│  • Sidebar Tabs:                       │   │  • Language Toolchains (Kotlin,      │
│    - Appearance & Themes               │   │    Java, Python, C++, Dart, Rust)    │
│    - SDK & Plugin Manager              │   │  • Step-through Debugger & Variable  │
│    - Editor & Formatting               │   │    Watch / Call Stack Inspector      │
│    - Keymap & Shortcuts                │   │  • IDE Extensions (Parameter Hints,  │
│    - Hidden Vault & AI Quota           │   │    Live Templates, AI Assistant)     │
└────────────────────────────────────────┘   └──────────────────────────────────────┘
```

### Data Model & State

- **`ToolchainPluginItem` & `PluginCategory`**: Represents downloadable SDK toolchains (`kotlin-sdk`, `openjdk-21`, `python-312`, `clang-lldb`, `dart-sdk`, `rust-cargo`) and IDE plugins (`param-hints`, `live-templates`, `ai-assistant`, `code-metrics`), tracking `isInstalled`, `isDownloading`, `downloadProgress`, `version`, `sizeLabel`, and `debuggerName`.
- **`LeftActivityPanel`**: Controls which panel is active in the collapsible left drawer (`FILES`, `SEARCH`, `STRUCTURE`, `EXPLAIN_NOTES`, `PLUGINS`, or `COLLAPSED`).
- **`BottomDrawerTab` & Log Filters**: Supports `TERMINAL` (Build & Run Output), `PROBLEMS` (Diagnostics), `DEBUGGER` (Variables, Breakpoints & Call Stack), and `LOGCAT` with severity filter chips (`ALL`, `WARNINGS`, `ERRORS`) and a live log search query.

### Interactive Component & State Mapping

- **Bottom Drawer Alignment Fix**: Wrap `IdeBottomDrawer` with `Modifier.align(Alignment.BottomCenter)` inside `DocumentEditorScreen`'s `BoxWithConstraints` so it always animates up from the bottom edge.
- **Caret Auto-Scroll Engine**: In `KotlinCodeSectionCard`, invoke `bringActiveCodeLineIntoView` on every `onValueChange` (both character input and newlines) and when `ime` insets change, calculating exact vertical scroll targets from `codeLayoutResult.getCursorRect(safeOffset)` rather than only line-count deltas.
- **SDK & Plugin Installation & Debugger Integration**: When a user runs or debugs a file in `CodeLanguageEngine.executeAndDebug`, check whether the active language's SDK toolchain is installed; if installed, display full compiler + debugger variable/stack trace output; if uninstalled, display a one-tap `"Install <Language> SDK & Debugger Plugin"` action right inside the Bottom Drawer and Settings Manager.
- **Preserved Core Invariants**:
  - 1:1 `TextLayoutResult` gutter synchronization (`13.sp`, `22.sp` line height, `includeFontPadding = false`).
  - Guarded language switching in `FileSettingsDialog` with the mandatory confirmation `AlertDialog` that preserves `codeContent` as-is.
  - Autocomplete popup offset at `cursorRect.right + 28.dp` plus parameter info tooltip (`println(message: Any?)`).
  - All existing Compose `testTag` identifiers maintained for test compatibility.
