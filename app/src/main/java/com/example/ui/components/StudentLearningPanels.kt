package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.spring
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.codeassist.AndroidProjectToolkit
import com.example.codeassist.AndroidResourceBundle
import com.example.compiler.VmExecutionState
import com.example.learning.AutoGradeResult
import com.example.learning.CodingLesson
import com.example.learning.ConceptQuizQuestion
import com.example.learning.DeclarativeUiPreviewParser
import com.example.learning.ErrorPlainEnglishTranslator
import com.example.learning.PreviewUiNode
import com.example.learning.WordMeaning
import com.example.model.LanguageType
import com.example.ui.theme.JetBrainsMonoFontFamily

@Composable
fun MemoryStackVisualizerPanel(
    vmDebugState: VmExecutionState?,
    vmTrace: List<VmExecutionState>,
    vmTraceIndex: Int,
    fallbackVariables: Map<String, String>,
    fallbackCallStack: List<String>,
    onStartOrResumeTrace: () -> Unit,
    onStepNext: () -> Unit,
    onResetTrace: () -> Unit,
    onJumpToLine: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val activeState = vmTrace.getOrNull(vmTraceIndex) ?: vmDebugState
    val prevState = if (vmTraceIndex > 0) vmTrace.getOrNull(vmTraceIndex - 1) else null

    val frames = activeState?.frameSnapshots?.takeIf { it.isNotEmpty() }
        ?: listOf(
            com.example.compiler.VmFrameSnapshot(
                functionName = fallbackCallStack.firstOrNull()?.substringBefore("()") ?: "main",
                line = activeState?.currentLine ?: 1,
                locals = activeState?.variables?.takeIf { it.isNotEmpty() } ?: fallbackVariables
            )
        )
    val heap = activeState?.heapPreview.orEmpty()
    val operandStack = activeState?.operandStackPreview.orEmpty()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Scrubber Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Surface(
                color = Color(0xFF10B981).copy(alpha = 0.16f),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(0.8.dp, Color(0xFF10B981).copy(alpha = 0.5f)),
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable(onClick = onStartOrResumeTrace)
                    .testTag("memory_trace_run_button")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(14.dp))
                    Text("Capture Trace", fontFamily = JetBrainsMonoFontFamily, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF10B981), maxLines = 1, softWrap = false)
                }
            }

            Surface(
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(0.75.dp, MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable(onClick = onStepNext)
                    .testTag("memory_trace_step_button")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                ) {
                    Icon(Icons.Default.SkipNext, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(14.dp))
                    Text("Next Frame", fontFamily = JetBrainsMonoFontFamily, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, maxLines = 1, softWrap = false)
                }
            }

            Surface(
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(0.75.dp, MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable(onClick = onResetTrace)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(13.dp))
                    Text("Reset", fontFamily = JetBrainsMonoFontFamily, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, softWrap = false)
                }
            }

            if (vmTrace.isNotEmpty()) {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(999.dp)
                ) {
                    Text(
                        text = "Step ${vmTraceIndex.coerceAtLeast(0) + 1}/${vmTrace.size} • Ln ${activeState?.currentLine ?: 1}",
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        maxLines = 1,
                        softWrap = false,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }

        // Stack Frames & Heap Side-by-Side / Stacked Cards
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // CALL STACK COLUMN
            Surface(
                color = MaterialTheme.colorScheme.surfaceContainer,
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(0.8.dp, MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier.weight(1f)
            ) {
                Column(
                    modifier = Modifier.padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Default.Memory, contentDescription = null, tint = Color(0xFFD4883A), modifier = Modifier.size(15.dp))
                        Text(
                            text = "CALL STACK (${frames.size})",
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFFD4883A),
                            maxLines = 1,
                            softWrap = false
                        )
                    }

                    frames.forEachIndexed { idx, frame ->
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceContainerHigh,
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(
                                width = if (idx == 0) 1.dp else 0.6.dp,
                                color = if (idx == 0) MaterialTheme.colorScheme.primary.copy(alpha = 0.7f) else MaterialTheme.colorScheme.outlineVariant
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { onJumpToLine(frame.line) }
                        ) {
                            Column(
                                modifier = Modifier.padding(8.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "${frame.functionName}()",
                                        fontFamily = JetBrainsMonoFontFamily,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Ln ${frame.line}",
                                        fontFamily = JetBrainsMonoFontFamily,
                                        fontSize = 9.5.sp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                if (frame.locals.isEmpty()) {
                                    Text(
                                        text = "(no local variables in frame)",
                                        fontFamily = JetBrainsMonoFontFamily,
                                        fontSize = 9.5.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                } else {
                                    frame.locals.entries.forEach { (varName, valStr) ->
                                        val changed = prevState?.variables?.get(varName) != null && prevState.variables[varName] != valStr
                                        val rowColor by animateColorAsState(
                                            targetValue = if (changed) Color(0xFFD4883A).copy(alpha = 0.22f) else MaterialTheme.colorScheme.surfaceContainerLowest,
                                            animationSpec = spring(),
                                            label = "VarChangeHighlight"
                                        )
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(rowColor)
                                                .padding(horizontal = 6.dp, vertical = 3.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = varName,
                                                fontFamily = JetBrainsMonoFontFamily,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = Color(0xFF9876AA)
                                            )
                                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                                if (valStr.contains("@")) {
                                                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Heap Pointer", tint = Color(0xFF38BDF8), modifier = Modifier.size(11.dp))
                                                }
                                                Text(
                                                    text = valStr,
                                                    fontFamily = JetBrainsMonoFontFamily,
                                                    fontSize = 10.sp,
                                                    fontWeight = if (changed) FontWeight.Bold else FontWeight.Normal,
                                                    color = if (changed) Color(0xFFFFD866) else Color(0xFFA9DC76),
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
                }
            }

            // HEAP & OPERAND STACK COLUMN
            Surface(
                color = MaterialTheme.colorScheme.surfaceContainer,
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(0.8.dp, MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier.weight(1f)
            ) {
                Column(
                    modifier = Modifier.padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "HEAP OBJECTS (${heap.size})",
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF38BDF8),
                        maxLines = 1,
                        softWrap = false
                    )
                    if (heap.isEmpty()) {
                        Text(
                            text = "Instantiate a class/data class or list to inspect live heap allocations.",
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 9.5.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        heap.entries.forEach { (addr, objDesc) ->
                            Surface(
                                color = MaterialTheme.colorScheme.surfaceContainerLowest,
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(0.75.dp, Color(0xFF38BDF8).copy(alpha = 0.45f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(7.dp)) {
                                    Text(
                                        text = addr,
                                        fontFamily = JetBrainsMonoFontFamily,
                                        fontSize = 9.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF38BDF8)
                                    )
                                    Text(
                                        text = objDesc,
                                        fontFamily = JetBrainsMonoFontFamily,
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                    Text(
                        text = "VM OPERAND STACK",
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (operandStack.isEmpty()) {
                        Text(
                            text = "[empty]",
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 9.5.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        Row(
                            modifier = Modifier.horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            operandStack.forEach { slot ->
                                Surface(
                                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = slot,
                                        fontFamily = JetBrainsMonoFontFamily,
                                        fontSize = 9.5.sp,
                                        color = MaterialTheme.colorScheme.onSurface,
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
}

@Composable
fun DeclarativeUiPreviewPanel(
    code: String,
    language: LanguageType,
    onInsertTemplateSnippet: (String) -> Unit,
    resourceBundle: AndroidResourceBundle? = null,
    modifier: Modifier = Modifier
) {
    val parsedNodes = remember(code, resourceBundle) {
        DeclarativeUiPreviewParser.parse(code, resourceBundle)
    }
    val nodeTotal = remember(parsedNodes) { parsedNodes.sumOf { it.nodeCount() } }
    var tapCount by remember { mutableIntStateOf(0) }
    var switchState by remember { mutableStateOf(true) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = RoundedCornerShape(999.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp)
                ) {
                    Icon(Icons.Default.PhoneAndroid, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.size(12.dp))
                    Text(
                        text = when {
                            language == LanguageType.DART -> "Flutter AST Live ($nodeTotal nodes)"
                            language == LanguageType.XML || code.trimStart().startsWith("<") -> "Android XML Live ($nodeTotal nodes)"
                            else -> "Compose Live ($nodeTotal nodes)"
                        },
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }

            if (tapCount > 0) {
                Surface(
                    color = Color(0xFF10B981).copy(alpha = 0.18f),
                    shape = RoundedCornerShape(999.dp)
                ) {
                    Text(
                        text = "Interactive clicks: $tapCount",
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 9.5.sp,
                        color = Color(0xFF10B981),
                        maxLines = 1,
                        softWrap = false,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Surface(
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(0.75.dp, MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable {
                        val snippet = when {
                            language == LanguageType.DART ->
                                "\nWidget buildSample() {\n  return Scaffold(\n    appBar: AppBar(title: Text('Flutter Live')),\n    body: Column(\n      children: [\n        Text('Hello Student UI!'),\n        ElevatedButton(child: Text('Tap Me')),\n      ],\n    ),\n  );\n}\n"
                            language == LanguageType.XML || code.trimStart().startsWith("<") ->
                                "\n" + AndroidProjectToolkit.defaultActivityMainXml() + "\n"
                            else ->
                                "\n@Composable\nfun PreviewDemo() {\n    Card {\n        Column {\n            Text(\"Compose Student Card\")\n            Button(onClick = {}) {\n                Text(\"Click Me\")\n            }\n        }\n    }\n}\n"
                        }
                        onInsertTemplateSnippet(snippet)
                    }
                    .testTag("ui_preview_insert_sample_button")
            ) {
                Text(
                    text = if (language == LanguageType.XML) "+ XML Layout" else "+ Sample UI",
                    fontFamily = JetBrainsMonoFontFamily,
                    fontSize = 9.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 1,
                    softWrap = false,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }

        // Mock Device Viewport Frame
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                color = MaterialTheme.colorScheme.surfaceContainer,
                shape = RoundedCornerShape(20.dp),
                tonalElevation = 4.dp,
                shadowElevation = 6.dp,
                border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.outline),
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 380.dp)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (parsedNodes.isEmpty()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 18.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Default.Code, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(26.dp))
                            Text(
                                text = "Write Compose (`Card`, `Column`, `Text`, `Button`), Flutter (`Scaffold`, `ElevatedButton`), or Android XML (`<LinearLayout>`, `<TextView>`, `<Button>`) to preview live.",
                                fontFamily = JetBrainsMonoFontFamily,
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        parsedNodes.forEach { node ->
                            RenderPreviewUiNode(
                                node = node,
                                switchChecked = switchState,
                                onToggleSwitch = { switchState = it },
                                onButtonTap = { tapCount++ }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RenderPreviewUiNode(
    node: PreviewUiNode,
    switchChecked: Boolean,
    onToggleSwitch: (Boolean) -> Unit,
    onButtonTap: () -> Unit
) {
    when (node) {
        is PreviewUiNode.ScaffoldNode -> {
            Surface(
                color = MaterialTheme.colorScheme.surfaceContainerLowest,
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(0.75.dp, MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    if (!node.title.isNullOrBlank()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.primaryContainer)
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = node.title,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                    Column(
                        modifier = Modifier.padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        node.body.forEach { child ->
                            RenderPreviewUiNode(child, switchChecked, onToggleSwitch, onButtonTap)
                        }
                    }
                }
            }
        }
        is PreviewUiNode.ColumnNode -> {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                node.children.forEach { child ->
                    RenderPreviewUiNode(child, switchChecked, onToggleSwitch, onButtonTap)
                }
            }
        }
        is PreviewUiNode.RowNode -> {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                node.children.forEach { child ->
                    RenderPreviewUiNode(child, switchChecked, onToggleSwitch, onButtonTap)
                }
            }
        }
        is PreviewUiNode.BoxNode -> {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                    .padding(8.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    node.children.forEach { child ->
                        RenderPreviewUiNode(child, switchChecked, onToggleSwitch, onButtonTap)
                    }
                }
            }
        }
        is PreviewUiNode.CardNode -> {
            ElevatedCard(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    node.children.forEach { child ->
                        RenderPreviewUiNode(child, switchChecked, onToggleSwitch, onButtonTap)
                    }
                }
            }
        }
        is PreviewUiNode.TextNode -> {
            Text(
                text = node.text,
                style = if (node.isTitle) MaterialTheme.typography.titleSmall else MaterialTheme.typography.bodyMedium,
                fontWeight = if (node.isBold || node.isTitle) FontWeight.Bold else FontWeight.Normal,
                color = node.colorArgb?.let { Color(it) } ?: MaterialTheme.colorScheme.onSurface
            )
        }
        is PreviewUiNode.ButtonNode -> {
            Button(
                onClick = onButtonTap,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = node.backgroundArgb?.let { Color(it) } ?: MaterialTheme.colorScheme.primary
                )
            ) {
                Text(node.label, fontWeight = FontWeight.Bold)
            }
        }
        is PreviewUiNode.SwitchNode -> {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(node.label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface)
                Switch(checked = switchChecked, onCheckedChange = onToggleSwitch)
            }
        }
        is PreviewUiNode.TextFieldNode -> {
            Surface(
                color = MaterialTheme.colorScheme.surfaceContainerLowest,
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = node.value.ifBlank { node.hint },
                    style = MaterialTheme.typography.bodySmall,
                    color = if (node.value.isBlank()) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 9.dp)
                )
            }
        }
        is PreviewUiNode.ImagePlaceholderNode -> {
            Surface(
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(0.75.dp, MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(10.dp)
                ) {
                    Icon(Icons.Default.Image, contentDescription = node.label, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                    Text(node.label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        is PreviewUiNode.SpacerNode -> Spacer(modifier = Modifier.height(node.heightDp.dp))
        PreviewUiNode.DividerNode -> HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ActiveLessonBannerCard(
    lesson: CodingLesson,
    gradeResult: AutoGradeResult?,
    isGuidedBeginnerMode: Boolean = true,
    onToggleGuidedMode: () -> Unit = {},
    onRunAndGrade: () -> Unit,
    onOpenQuiz: () -> Unit,
    onLoadSolution: () -> Unit,
    onCloseLessonMode: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showHint by remember(lesson.id) { mutableStateOf(false) }
    var showLineByLine by remember(lesson.id) { mutableStateOf(false) }
    var selectedWord by remember(lesson.id) { mutableStateOf<WordMeaning?>(null) }

    val mergedDictionary = remember(lesson.id) {
        (lesson.wordDictionary + ErrorPlainEnglishTranslator.globalGlossary).distinctBy { it.term }
    }

    ElevatedCard(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp),
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, lesson.track.accentColor.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
            .testTag("active_lesson_banner")
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        color = lesson.track.accentColor.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(999.dp)
                    ) {
                        Text(
                            text = "${lesson.track.shortBadge} • ${lesson.difficulty}",
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = lesson.track.accentColor,
                            maxLines = 1,
                            softWrap = false,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                    Text(
                        text = lesson.title,
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        color = if (isGuidedBeginnerMode) Color(0xFF10B981).copy(alpha = 0.18f) else MaterialTheme.colorScheme.surfaceContainerHigh,
                        shape = RoundedCornerShape(999.dp),
                        border = BorderStroke(
                            0.75.dp,
                            if (isGuidedBeginnerMode) Color(0xFF10B981) else MaterialTheme.colorScheme.outlineVariant
                        ),
                        modifier = Modifier
                            .clip(RoundedCornerShape(999.dp))
                            .clickable(onClick = onToggleGuidedMode)
                            .testTag("lesson_guided_mode_toggle")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.School,
                                contentDescription = "Toggle Beginner Guided Mode",
                                tint = if (isGuidedBeginnerMode) Color(0xFF10B981) else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                text = if (isGuidedBeginnerMode) "Guided: ON" else "Pro Mode",
                                fontFamily = JetBrainsMonoFontFamily,
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isGuidedBeginnerMode) Color(0xFF10B981) else MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }

                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Exit Guided Lesson",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .size(18.dp)
                            .clickable(onClick = onCloseLessonMode)
                    )
                }
            }

            Text(
                text = lesson.instructions,
                fontSize = 11.5.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 16.sp
            )

            // BEGINNER-FRIENDLY GUIDED MODE: Real-Life Story + Tap-a-Word Dictionary + Line-by-Line English
            AnimatedVisibility(visible = isGuidedBeginnerMode) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    // 1. Real-Life Analogy Story Card
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.42f),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(0.75.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("guided_real_life_story_card")
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(Icons.Default.AutoStories, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(14.dp))
                                Text(
                                    text = "Real-Life Story: ${lesson.realLifeAnalogy.headline}",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Text(
                                text = lesson.realLifeAnalogy.story,
                                fontSize = 11.5.sp,
                                lineHeight = 16.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Code <-> Life: ${lesson.realLifeAnalogy.mappingSummary}",
                                fontFamily = JetBrainsMonoFontFamily,
                                fontSize = 9.5.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // 2. Tap-a-Word Dictionary Strip
                    if (mergedDictionary.isNotEmpty()) {
                        Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                Icon(Icons.Default.MenuBook, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(13.dp))
                                Text(
                                    text = "Tap any coding word to see its everyday meaning:",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF38BDF8)
                                )
                            }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                mergedDictionary.forEach { word ->
                                    val isSelected = selectedWord?.term == word.term
                                    Surface(
                                        color = if (isSelected) Color(0xFF38BDF8).copy(alpha = 0.22f) else MaterialTheme.colorScheme.surfaceContainerHigh,
                                        shape = RoundedCornerShape(999.dp),
                                        border = BorderStroke(
                                            0.75.dp,
                                            if (isSelected) Color(0xFF38BDF8) else MaterialTheme.colorScheme.outlineVariant
                                        ),
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(999.dp))
                                            .clickable {
                                                selectedWord = if (isSelected) null else word
                                            }
                                    ) {
                                        Text(
                                            text = word.term,
                                            fontFamily = JetBrainsMonoFontFamily,
                                            fontSize = 10.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isSelected) Color(0xFF38BDF8) else MaterialTheme.colorScheme.onSurface,
                                            maxLines = 1,
                                            softWrap = false,
                                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            }

                            AnimatedVisibility(visible = selectedWord != null) {
                                selectedWord?.let { w ->
                                    Surface(
                                        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.55f),
                                        shape = RoundedCornerShape(10.dp),
                                        border = BorderStroke(0.75.dp, Color(0xFF38BDF8).copy(alpha = 0.45f)),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("word_dictionary_popup")
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(9.dp),
                                            verticalArrangement = Arrangement.spacedBy(3.dp)
                                        ) {
                                            Text(
                                                text = "${w.term}  •  ${w.partOfSpeech}",
                                                fontFamily = JetBrainsMonoFontFamily,
                                                fontSize = 10.5.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF38BDF8)
                                            )
                                            Text(
                                                text = w.simpleMeaning,
                                                fontSize = 11.sp,
                                                lineHeight = 15.sp,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = "Example: ${w.everydayExample}",
                                                fontSize = 10.5.sp,
                                                lineHeight = 15.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // 3. Collapsible Line-by-Line English Guide
                    if (lesson.lineWalkthrough.isNotEmpty()) {
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceContainerHigh,
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(0.75.dp, MaterialTheme.colorScheme.outlineVariant),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { showLineByLine = !showLineByLine },
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Line-by-Line Plain English (${lesson.lineWalkthrough.size} steps)",
                                        fontFamily = JetBrainsMonoFontFamily,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Icon(
                                        imageVector = if (showLineByLine) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                        contentDescription = "Toggle Line Walkthrough",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                AnimatedVisibility(visible = showLineByLine) {
                                    Column(
                                        modifier = Modifier.padding(top = 6.dp),
                                        verticalArrangement = Arrangement.spacedBy(5.dp)
                                    ) {
                                        lesson.lineWalkthrough.forEach { step ->
                                            Surface(
                                                color = MaterialTheme.colorScheme.surfaceContainerLowest,
                                                shape = RoundedCornerShape(8.dp),
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Column(
                                                    modifier = Modifier.padding(7.dp),
                                                    verticalArrangement = Arrangement.spacedBy(2.dp)
                                                ) {
                                                    Text(
                                                        text = "Ln ${step.lineNumber}: ${step.codeLine}",
                                                        fontFamily = JetBrainsMonoFontFamily,
                                                        fontSize = 9.5.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = MaterialTheme.colorScheme.primary
                                                    )
                                                    Text(
                                                        text = step.plainEnglish,
                                                        fontSize = 11.sp,
                                                        lineHeight = 15.sp,
                                                        color = MaterialTheme.colorScheme.onSurface
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

            AnimatedVisibility(visible = showHint) {
                Surface(
                    color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.55f),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Hint: ${lesson.hint}",
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 10.5.sp,
                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Button(
                    onClick = onRunAndGrade,
                    shape = RoundedCornerShape(999.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981), contentColor = Color(0xFF0A1410)),
                    modifier = Modifier.testTag("lesson_run_grade_button")
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(5.dp))
                    Text("Check Solution (+${lesson.xpReward} XP)", fontFamily = JetBrainsMonoFontFamily, fontSize = 10.5.sp, fontWeight = FontWeight.Bold, maxLines = 1, softWrap = false)
                }

                Surface(
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    shape = RoundedCornerShape(999.dp),
                    border = BorderStroke(0.75.dp, MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .clickable { showHint = !showHint }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp)
                    ) {
                        Icon(Icons.Default.Lightbulb, contentDescription = null, tint = Color(0xFFFFD866), modifier = Modifier.size(13.dp))
                        Text(if (showHint) "Hide Hint" else "AI Hint", fontFamily = JetBrainsMonoFontFamily, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface)
                    }
                }

                Surface(
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    shape = RoundedCornerShape(999.dp),
                    border = BorderStroke(0.75.dp, MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .clickable(onClick = onOpenQuiz)
                        .testTag("lesson_open_quiz_button")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp)
                    ) {
                        Icon(Icons.Default.Quiz, contentDescription = null, tint = Color(0xFFC084FC), modifier = Modifier.size(13.dp))
                        Text("Quiz Me", fontFamily = JetBrainsMonoFontFamily, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface)
                    }
                }

                Surface(
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    shape = RoundedCornerShape(999.dp),
                    border = BorderStroke(0.75.dp, MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .clickable(onClick = onLoadSolution)
                ) {
                    Text(
                        text = "Load Solution",
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp)
                    )
                }
            }

            if (gradeResult != null && gradeResult.lessonId == lesson.id) {
                Surface(
                    color = if (gradeResult.isAllPassed) Color(0xFF10B981).copy(alpha = 0.14f) else Color(0xFFFF5F56).copy(alpha = 0.14f),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(0.75.dp, if (gradeResult.isAllPassed) Color(0xFF10B981) else Color(0xFFFF5F56)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = gradeResult.summaryBanner,
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (gradeResult.isAllPassed) Color(0xFF81C784) else Color(0xFFFF8A80)
                        )
                        gradeResult.testResults.forEach { tc ->
                            Text(
                                text = "${if (tc.passed) "✓" else "✕"} ${tc.label} — ${tc.detail}",
                                fontFamily = JetBrainsMonoFontFamily,
                                fontSize = 9.5.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StudentQuizModalDialog(
    questions: List<ConceptQuizQuestion>,
    onAnswerSubmitted: (ConceptQuizQuestion, Int) -> Unit,
    onDismiss: () -> Unit
) {
    if (questions.isEmpty()) return
    var currentIdx by remember { mutableIntStateOf(0) }
    var selectedOption by remember(currentIdx) { mutableIntStateOf(-1) }
    var submitted by remember(currentIdx) { mutableStateOf(false) }

    val q = questions[currentIdx.coerceIn(0, questions.lastIndex)]
    val progress = (currentIdx + 1).toFloat() / questions.size.toFloat()

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        title = {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(Icons.Default.Quiz, contentDescription = null, tint = Color(0xFFC084FC))
                        Text(
                            text = "Concept Mastery Quiz",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Surface(color = q.track.accentColor.copy(alpha = 0.2f), shape = RoundedCornerShape(999.dp)) {
                        Text(
                            text = "${currentIdx + 1}/${questions.size}",
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = q.track.accentColor,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 420.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(color = MaterialTheme.colorScheme.surfaceContainer, shape = RoundedCornerShape(8.dp)) {
                    Text(
                        text = q.conceptTag,
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                Text(
                    text = q.question,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Surface(
                    color = MaterialTheme.colorScheme.surfaceContainerLowest,
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(0.75.dp, MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = q.codeSnippet,
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 11.sp,
                        color = Color(0xFFD8D0C8),
                        modifier = Modifier.padding(10.dp)
                    )
                }

                q.options.forEachIndexed { idx, opt ->
                    val isSelected = selectedOption == idx
                    val isCorrectOption = idx == q.correctIndex
                    val borderColor = when {
                        submitted && isCorrectOption -> Color(0xFF10B981)
                        submitted && isSelected && !isCorrectOption -> Color(0xFFFF5F56)
                        isSelected -> MaterialTheme.colorScheme.primary
                        else -> MaterialTheme.colorScheme.outlineVariant
                    }
                    Surface(
                        color = when {
                            submitted && isCorrectOption -> Color(0xFF10B981).copy(alpha = 0.16f)
                            submitted && isSelected -> Color(0xFFFF5F56).copy(alpha = 0.16f)
                            isSelected -> MaterialTheme.colorScheme.primaryContainer
                            else -> MaterialTheme.colorScheme.surfaceContainer
                        },
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, borderColor),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .clickable(enabled = !submitted) { selectedOption = idx }
                            .testTag("quiz_option_$idx")
                    ) {
                        Text(
                            text = "${('A' + idx)}. $opt",
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 9.dp)
                        )
                    }
                }

                if (submitted) {
                    val correct = selectedOption == q.correctIndex
                    Surface(
                        color = if (correct) Color(0xFF10B981).copy(alpha = 0.16f) else Color(0xFFFFB86C).copy(alpha = 0.16f),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = (if (correct) "✓ Correct (+35 XP)! " else "Review: ") + q.explanation,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            if (!submitted) {
                Button(
                    onClick = {
                        if (selectedOption >= 0) {
                            submitted = true
                            onAnswerSubmitted(q, selectedOption)
                        }
                    },
                    enabled = selectedOption >= 0,
                    modifier = Modifier.testTag("quiz_submit_button")
                ) {
                    Text("Check Answer", fontFamily = JetBrainsMonoFontFamily, fontWeight = FontWeight.Bold)
                }
            } else if (currentIdx < questions.lastIndex) {
                Button(
                    onClick = { currentIdx++ },
                    modifier = Modifier.testTag("quiz_next_button")
                ) {
                    Text("Next Question", fontFamily = JetBrainsMonoFontFamily, fontWeight = FontWeight.Bold)
                }
            } else {
                Button(onClick = onDismiss) {
                    Text("Finish", fontFamily = JetBrainsMonoFontFamily, fontWeight = FontWeight.Bold)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", fontFamily = JetBrainsMonoFontFamily)
            }
        }
    )
}
