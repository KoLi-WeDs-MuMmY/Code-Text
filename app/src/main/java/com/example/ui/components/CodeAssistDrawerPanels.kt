package com.example.ui.components

import android.widget.Toast
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.codeassist.AndroidProjectToolkit
import com.example.codeassist.AndroidResourceBundle
import com.example.codeassist.BuildPipelineTask
import com.example.codeassist.GitFileStatusItem
import com.example.codeassist.LocalGitCommit
import com.example.codeassist.ManifestSummary
import com.example.model.CodeSectionUiModel
import com.example.ui.theme.JetBrainsMonoFontFamily

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AndroidStudioDrawerTabContent(
    sections: List<CodeSectionUiModel>,
    manifestSummary: ManifestSummary,
    resourceBundle: AndroidResourceBundle,
    buildTasks: List<BuildPipelineTask>,
    onCreateStarterFile: (fileName: String, content: String) -> Unit,
    onAddManifestPermission: (String) -> Unit,
    onInsertSnippet: (String) -> Unit,
    onRunBuildPipeline: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedSubTab by remember { mutableIntStateOf(0) } // 0=Manifest, 1=Resources, 2=Gradle & Build
    var gradleSearch by remember { mutableStateOf("") }
    val clipboard = LocalClipboardManager.current
    val context = LocalContext.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Sub-tab switcher pill row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            listOf(
                Triple(0, "Manifest & Play", Icons.Default.Security),
                Triple(1, "Resources & Swatches", Icons.Default.Palette),
                Triple(2, "Gradle & Build Tree", Icons.Default.Build)
            ).forEach { (idx, label, icon) ->
                val isSelected = selectedSubTab == idx
                Surface(
                    color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHigh,
                    shape = RoundedCornerShape(999.dp),
                    border = BorderStroke(
                        0.75.dp,
                        if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                    ),
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .clickable { selectedSubTab = idx }
                        .testTag("android_subtab_$idx")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = label,
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 10.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }
            }
        }

        when (selectedSubTab) {
            0 -> {
                // 1. ANDROIDMANIFEST.XML VISUAL INSPECTOR & PLAY POLICY CHECKER
                Surface(
                    color = MaterialTheme.colorScheme.surfaceContainer,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(0.75.dp, MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Icon(Icons.Default.Android, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(16.dp))
                                Text(
                                    text = manifestSummary.packageName,
                                    fontFamily = JetBrainsMonoFontFamily,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Text(
                                text = "minSdk ${manifestSummary.minSdk} • target ${manifestSummary.targetSdk}",
                                fontFamily = JetBrainsMonoFontFamily,
                                fontSize = 9.5.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        if (!manifestSummary.hasSourceFile) {
                            Surface(
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable {
                                        onCreateStarterFile(
                                            "AndroidManifest.xml",
                                            AndroidProjectToolkit.defaultManifestXml(manifestSummary.appLabel)
                                        )
                                    }
                                    .testTag("create_manifest_starter_button")
                            ) {
                                Text(
                                    text = "+ Create AndroidManifest.xml file in this project",
                                    fontFamily = JetBrainsMonoFontFamily,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp)
                                )
                            }
                        }

                        Text(
                            text = "Activities (${manifestSummary.activities.size})",
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        manifestSummary.activities.forEach { act ->
                            Surface(
                                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 6.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = act.name,
                                        fontFamily = JetBrainsMonoFontFamily,
                                        fontSize = 10.5.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = buildString {
                                            if (act.isLauncher) append("MAIN/LAUNCHER ")
                                            if (act.isExported) append("exported=true")
                                        }.trim().ifBlank { "internal" },
                                        fontFamily = JetBrainsMonoFontFamily,
                                        fontSize = 9.sp,
                                        color = if (act.isLauncher) Color(0xFF10B981) else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                        Text(
                            text = "Declared Permissions (${manifestSummary.permissions.size})",
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (manifestSummary.permissions.isEmpty()) {
                            Text(
                                text = "No permissions declared yet (least-privilege compliant).",
                                fontSize = 11.sp,
                                color = Color(0xFF81C784)
                            )
                        } else {
                            manifestSummary.permissions.forEach { perm ->
                                Surface(
                                    color = if (perm.isPlayRestricted) Color(0xFFFF5F56).copy(alpha = 0.14f) else MaterialTheme.colorScheme.surfaceContainerHigh,
                                    shape = RoundedCornerShape(8.dp),
                                    border = BorderStroke(
                                        0.75.dp,
                                        if (perm.isPlayRestricted) Color(0xFFFF5F56) else MaterialTheme.colorScheme.outlineVariant
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(
                                        modifier = Modifier.padding(8.dp),
                                        verticalArrangement = Arrangement.spacedBy(2.dp)
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                            Icon(
                                                imageVector = if (perm.isPlayRestricted) Icons.Default.Warning else Icons.Default.CheckCircle,
                                                contentDescription = null,
                                                tint = if (perm.isPlayRestricted) Color(0xFFFF8A80) else Color(0xFF10B981),
                                                modifier = Modifier.size(13.dp)
                                            )
                                            Text(
                                                text = perm.shortName,
                                                fontFamily = JetBrainsMonoFontFamily,
                                                fontSize = 10.5.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                        Text(
                                            text = perm.plainMeaning,
                                            fontSize = 10.5.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        if (perm.isPlayRestricted) {
                                            Text(
                                                text = "Play Policy Tip: ${perm.playAdvice}",
                                                fontFamily = JetBrainsMonoFontFamily,
                                                fontSize = 9.5.sp,
                                                color = Color(0xFFFF8A80)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf(
                                "INTERNET" to "android.permission.INTERNET",
                                "CAMERA" to "android.permission.CAMERA",
                                "NOTIFICATIONS" to "android.permission.POST_NOTIFICATIONS",
                                "VIBRATE" to "android.permission.VIBRATE"
                            ).forEach { (shortLabel, fullPerm) ->
                                Surface(
                                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                                    shape = RoundedCornerShape(999.dp),
                                    border = BorderStroke(0.75.dp, MaterialTheme.colorScheme.outlineVariant),
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(999.dp))
                                        .clickable { onAddManifestPermission(fullPerm) }
                                ) {
                                    Text(
                                        text = "+ $shortLabel",
                                        fontFamily = JetBrainsMonoFontFamily,
                                        fontSize = 9.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            1 -> {
                // 2. VALUES & RESOURCES EXPLORER (strings.xml, colors.xml, activity_main.xml)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(
                        Triple("+ strings.xml", "strings.xml", AndroidProjectToolkit.defaultStringsXml()),
                        Triple("+ colors.xml", "colors.xml", AndroidProjectToolkit.defaultColorsXml()),
                        Triple("+ activity_main.xml", "activity_main.xml", AndroidProjectToolkit.defaultActivityMainXml())
                    ).forEach { (label, fileName, content) ->
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { onCreateStarterFile(fileName, content) }
                                .testTag("create_resource_${fileName.substringBefore('.')}")
                        ) {
                            Text(
                                text = label,
                                fontFamily = JetBrainsMonoFontFamily,
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp)
                            )
                        }
                    }
                }

                // Color Swatches Card
                Surface(
                    color = MaterialTheme.colorScheme.surfaceContainer,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(0.75.dp, MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(7.dp)
                    ) {
                        Text(
                            text = "@color/ Swatches (tap to insert)",
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            resourceBundle.colors.forEach { cItem ->
                                Surface(
                                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                                    shape = RoundedCornerShape(10.dp),
                                    border = BorderStroke(0.75.dp, MaterialTheme.colorScheme.outlineVariant),
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        .clickable {
                                            val ref = "@color/${cItem.key}"
                                            clipboard.setText(AnnotatedString(ref))
                                            onInsertSnippet(ref)
                                            Toast.makeText(context, "Inserted $ref", Toast.LENGTH_SHORT).show()
                                        }
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(14.dp)
                                                .clip(CircleShape)
                                                .background(Color(cItem.argbLong))
                                                .border(0.75.dp, Color.White.copy(alpha = 0.5f), CircleShape)
                                        )
                                        Column {
                                            Text(cItem.key, fontFamily = JetBrainsMonoFontFamily, fontSize = 9.5.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                            Text(cItem.hexValue, fontFamily = JetBrainsMonoFontFamily, fontSize = 8.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // @string/ entries Card
                Surface(
                    color = MaterialTheme.colorScheme.surfaceContainer,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(0.75.dp, MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "@string/ Entries (tap to insert)",
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF38BDF8)
                        )
                        resourceBundle.strings.forEach { sItem ->
                            Surface(
                                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable {
                                        val ref = "@string/${sItem.key}"
                                        clipboard.setText(AnnotatedString(ref))
                                        onInsertSnippet(ref)
                                        Toast.makeText(context, "Inserted $ref", Toast.LENGTH_SHORT).show()
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 6.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "@string/${sItem.key}",
                                        fontFamily = JetBrainsMonoFontFamily,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF38BDF8)
                                    )
                                    Text(
                                        text = "\"${sItem.value}\"",
                                        fontFamily = JetBrainsMonoFontFamily,
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }
            }

            else -> {
                // 3. GRADLE DEPENDENCY BROWSER & MULTI-STAGE BUILD PIPELINE
                Surface(
                    color = MaterialTheme.colorScheme.surfaceContainer,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(0.75.dp, MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Android Build Pipeline",
                                fontFamily = JetBrainsMonoFontFamily,
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Button(
                                onClick = onRunBuildPipeline,
                                shape = RoundedCornerShape(999.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981), contentColor = Color(0xFF0A1410)),
                                modifier = Modifier.testTag("run_android_build_pipeline_button")
                            ) {
                                Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(13.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Build Tasks", fontFamily = JetBrainsMonoFontFamily, fontSize = 9.5.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        val tasksToShow = buildTasks.ifEmpty {
                            AndroidProjectToolkit.runBuildPipeline(sections, sections.firstOrNull())
                        }
                        tasksToShow.forEach { task ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    modifier = Modifier.weight(1f),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = if (task.succeeded) Icons.Default.CheckCircle else Icons.Default.Warning,
                                        contentDescription = null,
                                        tint = if (task.succeeded) Color(0xFF10B981) else Color(0xFFFF5F56),
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Column {
                                        Text(
                                            text = task.taskPath,
                                            fontFamily = JetBrainsMonoFontFamily,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = task.summary,
                                            fontSize = 10.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                Text(
                                    text = "${task.durationMs}ms",
                                    fontFamily = JetBrainsMonoFontFamily,
                                    fontSize = 9.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                // Searchable Gradle Library Catalog
                Surface(
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(0.75.dp, MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp)
                    ) {
                        Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        BasicTextField(
                            value = gradleSearch,
                            onValueChange = { gradleSearch = it },
                            singleLine = true,
                            textStyle = TextStyle(
                                fontFamily = JetBrainsMonoFontFamily,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            ),
                            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                            decorationBox = { inner ->
                                if (gradleSearch.isEmpty()) {
                                    Text(
                                        text = "Search Gradle libraries (Compose, Room, Retrofit, Vico...)",
                                        fontFamily = JetBrainsMonoFontFamily,
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                inner()
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                val filteredDeps = remember(gradleSearch) {
                    val q = gradleSearch.trim()
                    if (q.isEmpty()) AndroidProjectToolkit.gradleCatalog
                    else AndroidProjectToolkit.gradleCatalog.filter {
                        it.artifact.contains(q, true) ||
                            it.category.contains(q, true) ||
                            it.beginnerSummary.contains(q, true)
                    }
                }

                filteredDeps.forEach { dep ->
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceContainer,
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(0.75.dp, MaterialTheme.colorScheme.outlineVariant),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(9.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text(
                                        text = dep.category,
                                        fontFamily = JetBrainsMonoFontFamily,
                                        fontSize = 8.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = "${dep.group}:${dep.artifact}:${dep.version}",
                                        fontFamily = JetBrainsMonoFontFamily,
                                        fontSize = 9.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                Text(
                                    text = dep.beginnerSummary,
                                    fontSize = 10.5.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Surface(
                                color = MaterialTheme.colorScheme.primaryContainer,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .padding(start = 8.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable {
                                        clipboard.setText(AnnotatedString(dep.implementationLine))
                                        Toast.makeText(context, "Copied ${dep.implementationLine}", Toast.LENGTH_SHORT).show()
                                    }
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                                ) {
                                    Icon(Icons.Default.ContentCopy, contentDescription = "Copy dependency", tint = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.size(12.dp))
                                    Text("Copy", fontFamily = JetBrainsMonoFontFamily, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
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
fun GitVcsExtendedHeader(
    activeBranch: String,
    commits: List<LocalGitCommit>,
    workingTreeStatus: List<GitFileStatusItem>,
    onSwitchBranch: (String) -> Unit,
    onCreateCommit: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var commitMessage by remember { mutableStateOf("") }

    Surface(
        color = MaterialTheme.colorScheme.surfaceContainer,
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(0.75.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.AccountTree, contentDescription = null, tint = Color(0xFFC084FC), modifier = Modifier.size(14.dp))
                Text("Branch:", fontFamily = JetBrainsMonoFontFamily, fontSize = 9.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                listOf("main", "practice-branch").forEach { branch ->
                    val active = branch == activeBranch
                    Surface(
                        color = if (active) Color(0xFFC084FC).copy(alpha = 0.22f) else MaterialTheme.colorScheme.surfaceContainerHigh,
                        shape = RoundedCornerShape(999.dp),
                        border = BorderStroke(0.75.dp, if (active) Color(0xFFC084FC) else MaterialTheme.colorScheme.outlineVariant),
                        modifier = Modifier
                            .clip(RoundedCornerShape(999.dp))
                            .clickable { onSwitchBranch(branch) }
                    ) {
                        Text(
                            text = branch,
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 9.5.sp,
                            fontWeight = if (active) FontWeight.Bold else FontWeight.Normal,
                            color = if (active) Color(0xFFC084FC) else MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            softWrap = false,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }

                workingTreeStatus.forEach { st ->
                    val badgeColor = when (st.statusBadge) {
                        "M" -> Color(0xFFFFD866)
                        "A" -> Color(0xFF10B981)
                        else -> Color(0xFF81C784)
                    }
                    Surface(
                        color = badgeColor.copy(alpha = 0.14f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = "${st.statusBadge} ${st.fileName}",
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 9.sp,
                            color = badgeColor,
                            maxLines = 1,
                            softWrap = false,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceContainerLowest,
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(0.75.dp, MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier.weight(1f)
                ) {
                    BasicTextField(
                        value = commitMessage,
                        onValueChange = { commitMessage = it },
                        singleLine = true,
                        textStyle = TextStyle(
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 10.5.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        ),
                        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                        decorationBox = { inner ->
                            Box(modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)) {
                                if (commitMessage.isEmpty()) {
                                    Text(
                                        text = "Commit message (e.g. Solve lesson 1)",
                                        fontFamily = JetBrainsMonoFontFamily,
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                inner()
                            }
                        }
                    )
                }

                Surface(
                    color = Color(0xFF10B981).copy(alpha = 0.18f),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(0.75.dp, Color(0xFF10B981)),
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable {
                            onCreateCommit(commitMessage)
                            commitMessage = ""
                        }
                        .testTag("git_commit_button")
                ) {
                    Text(
                        text = "Commit",
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF10B981),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }

            if (commits.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    commits.take(6).forEach { c ->
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceContainerHigh,
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "[${c.shortHash}] ${c.branch}: ${c.message}",
                                fontFamily = JetBrainsMonoFontFamily,
                                fontSize = 9.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                softWrap = false,
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RecentFilesModalDialog(
    sections: List<CodeSectionUiModel>,
    recentSectionIds: List<Long>,
    focusedSectionId: Long?,
    onSelectSection: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    var query by remember { mutableStateOf("") }
    val ordered = remember(sections, recentSectionIds) {
        val byId = sections.associateBy { it.id }
        val recents = recentSectionIds.mapNotNull { byId[it] }
        val rest = sections.filter { it.id !in recentSectionIds }
        (recents + rest).distinctBy { it.id }
    }
    val filtered = remember(ordered, query) {
        val q = query.trim()
        if (q.isEmpty()) ordered
        else ordered.filter {
            it.nativeFileName.contains(q, true) ||
                it.displayTitle.contains(q, true) ||
                it.language.displayName.contains(q, true)
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.History, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Text(
                    text = "Recent Files (Ctrl+E)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 360.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceContainerLowest,
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(0.75.dp, MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp)
                    ) {
                        Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        BasicTextField(
                            value = query,
                            onValueChange = { query = it },
                            singleLine = true,
                            textStyle = TextStyle(fontFamily = JetBrainsMonoFontFamily, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface),
                            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                            decorationBox = { inner ->
                                if (query.isEmpty()) {
                                    Text("Filter recent project files...", fontFamily = JetBrainsMonoFontFamily, fontSize = 10.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                inner()
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    filtered.forEach { sec ->
                        val isActive = sec.id == focusedSectionId
                        Surface(
                            color = if (isActive) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainer,
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(0.75.dp, if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { onSelectSection(sec.id) }
                                .testTag("recent_file_item_${sec.id}")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = sec.nativeFileName,
                                        fontFamily = JetBrainsMonoFontFamily,
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isActive) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = sec.effectiveLearnedConceptTag,
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Text(
                                    text = "${sec.language.shortLabel} • ${sec.lineCount}L",
                                    fontFamily = JetBrainsMonoFontFamily,
                                    fontSize = 9.5.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", fontFamily = JetBrainsMonoFontFamily)
            }
        }
    )
}
