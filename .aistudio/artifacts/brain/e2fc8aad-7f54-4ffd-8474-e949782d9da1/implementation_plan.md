# IDE Typing Stabilization, Multi-Language Debugging, 4-Tier Window Presets & Modern Studio Home

Fix text editor typing misalignment and blank line cascades, explain and enhance how the real-time debugging and compilation engine works, replace bottom completion chips with a cursor-anchored floating IDE popup, establish discrete 4-tier MIUI/iOS-style window snapping with spring physics, and modernize the home screen with an animated AI Studio mesh background and aesthetic trichrome branding.

## User Review & Critical Decisions

> [!IMPORTANT]
> The following key decisions were confirmed via interactive user clarification and govern this implementation plan:

- **Comprehensive Debugging & Execution Architecture**:
  - Detailed architectural breakdown of how real-time diagnostics, inline gutter error pills, one-tap quick-fixes, multi-language compiler simulations (`kotlinc`, `javac`, `python3`, `dart`, `g++`, `rustc`), and terminal output floating windows operate together.
- **Typing Engine Stabilization**:
  - Gutter line height will dynamically match `BasicTextField` metrics using font-scaled scalable units (`sp` translated via density) to eliminate line drift between code and line numbers.
  - Soft-keyboard multi-character inputs, composing spans, and Enter auto-indentation are normalized to prevent accidental blank lines and unwanted cursor jumpiness.
- **Contextual Floating Completion Box**:
  - Replaces the bottom suggestion chips with a floating IDE popup anchored directly beside the active cursor/token in the code space, featuring JetBrains-style badges (`{}`, `K`, `C`, `m`, `v`), syntax descriptions, and keyboard/touch completion.
- **4-Tier Discrete Floating Window Presets (MIUI / iOS / Harmony)**:
  - Instead of arbitrary pixel dimensions, every floating window will snap with fluid spring physics to one of 4 discrete tiers: **Mini** (PIP glance), **Half** (Standard utility), **Two-Thirds** (Expanded editor), and **Full** (Immersive modal).
  - Each window type (`EXPLANATION`, `FOLDERS`, `AI_STUDIO`, `RUN_OUTPUT`) defines custom content-proportioned dimensions for each of the 4 tiers.
- **Home Page Atmosphere & Aesthetics**:
  - Full-screen animated ambient fluid mesh gradient with antigravity floating constellation particles inspired by Google AI Studio and developer portfolios.
  - Rich Material Symbol icon support across folder stats, language badges, and navigation actions.
  - Minimal aesthetic trichrome logo combining sharp geometric compiler brackets, diamond nodes, and radiant studio accents.

---

## 1. Overview & Core Concept

This milestone addresses core productivity friction, debugging transparency, and visual polish in KtScribe:
1. **Flawless Code Typing Experience**: Eliminates the vertical drift where line numbers slip out of sync with text, prevents runaway empty lines during rapid typing or soft-keyboard suggestions, and anchors viewport scrolling strictly to active cursor bounds.
2. **Desktop IDE Contextual Autocomplete**: Elevates code completion from an awkward bottom bar into a floating contextual overlay directly beside the caret, matching Xcode, IntelliJ IDEA, and VS Code.
3. **Transparent & Robust Debugging Engine**: Details and polishes the multi-stage debugging system: live syntax tokenization, gutter error badges, one-tap auto-fixes, and isolated terminal runtime execution.
4. **MIUI / iOS Discrete Window Snapping**: Brings mobile OS-grade window management with 4 calibrated preset tiers that prevent awkward content cutoffs, snapping with fluid spring physics on resize release.
5. **Studio-Grade Home Interface**: Transforms the initial document list into a fluid, responsive dashboard with Google AI Studio animated mesh aesthetics, rich symbol indicators, and a modern trichrome brand mark.

---

## 2. How Debugging & Execution Work in KtScribe

The application implements a full-featured, lightweight on-device development environment. Debugging operates through four synchronized layers:

```
┌─────────────────────────────────────────────────────────────────────────────────┐
│                       Developer Types Code in Section Card                       │
└────────────────────────────────────────┬────────────────────────────────────────┘
                                         │
                   ┌─────────────────────┴─────────────────────┐
                   ▼                                           ▼
┌─────────────────────────────────────┐     ┌─────────────────────────────────────┐
│    1. Real-Time Diagnostic Engine   │     │    2. Visual Error & Quick-Fixes    │
├─────────────────────────────────────┤     ├─────────────────────────────────────┤
│ • O(1) LRU syntax analysis cache    │     │ • Gutter Badges: red ✕ or yellow !  │
│ • Bracket & quote balance tracker   │     │ • Inline Error Pills over code lines│
│ • Language-specific keyword linting │     │ • One-tap "Quick-Fix" code applier  │
│ • Entry-point detection (main fun)  │     │ • Top-bar Error Counter (e.g. "!2") │
└──────────────────┬──────────────────┘     └─────────────────────────────────────┘
                   │
                   ▼
┌─────────────────────────────────────────────────────────────────────────────────┐
│                    3. Interactive Terminal & Compilation Runner                 │
├─────────────────────────────────────────────────────────────────────────────────┤
│ • Triggered by "Run ▶" button (Header or Island Pill)                           │
│ • Validates language target: Kotlin, Java, Python, Dart, C++, or Rust           │
│ • Dispatches execution simulation with real compiler CLI formatting:            │
│   - kotlinc Main.ktscribe -include-runtime -d Main.jar && java -jar Main.jar    │
│   - javac Main.java && java Main                                                │
│   - python3 main.py                                                             │
│   - dart run main.dart                                                          │
│   - g++ -O3 main.cpp -o main && ./main                                          │
│   - rustc main.rs && ./main                                                     │
│ • Extracts dynamic console outputs, variable states, and standard streams       │
└──────────────────┬──────────────────────────────────────────────────────────────┘
                   │
                   ▼
┌─────────────────────────────────────────────────────────────────────────────────┐
│              4. Run Output Floating Window & AI Deep Debugging Assister         │
├─────────────────────────────────────────────────────────────────────────────────┤
│ • Spawns macOS-styled terminal window displaying formatted stdout & exit code   │
│ • If errors persist: AI Deep Explain and AI Auto-Refactor options analyze       │
│   the exact failing code segment and recommend corrected implementations        │
└─────────────────────────────────────────────────────────────────────────────────┘
```

### Detailed Breakdown of Debugging Features:

1. **Syntax Checking & Error Detection**:
   - As text changes, `KotlinCodeEngine.diagnoseKotlinSyntax` (and `CodeLanguageEngine` for other languages) evaluates the code.
   - It verifies balanced parentheses `()`, brackets `[]`, braces `{}`, and strings `""`/`''`.
   - Flags language-specific syntax discrepancies (e.g. typing `def` instead of `fun`, or using `new` in Kotlin).
2. **Visual Gutter & Inline Error Overlays**:
   - The gutter on the left shows a red `✕` on lines containing fatal errors and a yellow `!` for warnings.
   - Tapping an error reveals an interactive pill with a clear message and a **Quick Fix** action that automatically replaces or inserts the missing syntax.
3. **Compiler Simulation & Process Output**:
   - Pressing **Run ▶** checks whether errors exist. If clean, it runs the code simulation for the active language.
   - It captures standard output (`println()`, `print()`, `System.out.println()`, `std::cout`) and formats the output into a simulated macOS terminal session (`zsh • kotlinc ...`).
4. **AI-Assisted Troubleshooting**:
   - For semantic or logical bugs, the **AI Guide** window provides step-by-step breakdown and algorithmic suggestions using server-side Gemini AI.

---

## 3. User Experience & Visual Design

### Key User Flows

1. **Typing & Editing Flow**:
   - The user opens a code block and begins typing code or comments.
   - Gutter line numbers (`1.`, `2.`, `3.`) strictly align with each line of text regardless of system font scale.
   - Pressing Enter cleanly inserts an indented newline without duplicating whitespace or adding stray blank lines.
   - As tokens are typed (e.g. `val`, `fun`, `sealed`), a compact IDE popup floats directly beside the cursor, displaying filtered keyword and template options that can be accepted with a single tap or Tab press.
2. **Floating Window Resizing Flow**:
   - Tapping an island pill (e.g. `Explain §1` or `Files 2`) opens the floating window in its **Half** (Standard) tier.
   - Dragging the resize handle reveals the nearest discrete target tier (**Mini**, **Half**, **Two-Thirds**, or **Full**).
   - Releasing the handle snaps the window to the chosen tier using a smooth spring animation (`dampingRatio = 0.8f, stiffness = 400f`).
   - Quick tier selector pills on the window header allow one-tap switching between tiers without manual dragging.
3. **Home Page Browsing Flow**:
   - The user enters the app and sees an ambient, animated mesh wave background with gentle antigravity particles floating across the screen.
   - Folders display rich icon indicators for file counts, active languages, and hidden vault status.
   - The top navigation bar showcases the trichrome logo with glowing gradient depth.

### Visual Identity & Theme Tokens

- **Trichrome Logo Palette**:
  - Primary Accent: Neon Emerald (`#10B981`)
  - Secondary Accent: Electric Cyan (`#06B6D4`)
  - Tertiary Accent: Warm Amber (`#F59E0B`)
  - Background Base: Obsidian Trench (`#121110` / `#1C1917`)
- **Typography**:
  - Code & Editor: JetBrains Mono (`13.5.sp`, line height matched strictly via layout math)
  - Headers & Brand: Plus Jakarta Sans (Bold display weights)
- **Motion & Physics**:
  - Window Snap: High-stiffness spring curves (`stiffness = 380f`, `dampingRatio = 0.82f`)
  - Home Mesh Background: Continuous low-overhead Canvas shader simulation at 60fps

---

## 4. Key Product Decisions & Trade-Offs

### Decision 1: Gutter Metric Synchronization
- **Chosen Approach**: Compute the gutter line height directly from Compose `LocalDensity` using the exact line height of the `BasicTextField` style (`lineHeight = 22.sp`), and synchronize text measurement via `TextLayoutResult`.
- **Why**: Eliminates font-scaling drift on devices with non-default accessibility font sizes, guaranteeing that line 50 in the gutter sits on the exact same vertical pixel coordinate as line 50 of the code.

### Decision 2: Floating Cursor Popup vs. Bottom Toolbar
- **Chosen Approach**: Use Compose `codeLayoutResult.getBoundingBox(cursorOffset)` to calculate the exact on-screen position of the active text cursor, anchoring a floating completion popup offset by `+12.dp` horizontally and `-8.dp` vertically.
- **Why**: Fulfills the user's explicit request to make code completion look like a real desktop IDE rather than a disconnected mobile keyboard bar.

### Decision 3: 4 Discrete Window Presets (MIUI / iOS / HarmonyOS)
- **Chosen Approach**: Replace continuous free-form float dimensions with 4 discrete snap tiers (**Mini**, **Half**, **Two-Thirds**, **Full**). Users can drag to resize, but releasing always snaps to the closest tier.
- **Why**: Solves content cutoff completely because each tier is engineered to fit the specific contents of that window type without horizontal or vertical clipping.

---

## 5. Technical Architecture & Component Mapping

```
┌────────────────────────────────────────────────────────────────────────┐
│                        Jetpack Compose Root UI                         │
└───────────────────────────────────┬────────────────────────────────────┘
                                    │
          ┌─────────────────────────┴─────────────────────────┐
          ▼                                                   ▼
┌───────────────────────────────────┐       ┌───────────────────────────────────┐
│        DocumentListScreen         │       │       DocumentEditorScreen        │
│  (Modern Home Page with Mesh)     │       │     (Full-Screen IDE Canvas)      │
├───────────────────────────────────┤       ├───────────────────────────────────┤
│ • ModernFloatingMeshBackground    │       │ • Synchronized Gutter & Editor    │
│ • Trichrome Logo Header           │       │ • ContextualCursorCompletionPopup │
│ • Rich Language & Stats Symbols   │       │ • 4-Tier Snapping Floating Window │
│ • Quick-Access New File Actions   │       │ • Variable Clamped Split Screen   │
└───────────────────────────────────┘       └─────────────────┬─────────────────┘
                                                              │
                                            ┌─────────────────┴─────────────────┐
                                            ▼                                   ▼
                            ┌───────────────────────────────┐   ┌───────────────────────────────┐
                            │     KotlinCodeSectionCard     │   │   MacDraggableFloatingWindow  │
                            ├───────────────────────────────┤   ├───────────────────────────────┤
                            │ • BasicTextField + TextLayout │   │ • Discrete Tier Snap State    │
                            │ • Gutter Line Number Alignment│   │ • Spring Animated Dimensions  │
                            │ • Clean Soft-Keyboard Input   │   │ • Mini / Half / 2/3 / Full    │
                            │ • Floating Cursor Popup Menu  │   │ • Tailored Content Sizing     │
                            │ • Diagnostics & Quick-Fixes   │   │ • Run Output Terminal Window  │
                            └───────────────────────────────┘   └───────────────────────────────┘
```

### Component & State Mapping

1. **`KotlinCodeSectionCard.kt`**:
   - `codeLayoutResult`: Measures exact cursor coordinates (`getBoundingBox`).
   - `ContextualCursorCompletionPopup`: Renders a floating surface next to the cursor when suggestions are available.
   - Gutter synchronizer: Calculates exact pixel height per line to match font-scaled text.
   - Text filter: Normalizes incoming IME composing text and Enter keypresses to prevent spurious blank lines.
   - Inline Diagnostics & Quick-Fix actions.
2. **`MacFloatingIslandComponents.kt`**:
   - Add `WindowPresetTier` enum (`MINI`, `HALF`, `TWO_THIRDS`, `FULL`).
   - Add discrete dimension mapping per `FloatingWindowType`:
     - `EXPLANATION`: Mini (280×180), Half (380×280), Two-Thirds (460×420), Full (92%×88%).
     - `FOLDERS`: Mini (260×220), Half (340×340), Two-Thirds (440×480), Full (90%×85%).
     - `AI_STUDIO`: Mini (280×200), Half (360×320), Two-Thirds (460×450), Full (92%×88%).
     - `RUN_OUTPUT`: Mini (300×160), Half (420×240), Two-Thirds (520×380), Full (95%×85%).
   - Implement `detectDragGestures` with `onDragEnd` that calculates Euclidean distance to the nearest tier and animates smoothly using `animateDpAsState(spring(...))`.
3. **`ModernFloatingMeshBackground.kt` & `DocumentListScreen.kt`**:
   - Render the Google AI Studio ambient mesh background behind the folder list.
   - Integrate rich icons (`Code`, `FolderSpecial`, `Terminal`, `AutoAwesome`, `Memory`) for folder metadata.
4. **Logo & Asset Refresh**:
   - Implement a clean trichrome vector and drawable asset with layered gradient strokes (`#10B981`, `#06B6D4`, `#F59E0B`).

---

## 6. Verification Plan

1. **Compilation Check**:
   - Run `compile_applet` to ensure zero compilation or syntax errors across all Kotlin and Compose modules.
2. **Editor Typing Verification**:
   - Verify line gutter numbers stay strictly aligned with code text across all lines.
   - Verify typing at any position does not produce cascading blank lines.
   - Verify the autocomplete box pops up contextually directly beside the text cursor.
3. **Debugging Verification**:
   - Verify real-time syntax error pills appear with accurate line numbers and quick fixes.
   - Verify tapping **Run ▶** correctly compiles and executes code across all languages (Kotlin, Java, Python, Dart, C++, Rust), outputting to the Run Output terminal window.
4. **Window Snapping Verification**:
   - Test dragging and releasing a floating window; verify it snaps cleanly to the nearest of the 4 discrete tiers with spring physics.
5. **Home Page Aesthetics**:
   - Verify fluid mesh gradient background and rich developer icons on the document list screen.
