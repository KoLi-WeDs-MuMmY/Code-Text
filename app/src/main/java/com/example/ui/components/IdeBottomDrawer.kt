package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SubdirectoryArrowRight
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import android.widget.Toast
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.code.CodeLanguageEngine
import com.example.code.DiagnosticSeverity
import com.example.code.DiffEngine
import com.example.code.DiffLineKind
import com.example.code.KotlinDiagnostic
import com.example.compiler.CompilationArtifact
import com.example.compiler.KotlinCompilerPlugin
import com.example.compiler.ReplHistoryEntry
import com.example.compiler.VmExecutionState
import com.example.model.BottomDrawerTab
import com.example.model.CodeSectionUiModel
import com.example.model.LocalCodeSnapshot
import com.example.model.LogFilterLevel
import com.example.model.ToolchainPluginItem
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.LocalAdaptiveThemeSpec
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * CodeAssist + iOS Glass Bottom-Anchored Build, Terminal, Problems, Debugger & Logcat Panel.
 * Always slides up from the bottom of the workspace container.
 */
@Composable
fun IdeBottomDrawer(
    isOpen: Boolean,
    activeTab: BottomDrawerTab,
    activeSection: CodeSectionUiModel?,
    executionOutput: String?,
    heightFraction: Float,
    toolchainPlugins: List<ToolchainPluginItem> = emptyList(),
    activeBreakpoints: Set<Int> = emptySet(),
    watchExpressions: List<String> = emptyList(),
    stdinInput: String = "",
    compilationArtifact: CompilationArtifact? = null,
    vmDebugState: VmExecutionState? = null,
    vmTrace: List<VmExecutionState> = emptyList(),
    vmTraceIndex: Int = -1,
    replHistory: List<ReplHistoryEntry> = emptyList(),
    localSnapshots: List<LocalCodeSnapshot> = emptyList(),
    onUpdateStdinInput: (String) -> Unit = {},
    onTabSelected: (BottomDrawerTab) -> Unit,
    onHeightFractionChange: (Float) -> Unit,
    onRunCode: () -> Unit,
    onClearTerminal: () -> Unit,
    onCloseDrawer: () -> Unit,
    onInstallPlugin: (String) -> Unit = {},
    onOpenPluginManager: () -> Unit = {},
    onJumpToLine: (Int) -> Unit = {},
    onToggleBreakpoint: (Int) -> Unit = {},
    onAddWatchExpression: (String) -> Unit = {},
    onRemoveWatchExpression: (String) -> Unit = {},
    onApplyQuickFixCode: (String) -> Unit = {},
    onCompileAstBytecode: () -> Unit = {},
    onDebugResumeVm: () -> Unit = {},
    onDebugStepOverVm: () -> Unit = {},
    onDebugResetVm: () -> Unit = {},
    onEvaluateRepl: (String) -> Unit = {},
    onClearRepl: () -> Unit = {},
    onCreateSnapshot: () -> Unit = {},
    onRestoreSnapshot: (Long) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val adaptive = LocalAdaptiveThemeSpec.current
    var logFilterLevel by remember { mutableStateOf(LogFilterLevel.ALL) }
    var filterQuery by remember { mutableStateOf("") }
    var debugStepLine by remember(activeSection?.id, activeBreakpoints) {
        mutableIntStateOf(activeBreakpoints.minOrNull() ?: 1)
    }

    val activeLanguage = activeSection?.language ?: com.example.model.LanguageType.KOTLIN
    val requiredSdkId = ToolchainPluginItem.sdkIdForLanguage(activeLanguage)
    val matchingSdkPlugin = toolchainPlugins.find { it.id == requiredSdkId }
    val isSdkInstalled = matchingSdkPlugin?.isInstalled ?: true

    val diagnostics = remember(activeSection?.codeContent, activeSection?.language) {
        if (activeSection == null) emptyList()
        else CodeLanguageEngine.getDiagnosticsForLanguage(activeSection.codeContent, activeSection.language)
    }

    val debugInfo = remember(activeSection?.codeContent, activeSection?.language, isSdkInstalled, stdinInput) {
        if (activeSection == null) null
        else CodeLanguageEngine.executeAndDebug(
            code = activeSection.codeContent,
            language = activeSection.language,
            isSdkInstalled = isSdkInstalled,
            stdinInput = stdinInput
        )
    }

    val effectiveArtifact = remember(compilationArtifact, activeSection?.codeContent, activeSection?.language) {
        if (compilationArtifact != null && compilationArtifact.language == activeLanguage) {
            compilationArtifact
        } else if (activeSection != null) {
            KotlinCompilerPlugin.compile(activeSection.codeContent, activeSection.language)
        } else {
            null
        }
    }

    val problemCount = diagnostics.count { it.severity == DiagnosticSeverity.ERROR }
    val warningCount = diagnostics.count { it.severity == DiagnosticSeverity.WARNING }

    AnimatedVisibility(
        visible = isOpen,
        enter = slideInVertically(
            initialOffsetY = { it },
            animationSpec = spring(dampingRatio = 0.85f, stiffness = 420f)
        ) + fadeIn(),
        exit = slideOutVertically(
            targetOffsetY = { it },
            animationSpec = spring(dampingRatio = 0.85f, stiffness = 420f)
        ) + fadeOut(),
        modifier = modifier
            .fillMaxWidth()
            .fillMaxHeight(heightFraction.coerceIn(0.25f, 0.85f))
    ) {
        Surface(
            color = MaterialTheme.colorScheme.surfaceContainerLow,
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
            shadowElevation = adaptive.overlayElevation,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxSize()
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant,
                    shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
                )
                .testTag("ide_bottom_drawer")
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // 1. Top Drag Grabber Handle
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(14.dp)
                        .background(MaterialTheme.colorScheme.surfaceContainer)
                        .draggable(
                            orientation = Orientation.Vertical,
                            state = rememberDraggableState { deltaY ->
                                val deltaFraction = -deltaY / 1100f
                                onHeightFractionChange((heightFraction + deltaFraction).coerceIn(0.25f, 0.85f))
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .width(42.dp)
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(MaterialTheme.colorScheme.outline)
                    )
                }

                // 2. Header Bar: Tabs Strip ... [Status] [Run] [Clear] [Resize] [Close]
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceContainer)
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .horizontalScroll(rememberScrollState()),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        BottomDrawerTabChip(
                            label = "Build & Run",
                            badgeText = ">_",
                            isSelected = activeTab == BottomDrawerTab.TERMINAL,
                            badgeColor = Color(0xFFD4883A),
                            onClick = { onTabSelected(BottomDrawerTab.TERMINAL) },
                            testTag = "drawer_tab_terminal"
                        )

                        BottomDrawerTabChip(
                            label = "REPL",
                            badgeText = "${replHistory.size}",
                            isSelected = activeTab == BottomDrawerTab.REPL,
                            badgeColor = Color(0xFFC084FC),
                            onClick = { onTabSelected(BottomDrawerTab.REPL) },
                            testTag = "drawer_tab_repl"
                        )

                        BottomDrawerTabChip(
                            label = "Debugger",
                            badgeText = if (vmDebugState != null) "PC:${vmDebugState.currentPc}" else "${debugInfo?.variables?.size ?: 0}",
                            isSelected = activeTab == BottomDrawerTab.DEBUGGER,
                            badgeColor = Color(0xFF10B981),
                            onClick = { onTabSelected(BottomDrawerTab.DEBUGGER) },
                            testTag = "drawer_tab_debugger"
                        )

                        BottomDrawerTabChip(
                            label = "Bytecode",
                            badgeText = "${effectiveArtifact?.bytecode?.size ?: 0}",
                            isSelected = activeTab == BottomDrawerTab.AST_BYTECODE,
                            badgeColor = Color(0xFF38BDF8),
                            onClick = { onTabSelected(BottomDrawerTab.AST_BYTECODE) },
                            testTag = "drawer_tab_bytecode"
                        )

                        BottomDrawerTabChip(
                            label = "Git Diff",
                            badgeText = "${localSnapshots.size}",
                            isSelected = activeTab == BottomDrawerTab.GIT_DIFF,
                            badgeColor = Color(0xFFF59E0B),
                            onClick = { onTabSelected(BottomDrawerTab.GIT_DIFF) },
                            testTag = "drawer_tab_git_diff"
                        )

                        BottomDrawerTabChip(
                            label = "Problems",
                            badgeText = if (problemCount + warningCount > 0) "${problemCount + warningCount}" else "0",
                            isSelected = activeTab == BottomDrawerTab.PROBLEMS,
                            badgeColor = if (problemCount > 0) Color(0xFFFF5F56) else Color(0xFF8FB975),
                            onClick = { onTabSelected(BottomDrawerTab.PROBLEMS) },
                            testTag = "drawer_tab_problems"
                        )

                        BottomDrawerTabChip(
                            label = "Logcat",
                            badgeText = "${debugInfo?.logcatEntries?.size ?: 0}",
                            isSelected = activeTab == BottomDrawerTab.LOGCAT,
                            badgeColor = Color(0xFF6897BB),
                            onClick = { onTabSelected(BottomDrawerTab.LOGCAT) },
                            testTag = "drawer_tab_logcat"
                        )
                    }

                    // Right Controls: Status Pill, Run ▶, Clear, Resize, Minimize/Close
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        val statusText = when {
                            !isSdkInstalled -> "Off"
                            problemCount > 0 -> "Err ($problemCount)"
                            vmDebugState?.isPaused == true -> "Paused"
                            executionOutput != null -> "OK (0)"
                            else -> if (adaptive.useCompactTopActions) "KtVM" else "KtVM Ready"
                        }
                        val statusColor = when {
                            !isSdkInstalled -> Color(0xFFFFB86C)
                            problemCount > 0 -> Color(0xFFFF5F56)
                            vmDebugState?.isPaused == true -> Color(0xFFFFBD2E)
                            executionOutput != null -> Color(0xFF6A9955)
                            else -> Color(0xFFA39C93)
                        }

                        Surface(
                            color = MaterialTheme.colorScheme.surfaceContainerHigh,
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(0.5.dp, statusColor.copy(alpha = 0.45f))
                        ) {
                            Text(
                                text = statusText,
                                fontFamily = JetBrainsMonoFontFamily,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = statusColor,
                                maxLines = 1,
                                softWrap = false,
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                            )
                        }

                        // Run ▶ Button
                        Surface(
                            color = Color(0xFF27C93F).copy(alpha = 0.18f),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable(onClick = onRunCode)
                                .testTag("drawer_run_button")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = "Run Active Code Block",
                                    tint = Color(0xFF27C93F),
                                    modifier = Modifier.size(14.dp)
                                )
                                if (!adaptive.useCompactTopActions) {
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = "Run",
                                        fontFamily = JetBrainsMonoFontFamily,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF27C93F),
                                        maxLines = 1,
                                        softWrap = false
                                    )
                                }
                            }
                        }

                        // Clear Output
                        IconButton(
                            onClick = onClearTerminal,
                            modifier = Modifier
                                .size(26.dp)
                                .testTag("drawer_clear_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear Output",
                                tint = Color(0xFFA39C93),
                                modifier = Modifier.size(14.dp)
                            )
                        }

                        // Cycle Height (35% -> 55% -> 78%)
                        IconButton(
                            onClick = {
                                val next = when {
                                    heightFraction < 0.42f -> 0.56f
                                    heightFraction < 0.66f -> 0.78f
                                    else -> 0.36f
                                }
                                onHeightFractionChange(next)
                            },
                            modifier = Modifier
                                .size(26.dp)
                                .testTag("drawer_resize_toggle")
                        ) {
                            Icon(
                                imageVector = if (heightFraction > 0.52f) Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowUp,
                                contentDescription = "Toggle Drawer Height",
                                tint = Color(0xFFA39C93),
                                modifier = Modifier.size(17.dp)
                            )
                        }

                        // Minimize / Close Drawer (`—`)
                        IconButton(
                            onClick = onCloseDrawer,
                            modifier = Modifier
                                .size(26.dp)
                                .testTag("drawer_close_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Remove,
                                contentDescription = "Minimize Bottom Drawer",
                                tint = Color(0xFFA39C93),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                HorizontalDivider(color = Color(0xFF2E2724), thickness = 1.dp)

                // 3. CodeAssist Filter & Search Sub-Bar: (All) (Warnings) (Errors)  ... [🔍 Filter log...]
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF1A1615))
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        LogFilterLevel.entries.forEach { level ->
                            val isSelected = logFilterLevel == level
                            Surface(
                                color = if (isSelected) Color(0xFFD4883A) else Color(0xFF26201E),
                                shape = RoundedCornerShape(14.dp),
                                border = if (!isSelected) androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF3A322E)) else null,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(14.dp))
                                    .clickable { logFilterLevel = level }
                            ) {
                                Text(
                                    text = level.label,
                                    fontFamily = JetBrainsMonoFontFamily,
                                    fontSize = 10.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) Color(0xFF181412) else Color(0xFFD5CEC6),
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }

                    // Filter log search input matching CodeAssist screenshot
                    Box(
                        modifier = Modifier
                            .width(210.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFF241E1C))
                            .border(1.dp, Color(0xFF38302C), RoundedCornerShape(14.dp))
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                tint = Color(0xFF8E857D),
                                modifier = Modifier.size(13.dp)
                            )
                            Box(modifier = Modifier.weight(1f)) {
                                BasicTextField(
                                    value = filterQuery,
                                    onValueChange = { filterQuery = it },
                                    singleLine = true,
                                    textStyle = TextStyle(
                                        fontFamily = JetBrainsMonoFontFamily,
                                        fontSize = 10.5.sp,
                                        color = Color(0xFFE6E1DC)
                                    ),
                                    cursorBrush = SolidColor(Color(0xFFD4883A)),
                                    decorationBox = { inner ->
                                        if (filterQuery.isEmpty()) {
                                            Text(
                                                text = "Filter log...",
                                                fontFamily = JetBrainsMonoFontFamily,
                                                fontSize = 10.5.sp,
                                                color = Color(0xFF7D746C)
                                            )
                                        }
                                        inner()
                                    }
                                )
                            }
                            if (filterQuery.isNotEmpty()) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Clear Filter",
                                    tint = Color(0xFF8E857D),
                                    modifier = Modifier
                                        .size(12.dp)
                                        .clickable { filterQuery = "" }
                                )
                            }
                        }
                    }
                }

                HorizontalDivider(color = Color(0xFF282220), thickness = 0.5.dp)

                // 4. Missing SDK Toolchain Prompt Banner (if active file's language runtime isn't downloaded yet)
                AnimatedVisibility(visible = !isSdkInstalled && matchingSdkPlugin != null) {
                    if (matchingSdkPlugin != null) {
                        Surface(
                            color = Color(0xFF2B2118),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(0.5.dp, Color(0xFFD4883A).copy(alpha = 0.5f))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Extension,
                                        contentDescription = null,
                                        tint = Color(0xFFD4883A),
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Column {
                                        Text(
                                            text = "${activeLanguage.displayName} Engine Disabled (${matchingSdkPlugin.name})",
                                            fontFamily = JetBrainsMonoFontFamily,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFFF5E6D3)
                                        )
                                        Text(
                                            text = "Enable built-in ${matchingSdkPlugin.name} to run on-device or export a native Termux shell command below.",
                                            fontFamily = JetBrainsMonoFontFamily,
                                            fontSize = 9.5.sp,
                                            color = Color(0xFFC4B6A6)
                                        )
                                    }
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Button(
                                        onClick = { onInstallPlugin(matchingSdkPlugin.id) },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD4883A)),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.testTag("drawer_install_sdk_button")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = Color(0xFF181412),
                                            modifier = Modifier.size(13.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "Enable Engine",
                                            fontFamily = JetBrainsMonoFontFamily,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF181412)
                                        )
                                    }

                                    Surface(
                                        color = Color(0xFF382E26),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .clickable(onClick = onOpenPluginManager)
                                    ) {
                                        Text(
                                            text = "Toolchain Info",
                                            fontFamily = JetBrainsMonoFontFamily,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color(0xFFE6E1DC),
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // 5. Main Drawer Body Content
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .background(MaterialTheme.colorScheme.surfaceContainerLowest)
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    when (activeTab) {
                        BottomDrawerTab.TERMINAL -> {
                            TerminalTabContent(
                                output = executionOutput ?: debugInfo?.output ?: "Press Run to build & run this module.",
                                activeSection = activeSection,
                                sdkPlugin = matchingSdkPlugin,
                                filterLevel = logFilterLevel,
                                filterQuery = filterQuery,
                                stdinInput = stdinInput,
                                onUpdateStdinInput = onUpdateStdinInput,
                                onRunWithInput = onRunCode
                            )
                        }
                        BottomDrawerTab.REPL -> {
                            ReplTabContent(
                                history = replHistory,
                                onEvaluate = onEvaluateRepl,
                                onClear = onClearRepl
                            )
                        }
                        BottomDrawerTab.DEBUGGER -> {
                            DebuggerTabContent(
                                debugInfo = debugInfo,
                                vmDebugState = vmDebugState,
                                vmTrace = vmTrace,
                                vmTraceIndex = vmTraceIndex,
                                activeSection = activeSection,
                                sdkPlugin = matchingSdkPlugin,
                                activeStepLine = vmDebugState?.currentLine ?: debugStepLine,
                                activeBreakpoints = activeBreakpoints,
                                watchExpressions = watchExpressions,
                                onStepLineChange = { newLine ->
                                    debugStepLine = newLine
                                    onJumpToLine(newLine)
                                },
                                onResumeVm = onDebugResumeVm,
                                onStepOverVm = onDebugStepOverVm,
                                onResetVm = onDebugResetVm,
                                onToggleBreakpoint = onToggleBreakpoint,
                                onAddWatchExpression = onAddWatchExpression,
                                onRemoveWatchExpression = onRemoveWatchExpression,
                                onOpenPluginManager = onOpenPluginManager
                            )
                        }
                        BottomDrawerTab.AST_BYTECODE -> {
                            AstBytecodeTabContent(
                                artifact = effectiveArtifact,
                                activePc = vmDebugState?.currentPc,
                                onRecompile = onCompileAstBytecode,
                                onJumpToLine = onJumpToLine
                            )
                        }
                        BottomDrawerTab.GIT_DIFF -> {
                            GitDiffTabContent(
                                activeSection = activeSection,
                                snapshots = localSnapshots,
                                onCreateSnapshot = onCreateSnapshot,
                                onRestoreSnapshot = onRestoreSnapshot
                            )
                        }
                        BottomDrawerTab.PROBLEMS -> {
                            ProblemsTabContent(
                                diagnostics = diagnostics,
                                filterLevel = logFilterLevel,
                                filterQuery = filterQuery,
                                onJumpToLine = onJumpToLine,
                                onApplyQuickFixCode = onApplyQuickFixCode
                            )
                        }
                        BottomDrawerTab.LOGCAT -> {
                            LogcatTabContent(
                                entries = debugInfo?.logcatEntries.orEmpty(),
                                filterLevel = logFilterLevel,
                                filterQuery = filterQuery
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BottomDrawerTabChip(
    label: String,
    badgeText: String,
    isSelected: Boolean,
    badgeColor: Color,
    onClick: () -> Unit,
    testTag: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 7.dp, vertical = 4.dp)
            .testTag(testTag)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = label,
                fontFamily = JetBrainsMonoFontFamily,
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                softWrap = false
            )
            Spacer(modifier = Modifier.width(5.dp))
            Surface(
                color = badgeColor.copy(alpha = if (isSelected) 0.25f else 0.14f),
                shape = RoundedCornerShape(4.dp)
            ) {
                Text(
                    text = badgeText,
                    fontFamily = JetBrainsMonoFontFamily,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = badgeColor,
                    maxLines = 1,
                    softWrap = false,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(3.dp))
        Box(
            modifier = Modifier
                .width(if (isSelected) 32.dp else 0.dp)
                .height(2.dp)
                .clip(RoundedCornerShape(1.dp))
                .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent)
        )
    }
}

@Composable
private fun TerminalTabContent(
    output: String,
    activeSection: CodeSectionUiModel?,
    sdkPlugin: ToolchainPluginItem?,
    filterLevel: LogFilterLevel,
    filterQuery: String,
    stdinInput: String = "",
    onUpdateStdinInput: (String) -> Unit = {},
    onRunWithInput: () -> Unit = {}
) {
    val scrollState = rememberScrollState()
    val isError = output.contains("exit code 1") || output.contains("Error") || output.contains("error:")

    val filteredLines = remember(output, filterLevel, filterQuery) {
        output.lines().filter { line ->
            val matchesQuery = filterQuery.isBlank() || line.contains(filterQuery, ignoreCase = true)
            val matchesLevel = when (filterLevel) {
                LogFilterLevel.ALL -> true
                LogFilterLevel.WARNINGS -> line.contains("warn", ignoreCase = true) || line.contains("▲")
                LogFilterLevel.ERRORS -> line.contains("error", ignoreCase = true) || line.contains("exit code 1") || line.contains("^")
            }
            matchesQuery && matchesLevel
        }
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Interactive Standard Input (stdin) Bar for readln() / input() / Scanner / std::cin
        Surface(
            color = Color(0xFF1C1816),
            shape = RoundedCornerShape(10.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF342C28)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "stdin >",
                    fontFamily = JetBrainsMonoFontFamily,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFD4883A)
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFF13100F))
                        .border(0.75.dp, Color(0xFF3A312C), RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    BasicTextField(
                        value = stdinInput,
                        onValueChange = onUpdateStdinInput,
                        singleLine = true,
                        textStyle = TextStyle(
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 10.5.sp,
                            color = Color(0xFFE6E1DC)
                        ),
                        cursorBrush = SolidColor(Color(0xFFD4883A)),
                        decorationBox = { inner ->
                            if (stdinInput.isEmpty()) {
                                Text(
                                    text = "Console input for readln() / input() / Scanner / cin...",
                                    fontFamily = JetBrainsMonoFontFamily,
                                    fontSize = 10.sp,
                                    color = Color(0xFF7D746C),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            inner()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("terminal_stdin_input")
                    )
                }
                if (stdinInput.isNotEmpty()) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Clear Stdin",
                        tint = Color(0xFFA39C93),
                        modifier = Modifier
                            .size(13.dp)
                            .clickable { onUpdateStdinInput("") }
                    )
                }
                Surface(
                    color = Color(0xFF27C93F).copy(alpha = 0.2f),
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(0.5.dp, Color(0xFF27C93F)),
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable(onClick = onRunWithInput)
                        .testTag("terminal_send_stdin_button")
                ) {
                    Text(
                        text = "Send ▶",
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF27C93F),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }

        val clipboardManager = LocalClipboardManager.current
        val context = LocalContext.current

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(scrollState)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(if (isError) Color(0xFFFF5F56) else Color(0xFF27C93F))
                    )
                    Text(
                        text = "${activeSection?.language?.displayName ?: "Kotlin"} Console • ${sdkPlugin?.name ?: "On-Device AST"} • ${if (isError) "Process Failed" else "Ready"}",
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFA39C93),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                if (activeSection != null) {
                    Surface(
                        color = Color(0xFF24201E),
                        shape = RoundedCornerShape(999.dp),
                        border = androidx.compose.foundation.BorderStroke(0.5.dp, Color(0xFF5A4E46)),
                        modifier = Modifier
                            .clip(RoundedCornerShape(999.dp))
                            .clickable {
                                val cmd = CodeLanguageEngine.buildTermuxNativeCommand(
                                    fileName = activeSection.nativeFileName,
                                    code = activeSection.codeContent,
                                    language = activeSection.language
                                )
                                clipboardManager.setText(AnnotatedString(cmd))
                                Toast.makeText(
                                    context,
                                    "Copied native ${activeSection.language.displayName} Termux command",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                            .testTag("terminal_copy_termux_cmd_button")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copy Termux Native Command",
                                tint = Color(0xFFD4883A),
                                modifier = Modifier.size(11.dp)
                            )
                            Text(
                                text = "Termux Cmd",
                                fontFamily = JetBrainsMonoFontFamily,
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFD4883A)
                            )
                        }
                    }
                }
            }

            if (filteredLines.isEmpty()) {
                Text(
                    text = "No build or console lines match the current filter.",
                    fontFamily = JetBrainsMonoFontFamily,
                    fontSize = 11.sp,
                    color = Color(0xFF7D746C)
                )
            } else {
                filteredLines.forEach { line ->
                    val lineColor = when {
                        line.startsWith("$ ") -> Color(0xFFD4883A)
                        line.startsWith("[stdin]") -> Color(0xFF6897BB)
                        line.contains("Error") || line.contains("error:") || line.contains("SyntaxError") -> Color(0xFFF2B8B5)
                        line.contains("exit code 0") -> Color(0xFF8FB975)
                        line.contains("exit code 1") -> Color(0xFFFF5F56)
                        else -> Color(0xFFE6E1DC)
                    }
                    Text(
                        text = line,
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 12.sp,
                        lineHeight = 18.sp,
                        color = lineColor
                    )
                }
            }
        }
    }
}

@Composable
private fun DebuggerTabContent(
    debugInfo: CodeLanguageEngine.DebugSessionInfo?,
    vmDebugState: VmExecutionState? = null,
    vmTrace: List<VmExecutionState> = emptyList(),
    vmTraceIndex: Int = -1,
    activeSection: CodeSectionUiModel?,
    sdkPlugin: ToolchainPluginItem?,
    activeStepLine: Int,
    activeBreakpoints: Set<Int> = emptySet(),
    watchExpressions: List<String> = emptyList(),
    onStepLineChange: (Int) -> Unit,
    onResumeVm: () -> Unit = {},
    onStepOverVm: () -> Unit = {},
    onResetVm: () -> Unit = {},
    onToggleBreakpoint: (Int) -> Unit = {},
    onAddWatchExpression: (String) -> Unit = {},
    onRemoveWatchExpression: (String) -> Unit = {},
    onOpenPluginManager: () -> Unit
) {
    val variables = remember(vmDebugState?.variables, debugInfo?.variables) {
        linkedMapOf<String, String>().apply {
            debugInfo?.variables?.let { putAll(it) }
            vmDebugState?.variables?.let { putAll(it) }
        }
    }
    val callStack = vmDebugState?.callStack?.ifEmpty { debugInfo?.callStack.orEmpty() }
        ?: debugInfo?.callStack.orEmpty()
    val hasErrors = (debugInfo?.hasErrors == true) || (vmDebugState?.exitCode ?: 0) != 0
    val totalLines = (activeSection?.lineCount ?: 1).coerceAtLeast(1)
    val isPausedAtBreakpoint = vmDebugState?.isPaused == true || (activeStepLine in activeBreakpoints)
    var watchInput by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Step Debugger Toolbar
        item {
            Surface(
                color = if (isPausedAtBreakpoint) Color(0xFF2B1A19) else MaterialTheme.colorScheme.surfaceContainer,
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (isPausedAtBreakpoint) Color(0xFFFF5F56) else MaterialTheme.colorScheme.outlineVariant
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 6.dp),
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
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(
                                    when {
                                        isPausedAtBreakpoint -> Color(0xFFFF5F56)
                                        hasErrors -> Color(0xFFFF5F56)
                                        else -> Color(0xFF10B981)
                                    }
                                )
                        )
                        val vmBadge = if (vmDebugState != null && vmTrace.isNotEmpty()) {
                            "Step ${vmTraceIndex + 1}/${vmTrace.size} • PC=${vmDebugState.currentPc} • Ln $activeStepLine"
                        } else if (isPausedAtBreakpoint) {
                            "Paused • Ln $activeStepLine/$totalLines"
                        } else {
                            "${sdkPlugin?.debuggerName ?: "KtVM Debugger"} • Ln $activeStepLine/$totalLines"
                        }
                        Text(
                            text = vmBadge,
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isPausedAtBreakpoint) Color(0xFFFFB4AB) else MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                        modifier = Modifier.horizontalScroll(rememberScrollState())
                    ) {
                        // Resume / Run KtVM Debugger (F9)
                        Surface(
                            color = Color(0xFF233626),
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(0.5.dp, Color(0xFF6A8759)),
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    onResumeVm()
                                    if (activeBreakpoints.isNotEmpty()) {
                                        val sorted = activeBreakpoints.sorted()
                                        val nextBp = sorted.firstOrNull { it > activeStepLine } ?: sorted.first()
                                        onStepLineChange(nextBp)
                                    }
                                }
                                .testTag("debugger_resume_breakpoint_button")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = "Resume KtVM Session",
                                    tint = Color(0xFF81C784),
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = "Resume",
                                    fontFamily = JetBrainsMonoFontFamily,
                                    fontSize = 9.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF81C784),
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                        }

                        // Step Over (F8)
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceContainerHigh,
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant),
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    onStepOverVm()
                                    val next = if (activeStepLine < totalLines) activeStepLine + 1 else 1
                                    onStepLineChange(next)
                                }
                                .testTag("debugger_step_over_button")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.SkipNext,
                                    contentDescription = "Step Over",
                                    tint = Color(0xFFD4883A),
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = "Step",
                                    fontFamily = JetBrainsMonoFontFamily,
                                    fontSize = 9.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                        }

                        // Step Into (F7)
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceContainerHigh,
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant),
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    onStepOverVm()
                                    val next = (activeStepLine + 1).coerceAtMost(totalLines)
                                    onStepLineChange(next)
                                }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.SubdirectoryArrowRight,
                                    contentDescription = "Step Into",
                                    tint = Color(0xFF6897BB),
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = "Into",
                                    fontFamily = JetBrainsMonoFontFamily,
                                    fontSize = 9.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                        }

                        // Reset Frame
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceContainerHigh,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    onResetVm()
                                    onStepLineChange(1)
                                }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "Reset Debugger",
                                    tint = Color(0xFF8FB975),
                                    modifier = Modifier.size(13.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // KtVM Operand Stack & Frame Snapshots Preview
        if (vmDebugState != null) {
            item {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceContainer,
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(0.8.dp, Color(0xFF10B981).copy(alpha = 0.45f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "KTVM TRACE • ${vmDebugState.stepsExecuted} VM steps • Exit ${vmDebugState.exitCode}",
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF10B981)
                        )
                        if (vmDebugState.operandStackPreview.isNotEmpty()) {
                            Text(
                                text = "Operand Stack: [${vmDebugState.operandStackPreview.joinToString(", ")}]",
                                fontFamily = JetBrainsMonoFontFamily,
                                fontSize = 10.sp,
                                color = Color(0xFF93C5FD),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        if (vmDebugState.heapPreview.isNotEmpty()) {
                            Text(
                                text = "Heap Objects: ${vmDebugState.heapPreview.entries.joinToString(" | ") { "${it.key}=${it.value}" }}",
                                fontFamily = JetBrainsMonoFontFamily,
                                fontSize = 10.sp,
                                color = Color(0xFFFCD34D),
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        if (vmDebugState.frameSnapshots.isNotEmpty()) {
                            vmDebugState.frameSnapshots.forEach { snap ->
                                Text(
                                    text = "Frame ${snap.functionName} (Ln ${snap.line}): ${snap.locals.entries.joinToString(", ") { "${it.key}=${it.value}" }.ifEmpty { "no locals" }}",
                                    fontFamily = JetBrainsMonoFontFamily,
                                    fontSize = 9.5.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        }

        // Active Line Breakpoints Strip (tap gutter line numbers or pills here)
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "BREAKPOINTS (${activeBreakpoints.size}):",
                    fontFamily = JetBrainsMonoFontFamily,
                    fontSize = 9.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFA39C93)
                )
                if (activeBreakpoints.isEmpty()) {
                    Text(
                        text = "Tap any line number in the editor gutter to toggle ● breakpoint",
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 9.5.sp,
                        color = Color(0xFF7D746C)
                    )
                } else {
                    activeBreakpoints.sorted().forEach { bpLine ->
                        Surface(
                            color = if (bpLine == activeStepLine) Color(0xFF4F2220) else Color(0xFF291C1B),
                            shape = RoundedCornerShape(999.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFF5F56)),
                            modifier = Modifier
                                .clip(RoundedCornerShape(999.dp))
                                .clickable { onStepLineChange(bpLine) }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFFF5F56))
                                )
                                Text(
                                    text = "Ln $bpLine",
                                    fontFamily = JetBrainsMonoFontFamily,
                                    fontSize = 9.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFFFB4AB)
                                )
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Clear Breakpoint Ln $bpLine",
                                    tint = Color(0xFFFFB4AB),
                                    modifier = Modifier
                                        .size(11.dp)
                                        .clickable { onToggleBreakpoint(bpLine) }
                                )
                            }
                        }
                    }
                }
            }
        }

        // Expression Watch & Evaluator Bar
        item {
            Surface(
                color = Color(0xFF1C1816),
                shape = RoundedCornerShape(10.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF38302C)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF141110))
                                .border(0.75.dp, Color(0xFF3E3530), RoundedCornerShape(8.dp))
                                .padding(horizontal = 8.dp, vertical = 5.dp)
                        ) {
                            BasicTextField(
                                value = watchInput,
                                onValueChange = { watchInput = it },
                                singleLine = true,
                                textStyle = TextStyle(
                                    fontFamily = JetBrainsMonoFontFamily,
                                    fontSize = 10.5.sp,
                                    color = Color(0xFFE6E1DC)
                                ),
                                cursorBrush = SolidColor(Color(0xFFD4883A)),
                                decorationBox = { inner ->
                                    if (watchInput.isEmpty()) {
                                        Text(
                                            text = "Evaluate expression or add watch (e.g. count * 2)...",
                                            fontFamily = JetBrainsMonoFontFamily,
                                            fontSize = 10.sp,
                                            color = Color(0xFF7D746C)
                                        )
                                    }
                                    inner()
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("debugger_watch_input")
                            )
                        }

                        if (watchInput.isNotBlank()) {
                            val liveEval = remember(watchInput, variables) {
                                CodeLanguageEngine.evaluateWatchExpression(watchInput, variables)
                            }
                            Text(
                                text = "= $liveEval",
                                fontFamily = JetBrainsMonoFontFamily,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF81C784)
                            )
                        }

                        Surface(
                            color = Color(0xFFD4883A),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    if (watchInput.isNotBlank()) {
                                        onAddWatchExpression(watchInput)
                                        watchInput = ""
                                    }
                                }
                                .testTag("debugger_add_watch_button")
                        ) {
                            Text(
                                text = "+ Watch",
                                fontFamily = JetBrainsMonoFontFamily,
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF181412),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                            )
                        }
                    }

                    if (watchExpressions.isNotEmpty()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            watchExpressions.forEach { expr ->
                                val evaluated = CodeLanguageEngine.evaluateWatchExpression(expr, variables)
                                Surface(
                                    color = Color(0xFF26201D),
                                    shape = RoundedCornerShape(999.dp),
                                    border = androidx.compose.foundation.BorderStroke(0.5.dp, Color(0xFFD4883A).copy(alpha = 0.6f))
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    ) {
                                        Text(
                                            text = "$expr = ",
                                            fontFamily = JetBrainsMonoFontFamily,
                                            fontSize = 9.5.sp,
                                            color = Color(0xFFD4883A),
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = evaluated,
                                            fontFamily = JetBrainsMonoFontFamily,
                                            fontSize = 9.5.sp,
                                            color = Color(0xFF8FB975)
                                        )
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Remove Watch $expr",
                                            tint = Color(0xFFA39C93),
                                            modifier = Modifier
                                                .size(11.dp)
                                                .clickable { onRemoveWatchExpression(expr) }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Call Stack Frames
        if (callStack.isNotEmpty()) {
            item {
                Text(
                    text = "CALL STACK FRAMES (${callStack.size})",
                    fontFamily = JetBrainsMonoFontFamily,
                    fontSize = 9.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFA39C93)
                )
            }
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    callStack.forEach { frame ->
                        Surface(
                            color = Color(0xFF1F1B19),
                            shape = RoundedCornerShape(999.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF38302C)),
                            modifier = Modifier.clickable {
                                val lineNum = frame.substringAfterLast(":").toIntOrNull() ?: 1
                                onStepLineChange(lineNum)
                            }
                        ) {
                            Text(
                                text = "ƒ $frame",
                                fontFamily = JetBrainsMonoFontFamily,
                                fontSize = 10.sp,
                                color = Color(0xFFD4883A),
                                modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }

        item {
            Text(
                text = "VARIABLES INSPECTOR (${variables.size})",
                fontFamily = JetBrainsMonoFontFamily,
                fontSize = 9.5.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFA39C93),
                modifier = Modifier.padding(top = 2.dp)
            )
        }

        if (variables.isEmpty()) {
            item {
                Surface(
                    color = Color(0xFF1D1A18),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "No local variables in scope yet. Declare variables (e.g. 'val count = 42' or 'name = \"KtScribe\"') to inspect live values during step debugging.",
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 11.sp,
                        color = Color(0xFFA39C93),
                        modifier = Modifier.padding(10.dp)
                    )
                }
            }
        } else {
            items(variables.toList(), key = { it.first }) { (varName, varValue) ->
                Surface(
                    color = Color(0xFF1F1C1A),
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF36312C)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            IdeCompletionBadgeIcon(badgeChar = "v")
                            Text(
                                text = varName,
                                fontFamily = JetBrainsMonoFontFamily,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFD4883A)
                            )
                        }
                        Text(
                            text = varValue,
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 11.sp,
                            color = Color(0xFF8FB975),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ProblemsTabContent(
    diagnostics: List<KotlinDiagnostic>,
    filterLevel: LogFilterLevel,
    filterQuery: String,
    onJumpToLine: (Int) -> Unit,
    onApplyQuickFixCode: (String) -> Unit = {}
) {
    val filtered = remember(diagnostics, filterLevel, filterQuery) {
        diagnostics.filter { diag ->
            val matchesQuery = filterQuery.isBlank() || diag.message.contains(filterQuery, ignoreCase = true)
            val matchesLevel = when (filterLevel) {
                LogFilterLevel.ALL -> true
                LogFilterLevel.WARNINGS -> diag.severity == DiagnosticSeverity.WARNING
                LogFilterLevel.ERRORS -> diag.severity == DiagnosticSeverity.ERROR
            }
            matchesQuery && matchesLevel
        }
    }

    if (filtered.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = Color(0xFF6A9955),
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "No syntax errors or problems matching filter in active file.",
                    fontFamily = JetBrainsMonoFontFamily,
                    fontSize = 11.5.sp,
                    color = Color(0xFF8FB975)
                )
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(filtered) { diag ->
                val isErr = diag.severity == DiagnosticSeverity.ERROR
                Surface(
                    color = if (isErr) Color(0xFF2B1817) else Color(0xFF282319),
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isErr) Color(0xFF5E2724) else Color(0xFF5E4924)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onJumpToLine(diag.line) }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = if (isErr) "✕ Error" else "▲ Fix/Warn",
                                fontFamily = JetBrainsMonoFontFamily,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isErr) Color(0xFFFF5F56) else Color(0xFFFFBD2E)
                            )
                            Text(
                                text = "Ln ${diag.line}: ${diag.message}",
                                fontFamily = JetBrainsMonoFontFamily,
                                fontSize = 11.sp,
                                color = Color(0xFFE6E1DC),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        if (diag.quickFixLabel != null) {
                            Surface(
                                color = Color(0xFFD4883A).copy(alpha = 0.25f),
                                shape = RoundedCornerShape(999.dp),
                                border = androidx.compose.foundation.BorderStroke(0.5.dp, Color(0xFFD4883A)),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(999.dp))
                                    .clickable {
                                        diag.fixedCode?.let(onApplyQuickFixCode)
                                    }
                            ) {
                                Text(
                                    text = diag.quickFixLabel,
                                    fontFamily = JetBrainsMonoFontFamily,
                                    fontSize = 9.5.sp,
                                    color = Color(0xFFFFC66D),
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LogcatTabContent(
    entries: List<String>,
    filterLevel: LogFilterLevel,
    filterQuery: String
) {
    val filtered = remember(entries, filterLevel, filterQuery) {
        entries.filter { line ->
            val matchesQuery = filterQuery.isBlank() || line.contains(filterQuery, ignoreCase = true)
            val matchesLevel = when (filterLevel) {
                LogFilterLevel.ALL -> true
                LogFilterLevel.WARNINGS -> line.startsWith("W/")
                LogFilterLevel.ERRORS -> line.startsWith("E/")
            }
            matchesQuery && matchesLevel
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        if (filtered.isEmpty()) {
            item {
                Text(
                    text = "No Logcat entries matching current filter.",
                    fontFamily = JetBrainsMonoFontFamily,
                    fontSize = 11.sp,
                    color = Color(0xFF8E857D)
                )
            }
        } else {
            items(filtered) { entry ->
                val color = when {
                    entry.startsWith("E/") -> Color(0xFFFF6B68)
                    entry.startsWith("W/") -> Color(0xFFFFC66D)
                    entry.startsWith("D/") -> Color(0xFF6897BB)
                    else -> Color(0xFFA9B7C6)
                }
                Text(
                    text = entry,
                    fontFamily = JetBrainsMonoFontFamily,
                    fontSize = 11.5.sp,
                    lineHeight = 17.sp,
                    color = color
                )
            }
        }
    }
}

@Composable
private fun ReplTabContent(
    history: List<ReplHistoryEntry>,
    onEvaluate: (String) -> Unit,
    onClear: () -> Unit
) {
    var inputText by remember { mutableStateOf("") }
    val presets = listOf(
        "val x = 6 * 7; x",
        "(1..5).map { it * it }",
        "\"KtVM\".uppercase()",
        "fun sq(n: Int) = n * n; sq(9)"
    )

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Quick REPL Snippets Strip
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "λ Kotlin REPL",
                fontFamily = JetBrainsMonoFontFamily,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFC084FC),
                maxLines = 1,
                softWrap = false
            )
            presets.forEach { snippet ->
                Surface(
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    shape = RoundedCornerShape(999.dp),
                    border = androidx.compose.foundation.BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .clickable { onEvaluate(snippet) }
                ) {
                    Text(
                        text = snippet,
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 9.5.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        softWrap = false,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }
        }

        // REPL Input Bar
        Surface(
            color = MaterialTheme.colorScheme.surfaceContainer,
            shape = RoundedCornerShape(10.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "kt>",
                    fontFamily = JetBrainsMonoFontFamily,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFC084FC)
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(6.dp))
                        .background(MaterialTheme.colorScheme.surfaceContainerLowest)
                        .border(0.75.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 5.dp)
                ) {
                    BasicTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        singleLine = true,
                        textStyle = TextStyle(
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        ),
                        cursorBrush = SolidColor(Color(0xFFC084FC)),
                        decorationBox = { inner ->
                            if (inputText.isEmpty()) {
                                Text(
                                    text = "Type Kotlin expr, val/var, or fun (persistent VM scope)...",
                                    fontFamily = JetBrainsMonoFontFamily,
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            inner()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("repl_input_field")
                    )
                }
                Surface(
                    color = Color(0xFFC084FC).copy(alpha = 0.22f),
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(0.5.dp, Color(0xFFC084FC)),
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable {
                            if (inputText.isNotBlank()) {
                                onEvaluate(inputText)
                                inputText = ""
                            }
                        }
                        .testTag("repl_eval_button")
                ) {
                    Text(
                        text = "Eval ▶",
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFE9D5FF),
                        maxLines = 1,
                        softWrap = false,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                    )
                }
                if (history.isNotEmpty()) {
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceContainerHigh,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable(onClick = onClear)
                    ) {
                        Text(
                            text = "Reset",
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 9.5.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            softWrap = false,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 5.dp)
                        )
                    }
                }
            }
        }

        // REPL History List
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            if (history.isEmpty()) {
                item {
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceContainer,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "On-Device Kotlin REPL is ready. Expressions compile directly to KtVM bytecode and persist variables/functions across evaluations.",
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }
            } else {
                items(history.reversed(), key = { it.id }) { entry ->
                    Surface(
                        color = if (entry.isError) Color(0xFF2B1817) else MaterialTheme.colorScheme.surfaceContainer,
                        shape = RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(
                            0.75.dp,
                            if (entry.isError) Color(0xFF6B312E) else MaterialTheme.colorScheme.outlineVariant
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                            verticalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "kt> ${entry.input}",
                                    fontFamily = JetBrainsMonoFontFamily,
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.weight(1f),
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "${entry.inferredType} • ${entry.compiledOpsCount} ops",
                                    fontFamily = JetBrainsMonoFontFamily,
                                    fontSize = 9.sp,
                                    color = Color(0xFFC084FC),
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                            Text(
                                text = entry.output,
                                fontFamily = JetBrainsMonoFontFamily,
                                fontSize = 11.sp,
                                color = if (entry.isError) Color(0xFFF2B8B5) else Color(0xFF8FB975)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AstBytecodeTabContent(
    artifact: CompilationArtifact?,
    activePc: Int?,
    onRecompile: () -> Unit,
    onJumpToLine: (Int) -> Unit
) {
    var showBytecodeMode by remember { mutableStateOf(true) }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (artifact == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = "Open a source file to inspect AST & compiled KtVM bytecode.",
                    fontFamily = JetBrainsMonoFontFamily,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            return
        }

        // Top Inspector Summary & Toggle Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier
                    .weight(1f)
                    .horizontalScroll(rememberScrollState())
            ) {
                Surface(
                    color = if (showBytecodeMode) Color(0xFF38BDF8).copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceContainerHigh,
                    shape = RoundedCornerShape(999.dp),
                    border = androidx.compose.foundation.BorderStroke(0.75.dp, if (showBytecodeMode) Color(0xFF38BDF8) else MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .clickable { showBytecodeMode = true }
                ) {
                    Text(
                        text = "Bytecode (${artifact.bytecode.size})",
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (showBytecodeMode) Color(0xFF7DD3FC) else MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        softWrap = false,
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp)
                    )
                }

                Surface(
                    color = if (!showBytecodeMode) Color(0xFFD4883A).copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceContainerHigh,
                    shape = RoundedCornerShape(999.dp),
                    border = androidx.compose.foundation.BorderStroke(0.75.dp, if (!showBytecodeMode) Color(0xFFD4883A) else MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .clickable { showBytecodeMode = false }
                ) {
                    Text(
                        text = "AST (${artifact.astSummaryLines.size}) & Symbols (${artifact.symbols.size})",
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (!showBytecodeMode) Color(0xFFFFC66D) else MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        softWrap = false,
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp)
                    )
                }

                Text(
                    text = "${artifact.compileTimeMs}ms • Pool ${artifact.constantPool.size}",
                    fontFamily = JetBrainsMonoFontFamily,
                    fontSize = 9.5.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    softWrap = false
                )
            }

            Surface(
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                shape = RoundedCornerShape(8.dp),
                border = androidx.compose.foundation.BorderStroke(0.75.dp, MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable(onClick = onRecompile)
            ) {
                Text(
                    text = "⚙ Compile",
                    fontFamily = JetBrainsMonoFontFamily,
                    fontSize = 9.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF7DD3FC),
                    maxLines = 1,
                    softWrap = false,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            if (showBytecodeMode) {
                items(artifact.bytecode, key = { it.pc }) { instr ->
                    val isCurrentPc = activePc == instr.pc
                    Surface(
                        color = if (isCurrentPc) Color(0xFF283A2C) else MaterialTheme.colorScheme.surfaceContainer,
                        shape = RoundedCornerShape(6.dp),
                        border = if (isCurrentPc) androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF10B981)) else null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onJumpToLine(instr.line) }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "%03d".format(instr.pc),
                                fontFamily = JetBrainsMonoFontFamily,
                                fontSize = 9.5.sp,
                                color = if (isCurrentPc) Color(0xFF10B981) else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = instr.op.name,
                                fontFamily = JetBrainsMonoFontFamily,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF7DD3FC),
                                modifier = Modifier.width(104.dp)
                            )
                            Text(
                                text = instr.operand.ifBlank { instr.comment },
                                fontFamily = JetBrainsMonoFontFamily,
                                fontSize = 10.sp,
                                color = Color(0xFFFCD34D),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = "L${instr.line}",
                                fontFamily = JetBrainsMonoFontFamily,
                                fontSize = 9.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                item {
                    Text(
                        text = "AST NODES (${artifact.pluginName})",
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFFC66D)
                    )
                }
                items(artifact.astSummaryLines) { astLine ->
                    Text(
                        text = astLine,
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 10.5.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                if (artifact.symbols.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "SYMBOL TABLE (${artifact.symbols.size})",
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF7DD3FC)
                        )
                    }
                    items(artifact.symbols) { sym ->
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceContainer,
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onJumpToLine(sym.line) }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${sym.kind} ${sym.name}: ${sym.inferredType}",
                                    fontFamily = JetBrainsMonoFontFamily,
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.weight(1f),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "${sym.scope} • Ln ${sym.line}",
                                    fontFamily = JetBrainsMonoFontFamily,
                                    fontSize = 9.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GitDiffTabContent(
    activeSection: CodeSectionUiModel?,
    snapshots: List<LocalCodeSnapshot>,
    onCreateSnapshot: () -> Unit,
    onRestoreSnapshot: (Long) -> Unit
) {
    var selectedSnapshotId by remember(activeSection?.id, snapshots.size) {
        mutableStateOf(snapshots.firstOrNull()?.id)
    }
    val selectedSnapshot = snapshots.find { it.id == selectedSnapshotId } ?: snapshots.firstOrNull()
    val currentCode = activeSection?.codeContent.orEmpty()
    val diffResult = remember(selectedSnapshot?.codeContent, currentCode) {
        val base = selectedSnapshot?.codeContent ?: currentCode
        DiffEngine.computeLineDiff(base, currentCode)
    }
    val timeFormatter = remember { SimpleDateFormat("HH:mm:ss", Locale.getDefault()) }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Snapshot & Revision Header Controls
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier
                    .weight(1f)
                    .horizontalScroll(rememberScrollState())
            ) {
                Surface(
                    color = Color(0xFF27C93F).copy(alpha = 0.16f),
                    shape = RoundedCornerShape(999.dp)
                ) {
                    Text(
                        text = "+${diffResult.additionsCount}",
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF81C784),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
                Surface(
                    color = Color(0xFFFF5F56).copy(alpha = 0.16f),
                    shape = RoundedCornerShape(999.dp)
                ) {
                    Text(
                        text = "-${diffResult.deletionsCount}",
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFFB4AB),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                snapshots.forEachIndexed { idx, snap ->
                    val isSelected = selectedSnapshot?.id == snap.id
                    Surface(
                        color = if (isSelected) Color(0xFFF59E0B).copy(alpha = 0.22f) else MaterialTheme.colorScheme.surfaceContainerHigh,
                        shape = RoundedCornerShape(999.dp),
                        border = androidx.compose.foundation.BorderStroke(
                            0.75.dp,
                            if (isSelected) Color(0xFFF59E0B) else MaterialTheme.colorScheme.outlineVariant
                        ),
                        modifier = Modifier
                            .clip(RoundedCornerShape(999.dp))
                            .clickable { selectedSnapshotId = snap.id }
                    ) {
                        Text(
                            text = "v${snapshots.size - idx} (${timeFormatter.format(Date(snap.timestampMs))})",
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 9.5.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) Color(0xFFFCD34D) else MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            softWrap = false,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                Surface(
                    color = Color(0xFFF59E0B).copy(alpha = 0.22f),
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(0.5.dp, Color(0xFFF59E0B)),
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable(onClick = onCreateSnapshot)
                        .testTag("git_diff_create_snapshot_button")
                ) {
                    Text(
                        text = "+ Snapshot",
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFCD34D),
                        maxLines = 1,
                        softWrap = false,
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp)
                    )
                }

                if (selectedSnapshot != null && diffResult.hasChanges) {
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceContainerHigh,
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant),
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onRestoreSnapshot(selectedSnapshot.id) }
                            .testTag("git_diff_restore_snapshot_button")
                    ) {
                        Text(
                            text = "↺ Restore",
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF7DD3FC),
                            maxLines = 1,
                            softWrap = false,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }

        if (snapshots.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = "No snapshots saved yet for this file. Tap '+ Snapshot' to record a local Git-style revision baseline.",
                    fontFamily = JetBrainsMonoFontFamily,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                items(diffResult.lines) { line ->
                    val rowBg = when (line.kind) {
                        DiffLineKind.ADDED -> Color(0xFF1A2F1D)
                        DiffLineKind.REMOVED -> Color(0xFF331B1A)
                        DiffLineKind.UNCHANGED -> Color.Transparent
                    }
                    val marker = when (line.kind) {
                        DiffLineKind.ADDED -> "+"
                        DiffLineKind.REMOVED -> "-"
                        DiffLineKind.UNCHANGED -> " "
                    }
                    val textColor = when (line.kind) {
                        DiffLineKind.ADDED -> Color(0xFFA5D6A7)
                        DiffLineKind.REMOVED -> Color(0xFFFFB4AB)
                        DiffLineKind.UNCHANGED -> MaterialTheme.colorScheme.onSurfaceVariant
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(rowBg, RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = line.oldLineNumber?.toString()?.padStart(3) ?: "   ",
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 9.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f)
                        )
                        Text(
                            text = line.newLineNumber?.toString()?.padStart(3) ?: "   ",
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 9.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f)
                        )
                        Text(
                            text = marker,
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = textColor
                        )
                        Text(
                            text = line.text,
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 10.5.sp,
                            color = textColor,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Desktop IDE Status Bar along the bottom of the editor window.
 */
@Composable
fun IdeBottomStatusBar(
    activeSection: CodeSectionUiModel?,
    isDrawerOpen: Boolean,
    activeTab: BottomDrawerTab,
    onToggleDrawer: (BottomDrawerTab) -> Unit,
    modifier: Modifier = Modifier
) {
    val diagnostics = remember(activeSection?.codeContent, activeSection?.language) {
        if (activeSection == null) emptyList()
        else CodeLanguageEngine.getDiagnosticsForLanguage(activeSection.codeContent, activeSection.language)
    }
    val errCount = diagnostics.count { it.severity == DiagnosticSeverity.ERROR }
    val warnCount = diagnostics.count { it.severity == DiagnosticSeverity.WARNING }

    Surface(
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = modifier
            .fillMaxWidth()
            .height(28.dp)
            .testTag("ide_bottom_status_bar")
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f, fill = false)
            ) {
                if (activeSection != null) {
                    val langColor = activeSection.language.badgeColor
                    Surface(
                        color = langColor.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(3.dp)
                    ) {
                        Text(
                            text = activeSection.language.badgeLabel,
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = langColor,
                            maxLines = 1,
                            softWrap = false,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                        )
                    }
                    Text(
                        text = activeSection.nativeFileName,
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.horizontalScroll(rememberScrollState())
            ) {
                Surface(
                    color = if (isDrawerOpen && activeTab == BottomDrawerTab.PROBLEMS) MaterialTheme.colorScheme.surfaceContainerHigh else Color.Transparent,
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .clickable { onToggleDrawer(BottomDrawerTab.PROBLEMS) }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "✕ $errCount",
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (errCount > 0) Color(0xFFFF5F56) else MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            softWrap = false
                        )
                        Text(
                            text = "▲ $warnCount",
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (warnCount > 0) Color(0xFFFFBD2E) else MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }

                StatusBarQuickPill(
                    label = ">_ Build",
                    activeColor = Color(0xFFD4883A),
                    isActive = isDrawerOpen && activeTab == BottomDrawerTab.TERMINAL,
                    onClick = { onToggleDrawer(BottomDrawerTab.TERMINAL) }
                )
                StatusBarQuickPill(
                    label = "λ REPL",
                    activeColor = Color(0xFFC084FC),
                    isActive = isDrawerOpen && activeTab == BottomDrawerTab.REPL,
                    onClick = { onToggleDrawer(BottomDrawerTab.REPL) }
                )
                StatusBarQuickPill(
                    label = "⛁ Bytecode",
                    activeColor = Color(0xFF38BDF8),
                    isActive = isDrawerOpen && activeTab == BottomDrawerTab.AST_BYTECODE,
                    onClick = { onToggleDrawer(BottomDrawerTab.AST_BYTECODE) }
                )
                StatusBarQuickPill(
                    label = "± Diff",
                    activeColor = Color(0xFFF59E0B),
                    isActive = isDrawerOpen && activeTab == BottomDrawerTab.GIT_DIFF,
                    onClick = { onToggleDrawer(BottomDrawerTab.GIT_DIFF) }
                )
                StatusBarQuickPill(
                    label = "Debug",
                    activeColor = Color(0xFF10B981),
                    isActive = isDrawerOpen && activeTab == BottomDrawerTab.DEBUGGER,
                    onClick = { onToggleDrawer(BottomDrawerTab.DEBUGGER) }
                )
            }
        }
    }
}

@Composable
private fun StatusBarQuickPill(
    label: String,
    activeColor: Color,
    isActive: Boolean,
    onClick: () -> Unit
) {
    Surface(
        color = if (isActive) MaterialTheme.colorScheme.surfaceContainerHigh else Color.Transparent,
        shape = RoundedCornerShape(4.dp),
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .clickable(onClick = onClick)
    ) {
        Text(
            text = label,
            fontFamily = JetBrainsMonoFontFamily,
            fontSize = 9.5.sp,
            fontWeight = FontWeight.Medium,
            color = if (isActive) activeColor else MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            softWrap = false,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}
