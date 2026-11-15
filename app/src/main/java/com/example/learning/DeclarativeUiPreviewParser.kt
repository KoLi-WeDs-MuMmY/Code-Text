package com.example.learning

import com.example.codeassist.AndroidResourceBundle

sealed interface PreviewUiNode {
    data class ColumnNode(val children: List<PreviewUiNode>) : PreviewUiNode
    data class RowNode(val children: List<PreviewUiNode>) : PreviewUiNode
    data class BoxNode(val children: List<PreviewUiNode>) : PreviewUiNode
    data class CardNode(val children: List<PreviewUiNode>) : PreviewUiNode
    data class ScaffoldNode(val title: String?, val body: List<PreviewUiNode>) : PreviewUiNode
    data class TextNode(
        val text: String,
        val isBold: Boolean = false,
        val isTitle: Boolean = false,
        val colorArgb: Long? = null
    ) : PreviewUiNode
    data class ButtonNode(val label: String, val backgroundArgb: Long? = null) : PreviewUiNode
    data class SwitchNode(val label: String, val initialChecked: Boolean = false) : PreviewUiNode
    data class TextFieldNode(val hint: String, val value: String = "") : PreviewUiNode
    data class ImagePlaceholderNode(val label: String) : PreviewUiNode
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
 * Clean-room AST parser converting Jetpack Compose, Flutter/Dart, and Classic Android Layout XML
 * into safe live-rendered Material 3 preview trees without needing APK rebuilds.
 */
object DeclarativeUiPreviewParser {

    private data class ContainerFrame(
        val kind: String,
        var title: String?,
        val openTotalDepth: Int
    )

    fun parse(
        code: String,
        resourceBundle: AndroidResourceBundle? = null
    ): List<PreviewUiNode> {
        if (code.isBlank()) return emptyList()
        val trimmedStart = code.trimStart()
        if (trimmedStart.startsWith("<")) {
            return parseAndroidXmlLayout(code, resourceBundle)
        }
        return parseComposeOrFlutter(code)
    }

    private fun parseAndroidXmlLayout(
        xml: String,
        resourceBundle: AndroidResourceBundle?
    ): List<PreviewUiNode> {
        val stack = ArrayDeque<Pair<String, MutableList<PreviewUiNode>>>()
        val roots = mutableListOf<PreviewUiNode>()
        stack.addLast("Root" to roots)

        // Tokenize into XML tags `<...>` even across multi-line attributes
        val tagRegex = Regex("""<([^>]+)>""")
        val attrRegex = Regex("""(?:android:|app:)?([A-Za-z0-9_]+)\s*=\s*["']([^"']*)["']""")

        tagRegex.findAll(xml).forEach { match ->
            val rawTag = match.groupValues[1].trim()
            if (rawTag.startsWith("?") || rawTag.startsWith("!")) return@forEach

            if (rawTag.startsWith("/")) {
                if (stack.size > 1) {
                    val (kind, children) = stack.removeLast()
                    val node = buildXmlContainerNode(kind, children)
                    stack.last().second.add(node)
                }
                return@forEach
            }

            val isSelfClosing = rawTag.endsWith("/")
            val cleanBody = if (isSelfClosing) rawTag.dropLast(1).trim() else rawTag
            val tagName = cleanBody.split(Regex("\\s+")).firstOrNull()?.substringAfterLast('.') ?: return@forEach

            val attrs = mutableMapOf<String, String>()
            attrRegex.findAll(cleanBody).forEach { attrMatch ->
                attrs[attrMatch.groupValues[1]] = attrMatch.groupValues[2]
            }

            fun resolveText(raw: String?): String {
                if (raw.isNullOrBlank()) return ""
                return resourceBundle?.resolveString(raw) ?: raw
            }

            fun resolveColor(raw: String?): Long? {
                if (raw.isNullOrBlank()) return null
                return resourceBundle?.resolveColorHex(raw)
            }

            val isContainer = tagName in setOf(
                "LinearLayout", "ConstraintLayout", "RelativeLayout", "FrameLayout",
                "ScrollView", "NestedScrollView", "CardView", "MaterialCardView", "CoordinatorLayout"
            )

            if (isContainer) {
                val containerKind = when (tagName) {
                    "CardView", "MaterialCardView" -> "Card"
                    "LinearLayout" -> if (attrs["orientation"]?.equals("horizontal", ignoreCase = true) == true) "Row" else "Column"
                    "FrameLayout" -> "Box"
                    else -> "Column"
                }
                if (isSelfClosing) {
                    stack.last().second.add(buildXmlContainerNode(containerKind, emptyList()))
                } else {
                    stack.addLast(containerKind to mutableListOf())
                }
                return@forEach
            }

            val leafNode: PreviewUiNode? = when {
                tagName.contains("TextView") -> {
                    val txt = resolveText(attrs["text"]).ifBlank { "TextView" }
                    val isBold = attrs["textStyle"]?.contains("bold", ignoreCase = true) == true
                    val colorArgb = resolveColor(attrs["textColor"])
                    PreviewUiNode.TextNode(txt, isBold = isBold, isTitle = isBold, colorArgb = colorArgb)
                }
                tagName.contains("EditText") || tagName.contains("TextInputEditText") -> {
                    val hint = resolveText(attrs["hint"]).ifBlank { "Enter input..." }
                    val value = resolveText(attrs["text"])
                    PreviewUiNode.TextFieldNode(hint = hint, value = value)
                }
                tagName.contains("Switch") || tagName.contains("CheckBox") -> {
                    val label = resolveText(attrs["text"]).ifBlank { tagName }
                    val checked = attrs["checked"].equals("true", ignoreCase = true)
                    PreviewUiNode.SwitchNode(label = label, initialChecked = checked)
                }
                tagName.contains("Button") -> {
                    val label = resolveText(attrs["text"]).ifBlank { "Button" }
                    val bg = resolveColor(attrs["backgroundTint"] ?: attrs["background"])
                    PreviewUiNode.ButtonNode(label = label, backgroundArgb = bg)
                }
                tagName.contains("ImageView") -> {
                    val desc = resolveText(attrs["contentDescription"] ?: attrs["src"]).ifBlank { "ImageView Preview" }
                    PreviewUiNode.ImagePlaceholderNode(desc)
                }
                tagName == "View" -> PreviewUiNode.DividerNode
                tagName == "Space" -> PreviewUiNode.SpacerNode(10)
                else -> null
            }

            if (leafNode != null) {
                stack.last().second.add(leafNode)
            }
        }

        while (stack.size > 1) {
            val (kind, children) = stack.removeLast()
            stack.last().second.add(buildXmlContainerNode(kind, children))
        }
        return roots
    }

    private fun buildXmlContainerNode(kind: String, children: List<PreviewUiNode>): PreviewUiNode = when (kind) {
        "Row" -> PreviewUiNode.RowNode(children)
        "Box" -> PreviewUiNode.BoxNode(children)
        "Card" -> PreviewUiNode.CardNode(children)
        else -> PreviewUiNode.ColumnNode(children)
    }

    private fun parseComposeOrFlutter(code: String): List<PreviewUiNode> {
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

            val appBarMatch = appBarTitleRegex.find(line)
            if (appBarMatch != null) {
                val scaffoldFrame = pendingFrames.lastOrNull { it.kind == "Scaffold" }
                if (scaffoldFrame != null) {
                    scaffoldFrame.title = appBarMatch.groupValues[1]
                }
            }

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
