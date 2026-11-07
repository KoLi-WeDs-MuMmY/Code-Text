# KtScribe IDE — Full-Screen `.ktscribe` File Workspace, Syntax-Aware Completion & Split-Screen Engine

This update refines KtScribe into a fast, bezel-safe, full-screen Kotlin IDE workspace organized around **Folders $\rightarrow$ `.ktscribe` Files (1 File = 1 Code Block + Bundled Notes)**, with syntax-aware completion that never jumps the scroll view, reversible left-rail IDE tools with one-tap Undo, unrestricted full-screen floating window dragging, adjustable side-by-side or bottom split-screen docking, and a bottom-docked IDE Command Block.

---

## User Review & Critical Decisions

> [!IMPORTANT]
> All three interactive design questions from Phase 1 have been confirmed and integrated into this plan:

- **Confirmed Decision 1 — Folders $\rightarrow$ `.ktscribe` Files (`1 File = 1 Code Block`)**:
  - Each Folder contains individual **`.ktscribe` files** (e.g., `Main.ktscribe`, `CoroutinesLab.ktscribe`).
  - **1 `.ktscribe` file = 1 single full-screen code block** bundled with its explanation/notes and learned concept metadata. Switching `.ktscribe` files switches the active full-screen code block cleanly without stacking multiple code blocks vertically.
- **Confirmed Decision 2 — Adjustable Split-Screen Docking (Side-by-Side + Bottom Switch)**:
  - Any open floating window can be converted into an **adjustable split screen** via a dock button in its window header, defaulting to a **side-by-side split** with a draggable divider and a one-tap toggle to switch between **Side-by-Side Split (`◫`)** and **Bottom Split (`⬒`)**, or undock back to a floating window.
- **Confirmed Decision 3 — Left Rail Toggle-Off, Undo & Rail Collapse**:
  - Tapping an active left-rail circular tool button a second time **toggles it off** immediately.
  - Every code-mutating left-rail action (`{ }` Format, `!` Quick-Fix, `⌘/` Line Actions) records an undo snapshot, and the left rail includes a dedicated **Undo (`↶`)** button plus a **Collapse/Hide Rail (`«` / `»`)** toggle so the rail can be tucked away at any time.

---

## 1. Overview & Core Concept

- **What It Does**:
  1. **True Full-Screen Code Block with Bezel Breathing Room**: The central Matte Darcula code block fills the entire workspace height and width (`fillMaxSize()`), surrounded by generous safe-area and bezel padding (`14.dp` horizontal, `10.dp` vertical) so the top and left circular/oval buttons never clip against curved phone corners or screen bezels.
  2. **Folders $\rightarrow$ `.ktscribe` Files (`1 File = 1 Code Block`)**: Inside each Folder, items are represented as `.ktscribe` files. Each `.ktscribe` file holds exactly one Kotlin code block and its paired explanation/concept notes, exportable and recoverable as `.ktscribe` payloads.
  3. **Syntax-Context-Aware Autocomplete (No False Triggers in Strings/Comments)**: The completion engine inspects the AST/lexical state up to the cursor and suppresses variable/type suggestions when typing inside string literals (`"..."`, `"""..."""`), character literals, or comments (`// ...`, `/* ... */`), unless typing a string template interpolation (`$identifier` or `${expression}`).
  4. **Zero-Scroll-Jump Keyboard-Aware Completion Overlay**: The completion dropdown and companion doc card render in a zero-layout-size overlay layer (`layout(0, 0)`) rather than expanding the code container height, eliminating scroll jumps when suggestions appear and automatically flipping above the cursor line when the soft keyboard is near the cursor.
  5. **Full-Screen Floating Window Drag + Adjustable Split-Screen Conversion**: Floating windows use dynamic viewport bounds (`BoxWithConstraints` width/height in pixels) so they can pass smoothly across the middle of the screen from edge to edge, or dock into an adjustable Side-by-Side or Bottom split screen.
  6. **Bottom-Docked IDE Command Block**: The Command Block (`⌘P` / Terminal & IDE Actions) spawns at the **bottom of the screen** like a classic desktop IDE bottom panel, sliding smoothly up above the navigation bar / keyboard.

---

## 2. User Experience & Visual Design

### Spatial Layout & Bezel-Safe Breathing Room

```
┌────────────────────────────────────────────────────────────────────────────┐
│  [14dp Bezel Safe Margin]                                                  │
│  [← Folders] [Main.ktscribe ▾]         (Explain) (Files) (AI Guide) (Run▶) │
├──────┬─────────────────────────────────────────────────────────────────────┤
│  («) │  ┌───────────────────────────────────────────────────────────────┐  │
│      │  │ • • •   Main.ktscribe               [Concept Tag] [Undo ↶]    │  │
│  { } │  ├────┬──────────────────────────────────────────────────────────┤  │
│      │  │ 1. │ fun main() {                                             │  │
│  ✓/! │  │ 2. │     println("Typing prose here won't trigger vars")      │  │
│      │  │ 3. │     sout▌                                                │  │
│  🔍  │  │    │       ┌──────────────────────────┬────────────────────┐  │  │
│      │  │ 4. │       │ {} sout        println() │ sout  println()    │  │  │
│  ⌘/  │  │ 5. │       └──────────────────────────┴────────────────────┘  │  │
│      │  │    │  (Zero-layout-impact overlay: flips above keyboard)      │  │
│  ↶   │  ├────┴──────────────────────────────────────────────────────────┤  │
│      │  │  [ Tab ] [ ( ) ] [ { } ] [ " " ] [ -> ] [  .  ] [  =  ] [  :  ]│  │
│  +   │  └───────────────────────────────────────────────────────────────┘  │
├──────┴─────────────────────────────────────────────────────────────────────┤
│  ▼ Bottom-Docked IDE Command Block (Spawns at Bottom like IntelliJ/VSCode) │
└────────────────────────────────────────────────────────────────────────────┘
```

### Visual Identity & Motion Refinements
- **Bezel-Safe Breathing Space**:
  - Top bar and left vertical rail apply `WindowInsets.safeDrawing` plus an extra `12.dp–14.dp` outer inset margin from physical screen edges so curved screen corners and display cutouts never obscure the buttons.
- **Smooth 60fps Transitions & Performance**:
  - All floating windows, split-screen docks, left-rail collapse toggles, and the bottom Command Block use unified critically damped spring specs (`dampingRatio = 0.82f`, `stiffness = 420f`).
  - Expensive regex syntax highlighting, diagnostic passes, and structure symbol extraction are debounced/memoized on background/derived states and stripped of layout-thrashing `animateContentSize()` modifiers on the main editor canvas.

---

## 3. Key Product Decisions & Trade-Offs

- **Decision 1 — Lexical Context Gate for Autocomplete (Item 3)**:
  - *Chosen Approach*: Before computing suggestions, scan from the start of the code (or current statement) up to `cursorPosition` to track whether the cursor is inside a double-quoted string (`"..."`), raw triple-quoted string (`"""..."""`), line comment (`//`), or block comment (`/* ... */`). Inside strings, only trigger completion if the user explicitly types `$` or `${...}` for Kotlin string interpolation; inside comments, never trigger code completion. Furthermore, after `.` only suggest member methods/properties, and after `:` only suggest types.
  - *Why*: Prevents prose words inside `println("Hello world...")` from matching variable names or types declared earlier in the file.
- **Decision 2 — Zero-Layout-Impact Overlay for Completion Box (Item 4)**:
  - *Chosen Approach*: Render the cursor completion box inside an overlay modifier/layer whose measured layout footprint is `0x0` in the parent scroll container, and compute whether `(cursorY + popupHeight)` exceeds the visible viewport height above the IME keyboard. If it would be clipped by the keyboard, anchor the popup **above** the active line (`cursorY - popupHeight - 6.dp`) instead of below it.
  - *Why*: Eliminates the layout-height expansion that previously forced the scroll container to jump up and down when opening or closing the suggestion popup.
- **Decision 3 — Full-Screen Coordinate System for Floating Windows (Item 6 & Item 7)**:
  - *Chosen Approach*: Anchor floating windows in a full-screen `BoxWithConstraints` using pixel bounds derived from the actual screen width and height (`-maxWidthPx..maxWidthPx` and `-maxHeightPx..maxHeightPx` clamped to keep the header bar reachable), allowing any floating window to cross the center line freely to the far left, right, top, or bottom. Adding a `Dock Split (◫ / ⬒)` button in the window header docks that window into an adjustable Side-by-Side or Bottom split pane with a draggable resize divider.

---

## 4. Technical Architecture & Data Strategy

### Component & State Flow Diagram

```
┌─────────────────────────────────────────────────────────────────────────┐
│                   EditorViewModel & EditorUiState                       │
│  • Active Folder & Active `.ktscribe` File (1 File = 1 Code Block)      │
│  • Per-File Undo/Redo Stack (captures typing + Left-Rail Format/Fix)    │
│  • Floating Windows (max 2) + Split-Screen Target & Orientation (◫ / ⬒) │
│  • Bottom Command Block State (Docked at Bottom with filter & terminal) │
└───────────────────┬─────────────────────────────────┬───────────────────┘
                    │                                 │
                    ▼                                 ▼
┌───────────────────────────────────────┐ ┌───────────────────────────────┐
│   Syntax-Context KotlinCodeEngine     │ │   Workspace & Window Layer    │
│ • Lexical state scanner (inString /   │ │ • Bezel-safe Top & Left Rails │
│   inInterpolation / inComment / dot)  │ │ • Left-Rail Toggle + Undo + « │
│ • O(1) memoized syntax highlighting   │ │ • Full-screen free-drag float │
│ • `.ktscribe` file serializer/export  │ │ • Side/Bottom Split + Bottom  │
│                                       │ │   Command Block Drawer        │
└───────────────────────────────────────┘ └───────────────────────────────┘
```

### Interactive Component & State Mapping
1. **Full-Screen Single-Block `.ktscribe` Editor**:
   - Removes outer vertical page scrolling around cards so the single `.ktscribe` code block occupies 100% of the editor frame (`fillMaxSize()`) with internal line scrolling and a fixed constant 8-key bottom bar (`Tab`, `( )`, `{ }`, `" "`, `->`, `.`, `=`, `:`).
   - Top bar and Files window display `.ktscribe` file names (e.g., `Coroutines.ktscribe`), allow creating/switching/renaming/exporting `.ktscribe` files inside the active Folder, and enforce 1 code block per `.ktscribe` file.
2. **Left Rail Controls (Undo + Toggle Off + Collapse)**:
   - Adds an explicit **Collapse/Expand (`«` / `»`)** pill at the top of the left rail to hide or show the rail when desired.
   - Adds an **Undo (`↶`)** circular button on the left rail that immediately reverts the last Format, Quick-Fix, Line Action, or text edit.
   - Tapping any active tool button (`✓/!`, `🔍`, `⌘/`) when it is already active turns it off (`LeftRailIdeTool.NONE`) and closes its inline bar.
3. **Bottom-Docked IDE Command Block**:
   - Clicking `⌘P` or opening the Command Block slides up a docked IDE drawer at the **bottom** of the screen (above the keyboard/nav bar) containing instant command search, `.ktscribe` file switcher, live Kotlin execution output, and quick refactorings.
