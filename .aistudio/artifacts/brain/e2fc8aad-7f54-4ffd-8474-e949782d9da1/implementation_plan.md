# KtScribe IDE — Multi-Language Studio, Docked Non-Colliding Completion, 6 Aesthetic Themes & Window Reflow

This update addresses all 6 core enhancements requested by the user, incorporating the three confirmed design choices from Phase 1:
- **No-Collision Code Suggestions & Syntax Errors**: Suggestions dock directly into a dedicated bar above the bottom 8-key shortcut bar, and syntax errors appear as clickable gutter badges and a docked status pill, ensuring the active typing line is never covered.
- **Proportional Zoom & Responsive Reflow for Floating Windows**: Floating windows smoothly reflow their internal content with proportional font scaling so that every control, chip, and code preview remains legible and accessible regardless of window size.
- **Main Screen Settings with 6 IDE Themes & Color Code Palettes**: A dedicated settings dialog accessible directly from the home screen featuring 6 rich, non-neon themes with live color swatch palettes and per-file theme overrides.
- **Folder and File Renaming**: In-place renaming for both workspace folders and individual code files.
- **Multi-Language Architecture**: Native support for **Kotlin (`.kt` / `.ktscribe`)**, **Java (`.java`)**, **Python (`.py`)**, and **Dart (`.dart`)**, with dedicated tokenizers, syntax highlighters, snippets (`sout`, `psvm`, `def`, `class`), and simulation runners.

---

## User Review & Critical Decisions

> [!IMPORTANT]
> The three design clarifications confirmed by the user have been prioritized:

- **Confirmed Decision 1 — Non-Colliding Suggestion & Error UI**:
  - *Chosen Approach*: Suggestions are docked in a sleek horizontal prediction bar situated immediately above the constant 8-key bottom bar. Syntax errors are displayed as gutter indicators (`•` / `✕`) next to the affected line number and a compact status pill in the pinned header.
  - *Why*: Eliminates popups covering the active code line while preserving instant tap access.
- **Confirmed Decision 2 — Floating Window Resizing & Proportional Reflow**:
  - *Chosen Approach*: Floating windows use container-based responsive scaling (`BoxWithConstraints`) with dynamic font sizing (`10.sp`–`13.sp`) and wrapped flow layouts so text and buttons never get clipped, squeezed, or hidden.
- **Confirmed Decision 3 — 6 Themes with Palettes & File Overrides**:
  - *Chosen Approach*: A theme studio in the main screen Settings providing 6 themes:
    1. **Darcula Espresso** (Default warm matte amber/brown)
    2. **macOS Midnight** (Deep space blue & frosted steel)
    3. **Monokai Pro** (Muted charcoal with warm pastel accents)
    4. **GitHub Dark High-Contrast** (Obsidian with slate & blue)
    5. **Solarized Dark** (Deep teal-cyan with warm ochre)
    6. **Nord Frost** (Arctic night blue-grey with polar accents)
    Each theme shows its hex color palette codes, and individual files can override the active theme.

---

## 1. Aesthetic Enhancements (What Will Change First)

Before code changes, we establish the refined visual system:
1. **Pristine Apple cmd / Terminal Matte Hierarchy**:
   - Complete removal of remaining high-contrast borders and neon tints.
   - Refined 3-dot macOS window headers (`#FF5F56`, `#FFBD2E`, `#27C93F`) with subtle embossed rims.
   - Clean micro-beveled borders (`1.dp` solid `#2E2A27` or theme-specific border tokens).
2. **Unified Typography & Metrics**:
   - `JetBrains Mono` as the primary code font paired with `Plus Jakarta Sans` for UI labels.
   - Distinctive color-coded badges for languages: `[KT]` Amber, `[JAVA]` Crimson/Coral, `[PY]` Emerald/Teal, `[DART]` Sky Blue.
3. **Breathing Room & Ergonomics**:
   - Generous margins between the left rail, the code editor canvas, and floating windows.
   - Floating window drag handles and close buttons enlarged to standard 48dp touch targets while visually maintaining a compact, elegant footprint.

---

## 2. Multi-Language & File Type Engine

We expand `LanguageType` enum and engine support:
- **Languages Supported**:
  - **Kotlin (`.kt` / `.ktscribe`)**: Full existing support + coroutines, sealed classes, and `sout` expansion.
  - **Java (`.java`)**: Keywords (`public`, `class`, `static`, `void`, `int`, `boolean`, `import`), snippets (`psvm` $\rightarrow$ `public static void main`, `sout` $\rightarrow$ `System.out.println()`).
  - **Python (`.py`)**: Keywords (`def`, `class`, `import`, `from`, `if`, `elif`, `else`, `return`, `None`, `True`, `False`), snippets (`pr` $\rightarrow$ `print()`, `def` $\rightarrow$ `def func():`, `main` $\rightarrow$ `if __name__ == '__main__':`).
  - **Dart (`.dart`)**: Keywords (`void`, `main`, `class`, `Widget`, `build`, `async`, `await`, `final`, `const`), snippets (`pr` $\rightarrow$ `print()`, `stless` $\rightarrow$ StatelessWidget skeleton).
- **Per-Language Syntax Highlighting & Diagnostics**:
  - Extensible tokenizer in `CodeLanguageEngine` providing language-specific keywords, comments, string literals, and type identifiers.
  - File extension badges and icons displayed in the file tabs, switcher bar, and folder cards.

---

## 3. Non-Colliding Autocomplete & Gutter Errors

```
┌────────────────────────────────────────────────────────────────────────┐
│ • • •  Main.ktscribe [Kotlin]              [Theme: Darcula] [↶ Undo]   │
├─────┬──────────────────────────────────────────────────────────────────┤
│  1  │ fun main() {                                                     │
│  2  │     val count = 42                                               │
│  3✕ │     val badString = "unclosed                                    │
│  4  │     pri▌  <-- Active line stays completely clear and visible!    │
├─────┴──────────────────────────────────────────────────────────────────┤
│ [Docked Suggestion Bar: println("") (fn) | print() (fn) | private (kw)]│
├────────────────────────────────────────────────────────────────────────┤
│ [ Tab ] [ ( ) ] [ { } ] [ " " ] [ -> ] [  .  ] [  =  ] [  :  ]         │
└────────────────────────────────────────────────────────────────────────┘
```

1. **Docked Suggestion Bar**:
   - When suggestions exist, a sleek horizontal suggestion strip appears directly above the 8-key bottom bar.
   - Tapping any suggestion instantly inserts it at the cursor without ever covering the line the user is typing.
2. **Gutter Error Badges & Clickable Diagnostics**:
   - Syntax errors render a small marker (`✕` or red dot) in the line-number gutter on the exact line.
   - Tapping the gutter icon reveals the error message and one-tap quick fix in a non-obtrusive tooltip or in the bottom status panel.

---

## 4. Responsive Floating Windows (Proportional Scaling & Reflow)

1. **Fluid Responsive Reflow**:
   - Wrap controls and chips inside `FlowRow` and responsive `Column`/`Row` layouts with `BoxWithConstraints`.
   - Prevent text clipping: font sizes dynamically calculate between `10.sp` and `13.sp` depending on available width.
2. **No-Scroll Clutter**:
   - Buttons scale to compact variants (e.g. icon-only with tooltips or shortened labels) when window width shrinks below `280.dp`.
   - All critical details remain accessible regardless of how the user resizes the window.

---

## 5. Renaming Folders & Files

1. **Folder Renaming**:
   - Available from the folder card on the main screen (overflow menu / edit icon) and inside the editor top bar.
2. **File Renaming**:
   - Tapping the file title or selecting "Rename File" from the file dropdown or tab allows changing both the name and switching file extension (`.kt`, `.ktscribe`, `.java`, `.py`, `.dart`).

---

## 6. Technical Architecture & File Plan

```
┌────────────────────────────────────────────────────────────────────────┐
│                          ThemeRepository                               │
│  • 6 App Themes: Darcula, Midnight, Monokai, GitHub Dark, Solarized,   │
│    Nord Frost                                                          │
│  • Theme Palette Swatches & Hex Codes                                  │
│  • Global App Theme + Per-File / Per-Folder Override                   │
└──────────────────────────────────┬─────────────────────────────────────┘
                                   │
┌──────────────────────────────────┴─────────────────────────────────────┐
│                       CodeLanguageEngine                               │
│  • Multi-language tokenization: Kotlin, Java, Python, Dart             │
│  • Snippet expansions: sout, psvm, def, pr                             │
│  • Non-colliding docked suggestion provider                            │
└──────────────────────────────────┬─────────────────────────────────────┘
                                   │
┌──────────────────────────────────┴─────────────────────────────────────┐
│                          UI Layer                                      │
│  • DocumentListScreen: Settings button, Theme picker & Palette sheet   │
│  • DocumentEditorScreen: Docked suggestion bar, Rename dialogs         │
│  • KotlinCodeSectionCard: Gutter errors, multi-language highlight      │
│  • MacDraggableFloatingWindow: Proportional zoom & responsive reflow   │
└────────────────────────────────────────────────────────────────────────┘
```

- **`com/example/model/EditorModels.kt`**:
  - Add `LanguageType` enum (`KOTLIN`, `JAVA`, `PYTHON`, `DART`).
  - Add `AppThemeId` enum (6 themes) and `ThemePaletteDefinition` with explicit hex codes.
  - Update `CodeSectionUiModel` to include `language: LanguageType` and optional `themeOverride: AppThemeId?`.
- **`com/example/code/CodeLanguageEngine.kt`**:
  - Unified multi-language syntax highlighter, token analyzer, and docked suggestion provider.
- **`com/example/ui/theme/Theme.kt` & `Color.kt`**:
  - Implement full color schemes and syntax palettes for all 6 themes.
- **`com/example/ui/screens/DocumentListScreen.kt`**:
  - Add Settings button in TopBar with 6-theme picker, palette viewer, and folder renaming options.
- **`com/example/ui/screens/DocumentEditorScreen.kt`**:
  - Docked suggestion bar, file renaming dialog, language switcher, and per-file theme picker.
- **`com/example/ui/components/MacFloatingIslandComponents.kt`**:
  - Responsive reflow and proportional zoom inside `MacDraggableFloatingWindow`.
