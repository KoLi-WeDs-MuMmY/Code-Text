# KtScribe — Matte Darcula IDE, Tactile Window Depth & Dual Floating Window Studio

KtScribe refines its workspace to match your hand-drawn layout sketches and IDE reference screenshots: replacing all neon colors with a warm, professional **Matte Darcula / Espresso IDE** palette, adding **multi-layered physical window depth (beveled rims, deep ambient drop shadows, and active/inactive z-index elevation)** so every window layer feels distinct, introducing a **cursor-anchored vertical autocomplete dropdown with side documentation card**, locking the bottom shortcut bar to a **constant (`val`) 8-key strip**, and supporting **up to 2 simultaneous free-draggable & resizable floating windows** with fluid macOS spring transitions.

---

## User Review & Critical Decisions

> [!IMPORTANT]
> All product, visual depth, and layout decisions below incorporate your feedback and 5 reference images:

- **Confirmed Decision 1 — Zero Neon + 4-Tier Tactile Window Depth Hierarchy**:
  - All neon colors are removed in favor of a warm Matte Darcula & Espresso palette where each layer is separated by physical depth, drop shadows, and dual-bevel borders:
    1. **Tier 0 — Recessed Outer Frame (`#11100F`)**: Deepest matte chassis holding the left 5-icon IDE rail and top-right window launcher pills.
    2. **Tier 1 — Code Editor Canvas (`#1B1A19`, `6.dp` elevation)**: Framed by a recessed dark outer trench (`#090808`) and a subtle top-edge light catch (`#3A3632`), with a slightly recessed line-number gutter (`#161514`).
    3. **Tier 2 — Cursor-Anchored Completion & Doc Popup (`#252320`, `18.dp` shadow)**: Floats above the code lines with a crisp `1.dp` warm bevel (`#4C463F`) and deep drop shadow.
    4. **Tier 3 — Floating macOS Windows (`28.dp` ambient + key drop shadow, Top Bevel Highlight & Z-Index Focus Depth)**:
       - Floating windows use a distinct **Warm Mocha Slate (`#2B2724`)** surface, a `1.5.dp` top specular bevel (`#5E554C`) simulating overhead studio lighting, and a deep multi-layer ambient shadow (`#000000CC`) so they clearly detach from the code canvas below.
       - **Active vs. Secondary Window Depth**: When 2 floating windows are open at the same time, tapping or dragging either window promotes it to the foreground (`zIndex = 2f`, `28.dp` shadow, `#2C2825` surface, bright top rim) while the other floating window recedes slightly (`zIndex = 1f`, `14.dp` shadow, `#23201E` surface) so both windows feel three-dimensional and distinct.
- **Confirmed Decision 2 — Exact Sketch Layout (Left 5-Icon IDE Rail + Top-Right 4 Window Pills)**:
  - **Top-Left Back Button (`←`)**: Returns to the Folder overview.
  - **Left Vertical Rail (5 Circular IDE Tool Buttons)**: Quick access to **Format (`{ }`)**, **Syntax Quick-Fix (`✓/⚠`)**, **Structure & Find (`🔍`)**, **Line Refactoring (`⌘/`)**, and **New Code Section (`+`)**.
  - **Top-Right Bar (4 Horizontal Oval Pills)**: Launches and toggles the floating windows (**`Explanation`**, **`Folders & Sections`**, **`AI Studio`**, and **`Run Output`**), plus a Command Palette trigger.
- **Confirmed Decision 3 — Constant (`val`) Bottom Shortcut Bar & Cursor-Anchored IDE Completion Box**:
  - The bottom bar inside the code block is strictly a **constant (`val`) 8-key shortcut strip**: `Tab`, `( )`, `{ }`, `" "`, `->`, `.`, `=`, `:`.
  - Autocomplete suggestions (`sout` → `println()`, `soutv`, `fun`, classes, interfaces, and newly declared user symbols badged with `NEW`) appear in a **Vertical IDE Completion Popup + Side Documentation Tooltip** directly beneath the active cursor line.
- **Confirmed Decision 4 — Max 2 Simultaneous Floating Windows with Shake-and-Notify Guard**:
  - Up to **2 floating windows** (excluding the Command Palette) can be open at the same time.
  - If 2 floating windows are already open and you tap a 3rd window pill, the existing 2 windows stay open, perform a tactile **horizontal shake animation**, and show a subtle banner (*"Max 2 floating windows open — cancel (`✕`) one first"*).
  - Every floating window can be **dragged anywhere on screen**, **resized from its left edge or bottom-right corner** (matching Sketch 2 and Sketch 5), and **canceled/closed** via its `✕` button or red traffic light dot with a fluid macOS spring scale+fade transition.

---

## 1. Overview & Core Concept

- **What It Does**: KtScribe delivers a full-screen Apple `cmd` / JetBrains Darcula Kotlin editor framed by a 5-button left IDE tool rail and a 4-pill top-right floating window bar. Every UI plane—outer frame, code canvas, inline completion popup, and floating windows—has distinct surface shading, overhead bevel lighting, and drop-shadow depth.
- **Target Audience / Persona**: Android and Kotlin developers who want an authentic, non-neon desktop IDE experience with tactile spatial depth and fluid macOS window management.
- **Key Value**: Eliminates flat "AI slop" styling by combining true spatial elevation, high-signal IDE diagnostics, and smooth multi-window multitasking.

---

## 2. User Experience & Visual Design

### Key User Flows
1. **Workspace Frame (Matching Your Blue/White/Pink/Green Sketch)**:
   - **Top-Left (`←`)**: Navigates back to the Folders library while auto-saving to the hidden vault.
   - **Left Vertical Rail (5 Circular Buttons)**:
     1. **`{ }` Format**: Auto-indents and normalizes Kotlin code.
     2. **`⚠/✓` Diagnostics & Quick-Fix**: Applies instant syntax fixes or jumps to the next error line.
     3. **`🔍` Find, Replace & Structure**: Opens the inline symbol structure tree (`m main()`, classes, properties) and find/replace bar.
     4. **`⌘/` Line & Block Actions**: Comment/uncomment line, duplicate line (`⌘D`), move line up/down, or surround with `try/catch` / `if`.
     5. **`+` Add Section (`Ctrl+K`)**: Creates the next Kotlin section in the active folder.
   - **Top-Right Oval Pills (4 Floating Window Launchers)**:
     - `[ Explain ]` • `[ Folders ]` • `[ AI Guide ]` • `[ Run ▶ ]` (plus `⌘P` Command Block).
2. **Real IDE Inline Diagnostics & Cursor-Anchored Completion Popup (Matching Screenshots 3 & 4)**:
   - **Inline Line Error Pill**: When a line has a syntax issue or unexpanded shorthand (e.g., line 3 has `sout` or line 1 has incomplete `fun`), an inline pill appears right after the code on that exact line: `⊗ Unresolved reference: sout` or `⊗ Syntax error  2`. Tapping the pill applies the quick fix immediately.
   - **Vertical Completion Box + Companion Doc Tooltip**:
     - Floats at **Tier 2 Depth (`18.dp` shadow)** directly below the active typing line when typing identifiers or abbreviations.
     - Displays category badges (`{}` green for templates like `sout` / `soutv`, `K` for keywords like `fun` / `fun interface`, `C` for classes, `I` for interfaces, `@` for annotations, `m` for functions, and a `NEW` badge for newly declared symbols in your script).
     - The top selected row is highlighted in warm amber-brown (`#75420E`), and a **Companion Documentation Box** sits to the right showing the expansion signature and description (e.g., `sout   println()` / `Print to standard output`).
   - **Constant `val` Bottom Shortcut Strip**:
     - Anchored at the bottom of the code block with a fixed set of 8 essential typing keys: `Tab`, `( )`, `{ }`, `" "`, `->`, `.`, `=`, `:`.
3. **Dual Floating Windows with Tactile 3D Depth (Max 2 Open, Free Drag, Left/Corner Resize, Cancel, Fluid Transition)**:
   - Tapping a top-right oval pill smoothly animates a floating window into view using a macOS-style spring scale + fade + slide transition originating from the top-right.
   - Each floating window renders at **Tier 3 Depth**: a custom multi-layer ambient shadow halo (`drawBehind` radial/drop shadow + `28.dp` elevation), a recessed dark outer ring (`#0D0C0B`), a warm mocha surface (`#2B2724`), and a brighter top-edge bevel (`#63594F`) so the window visually pops off the dark code editor behind it.
   - Tapping anywhere on a floating window brings it to the top of the z-order (`zIndex = 2f`) with deeper shadow contrast, while the secondary floating window steps back (`zIndex = 1f`).

### Visual Identity & Theme
- **Aesthetic Direction**: *JetBrains Darcula & macOS Tactile Graphite* — zero neon, multi-tiered depth, subtle overhead specular bevels, and rich drop shadows.
- **4-Tier Depth & Surface Palette**:
  - **Tier 0 — Outer Workspace Trench**: `#11100F`
  - **Tier 1 — Code Editor Canvas**: `#1B1A19` (Gutter `#151413`, Top Bevel `#383430`, Outer Rim `#090808`)
  - **Tier 2 — Cursor Autocomplete & Doc Cards**: `#252320` (`18.dp` shadow, Highlight `#75420E`, Border `#4C463F`)
  - **Tier 3 — Floating Windows**: Foreground `#2C2825` (`28.dp` shadow + ambient halo, Header `#36312D`, Top Bevel `#63594F`) / Background `#23201E` (`14.dp` shadow, Header `#2B2724`, Top Bevel `#47413B`)

---

## 3. Key Product Decisions & Trade-Offs

- **Decision 1: Multi-Layer Shadow + Dual-Bevel Rim Rendering for True Window Depth**
  - *Chosen Approach*: Combine Compose `shadow(...)` with a custom `drawBehind` ambient shadow pass and a 2-layer border (a `1.dp` dark outer contour `#0A0908` paired with a vertical gradient inner bevel that is lighter at the top `#63594F` and darker at the bottom `#2E2A27`), plus dynamic `zIndex` focus promotion when 2 windows are open.
  - *Why*: On dark themes, standard flat cards blend into the background. A dark outer trench + top-lit inner bevel + soft dark drop shadow is how macOS and JetBrains IDEs create unmistakable physical depth without using garish bright colors.
- **Decision 2: Strict Separation of Constant (`val`) Bottom Keys vs. Cursor-Anchored Autocomplete**
  - *Chosen Approach*: Keep `VAL_SHORTCUT_KEYS` immutable (`Tab`, `( )`, `{ }`, `" "`, `->`, `.`, `=`, `:`) at the bottom of the code block, and render all live completions (`sout`, `soutv`, `fun`, classes, interfaces, `NEW` user symbols) in a floating IDE popup directly below the active cursor line with a side documentation card.
  - *Why*: Muscle memory requires shortcut keys to stay in fixed positions (`val`), while code completions belong at eye-level right under the cursor.

---

## 4. Technical Architecture & Data Strategy *(Technical Reference)*

### Architecture & Component Diagram
```
┌──────────────────────────────────────────────────────────────────────────────┐
│              Tier 0: Outer Workspace Trench (#11100F)                        │
│  [← Back]                                  [Explain] [Folders] [AI] [Run ▶]  │
├────────┬─────────────────────────────────────────────────────────────────────┤
│  Left  │  Tier 1: Beveled Code Block Canvas (#1B1A19, 6.dp Depth)            │
│  Rail  │  • • •   §1 • Section Title                            [⌘P Palette] │
│        │  ─────────────────────────────────────────────────────────────────  │
│  ({ }) │  1 │ fun main() {                                                   │
│        │  2 │     sout|   [⊗ Unresolved reference: sout]                     │
│  (✓/⚠) │    │     ╔══════════════════════════════════╗ ╔══════════════════╗  │
│        │    │     ║ {} sout               println()  ║ ║ sout  println()  ║  │
│  (🔍)  │    │     ║ {} soutv   println("v = ${v}")   ║ ║ Print to stdout  ║  │
│        │    │     ║ K  fun          fun name() { }   ║ ╚══════════════════╝  │
│  (⌘/)  │    │     ╚══ Tier 2: 18.dp Drop Shadow ═════╝                       │
│        │    │                                  ╔══════════════════════════╗  │
│  ( + ) │    │                                  ║ • • •  Floating Win #1   ║  │
│        │    │                                  ║ ▐ Left & Corner Resize   ║  │
│        │    │                                  ║ Tier 3: 28.dp + Bevel Rim║  │
│        │    │                                  ╚══════════════════════════╝  │
│        │  ─────────────────────────────────────────────────────────────────  │
│        │  [ Tab ]  [ ( ) ]  [ { } ]  [ " " ]  [ -> ]  [ . ]  [ = ]  [ : ]    │
└────────┴─────────────────────────────────────────────────────────────────────┘
```

### Data Model & State
- **`FloatingWindowType` (`EXPLANATION`, `FOLDERS`, `AI_STUDIO`, `RUN_OUTPUT`)**:
  - Managed via `openFloatingWindows: List<FloatingWindowType>` (max size 2, ordered from background `index 0` to foreground `lastIndex` so tapping any window moves it to `lastIndex` with highest `zIndex` and deepest shadow).
- **`KotlinSuggestion` (Extended IDE Metadata)**:
  - `label: String` (e.g., `"sout"`), `tailSignature: String` (e.g., `"println()"`), `insertText: String`, `badgeChar: String` (`"{}"`, `"K"`, `"C"`, `"I"`, `"@"`, `"m"`), `docSummary: String` (for the side documentation card), and `isNewSymbol: Boolean` (true for newly declared identifiers in the current code).
- **`VAL_SHORTCUT_KEYS`**:
  - Compile-time constant list of the 8 essential typing shortcuts (`Tab`, `( )`, `{ }`, `" "`, `->`, `.`, `=`, `:`).

### Interactive Component & State Mapping
- **3D Window Depth Modifier (`Modifier.macWindowDepth`)**: Renders a soft ambient drop shadow behind the window, a `1.dp` pitch-black outer separation border, and a `1.dp` top-lit specular bevel border that brightens when the window is the focused top floating window.
- **macOS Fluid Window Transition**: Each floating window is wrapped in `AnimatedVisibility` with `scaleIn(initialScale = 0.86f, transformOrigin = TransformOrigin(0.9f, 0.05f), animationSpec = spring(dampingRatio = 0.78f, stiffness = 380f)) + fadeIn()` and matching `scaleOut + fadeOut`.
- **Inline Line Error Pill & Cursor-Anchored Completion Overlay**:
  - If `diagnostics` contains an error on a line, an inline `⊗` error pill is drawn right next to that line's text.
  - When `currentPrefix` is non-empty, the Tier-2 floating IDE completion box + side documentation card renders directly below the active cursor line.
