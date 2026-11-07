# KtScribe — Lightweight Text & Kotlin Code Section Editor

KtScribe is a fast, lightweight Android text editor built for developers and technical writers that seamlessly blends rich prose editing with auto-expanding Kotlin code blocks, paired explanation sections, rate-limited AI section naming, reliable local auto-saving, and automated GitHub CI testing.

## User Review & Critical Decisions

> [!IMPORTANT]
> The following core product decisions have been aligned based on your answers and requirements:

- **Confirmed Decision 1 — Local On-Device Storage (Room SQLite)**: All documents, rich text formatting, code blocks, explanations, and preferences are stored locally on-device using Room for instant startup and lightweight offline access.
- **Confirmed Decision 2 — Next-Block Triggered AI Section Naming + Rate Limiter**: When you finish writing a code block and its explanation and insert the *next* code block, the app automatically triggers AI section naming for the completed section. A built-in RPM rate-limiter (capped at 10 requests/min with content-hash caching) ensures calls stay safely within free-tier non-paid API limits.
- **Confirmed Decision 3 — 30-Second & Lifecycle Auto-Save**: Ongoing edits across all sections auto-save every 30 seconds, and immediately save whenever the app is minimized/closed (`ON_PAUSE` / `ON_STOP`) or when exiting a document back to the file list.
- **Confirmed Decision 4 — Customizable `Ctrl + [Letter]` Shortcut**: Pressing `Ctrl + K` (default) on a hardware/emulator keyboard—or tapping the toolbar Code Block button—inserts a new code block. You can change the shortcut letter (`Ctrl + any letter A–Z`) anytime in Settings.
- **Confirmed Decision 5 — Multi-Document Organization with Search**: A clean home screen provides a lightweight document list, instant search across document titles and code/explanation sections, and quick filtering.

---

## 1. Overview & Core Concept

- **What It Does**: KtScribe lets users write structured documents composed of rich prose and executable-style Kotlin code blocks. Every code block automatically pairs with the prose written below it (until the next code block) as a unified **Code + Explanation Section**.
- **Target Audience / Persona**: Kotlin & Android developers, students, and technical note-takers who want a lightweight editor that understands Kotlin syntax and automatically organizes code snippets and their explanations into titled sections.
- **Key Value**: Combines the speed of a lightweight text editor with IDE-grade Kotlin code block tools (syntax highlighting, autocomplete, one-tap Kotlin formatter, 4-line auto-expanding layout) and zero-friction AI section organization.

---

## 2. User Experience & Visual Design

### Key User Flows
1. **Document Library & Search**:
   - Launch into a clean document browser with a real-time search bar, recent edits timestamp, section count badges, and a floating action button to create a new document.
2. **Rich Text Editing**:
   - Write introductory prose at the top of a document with a lightweight formatting bar supporting **Font Family** selection (Sans, Serif, Display, Monospace bundled locally), **Font Size** (`12sp`–`28sp`), **Bold**, **Italic**, **Underline**, **Text Alignment**, and **Undo/Redo**.
3. **Inserting & Editing a Kotlin Code Block (`Ctrl + Letter` or Button)**:
   - Press `Ctrl + K` (or your custom chosen `Ctrl + <letter>`) or tap **+ Code Block** in the toolbar.
   - A newly created **Section Container** appears, visually separated from surrounding content by a toggleable section divider (which can be hidden or shown per section).
   - Inside the section, the **Kotlin Code Block** starts at exactly **4 lines of height** and expands smoothly line-by-line as you write more code.
   - As you type Kotlin code, live **Syntax Highlighting** colors keywords (`fun`, `val`, `var`, `class`, `suspend`, etc.), types, strings, comments, annotations, and numbers.
   - A contextual **Autocomplete Strip** suggests Kotlin keywords, standard library functions (`println`, `listOf`, `mapOf`, `coroutineScope`, `Flow`), and identifiers declared in your document.
   - Tapping **Format (`{ }`)** runs the built-in Kotlin formatter to normalize indentation, brace placement, and spacing.
4. **Writing the Paired Explanation & Triggering AI Naming**:
   - Directly below each code block is its bound **Explanation Editor** where you explain how the snippet works (using your chosen prose font, size, and styling).
   - The moment you insert the **next** code block, KtScribe detects that the previous Code + Explanation pair is complete and requests a concise, descriptive **Section Title** from Gemini (e.g., *"Coroutine Flow Debounce Operator"*), displaying a subtle status badge while respecting free-tier RPM limits. You can also tap the sparkle icon on any section header to name or rename it manually.
5. **Auto-Save & Export**:
   - While editing any section, a 30-second countdown auto-saves changes to the local Room database, and leaving the screen or minimizing the app flushes pending edits immediately.

### Visual Identity & Theme
- **Aesthetic Direction**: *Editorial Studio & Kotlin IDE* — crisp typographic hierarchy for prose paired with high-contrast, tactile code cards.
- **Color Palette & Mood (Dark & Light Mode)**:
  - **Light Mode**: Warm Paper background (`#FAF8F5`), Pure Alabaster cards (`#FFFFFF`), Deep Ink prose (`#18181B`), Kotlin Violet primary (`#7F52FF`), and Slate code surface (`#F1F5F9`).
  - **Dark Mode**: Obsidian Slate background (`#0F1117`), Elevated Carbon cards (`#181B24`), Crisp Ivory prose (`#F3F4F6`), Electric Kotlin Violet accent (`#9B72FF`), and Deep Terminal code surface (`#13161F`).
  - **Syntax Highlighting Palette**: Distinct semantic colors for Kotlin keywords (Violet/Magenta), strings (Emerald), comments (Muted Slate italic), numbers/constants (Amber), and function names (Sky Blue).
- **Typography & Hierarchy**:
  - Locally bundled Google Fonts via `font-util`: **Plus Jakarta Sans** (UI & headings), **Merriweather** (optional editorial serif prose), **Roboto** (clean sans prose), and **JetBrains Mono** (Kotlin code blocks and line numbers).
- **Interactive Feedback & Motion**:
  - Smooth spring height expansion as code blocks grow beyond 4 lines, subtle animated section dividers, and clear save-state indicators in the top bar.

---

## 3. Key Product Decisions & Trade-Offs

- **Decision 1: Structured Section Model (Intro Prose + List of `[CodeBlock + Explanation]` Sections)**
  - *Chosen Approach*: Represent each document as an optional introductory rich-text area followed by an ordered list of logical `EditorSection` items, where each section encapsulates its `codeContent`, `explanationContent`, `sectionTitle`, `showDivider` flag, and text styling metadata.
  - *Why*: Guarantees the requirement that *"after each code block is written, the next text (till the next code block) must be considered as part of the code block + its explanation"*, making AI naming, section reordering, divider toggling, and auto-saving deterministic.
- **Decision 2: Free-Tier Safe AI Rate Limiter (Token Bucket + Content Hashing)**
  - *Chosen Approach*: Trigger AI section naming via Firebase AI Logic (`gemini-3.5-flash-lite`) when the subsequent code block is created, guarded by a client-side sliding-window rate limiter (max 10 requests per 60 seconds, queued automatically if exceeded) and a SHA-256 content hash check so unchanged code+explanation pairs never consume extra quota.
  - *Why*: Prevents `429 RESOURCE_EXHAUSTED` errors on Google AI Studio's free tier while keeping section naming automatic and effortless.
- **Decision 3: Updatable APK Configuration & GitHub Actions CI**
  - *Chosen Approach*: Preserve consistent `applicationId` and deterministic debug/release `signingConfigs` in `app/build.gradle.kts`, and include a ready-to-run `.github/workflows/android-ci.yml` pipeline that runs unit/Robolectric tests and builds the debug APK on every push/PR.
  - *Why*: Ensures re-installing after debugging updates the existing app on device without package signature conflicts, and enables automated testing and verification on GitHub.

---

## 4. Technical Architecture & Data Strategy *(Technical Reference)*

### Architecture & Component Diagram
```
┌───────────────────────────────────────────────────────────────────────────┐
│                              MainActivity                                 │
│  • Firebase App Check Init   • Hardware Key Intercept (Ctrl + <Letter>)   │
│  • Lifecycle Observer (ON_STOP / ON_PAUSE Immediate Auto-Save Flush)      │
└────────────────────────────────────┬──────────────────────────────────────┘
                                     │
                                     ▼
┌───────────────────────────────────────────────────────────────────────────┐
│                        EditorViewModel (StateFlow)                        │
│  • Document List & Search State      • Active Document & Sections State   │
│  • 30s Coroutine Auto-Save Ticker    • Shortcut Key & Theme Preferences   │
└───────────┬────────────────────────┴──────────────────────┬───────────────┘
            │                                               │
            ▼                                               ▼
┌──────────────────────────────────────┐     ┌──────────────────────────────┐
│      DocumentRepository (Room)       │     │   AiSectionNamingService     │
│  • DocumentEntity & SectionEntity    │     │  • Firebase.ai (Flash-Lite)  │
│  • Transactional Upsert & Search     │     │  • 10 RPM Sliding Queue      │
│  • Preferences (Theme, Ctrl+Key)     │     │  • Content-Hash Cache        │
└──────────────────────────────────────┘     └──────────────────────────────┘
            │
            ▼
┌───────────────────────────────────────────────────────────────────────────┐
│                         Jetpack Compose UI Layer                          │
│  ┌─────────────────────────┐   ┌───────────────────────────────────────┐  │
│  │  DocumentListScreen     │   │  DocumentEditorScreen                 │  │
│  │  • Search & Filter Bar  │   │  • Top Bar (Save Status, Shortcut,    │  │
│  │  • Document Cards       │   │    Theme Toggle, Export)              │  │
│  │  • New Document FAB     │   │  • Rich Text Formatting Toolbar       │  │
│  └─────────────────────────┘   │  • Intro Text Editor                  │  │
│                                │  • SectionCardList (LazyColumn):      │  │
│                                │    ├─ Toggleable Section Divider      │  │
│                                │    ├─ AI Section Title Header         │  │
│                                │    ├─ KotlinCodeBlockEditor           │  │
│                                │    │  (4-line min, auto-expand,       │  │
│                                │    │   syntax highlight, autocomplete,│  │
│                                │    │   Kotlin formatter)              │  │
│                                │    └─ Paired Explanation Rich Editor  │  │
│                                └───────────────────────────────────────┘  │
└───────────────────────────────────────────────────────────────────────────┘
```

### Data Model & State
- **`DocumentEntity` (Room Table `documents`)**:
  - `id: Long`, `title: String`, `introText: String`, `introStyleJson: String`, `updatedAt: Long`, `createdAt: Long`.
- **`CodeSectionEntity` (Room Table `code_sections`)**:
  - `id: Long`, `documentId: Long`, `orderIndex: Int`, `sectionTitle: String`, `isAiNamed: Boolean`, `lastNamedHash: String`, `codeContent: String`, `explanationText: String`, `explanationStyleJson: String`, `showDivider: Boolean`.
- **`EditorPreferences`**:
  - `shortcutLetter: Char` (default `'K'`), `themeMode: ThemeMode` (`SYSTEM`, `LIGHT`, `DARK`), `defaultFontFamily: EditorFontFamily`, `defaultFontSizeSp: Int`.

### Interactive Component & State Mapping
- **Hardware Shortcut Handler (`Modifier.onPreviewKeyEvent`)**: Listens for `KeyEventType.KeyDown` where `isCtrlPressed` is true and the pressed key matches the user's configured `shortcutLetter` (or any letter chosen in the Shortcut Picker dialog). Immediately inserts a new `CodeSection` after the active section, triggers AI naming on the just-completed previous section, and shifts focus to the new Kotlin code block.
- **Kotlin Code Block Component**:
  - Uses a custom `VisualTransformation` for zero-lag Kotlin syntax highlighting (keywords, strings, comments, numbers, types, annotations, functions).
  - Calculates line count (`max(4, lineCount)`) so the editor starts at 4 lines high and expands automatically with each newline.
  - Displays a live autocomplete bar above/below the active code line matching the current token prefix against Kotlin keywords, standard library snippets, and user-defined identifiers.
  - Includes a **Format Kotlin** action that parses brace depth, fixes 4-space indentation, and normalizes operator/keyword spacing.
- **30s & Lifecycle Auto-Save**:
  - A background coroutine loop inside `EditorViewModel` checks a `isDirty` flag every 30 seconds and persists all modified sections to Room.
  - `LifecycleEventObserver` on `ON_PAUSE`/`ON_STOP` and screen back-navigation invoke `saveNow()` synchronously via `viewModelScope` so zero keystrokes are ever lost.
- **GitHub Actions CI (`.github/workflows/android-ci.yml`)**:
  - Triggered on `push` and `pull_request` across branches.
  - Sets up JDK 17 and Gradle cache, ensures `debug.keystore` is materialized from `debug.keystore.base64` if needed, executes `./gradlew testDebugUnitTest assembleDebug` (or `gradle testDebugUnitTest assembleDebug`), and uploads test reports and the debug APK artifact.
