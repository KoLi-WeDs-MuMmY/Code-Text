# KtScribe IDE: Dynamic Themes, Multi-Language File Types, Dual Settings, Floating Window Presets & Clamped Split Screen

Comprehensive implementation plan to resolve theme application across all UI components, add support for Kotlin, Java, Python, Dart, C++, and Rust with unique file extensions and distinctive color badges, implement separate Main Settings vs. Intra-File Settings, introduce dual default dimensions (horizontal and vertical) with interactive corner resizing and minimum size protection for floating windows, and enable variable split-screen resizing clamped between 25% and 75%.

---

## User Review & Critical Decisions

> [!IMPORTANT]
> User requirements and choices confirmed during clarification:
> - **Programming Languages & Extensions**: Expand beyond Kotlin (`.ktscribe` / `.kt`), Java (`.java`), Python (`.py`), and Dart (`.dart`) to also include **C++ (`.cpp`)** and **Rust (`.rs`)**, each with distinct file extension badges, directory paths (`src/cpp/`, `src/rust/`), unique accent color codes, and dedicated syntax engines and snippets.
> - **Dual Settings Architecture**:
>   - **Main Page Settings**: Global studio theme (Darcula Espresso, macOS Midnight, Monokai Pro, GitHub Dark, Solarized Dark, Nord Frost), dark/light/system mode toggle, default new file language, code block keyboard shortcut (`Ctrl + [Letter]`), and API/quota monitoring.
>   - **Intra-File Settings**: File-specific theme override (can inherit or override studio theme), file name & extension customization, target directory assignment, AI naming & explanation triggers, tab indent configuration (2 vs 4 spaces), line numbers toggle, and file export actions (.ktscribe bundle, clean raw code, or markdown).
> - **Floating Window Sizing & Drag Interaction**:
>   - Every floating window has **2 default size presets**: **Horizontal (Wide Landscape)** and **Vertical (Tall Portrait)** with strict minimum width and height limits (preventing content cutoff).
>   - Corner and edge drag interaction allows fluid resizing above default minimums, and dragging dynamically snaps/adjusts to horizontal or vertical orientation based on whether width > height.
> - **Variable Split-Screen Ratio**:
>   - Replace static 50:50 split with a variable split-screen divider clamped strictly between **25% (0.25f)** and **75% (0.75f)**, featuring a macOS-inspired tactile drag handle bar with quick ratio snaps (25%, 50%, 75%).

---

## 1. Overview & Core Concept

**KtScribe** is a lightweight, high-aesthetic developer environment designed with macOS floating island and floating window aesthetics. Each file represents a single code block with an associated explanation/concept block, AI-assisted naming, and real-time execution preview.

### Core Capabilities:
1. **Dynamic Theme Application**: Full reactive integration of 6 curated studio color schemes (`DARCULA_ESPRESSO`, `MACOS_MIDNIGHT`, `MONOKAI_PRO`, `GITHUB_DARK`, `SOLARIZED_DARK`, `NORD_FROST`) across all floating windows, backgrounds, cards, dividers, and typography, removing hardcoded espresso hex values.
2. **Multi-Language Architecture**: 6 first-class programming languages with unique file extensions, syntax highlighting tokens, language-specific directory structures, autocomplete suggestions, and execution output simulation:
   - **Kotlin**: `.ktscribe` / `.kt` (`src/kotlin/`) — Amber/Orange (`#D4883A`)
   - **Java**: `.java` (`src/java/`) — Coral/Terracotta (`#E76F51`)
   - **Python**: `.py` (`src/python/`) — Steel Blue (`#457B9D`)
   - **Dart**: `.dart` (`src/dart/`) — Teal Emerald (`#2A9D8F`)
   - **C++**: `.cpp` (`src/cpp/`) — Royal Violet (`#8338EC`)
   - **Rust**: `.rs` (`src/rust/`) — Rust Crimson (`#E63946`)
3. **Dedicated Main Page Settings vs. Intra-File Settings**: Clear separation of concerns between global studio configuration and intra-file actions.
4. **Adaptive Floating Windows with 2 Default Presets**:
   - **Horizontal Preset (Wide)**: Optimized for code reading and terminal output (e.g. 380dp x 280dp).
   - **Vertical Preset (Tall)**: Optimized for file browsing and explanation note-taking (e.g. 320dp x 420dp).
   - Interactive bottom-right and corner drag handles with minimum bounds preventing element cutoff.
5. **Clamped Variable Split-Screen**: Draggable divider smoothly clamped between 25% and 75% for custom code-to-notes or code-to-terminal ratios.

---

## 2. User Experience & Visual Design

### Key User Flows

```
┌────────────────────────────────────────────────────────────────────────┐
│ Main Page (Folder / Document List)                                    │
│ ├─ Modern Animated Floating Mesh Particle Background (Theme-reactive) │
│ ├─ macOS Search & Filter Bar                                          │
│ ├─ List of Document Folders with Language Badges (KT, PY, CPP, RS...) │
│ └─ Main Studio Settings Button [⚙]                                    │
│     └─ Modal: Global Theme, Appearance Mode, Shortcuts, Quotas         │
└────────────────────────────────────┬───────────────────────────────────┘
                                     │ (Tap folder to open)
                                     ▼
┌────────────────────────────────────────────────────────────────────────┐
│ Intra-File Editor Workspace                                           │
│ ├─ Top Island: Folder breadcrumb, Language indicator, Status pills    │
│ ├─ Fullscreen Single Code Block with Syntax Highlighting & Line Numbers│
│ ├─ macOS Action Island & Floating Window Toggles:                     │
│ │   [Explain] [Files] [AI Studio] [Run ▶] [File Settings ⚙]           │
│ ├─ Floating Windows (Draggable & Corner-Resizable):                    │
│ │   ├─ Enforced minimum bounds (no content clipping)                  │
│ │   ├─ Horizontal Default vs. Vertical Default presets                │
│ │   └─ [◫ Dock to Split] action                                      │
│ └─ Variable Split-Screen (Docked Mode):                                │
│     └─ Draggable split divider bar (Clamped strictly 25% – 75%)        │
└────────────────────────────────────────────────────────────────────────┘
```

### Visual Identity & Theme Tokens

- **Dynamic Theming Integration**:
  - Replace hardcoded hex values in `MacFloatingIslandComponents.kt`, `KotlinCodeSectionCard.kt`, and `DocumentEditorScreen.kt` with `MaterialTheme.colorScheme` tokens:
    - `surface`: Floating window bodies, card backgrounds, dialogs.
    - `surfaceVariant`: Title bars, secondary pills, inactive resize handles.
    - `primary`: Active pills, focus borders, primary actions, keyword highlighting.
    - `outline` / `outlineVariant`: Specular bevels and separation borders.
    - `background`: Overall canvas and mesh background gradient anchors.
- **Language Badges & Accent Colors**:
  - Each language displays a distinct chip in file headers, directory trees, and autocomplete menus:
    - Kotlin: `#D4883A`
    - Java: `#E76F51`
    - Python: `#457B9D`
    - Dart: `#2A9D8F`
    - C++: `#8338EC`
    - Rust: `#E63946`

---

## 3. Key Product Decisions & Trade-Offs

- **Decision 1: Dynamic Theme Color Propagation**:
  - *Approach*: Bind all window chrome, traffic light borders, pill menus, and dialog backgrounds to `MaterialTheme.colorScheme` and `LocalKotlinSyntaxColors.current`. When a theme is selected (or overridden per file), the entire UI immediately recalculates and reflects the new palette without requiring an app reload.
  - *Why*: The previous implementation had hardcoded `#1B1A19` and `#11100F` color literals in UI components, making the theme picker ineffective.
- **Decision 2: C++ & Rust Integration with Autocomplete & Tokenization**:
  - *Approach*: Add `CPP` (`.cpp`, `src/cpp/`) and `RUST` (`.rs`, `src/rust/`) to `LanguageType` enum, define their keywords and snippets in `CodeLanguageEngine`, and implement token regexes for syntax highlighting and simulated execution.
  - *Why*: Supports the user's expanded language specifications natively.
- **Decision 3: Floating Window Corner Dragging & Aspect Ratio Snap**:
  - *Approach*: Provide both a corner resize handle and a quick preset toggle. As the user drags the corner, if `width > height * 1.15`, the window adopts horizontal aspect metrics; if `height > width * 1.15`, it adopts vertical aspect metrics. Both dimensions are strictly clamped at or above `minWidthDp` and `minHeightDp`.
  - *Why*: Guarantees that terminal output and explanation text are never truncated due to an accidental micro-window collapse.
- **Decision 4: Variable Split Clamping (25% to 75%)**:
  - *Approach*: Allow dragging the split divider with an interactive gesture listener that calculates `ratio = (dragPosition / totalDimension).coerceIn(0.25f, 0.75f)`, alongside quick-snap pill buttons for 30%, 50%, and 70%.
  - *Why*: Prevents collapsing either side into an unreadable state while giving users flexible room to view code and notes simultaneously.

---

## 4. Technical Architecture & Data Strategy

### Component Hierarchy & Interaction Mapping

```
┌────────────────────────────────────────────────────────────────────────┐
│                             MainActivity                               │
│  - Observes uiState.activeAppTheme & uiState.themeMode                 │
│  - Wraps tree in KtScribeTheme(activeTheme, themeMode)                │
└───────────────────────────────────┬────────────────────────────────────┘
                                    │
          ┌─────────────────────────┴─────────────────────────┐
          ▼                                                   ▼
┌───────────────────────────┐                       ┌───────────────────────────┐
│    DocumentListScreen     │                       │   DocumentEditorScreen    │
│ - Global Mesh Background  │                       │ - Main Code Editor Canvas │
│ - Search & Folder Cards   │                       │ - Top Island Status Bar   │
│ - Main Studio Settings [⚙]│                       │ - Intra-File Settings [⚙] │
│   (Global Theme, Mode,    │                       │ - Floating Window Manager │
│    Shortcuts, Quotas)     │                       │ - Variable Split Screen   │
└───────────────────────────┘                       └─────────────┬─────────────┘
                                                                  │
                                            ┌─────────────────────┴─────────────────────┐
                                            ▼                                           ▼
                             ┌──────────────────────────────┐            ┌──────────────────────────────┐
                             │    MacFloatingWindowItem     │            │    Variable Split Layout     │
                             │ - Default Horizontal/Vertical│            │ - Clamped 0.25f to 0.75f     │
                             │ - Bottom-Right Corner Drag   │            │ - Tactile macOS Drag Bar     │
                             │ - Enforced minWidth/minHeight│            │ - Side-by-Side or Bottom     │
                             └──────────────────────────────┘            └──────────────────────────────┘
```

### Data Model Enhancements

1. **`LanguageType` enum**:
   - `KOTLIN`: `.ktscribe`, `#D4883A`, `src/kotlin/`
   - `JAVA`: `.java`, `#E76F51`, `src/java/`
   - `PYTHON`: `.py`, `#457B9D`, `src/python/`
   - `DART`: `.dart`, `#2A9D8F`, `src/dart/`
   - `CPP`: `.cpp`, `#8338EC`, `src/cpp/`
   - `RUST`: `.rs`, `#E63946`, `src/rust/`
2. **`FloatingWindowType` enum**:
   - Add explicit `defaultHorizontalWidthDp`, `defaultHorizontalHeightDp`, `defaultVerticalWidthDp`, `defaultVerticalHeightDp`, `minWidthDp`, `minHeightDp`.
3. **`EditorViewModel`**:
   - `updateSplitScreenRatio(ratio: Float)`: Clamps strictly between `0.25f` and `0.75f`.
   - `setFloatingWindowOrientation(type, isHorizontal)`: Applies default dimensions.
   - Separate state handlers for Main Studio Settings dialog vs Intra-File Settings dialog.

---

## 5. Planned Verification Steps

1. **Compilation Check**: Verify successful compilation via `compile_applet`.
2. **Theme Switching Verification**: Test switching between all 6 themes and confirm that backgrounds, window borders, text, and syntax highlighting transform reactively.
3. **Language & Extension Verification**: Create and edit files in Kotlin, Java, Python, Dart, C++, and Rust; confirm unique color badges, syntax highlighting, and directories.
4. **Settings Dialog Verification**: Verify Main Page Settings (global theme, shortcuts, mode) vs Intra-File Settings (language, file theme override, export, formatting).
5. **Floating Window & Split Screen Verification**: Test horizontal/vertical defaults, corner resizing constraints, and 25%-75% variable split dragging.
