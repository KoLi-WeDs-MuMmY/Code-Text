package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import com.example.model.WindowPresetTier
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.FolderSpecial
import androidx.compose.material.icons.filled.FormatBold
import androidx.compose.material.icons.filled.FormatItalic
import androidx.compose.material.icons.filled.FormatUnderlined
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.OpenInFull
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.TransformOrigin
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
import androidx.compose.ui.zIndex
import com.example.data.local.VaultInspectionResult
import com.example.model.CodeSectionUiModel
import com.example.model.CommandPaletteFilter
import com.example.model.DocumentDetailUiModel
import com.example.model.DocumentSummaryUiModel
import com.example.model.EditorFontChoice
import com.example.model.ExplanationWindowMode
import com.example.model.FloatingWindowType
import com.example.model.IslandDropdownMenu
import com.example.model.TextAlignmentChoice
import com.example.model.TextFormattingStyle
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.Tier0OuterTrenchBg
import com.example.ui.theme.Tier2CompletionSelectedBg
import com.example.ui.theme.Tier3FloatingWindowActiveBevel
import com.example.ui.theme.Tier3FloatingWindowActiveBg
import com.example.ui.theme.Tier3FloatingWindowActiveHeader
import com.example.ui.theme.Tier3FloatingWindowInactiveBevel
import com.example.ui.theme.Tier3FloatingWindowInactiveBg
import com.example.ui.theme.Tier3FloatingWindowInactiveHeader
import kotlin.math.roundToInt

// Dynamic Window Palette (Reacts instantly to active theme selection)
private val OuterTrenchBg: Color @Composable get() = MaterialTheme.colorScheme.background
private val PillSurfaceBg: Color @Composable get() = MaterialTheme.colorScheme.surface
private val PillSelectedBg: Color @Composable get() = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
private val PillBorderColor: Color @Composable get() = MaterialTheme.colorScheme.outlineVariant
private val WarmAmberAccent: Color @Composable get() = MaterialTheme.colorScheme.primary
private val MutedSageAccent: Color @Composable get() = MaterialTheme.colorScheme.tertiary
private val MutedSteelAccent: Color @Composable get() = MaterialTheme.colorScheme.secondary
private val WarmCreamText: Color @Composable get() = MaterialTheme.colorScheme.onSurface
private val MutedTaupeText: Color @Composable get() = MaterialTheme.colorScheme.onSurfaceVariant
private val MacRedDot = Color(0xFFD9534F)
private val MacYellowDot = Color(0xFFD49B35)
private val MacGreenDot = Color(0xFF6A9955)

/**
 * Custom 3D Window Depth Modifier:
 * Draws a multi-layer ambient drop-shadow halo behind the floating window so it visually detaches
 * from the dark code block beneath it.
 */
fun Modifier.tactileWindowDepth(
    isForeground: Boolean,
    cornerRadiusDp: Float = 18f
): Modifier = this.drawBehind {
    val cornerPx = cornerRadiusDp.dp.toPx()
    val spreadPx = (if (isForeground) 12.dp else 6.dp).toPx()
    val offsetY = (if (isForeground) 10.dp else 5.dp).toPx()
    drawRoundRect(
        color = Color.Black.copy(alpha = if (isForeground) 0.62f else 0.38f),
        topLeft = Offset(-spreadPx * 0.5f, offsetY - spreadPx * 0.25f),
        size = Size(size.width + spreadPx, size.height + spreadPx),
        cornerRadius = CornerRadius(cornerPx + 4f, cornerPx + 4f)
    )
}

/**
 * Top Bar matching Sketch 1 & Sketch 2:
 * - Left: Top-left back arrow (`←`) + Active Folder/Vault status
 * - Right: 4 Horizontal Oval Pills (`Explain`, `Folders`, `AI Guide`, `Run ▶`) + `⌘P` Command Block
 */
@Composable
fun SketchTopBarWithFourPills(
    activeFolder: DocumentDetailUiModel,
    activeSection: CodeSectionUiModel?,
    openFloatingWindows: List<FloatingWindowType>,
    activeDropdown: IslandDropdownMenu,
    syntaxErrorCount: Int,
    hasUnsavedChanges: Boolean,
    secondsUntilAutoSave: Int,
    onBackToFolders: () -> Unit,
    onToggleFloatingWindow: (FloatingWindowType) -> Unit,
    onToggleCommandPalette: () -> Unit,
    onOpenVaultModal: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(OuterTrenchBg)
            // Generous bezel-safe breathing space so top buttons never clip at curved screen corners
            .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 8.dp)
            .testTag("mac_floating_dynamic_island"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Top-Left Back Arrow (`←` from Sketch 1) + Folder & Active `.ktscribe` File badge
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Surface(
                color = PillSurfaceBg,
                shape = RoundedCornerShape(14.dp),
                shadowElevation = 4.dp,
                modifier = Modifier
                    .clip(RoundedCornerShape(14.dp))
                    .border(1.dp, PillBorderColor, RoundedCornerShape(14.dp))
                    .clickable(onClick = onBackToFolders)
                    .testTag("back_to_documents_button")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 11.dp, vertical = 7.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back to Folders",
                        tint = WarmAmberAccent,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = activeSection?.ktScribeFileName ?: activeFolder.title.take(14),
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = WarmCreamText,
                        maxLines = 1
                    )
                }
            }

            // Hidden Vault Sync Pill
            Surface(
                color = PillSurfaceBg,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.dp, PillBorderColor, RoundedCornerShape(12.dp))
                    .clickable(onClick = onOpenVaultModal)
                    .testTag("autosave_status_pill")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Hidden Vault Status",
                        tint = if (hasUnsavedChanges) WarmAmberAccent else MutedSageAccent,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (hasUnsavedChanges) "${secondsUntilAutoSave}s" else "Vault",
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MutedTaupeText
                    )
                }
            }
        }

        // Top-Right: 4 Horizontal Oval Pills (matching the 4 green ovals at the top-right of Sketch 1 & 2)
        // + Command Block (`⌘P`) and Settings
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Oval Pill 1: Explain Floating Window
            TopRightOvalWindowPill(
                label = FloatingWindowType.EXPLANATION.pillLabel,
                badge = activeSection?.let { "§${it.orderIndex + 1}" },
                isOpen = FloatingWindowType.EXPLANATION in openFloatingWindows,
                isTopWindow = openFloatingWindows.lastOrNull() == FloatingWindowType.EXPLANATION,
                onClick = { onToggleFloatingWindow(FloatingWindowType.EXPLANATION) },
                testTag = "island_explanation_window_toggle"
            )

            // Oval Pill 2: Folders & Sections Floating Window
            TopRightOvalWindowPill(
                label = FloatingWindowType.FOLDERS.pillLabel,
                badge = "${activeFolder.sections.size}",
                isOpen = FloatingWindowType.FOLDERS in openFloatingWindows,
                isTopWindow = openFloatingWindows.lastOrNull() == FloatingWindowType.FOLDERS,
                onClick = { onToggleFloatingWindow(FloatingWindowType.FOLDERS) },
                testTag = "island_folder_dropdown_button"
            )

            // Oval Pill 3: AI Guide Floating Window
            TopRightOvalWindowPill(
                label = FloatingWindowType.AI_STUDIO.pillLabel,
                badge = null,
                isOpen = FloatingWindowType.AI_STUDIO in openFloatingWindows,
                isTopWindow = openFloatingWindows.lastOrNull() == FloatingWindowType.AI_STUDIO,
                onClick = { onToggleFloatingWindow(FloatingWindowType.AI_STUDIO) },
                testTag = "island_ai_studio_button"
            )

            // Oval Pill 4: Run Output Floating Window
            TopRightOvalWindowPill(
                label = FloatingWindowType.RUN_OUTPUT.pillLabel,
                badge = if (syntaxErrorCount > 0) "!$syntaxErrorCount" else null,
                isOpen = FloatingWindowType.RUN_OUTPUT in openFloatingWindows,
                isTopWindow = openFloatingWindows.lastOrNull() == FloatingWindowType.RUN_OUTPUT,
                onClick = { onToggleFloatingWindow(FloatingWindowType.RUN_OUTPUT) },
                testTag = "island_run_code_button"
            )

            // Command Block Trigger (`⌘P`, exempt from the 2-window limit)
            Surface(
                color = if (activeDropdown == IslandDropdownMenu.COMMAND_PALETTE) PillSelectedBg else PillSurfaceBg,
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier
                    .clip(RoundedCornerShape(18.dp))
                    .border(
                        width = 1.dp,
                        color = if (activeDropdown == IslandDropdownMenu.COMMAND_PALETTE) WarmAmberAccent else PillBorderColor,
                        shape = RoundedCornerShape(18.dp)
                    )
                    .clickable(onClick = onToggleCommandPalette)
                    .testTag("island_command_palette_button")
            ) {
                Text(
                    text = "⌘P",
                    fontFamily = JetBrainsMonoFontFamily,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = WarmCreamText,
                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp)
                )
            }

            IconButton(
                onClick = onOpenSettings,
                modifier = Modifier
                    .size(28.dp)
                    .testTag("editor_shortcut_settings_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "IDE Settings",
                    tint = MutedTaupeText,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
private fun TopRightOvalWindowPill(
    label: String,
    badge: String?,
    isOpen: Boolean,
    isTopWindow: Boolean,
    onClick: () -> Unit,
    testTag: String
) {
    Surface(
        color = when {
            isTopWindow -> PillSelectedBg
            isOpen -> Color(0xFF332920)
            else -> PillSurfaceBg
        },
        shape = RoundedCornerShape(20.dp),
        shadowElevation = if (isOpen) 6.dp else 2.dp,
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .border(
                width = if (isOpen) 1.5.dp else 1.dp,
                color = when {
                    isTopWindow -> WarmAmberAccent
                    isOpen -> MutedSageAccent
                    else -> PillBorderColor
                },
                shape = RoundedCornerShape(20.dp)
            )
            .clickable(onClick = onClick)
            .testTag(testTag)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 11.dp, vertical = 5.dp),
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            if (isOpen) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(if (isTopWindow) WarmAmberAccent else MutedSageAccent)
                )
            }
            Text(
                text = label,
                fontFamily = JetBrainsMonoFontFamily,
                fontSize = 11.sp,
                fontWeight = if (isOpen) FontWeight.Bold else FontWeight.Medium,
                color = WarmCreamText,
                maxLines = 1
            )
            if (!badge.isNullOrBlank()) {
                Text(
                    text = badge,
                    fontFamily = JetBrainsMonoFontFamily,
                    fontSize = 9.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = WarmAmberAccent
                )
            }
        }
    }
}

/**
 * Left Vertical Rail with Bezel Breathing Room, Collapse Toggle (`«`/`»`), Undo (`↶`), and 5 Circular IDE Tool Buttons:
 * - Tapping any active tool button a second time turns it OFF (`LeftRailIdeTool.NONE`).
 * - Includes an explicit Undo (`↶`) button so any left-rail action (`{ }` Format, `!` Quick-Fix, `⌘/` Line Actions, `+` New `.ktscribe` File) can be undone immediately.
 * - Includes a Collapse/Hide toggle (`«` / `»`) so the user can collapse or restore the left rail.
 */
@Composable
fun SketchLeftCircularIdeRail(
    activeTool: LeftRailIdeTool,
    syntaxErrorCount: Int,
    canUndo: Boolean = false,
    onUndo: () -> Unit = {},
    onFormatCode: () -> Unit,
    onSelectTool: (LeftRailIdeTool) -> Unit,
    onApplyQuickFixOrDiagnostics: () -> Unit,
    onInsertNewSection: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isRailCollapsed by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            // Generous bezel-safe breathing room from the left screen edge and top/bottom corners
            .padding(start = 14.dp, end = 8.dp, top = 12.dp, bottom = 14.dp)
            .verticalScroll(rememberScrollState())
            .testTag("left_circular_ide_rail"),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top Rail Toggle: Collapse / Expand Left Rail (`«` / `»`)
        Surface(
            color = Color(0xFF1E1C1A),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .border(1.dp, PillBorderColor, RoundedCornerShape(12.dp))
                .clickable {
                    isRailCollapsed = !isRailCollapsed
                    if (isRailCollapsed && activeTool != LeftRailIdeTool.NONE) {
                        onSelectTool(LeftRailIdeTool.NONE)
                    }
                }
                .testTag("left_rail_collapse_toggle_button")
        ) {
            Text(
                text = if (isRailCollapsed) "»" else "«",
                fontFamily = JetBrainsMonoFontFamily,
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MutedTaupeText,
                modifier = Modifier.padding(horizontal = 9.dp, vertical = 3.dp)
            )
        }

        if (!isRailCollapsed) {
            // Circle 1: Format Kotlin Code (`{ }`) — Undoable via `↶` below
            LeftRailCircleButton(
                textSymbol = "{ }",
                icon = null,
                tooltip = "Format Code (Undoable)",
                isSelected = false,
                accentColor = WarmAmberAccent,
                onClick = onFormatCode,
                testTag = "left_rail_format_button"
            )

            // Circle 2: Quick-Fix / Syntax Diagnostics (`✓` or `!`) — Tap again to turn OFF
            LeftRailCircleButton(
                textSymbol = if (syntaxErrorCount > 0) "!$syntaxErrorCount" else "✓",
                icon = null,
                tooltip = "Diagnostics & Quick-Fix (Tap again to turn off)",
                isSelected = activeTool == LeftRailIdeTool.DIAGNOSTICS,
                accentColor = if (syntaxErrorCount > 0) MacRedDot else MutedSageAccent,
                onClick = onApplyQuickFixOrDiagnostics,
                testTag = "left_rail_diagnostics_button"
            )

            // Circle 3: Structure Outline & Find (`🔍`) — Tap again to turn OFF
            LeftRailCircleButton(
                textSymbol = null,
                icon = Icons.Default.Search,
                tooltip = "Structure & Find (Tap again to turn off)",
                isSelected = activeTool == LeftRailIdeTool.STRUCTURE_AND_FIND,
                accentColor = MutedSteelAccent,
                onClick = {
                    onSelectTool(
                        if (activeTool == LeftRailIdeTool.STRUCTURE_AND_FIND) LeftRailIdeTool.NONE
                        else LeftRailIdeTool.STRUCTURE_AND_FIND
                    )
                },
                testTag = "left_rail_structure_find_button"
            )

            // Circle 4: Line Comment, Duplicate & Surround (`⌘/`) — Tap again to turn OFF
            LeftRailCircleButton(
                textSymbol = "⌘/",
                icon = null,
                tooltip = "Line & Surround Actions (Tap again to turn off)",
                isSelected = activeTool == LeftRailIdeTool.LINE_ACTIONS,
                accentColor = WarmAmberAccent,
                onClick = {
                    onSelectTool(
                        if (activeTool == LeftRailIdeTool.LINE_ACTIONS) LeftRailIdeTool.NONE
                        else LeftRailIdeTool.LINE_ACTIONS
                    )
                },
                testTag = "left_rail_line_actions_button"
            )

            // Circle 5: Add New `.ktscribe` File in Folder (`+`)
            LeftRailCircleButton(
                textSymbol = null,
                icon = Icons.Default.Add,
                tooltip = "New .ktscribe File in Folder",
                isSelected = false,
                accentColor = MutedSageAccent,
                onClick = onInsertNewSection,
                testTag = "toolbar_insert_code_block_button"
            )

            // Circle 6: Dedicated Undo (`↶`) Button for undoing any Left-Rail action or code edit
            LeftRailCircleButton(
                textSymbol = "↶",
                icon = null,
                tooltip = "Undo Last Action",
                isSelected = canUndo,
                accentColor = if (canUndo) WarmAmberAccent else MutedTaupeText.copy(alpha = 0.45f),
                onClick = onUndo,
                testTag = "left_rail_undo_button"
            )

            // Explicit Turn-Off Button whenever a Left-Rail Tool Bar is active
            if (activeTool != LeftRailIdeTool.NONE) {
                LeftRailCircleButton(
                    textSymbol = "Off",
                    icon = null,
                    tooltip = "Turn Off Active Left Rail Tool",
                    isSelected = true,
                    accentColor = MacRedDot,
                    onClick = { onSelectTool(LeftRailIdeTool.NONE) },
                    testTag = "left_rail_turn_off_tool_button"
                )
            }
        }
    }
}

@Composable
private fun LeftRailCircleButton(
    textSymbol: String?,
    icon: ImageVector?,
    tooltip: String,
    isSelected: Boolean,
    accentColor: Color,
    onClick: () -> Unit,
    testTag: String
) {
    Surface(
        color = if (isSelected) PillSelectedBg else Color(0xFF23201E),
        shape = CircleShape,
        shadowElevation = if (isSelected) 8.dp else 4.dp,
        modifier = Modifier
            .size(38.dp)
            .border(
                width = 1.dp,
                color = Color(0xFF0A0908),
                shape = CircleShape
            )
            .padding(1.dp)
            .border(
                width = if (isSelected) 1.5.dp else 1.dp,
                color = if (isSelected) accentColor else Color(0xFF47403A),
                shape = CircleShape
            )
            .clip(CircleShape)
            .clickable(onClick = onClick)
            .testTag(testTag)
    ) {
        Box(contentAlignment = Alignment.Center) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = tooltip,
                    tint = if (isSelected) accentColor else WarmCreamText,
                    modifier = Modifier.size(16.dp)
                )
            } else if (textSymbol != null) {
                Text(
                    text = textSymbol,
                    fontFamily = JetBrainsMonoFontFamily,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (isSelected) accentColor else WarmCreamText
                )
            }
        }
    }
}

/**
 * TIER 3 TACTILE 3D FLOATING MACOS WINDOW:
 * - Distinct Warm Mocha Slate color (`#2C2825` foreground / `#23201E` secondary) — not bright or neon, clearly distinct from the `#1B1A19` code canvas.
 * - Multi-layered 3D depth: custom ambient shadow halo + `28.dp` active / `14.dp` secondary shadow + `1.dp` pitch-black outer separation rim + `1.5.dp` top-lit specular bevel (`#63594F`).
 * - Can be dragged freely to any part of the screen.
 * - Interactive Left-Edge Resize Grip (`▐`, matching Sketch 2) + Bottom-Right Corner Resize Grip (`⤡`).
 * - Clear Cancel / Close button (`✕` + red traffic light dot).
 * - Smooth macOS spring scale + fade transition (`scaleIn` / `scaleOut` + `fadeIn` / `fadeOut`).
 * - Performs a horizontal shake animation when `shakeTriggerCount` increments (when 2 windows are open and the user taps a 3rd pill).
 */
@Composable
fun MacDraggableFloatingWindow(
    windowType: FloatingWindowType,
    isOpen: Boolean,
    isForegroundWindow: Boolean,
    windowSlotIndex: Int, // 0 or 1 so 2 open windows cascade with distinct initial offsets
    shakeTriggerCount: Int,
    workspaceWidthPx: Float = 2400f,
    workspaceHeightPx: Float = 2400f,
    initialWidthDp: Float = windowType.defaultWidthDp,
    initialHeightDp: Float = windowType.defaultHeightDp,
    onFocusWindow: () -> Unit,
    onCancelWindow: () -> Unit,
    onDockToSplitScreen: (() -> Unit)? = null,
    headerExtraActions: (@Composable () -> Unit)? = null,
    content: @Composable () -> Unit
) {
    val density = LocalDensity.current
    var offsetX by remember(windowType) {
        mutableFloatStateOf(if (windowSlotIndex == 0) 0f else -56f)
    }
    var offsetY by remember(windowType) {
        mutableFloatStateOf(if (windowSlotIndex == 0) 6f else 48f)
    }
    // 4 Discrete Window Presets (Mini, Half, Two-Thirds, Full - MIUI / iOS / Harmony style)
    var currentTier by remember(windowType) { mutableStateOf(WindowPresetTier.HALF) }
    var isDraggingResize by remember { mutableStateOf(false) }
    var dragWidthDp by remember(windowType) { mutableFloatStateOf(windowType.getTierWidthDp(WindowPresetTier.HALF)) }
    var dragHeightDp by remember(windowType) { mutableFloatStateOf(windowType.getTierHeightDp(WindowPresetTier.HALF)) }

    val targetWidth = if (isDraggingResize) dragWidthDp else windowType.getTierWidthDp(currentTier)
    val targetHeight = if (isDraggingResize) dragHeightDp else windowType.getTierHeightDp(currentTier)

    val animatedWidthDp by animateFloatAsState(
        targetValue = targetWidth,
        animationSpec = spring(dampingRatio = 0.82f, stiffness = 380f),
        label = "win_w"
    )
    val animatedHeightDp by animateFloatAsState(
        targetValue = targetHeight,
        animationSpec = spring(dampingRatio = 0.82f, stiffness = 380f),
        label = "win_h"
    )

    val shakeOffsetX = remember { Animatable(0f) }
    LaunchedEffect(shakeTriggerCount) {
        if (shakeTriggerCount > 0 && isOpen) {
            val keyframes = listOf(-18f, 18f, -14f, 14f, -8f, 8f, 0f)
            for (target in keyframes) {
                shakeOffsetX.animateTo(target, animationSpec = tween(durationMillis = 38))
            }
        }
    }

    val surfaceBg = if (isForegroundWindow) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant
    val headerBg = if (isForegroundWindow) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.75f)
    val topBevelColor = if (isForegroundWindow) MaterialTheme.colorScheme.primary.copy(alpha = 0.45f) else MaterialTheme.colorScheme.outlineVariant
    val elevationDp = if (isForegroundWindow) 24.dp else 12.dp

    val maxDragLeftPx = -(workspaceWidthPx.coerceAtLeast(1200f) * 0.95f)
    val maxDragRightPx = (workspaceWidthPx.coerceAtLeast(1200f) * 0.95f)
    val maxDragUpPx = -(workspaceHeightPx.coerceAtLeast(1200f) * 0.85f)
    val maxDragDownPx = (workspaceHeightPx.coerceAtLeast(1200f) * 0.85f)

    AnimatedVisibility(
        visible = isOpen,
        enter = scaleIn(
            initialScale = 0.88f,
            transformOrigin = TransformOrigin(0.92f, 0.04f),
            animationSpec = spring(
                dampingRatio = 0.82f,
                stiffness = 420f
            )
        ) + fadeIn(animationSpec = tween(durationMillis = 160)),
        exit = scaleOut(
            targetScale = 0.88f,
            transformOrigin = TransformOrigin(0.92f, 0.04f),
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioNoBouncy,
                stiffness = Spring.StiffnessMedium
            )
        ) + fadeOut(animationSpec = tween(durationMillis = 135)),
        modifier = Modifier
            .zIndex(if (isForegroundWindow) 20f else 10f)
            .offset {
                IntOffset(
                    x = (offsetX + shakeOffsetX.value).roundToInt(),
                    y = offsetY.roundToInt()
                )
            }
    ) {
        Surface(
            color = surfaceBg,
            shape = RoundedCornerShape(18.dp),
            shadowElevation = elevationDp,
            modifier = Modifier
                .padding(8.dp)
                .width(animatedWidthDp.dp)
                .height(animatedHeightDp.dp)
                .tactileWindowDepth(isForeground = isForegroundWindow, cornerRadiusDp = 18f)
                .shadow(
                    elevation = elevationDp,
                    shape = RoundedCornerShape(18.dp),
                    ambientColor = Color.Black,
                    spotColor = Color.Black
                )
                // Outer separation border adapted to theme
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant,
                    shape = RoundedCornerShape(18.dp)
                )
                .padding(1.dp)
                // Specular bevel border
                .border(
                    width = if (isForegroundWindow) 1.5.dp else 1.dp,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            topBevelColor,
                            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                        )
                    ),
                    shape = RoundedCornerShape(17.dp)
                )
                .pointerInput(windowType) {
                    detectTapGestures(onPress = {
                        onFocusWindow()
                    })
                }
                .testTag(
                    if (windowType == FloatingWindowType.EXPLANATION) {
                        "mac_explanation_floating_window"
                    } else {
                        "floating_window_${windowType.name.lowercase()}"
                    }
                )
        ) {
            Row(modifier = Modifier.fillMaxSize()) {
                // LEFT-EDGE INTERACTIVE RESIZE HANDLE
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(12.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                        .pointerInput(windowType) {
                            detectDragGestures(
                                onDragStart = {
                                    onFocusWindow()
                                    isDraggingResize = true
                                    dragWidthDp = animatedWidthDp
                                    dragHeightDp = animatedHeightDp
                                },
                                onDragEnd = {
                                    isDraggingResize = false
                                    currentTier = windowType.getClosestTier(dragWidthDp, dragHeightDp)
                                },
                                onDragCancel = {
                                    isDraggingResize = false
                                    currentTier = windowType.getClosestTier(dragWidthDp, dragHeightDp)
                                }
                            ) { change, dragAmount ->
                                change.consume()
                                val dxDp = with(density) { dragAmount.x.toDp().value }
                                dragWidthDp = (dragWidthDp - dxDp).coerceIn(
                                    windowType.getTierWidthDp(WindowPresetTier.MINI),
                                    windowType.getTierWidthDp(WindowPresetTier.FULL)
                                )
                            }
                        }
                        .testTag("left_edge_resize_handle_${windowType.name.lowercase()}"),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .width(3.5.dp)
                            .height(42.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (isForegroundWindow) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant)
                    )
                }

                // MAIN FLOATING WINDOW COLUMN
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                ) {
                    // 1. Draggable macOS Window Header Bar (`• • •` + Title + Preset + `◫ Split` + Cancel `✕`)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(headerBg)
                            .pointerInput(windowType, maxDragLeftPx, maxDragRightPx, maxDragUpPx, maxDragDownPx) {
                                detectDragGestures(
                                    onDragStart = { onFocusWindow() }
                                ) { change, dragAmount ->
                                    change.consume()
                                    offsetX = (offsetX + dragAmount.x).coerceIn(maxDragLeftPx, maxDragRightPx)
                                    offsetY = (offsetY + dragAmount.y).coerceIn(maxDragUpPx, maxDragDownPx)
                                }
                            }
                            .padding(horizontal = 10.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            // Traffic Light Dots
                            Box(
                                modifier = Modifier
                                    .size(11.dp)
                                    .clip(CircleShape)
                                    .background(MacRedDot)
                                    .clickable(onClick = onCancelWindow)
                            )
                            // Yellow: Half size preset
                            Box(
                                modifier = Modifier
                                    .size(11.dp)
                                    .clip(CircleShape)
                                    .background(MacYellowDot)
                                    .clickable {
                                        isDraggingResize = false
                                        currentTier = WindowPresetTier.HALF
                                    }
                            )
                            // Green: Full size toggle
                            Box(
                                modifier = Modifier
                                    .size(11.dp)
                                    .clip(CircleShape)
                                    .background(MacGreenDot)
                                    .clickable {
                                        isDraggingResize = false
                                        currentTier = if (currentTier == WindowPresetTier.FULL) WindowPresetTier.HALF else WindowPresetTier.FULL
                                    }
                            )

                            Spacer(modifier = Modifier.width(4.dp))
                            Column {
                                Text(
                                    text = windowType.windowTitle,
                                    fontFamily = JetBrainsMonoFontFamily,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "Tier: ${currentTier.label} (${animatedWidthDp.roundToInt()}×${animatedHeightDp.roundToInt()} dp)",
                                    fontFamily = JetBrainsMonoFontFamily,
                                    fontSize = 9.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1
                                )
                            }
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            if (headerExtraActions != null) {
                                headerExtraActions()
                            }

                            // 4 Discrete Tier Snapping Pills (Mini, Half, 2/3, Full)
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                WindowPresetTier.entries.forEach { tier ->
                                    val isSelected = currentTier == tier
                                    Surface(
                                        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                                        shape = RoundedCornerShape(6.dp),
                                        border = androidx.compose.foundation.BorderStroke(
                                            width = if (isSelected) 1.dp else 0.5.dp,
                                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                                        ),
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .clickable {
                                                isDraggingResize = false
                                                currentTier = tier
                                            }
                                            .testTag("tier_${tier.name.lowercase()}_${windowType.name.lowercase()}")
                                    ) {
                                        Text(
                                            text = "${tier.badge} ${tier.label}",
                                            fontFamily = JetBrainsMonoFontFamily,
                                            fontSize = 8.5.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.padding(horizontal = 4.5.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }

                            // Option to convert ANY floating window into an Adjustable Split Screen (`◫ Split`)!
                            if (onDockToSplitScreen != null) {
                                Surface(
                                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f),
                                    shape = RoundedCornerShape(7.dp),
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(7.dp))
                                        .border(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.55f), RoundedCornerShape(7.dp))
                                        .clickable(onClick = onDockToSplitScreen)
                                        .testTag(
                                            if (windowType == FloatingWindowType.EXPLANATION) {
                                                "toggle_split_screen_explanation_button"
                                            } else {
                                                "dock_split_window_${windowType.name.lowercase()}"
                                            }
                                        )
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.VerticalSplit,
                                            contentDescription = "Convert Floating Window to Adjustable Split Screen",
                                            tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                            modifier = Modifier.size(11.dp)
                                        )
                                        Text(
                                            text = "Split",
                                            fontFamily = JetBrainsMonoFontFamily,
                                            fontSize = 9.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSecondaryContainer
                                        )
                                    }
                                }
                            }

                            // Explicit Cancel / Close Window Button (`✕`)
                            Surface(
                                color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f),
                                shape = RoundedCornerShape(7.dp),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(7.dp))
                                    .border(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f), RoundedCornerShape(7.dp))
                                    .clickable(onClick = onCancelWindow)
                                    .testTag("cancel_floating_window_${windowType.name.lowercase()}")
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Cancel Floating Window",
                                        tint = MaterialTheme.colorScheme.onErrorContainer,
                                        modifier = Modifier.size(11.dp)
                                    )
                                    Text(
                                        text = "Cancel",
                                        fontFamily = JetBrainsMonoFontFamily,
                                        fontSize = 9.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                }
                            }
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 1.dp)

                    // 2. Window Body Content with Proportional Scaling
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .padding(8.dp)
                    ) {
                        content()
                    }

                    // 3. Bottom Resize Bar with Preset Quick-Snaps & Bottom-Right Corner Drag (`⤡`)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.85f))
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "${animatedWidthDp.roundToInt()}×${animatedHeightDp.roundToInt()} dp",
                                fontFamily = JetBrainsMonoFontFamily,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            // Quick discrete tier preset selector
                            WindowPresetTier.entries.forEach { tier ->
                                val isSelected = currentTier == tier
                                Surface(
                                    color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                                    shape = RoundedCornerShape(4.dp),
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .clickable {
                                            isDraggingResize = false
                                            currentTier = tier
                                        }
                                ) {
                                    Text(
                                        text = tier.label,
                                        fontFamily = JetBrainsMonoFontFamily,
                                        fontSize = 8.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }

                        // Bottom-Right Corner Resize Drag Handle (`⤡`)
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                                .pointerInput(windowType) {
                                    detectDragGestures(
                                        onDragStart = {
                                            onFocusWindow()
                                            isDraggingResize = true
                                            dragWidthDp = animatedWidthDp
                                            dragHeightDp = animatedHeightDp
                                        },
                                        onDragEnd = {
                                            isDraggingResize = false
                                            currentTier = windowType.getClosestTier(dragWidthDp, dragHeightDp)
                                        },
                                        onDragCancel = {
                                            isDraggingResize = false
                                            currentTier = windowType.getClosestTier(dragWidthDp, dragHeightDp)
                                        }
                                    ) { change, dragAmount ->
                                        change.consume()
                                        val dxDp = with(density) { dragAmount.x.toDp().value }
                                        val dyDp = with(density) { dragAmount.y.toDp().value }
                                        dragWidthDp = (dragWidthDp + dxDp).coerceIn(
                                            windowType.getTierWidthDp(WindowPresetTier.MINI),
                                            windowType.getTierWidthDp(WindowPresetTier.FULL)
                                        )
                                        dragHeightDp = (dragHeightDp + dyDp).coerceIn(
                                            windowType.getTierHeightDp(WindowPresetTier.MINI),
                                            windowType.getTierHeightDp(WindowPresetTier.FULL)
                                        )
                                    }
                                }
                                .testTag("corner_resize_handle_${windowType.name.lowercase()}"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "⤡",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * BOTTOM-DOCKED IDE COMMAND BLOCK (`⌘P` Command & Terminal Drawer):
 * Spawns at the BOTTOM of the IDE workspace (just like IntelliJ IDEA / VS Code bottom panels)
 * with a top resize drag handle (`═`) and smooth vertical slide transition.
 */
@Composable
fun ResizableIslandDropdownWindow(
    title: String,
    subtitle: String,
    initialWidthDp: Float = 720f,
    initialHeightDp: Float = 290f,
    onClose: () -> Unit,
    content: @Composable () -> Unit
) {
    val density = LocalDensity.current
    var heightDp by remember { mutableFloatStateOf(initialHeightDp) }

    Surface(
        color = Tier3FloatingWindowActiveBg,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp, bottomStart = 12.dp, bottomEnd = 12.dp),
        shadowElevation = 28.dp,
        modifier = Modifier
            .padding(horizontal = 14.dp, vertical = 4.dp)
            .fillMaxWidth()
            .heightIn(min = 190.dp, max = heightDp.dp)
            .tactileWindowDepth(isForeground = true, cornerRadiusDp = 20f)
            .border(
                width = 1.5.dp,
                color = Color(0xFF080707),
                shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp, bottomStart = 12.dp, bottomEnd = 12.dp)
            )
            .padding(1.dp)
            .border(
                width = 1.dp,
                color = Tier3FloatingWindowActiveBevel,
                shape = RoundedCornerShape(topStart = 19.dp, topEnd = 19.dp, bottomStart = 11.dp, bottomEnd = 11.dp)
            )
            .testTag("resizable_island_dropdown_window")
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Top Resize Drag Handle (`═`) for the Bottom-Docked Command Block
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF1F1C1A))
                    .pointerInput(Unit) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            val dyDp = with(density) { dragAmount.y.toDp().value }
                            heightDp = (heightDp - dyDp).coerceIn(190f, 520f)
                        }
                    }
                    .padding(vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .width(44.dp)
                        .height(3.5.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(WarmAmberAccent.copy(alpha = 0.7f))
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Tier3FloatingWindowActiveHeader)
                    .padding(horizontal = 12.dp, vertical = 6.dp),
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
                            .clickable { heightDp = 210f }
                    )
                    Box(
                        modifier = Modifier
                            .size(11.dp)
                            .clip(CircleShape)
                            .background(MacGreenDot)
                            .clickable { heightDp = 440f }
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        Text(
                            text = title,
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = WarmCreamText
                        )
                        Text(
                            text = "$subtitle • Bottom IDE Dock (Drag top bar ═ to resize)",
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 9.5.sp,
                            color = MutedTaupeText
                        )
                    }
                }

                IconButton(onClick = onClose, modifier = Modifier.size(24.dp)) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close Bottom Command Block",
                        tint = MutedTaupeText,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            Box(
                modifier = Modifier
                    .weight(1f, fill = false)
                    .fillMaxWidth()
                    .padding(10.dp)
            ) {
                content()
            }
        }
    }
}

/**
 * Floating Command Palette Content (`⌘P`).
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
                    title = "Open Explanation Floating Window",
                    subtitle = "Draggable & resizable Warm Mocha floating window",
                    icon = Icons.Default.Description,
                    action = { onExecuteCommand("float_explain") }
                )
            )
            add(
                PaletteEntry(
                    id = "cmd_run",
                    category = CommandPaletteFilter.COMMANDS,
                    title = "Run Build & Execute in Apple cmd",
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

            allFolders.forEach { folder ->
                add(
                    PaletteEntry(
                        id = "folder_${folder.id}",
                        category = CommandPaletteFilter.FOLDERS,
                        title = "Folder: ${folder.title}",
                        subtitle = "${folder.sectionCount} Kotlin sections • ${folder.wordCount} words",
                        icon = Icons.Default.Folder,
                        action = { onSelectFolder(folder.id) }
                    )
                )
            }

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
                    color = MutedTaupeText
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    tint = MutedTaupeText,
                    modifier = Modifier.size(18.dp)
                )
            },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color(0xFF191716),
                unfocusedContainerColor = Color(0xFF191716),
                focusedTextColor = WarmCreamText,
                unfocusedTextColor = WarmCreamText,
                focusedBorderColor = WarmAmberAccent,
                unfocusedBorderColor = Color(0xFF3E3832)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("command_palette_search_input")
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            CommandPaletteFilter.entries.forEach { filter ->
                val selected = selectedFilter == filter
                Surface(
                    color = if (selected) Tier2CompletionSelectedBg else Color(0xFF211E1C),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .border(
                            width = 1.dp,
                            color = if (selected) WarmAmberAccent else Color(0xFF3E3832),
                            shape = RoundedCornerShape(16.dp)
                        )
                        .clickable { selectedFilter = filter }
                ) {
                    Text(
                        text = filter.label,
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 11.sp,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                        color = WarmCreamText,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                    )
                }
            }
        }

        HorizontalDivider(color = Color(0xFF3A342E))

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 250.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(filtered, key = { it.id }) { item ->
                Surface(
                    color = Color(0xFF23201E),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .border(1.dp, Color(0xFF36312C), RoundedCornerShape(10.dp))
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
                            tint = WarmAmberAccent,
                            modifier = Modifier.size(16.dp)
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = item.title,
                                fontFamily = JetBrainsMonoFontFamily,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = WarmCreamText
                            )
                            Text(
                                text = item.subtitle,
                                fontFamily = JetBrainsMonoFontFamily,
                                fontSize = 10.sp,
                                color = MutedTaupeText,
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
 * Folder & Sections Explorer Content inside the `Folders` Floating Window.
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
    onInsertNewLanguageSection: (com.example.model.LanguageType) -> Unit = {},
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
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.FolderSpecial,
                contentDescription = null,
                tint = WarmAmberAccent,
                modifier = Modifier.size(20.dp)
            )
            OutlinedTextField(
                value = activeFolder.title,
                onValueChange = onFolderTitleChange,
                label = { Text("Active Folder Name", fontSize = 10.sp) },
                singleLine = true,
                shape = RoundedCornerShape(10.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFF191716),
                    unfocusedContainerColor = Color(0xFF191716),
                    focusedTextColor = WarmCreamText,
                    unfocusedTextColor = WarmCreamText,
                    focusedBorderColor = WarmAmberAccent
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
                Text("All", fontSize = 11.sp)
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "FILES BY LANGUAGE DIRECTORY (${activeFolder.sections.size} Files)",
                fontFamily = JetBrainsMonoFontFamily,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = MutedSageAccent
            )
        }

        // Quick create file chips for all 4 supported languages & directories
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            com.example.model.LanguageType.entries.forEach { lang ->
                Surface(
                    color = lang.badgeColor.copy(alpha = 0.16f),
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, lang.badgeColor.copy(alpha = 0.5f)),
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onInsertNewLanguageSection(lang) }
                        .testTag("add_file_chip_${lang.id}")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = lang.badgeColor, modifier = Modifier.size(12.dp))
                        Text(
                            text = "+ ${lang.displayName} (${lang.extension})",
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = lang.badgeColor
                        )
                    }
                }
            }
        }

        val sectionsByDir = activeFolder.sections.groupBy { it.language }
        sectionsByDir.forEach { (lang, files) ->
            // Directory folder header
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.FolderOpen,
                        contentDescription = null,
                        tint = lang.badgeColor,
                        modifier = Modifier.size(15.dp)
                    )
                    Text(
                        text = "${lang.directoryPath}  •  ${lang.displayName} (${files.size} ${if (files.size == 1) "file" else "files"})",
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            files.forEach { sec ->
                val isCurrent = sec.id == activeSectionId
                Surface(
                    color = if (isCurrent) Tier2CompletionSelectedBg.copy(alpha = 0.65f) else Color(0xFF211E1C),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 8.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .border(
                            width = 1.dp,
                            color = if (isCurrent) WarmAmberAccent else Color(0xFF3A342E),
                            shape = RoundedCornerShape(10.dp)
                        )
                        .clickable { onSelectSection(sec.id) }
                        .testTag("jump_concept_chip_${sec.orderIndex}")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Surface(
                                    color = sec.language.badgeColor.copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(4.dp),
                                    modifier = Modifier.border(0.5.dp, sec.language.badgeColor, RoundedCornerShape(4.dp))
                                ) {
                                    Text(
                                        text = sec.language.shortLabel,
                                        fontFamily = JetBrainsMonoFontFamily,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = sec.language.badgeColor,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                    )
                                }
                                Text(
                                    text = sec.ktScribeFileName,
                                    fontFamily = JetBrainsMonoFontFamily,
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = WarmCreamText,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Text(
                                text = "${sec.language.directoryPath}${sec.ktScribeFileName} (${sec.lineCount}L) • [${sec.effectiveLearnedConceptTag}]",
                                fontFamily = JetBrainsMonoFontFamily,
                                fontSize = 9.5.sp,
                                color = MutedSageAccent
                            )
                        }
                        if (activeFolder.sections.size > 1) {
                            IconButton(
                                onClick = { onDeleteSection(sec.id) },
                                modifier = Modifier.size(26.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DeleteOutline,
                                    contentDescription = "Delete File",
                                    tint = MutedTaupeText,
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        HorizontalDivider(color = Color(0xFF3A342E))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "WORKSPACE FOLDERS (${allFolders.size})",
                fontFamily = JetBrainsMonoFontFamily,
                fontSize = 10.5.sp,
                fontWeight = FontWeight.Bold,
                color = MutedTaupeText
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
                    placeholder = { Text("Folder name", fontSize = 11.sp) },
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
                    color = if (isCurrentFolder) PillSelectedBg else Color(0xFF211E1C),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .border(
                            width = 1.dp,
                            color = if (isCurrentFolder) WarmAmberAccent else Color(0xFF3A342E),
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
                            tint = WarmAmberAccent,
                            modifier = Modifier.size(15.dp)
                        )
                        Column {
                            Text(
                                text = folder.title,
                                fontFamily = JetBrainsMonoFontFamily,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = WarmCreamText
                            )
                            Text(
                                text = "${folder.sectionCount} sections",
                                fontFamily = JetBrainsMonoFontFamily,
                                fontSize = 9.5.sp,
                                color = MutedTaupeText
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Rich Explanation & Notes Content used inside both:
 * 1. The `Explain` Draggable Floating Window (Tier 3 Depth)
 * 2. The 2-Window Resizable Split-Screen Mode ("Set Aside")
 */
@Composable
fun ExplanationWindowBodyContent(
    section: CodeSectionUiModel,
    formattingStyle: TextFormattingStyle,
    onExplanationChange: (String) -> Unit,
    onConceptTagChange: (String) -> Unit,
    onFontChoiceChange: (EditorFontChoice) -> Unit,
    onToggleBold: () -> Unit,
    onToggleItalic: () -> Unit,
    onToggleUnderline: () -> Unit
) {
    var isEditingTag by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        // Formatting & Learned Concept Tag Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF1F1C1A))
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 8.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Surface(
                color = Color(0xFF2D2722),
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .border(1.dp, Color(0xFF4A4037), RoundedCornerShape(6.dp))
                    .clickable { isEditingTag = !isEditingTag }
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.School,
                        contentDescription = null,
                        tint = WarmAmberAccent,
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
                                color = WarmCreamText
                            ),
                            cursorBrush = SolidColor(WarmAmberAccent),
                            modifier = Modifier.width(120.dp)
                        )
                    } else {
                        Text(
                            text = "Tag: ${section.effectiveLearnedConceptTag}",
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = WarmAmberAccent
                        )
                    }
                }
            }

            EditorFontChoice.entries.forEach { font ->
                val selected = formattingStyle.fontChoice == font
                Surface(
                    color = if (selected) Tier2CompletionSelectedBg else Color(0xFF292522),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .clickable { onFontChoiceChange(font) }
                ) {
                    Text(
                        text = font.badge,
                        fontSize = 10.sp,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                        color = WarmCreamText,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            IconButton(onClick = onToggleBold, modifier = Modifier.size(22.dp)) {
                Icon(
                    imageVector = Icons.Default.FormatBold,
                    contentDescription = "Bold",
                    tint = if (formattingStyle.isBold) WarmAmberAccent else MutedTaupeText,
                    modifier = Modifier.size(14.dp)
                )
            }
            IconButton(onClick = onToggleItalic, modifier = Modifier.size(22.dp)) {
                Icon(
                    imageVector = Icons.Default.FormatItalic,
                    contentDescription = "Italic",
                    tint = if (formattingStyle.isItalic) WarmAmberAccent else MutedTaupeText,
                    modifier = Modifier.size(14.dp)
                )
            }
            IconButton(onClick = onToggleUnderline, modifier = Modifier.size(22.dp)) {
                Icon(
                    imageVector = Icons.Default.FormatUnderlined,
                    contentDescription = "Underline",
                    tint = if (formattingStyle.isUnderline) WarmAmberAccent else MutedTaupeText,
                    modifier = Modifier.size(14.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

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
                .padding(horizontal = 6.dp, vertical = 4.dp)
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
                    color = WarmCreamText,
                    lineHeight = (formattingStyle.fontSizeSp + 6).sp
                ),
                cursorBrush = SolidColor(WarmAmberAccent),
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
                                color = MutedTaupeText
                            )
                        )
                    }
                    innerTextField()
                }
            )
        }
    }
}

/**
 * Split-Screen Two-Window Explanation Pane (when user clicks "Set Aside / 2-Win").
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
    Surface(
        color = Tier3FloatingWindowActiveBg,
        shape = RoundedCornerShape(18.dp),
        shadowElevation = 18.dp,
        modifier = modifier
            .fillMaxSize()
            .tactileWindowDepth(isForeground = true, cornerRadiusDp = 18f)
            .border(1.5.dp, Color(0xFF090808), RoundedCornerShape(18.dp))
            .padding(1.dp)
            .border(1.dp, Tier3FloatingWindowActiveBevel, RoundedCornerShape(17.dp))
            .testTag("mac_explanation_floating_window")
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Tier3FloatingWindowActiveHeader)
                    .padding(horizontal = 10.dp, vertical = 7.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.weight(1f)
                ) {
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
                            .clickable(onClick = onCloseWindow)
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
                        text = "Split Window • Explanation",
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = WarmCreamText,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Surface(
                        color = Tier2CompletionSelectedBg,
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
                                tint = WarmCreamText,
                                modifier = Modifier.size(11.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "AI Explain",
                                fontFamily = JetBrainsMonoFontFamily,
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = WarmCreamText
                            )
                        }
                    }

                    Surface(
                        color = Color(0xFF29323B),
                        shape = RoundedCornerShape(7.dp),
                        modifier = Modifier
                            .clip(RoundedCornerShape(7.dp))
                            .clickable(onClick = onToggleSplitMode)
                            .testTag("toggle_split_screen_explanation_button")
                    ) {
                        Text(
                            text = "Float Window",
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = MutedSteelAccent,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                        )
                    }
                }
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(10.dp)
            ) {
                ExplanationWindowBodyContent(
                    section = section,
                    formattingStyle = formattingStyle,
                    onExplanationChange = onExplanationChange,
                    onConceptTagChange = onConceptTagChange,
                    onFontChoiceChange = onFontChoiceChange,
                    onToggleBold = onToggleBold,
                    onToggleItalic = onToggleItalic,
                    onToggleUnderline = onToggleUnderline
                )
            }
        }
    }
}

/**
 * First-Launch Storage Permission & Hidden Vault ("xyz" / ".ktscribe_vault_xyz") Recovery Dialog.
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
        containerColor = Tier3FloatingWindowActiveBg,
        titleContentColor = WarmCreamText,
        textContentColor = MutedTaupeText,
        icon = {
            Icon(
                imageVector = Icons.Default.Security,
                contentDescription = null,
                tint = WarmAmberAccent,
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
                        color = WarmCreamText
                    )
                    Surface(
                        color = Color(0xFF211E1C),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, Color(0xFF3E3832), RoundedCornerShape(10.dp))
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
                                color = MutedSageAccent
                            )
                            Text(
                                text = "When uninstalling KtScribe, Android will show a checkbox asking whether to keep or delete your app data.",
                                fontSize = 11.sp,
                                color = MutedTaupeText
                            )
                        }
                    }
                } else {
                    Surface(
                        color = Color(0xFF211E1C),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, WarmAmberAccent.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
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
                                color = WarmAmberAccent
                            )
                            Text(
                                text = "Path: ${vaultInspection.vaultPathDisplay}",
                                fontFamily = JetBrainsMonoFontFamily,
                                fontSize = 10.sp,
                                color = MutedTaupeText
                            )
                            Text(
                                text = "Recoverable Backup: ${vaultInspection.recoverableFolderCount} Folders • ${vaultInspection.recoverableSectionCount} Kotlin Sections",
                                fontFamily = JetBrainsMonoFontFamily,
                                fontSize = 10.5.sp,
                                color = WarmCreamText
                            )
                        }
                    }
                    Text(
                        text = "Would you like to recover your previous folders from the existing 'xyz' vault or create a new hidden vault folder?",
                        style = MaterialTheme.typography.bodySmall,
                        color = WarmCreamText
                    )
                }
            }
        },
        confirmButton = {
            if (!storagePermissionGrantedInUi) {
                Button(
                    onClick = { storagePermissionGrantedInUi = true },
                    colors = ButtonDefaults.buttonColors(containerColor = Tier2CompletionSelectedBg),
                    modifier = Modifier.testTag("grant_vault_storage_permission_button")
                ) {
                    Text("Allow Vault Storage & Scan 'xyz'", color = WarmCreamText, fontWeight = FontWeight.Bold)
                }
            } else {
                Button(
                    onClick = onRecoverPreviousData,
                    colors = ButtonDefaults.buttonColors(containerColor = Tier2CompletionSelectedBg),
                    modifier = Modifier.testTag("recover_xyz_vault_button")
                ) {
                    Icon(Icons.Default.Restore, contentDescription = null, tint = WarmCreamText, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Recover Previous Data", color = WarmCreamText, fontWeight = FontWeight.ExtraBold)
                }
            }
        },
        dismissButton = {
            if (!storagePermissionGrantedInUi) {
                TextButton(onClick = onDismiss) {
                    Text("Later", color = MutedTaupeText)
                }
            } else {
                OutlinedButton(
                    onClick = onCreateNewVaultFolder,
                    modifier = Modifier.testTag("create_new_vault_folder_button")
                ) {
                    Text("Create New Hidden Vault", color = WarmCreamText)
                }
            }
        }
    )
}
