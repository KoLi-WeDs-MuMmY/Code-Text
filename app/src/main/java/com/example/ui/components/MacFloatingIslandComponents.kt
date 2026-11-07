package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.FolderSpecial
import androidx.compose.material.icons.filled.FormatAlignCenter
import androidx.compose.material.icons.filled.FormatAlignLeft
import androidx.compose.material.icons.filled.FormatAlignRight
import androidx.compose.material.icons.filled.FormatBold
import androidx.compose.material.icons.filled.FormatItalic
import androidx.compose.material.icons.filled.FormatUnderlined
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.OpenInFull
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Splitscreen
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.VerticalSplit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.code.DiagnosticSeverity
import com.example.code.KotlinCodeEngine
import com.example.data.local.VaultInspectionResult
import com.example.model.CodeSectionUiModel
import com.example.model.CommandPaletteFilter
import com.example.model.DocumentDetailUiModel
import com.example.model.DocumentSummaryUiModel
import com.example.model.EditorFontChoice
import com.example.model.ExplanationWindowMode
import com.example.model.IslandDropdownMenu
import com.example.model.TextAlignmentChoice
import com.example.model.TextFormattingStyle
import com.example.ui.theme.JetBrainsMonoFontFamily
import kotlin.math.roundToInt

// macOS Dynamic Island & Floating Glass Aesthetics
private val IslandPillBg = Color(0xF507090E)
private val IslandPillBorder = Color(0xFF283142)
private val IslandActiveChipBg = Color(0xFF1B2436)
private val IslandGlassWindowBg = Color(0xF7161922)
private val IslandGlassHeaderBg = Color(0xFF1F2430)
private val IslandAccentBlue = Color(0xFF0A84FF)
private val IslandAccentEmerald = Color(0xFF10B981)
private val IslandAccentViolet = Color(0xFFA78BFA)
private val IslandAccentAmber = Color(0xFFFFBD2E)
private val MacRedDot = Color(0xFFFF5F56)
private val MacYellowDot = Color(0xFFFFBD2E)
private val MacGreenDot = Color(0xFF27C93F)

/**
 * Top-center macOS Dynamic Island / Notch Pill.
 * Clicking any option drops down a resizable macOS floating window directly below the Island.
 */
@Composable
fun MacFloatingDynamicIsland(
    activeFolder: DocumentDetailUiModel,
    activeSection: CodeSectionUiModel?,
    activeDropdown: IslandDropdownMenu,
    explanationWindowMode: ExplanationWindowMode,
    syntaxErrorCount: Int,
    hasUnsavedChanges: Boolean,
    secondsUntilAutoSave: Int,
    shortcutLetter: Char,
    onToggleDropdown: (IslandDropdownMenu) -> Unit,
    onCycleExplanationMode: () -> Unit,
    onSetExplanationMode: (ExplanationWindowMode) -> Unit,
    onRunKotlinCode: () -> Unit,
    onOpenAllFoldersOverview: () -> Unit,
    onInsertNewSection: () -> Unit,
    onOpenVaultModal: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Outer Notch / Floating Island Pill
        Surface(
            color = IslandPillBg,
            shape = RoundedCornerShape(26.dp),
            shadowElevation = 14.dp,
            modifier = Modifier
                .padding(horizontal = 10.dp, vertical = 6.dp)
                .border(
                    width = 1.dp,
                    brush = Brush.horizontalGradient(
                        listOf(
                            Color(0xFF38BDF8).copy(alpha = 0.45f),
                            IslandPillBorder,
                            Color(0xFFA78BFA).copy(alpha = 0.45f)
                        )
                    ),
                    shape = RoundedCornerShape(26.dp)
                )
                .testTag("mac_floating_dynamic_island")
        ) {
            Row(
                modifier = Modifier
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 12.dp, vertical = 7.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // macOS Traffic Light Dots on the left of the Island (Red = All Folders Grid, Yellow = Minimize Overlays, Green = Split View)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                    modifier = Modifier.padding(end = 2.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(11.dp)
                            .clip(CircleShape)
                            .background(MacRedDot)
                            .clickable(onClick = onOpenAllFoldersOverview)
                            .testTag("back_to_documents_button")
                    )
                    Box(
                        modifier = Modifier
                            .size(11.dp)
                            .clip(CircleShape)
                            .background(MacYellowDot)
                            .clickable { onSetExplanationMode(ExplanationWindowMode.HIDDEN) }
                    )
                    Box(
                        modifier = Modifier
                            .size(11.dp)
                            .clip(CircleShape)
                            .background(MacGreenDot)
                            .clickable { onCycleExplanationMode() }
                    )
                }

                // 1. Folder & Sections Dropdown Trigger
                IslandActionPill(
                    icon = Icons.Default.FolderOpen,
                    label = activeFolder.title.take(18),
                    badge = if (activeSection != null) "§${activeSection.orderIndex + 1}/${activeFolder.sections.size}" else "${activeFolder.sections.size}",
                    isSelected = activeDropdown == IslandDropdownMenu.FOLDER_AND_SECTIONS,
                    accentColor = IslandAccentBlue,
                    onClick = { onToggleDropdown(IslandDropdownMenu.FOLDER_AND_SECTIONS) },
                    testTag = "island_folder_dropdown_button"
                )

                // 2. Command Palette Trigger (⌘P)
                IslandActionPill(
                    icon = Icons.Default.Search,
                    label = "⌘P Palette",
                    badge = null,
                    isSelected = activeDropdown == IslandDropdownMenu.COMMAND_PALETTE,
                    accentColor = IslandAccentViolet,
                    onClick = { onToggleDropdown(IslandDropdownMenu.COMMAND_PALETTE) },
                    testTag = "island_command_palette_button"
                )

                // 3. Explanation Floating / 2-Window Split Trigger
                IslandActionPill(
                    icon = when (explanationWindowMode) {
                        ExplanationWindowMode.FLOATING_WINDOW -> Icons.Default.Description
                        ExplanationWindowMode.SPLIT_SCREEN -> Icons.Default.VerticalSplit
                        ExplanationWindowMode.HIDDEN -> Icons.Default.Description
                    },
                    label = when (explanationWindowMode) {
                        ExplanationWindowMode.FLOATING_WINDOW -> "Explain: Float"
                        ExplanationWindowMode.SPLIT_SCREEN -> "Explain: 2-Win"
                        ExplanationWindowMode.HIDDEN -> "Explain: Off"
                    },
                    badge = activeSection?.effectiveLearnedConceptTag?.take(10),
                    isSelected = explanationWindowMode != ExplanationWindowMode.HIDDEN,
                    accentColor = IslandAccentEmerald,
                    onClick = onCycleExplanationMode,
                    testTag = "island_explanation_window_toggle"
                )

                // 4. Add New Code Section (+ Ctrl+K)
                IslandActionPill(
                    icon = Icons.Default.Add,
                    label = "+Section",
                    badge = "^$shortcutLetter",
                    isSelected = false,
                    accentColor = Color(0xFF38BDF8),
                    onClick = onInsertNewSection,
                    testTag = "toolbar_insert_code_block_button"
                )

                // 5. Run in Apple Terminal Console
                IslandActionPill(
                    icon = Icons.Default.PlayArrow,
                    label = "Run",
                    badge = if (syntaxErrorCount > 0) "⚠$syntaxErrorCount" else "✓",
                    isSelected = activeDropdown == IslandDropdownMenu.TERMINAL_OUTPUT,
                    accentColor = if (syntaxErrorCount > 0) MacRedDot else IslandAccentEmerald,
                    onClick = onRunKotlinCode,
                    testTag = "island_run_code_button"
                )

                // 6. AI Studio Dropdown
                IslandActionPill(
                    icon = Icons.Default.AutoAwesome,
                    label = "AI Studio",
                    badge = null,
                    isSelected = activeDropdown == IslandDropdownMenu.AI_STUDIO,
                    accentColor = IslandAccentAmber,
                    onClick = { onToggleDropdown(IslandDropdownMenu.AI_STUDIO) },
                    testTag = "island_ai_studio_button"
                )

                // 7. Hidden Vault ("xyz") Status & Auto-Save Pill
                Surface(
                    color = Color.White.copy(alpha = 0.07f),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .clickable(onClick = onOpenVaultModal)
                        .testTag("autosave_status_pill")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Hidden Vault Status",
                            tint = if (hasUnsavedChanges) IslandAccentAmber else IslandAccentEmerald,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (hasUnsavedChanges) "${secondsUntilAutoSave}s" else "Vault✓",
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFE6EDF3)
                        )
                    }
                }

                // 8. Shortcut & Theme Settings
                IconButton(
                    onClick = onOpenSettings,
                    modifier = Modifier
                        .size(26.dp)
                        .testTag("editor_shortcut_settings_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "IDE Settings",
                        tint = Color(0xFF94A3B8),
                        modifier = Modifier.size(15.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun IslandActionPill(
    icon: ImageVector,
    label: String,
    badge: String?,
    isSelected: Boolean,
    accentColor: Color,
    onClick: () -> Unit,
    testTag: String
) {
    Surface(
        color = if (isSelected) accentColor.copy(alpha = 0.22f) else IslandActiveChipBg,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .border(
                width = 1.dp,
                color = if (isSelected) accentColor else Color.White.copy(alpha = 0.08f),
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(onClick = onClick)
            .testTag(testTag)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isSelected) accentColor else Color(0xFFE2E8F0),
                modifier = Modifier.size(13.dp)
            )
            Text(
                text = label,
                fontFamily = JetBrainsMonoFontFamily,
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = Color(0xFFF8FAFC),
                maxLines = 1
            )
            if (!badge.isNullOrBlank()) {
                Surface(
                    color = accentColor.copy(alpha = 0.24f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = badge,
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = accentColor,
                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                    )
                }
            }
        }
    }
}

/**
 * Resizable macOS Drop-Down Window container that drops down from the Top Floating Dynamic Island.
 * Supports interactive drag-to-resize on both width and height plus quick S / M / L size presets!
 */
@Composable
fun ResizableIslandDropdownWindow(
    title: String,
    subtitle: String,
    initialWidthDp: Float = 520f,
    initialHeightDp: Float = 360f,
    onClose: () -> Unit,
    content: @Composable () -> Unit
) {
    val density = LocalDensity.current
    var widthDp by remember { mutableFloatStateOf(initialWidthDp) }
    var heightDp by remember { mutableFloatStateOf(initialHeightDp) }

    Surface(
        color = IslandGlassWindowBg,
        shape = RoundedCornerShape(18.dp),
        shadowElevation = 22.dp,
        modifier = Modifier
            .padding(horizontal = 12.dp, vertical = 4.dp)
            .widthIn(min = 280.dp, max = widthDp.dp)
            .fillMaxWidth()
            .heightIn(min = 200.dp, max = heightDp.dp)
            .border(
                width = 1.dp,
                color = Color(0xFF334155),
                shape = RoundedCornerShape(18.dp)
            )
            .testTag("resizable_island_dropdown_window")
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // macOS Floating Window Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(IslandGlassHeaderBg)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(11.dp)
                            .clip(CircleShape)
                            .background(MacRedDot)
                            .clickable(onClick = onClose)
                    )
                    Box(
                        modifier = Modifier
                            .size(11.dp)
                            .clip(CircleShape)
                            .background(MacYellowDot)
                            .clickable { heightDp = 240f }
                    )
                    Box(
                        modifier = Modifier
                            .size(11.dp)
                            .clip(CircleShape)
                            .background(MacGreenDot)
                            .clickable {
                                widthDp = 680f
                                heightDp = 500f
                            }
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        Text(
                            text = title,
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFF8FAFC)
                        )
                        Text(
                            text = subtitle,
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 10.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }
                }

                // Quick Window Size Presets (S / M / L) + Close
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    listOf("S" to 260f, "M" to 370f, "L" to 500f).forEach { (label, targetH) ->
                        Surface(
                            color = if (kotlin.math.abs(heightDp - targetH) < 40f) {
                                IslandAccentBlue.copy(alpha = 0.28f)
                            } else {
                                Color.White.copy(alpha = 0.06f)
                            },
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .clickable { heightDp = targetH }
                        ) {
                            Text(
                                text = label,
                                fontFamily = JetBrainsMonoFontFamily,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFE2E8F0),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    IconButton(onClick = onClose, modifier = Modifier.size(24.dp)) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close Window",
                            tint = Color(0xFF94A3B8),
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }

            // Main Dropdown Body
            Box(
                modifier = Modifier
                    .weight(1f, fill = false)
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                content()
            }

            // Interactive Drag-to-Resize Bottom Grip Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF11151E))
                    .pointerInput(Unit) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            val dxDp = with(density) { dragAmount.x.toDp().value }
                            val dyDp = with(density) { dragAmount.y.toDp().value }
                            widthDp = (widthDp + dxDp).coerceIn(300f, 760f)
                            heightDp = (heightDp + dyDp).coerceIn(220f, 620f)
                        }
                    }
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "⇥ Drag bar or corner to resize window (${widthDp.roundToInt()}×${heightDp.roundToInt()} dp)",
                    fontFamily = JetBrainsMonoFontFamily,
                    fontSize = 9.5.sp,
                    color = Color(0xFF64748B)
                )
                Icon(
                    imageVector = Icons.Default.OpenInFull,
                    contentDescription = "Resize Window Handle",
                    tint = Color(0xFF64748B),
                    modifier = Modifier.size(12.dp)
                )
            }
        }
    }
}

/**
 * Floating Command Palette Window (Modeled directly after the user's attached screenshot:
 * "Run a command, jump to a file or symbol..." with filter pills All, Commands, Folders, Symbols, Concepts).
 */
@Composable
fun CommandPaletteDropdownContent(
    activeFolder: DocumentDetailUiModel,
    allFolders: List<DocumentSummaryUiModel>,
    activeSection: CodeSectionUiModel?,
    onExecuteCommand: (String) -> Unit,
    onSelectFolder: (Long) -> Unit,
    onJumpToSection: (Long) -> Unit,
    onClose: () -> Unit
) {
    var query by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf(CommandPaletteFilter.ALL) }

    data class PaletteEntry(
        val id: String,
        val category: CommandPaletteFilter,
        val title: String,
        val subtitle: String,
        val icon: ImageVector,
        val action: () -> Unit
    )

    val entries = remember(activeFolder, allFolders, activeSection) {
        buildList {
            // Commands
            add(
                PaletteEntry(
                    id = "cmd_ask_ai",
                    category = CommandPaletteFilter.COMMANDS,
                    title = "Ask AI: Write Thorough Explanation & Concept Tag",
                    subtitle = "Gemini 3 Flash • Analyzes current Kotlin section",
                    icon = Icons.Default.AutoAwesome,
                    action = { onExecuteCommand("ai_explain") }
                )
            )
            add(
                PaletteEntry(
                    id = "cmd_refactor",
                    category = CommandPaletteFilter.COMMANDS,
                    title = "AI Refactor & Fix Syntax Errors",
                    subtitle = "Converts code to idiomatic Kotlin",
                    icon = Icons.Default.AutoFixHigh,
                    action = { onExecuteCommand("ai_refactor") }
                )
            )
            add(
                PaletteEntry(
                    id = "cmd_format",
                    category = CommandPaletteFilter.COMMANDS,
                    title = "Format Kotlin Code ({ })",
                    subtitle = "Normalizes 4-space indentation and braces",
                    icon = Icons.Default.Code,
                    action = { onExecuteCommand("format_code") }
                )
            )
            add(
                PaletteEntry(
                    id = "cmd_split",
                    category = CommandPaletteFilter.COMMANDS,
                    title = "Toggle Split-Screen Two-Window Mode (◫ Set Aside)",
                    subtitle = "Divides screen into resizable Code & Explanation windows",
                    icon = Icons.Default.VerticalSplit,
                    action = { onExecuteCommand("toggle_split") }
                )
            )
            add(
                PaletteEntry(
                    id = "cmd_float",
                    category = CommandPaletteFilter.COMMANDS,
                    title = "Show Explanation as Draggable Floating Window",
                    subtitle = "macOS Notes-style resizable overlay window",
                    icon = Icons.Default.Description,
                    action = { onExecuteCommand("float_explain") }
                )
            )
            add(
                PaletteEntry(
                    id = "cmd_run",
                    category = CommandPaletteFilter.COMMANDS,
                    title = "Run Build & Execute in Apple Terminal",
                    subtitle = "Evaluates println() output and checks syntax",
                    icon = Icons.Default.PlayArrow,
                    action = { onExecuteCommand("run_code") }
                )
            )
            add(
                PaletteEntry(
                    id = "cmd_new_section",
                    category = CommandPaletteFilter.COMMANDS,
                    title = "New Kotlin Code Section in Folder",
                    subtitle = "Inserts a 4-line auto-expanding code block",
                    icon = Icons.Default.Add,
                    action = { onExecuteCommand("new_section") }
                )
            )
            add(
                PaletteEntry(
                    id = "cmd_vault",
                    category = CommandPaletteFilter.COMMANDS,
                    title = "Hidden Vault ('xyz') Recovery & Storage Settings",
                    subtitle = "Recover previous download data or manage hidden vault",
                    icon = Icons.Default.Security,
                    action = { onExecuteCommand("open_vault") }
                )
            )

            // Folders
            allFolders.forEach { folder ->
                add(
                    PaletteEntry(
                        id = "folder_${folder.id}",
                        category = CommandPaletteFilter.FOLDERS,
                        title = "📁 ${folder.title}",
                        subtitle = "${folder.sectionCount} Kotlin sections • ${folder.wordCount} words",
                        icon = Icons.Default.Folder,
                        action = { onSelectFolder(folder.id) }
                    )
                )
            }

            // Concepts / Sections
            activeFolder.sections.forEach { sec ->
                add(
                    PaletteEntry(
                        id = "sec_${sec.id}",
                        category = CommandPaletteFilter.CONCEPTS,
                        title = "§${sec.orderIndex + 1} • ${sec.effectiveLearnedConceptTag}",
                        subtitle = sec.displayTitle,
                        icon = Icons.Default.School,
                        action = { onJumpToSection(sec.id) }
                    )
                )
            }

            // Symbols extracted from active code
            val symbolRegex = Regex("""\b(?:fun|class|interface|object|val|var)\s+([A-Za-z_][A-Za-z0-9_]*)""")
            activeFolder.sections.forEach { sec ->
                symbolRegex.findAll(sec.codeContent).forEach { match ->
                    val sym = match.groupValues[1]
                    add(
                        PaletteEntry(
                            id = "sym_${sec.id}_$sym",
                            category = CommandPaletteFilter.SYMBOLS,
                            title = "$sym  (§${sec.orderIndex + 1})",
                            subtitle = "Symbol in ${sec.displayTitle}",
                            icon = Icons.Default.Terminal,
                            action = { onJumpToSection(sec.id) }
                        )
                    )
                }
            }
        }
    }

    val filtered = entries.filter { item ->
        val matchesCategory = selectedFilter == CommandPaletteFilter.ALL || item.category == selectedFilter
        val matchesQuery = query.isBlank() ||
            item.title.contains(query, ignoreCase = true) ||
            item.subtitle.contains(query, ignoreCase = true)
        matchesCategory && matchesQuery
    }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            placeholder = {
                Text(
                    text = "Run a command, jump to a folder, section, or symbol...",
                    fontFamily = JetBrainsMonoFontFamily,
                    fontSize = 12.sp,
                    color = Color(0xFF94A3B8)
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    tint = Color(0xFF94A3B8),
                    modifier = Modifier.size(18.dp)
                )
            },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color(0xFF0F131A),
                unfocusedContainerColor = Color(0xFF0F131A),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = IslandAccentBlue,
                unfocusedBorderColor = Color(0xFF2D3748)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("command_palette_search_input")
        )

        // Filter Chips (All, Commands, Folders, Symbols, Concepts)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            CommandPaletteFilter.entries.forEach { filter ->
                val selected = selectedFilter == filter
                Surface(
                    color = if (selected) Color(0xFF5C4033) else Color(0xFF1E2430),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .border(
                            width = 1.dp,
                            color = if (selected) Color(0xFFD97706) else Color(0xFF334155),
                            shape = RoundedCornerShape(16.dp)
                        )
                        .clickable { selectedFilter = filter }
                ) {
                    Text(
                        text = filter.label,
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 11.sp,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                        color = Color(0xFFF8FAFC),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                    )
                }
            }
        }

        HorizontalDivider(color = Color(0xFF283142))

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 260.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(filtered, key = { it.id }) { item ->
                Surface(
                    color = Color.White.copy(alpha = 0.04f),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .clickable {
                            item.action()
                            onClose()
                        }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 9.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = null,
                            tint = IslandAccentBlue,
                            modifier = Modifier.size(16.dp)
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = item.title,
                                fontFamily = JetBrainsMonoFontFamily,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFFF1F5F9)
                            )
                            Text(
                                text = item.subtitle,
                                fontFamily = JetBrainsMonoFontFamily,
                                fontSize = 10.sp,
                                color = Color(0xFF94A3B8),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Folder & Sections Explorer Drop-Down Window inside the Floating Island.
 * Lets the user rename the active Folder, switch between Folders, create a new Folder,
 * or switch between Kotlin code sections inside the active Folder.
 */
@Composable
fun FolderAndSectionsDropdownContent(
    activeFolder: DocumentDetailUiModel,
    allFolders: List<DocumentSummaryUiModel>,
    activeSectionId: Long?,
    onFolderTitleChange: (String) -> Unit,
    onSelectFolder: (Long) -> Unit,
    onCreateNewFolder: (String) -> Unit,
    onOpenAllFoldersGrid: () -> Unit,
    onSelectSection: (Long) -> Unit,
    onInsertNewSection: () -> Unit,
    onDeleteSection: (Long) -> Unit
) {
    var newFolderTitle by remember { mutableStateOf("") }
    var showCreateFolderRow by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Active Folder Title Editor + All Folders Grid Button
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.FolderSpecial,
                contentDescription = null,
                tint = IslandAccentBlue,
                modifier = Modifier.size(20.dp)
            )
            OutlinedTextField(
                value = activeFolder.title,
                onValueChange = onFolderTitleChange,
                label = { Text("Active Folder Name", fontSize = 10.sp) },
                singleLine = true,
                shape = RoundedCornerShape(10.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFF0F131A),
                    unfocusedContainerColor = Color(0xFF0F131A),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                modifier = Modifier
                    .weight(1f)
                    .testTag("document_title_input")
            )
            OutlinedButton(
                onClick = onOpenAllFoldersGrid,
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.GridView,
                    contentDescription = "All Folders Grid",
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Folders", fontSize = 11.sp)
            }
        }

        // Sections inside Active Folder
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "KOTLIN SECTIONS IN THIS FOLDER (${activeFolder.sections.size})",
                fontFamily = JetBrainsMonoFontFamily,
                fontSize = 10.5.sp,
                fontWeight = FontWeight.Bold,
                color = IslandAccentEmerald
            )
            TextButton(onClick = onInsertNewSection) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Add Section", fontSize = 11.sp)
            }
        }

        activeFolder.sections.forEach { sec ->
            val isCurrent = sec.id == activeSectionId
            Surface(
                color = if (isCurrent) IslandAccentBlue.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.04f),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .border(
                        width = 1.dp,
                        color = if (isCurrent) IslandAccentBlue else Color.White.copy(alpha = 0.08f),
                        shape = RoundedCornerShape(10.dp)
                    )
                    .clickable { onSelectSection(sec.id) }
                    .testTag("jump_concept_chip_${sec.orderIndex}")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "§${sec.orderIndex + 1} • ${sec.displayTitle}",
                                fontFamily = JetBrainsMonoFontFamily,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Text(
                            text = "Concept: [${sec.effectiveLearnedConceptTag}] • ${sec.lineCount} lines",
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 10.sp,
                            color = IslandAccentEmerald
                        )
                    }
                    if (activeFolder.sections.size > 1) {
                        IconButton(
                            onClick = { onDeleteSection(sec.id) },
                            modifier = Modifier.size(26.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteOutline,
                                contentDescription = "Delete Section",
                                tint = Color(0xFF94A3B8),
                                modifier = Modifier.size(15.dp)
                            )
                        }
                    }
                }
            }
        }

        HorizontalDivider(color = Color(0xFF283142))

        // Switch or Create Another Folder
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "ALL WORKSPACE FOLDERS (${allFolders.size})",
                fontFamily = JetBrainsMonoFontFamily,
                fontSize = 10.5.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF94A3B8)
            )
            TextButton(onClick = { showCreateFolderRow = !showCreateFolderRow }) {
                Icon(Icons.Default.CreateNewFolder, contentDescription = null, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("New Folder", fontSize = 11.sp)
            }
        }

        AnimatedVisibility(visible = showCreateFolderRow) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                OutlinedTextField(
                    value = newFolderTitle,
                    onValueChange = { newFolderTitle = it },
                    placeholder = { Text("Folder name (e.g. Compose UI Lab)", fontSize = 11.sp) },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                Button(
                    onClick = {
                        onCreateNewFolder(newFolderTitle.ifBlank { "New Kotlin Folder" })
                        newFolderTitle = ""
                        showCreateFolderRow = false
                    }
                ) {
                    Text("Create")
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            allFolders.forEach { folder ->
                val isCurrentFolder = folder.id == activeFolder.id
                Surface(
                    color = if (isCurrentFolder) IslandAccentViolet.copy(alpha = 0.22f) else Color.White.copy(alpha = 0.05f),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .border(
                            width = 1.dp,
                            color = if (isCurrentFolder) IslandAccentViolet else Color.White.copy(alpha = 0.08f),
                            shape = RoundedCornerShape(10.dp)
                        )
                        .clickable { onSelectFolder(folder.id) }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Folder,
                            contentDescription = null,
                            tint = if (isCurrentFolder) IslandAccentViolet else IslandAccentAmber,
                            modifier = Modifier.size(15.dp)
                        )
                        Column {
                            Text(
                                text = folder.title,
                                fontFamily = JetBrainsMonoFontFamily,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "${folder.sectionCount} sections",
                                fontFamily = JetBrainsMonoFontFamily,
                                fontSize = 9.5.sp,
                                color = Color(0xFF94A3B8)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Draggable & Resizable macOS Floating Explanation Window (or Docked Split-Screen Pane).
 * Modeled after the user's attached macOS Notes window screenshot!
 * Includes:
 * - macOS Traffic Lights (`● ● ●`)
 * - Drag-to-move title bar (when floating)
 * - Drag-to-resize bottom-right corner & bottom grip (when floating)
 * - 1-Tap `◫ Split Screen (Set Aside)` toggle button to divide the screen into 2 resizable windows!
 */
@Composable
fun MacFloatingOrSplitExplanationWindow(
    section: CodeSectionUiModel,
    windowMode: ExplanationWindowMode,
    formattingStyle: TextFormattingStyle,
    onExplanationChange: (String) -> Unit,
    onConceptTagChange: (String) -> Unit,
    onRequestAiExplain: () -> Unit,
    onToggleSplitMode: () -> Unit,
    onCloseWindow: () -> Unit,
    onFontChoiceChange: (EditorFontChoice) -> Unit,
    onFontSizeChange: (Int) -> Unit,
    onToggleBold: () -> Unit,
    onToggleItalic: () -> Unit,
    onToggleUnderline: () -> Unit,
    onAlignmentChange: (TextAlignmentChoice) -> Unit,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }
    var floatWidthDp by remember { mutableFloatStateOf(390f) }
    var floatHeightDp by remember { mutableFloatStateOf(255f) }
    var isEditingTag by remember { mutableStateOf(false) }

    val isFloating = windowMode == ExplanationWindowMode.FLOATING_WINDOW

    val containerModifier = if (isFloating) {
        modifier
            .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
            .widthIn(min = 260.dp, max = floatWidthDp.dp)
            .fillMaxWidth(0.92f)
            .height(floatHeightDp.dp)
            .shadow(20.dp, RoundedCornerShape(16.dp))
    } else {
        modifier.fillMaxSize()
    }

    Surface(
        color = Color(0xF7161B24),
        shape = RoundedCornerShape(16.dp),
        modifier = containerModifier
            .border(
                width = 1.dp,
                color = Color(0xFF334155),
                shape = RoundedCornerShape(16.dp)
            )
            .testTag("mac_explanation_floating_window")
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // 1. macOS Window Title Bar (Draggable in Floating Mode!)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF1F2633))
                    .then(
                        if (isFloating) {
                            Modifier.pointerInput(Unit) {
                                detectDragGestures { change, dragAmount ->
                                    change.consume()
                                    offsetX = (offsetX + dragAmount.x).coerceIn(-280f, 280f)
                                    offsetY = (offsetY + dragAmount.y).coerceIn(-360f, 360f)
                                }
                            }
                        } else {
                            Modifier
                        }
                    )
                    .padding(horizontal = 10.dp, vertical = 7.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    // macOS Traffic Light Buttons
                    Box(
                        modifier = Modifier
                            .size(11.dp)
                            .clip(CircleShape)
                            .background(MacRedDot)
                            .clickable(onClick = onCloseWindow)
                    )
                    Box(
                        modifier = Modifier
                            .size(11.dp)
                            .clip(CircleShape)
                            .background(MacYellowDot)
                            .clickable {
                                if (isFloating) floatHeightDp = 175f else onCloseWindow()
                            }
                    )
                    Box(
                        modifier = Modifier
                            .size(11.dp)
                            .clip(CircleShape)
                            .background(MacGreenDot)
                            .clickable(onClick = onToggleSplitMode)
                    )

                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isFloating) "📝 Floating Explanation (Drag to Move)" else "📝 Split Window • Explanation",
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFF8FAFC),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Set Aside / Split-Screen Two-Window Mode Toggle + AI Explain Button
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Surface(
                        color = IslandAccentEmerald.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(7.dp),
                        modifier = Modifier
                            .clip(RoundedCornerShape(7.dp))
                            .clickable(onClick = onRequestAiExplain)
                            .testTag("ai_explain_button_${section.orderIndex}")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = "AI Thorough Explain",
                                tint = IslandAccentEmerald,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "AI Explain",
                                fontFamily = JetBrainsMonoFontFamily,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = IslandAccentEmerald
                            )
                        }
                    }

                    Surface(
                        color = IslandAccentBlue.copy(alpha = 0.22f),
                        shape = RoundedCornerShape(7.dp),
                        modifier = Modifier
                            .clip(RoundedCornerShape(7.dp))
                            .clickable(onClick = onToggleSplitMode)
                            .testTag("toggle_split_screen_explanation_button")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                        ) {
                            Icon(
                                imageVector = if (isFloating) Icons.Default.VerticalSplit else Icons.Default.OpenInFull,
                                contentDescription = "Toggle 2-Window Split Screen",
                                tint = IslandAccentBlue,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = if (isFloating) "◫ Set Aside (2-Win)" else "Float Overlay",
                                fontFamily = JetBrainsMonoFontFamily,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = IslandAccentBlue
                            )
                        }
                    }
                }
            }

            // 2. Compact Rich-Text & Concept Tag Strip
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF121721))
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 10.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Learned Concept Tag Chip
                Surface(
                    color = IslandAccentViolet.copy(alpha = 0.18f),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .clickable { isEditingTag = !isEditingTag }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.School,
                            contentDescription = null,
                            tint = IslandAccentViolet,
                            modifier = Modifier.size(11.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        if (isEditingTag) {
                            BasicTextField(
                                value = section.explanationStyle.learnedConceptTag.ifBlank { section.effectiveLearnedConceptTag },
                                onValueChange = onConceptTagChange,
                                singleLine = true,
                                textStyle = TextStyle(
                                    fontFamily = JetBrainsMonoFontFamily,
                                    fontSize = 10.5.sp,
                                    color = Color.White
                                ),
                                cursorBrush = SolidColor(IslandAccentViolet),
                                modifier = Modifier.width(120.dp)
                            )
                        } else {
                            Text(
                                text = "Tag: ${section.effectiveLearnedConceptTag}",
                                fontFamily = JetBrainsMonoFontFamily,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = IslandAccentViolet
                            )
                        }
                    }
                }

                // Font Selector Pills
                EditorFontChoice.entries.forEach { font ->
                    val selected = formattingStyle.fontChoice == font
                    Surface(
                        color = if (selected) IslandAccentBlue.copy(alpha = 0.24f) else Color.White.copy(alpha = 0.05f),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .clickable { onFontChoiceChange(font) }
                    ) {
                        Text(
                            text = font.badge,
                            fontSize = 10.sp,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                            color = if (selected) IslandAccentBlue else Color(0xFFCBD5E1),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                // Bold / Italic / Underline
                IconButton(onClick = onToggleBold, modifier = Modifier.size(22.dp)) {
                    Icon(
                        imageVector = Icons.Default.FormatBold,
                        contentDescription = "Bold",
                        tint = if (formattingStyle.isBold) IslandAccentBlue else Color(0xFF94A3B8),
                        modifier = Modifier.size(14.dp)
                    )
                }
                IconButton(onClick = onToggleItalic, modifier = Modifier.size(22.dp)) {
                    Icon(
                        imageVector = Icons.Default.FormatItalic,
                        contentDescription = "Italic",
                        tint = if (formattingStyle.isItalic) IslandAccentBlue else Color(0xFF94A3B8),
                        modifier = Modifier.size(14.dp)
                    )
                }
                IconButton(onClick = onToggleUnderline, modifier = Modifier.size(22.dp)) {
                    Icon(
                        imageVector = Icons.Default.FormatUnderlined,
                        contentDescription = "Underline",
                        tint = if (formattingStyle.isUnderline) IslandAccentBlue else Color(0xFF94A3B8),
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            // 3. Scrollable Explanation Rich Text Body
            val textDecoration = when {
                formattingStyle.isUnderline && formattingStyle.isStrikethrough ->
                    TextDecoration.combine(listOf(TextDecoration.Underline, TextDecoration.LineThrough))
                formattingStyle.isUnderline -> TextDecoration.Underline
                formattingStyle.isStrikethrough -> TextDecoration.LineThrough
                else -> TextDecoration.None
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(12.dp)
            ) {
                BasicTextField(
                    value = section.explanationText,
                    onValueChange = onExplanationChange,
                    textStyle = TextStyle(
                        fontFamily = formattingStyle.fontChoice.toComposeFontFamily(),
                        fontSize = formattingStyle.fontSizeSp.sp,
                        fontWeight = if (formattingStyle.isBold) FontWeight.Bold else FontWeight.Normal,
                        fontStyle = if (formattingStyle.isItalic) FontStyle.Italic else FontStyle.Normal,
                        textDecoration = textDecoration,
                        textAlign = formattingStyle.alignment.toComposeTextAlign(),
                        color = Color(0xFFE2E8F0),
                        lineHeight = (formattingStyle.fontSizeSp + 6).sp
                    ),
                    cursorBrush = SolidColor(IslandAccentEmerald),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("section_explanation_input_${section.orderIndex}"),
                    decorationBox = { innerTextField ->
                        if (section.explanationText.isEmpty()) {
                            Text(
                                text = "Write your explanation & notes for §${section.orderIndex + 1} here, or tap 'AI Explain' above to generate a thorough step-by-step guide…",
                                style = TextStyle(
                                    fontFamily = formattingStyle.fontChoice.toComposeFontFamily(),
                                    fontSize = formattingStyle.fontSizeSp.sp,
                                    color = Color(0xFF64748B)
                                )
                            )
                        }
                        innerTextField()
                    }
                )
            }

            // 4. Interactive Resize Handle (when in Floating Mode)
            if (isFloating) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF0E131B))
                        .pointerInput(Unit) {
                            detectDragGestures { change, dragAmount ->
                                change.consume()
                                val dxDp = with(density) { dragAmount.x.toDp().value }
                                val dyDp = with(density) { dragAmount.y.toDp().value }
                                floatWidthDp = (floatWidthDp + dxDp).coerceIn(260f, 680f)
                                floatHeightDp = (floatHeightDp + dyDp).coerceIn(160f, 520f)
                            }
                        }
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "⤡ Drag to resize (${floatWidthDp.roundToInt()}×${floatHeightDp.roundToInt()} dp)",
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 9.5.sp,
                        color = Color(0xFF64748B)
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "Reset Pos",
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 9.5.sp,
                            color = IslandAccentBlue,
                            modifier = Modifier.clickable {
                                offsetX = 0f
                                offsetY = 0f
                            }
                        )
                        Icon(
                            imageVector = Icons.Default.OpenInFull,
                            contentDescription = "Resize Floating Explanation Window",
                            tint = Color(0xFF94A3B8),
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * First-Launch Storage Permission & Hidden Vault ("xyz" / ".ktscribe_vault_xyz") Recovery Dialog.
 * Also explains that `android:hasFragileUserData="true"` is enabled so uninstalling the app
 * displays the native checkbox to keep or delete local data.
 */
@Composable
fun FirstLaunchVaultRecoveryDialog(
    vaultInspection: VaultInspectionResult,
    onRecoverPreviousData: () -> Unit,
    onCreateNewVaultFolder: () -> Unit,
    onDismiss: () -> Unit
) {
    var storagePermissionGrantedInUi by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF141821),
        titleContentColor = Color.White,
        textContentColor = Color(0xFFCBD5E1),
        icon = {
            Icon(
                imageVector = Icons.Default.Security,
                contentDescription = null,
                tint = IslandAccentEmerald,
                modifier = Modifier.size(30.dp)
            )
        },
        title = {
            Text(
                text = if (!storagePermissionGrantedInUi) {
                    "Local Storage Permission & Hidden Vault"
                } else {
                    "Existing 'xyz' Vault Folder Detected!"
                },
                fontFamily = JetBrainsMonoFontFamily,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                if (!storagePermissionGrantedInUi) {
                    Text(
                        text = "KtScribe stores all of your Folders, Kotlin Code Sections, and AI Explanations 100% locally on your device inside a protected, obfuscated vault folder (\"xyz\" / .ktscribe_vault_xyz) that cannot be casually opened by normal file browsers.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFE2E8F0)
                    )
                    Surface(
                        color = Color(0xFF1E2636),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "✓ Keep Data on Uninstall Enabled",
                                fontFamily = JetBrainsMonoFontFamily,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = IslandAccentEmerald
                            )
                            Text(
                                text = "When uninstalling KtScribe, Android will show a checkbox asking whether to keep or delete your app data.",
                                fontSize = 11.sp,
                                color = Color(0xFF94A3B8)
                            )
                        }
                    }
                } else {
                    Surface(
                        color = IslandAccentEmerald.copy(alpha = 0.14f),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, IslandAccentEmerald.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "Folder Found: \"${vaultInspection.vaultFolderName}\"",
                                fontFamily = JetBrainsMonoFontFamily,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = IslandAccentEmerald
                            )
                            Text(
                                text = "Path: ${vaultInspection.vaultPathDisplay}",
                                fontFamily = JetBrainsMonoFontFamily,
                                fontSize = 10.sp,
                                color = Color(0xFF94A3B8)
                            )
                            Text(
                                text = "Recoverable Backup: ${vaultInspection.recoverableFolderCount} Folders • ${vaultInspection.recoverableSectionCount} Kotlin Sections",
                                fontFamily = JetBrainsMonoFontFamily,
                                fontSize = 10.5.sp,
                                color = Color.White
                            )
                        }
                    }
                    Text(
                        text = "Would you like to recover your previous folders from the existing 'xyz' vault or create a new hidden vault folder?",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFE2E8F0)
                    )
                }
            }
        },
        confirmButton = {
            if (!storagePermissionGrantedInUi) {
                Button(
                    onClick = { storagePermissionGrantedInUi = true },
                    colors = ButtonDefaults.buttonColors(containerColor = IslandAccentBlue),
                    modifier = Modifier.testTag("grant_vault_storage_permission_button")
                ) {
                    Text("Allow Vault Storage & Scan 'xyz'", fontWeight = FontWeight.Bold)
                }
            } else {
                Button(
                    onClick = onRecoverPreviousData,
                    colors = ButtonDefaults.buttonColors(containerColor = IslandAccentEmerald),
                    modifier = Modifier.testTag("recover_xyz_vault_button")
                ) {
                    Icon(Icons.Default.Restore, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Recover Previous Data", color = Color(0xFF042F2E), fontWeight = FontWeight.ExtraBold)
                }
            }
        },
        dismissButton = {
            if (!storagePermissionGrantedInUi) {
                TextButton(onClick = onDismiss) {
                    Text("Later", color = Color(0xFF94A3B8))
                }
            } else {
                OutlinedButton(
                    onClick = onCreateNewVaultFolder,
                    modifier = Modifier.testTag("create_new_vault_folder_button")
                ) {
                    Text("Create New Hidden Vault", color = Color.White)
                }
            }
        }
    )
}
