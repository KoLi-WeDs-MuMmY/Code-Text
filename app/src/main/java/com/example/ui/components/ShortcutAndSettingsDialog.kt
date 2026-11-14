package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ai.AiSectionNamingService
import com.example.model.LanguageType
import com.example.model.PluginCategory
import com.example.model.ToolchainPluginItem
import com.example.ui.theme.AppThemeId
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.ThemeMode

enum class StudioSettingsCategory(val id: String, val label: String, val icon: ImageVector) {
    ALL("all", "All", Icons.Default.Build),
    SDK_PLUGINS("sdk_plugins", "SDK & Plugins", Icons.Default.Extension),
    APPEARANCE("appearance", "Appearance", Icons.Default.Palette),
    EDITOR("editor", "Editor & Caret", Icons.Default.Code),
    SHORTCUTS("shortcuts", "Shortcuts", Icons.Default.Keyboard),
    VAULT_AI("vault_ai", "Vault & AI", Icons.Default.Security)
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ShortcutAndSettingsDialog(
    currentShortcutLetter: Char,
    currentThemeMode: ThemeMode,
    currentAppTheme: AppThemeId,
    remainingAiQuota: Int,
    defaultLanguage: LanguageType = LanguageType.KOTLIN,
    toolchainPlugins: List<ToolchainPluginItem> = ToolchainPluginItem.defaultCatalog(),
    initialCategory: StudioSettingsCategory = StudioSettingsCategory.ALL,
    onSelectDefaultLanguage: (LanguageType) -> Unit = {},
    onSelectShortcutLetter: (Char) -> Unit,
    onSelectThemeMode: (ThemeMode) -> Unit,
    onSelectAppTheme: (AppThemeId) -> Unit,
    onInstallOrTogglePlugin: (String) -> Unit = {},
    onInstallAllSdks: () -> Unit = {},
    onDismiss: () -> Unit
) {
    var selectedDefaultLang by remember(defaultLanguage) { mutableStateOf(defaultLanguage) }
    var activeCategory by remember(initialCategory) { mutableStateOf(initialCategory) }

    val languageSdks = remember(toolchainPlugins) {
        toolchainPlugins.filter { it.category == PluginCategory.LANGUAGE_SDK }
    }
    val ideExtensions = remember(toolchainPlugins) {
        toolchainPlugins.filter { it.category == PluginCategory.IDE_EXTENSION }
    }
    val installedSdkCount = remember(languageSdks) { languageSdks.count { it.isInstalled } }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1C1917),
        tonalElevation = 12.dp,
        title = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Studio Settings & SDK Manager",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFE6E1DC)
                        )
                        Text(
                            text = "CodeAssist Toolchain • iOS Grouped Preferences",
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 10.sp,
                            color = Color(0xFFA39C93)
                        )
                    }
                    Surface(
                        color = Color(0xFF283728),
                        shape = RoundedCornerShape(999.dp),
                        border = BorderStroke(0.5.dp, Color(0xFF6A8759))
                    ) {
                        Text(
                            text = "$installedSdkCount/${languageSdks.size} Engines Active",
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF81C784),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }

                // iOS-style Segmented Category Pill Selector
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF141210))
                        .border(0.5.dp, Color(0xFF36312C), RoundedCornerShape(10.dp))
                        .padding(3.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    StudioSettingsCategory.entries.forEach { cat ->
                        val isSelected = activeCategory == cat
                        Surface(
                            color = if (isSelected) Color(0xFF332C27) else Color.Transparent,
                            shape = RoundedCornerShape(8.dp),
                            border = if (isSelected) BorderStroke(0.5.dp, Color(0xFFD4883A).copy(alpha = 0.6f)) else null,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { activeCategory = cat }
                                .testTag("settings_cat_${cat.id}")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                            ) {
                                Icon(
                                    imageVector = cat.icon,
                                    contentDescription = null,
                                    tint = if (isSelected) Color(0xFFD4883A) else Color(0xFFA39C93),
                                    modifier = Modifier.size(12.dp)
                                )
                                Text(
                                    text = cat.label,
                                    fontFamily = JetBrainsMonoFontFamily,
                                    fontSize = 10.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) Color(0xFFE6E1DC) else Color(0xFFA39C93)
                                )
                            }
                        }
                    }
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 470.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // =========================================================================
                // GROUP 1: ON-DEVICE LANGUAGE ENGINES & PLUGIN MODULES
                // =========================================================================
                if (activeCategory == StudioSettingsCategory.ALL || activeCategory == StudioSettingsCategory.SDK_PLUGINS) {
                    IosSettingsGroupCard(
                        headerTitle = "ON-DEVICE LANGUAGE ENGINES & PLUGINS",
                        headerSubtitle = "All 6 engines execute variables, loops, functions & stdin natively on-device via our built-in AST interpreter (zero fake downloads).",
                        trailingAction = {
                            if (languageSdks.any { !it.isInstalled }) {
                                Surface(
                                    color = Color(0xFFD4883A).copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(6.dp),
                                    border = BorderStroke(0.5.dp, Color(0xFFD4883A)),
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .clickable(onClick = onInstallAllSdks)
                                        .testTag("install_all_sdks_button")
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = "Enable All Engines",
                                            tint = Color(0xFFD4883A),
                                            modifier = Modifier.size(11.dp)
                                        )
                                        Text(
                                            text = "Enable All",
                                            fontFamily = JetBrainsMonoFontFamily,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFFFFC66D)
                                        )
                                    }
                                }
                            }
                        }
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Surface(
                                color = Color(0xFF1A1E24),
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(0.75.dp, Color(0xFF334155)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(10.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = "Why no 90MB compiler download?",
                                        fontFamily = JetBrainsMonoFontFamily,
                                        fontSize = 10.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF7CB7FF)
                                    )
                                    Text(
                                        text = "Android W^X / SELinux & Google Play policy block apps from downloading & executing raw desktop compiler binaries (gcc, rustc, python3) in app storage. KtScribe bundles a real multi-language AST interpreter on-device, and provides 1-tap 'Termux Cmd' export in Terminal if you want bare-metal Linux binaries via Termux:\n$ pkg install openjdk-17 kotlin python clang rust dart",
                                        fontSize = 10.sp,
                                        lineHeight = 14.sp,
                                        color = Color(0xFFC5BEB6)
                                    )
                                }
                            }

                            Text(
                                text = "Built-in Language Engines",
                                fontFamily = JetBrainsMonoFontFamily,
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFD4883A)
                            )
                            languageSdks.forEach { plugin ->
                                ToolchainPluginRowItem(
                                    plugin = plugin,
                                    onActionClick = { onInstallOrTogglePlugin(plugin.id) }
                                )
                            }

                            Spacer(modifier = Modifier.height(4.dp))
                            HorizontalDivider(thickness = 0.5.dp, color = Color(0xFF36312C))
                            Spacer(modifier = Modifier.height(2.dp))

                            Text(
                                text = "Built-in IDE Modules",
                                fontFamily = JetBrainsMonoFontFamily,
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF7CB7FF)
                            )
                            ideExtensions.forEach { plugin ->
                                ToolchainPluginRowItem(
                                    plugin = plugin,
                                    onActionClick = { onInstallOrTogglePlugin(plugin.id) }
                                )
                            }
                        }
                    }
                }

                // =========================================================================
                // GROUP 2: APPEARANCE & 6 CURATED STUDIO PALETTES
                // =========================================================================
                if (activeCategory == StudioSettingsCategory.ALL || activeCategory == StudioSettingsCategory.APPEARANCE) {
                    IosSettingsGroupCard(
                        headerTitle = "APPEARANCE & STUDIO THEMES",
                        headerSubtitle = "Switch between Dark, Light, or System mode and 6 curated IDE color palettes"
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text(
                                text = "Appearance Mode",
                                fontFamily = JetBrainsMonoFontFamily,
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFE6E1DC)
                            )
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                ThemeMode.entries.forEach { mode ->
                                    FilterChip(
                                        selected = currentThemeMode == mode,
                                        onClick = { onSelectThemeMode(mode) },
                                        label = { Text(mode.label, fontSize = 11.sp) },
                                        modifier = Modifier.testTag("theme_chip_${mode.name.lowercase()}")
                                    )
                                }
                            }

                            HorizontalDivider(thickness = 0.5.dp, color = Color(0xFF36312C))

                            Text(
                                text = "Curated Studio Palettes (6 Themes)",
                                fontFamily = JetBrainsMonoFontFamily,
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFD4883A)
                            )

                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                AppThemeId.entries.forEach { theme ->
                                    val isSelected = currentAppTheme == theme
                                    Card(
                                        shape = RoundedCornerShape(10.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (isSelected) {
                                                Color(0xFF332922)
                                            } else {
                                                Color(0xFF1A1715)
                                            }
                                        ),
                                        border = BorderStroke(
                                            width = if (isSelected) 1.5.dp else 0.5.dp,
                                            color = if (isSelected) Color(0xFFD4883A) else Color(0xFF36312C)
                                        ),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(10.dp))
                                            .clickable { onSelectAppTheme(theme) }
                                            .testTag("theme_card_${theme.name.lowercase()}")
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(9.dp),
                                            verticalArrangement = Arrangement.spacedBy(5.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(
                                                        text = theme.displayName,
                                                        fontFamily = JetBrainsMonoFontFamily,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 11.5.sp,
                                                        color = Color(0xFFE6E1DC)
                                                    )
                                                    Text(
                                                        text = theme.tagline,
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = Color(0xFFA39C93)
                                                    )
                                                }

                                                Row(
                                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    listOf(
                                                        Color(android.graphics.Color.parseColor(theme.previewPrimaryHex)),
                                                        Color(android.graphics.Color.parseColor(theme.previewBgHex)),
                                                        Color(android.graphics.Color.parseColor(theme.previewSurfaceHex))
                                                    ).forEach { swatch ->
                                                        Box(
                                                            modifier = Modifier
                                                                .size(13.dp)
                                                                .clip(CircleShape)
                                                                .background(swatch)
                                                                .border(0.5.dp, Color.White.copy(alpha = 0.3f), CircleShape)
                                                        )
                                                    }
                                                }
                                            }

                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .horizontalScroll(rememberScrollState()),
                                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                                            ) {
                                                theme.getSwatches().forEach { swatch ->
                                                    Surface(
                                                        color = Color(0xFF141210),
                                                        shape = RoundedCornerShape(5.dp),
                                                        border = BorderStroke(0.5.dp, Color(0xFF36312C))
                                                    ) {
                                                        Row(
                                                            verticalAlignment = Alignment.CenterVertically,
                                                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
                                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                                        ) {
                                                            Box(
                                                                modifier = Modifier
                                                                    .size(8.dp)
                                                                    .clip(CircleShape)
                                                                    .background(swatch.color)
                                                            )
                                                            Text(
                                                                text = "${swatch.name}: ${swatch.hexCode}",
                                                                fontFamily = JetBrainsMonoFontFamily,
                                                                fontSize = 8.sp,
                                                                color = Color(0xFFC5BEB6)
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // =========================================================================
                // GROUP 3: EDITOR, DEFAULT LANGUAGE & CARET TRACKING
                // =========================================================================
                if (activeCategory == StudioSettingsCategory.ALL || activeCategory == StudioSettingsCategory.EDITOR) {
                    IosSettingsGroupCard(
                        headerTitle = "EDITOR, DEFAULT LANGUAGE & CARET TRACKING",
                        headerSubtitle = "Configure default language for new files and automatic keyboard caret scrolling"
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "Default Language for New Files",
                                fontFamily = JetBrainsMonoFontFamily,
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFE6E1DC)
                            )
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                LanguageType.entries.forEach { lang ->
                                    FilterChip(
                                        selected = selectedDefaultLang == lang,
                                        onClick = {
                                            selectedDefaultLang = lang
                                            onSelectDefaultLanguage(lang)
                                        },
                                        label = { Text("${lang.displayName} (${lang.extension})", fontSize = 10.5.sp) },
                                        leadingIcon = {
                                            Box(
                                                modifier = Modifier
                                                    .size(8.dp)
                                                    .clip(CircleShape)
                                                    .background(lang.badgeColor)
                                            )
                                        },
                                        modifier = Modifier.testTag("default_lang_${lang.id}")
                                    )
                                }
                            }

                            HorizontalDivider(thickness = 0.5.dp, color = Color(0xFF36312C))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Real-Time Caret Auto-Scroll & IME Tracking",
                                        fontFamily = JetBrainsMonoFontFamily,
                                        fontSize = 10.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFE6E1DC)
                                    )
                                    Text(
                                        text = "Automatically keeps the active line and completion popup visible above the soft keyboard as you type on any line.",
                                        fontSize = 10.sp,
                                        color = Color(0xFFA39C93)
                                    )
                                }
                                Surface(
                                    color = Color(0xFF283728),
                                    shape = RoundedCornerShape(6.dp),
                                    border = BorderStroke(0.5.dp, Color(0xFF6A8759))
                                ) {
                                    Text(
                                        text = "Active",
                                        fontFamily = JetBrainsMonoFontFamily,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF81C784),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // =========================================================================
                // GROUP 4: KEYBOARD SHORTCUTS (CTRL + LETTER)
                // =========================================================================
                if (activeCategory == StudioSettingsCategory.ALL || activeCategory == StudioSettingsCategory.SHORTCUTS) {
                    IosSettingsGroupCard(
                        headerTitle = "KEYBOARD SHORTCUTS",
                        headerSubtitle = "Active new-file shortcut: Ctrl + $currentShortcutLetter"
                    ) {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            ('A'..'Z').forEach { letter ->
                                val isSelected = letter == currentShortcutLetter
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(
                                            if (isSelected) Color(0xFFD4883A) else Color(0xFF1A1715)
                                        )
                                        .border(
                                            width = 1.dp,
                                            color = if (isSelected) Color(0xFFFFC66D) else Color(0xFF36312C),
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                        .clickable { onSelectShortcutLetter(letter) }
                                        .testTag("shortcut_letter_$letter")
                                ) {
                                    Text(
                                        text = letter.toString(),
                                        fontFamily = JetBrainsMonoFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = if (isSelected) Color(0xFF161412) else Color(0xFFE6E1DC)
                                    )
                                }
                            }
                        }
                    }
                }

                // =========================================================================
                // GROUP 5: VAULT AUTO-SAVE & AI QUOTA
                // =========================================================================
                if (activeCategory == StudioSettingsCategory.ALL || activeCategory == StudioSettingsCategory.VAULT_AI) {
                    IosSettingsGroupCard(
                        headerTitle = "VAULT PERSISTENCE & AI QUOTA",
                        headerSubtitle = "30-second automatic backup & Gemini 2.5 Flash RPM rate protection"
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = "• Auto-saves every 30 seconds while editing, plus immediately when minimizing the app.",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFFC5BEB6)
                            )
                            Text(
                                text = "Free-Tier RPM Quota: $remainingAiQuota / ${AiSectionNamingService.FREE_TIER_SAFE_RPM_LIMIT} requests per min",
                                fontFamily = JetBrainsMonoFontFamily,
                                fontSize = 10.5.sp,
                                color = Color(0xFFD4883A),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("close_settings_dialog_button")
            ) {
                Text(
                    text = "Done",
                    fontFamily = JetBrainsMonoFontFamily,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFD4883A)
                )
            }
        }
    )
}

@Composable
fun IosSettingsGroupCard(
    headerTitle: String,
    headerSubtitle: String? = null,
    trailingAction: (@Composable () -> Unit)? = null,
    content: @Composable () -> Unit
) {
    Surface(
        color = Color(0xFF24201D),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(0.75.dp, Color(0xFF3A342E)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = headerTitle,
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFFD4883A),
                        letterSpacing = 0.6.sp
                    )
                    if (!headerSubtitle.isNullOrBlank()) {
                        Text(
                            text = headerSubtitle,
                            fontSize = 10.5.sp,
                            color = Color(0xFFA39C93),
                            lineHeight = 14.sp
                        )
                    }
                }
                if (trailingAction != null) {
                    Spacer(modifier = Modifier.width(8.dp))
                    trailingAction()
                }
            }
            HorizontalDivider(thickness = 0.5.dp, color = Color(0xFF36312C))
            content()
        }
    }
}

@Composable
fun ToolchainPluginRowItem(
    plugin: ToolchainPluginItem,
    onActionClick: () -> Unit
) {
    Surface(
        color = if (plugin.isInstalled) Color(0xFF1E241E) else Color(0xFF1A1715),
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(
            width = 0.75.dp,
            color = if (plugin.isInstalled) Color(0xFF4E6E4E) else Color(0xFF36312C)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("plugin_card_${plugin.id}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = plugin.name,
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFE6E1DC)
                        )
                        Surface(
                            color = Color(0xFF2B2623),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "${plugin.version} • ${plugin.sizeLabel}",
                                fontFamily = JetBrainsMonoFontFamily,
                                fontSize = 8.5.sp,
                                color = Color(0xFFA39C93),
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                            )
                        }
                    }
                    Text(
                        text = plugin.subtitle,
                        fontSize = 10.sp,
                        color = Color(0xFFC5BEB6)
                    )
                    Text(
                        text = "Binary: ${plugin.binaryCommand}  •  Debugger: ${plugin.debuggerName}",
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 9.sp,
                        color = Color(0xFF8CA6B8)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                if (plugin.isDownloading) {
                    Surface(
                        color = Color(0xFF332922),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(0.5.dp, Color(0xFFD4883A))
                    ) {
                        Text(
                            text = "${(plugin.downloadProgress * 100).toInt()}%",
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFFC66D),
                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp)
                        )
                    }
                } else {
                    Surface(
                        color = if (plugin.isInstalled) Color(0xFF283728) else Color(0xFFD4883A),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(
                            0.5.dp,
                            if (plugin.isInstalled) Color(0xFF6A8759) else Color(0xFFFFC66D)
                        ),
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable(onClick = onActionClick)
                            .testTag("plugin_action_${plugin.id}")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = if (plugin.isInstalled) Color(0xFF81C784) else Color(0xFF161412),
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                text = if (plugin.isInstalled) "Active" else "Enable",
                                fontFamily = JetBrainsMonoFontFamily,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (plugin.isInstalled) Color(0xFF81C784) else Color(0xFF161412)
                            )
                        }
                    }
                }
            }

            if (plugin.isDownloading) {
                LinearProgressIndicator(
                    progress = { plugin.downloadProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    color = Color(0xFFD4883A),
                    trackColor = Color(0xFF2B2623)
                )
            }
        }
    }
}
