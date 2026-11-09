# IDE Engine & Workspace Architecture Plan

## Executive Summary
This update refines the code editor typography and gutter line alignment, implements MIUI/HyperOS-style vertical and horizontal floating window presets with smooth spring physics, establishes real language file packaging inside `.xScribe` containers (`main.py`, `main.kt`, `Main.java` + `explanation.txt`), rebuilds the multi-language compiler & debugger engine with accurate syntax validation, shifts the floating autocomplete popup further right to keep the typing area unobstructed, introduces the permanent `IDE.md` registry with strict update protocols, adds extension rename locks with warning modals, and delivers a desktop-IDE bottom drawer with Terminal, Debugger, and Problems tabs.

---

## User Review & Critical Decisions

> [!IMPORTANT]
> The following user preferences were confirmed in Phase 1 clarification:
> - **Floating Presets**: Toggle button in window header plus auto-snap by drag aspect ratio (Vertical portrait preset ~340x520dp vs. Horizontal landscape preset ~560x360dp, matching user screenshots).
> - **Bottom Command Block**: IDE bottom drawer sliding up from the downside with dedicated tabs for **Terminal**, **Debugger**, and **Problems**.
> - **File Structure**: File tabs and headers display the native code file (`main.kt`, `main.py`, `Main.java`, etc.) with language color badges, while the parent container manages the `.xScribe` package.

---

## 1. Overview & Core Concept

### What It Delivers
1. **Pixel-Perfect Typography & Zero Drift**:
   - Unifies `BasicTextField` and line gutter font metrics using identical `PlatformTextStyle(includeFontPadding = false)`, `LineHeightStyle(alignment = Center, trim = None)`, and line height (`22.sp`).
   - Eliminates excessive blank line gaps and vertical cursor displacement shown in screenshot Image 4.
2. **MIUI / HyperOS Floating Window Presets**:
   - **Vertical Portrait Preset** (~340dp × 520dp, ~9:16 aspect ratio): Ideal for mobile code previews, AI chat, and document notes.
   - **Horizontal Landscape Preset** (~560dp × 360dp, ~16:9 aspect ratio): Ideal for wide code terminals, split diffs, and side-by-side inspection.
   - **Mini & Full Tiers**: Compact pill preview and maximized workspace.
   - **Smooth Spring Physics**: Window snaps cleanly to the nearest dimension preset upon release, avoiding awkward intermediate sizes.
   - **Header Toggle**: One-tap `⬍ Vertical` / `⬌ Horizontal` switch button in the window header.
3. **Real Language File Extensions & xScribe Container Model**:
   - **Container**: `ProjectName.xScribe` (e.g. `MyProject.ktscribe`, `Script.pyscribe`, `App.javascribe`).
   - **Code File**: Native code file with real extension: `main.kt`, `main.py`, `Main.java`, `main.cpp`, `main.dart`, `main.rs`.
   - **Explanation File**: `explanation.txt` (or `.md`).
   - Editor tabs and window headers display the native filename with its language badge color.
4. **Accurate Multi-Language Debugger & Compiler Diagnostics**:
   - Real syntax checking for Python (indentation, colons, def/class signatures), Java (class declarations, semicolons, brackets), Kotlin, C++, Dart, and Rust.
   - Inline error squiggles, gutter warning icons, and direct error jumping.
   - Debug execution simulation reporting exact line errors, stack trace, and execution metrics.
5. **Right-Shifted Autocomplete Popup**:
   - Autocomplete suggestions are anchored `+28.dp` to the right of the cursor caret with bounds clamping, ensuring the word being typed remains 100% visible.
6. **Dedicated `IDE.md` Feature Tracker**:
   - Creates `/IDE.md` documenting all implemented IDE features and queued upcoming features.
   - Embedded prompt directive requiring AI updates on every subsequent turn.
7. **Extension Rename Protection**:
   - When renaming files, the extension is locked. If the user attempts to alter the extension, a warning dialog appears: *"File extension cannot be changed. Modifying the file type will break compilation and debugging because syntax and runtimes differ."*
8. **Sliding Bottom Command & Debug Drawer**:
   - Replaces the floating command window with a sliding IDE bottom drawer.
   - Three tabs: **Terminal** (build & run output), **Debugger** (variable inspector, step simulation), and **Problems** (live syntax errors).
   - Expandable heights: Minimized bar (36dp), Half (40% screen), and Full (72% screen).

---

## 2. User Experience & Visual Design

### A. Code Editor Typography & Spacing
- **Gutter & Text Synchronization**:
  - Gutter numbers and code text share identical line height (`22.sp`), padding (`0.dp` vertical inside line container), and font metrics (`JetBrains Mono`, `13.5.sp`, `includeFontPadding = false`).
  - Gutter row background highlights the active cursor line seamlessly across both the gutter and editor text.
- **Unobstructed Floating Suggestion Box**:
  - Placed at `cursorRect.right + 28.dp`.
  - JetBrains-style badges (`{}`, `K`, `PY`, `v`, `m`) with dark obsidian elevation and cyan/amber highlights.

### B. MIUI / HyperOS Floating Window Presets
- Modeled after the user's uploaded MIUI floating window screenshots:
  - **Portrait Mode**: 340dp width × 520dp height.
  - **Landscape Mode**: 560dp width × 360dp height.
  - Smooth scale and translation animations powered by Compose `spring(dampingRatio = 0.82f, stiffness = 380f)`.
  - Top header features: Traffic light dots (Close, Minimize, Expand), Mode Toggle (`⬍ / ⬌`), and Title badge.

### C. Bottom Command & Debug Drawer
- Slides up from the bottom edge with a sleek drag handle:
  - **Tabs Bar**: `[ >_ Terminal ]` `[ 🪲 Debugger (0) ]` `[ ⚠ Problems (1) ]`.
  - **Controls**: Run button (`▶`), Clear output (`⊘`), Expand/Collapse toggle (`▲ / ▼`), Close (`✕`).
  - Dark terminal theme with colored syntax output (`stdout` in emerald/cream, `stderr` in crimson).

---

## 3. Architecture & Component Diagram

```
┌────────────────────────────────────────────────────────────────────────┐
│                        DocumentEditorScreen                            │
│  ┌──────────────────────────────────────────────────────────────────┐  │
│  │ SketchTopBarWithFourPills (Files, Explain, AI Guide, Run, Vault) │  │
│  └──────────────────────────────────────────────────────────────────┘  │
│  ┌──────────────────────────────────────────────────────────────────┐  │
│  │ Native File Switcher Tabs: [ main.kt ] [ main.py ] [ Main.java ] │  │
│  └──────────────────────────────────────────────────────────────────┘  │
│  ┌─────────────────────────────────┬────────────────────────────────┐  │
│  │       KotlinCodeSectionCard     │    MacDraggableFloatingWindow  │  │
│  │  ┌──────────┬────────────────┐  │    (MIUI Presets: ⬍ / ⬌)       │  │
│  │  │ Gutter   │ BasicTextField │  │    - Explanation (.txt)        │  │
│  │  │ (Ln 1..) │ (Zero Drift)   │  │    - AI Guide                  │  │
│  │  └──────────┴────────────────┘  │    - Files Explorer            │  │
│  │       ▲                         │                                │  │
│  │  [Right-Shifted Autocomplete]   │                                │  │
│  └─────────────────────────────────┴────────────────────────────────┘  │
│  ┌──────────────────────────────────────────────────────────────────┐  │
│  │             Sliding IDE Bottom Command & Debug Drawer            │  │
│  │  [ Terminal ]  [ Debugger ]  [ Problems (0) ]           [ ▲ / ▼ ]│  │
│  │  - Kotlinc / Python3 / Javac / G++ compilation output & traces   │  │
│  └──────────────────────────────────────────────────────────────────┘  │
└──────────────────────────────────┬─────────────────────────────────────┘
                                   │
                                   ▼
          ┌─────────────────────────────────────────────────┐
          │               CodeLanguageEngine                │
          │  - Multi-language AST syntax validator          │
          │  - Diagnostics engine (line/col/severity)       │
          │  - Debugger simulation & compiler runner        │
          └─────────────────────────────────────────────────┘
```

---

## 4. Technical Strategy & Implementation Details

### 1. Typography & Spacing Fix
- In `KotlinCodeSectionCard.kt`:
  - Set `TextStyle(fontFamily = JetBrainsMonoFontFamily, fontSize = 13.5.sp, lineHeight = 22.sp, platformStyle = PlatformTextStyle(includeFontPadding = false), lineHeightStyle = LineHeightStyle(alignment = Alignment.Center, trim = Trim.None))`.
  - Apply the exact same `TextStyle` to the gutter line numbers.
  - Remove rogue vertical padding inside `Row` and `decorationBox` that caused line offsets.

### 2. MIUI Floating Presets in `MacFloatingIslandComponents.kt`
- Define `enum class WindowOrientationPreset { VERTICAL, HORIZONTAL }`.
- Preset dimensions:
  - Vertical: `widthDp = 340f`, `heightDp = 520f`.
  - Horizontal: `widthDp = 560f`, `heightDp = 360f`.
- Drag release calculates aspect ratio (`width / height`). If ratio > 1.25 -> snaps to `HORIZONTAL`; else snaps to `VERTICAL`.
- Header button allows direct toggle between `VERTICAL` and `HORIZONTAL`.

### 3. Native File Naming in `EditorModels.kt`
- Add `val nativeFileName: String` to `CodeSectionUiModel`:
  - Default: `main.kt` for Kotlin, `main.py` for Python, `Main.java` for Java, `main.cpp` for C++, `main.dart` for Dart, `main.rs` for Rust.
- Rename handler verifies extension: if extension changes, triggers `showExtensionLockedAlert = true`.

### 4. Compiler & Debugger Engine in `CodeLanguageEngine.kt`
- Enhance syntax validation for Python, Java, Kotlin, C++, Dart, Rust:
  - Python: Validates indent levels, missing `:` on def/if/for/while, unclosed strings.
  - Java: Validates class declaration, main method signature, semicolons, brackets.
  - Kotlin: Validates braces, keyword syntax, string templates.
- Returns rich `LanguageDiagnostic` items with line numbers, error descriptions, and quick fixes.

### 5. Right-Shifted Autocomplete Popup
- Position calculation: `popupX = cursorRect.right + 28.dp`.
- Width: `240.dp`, clamped within container width so it never overflows off-screen.

### 6. Sliding Bottom Command & Debug Drawer in `DocumentEditorScreen.kt`
- Replace floating run window with a docked bottom sheet / drawer:
  - Header: Tabs (`Terminal`, `Debugger`, `Problems`), Run button, Clear, Minimize/Expand, Close.
  - Terminal view: shows compiler execution, standard output, error streams, timing, exit code.
  - Debugger view: shows line execution status, simulated local variables, call stack.
  - Problems view: lists all syntax errors with clickable navigation to jump straight to the offending line.

### 7. Feature Tracker Registry `/IDE.md`
- Create `/IDE.md` with:
  - Implemented IDE Features checklist.
  - Planned IDE Features queue.
  - AI maintenance instruction block.

---

## 5. Verification & Test Plan
- **Typography Check**: Ensure 1st, 10th, and 50th line numbers match text lines with 0px offset.
- **MIUI Preset Check**: Drag corner or tap header toggle to verify smooth snap between portrait (340x520) and landscape (560x360).
- **Extension Lock Check**: Attempt to rename `.py` to `.kt` and verify the warning modal appears.
- **Compilation & Test**: Run `compile_applet` and `gradle :app:testDebugUnitTest` to guarantee clean build.
