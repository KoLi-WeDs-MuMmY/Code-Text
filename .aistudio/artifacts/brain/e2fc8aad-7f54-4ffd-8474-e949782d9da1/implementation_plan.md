# KtScribe IDE — Precision Line Alignment, File Settings Language Guard, Top/Left Toolbar Re-Architecture & Export Flow

This update eliminates the vertical drift between gutter line numbers and code text, removes the accidental single-click language switcher from the main editor header in favor of a confirmation-guarded selector inside File Settings, consolidates primary workspace actions (`Explain`, `Files`, `Terminal`, `Run`, `Settings`) into the Top Bar while placing secondary editor/export tools in the Left Rail, and wires working Android Share/Save and Clipboard export actions with visual toast confirmation.

## User Review & Critical Decisions

> [!IMPORTANT]
> All core UX and functional decisions have been confirmed and incorporated into this plan:

- **Confirmed Decision 1 (Language Change Behavior)**: Changing a file's language/extension is removed from the main code card header and restricted to **File Settings**. Selecting a new language in File Settings triggers a mandatory confirmation warning popup explaining that the existing code may not compile or run under the new language syntax; upon confirmation, the existing code is preserved as-is and the file extension and language syntax are updated.
- **Confirmed Decision 2 (Top Row vs. Left Rail Hierarchy)**:
  - **Top Row**: `Explain`, `Files`, `Terminal` (toggles the bottom IDE drawer for Terminal/Debugger/Problems), `Run ▶`, `⌘P`, and `Settings` (opens File Settings). The redundant bottom status bar is removed so `Terminal` is never hidden at the very bottom corner.
  - **Left Rail**: Editor and file tools including `{ }` Format, `!` Diagnostics/Problems, `🔍` Find & Structure, `⌘/` Line Actions, `🎨` Color Picker, `✨` AI Tools, `📤` Export/Copy File, `+` New File, and `↶` Undo.
- **Confirmed Decision 3 (Export & Copy Delivery)**: Triggering Export or Copy both copies the formatted payload to the system clipboard with an immediate confirmation banner and launches the Android system Share/Save sheet (`Intent.ACTION_SEND`) so the user can save or share the `.kt`, `.py`, `.java`, `.ktscribe`, or `.md` file directly.

---

## 1. Overview & Core Concept

- **What It Does**: Fixes the visual line-height mismatch between the gutter numbers and `BasicTextField` code lines, prevents accidental single-tap language switches on the main canvas, elevates Terminal/Debug access to the top bar, and makes every Copy/Export button deliver immediate clipboard + Android Share sheet output with toast feedback.
- **Key Value**: Gives developers rock-solid cursor and line-number alignment at any system font scale, safe language configuration inside File Settings, and ergonomic access to Terminal and Export tools.

---

## 2. User Experience & Visual Design

- **Pixel-Locked Gutter & Code Line Alignment**:
  - Previously, the gutter row height used `22.dp` (density-independent pixels) while `BasicTextField` used `lineHeight = 22.sp` (scaled pixels) with `fontSize = 13.5.sp`, causing cumulative vertical drift across lines (`1.` through `9.`).
  - Now, every gutter number row reads the exact rendered line top and height directly from `TextLayoutResult.getLineTop(i)` and `getLineBottom(i)` (with an exact `with(density) { 22.sp.toDp() }` fallback before initial layout), guaranteeing 1:1 pixel alignment between every line number and its corresponding code line—even when lines wrap or system font scaling is active.
- **Read-Only Language Badge on Main Canvas + Guarded Switcher in File Settings**:
  - On the main editor card, the language badge (`KT`, `PY`, `JAVA`, `C++`, `DART`, `RS`) is a clean indicator pill without a dropdown arrow; tapping it opens **File Settings**.
  - Inside **File Settings**, tapping a different language opens a dedicated **Language & Extension Change Warning Dialog** warning: *"Changing the file type from Kotlin (.kt) to Python (.py) means existing code contents may not compile or run under the new language syntax. Do you want to proceed?"*
- **Top Bar & Left Rail Ergonomics**:
  - The bottom-right status bar (`>_ Terminal` at the very bottom edge) is removed so it never gets cut off by screen curves or gesture bars.
  - The **Top Bar** houses the primary workspace pills: `Explain`, `Files`, `Terminal` (opens/toggles the IDE Terminal/Debugger/Problems drawer), `Run ▶`, `⌘P`, and `⚙ File Settings`.
  - The **Left Circular IDE Rail** houses the secondary coding and file utilities: `{ }` Format, `!` Diagnostics, `🔍` Find/Replace, `⌘/` Line Actions, `🎨` Color Palette, `✨` AI Assistant Menu, `📤` Export & Copy File, `+` New File, and `↶` Undo.

---

## 3. Key Product Decisions & Trade-Offs

- **Decision 1: `TextLayoutResult`-Driven Gutter Positioning**
  - *Chosen Approach*: Drive each gutter line number's vertical height from `codeLayoutResult.getLineBottom(lineIdx) - codeLayoutResult.getLineTop(lineIdx)` converted via `LocalDensity`, and ensure both gutter `Text` and `BasicTextField` share identical `fontSize = 13.sp` and `lineHeight = 22.sp`.
  - *Why*: `dp` and `sp` diverge whenever Android's system font scale is not `1.0` (e.g. `1.15x` or `1.3x` on tablets/phones). Measuring directly from `TextLayoutResult` eliminates any possibility of drift.
- **Decision 2: Confirmation-Guarded Language Change in File Settings**
  - *Chosen Approach*: Require explicit confirmation in File Settings before switching `LanguageType`, preserving the user's code while updating the language extension and syntax highlighter.
  - *Why*: Prevents accidental single-click language changes while editing code and warns the user about syntax/compiler mismatches without destroying their written code.

---

## 4. Technical Architecture & Data Strategy

```
┌────────────────────────────────────────────────────────────────────────────┐
│                             TOP WORKSPACE BAR                              │
│  [← File.kt] [Vault]      [Explain] [Files] [>_ Terminal] [Run ▶] [⌘P] [⚙] │
└──────────────────────────────────────┬─────────────────────────────────────┘
                                       │
       ┌───────────────────────────────┴───────────────────────────────┐
       ▼                                                               ▼
┌──────────────────────────┐                           ┌─────────────────────┐
│   LEFT CIRCULAR RAIL     │                           │  FILE SETTINGS (⚙)  │
│  • { } Format Code       │                           │  • File Name Input  │
│  • !   Diagnostics       │                           │  • Language Picker  │
│  • 🔍  Find & Replace    │                           │    └─► Warning Popup│
│  • ⌘/  Line Actions      │                           │        (Keep Code & │
│  • 🎨  Color Picker      │                           │         Switch Ext) │
│  • ✨  AI Actions        │                           │  • Theme Override   │
│  • 📤  Export / Copy     │                           │  • Export & Share   │
│  • +   New Code File     │                           └─────────────────────┘
│  • ↶   Undo              │
└────────────┬─────────────┘
             │
             ▼
┌────────────────────────────────────────────────────────────────────────────┐
│                   CODE EDITOR CANVAS (TextLayoutResult Sync)               │
│  Gutter Line [i] Height == codeLayoutResult.getLineBottom(i) - LineTop(i)  │
│  1.  sealed interface NoteUiState {                                        │
│  2.      data object Loading : NoteUiState                                 │
│  3.  }                                                                     │
└────────────────────────────────────────────────────────────────────────────┘
```

### Interactive Component & State Mapping
1. **Gutter & Code Line Sync**:
   - Both gutter and `BasicTextField` use `fontSize = 13.sp`, `lineHeight = 22.sp`, `PlatformTextStyle(includeFontPadding = false)`, and `LineHeightStyle(Alignment.Center, Trim.None)`.
   - Each gutter row for `lineNum` (`1..visibleLines`) calculates its exact height from `codeLayoutResult`: if `lineIndex < layout.lineCount`, height is `with(density) { (layout.getLineBottom(lineIndex) - layout.getLineTop(lineIndex)).toDp() }`; otherwise `with(density) { 22.sp.toDp() }`.
2. **Guarded Language Change in File Settings**:
   - Main editor card header removes the `DropdownMenu` from the language badge; clicking the badge opens `FileSettingsDialog`.
   - In `FileSettingsDialog`, clicking another `LanguageType` sets `pendingLanguageChange = lang`, opening an `AlertDialog` warning that existing file contents may not run or compile under the new extension. Confirming calls `onUpdateLanguage(lang)` (which preserves `codeContent` as-is and updates `language`) and shows a status banner.
3. **Top Bar & Left Rail Consolidation**:
   - Remove `IdeBottomStatusBar` from `DocumentEditorScreen`'s `Scaffold.bottomBar`.
   - Add `>_ Terminal` pill to `SketchTopBarWithFourPills` to toggle `IdeBottomDrawer`, and move the clutter of small right-side card icons into `SketchLeftCircularIdeRail` (`COLOR_PALETTE`, `AI_TOOLS`, `EXPORT_COPY`).
4. **Export & Copy Execution**:
   - Add `exportAndShareText(context, fileName, content)` helper using `Intent.ACTION_SEND` (`text/plain`) + `Intent.createChooser` alongside `clipboardManager.setText(AnnotatedString(content))` and `onShowStatusBanner("Copied & ready to share/save $fileName")`.
