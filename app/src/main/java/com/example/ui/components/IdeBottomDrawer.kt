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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.code.CodeLanguageEngine
import com.example.code.DiagnosticSeverity
import com.example.code.KotlinDiagnostic
import com.example.model.BottomDrawerTab
import com.example.model.CodeSectionUiModel
import com.example.ui.theme.JetBrainsMonoFontFamily

/**
 * Desktop-IDE sliding bottom drawer (VS Code / IntelliJ style) containing:
 * - Terminal: execution logs, stdout, stderr, process exit code
 * - Debugger: live variable inspector, call stack, execution status
 * - Problems: aggregate syntax errors, warnings, quick fixes, and line jumping
 */
@Composable
fun IdeBottomDrawer(
    isOpen: Boolean,
    activeTab: BottomDrawerTab,
    activeSection: CodeSectionUiModel?,
    executionOutput: String?,
    heightFraction: Float,
    onTabSelected: (BottomDrawerTab) -> Unit,
    onHeightFractionChange: (Float) -> Unit,
    onRunCode: () -> Unit,
    onClearTerminal: () -> Unit,
    onCloseDrawer: () -> Unit,
    onJumpToLine: (Int) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val diagnostics = remember(activeSection?.codeContent, activeSection?.language) {
        if (activeSection == null) emptyList()
        else CodeLanguageEngine.getDiagnosticsForLanguage(activeSection.codeContent, activeSection.language)
    }

    val debugInfo = remember(activeSection?.codeContent, activeSection?.language) {
        if (activeSection == null) null
        else CodeLanguageEngine.executeAndDebug(activeSection.codeContent, activeSection.language)
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
            .fillMaxHeight(heightFraction)
    ) {
        Surface(
            color = Color(0xFF181615),
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
            shadowElevation = 24.dp,
            modifier = Modifier
                .fillMaxSize()
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant,
                    shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
                )
                .testTag("ide_bottom_drawer")
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Drag handle bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(14.dp)
                        .draggable(
                            orientation = Orientation.Vertical,
                            state = rememberDraggableState { deltaY ->
                                val deltaFraction = -deltaY / 1200f
                                onHeightFractionChange((heightFraction + deltaFraction).coerceIn(0.25f, 0.85f))
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .width(36.dp)
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(Color(0xFF5A524A))
                    )
                }

                // Drawer Header Bar with Tabs and Controls
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF201D1A))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Left Tabs: Terminal, Debugger, Problems
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        // Terminal Tab
                        BottomDrawerTabChip(
                            label = "Terminal",
                            badgeText = ">_",
                            isSelected = activeTab == BottomDrawerTab.TERMINAL,
                            badgeColor = Color(0xFF06B6D4),
                            onClick = { onTabSelected(BottomDrawerTab.TERMINAL) },
                            testTag = "drawer_tab_terminal"
                        )

                        // Debugger Tab
                        BottomDrawerTabChip(
                            label = "Debugger",
                            badgeText = "${debugInfo?.variables?.size ?: 0} vars",
                            isSelected = activeTab == BottomDrawerTab.DEBUGGER,
                            badgeColor = Color(0xFF10B981),
                            onClick = { onTabSelected(BottomDrawerTab.DEBUGGER) },
                            testTag = "drawer_tab_debugger"
                        )

                        // Problems Tab
                        BottomDrawerTabChip(
                            label = "Problems",
                            badgeText = if (problemCount > 0) "$problemCount" else "0",
                            isSelected = activeTab == BottomDrawerTab.PROBLEMS,
                            badgeColor = if (problemCount > 0) Color(0xFFFF5F56) else Color(0xFF8FB975),
                            onClick = { onTabSelected(BottomDrawerTab.PROBLEMS) },
                            testTag = "drawer_tab_problems"
                        )
                    }

                    // Right Controls: Run, Clear, Resize Height, Close
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        // Quick Run
                        Surface(
                            color = Color(0xFF27C93F).copy(alpha = 0.2f),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
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
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = "Run",
                                    fontFamily = JetBrainsMonoFontFamily,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF27C93F)
                                )
                            }
                        }

                        // Clear Output
                        IconButton(
                            onClick = onClearTerminal,
                            modifier = Modifier.size(28.dp).testTag("drawer_clear_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear Terminal Output",
                                tint = Color(0xFFA39C93),
                                modifier = Modifier.size(15.dp)
                            )
                        }

                        // Cycle Height (35% -> 55% -> 75%)
                        IconButton(
                            onClick = {
                                val next = when {
                                    heightFraction < 0.40f -> 0.55f
                                    heightFraction < 0.65f -> 0.78f
                                    else -> 0.35f
                                }
                                onHeightFractionChange(next)
                            },
                            modifier = Modifier.size(28.dp).testTag("drawer_resize_toggle")
                        ) {
                            Icon(
                                imageVector = if (heightFraction > 0.50f) Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowUp,
                                contentDescription = "Toggle Drawer Height",
                                tint = Color(0xFFA39C93),
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        // Close Drawer
                        IconButton(
                            onClick = onCloseDrawer,
                            modifier = Modifier.size(28.dp).testTag("drawer_close_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close Bottom Drawer",
                                tint = Color(0xFFA39C93),
                                modifier = Modifier.size(15.dp)
                            )
                        }
                    }
                }

                HorizontalDivider(color = Color(0xFF36312C), thickness = 1.dp)

                // Body content based on active tab
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .background(Color(0xFF141211))
                        .padding(10.dp)
                ) {
                    when (activeTab) {
                        BottomDrawerTab.TERMINAL -> {
                            TerminalTabContent(
                                output = executionOutput ?: debugInfo?.output ?: "$ kotlinc main.kt (Ready to execute)",
                                activeSection = activeSection
                            )
                        }
                        BottomDrawerTab.DEBUGGER -> {
                            DebuggerTabContent(
                                debugInfo = debugInfo,
                                activeSection = activeSection
                            )
                        }
                        BottomDrawerTab.PROBLEMS -> {
                            ProblemsTabContent(
                                diagnostics = diagnostics,
                                onJumpToLine = onJumpToLine
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
    Surface(
        color = if (isSelected) Color(0xFF2C2723) else Color.Transparent,
        shape = RoundedCornerShape(6.dp),
        border = if (isSelected) androidx.compose.foundation.BorderStroke(1.dp, badgeColor.copy(alpha = 0.5f)) else null,
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .clickable(onClick = onClick)
            .testTag(testTag)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
        ) {
            Text(
                text = label,
                fontFamily = JetBrainsMonoFontFamily,
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) Color(0xFFE6E1DC) else Color(0xFFA39C93)
            )
            Spacer(modifier = Modifier.width(5.dp))
            Surface(
                color = badgeColor.copy(alpha = 0.18f),
                shape = RoundedCornerShape(4.dp)
            ) {
                Text(
                    text = badgeText,
                    fontFamily = JetBrainsMonoFontFamily,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = badgeColor,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                )
            }
        }
    }
}

@Composable
private fun TerminalTabContent(
    output: String,
    activeSection: CodeSectionUiModel?
) {
    val scrollState = rememberScrollState()
    val isError = output.contains("exit code 1") || output.contains("Error") || output.contains("error:")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.padding(bottom = 6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(if (isError) Color(0xFFFF5F56) else Color(0xFF27C93F))
            )
            Text(
                text = "${activeSection?.language?.displayName ?: "Terminal"} Console • ${if (isError) "Process Failed (1)" else "Process Finished (0)"}",
                fontFamily = JetBrainsMonoFontFamily,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFA39C93)
            )
        }

        output.lines().forEach { line ->
            val lineColor = when {
                line.startsWith("$ ") -> Color(0xFF06B6D4)
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

@Composable
private fun DebuggerTabContent(
    debugInfo: CodeLanguageEngine.DebugSessionInfo?,
    activeSection: CodeSectionUiModel?
) {
    val variables = debugInfo?.variables ?: emptyMap()
    val hasErrors = debugInfo?.hasErrors == true

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(if (hasErrors) Color(0xFFFF5F56) else Color(0xFF10B981))
                    )
                    Text(
                        text = "Debug Session: ${activeSection?.nativeFileName ?: "main"} (${activeSection?.language?.displayName ?: "Code"})",
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFE6E1DC)
                    )
                }
                Surface(
                    color = (if (hasErrors) Color(0xFFFF5F56) else Color(0xFF10B981)).copy(alpha = 0.2f),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = if (hasErrors) "HALTED ON ERROR" else "PAUSED / READY",
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (hasErrors) Color(0xFFFF5F56) else Color(0xFF10B981),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        }

        item {
            Text(
                text = "VARIABLES INSPECTOR (${variables.size})",
                fontFamily = JetBrainsMonoFontFamily,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFA39C93),
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        if (variables.isEmpty()) {
            item {
                Surface(
                    color = Color(0xFF1D1A18),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "No active variables declared in scope yet. Type assignments like 'val x = 10' or 'name = \"KtScribe\"' to inspect.",
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 11.sp,
                        color = Color(0xFFA39C93),
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }
        } else {
            items(variables.toList(), key = { it.first }) { (varName, varValue) ->
                Surface(
                    color = Color(0xFF1F1C1A),
                    shape = RoundedCornerShape(8.dp),
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
                            Text(
                                text = "v",
                                fontFamily = JetBrainsMonoFontFamily,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFF59E0B)
                            )
                            Text(
                                text = varName,
                                fontFamily = JetBrainsMonoFontFamily,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF06B6D4)
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
    onJumpToLine: (Int) -> Unit
) {
    if (diagnostics.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = Color(0xFF27C93F),
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "No syntax errors or diagnostics detected in active file.",
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
            items(diagnostics) { diag ->
                val isErr = diag.severity == DiagnosticSeverity.ERROR
                Surface(
                    color = if (isErr) Color(0xFF2B1817) else Color(0xFF282319),
                    shape = RoundedCornerShape(8.dp),
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
                                text = if (isErr) "✕ Error" else "▲ Warning",
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
                                color = Color(0xFF06B6D4).copy(alpha = 0.2f),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = diag.quickFixLabel,
                                    fontFamily = JetBrainsMonoFontFamily,
                                    fontSize = 9.sp,
                                     color = Color(0xFF06B6D4),
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Desktop IDE Status Bar along the bottom of the editor window.
 * Shows active language, problems summary, terminal, and debugger buttons.
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
        color = Color(0xFF141210),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF262320)),
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
            // Left: Active file and language
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
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
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                        )
                    }
                    Text(
                        text = activeSection.nativeFileName,
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 10.sp,
                        color = Color(0xFFE6E1DC),
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "•",
                        fontSize = 10.sp,
                        color = Color(0xFF6B6560)
                    )
                    Text(
                        text = activeSection.containerFileName,
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 9.5.sp,
                        color = Color(0xFFA39C93)
                    )
                }
            }

            // Right: Interactive Drawer Tabs (Problems, Terminal, Debugger)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Problems
                Surface(
                    color = if (isDrawerOpen && activeTab == BottomDrawerTab.PROBLEMS) Color(0xFF2B241E) else Color.Transparent,
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
                            color = if (errCount > 0) Color(0xFFFF5F56) else Color(0xFFA39C93)
                        )
                        Text(
                            text = "▲ $warnCount",
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (warnCount > 0) Color(0xFFFFBD2E) else Color(0xFFA39C93)
                        )
                    }
                }

                // Terminal
                Surface(
                    color = if (isDrawerOpen && activeTab == BottomDrawerTab.TERMINAL) Color(0xFF2B241E) else Color.Transparent,
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .clickable { onToggleDrawer(BottomDrawerTab.TERMINAL) }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = ">_ Terminal",
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (isDrawerOpen && activeTab == BottomDrawerTab.TERMINAL) Color(0xFF68D391) else Color(0xFFA39C93)
                        )
                    }
                }

                // Debugger
                Surface(
                    color = if (isDrawerOpen && activeTab == BottomDrawerTab.DEBUGGER) Color(0xFF2B241E) else Color.Transparent,
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .clickable { onToggleDrawer(BottomDrawerTab.DEBUGGER) }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "🪲 Debug",
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (isDrawerOpen && activeTab == BottomDrawerTab.DEBUGGER) Color(0xFFBD93F9) else Color(0xFFA39C93)
                        )
                    }
                }

                // Drawer Expand / Collapse Indicator
                IconButton(
                    onClick = { onToggleDrawer(activeTab) },
                    modifier = Modifier.size(20.dp)
                ) {
                    Icon(
                        imageVector = if (isDrawerOpen) Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowUp,
                        contentDescription = "Toggle IDE Drawer",
                        tint = Color(0xFFA39C93),
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}

