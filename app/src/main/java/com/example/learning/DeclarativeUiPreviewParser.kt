package com.example.learning

sealed interface PreviewUiNode {
    data class ColumnNode(val children: List<PreviewUiNode>) : PreviewUiNode
    data class RowNode(val children: List<PreviewUiNode>) : PreviewUiNode
    data class BoxNode(val children: List<PreviewUiNode>) : PreviewUiNode
    data class CardNode(val children: List<PreviewUiNode>) : PreviewUiNode
    data class ScaffoldNode(val title: String?, val body: List<PreviewUiNode>) : PreviewUiNode
    data class TextNode(val text: String, val isBold: Boolean = false, val isTitle: Boolean = false) : PreviewUiNode
    data class ButtonNode(val label: String) : PreviewUiNode
    data class SwitchNode(val label: String, val initialChecked: Boolean = false) : PreviewUiNode
    data class SpacerNode(val heightDp: Int = 8) : PreviewUiNode
    data object DividerNode : PreviewUiNode

    fun nodeCount(): Int = when (this) {
        is ColumnNode -> 1 + children.sumOf { it.nodeCount() }
        is RowNode -> 1 + children.sumOf { it.nodeCount() }
        is BoxNode -> 1 + children.sumOf { it.nodeCount() }
        is CardNode -> 1 + children.sumOf { it.nodeCount() }
        is ScaffoldNode -> 1 + body.sumOf { it.nodeCount() }
        else -> 1
    }
}

/**
 * Clean-room AST parser converting Jetpack Compose & Flutter/Dart declarative snippets
 * into safe live-rendered Material 3 preview trees without needing APK rebuilds.
 */
object DeclarativeUiPreviewParser {

    private data class ContainerFrame(
        val kind: String,
        var title: String?,
        val openTotalDepth: Int
    )

    fun parse(code: String): List<PreviewUiNode> {
        if (code.isBlank()) return emptyList()
        val lines = code.replace("\r\n", "\n").lines()
        val rootStack = ArrayDeque<MutableList<PreviewUiNode>>()
        val pendingFrames = ArrayDeque<ContainerFrame>()

        val rootNodes = mutableListOf<PreviewUiNode>()
        rootStack.addLast(rootNodes)

        var parenDepth = 0
        var braceDepth = 0

        fun closeTopContainer() {
            if (pendingFrames.isEmpty() || rootStack.size <= 1) return
            val children = rootStack.removeLast()
            val frame = pendingFrames.removeLast()
            val node: PreviewUiNode = when (frame.kind) {
                "Column" -> PreviewUiNode.ColumnNode(children)
                "Row" -> PreviewUiNode.RowNode(children)
                "Box", "Center", "Container" -> PreviewUiNode.BoxNode(children)
                "Card", "ElevatedCard" -> PreviewUiNode.CardNode(children)
                "Scaffold" -> PreviewUiNode.ScaffoldNode(title = frame.title, body = children)
                else -> PreviewUiNode.ColumnNode(children)
            }
            rootStack.last().add(node)
        }

        val containerStartRegex = Regex("""\b(Scaffold|Column|Row|Box|Card|ElevatedCard|Center|Container)\s*[\(\{]""")
        val textRegex = Regex("""\bText\s*\(\s*["']([^"']*)["']""")
        val buttonLabelRegex = Regex("""\b(?:Button|ElevatedButton|OutlinedButton|TextButton)\b[^\n]*""")
        val appBarTitleRegex = Regex("""AppBar\s*\([^\)]*Text\s*\(\s*["']([^"']+)["']""")
        val switchRegex = Regex("""\bSwitch\s*\(""")
        val dividerRegex = Regex("""\b(?:HorizontalDivider|Divider)\s*\(""")
        val spacerRegex = Regex("""\bSpacer\b""")

        for (i in lines.indices) {
            val line = lines[i].trim()
            if (line.isEmpty() || line.startsWith("//") || line.startsWith("#")) continue

            val depthBeforeLine = braceDepth + parenDepth
            val deltaBrace = countOutsideStrings(line, '{') - countOutsideStrings(line, '}')
            val deltaParen = countOutsideStrings(line, '(') - countOutsideStrings(line, ')')

            // Capture Flutter AppBar(title: Text('...')) for enclosing Scaffold
            val appBarMatch = appBarTitleRegex.find(line)
            if (appBarMatch != null) {
                val scaffoldFrame = pendingFrames.lastOrNull { it.kind == "Scaffold" }
                if (scaffoldFrame != null) {
                    scaffoldFrame.title = appBarMatch.groupValues[1]
                }
            }

            // Detect container opening lines
            val containerMatch = containerStartRegex.find(line)
            val openedContainer = if (containerMatch != null && !line.contains("AppBar")) {
                val kind = containerMatch.groupValues[1]
                val opensScope = line.endsWith("{") || line.endsWith("(") || (deltaBrace + deltaParen > 0)
                if (opensScope) {
                    pendingFrames.addLast(
                        ContainerFrame(
                            kind = kind,
                            title = if (kind == "Scaffold") "Preview App" else null,
                            openTotalDepth = depthBeforeLine
                        )
                    )
                    rootStack.addLast(mutableListOf())
                    true
                } else {
                    false
                }
            } else {
                false
            }

            if (!openedContainer) {
                if (buttonLabelRegex.containsMatchIn(line)) {
                    val inlineLabel = textRegex.find(line)?.groupValues?.getOrNull(1)
                    val nextLineLabel = if (inlineLabel == null && i + 1 < lines.size) {
                        textRegex.find(lines[i + 1])?.groupValues?.getOrNull(1)
                    } else null
                    val finalLabel = inlineLabel ?: nextLineLabel ?: "Action"
                    rootStack.last().add(PreviewUiNode.ButtonNode(finalLabel))
                } else {
                    if (appBarMatch == null) {
                        val textMatch = textRegex.find(line)
                        if (textMatch != null) {
                            val prevTrim = lines.getOrNull(i - 1)?.trim().orEmpty()
                            val isButtonChildLine = buttonLabelRegex.containsMatchIn(prevTrim) && prevTrim.endsWith("{")
                            if (!isButtonChildLine) {
                                val txt = textMatch.groupValues[1]
                                val isBold = line.contains("Bold") || line.contains("title", ignoreCase = true)
                                val isTitle = line.contains("headline", ignoreCase = true) || line.contains("title", ignoreCase = true)
                                rootStack.last().add(PreviewUiNode.TextNode(txt, isBold = isBold, isTitle = isTitle))
                            }
                        }
                    }

                    if (switchRegex.containsMatchIn(line)) {
                        rootStack.last().add(
                            PreviewUiNode.SwitchNode(
                                label = "Toggle State",
                                initialChecked = line.contains("true")
                            )
                        )
                    } else if (dividerRegex.containsMatchIn(line)) {
                        rootStack.last().add(PreviewUiNode.DividerNode)
                    } else if (spacerRegex.containsMatchIn(line)) {
                        rootStack.last().add(PreviewUiNode.SpacerNode(8))
                    }
                }
            }

            braceDepth += deltaBrace
            parenDepth += deltaParen
            val depthAfterLine = braceDepth + parenDepth

            while (pendingFrames.isNotEmpty() && depthAfterLine <= pendingFrames.last().openTotalDepth) {
                closeTopContainer()
            }
        }

        while (rootStack.size > 1) {
            closeTopContainer()
        }

        return rootNodes
    }

    private fun countOutsideStrings(line: String, target: Char): Int {
        var count = 0
        var inDouble = false
        var inSingle = false
        var escaped = false
        for (ch in line) {
            if (escaped) {
                escaped = false
                continue
            }
            if (ch == '\\') {
                escaped = true
                continue
            }
            if (!inSingle && ch == '"') {
                inDouble = !inDouble
                continue
            }
            if (!inDouble && ch == '\'') {
                inSingle = !inSingle
                continue
            }
            if (!inDouble && !inSingle && ch == target) {
                count++
            }
        }
        return count
    }
}
