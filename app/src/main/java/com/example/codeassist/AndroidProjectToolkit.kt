package com.example.codeassist

import com.example.model.CodeSectionUiModel
import com.example.model.LanguageType

data class ManifestPermissionInfo(
    val fullName: String,
    val shortName: String,
    val plainMeaning: String,
    val isPlayRestricted: Boolean = false,
    val playAdvice: String = ""
)

data class ManifestActivityInfo(
    val name: String,
    val isLauncher: Boolean = false,
    val isExported: Boolean = false
)

data class ManifestSummary(
    val packageName: String,
    val appLabel: String,
    val minSdk: Int,
    val targetSdk: Int,
    val activities: List<ManifestActivityInfo>,
    val permissions: List<ManifestPermissionInfo>,
    val hasSourceFile: Boolean
) {
    val policyWarnings: List<String>
        get() = permissions.filter { it.isPlayRestricted }.map {
            "${it.shortName}: ${it.playAdvice}"
        }
}

data class StringResourceItem(
    val key: String,
    val value: String,
    val sourceFile: String
)

data class ColorResourceItem(
    val key: String,
    val hexValue: String,
    val argbLong: Long,
    val sourceFile: String
)

data class DrawableResourceItem(
    val name: String,
    val kind: String
)

data class AndroidResourceBundle(
    val strings: List<StringResourceItem>,
    val colors: List<ColorResourceItem>,
    val drawables: List<DrawableResourceItem>
) {
    fun resolveString(raw: String): String {
        val trimmed = raw.trim()
        if (!trimmed.startsWith("@string/")) return trimmed
        val key = trimmed.removePrefix("@string/").trim()
        return strings.firstOrNull { it.key == key }?.value ?: trimmed
    }

    fun resolveColorHex(raw: String): Long? {
        val trimmed = raw.trim()
        val hex: String = when {
            trimmed.startsWith("@color/") -> {
                val key = trimmed.removePrefix("@color/").trim()
                colors.firstOrNull { it.key == key }?.hexValue
            }
            trimmed.startsWith("#") -> trimmed
            else -> null
        } ?: return null

        val clean = hex.removePrefix("#").trim()
        return when (clean.length) {
            6 -> ("FF$clean").toLongOrNull(16)
            8 -> clean.toLongOrNull(16)
            else -> null
        }
    }
}

data class GradleDependencyItem(
    val id: String,
    val group: String,
    val artifact: String,
    val version: String,
    val category: String,
    val beginnerSummary: String
) {
    val implementationLine: String
        get() = "implementation(\"$group:$artifact:$version\")"

    val tomlKey: String
        get() = artifact.replace('.', '-')
}

data class BuildPipelineTask(
    val taskPath: String,
    val summary: String,
    val durationMs: Int,
    val succeeded: Boolean
)

data class VirtualShellResult(
    val outputText: String,
    val clearedTerminal: Boolean = false
)

data class LocalGitCommit(
    val id: Long,
    val shortHash: String,
    val branch: String,
    val message: String,
    val timestampMs: Long,
    val snapshotsBySectionId: Map<Long, String>
)

data class GitFileStatusItem(
    val sectionId: Long,
    val fileName: String,
    val statusBadge: String // "M" modified, "A" added, "✓" clean
)

object AndroidProjectToolkit {

    private val COMMON_PERMISSION_MEANINGS = mapOf(
        "android.permission.INTERNET" to ("Allows the app to load websites or call cloud APIs over Wi-Fi/mobile data." to false),
        "android.permission.CAMERA" to ("Allows taking photos or scanning QR codes (must ask the user at runtime)." to false),
        "android.permission.POST_NOTIFICATIONS" to ("Allows showing study reminders or build-complete alerts (asks user on Android 13+)." to false),
        "android.permission.VIBRATE" to ("Allows gentle haptic feedback taps on buttons." to false),
        "android.permission.ACCESS_FINE_LOCATION" to ("Allows reading GPS location when the user grants permission." to false),
        "android.permission.READ_EXTERNAL_STORAGE" to ("Legacy broad storage read — Google Play requires the zero-permission Photo Picker instead." to true),
        "android.permission.READ_MEDIA_IMAGES" to ("Broad gallery scan — Google Play prefers PickVisualMedia Photo Picker for single/multi image picks." to true),
        "android.permission.MANAGE_EXTERNAL_STORAGE" to ("All-Files Access — restricted by Google Play unless the app is a dedicated file manager." to true)
    )

    fun parseManifest(sections: List<CodeSectionUiModel>, fallbackAppName: String = "KtScribeApp"): ManifestSummary {
        val manifestSec = sections.firstOrNull {
            it.sectionTitle.contains("AndroidManifest", ignoreCase = true) ||
                it.codeContent.contains("<manifest", ignoreCase = true)
        }
        val xml = manifestSec?.codeContent ?: defaultManifestXml(fallbackAppName)

        val pkg = Regex("""package\s*=\s*["']([^"']+)["']""").find(xml)?.groupValues?.getOrNull(1)
            ?: "com.aistudio.student.app"
        val rawAppLabel = Regex("""<application[^>]*android:label\s*=\s*["']([^"']+)["']""")
            .find(xml)?.groupValues?.getOrNull(1) ?: fallbackAppName
        val appLabel = if (rawAppLabel.startsWith("@string/")) {
            parseResources(sections).resolveString(rawAppLabel)
        } else {
            rawAppLabel
        }
        val minSdk = Regex("""android:minSdkVersion\s*=\s*["'](\d+)["']""").find(xml)?.groupValues?.getOrNull(1)?.toIntOrNull() ?: 26
        val targetSdk = Regex("""android:targetSdkVersion\s*=\s*["'](\d+)["']""").find(xml)?.groupValues?.getOrNull(1)?.toIntOrNull() ?: 35

        val perms = Regex("""<uses-permission[^>]*android:name\s*=\s*["']([^"']+)["']""")
            .findAll(xml)
            .map { it.groupValues[1].trim() }
            .distinct()
            .map { full ->
                val shortName = full.substringAfterLast('.')
                val meta = COMMON_PERMISSION_MEANINGS[full]
                val isRestricted = meta?.second ?: false
                ManifestPermissionInfo(
                    fullName = full,
                    shortName = shortName,
                    plainMeaning = meta?.first ?: "Custom Android capability declaration ($shortName).",
                    isPlayRestricted = isRestricted,
                    playAdvice = if (isRestricted) "Replace with zero-permission Photo Picker (PickVisualMedia) or SAF." else ""
                )
            }
            .toList()

        val activityRegex = Regex("""<activity\b([\s\S]*?)(?:/>|</activity>)""")
        val activities = activityRegex.findAll(xml).map { match ->
            val block = match.value
            val name = Regex("""android:name\s*=\s*["']([^"']+)["']""").find(block)?.groupValues?.getOrNull(1) ?: ".MainActivity"
            val exported = block.contains("""android:exported="true"""") || block.contains("""android:exported='true'""")
            val launcher = block.contains("android.intent.action.MAIN") || block.contains("LAUNCHER")
            ManifestActivityInfo(
                name = name,
                isLauncher = launcher,
                isExported = exported
            )
        }.toList().ifEmpty {
            listOf(ManifestActivityInfo(name = ".MainActivity", isLauncher = true, isExported = true))
        }

        return ManifestSummary(
            packageName = pkg,
            appLabel = appLabel,
            minSdk = minSdk,
            targetSdk = targetSdk,
            activities = activities,
            permissions = perms,
            hasSourceFile = manifestSec != null
        )
    }

    fun insertPermissionIntoManifest(xml: String, permissionFullName: String): String {
        if (xml.contains(permissionFullName)) return xml
        val tag = "    <uses-permission android:name=\"$permissionFullName\" />"
        val appIdx = xml.indexOf("<application")
        return if (appIdx >= 0) {
            val before = xml.substring(0, appIdx).trimEnd()
            val after = xml.substring(appIdx)
            "$before\n\n$tag\n\n$after"
        } else {
            "$tag\n$xml"
        }
    }

    fun parseResources(sections: List<CodeSectionUiModel>): AndroidResourceBundle {
        val strings = mutableListOf<StringResourceItem>()
        val colors = mutableListOf<ColorResourceItem>()

        val stringRegex = Regex("""<string\s+name\s*=\s*["']([^"']+)["']\s*>([^<]*)</string>""")
        val colorRegex = Regex("""<color\s+name\s*=\s*["']([^"']+)["']\s*>\s*(#[0-9A-Fa-f]{6,8})\s*</color>""")

        sections.forEach { sec ->
            stringRegex.findAll(sec.codeContent).forEach { m ->
                strings.add(
                    StringResourceItem(
                        key = m.groupValues[1],
                        value = m.groupValues[2].trim(),
                        sourceFile = sec.nativeFileName
                    )
                )
            }
            colorRegex.findAll(sec.codeContent).forEach { m ->
                val hex = m.groupValues[2].uppercase()
                val argb = parseColorLong(hex)
                if (argb != null) {
                    colors.add(
                        ColorResourceItem(
                            key = m.groupValues[1],
                            hexValue = hex,
                            argbLong = argb,
                            sourceFile = sec.nativeFileName
                        )
                    )
                }
            }
        }

        if (strings.none { it.key == "app_name" }) {
            strings.add(StringResourceItem("app_name", "Student Study App", "built-in defaults"))
        }
        if (strings.none { it.key == "welcome_title" }) {
            strings.add(StringResourceItem("welcome_title", "Welcome, Builder!", "built-in defaults"))
        }
        if (strings.none { it.key == "btn_start" }) {
            strings.add(StringResourceItem("btn_start", "Start Lesson", "built-in defaults"))
        }

        val defaultColors = listOf(
            "primary" to "#D4883A",
            "secondary" to "#2A9D8F",
            "surface_card" to "#23201E",
            "text_primary" to "#F5EFE6"
        )
        defaultColors.forEach { (key, hex) ->
            if (colors.none { it.key == key }) {
                colors.add(
                    ColorResourceItem(
                        key = key,
                        hexValue = hex,
                        argbLong = parseColorLong(hex) ?: 0xFFD4883AL,
                        sourceFile = "built-in defaults"
                    )
                )
            }
        }

        val drawables = listOf(
            DrawableResourceItem("ic_launcher_foreground", "Adaptive Vector"),
            DrawableResourceItem("ic_student_badge", "Compose Vector"),
            DrawableResourceItem("bg_card_surface", "Material Tonal Surface")
        )

        return AndroidResourceBundle(
            strings = strings.distinctBy { it.key },
            colors = colors.distinctBy { it.key },
            drawables = drawables
        )
    }

    private fun parseColorLong(hex: String): Long? {
        val clean = hex.removePrefix("#").trim()
        return when (clean.length) {
            6 -> ("FF$clean").toLongOrNull(16)
            8 -> clean.toLongOrNull(16)
            else -> null
        }
    }

    val gradleCatalog: List<GradleDependencyItem> = listOf(
        GradleDependencyItem(
            id = "compose_m3",
            group = "androidx.compose.material3",
            artifact = "material3",
            version = "1.3.1",
            category = "UI & Material",
            beginnerSummary = "Provides modern buttons, cards, switches, and Material 3 colors."
        ),
        GradleDependencyItem(
            id = "nav_compose",
            group = "androidx.navigation",
            artifact = "navigation-compose",
            version = "2.8.5",
            category = "Navigation",
            beginnerSummary = "Moves students smoothly between screens using type-safe routes."
        ),
        GradleDependencyItem(
            id = "coroutines_android",
            group = "org.jetbrains.kotlinx",
            artifact = "kotlinx-coroutines-android",
            version = "1.9.0",
            category = "Async",
            beginnerSummary = "Runs background work safely without freezing the phone UI."
        ),
        GradleDependencyItem(
            id = "room_runtime",
            group = "androidx.room",
            artifact = "room-ktx",
            version = "2.6.1",
            category = "Database",
            beginnerSummary = "Saves notes, scores, and files permanently inside local SQLite tables."
        ),
        GradleDependencyItem(
            id = "retrofit_core",
            group = "com.squareup.retrofit2",
            artifact = "retrofit",
            version = "2.11.0",
            category = "Networking",
            beginnerSummary = "Fetches JSON data cleanly from REST web APIs."
        ),
        GradleDependencyItem(
            id = "coil_compose",
            group = "io.coil-kt.coil3",
            artifact = "coil-compose",
            version = "3.0.4",
            category = "Images",
            beginnerSummary = "Loads and caches photos smoothly inside Jetpack Compose."
        ),
        GradleDependencyItem(
            id = "vico_charts",
            group = "com.patrykandpatrick.vico",
            artifact = "compose-m3",
            version = "2.0.0-beta.2",
            category = "Charts",
            beginnerSummary = "Draws animated bar and line graphs for trackers and dashboards."
        ),
        GradleDependencyItem(
            id = "firebase_ai",
            group = "com.google.firebase",
            artifact = "firebase-ai",
            version = "16.0.0",
            category = "AI Logic",
            beginnerSummary = "Connects Android apps to Gemini models via Firebase App Check."
        )
    )

    fun runBuildPipeline(
        sections: List<CodeSectionUiModel>,
        activeSection: CodeSectionUiModel?
    ): List<BuildPipelineTask> {
        val kotlinCount = sections.count { it.language == LanguageType.KOTLIN }
        val xmlCount = sections.count { it.language == LanguageType.XML }
        val target = activeSection ?: sections.firstOrNull()
        val hasDiagnosticsError = if (target != null) {
            com.example.code.CodeLanguageEngine.getDiagnosticsForLanguage(target.codeContent, target.language)
                .any { it.severity == com.example.code.DiagnosticSeverity.ERROR }
        } else {
            false
        }
        return listOf(
            BuildPipelineTask(":app:preBuild", "Verified ${sections.size} workspace source file(s)", 14, true),
            BuildPipelineTask(":app:processDebugManifest", "Parsed package & AndroidManifest permissions", 28, true),
            BuildPipelineTask(":app:mergeDebugResources", "Indexed $xmlCount XML layout/resource file(s)", 34, true),
            BuildPipelineTask(
                ":app:compileDebugKotlin",
                if (hasDiagnosticsError) "Syntax errors in ${target?.nativeFileName}" else "Compiled $kotlinCount Kotlin unit(s) -> KtVM bytecode",
                62,
                !hasDiagnosticsError
            ),
            BuildPipelineTask(
                ":app:dexBuilderDebug",
                if (hasDiagnosticsError) "Skipped (compile task failed)" else "Packaged on-device execution frame",
                19,
                !hasDiagnosticsError
            ),
            BuildPipelineTask(
                ":app:packageDebug",
                if (hasDiagnosticsError) "Blocked until syntax errors are fixed" else "Ready for Live Preview / Termux Gradle export",
                11,
                !hasDiagnosticsError
            )
        )
    }

    fun executeShellCommand(
        rawCommand: String,
        sections: List<CodeSectionUiModel>,
        activeSection: CodeSectionUiModel?,
        manifestSummary: ManifestSummary,
        resourceBundle: AndroidResourceBundle
    ): VirtualShellResult {
        val cmd = rawCommand.trim()
        if (cmd.isBlank()) return VirtualShellResult("")
        val parts = cmd.split(Regex("\\s+"))
        val head = parts.first().lowercase()
        val arg = parts.drop(1).joinToString(" ").trim()

        return when (head) {
            "clear", "cls" -> VirtualShellResult("", clearedTerminal = true)
            "help", "?" -> VirtualShellResult(
                buildString {
                    appendLine("$ help  — KtScribe On-Device Virtual Shell")
                    appendLine("  ls                   List all files in active project folder")
                    appendLine("  pwd                  Print current project directory")
                    appendLine("  cat <filename>       Show file contents")
                    appendLine("  grep <keyword>       Search text across all project files")
                    appendLine("  wc                   Count lines and words per file")
                    appendLine("  manifest             Inspect AndroidManifest activities & permissions")
                    appendLine("  res                  List @string and @color resources")
                    appendLine("  gradle assembleDebug Run multi-stage Android build check")
                    appendLine("  clear                Clear terminal output")
                }
            )
            "pwd" -> VirtualShellResult("$ pwd\n/workspace/ktscribe/app/src/main")
            "ls" -> VirtualShellResult(
                buildString {
                    appendLine("$ ls -la app/src/main")
                    if (sections.isEmpty()) {
                        appendLine("(empty project folder)")
                    } else {
                        sections.forEach { sec ->
                            val marker = if (sec.id == activeSection?.id) "*" else " "
                            appendLine("$marker ${sec.language.directoryPath}${sec.nativeFileName}  (${sec.lineCount}L, ${sec.language.displayName})")
                        }
                    }
                }
            )
            "cat" -> {
                val target = if (arg.isBlank()) activeSection else sections.firstOrNull {
                    it.nativeFileName.equals(arg, ignoreCase = true) ||
                        it.nativeFileName.contains(arg, ignoreCase = true)
                }
                if (target == null) {
                    VirtualShellResult("cat: $arg: No such file in project (try `ls`)")
                } else {
                    VirtualShellResult("--- ${target.nativeFileName} ---\n${target.codeContent}")
                }
            }
            "grep" -> {
                if (arg.isBlank()) {
                    VirtualShellResult("usage: grep <search_text>")
                } else {
                    val hits = mutableListOf<String>()
                    sections.forEach { sec ->
                        sec.codeContent.lines().forEachIndexed { idx, line ->
                            if (line.contains(arg, ignoreCase = true)) {
                                hits.add("${sec.nativeFileName}:${idx + 1}: ${line.trim()}")
                            }
                        }
                    }
                    if (hits.isEmpty()) VirtualShellResult("grep: no matches for \"$arg\"")
                    else VirtualShellResult("$ grep \"$arg\"\n" + hits.joinToString("\n"))
                }
            }
            "wc", "stat" -> VirtualShellResult(
                buildString {
                    appendLine("$ wc app/src/main/*")
                    sections.forEach { sec ->
                        val words = sec.codeContent.trim().split(Regex("\\s+")).filter { it.isNotBlank() }.size
                        appendLine("  ${sec.lineCount.toString().padStart(4)} lines  ${words.toString().padStart(4)} words  ${sec.nativeFileName}")
                    }
                }
            )
            "manifest" -> VirtualShellResult(
                buildString {
                    appendLine("Package : ${manifestSummary.packageName}")
                    appendLine("SDK     : minSdk=${manifestSummary.minSdk}, targetSdk=${manifestSummary.targetSdk}")
                    appendLine("Activity: ${manifestSummary.activities.joinToString { it.name }}")
                    appendLine("Perms   : ${manifestSummary.permissions.joinToString { it.shortName }.ifBlank { "None" }}")
                }
            )
            "res" -> VirtualShellResult(
                buildString {
                    appendLine("@string resources (${resourceBundle.strings.size}):")
                    resourceBundle.strings.forEach { appendLine("  @string/${it.key} = \"${it.value}\"") }
                    appendLine("@color resources (${resourceBundle.colors.size}):")
                    resourceBundle.colors.forEach { appendLine("  @color/${it.key} = ${it.hexValue}") }
                }
            )
            "gradle", "./gradlew", "build" -> {
                val tasks = runBuildPipeline(sections, activeSection)
                val allOk = tasks.all { it.succeeded }
                VirtualShellResult(
                    buildString {
                        appendLine("$ gradle ${arg.ifBlank { "assembleDebug" }}")
                        tasks.forEach { t ->
                            val tag = if (t.succeeded) "UP-TO-DATE / OK" else "FAILED"
                            appendLine("> Task ${t.taskPath} [$tag] (${t.durationMs}ms)")
                            appendLine("  ${t.summary}")
                        }
                        appendLine(if (allOk) "\nBUILD SUCCESSFUL in ${tasks.sumOf { it.durationMs }}ms" else "\nBUILD FAILED: fix errors above")
                    }
                )
            }
            "echo" -> VirtualShellResult(arg)
            else -> {
                // Fallback: try evaluating with Kotlinc/Active Language Engine if user runs e.g. `kotlinc`
                val target = activeSection
                if ((head == "kotlinc" || head == "ktvm" || head == "run") && target != null) {
                    val res = com.example.code.CodeLanguageEngine.executeAndDebug(target.codeContent, target.language, true)
                    VirtualShellResult(res.output)
                } else {
                    VirtualShellResult("sh: $head: unknown command. Type `help` to list CodeAssist virtual shell commands.")
                }
            }
        }
    }

    fun computeWorkingTreeStatus(
        sections: List<CodeSectionUiModel>,
        latestCommit: LocalGitCommit?
    ): List<GitFileStatusItem> {
        return sections.map { sec ->
            val committedText = latestCommit?.snapshotsBySectionId?.get(sec.id)
            val status = when {
                committedText == null -> "A"
                committedText != sec.codeContent -> "M"
                else -> "✓"
            }
            GitFileStatusItem(
                sectionId = sec.id,
                fileName = sec.nativeFileName,
                statusBadge = status
            )
        }
    }

    fun defaultManifestXml(appName: String = "StudentStudyApp"): String = """
        <manifest xmlns:android="http://schemas.android.com/apk/res/android"
            package="com.aistudio.student.studyapp">

            <uses-sdk android:minSdkVersion="26" android:targetSdkVersion="35" />
            <uses-permission android:name="android.permission.INTERNET" />

            <application
                android:label="@string/app_name"
                android:allowBackup="true">
                <activity
                    android:name=".MainActivity"
                    android:exported="true">
                    <intent-filter>
                        <action android:name="android.intent.action.MAIN" />
                        <category android:name="android.intent.category.LAUNCHER" />
                    </intent-filter>
                </activity>
            </application>
        </manifest>
    """.trimIndent()

    fun defaultStringsXml(appName: String = "Student Study App"): String = """
        <resources>
            <string name="app_name">$appName</string>
            <string name="welcome_title">Hello Android Student!</string>
            <string name="lesson_subtitle">Edit XML on the left, preview live below.</string>
            <string name="btn_start">Continue Practice</string>
        </resources>
    """.trimIndent()

    fun defaultColorsXml(): String = """
        <resources>
            <color name="primary">#D4883A</color>
            <color name="secondary">#2A9D8F</color>
            <color name="surface_card">#23201E</color>
            <color name="text_primary">#F5EFE6</color>
        </resources>
    """.trimIndent()

    fun defaultActivityMainXml(): String = """
        <LinearLayout
            android:orientation="vertical">

            <CardView>
                <LinearLayout android:orientation="vertical">
                    <TextView
                        android:text="@string/welcome_title"
                        android:textStyle="bold" />
                    <TextView
                        android:text="@string/lesson_subtitle" />
                    <EditText
                        android:hint="Type your study goal..." />
                    <Switch
                        android:text="Guided Beginner Mode"
                        android:checked="true" />
                    <Button
                        android:text="@string/btn_start" />
                </LinearLayout>
            </CardView>
        </LinearLayout>
    """.trimIndent()
}
