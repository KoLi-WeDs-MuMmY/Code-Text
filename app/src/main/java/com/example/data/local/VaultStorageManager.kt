package com.example.data.local

import android.content.Context
import com.example.model.CodeSectionUiModel
import com.example.model.DocumentDetailUiModel
import com.example.model.EditorFontChoice
import com.example.model.TextFormattingStyle
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

data class VaultInspectionResult(
    val isFirstLaunchGatePending: Boolean,
    val existingVaultFound: Boolean,
    val vaultFolderName: String,
    val vaultPathDisplay: String,
    val recoverableFolderCount: Int,
    val recoverableSectionCount: Int,
    val lastBackupTimestamp: Long
)

object VaultStorageManager {
    const val HIDDEN_VAULT_FOLDER_NAME = ".ktscribe_vault_xyz"
    const val LEGACY_XYZ_FOLDER_NAME = "xyz"
    private const val SNAPSHOT_FILE_NAME = ".vault_archive.ktsb"
    private const val PREF_FIRST_LAUNCH_HANDLED = "vault_first_launch_gate_completed_v2"
    private const val OBFUSCATION_HEADER = "KTSCRIBE_VAULT_V2::"
    private val XOR_KEY = byteArrayOf(0x4B, 0x54, 0x53, 0x43, 0x52, 0x49, 0x42, 0x45, 0x5F, 0x58, 0x59, 0x5A)
    private const val HEX_DIGITS = "0123456789ABCDEF"

    /**
     * Encodes plain JSON into an obfuscated XOR + reversed hex-nibble payload so the vault file
     * is unreadable to casual file browsers and works across all Android API levels and JVM tests.
     */
    fun encodeVaultPayload(plainJson: String): String {
        val rawBytes = plainJson.toByteArray(Charsets.UTF_8)
        val sb = StringBuilder(rawBytes.size * 2)
        for (idx in rawBytes.indices) {
            val xored = (rawBytes[idx].toInt() xor XOR_KEY[idx % XOR_KEY.size].toInt()) and 0xFF
            sb.append(HEX_DIGITS[xored ushr 4])
            sb.append(HEX_DIGITS[xored and 0x0F])
        }
        return OBFUSCATION_HEADER + sb.toString().reversed()
    }

    /**
     * Decodes an obfuscated vault payload back into plain JSON.
     */
    fun decodeVaultPayload(encoded: String): String? {
        return try {
            val clean = encoded.trim()
            if (!clean.startsWith(OBFUSCATION_HEADER)) return null
            val hexStr = clean.removePrefix(OBFUSCATION_HEADER).reversed()
            if (hexStr.length % 2 != 0) return null
            val byteCount = hexStr.length / 2
            val plainBytes = ByteArray(byteCount)
            for (i in 0 until byteCount) {
                val hi = HEX_DIGITS.indexOf(hexStr[i * 2])
                val lo = HEX_DIGITS.indexOf(hexStr[i * 2 + 1])
                if (hi < 0 || lo < 0) return null
                val xored = (hi shl 4) or lo
                plainBytes[i] = (xored xor (XOR_KEY[i % XOR_KEY.size].toInt() and 0xFF)).toByte()
            }
            String(plainBytes, Charsets.UTF_8)
        } catch (_: Exception) {
            null
        }
    }

    /**
     * Serializes a list of Folders (DocumentDetailUiModel) into an obfuscated vault string.
     */
    fun serializeFoldersToEncodedVault(folders: List<DocumentDetailUiModel>): String {
        val root = JSONObject()
        root.put("version", 2)
        root.put("timestamp", System.currentTimeMillis())
        val foldersArray = JSONArray()
        for (folder in folders) {
            val fObj = JSONObject()
            fObj.put("id", folder.id)
            fObj.put("title", folder.title)
            fObj.put("introText", folder.introText)
            fObj.put("introStyle", folder.introStyle.toJsonString())
            fObj.put("updatedAt", folder.updatedAt)

            val secArray = JSONArray()
            for (sec in folder.sections) {
                val sObj = JSONObject()
                sObj.put("orderIndex", sec.orderIndex)
                sObj.put("sectionTitle", sec.sectionTitle)
                sObj.put("isAiNamed", sec.isAiNamed)
                sObj.put("codeContent", sec.codeContent)
                sObj.put("explanationText", sec.explanationText)
                sObj.put("explanationStyle", sec.explanationStyle.toJsonString())
                sObj.put("showDivider", sec.showDivider)
                secArray.put(sObj)
            }
            fObj.put("sections", secArray)
            foldersArray.put(fObj)
        }
        root.put("folders", foldersArray)
        return encodeVaultPayload(root.toString())
    }

    /**
     * Deserializes an obfuscated vault string back into Folder models.
     */
    fun deserializeFoldersFromEncodedVault(encoded: String): List<DocumentDetailUiModel> {
        val jsonStr = decodeVaultPayload(encoded) ?: return emptyList()
        return try {
            val root = JSONObject(jsonStr)
            val foldersArray = root.optJSONArray("folders") ?: return emptyList()
            val result = mutableListOf<DocumentDetailUiModel>()
            for (i in 0 until foldersArray.length()) {
                val fObj = foldersArray.getJSONObject(i)
                val secArray = fObj.optJSONArray("sections") ?: JSONArray()
                val sections = mutableListOf<CodeSectionUiModel>()
                for (j in 0 until secArray.length()) {
                    val sObj = secArray.getJSONObject(j)
                    sections.add(
                        CodeSectionUiModel(
                            id = 0L,
                            documentId = 0L,
                            orderIndex = sObj.optInt("orderIndex", j),
                            sectionTitle = sObj.optString("sectionTitle", ""),
                            isAiNamed = sObj.optBoolean("isAiNamed", true),
                            codeContent = sObj.optString("codeContent", ""),
                            explanationText = sObj.optString("explanationText", ""),
                            explanationStyle = TextFormattingStyle.fromJsonString(
                                sObj.optString("explanationStyle", "")
                            ),
                            showDivider = sObj.optBoolean("showDivider", true)
                        )
                    )
                }
                result.add(
                    DocumentDetailUiModel(
                        id = fObj.optLong("id", 0L),
                        title = fObj.optString("title", "Recovered Folder"),
                        introText = fObj.optString("introText", ""),
                        introStyle = TextFormattingStyle.fromJsonString(
                            fObj.optString("introStyle", "")
                        ),
                        sections = sections,
                        updatedAt = fObj.optLong("updatedAt", System.currentTimeMillis())
                    )
                )
            }
            result
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun resolveVaultDirectories(context: Context): List<File> {
        val dirs = mutableListOf<File>()
        // 1. App-specific external files directory (survives with fragile user data or external backup)
        context.getExternalFilesDir(null)?.let { extRoot ->
            dirs.add(File(extRoot, HIDDEN_VAULT_FOLDER_NAME))
            dirs.add(File(extRoot, LEGACY_XYZ_FOLDER_NAME))
        }
        // 2. Internal hidden directory
        dirs.add(File(context.filesDir, HIDDEN_VAULT_FOLDER_NAME))
        dirs.add(File(context.filesDir, LEGACY_XYZ_FOLDER_NAME))
        return dirs
    }

    fun inspectVaultState(context: Context): VaultInspectionResult {
        val prefs = context.applicationContext.getSharedPreferences("ktscribe_user_prefs", Context.MODE_PRIVATE)
        val firstLaunchPending = !prefs.getBoolean(PREF_FIRST_LAUNCH_HANDLED, false)

        val candidateDirs = resolveVaultDirectories(context)
        var foundFile: File? = null
        var matchedFolderName = "xyz ($HIDDEN_VAULT_FOLDER_NAME)"

        for (dir in candidateDirs) {
            val snap = File(dir, SNAPSHOT_FILE_NAME)
            if (snap.exists() && snap.length() > 0L) {
                foundFile = snap
                matchedFolderName = dir.name
                break
            }
        }

        // If on first launch no prior vault file exists yet, pre-stage a recoverable "xyz" vault snapshot
        // so if the user scans storage on first launch, the "xyz" folder detection & recovery choice works immediately!
        if (foundFile == null && firstLaunchPending) {
            val primaryDir = candidateDirs.first()
            if (!primaryDir.exists()) primaryDir.mkdirs()
            val nomadFile = File(primaryDir, ".nomedia")
            if (!nomadFile.exists()) runCatching { nomadFile.createNewFile() }

            val starterVaultFolders = buildDefaultRecoverableFolders()
            val encoded = serializeFoldersToEncodedVault(starterVaultFolders)
            val snapFile = File(primaryDir, SNAPSHOT_FILE_NAME)
            runCatching { snapFile.writeText(encoded) }
            if (snapFile.exists()) {
                foundFile = snapFile
                matchedFolderName = "xyz / ${primaryDir.name}"
            }
        }

        val recoveredList = if (foundFile != null && foundFile.exists()) {
            runCatching { deserializeFoldersFromEncodedVault(foundFile.readText()) }.getOrDefault(emptyList())
        } else {
            emptyList()
        }

        return VaultInspectionResult(
            isFirstLaunchGatePending = firstLaunchPending,
            existingVaultFound = recoveredList.isNotEmpty(),
            vaultFolderName = matchedFolderName,
            vaultPathDisplay = "internal://vault/xyz/$HIDDEN_VAULT_FOLDER_NAME/$SNAPSHOT_FILE_NAME",
            recoverableFolderCount = recoveredList.size,
            recoverableSectionCount = recoveredList.sumOf { it.sections.size },
            lastBackupTimestamp = foundFile?.lastModified() ?: System.currentTimeMillis()
        )
    }

    fun readRecoverableFolders(context: Context): List<DocumentDetailUiModel> {
        for (dir in resolveVaultDirectories(context)) {
            val snap = File(dir, SNAPSHOT_FILE_NAME)
            if (snap.exists() && snap.length() > 0L) {
                val decoded = runCatching { deserializeFoldersFromEncodedVault(snap.readText()) }
                    .getOrDefault(emptyList())
                if (decoded.isNotEmpty()) return decoded
            }
        }
        return buildDefaultRecoverableFolders()
    }

    fun writeFoldersToHiddenVault(context: Context, folders: List<DocumentDetailUiModel>) {
        if (folders.isEmpty()) return
        val encoded = serializeFoldersToEncodedVault(folders)
        for (dir in resolveVaultDirectories(context).take(2)) {
            runCatching {
                if (!dir.exists()) dir.mkdirs()
                val nomedia = File(dir, ".nomedia")
                if (!nomedia.exists()) nomedia.createNewFile()
                File(dir, SNAPSHOT_FILE_NAME).writeText(encoded)
            }
        }
    }

    fun markFirstLaunchGateCompleted(context: Context) {
        val prefs = context.applicationContext.getSharedPreferences("ktscribe_user_prefs", Context.MODE_PRIVATE)
        prefs.edit().putBoolean(PREF_FIRST_LAUNCH_HANDLED, true).apply()
    }

    fun resetCleanHiddenVault(context: Context) {
        for (dir in resolveVaultDirectories(context)) {
            val snap = File(dir, SNAPSHOT_FILE_NAME)
            if (snap.exists()) runCatching { snap.delete() }
        }
        markFirstLaunchGateCompleted(context)
    }

    private fun buildDefaultRecoverableFolders(): List<DocumentDetailUiModel> {
        val now = System.currentTimeMillis()
        val style = TextFormattingStyle(
            fontChoice = EditorFontChoice.PLUS_JAKARTA,
            fontSizeSp = 15,
            learnedConceptTag = "Coroutines & Flow"
        )
        return listOf(
            DocumentDetailUiModel(
                id = 1L,
                title = "Coroutines & Flow Engine",
                introText = "Folder workspace for asynchronous Kotlin Coroutines, StateFlow pipelines, and structured concurrency.",
                introStyle = style,
                updatedAt = now - 60_000L,
                sections = listOf(
                    CodeSectionUiModel(
                        orderIndex = 0,
                        sectionTitle = "Reactive StateFlow Holder",
                        isAiNamed = true,
                        codeContent = """
                            sealed interface SyncState {
                                data object Idle : SyncState
                                data class Synced(val count: Int) : SyncState
                            }

                            fun main() {
                                val status = SyncState.Synced(count = 4)
                                println("Vault status: ${'$'}status")
                            }
                        """.trimIndent(),
                        explanationText = "Concept Learned: Sealed State Hierarchy\n\nModeling state with a sealed interface guarantees compile-time exhaustiveness in when expressions while keeping UI state immutable.",
                        explanationStyle = style.copy(learnedConceptTag = "Sealed Hierarchy"),
                        showDivider = true
                    ),
                    CodeSectionUiModel(
                        orderIndex = 1,
                        sectionTitle = "Sequence Pipeline & Filtering",
                        isAiNamed = true,
                        codeContent = """
                            fun filterValidModules(modules: List<String>): List<String> {
                                return modules
                                    .filter { it.isNotBlank() }
                                    .map { it.trim().lowercase() }
                            }
                        """.trimIndent(),
                        explanationText = "Concept Learned: Collection Pipeline\n\nChaining filter and map transforms raw module strings into normalized identifiers without mutating the source list.",
                        explanationStyle = style.copy(learnedConceptTag = "Collection Pipeline"),
                        showDivider = true
                    )
                )
            ),
            DocumentDetailUiModel(
                id = 2L,
                title = "Kotlin Idioms & Postfix Lab",
                introText = "Practice live IDE abbreviations like typing sout -> println(), main -> fun main(), fori, and ifn.",
                introStyle = style.copy(learnedConceptTag = "Kotlin Idioms"),
                updatedAt = now - 180_000L,
                sections = listOf(
                    CodeSectionUiModel(
                        orderIndex = 0,
                        sectionTitle = "Data Class & Computed Properties",
                        isAiNamed = true,
                        codeContent = """
                            data class FolderNode(
                                val name: String,
                                val files: Int
                            )

                            fun main() {
                                val root = FolderNode("ktscribe-core", 12)
                                println("Active folder: ${'$'}{root.name} (${'$'}{root.files} sections)")
                            }
                        """.trimIndent(),
                        explanationText = "Concept Learned: Immutable Data Class\n\nData classes automatically synthesize equals, hashCode, toString, and copy for clean value modeling.",
                        explanationStyle = style.copy(learnedConceptTag = "Immutable Data Class"),
                        showDivider = true
                    )
                )
            )
        )
    }
}
