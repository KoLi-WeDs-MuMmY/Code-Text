# Open-Source Notices & Distribution Architecture (KtScribe Student IDE)

KtScribe Student IDE is distributed under the **GNU General Public License v3.0 or later (GPL-3.0-or-later)**.

## 1. Architecture & CodeAssist Attribution Notice
- KtScribe maintains an independent Jetpack Compose Material 3 UI, adaptive screen-ratio theme engine, built-in `KotlinCompilerPlugin`, `KtBytecodeVm`, and interactive Student Learning Suite.
- If optional Gradle toolchain/completion adapters inspired by or derived from `tyron12233/CodeAssist` (GPL-3.0-or-later) are linked in source-release builds:
  - Original copyright and GPL-3.0 headers MUST remain intact.
  - Complete corresponding source code MUST remain available under GPL-3.0.
  - KtScribe branding, package ID, and launcher identity remain separate to avoid trademark confusion.

## 2. Google Play vs. Open-Source Toolchain Distribution Split
- **Google Play Compliant Mode (Default)**: Runs 100% bundled on-device compilers/interpreters (`KtBytecodeVm`, multi-language AST interpreter, and declarative Compose/Flutter UI previewer) with zero dynamic `.jar`/`.dex`/`.so` downloads.
- **Bare-Metal Export Mode**: Exports clean shell scripts and native files (`kotlinc`, `javac`, `python3`, `g++`, `dart`, `rustc`) for Termux or desktop toolchains.
