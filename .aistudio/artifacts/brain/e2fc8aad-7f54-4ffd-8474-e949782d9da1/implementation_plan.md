# KtScribe — macOS Dynamic Island & Full-Screen Apple `cmd` Kotlin IDE

KtScribe transforms into a professional, folder-driven Kotlin IDE and technical note studio featuring a full-screen Apple macOS Terminal (`cmd`) code editor, a top-center **Floating Dynamic Island** with resizable drop-down macOS windows, a **Draggable & Resizable Explanation Window** with a **Split-Screen Two-Window Mode**, inline IDE abbreviation expansions (`sout` → `println()`), first-launch **Hidden Vault Recovery**, and conflict-free **APK Updates**.

---

## User Review & Critical Decisions

> [!IMPORTANT]
> The following architectural and UX decisions incorporate all of your answers and visual references:

- **Confirmed Decision 1 — Automatic Hidden Vault & First-Launch Recovery (`xyz` / `.ktscribe_vault`)**: On first launch, KtScribe prompts for local vault storage permission and checks if a previous backup vault exists on the device. If detected, a recovery modal lets you **Recover Previous Folders** or **Initialize a Fresh Vault**. All vault snapshots are stored in an obfuscated, Base64+XOR encoded binary structure inside a hidden directory so they are inaccessible to casual browsing.
- **Confirmed Decision 2 — Keep Data on Uninstall + Conflict-Free APK Updates**:
  - **Uninstall Prompt**: Declares `android:hasFragileUserData="true"` on the application manifest so Android presents the system checkbox asking whether to keep or delete your local app data when uninstalling.
  - **Zero Update Conflicts**: Commits the deterministic `debug.keystore.base64` signature and increments `versionCode` so every GitHub Actions build and local build is signed with the exact same certificate—eliminating the *"package conflicts with an existing package"* error on update.
- **Confirmed Decision 3 — Folders-Only Workspace Switched via Dynamic Island**: Flat files are replaced by **Folders** (projects/modules). Each Folder holds multiple ordered Kotlin code sections and concept tags that you switch between or search from the top Floating Dynamic Island.
- **Confirmed Decision 4 — Full-Screen Apple `cmd` Code Editor + Resizable Floating & Split-Screen Explanation Window**:
  - The Kotlin Code Editor now occupies the **full screen** with an authentic Apple macOS Terminal (`cmd`) aesthetic and automatically resizes above the on-screen keyboard so your active cursor line is always visible.
  - The paired **Explanation Block** operates in two user-toggleable modes:
    1. **Floating macOS Window Overlay**: A draggable, resizable macOS window (`● ● ●` traffic lights + drag-to-resize corner/bottom handle) floating over the full-screen code editor.
    2. **Split-Screen Two-Window Mode ("Set Aside")**: Divides the workspace into two side-by-side or stacked resizable macOS windows (Code Window + Explanation Window) with a draggable split divider so you can resize both panes freely.
- **Confirmed Decision 5 — Streamlined Live IDE Abbreviations (No Snippet Overload)**: Replaces the cluttered snippet bar with intelligent live postfix/abbreviation popups (`sout` → `println()`, `main` → `fun main()`, `fori` → `for (i in 0 until n)`, `ifn` → `if (x == null)`, `inn` → `if (x != null)`, `tryc` → `try / catch`) alongside real-time syntax error detection.

---

## 1. Overview & Core Concept

- **What It Does**: KtScribe provides a distraction-free, full-screen Apple `cmd` Kotlin code editor controlled from a macOS-inspired **Floating Dynamic Island** at the top of the display. Every option clicked in the Dynamic Island—Folder Explorer, Command Palette, Section Navigator, AI Concept Studio, or Terminal Runner—drops down as a smooth, resizable macOS floating window.
- **Target Audience / Persona**: Kotlin developers, students, and software engineers who want a desktop-class macOS floating-window workflow on Android with instant keyboard adaptation, live abbreviation completions, and safe local data recovery across app updates or reinstalls.
- **Key Value**: Delivers maximum screen real estate for writing code while keeping explanations, AI deep-dives, command search, and folder navigation one tap away inside resizable floating or split-screen windows.

---

## 2. User Experience & Visual Design

### Key User Flows
1. **First-Launch Storage Permission & Hidden Vault Recovery**:
   - On first launch, KtScribe displays a sleek macOS-styled **Vault Storage & Recovery Gate** requesting permission to initialize and scan the local hidden vault (`.ktscribe_vault_xyz`).
   - If encoded vault data from a previous installation or session is found, the user can choose **Recover Previous Folders & Code** (restoring all folders, sections, explanations, and AI concept tags) or **Create Fresh Vault**.
2. **Top-Center Floating Dynamic Island & Resizable Drop-Down Windows**:
   - Anchored at the top center of the screen is the **Floating Dynamic Island** pill showing:
     - Active Folder icon & name (`📁 Coroutines Lab`)
     - Active Section & AI Concept Tag (`#1 • Structured Concurrency`)
     - Live Syntax Health indicator (`✓ 0 Errors` or `⚠ 2 Syntax Issues`)
     - Quick triggers for **Command Palette (`⌘P`)**, **Explanation Window**, **Run (`▶`)**, and **AI Studio (`✨`)**.
   - Tapping any item in the Dynamic Island drops down a translucent **macOS Floating Window** with a bottom-right and bottom-edge **Resize Handle** so the user can adjust its width and height interactively:
     - **Command Palette Dropdown** (modeled after your reference image): Search bar (`"Run a command, jump to a folder, section, or symbol..."`) with filter pills (`All`, `Commands`, `Folders`, `Symbols`, `Sections`) and instant keyboard/tap execution.
     - **Folders & Sections Explorer Dropdown**: Switch folders, create new folders, reorder or add code sections (`Ctrl + K`), or jump to any learned AI concept tag.
     - **AI Deep-Dive & Concept Tag Dropdown**: Generates thorough step-by-step explanations, time/space complexity, best practices, and automatically names the section's **Learned Concept Tag** while staying within free-tier RPM limits.
3. **Full-Screen Apple `cmd` Code Editor & Live IDE Assistance**:
   - The Kotlin editor fills the workspace with an authentic macOS Terminal aesthetic (`● ● ●` window controls, shell path header `ktscribe — -zsh — kotlin`, line numbers, active-line spotlight, and indent guides).
   - **Keyboard-Aware Viewport Resizing**: When the on-screen keyboard opens, the full-screen editor dynamically shrinks its bottom bounds (`imePadding` + automatic cursor `BringIntoViewRequester`) so the cursor and surrounding lines remain centered in the visible area.
   - **Live Postfix & Abbreviation Suggestions**: Typing shorthand abbreviations like `sout`, `serr`, `main`, `psvm`, `fori`, `ifn`, `inn`, `lazy`, or ` tryc` surfaces a sleek inline completion popup right near the cursor—expanding `sout` into `println("")` with the cursor placed inside the quotes.
   - **Real-Time Syntax Diagnostics**: As you type, the syntax analyzer checks for unbalanced parentheses/braces/brackets, unclosed string literals, invalid token sequences, and common Kotlin typos (e.g., `func` instead of `fun`, `Sting` instead of `String`, `println` without parens), highlighting the exact line in red and offering a one-tap Quick Fix.
4. **Explanation Block — Floating Window vs. Split-Screen Two-Window Mode**:
   - Tapping the **Explanation (`📝`)** pill in the Dynamic Island opens the section's Explanation Editor.
   - **Mode A — Floating Resizable Window**: Floats over the code editor like the macOS Notes window in your reference image. You can drag its title bar anywhere on screen and drag its resize handle to make it larger or smaller.
   - **Mode B — Set Aside / Split-Screen Two-Window Mode**: Tapping the **Split View (`◫`)** button on the Explanation title bar docks the Code Editor and Explanation Editor into two side-by-side (or top/bottom) macOS windows separated by a draggable split divider so you can read/write code and prose simultaneously.

### Visual Identity & Theme
- **Aesthetic Direction**: *macOS Sequoia Dynamic Island & Apple Terminal (`cmd`) Studio* — ultra-clean translucent glass surfaces (`#16181FCC`), crisp `1dp` specular borders (`#FFFFFF1F`), macOS traffic-light window controls (`#FF5F56`, `#FFBD2E`, `#27C93F`), and high-contrast JetBrains Mono typography.
- **Color Palette & Mood (Dark & Light Mode)**:
  - **Apple `cmd` Dark Surface**: Deep Charcoal Terminal (`#0B0D12`), Floating Island Pill (`#050608F2`), Translucent Glass Dropdown Window (`#181A22F0`), Electric Amber/Blue selection accents (`#0A84FF` / `#FF9F0A`), and vivid syntax tokens.
  - **macOS Daylight Mode**: Frosted Silver desktop backdrop (`#ECEFF4`), Crisp White floating windows (`#FFFFFFF2`) with subtle drop shadows, and classic dark or light terminal themes.

---

## 3. Key Product Decisions & Trade-Offs

- **Decision 1: Deterministic APK Signing for Seamless Updates**
  - *Chosen Approach*: Generate and track `debug.keystore.base64` from the project's `debug.keystore` and increment `versionCode` in Gradle so GitHub Actions decodes the identical signing certificate on every push rather than running `keytool -genkeypair` to create a random key.
  - *Why*: Android rejects APK updates (`INSTALL_FAILED_UPDATE_INCOMPATIBLE`) whenever two APKs with the same `applicationId` are signed with different certificates. Sharing the exact encoded debug certificate ensures every new build installs cleanly as an update over the existing app.
- **Decision 2: Dual-Layer Persistence (Room SQLite + Obfuscated Hidden Vault `.ktscribe_vault_xyz`)**
  - *Chosen Approach*: Every 30 seconds and on lifecycle pause/stop, persist all folders and sections to Room SQLite **and** mirror an obfuscated, Base64+XOR encoded vault snapshot into the hidden vault folder, paired with `android:hasFragileUserData="true"` in `AndroidManifest.xml`.
  - *Why*: Satisfies Android Play Policy while giving users both the native Android "Keep app data on uninstall" checkbox and a first-launch Vault Recovery prompt that restores previous folders without exposing plain-text files.
- **Decision 3: Floating Resizable Windows + Split-Screen Toggle Instead of Cluttered Toolbars**
  - *Chosen Approach*: Consolidate navigation, search, AI tools, and folder management into the top **Floating Dynamic Island** and render active tools (Command Palette, Folder Switcher, Explanation Editor, AI Studio) as resizable macOS windows or a 2-window split view.
  - *Why*: Eliminates visual clutter and snippet overload while keeping the Kotlin code editor full-screen and responsive when the keyboard is up.

---

## 4. Technical Architecture & Data Strategy *(Technical Reference)*

### Architecture & Component Diagram
```
┌──────────────────────────────────────────────────────────────────────────────┐
│                                 MainActivity                                 │
│  • Firebase App Check Init            • Hardware Key Intercept (Ctrl+Letter) │
│  • First-Launch Vault Recovery Gate   • Lifecycle Auto-Save & Vault Sync     │
└──────────────────────────────────────┬───────────────────────────────────────┘
                                       │
                                       ▼
┌──────────────────────────────────────────────────────────────────────────────┐
│                          EditorViewModel (StateFlow)                         │
│  • Active Folder & Code Sections      • Floating Window & Split-View State   │
│  • Command Palette Filter & Search    • 30s Auto-Save & Hidden Vault Mirror  │
└──────────┬───────────────────────────┴────────────────────────┬──────────────┘
           │                                                    │
           ▼                                                    ▼
┌────────────────────────────────────────┐    ┌────────────────────────────────┐
│   EditorRepository & VaultManager      │    │     AiSectionNamingService     │
│  • Room SQLite (Folders & Sections)    │    │  • Firebase.ai (Gemini 3.x)    │
│  • Hidden Vault (.ktscribe_vault_xyz)  │    │  • 10 RPM Free-Tier Limiter    │
│  • Obfuscated Snapshot Encode/Decode   │    │  • Thorough Guide & Concept Tag│
└────────────────────────────────────────┘    └────────────────────────────────┘
           │
           ▼
┌──────────────────────────────────────────────────────────────────────────────┐
│                    Jetpack Compose macOS Island Workspace                    │
│  ┌────────────────────────────────────────────────────────────────────────┐  │
│  │              Top Floating Dynamic Island (Notch Menu Pill)             │  │
│  │   [📁 Folder]  [🏷️ Concept Tag]  [⌘P Palette]  [📝 Explain]  [✨ AI]   │  │
│  └───────────────┬────────────────────────────────────────┬───────────────┘  │
│                  │ Drops down resizable macOS windows     │                  │
│                  ▼                                        ▼                  │
│  ┌────────────────────────────────────┐  ┌────────────────────────────────┐  │
│  │ Full-Screen Apple `cmd` Code Window│  │ Resizable Explanation Window   │  │
│  │ • macOS Traffic Lights & zsh Header│  │ • Mode A: Draggable & Resizable│  │
│  │ • Live Syntax Error Underlines     │  │   Floating macOS Notes Window  │  │
│  │ • Inline Postfix Popup (sout, fori)│  │ • Mode B: Set-Aside Resizable  │  │
│  │ • Keyboard-Aware Viewport Resize   │  │   Split-Screen Two-Window View │  │
│  └────────────────────────────────────┘  └────────────────────────────────┘  │
└──────────────────────────────────────────────────────────────────────────────┘
```

### Data Model & State
- **`Folder` (Workspace Folder Entity)**:
  - `id: Long`, `folderName: String`, `description: String`, `sections: List<CodeSection>`, `updatedAt: Long`, `createdAt: Long`.
- **`CodeSection` (Inside Folder)**:
  - `id: Long`, `folderId: Long`, `orderIndex: Int`, `sectionTitle: String`, `learnedConceptTag: String`, `codeContent: String`, `explanationText: String`, `aiDeepExplanation: String`.
- **`FloatingWindowState`**:
  - Tracks active dropdown window (`NONE`, `COMMAND_PALETTE`, `FOLDER_EXPLORER`, `AI_STUDIO`, `TERMINAL_OUTPUT`), window dimensions (`widthDp`, `heightDp` adjusted via drag resize handle), and Explanation Window layout mode (`HIDDEN`, `FLOATING_WINDOW`, `SPLIT_SCREEN` with adjustable split ratio `0.25f..0.75f`).
- **`VaultSnapshot` (`.ktscribe_vault_xyz`)**:
  - Obfuscated binary backup file containing serialized folders, code sections, and concept tags, checked on first launch to offer one-tap recovery or fresh initialization.

### Interactive Component & State Mapping
- **First-Launch Storage Permission & Vault Recovery Flow**:
  - On first launch, `VaultStorageManager` requests user confirmation to access/initialize the local `.ktscribe_vault_xyz` directory. If an existing vault snapshot is detected, a modal asks whether to **Recover Previous Data** (importing folders into Room) or **Start Fresh in New Vault**.
- **Top Floating Dynamic Island & Resizable Drop-Down Windows**:
  - Tapping the Island expands the selected tool into a macOS window anchored beneath the Island (or centered for the Command Palette). Dragging the bottom/corner resize handle updates `windowWidthDp` and `windowHeightDp` in real time.
- **Explanation Floating & Split-Screen Window**:
  - In `FLOATING_WINDOW` mode, `pointerInput` drag gestures on the window header move `(offsetX, offsetY)` and drag gestures on the bottom-right resize grip resize `(widthDp, heightDp)`.
  - In `SPLIT_SCREEN` mode, the workspace divides into two macOS windows (Code Editor + Explanation Editor) with a draggable divider bar between them, automatically adapting to portrait (top/bottom split) or landscape/tablet (left/right split) and resizing cleanly when the soft keyboard opens.
- **Live Postfix Abbreviation & Syntax Error Engine (`KotlinCodeEngine`)**:
  - Detects abbreviations immediately before the cursor (`sout` → `println("")`, `serr` → `System.err.println("")`, `main` → `fun main() {\n    \n}`, `fori` → `for (i in 0 until n) {\n    \n}`, `ifn` → `if (value == null) {\n    \n}`, `inn` → `if (value != null) {\n    \n}`, `tryc` → `try {\n    \n} catch (e: Exception) {\n    \n}`) and shows a compact inline popup (no cluttered snippet bar).
  - Performs real-time lexical and structural validation on every edit, surfacing exact line numbers, error messages, and one-tap Quick Fixes in both the code gutter and the Dynamic Island status badge.
