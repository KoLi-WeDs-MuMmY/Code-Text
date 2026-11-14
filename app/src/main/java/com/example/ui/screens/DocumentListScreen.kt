package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.FolderSpecial
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SettingsBrightness
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.DocumentSummaryUiModel
import com.example.model.LanguageType
import com.example.model.PluginCategory
import com.example.model.ToolchainPluginItem
import com.example.ui.components.ModernFloatingMeshBackground
import com.example.ui.components.TrichromeBrandHeader
import com.example.ui.components.TrichromeStudioLogo
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.ThemeMode
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocumentListScreen(
    documents: List<DocumentSummaryUiModel>,
    searchQuery: String,
    shortcutLetter: Char,
    themeMode: ThemeMode,
    toolchainPlugins: List<ToolchainPluginItem> = emptyList(),
    onSearchQueryChange: (String) -> Unit,
    onOpenDocument: (Long) -> Unit,
    onCreateDocument: () -> Unit,
    onCreateDocumentWithTemplate: (String, LanguageType) -> Unit = { _, _ -> onCreateDocument() },
    onDeleteDocument: (Long) -> Unit,
    onRenameDocument: (Long, String) -> Unit = { _, _ -> },
    onCycleThemeMode: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenVaultModal: () -> Unit = {},
    onInstallAllSdks: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val totalSections = documents.sumOf { it.sectionCount }
    val allLearnedConcepts = documents.flatMap { it.conceptTags }.distinct()

    var selectedLanguageFilter by remember { mutableStateOf<LanguageType?>(null) }
    var showNewTemplateDialog by remember { mutableStateOf(false) }
    var newProjectTitle by remember { mutableStateOf("") }
    var newProjectLanguage by remember { mutableStateOf(LanguageType.KOTLIN) }

    val filteredList = documents.filter { doc ->
        selectedLanguageFilter == null || doc.primaryLanguage == selectedLanguageFilter
    }

    val compilerPlugins = remember(toolchainPlugins) {
        toolchainPlugins.filter { it.category == PluginCategory.LANGUAGE_SDK }
    }
    val installedSdkCount = remember(compilerPlugins) {
        compilerPlugins.count { it.isInstalled }
    }
    val totalSdkCount = if (compilerPlugins.isNotEmpty()) compilerPlugins.size else 6

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        containerColor = Color(0xFF1A1615),
        topBar = {
            TopAppBar(
                title = {
                    TrichromeBrandHeader(
                        appName = stringResource(R.string.app_name)
                    )
                },
                actions = {
                    // Hidden Vault ("xyz") Recovery & Status Pill
                    Surface(
                        color = Color(0xFF232E23),
                        shape = RoundedCornerShape(999.dp),
                        border = BorderStroke(0.8.dp, Color(0xFF4E6E4E)),
                        modifier = Modifier
                            .clip(RoundedCornerShape(999.dp))
                            .clickable(onClick = onOpenVaultModal)
                            .testTag("home_vault_recovery_button")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Hidden Vault Recovery",
                                tint = Color(0xFF81C784),
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Vault 'xyz'",
                                fontFamily = JetBrainsMonoFontFamily,
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF81C784)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    // Shortcut Key Pill
                    Surface(
                        color = Color(0xFF2B241E),
                        shape = RoundedCornerShape(999.dp),
                        border = BorderStroke(0.8.dp, Color(0xFF524336)),
                        modifier = Modifier
                            .clip(RoundedCornerShape(999.dp))
                            .clickable(onClick = onOpenSettings)
                            .testTag("home_shortcut_settings_button")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Keyboard,
                                contentDescription = "Configure Ctrl Shortcut",
                                tint = Color(0xFFFFC66D),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Ctrl+$shortcutLetter",
                                fontFamily = JetBrainsMonoFontFamily,
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFE6E1DC)
                            )
                        }
                    }

                    IconButton(
                        onClick = onCycleThemeMode,
                        modifier = Modifier.testTag("home_theme_toggle_button")
                    ) {
                        Icon(
                            imageVector = when (themeMode) {
                                ThemeMode.LIGHT -> Icons.Default.LightMode
                                ThemeMode.DARK -> Icons.Default.DarkMode
                                ThemeMode.SYSTEM -> Icons.Default.SettingsBrightness
                            },
                            contentDescription = "Switch Theme (${themeMode.label})",
                            tint = Color(0xFFE6E1DC)
                        )
                    }

                    IconButton(
                        onClick = onOpenSettings,
                        modifier = Modifier.testTag("home_main_settings_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Studio Settings (Theme, SDKs, Language)",
                            tint = Color(0xFFD4883A)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF1F1B19).copy(alpha = 0.96f)
                )
            )
        },
        floatingActionButton = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    color = Color(0xFF2B241E),
                    shape = RoundedCornerShape(999.dp),
                    border = BorderStroke(1.dp, Color(0xFFD4883A).copy(alpha = 0.7f)),
                    shadowElevation = 6.dp,
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .clickable {
                            newProjectTitle = ""
                            showNewTemplateDialog = true
                        }
                        .testTag("open_template_wizard_button")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Code,
                            contentDescription = "New Project Template",
                            tint = Color(0xFFFFC66D),
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Template",
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFE6E1DC)
                        )
                    }
                }

                ExtendedFloatingActionButton(
                    onClick = onCreateDocument,
                    shape = RoundedCornerShape(999.dp),
                    icon = {
                        Icon(
                            imageVector = Icons.Default.CreateNewFolder,
                            contentDescription = stringResource(R.string.new_document)
                        )
                    },
                    text = {
                        Text(
                            text = stringResource(R.string.new_document),
                            fontFamily = JetBrainsMonoFontFamily,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    containerColor = Color(0xFFD4883A),
                    contentColor = Color(0xFF1A1615),
                    modifier = Modifier.testTag("create_document_fab")
                )
            }
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.TopCenter
        ) {
            ModernFloatingMeshBackground(
                modifier = Modifier.fillMaxSize()
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 780.dp)
                    .animateContentSize(
                        animationSpec = spring(
                            dampingRatio = 0.82f,
                            stiffness = Spring.StiffnessMediumLow
                        )
                    ),
                contentPadding = PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    top = 10.dp,
                    bottom = 104.dp
                ),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 1. CodeAssist + iOS Glass Studio Welcome Hero Banner
                item {
                    HeroWorkspaceBanner(
                        shortcutLetter = shortcutLetter,
                        folderCount = documents.size,
                        totalSections = totalSections,
                        learnedConceptsCount = allLearnedConcepts.size,
                        installedSdkCount = installedSdkCount,
                        totalSdkCount = totalSdkCount,
                        onOpenSdkManager = onOpenSettings,
                        onInstallAllSdks = onInstallAllSdks
                    )
                }

                // 2. Quick Create Project by Language Template Bar
                item {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF231E1B).copy(alpha = 0.94f)),
                        border = BorderStroke(1.dp, Color(0xFF38302A)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Extension,
                                        contentDescription = null,
                                        tint = Color(0xFFD4883A),
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(
                                        text = "Quick Start Project Template",
                                        fontFamily = JetBrainsMonoFontFamily,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFE6E1DC)
                                    )
                                }
                                Text(
                                    text = "1-Tap Workspace",
                                    fontFamily = JetBrainsMonoFontFamily,
                                    fontSize = 9.5.sp,
                                    color = Color(0xFFA39C93)
                                )
                            }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                LanguageType.entries.forEach { lang ->
                                    Surface(
                                        color = Color(0xFF2C2521),
                                        shape = RoundedCornerShape(999.dp),
                                        border = BorderStroke(0.8.dp, lang.badgeColor.copy(alpha = 0.65f)),
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(999.dp))
                                            .clickable {
                                                onCreateDocumentWithTemplate(
                                                    "${lang.displayName} App",
                                                    lang
                                                )
                                            }
                                            .testTag("quick_template_${lang.name.lowercase()}")
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                                            modifier = Modifier.padding(horizontal = 11.dp, vertical = 6.dp)
                                        ) {
                                            Box(
                                                contentAlignment = Alignment.Center,
                                                modifier = Modifier
                                                    .size(16.dp)
                                                    .clip(CircleShape)
                                                    .background(lang.badgeColor.copy(alpha = 0.25f))
                                            ) {
                                                Text(
                                                    text = lang.badgeLabel.take(2),
                                                    fontFamily = JetBrainsMonoFontFamily,
                                                    fontSize = 7.5.sp,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    color = lang.badgeColor
                                                )
                                            }
                                            Text(
                                                text = "+ ${lang.displayName}",
                                                fontFamily = JetBrainsMonoFontFamily,
                                                fontSize = 10.5.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFFE6E1DC)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // 3. Pill-Rounded Search Bar
                item {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = onSearchQueryChange,
                        placeholder = {
                            Text(
                                text = stringResource(R.string.search_documents_hint),
                                fontFamily = JetBrainsMonoFontFamily,
                                fontSize = 12.sp,
                                color = Color(0xFFA39C93)
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search Folders",
                                tint = Color(0xFFD4883A)
                            )
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { onSearchQueryChange("") }) {
                                    Icon(
                                        imageVector = Icons.Default.Clear,
                                        contentDescription = "Clear Search",
                                        tint = Color(0xFFA39C93)
                                    )
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(999.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFF231E1B),
                            unfocusedContainerColor = Color(0xFF211C19),
                            focusedBorderColor = Color(0xFFD4883A),
                            unfocusedBorderColor = Color(0xFF3A322C),
                            focusedTextColor = Color(0xFFE6E1DC),
                            unfocusedTextColor = Color(0xFFE6E1DC)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("search_documents_input")
                    )
                }

                // 4. Pill-Rounded Multi-Language Filter Strip
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            color = Color(0xFF28221F),
                            shape = RoundedCornerShape(999.dp),
                            border = BorderStroke(0.8.dp, Color(0xFF3E352F))
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.FilterList,
                                    contentDescription = null,
                                    modifier = Modifier.size(13.dp),
                                    tint = Color(0xFFD4883A)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "PROJECTS",
                                    fontFamily = JetBrainsMonoFontFamily,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFE6E1DC)
                                )
                            }
                        }

                        // "All" pill chip
                        val isAllSelected = selectedLanguageFilter == null
                        Surface(
                            color = if (isAllSelected) Color(0xFF473628) else Color(0xFF231E1B),
                            shape = RoundedCornerShape(999.dp),
                            border = BorderStroke(
                                width = if (isAllSelected) 1.dp else 0.75.dp,
                                color = if (isAllSelected) Color(0xFFD4883A) else Color(0xFF38302A)
                            ),
                            modifier = Modifier
                                .clip(RoundedCornerShape(999.dp))
                                .clickable { selectedLanguageFilter = null }
                        ) {
                            Text(
                                text = "All (${documents.size})",
                                fontFamily = JetBrainsMonoFontFamily,
                                fontSize = 11.sp,
                                fontWeight = if (isAllSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isAllSelected) Color(0xFFFFC66D) else Color(0xFFD5CEC6),
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }

                        // Language pill chips
                        LanguageType.entries.forEach { lang ->
                            val count = documents.count { it.primaryLanguage == lang }
                            val isSelected = selectedLanguageFilter == lang
                            Surface(
                                color = if (isSelected) lang.badgeColor.copy(alpha = 0.24f) else Color(0xFF231E1B),
                                shape = RoundedCornerShape(999.dp),
                                border = BorderStroke(
                                    width = if (isSelected) 1.dp else 0.75.dp,
                                    color = if (isSelected) lang.badgeColor else Color(0xFF38302A)
                                ),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(999.dp))
                                    .clickable {
                                        selectedLanguageFilter = if (isSelected) null else lang
                                    }
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(lang.badgeColor)
                                    )
                                    Text(
                                        text = "${lang.displayName} ($count)",
                                        fontFamily = JetBrainsMonoFontFamily,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) lang.badgeColor else Color(0xFFD5CEC6)
                                    )
                                }
                            }
                        }
                    }
                }

                // 5. Rounded CodeAssist Project Cards or Empty State
                if (filteredList.isEmpty()) {
                    item {
                        EmptyDocumentsCard(onCreateDocument = onCreateDocument)
                    }
                } else {
                    items(
                        items = filteredList,
                        key = { it.id }
                    ) { folder ->
                        FolderSummaryCard(
                            folder = folder,
                            onClick = { onOpenDocument(folder.id) },
                            onDelete = { onDeleteDocument(folder.id) },
                            onRename = { newTitle -> onRenameDocument(folder.id, newTitle) }
                        )
                    }
                }
            }
        }
    }

    if (showNewTemplateDialog) {
        AlertDialog(
            onDismissRequest = { showNewTemplateDialog = false },
            shape = RoundedCornerShape(24.dp),
            containerColor = Color(0xFF221D1A),
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CreateNewFolder,
                        contentDescription = null,
                        tint = Color(0xFFD4883A)
                    )
                    Text(
                        text = "New CodeAssist Project",
                        fontFamily = JetBrainsMonoFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color(0xFFE6E1DC)
                    )
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = newProjectTitle,
                        onValueChange = { newProjectTitle = it },
                        label = { Text("Project Name", fontFamily = JetBrainsMonoFontFamily, fontSize = 11.sp) },
                        placeholder = { Text("${newProjectLanguage.displayName} Project", fontSize = 12.sp) },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFD4883A),
                            unfocusedBorderColor = Color(0xFF3E352F),
                            focusedTextColor = Color(0xFFE6E1DC),
                            unfocusedTextColor = Color(0xFFE6E1DC)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text(
                        text = "Select Starter Toolchain & Template:",
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFA39C93)
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        LanguageType.entries.chunked(2).forEach { pair ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                pair.forEach { lang ->
                                    val isSelected = newProjectLanguage == lang
                                    Surface(
                                        color = if (isSelected) lang.badgeColor.copy(alpha = 0.22f) else Color(0xFF1A1615),
                                        shape = RoundedCornerShape(14.dp),
                                        border = BorderStroke(
                                            width = if (isSelected) 1.2.dp else 0.75.dp,
                                            color = if (isSelected) lang.badgeColor else Color(0xFF38302A)
                                        ),
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(14.dp))
                                            .clickable { newProjectLanguage = lang }
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)
                                        ) {
                                            Box(
                                                contentAlignment = Alignment.Center,
                                                modifier = Modifier
                                                    .size(20.dp)
                                                    .clip(CircleShape)
                                                    .background(lang.badgeColor.copy(alpha = 0.25f))
                                            ) {
                                                Text(
                                                    text = lang.badgeLabel.take(2),
                                                    fontFamily = JetBrainsMonoFontFamily,
                                                    fontSize = 8.5.sp,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    color = lang.badgeColor
                                                )
                                            }
                                            Column {
                                                Text(
                                                    text = lang.displayName,
                                                    fontFamily = JetBrainsMonoFontFamily,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFFE6E1DC)
                                                )
                                                Text(
                                                    text = "Main.${lang.nativeExtension}",
                                                    fontFamily = JetBrainsMonoFontFamily,
                                                    fontSize = 9.sp,
                                                    color = Color(0xFFA39C93)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val finalTitle = newProjectTitle.trim().ifBlank { "${newProjectLanguage.displayName} App" }
                        showNewTemplateDialog = false
                        onCreateDocumentWithTemplate(finalTitle, newProjectLanguage)
                    },
                    shape = RoundedCornerShape(999.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFD4883A),
                        contentColor = Color(0xFF1A1615)
                    )
                ) {
                    Text(
                        text = "Create Project",
                        fontFamily = JetBrainsMonoFontFamily,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showNewTemplateDialog = false }) {
                    Text("Cancel", color = Color(0xFFA39C93), fontFamily = JetBrainsMonoFontFamily)
                }
            }
        )
    }
}

@Composable
private fun HeroWorkspaceBanner(
    shortcutLetter: Char,
    folderCount: Int,
    totalSections: Int,
    learnedConceptsCount: Int,
    installedSdkCount: Int,
    totalSdkCount: Int,
    onOpenSdkManager: () -> Unit,
    onInstallAllSdks: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFF3E352F), RoundedCornerShape(24.dp))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(194.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.img_editor_hero_1794022926039),
                contentDescription = "KtScribe CodeAssist Studio",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                Color(0xFF1A1615).copy(alpha = 0.96f),
                                Color(0xFF241E1B).copy(alpha = 0.90f),
                                Color(0xFF1A1615).copy(alpha = 0.76f)
                            )
                        )
                    )
                    .padding(18.dp),
                contentAlignment = Alignment.BottomStart
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TrichromeStudioLogo(sizeDp = 28.dp)

                        Surface(
                            color = Color(0xFF3B2E25),
                            shape = RoundedCornerShape(999.dp),
                            border = BorderStroke(0.8.dp, Color(0xFFD4883A))
                        ) {
                            Text(
                                text = "● ● ●  CODEASSIST + iOS STUDIO",
                                fontFamily = JetBrainsMonoFontFamily,
                                fontSize = 9.5.sp,
                                color = Color(0xFFFFC66D),
                                fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                            )
                        }

                        Surface(
                            color = Color(0xFF233023),
                            shape = RoundedCornerShape(999.dp),
                            border = BorderStroke(0.8.dp, Color(0xFF6A8759)),
                            modifier = Modifier
                                .clip(RoundedCornerShape(999.dp))
                                .clickable(onClick = onOpenSdkManager)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                modifier = Modifier.padding(horizontal = 9.dp, vertical = 3.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = Color(0xFF81C784),
                                    modifier = Modifier.size(11.dp)
                                )
                                Text(
                                    text = "$installedSdkCount/$totalSdkCount SDKs Ready",
                                    fontFamily = JetBrainsMonoFontFamily,
                                    fontSize = 9.5.sp,
                                    color = Color(0xFF81C784),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Text(
                        text = "CodeAssist IDE • Breakpoint Debugger • Auto-Import & AST",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color(0xFFF5EFEA),
                        fontWeight = FontWeight.Bold
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Terminal,
                                contentDescription = null,
                                tint = Color(0xFFD4883A),
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "$folderCount Projects • $totalSections Files • $learnedConceptsCount Concepts",
                                fontFamily = JetBrainsMonoFontFamily,
                                fontSize = 11.sp,
                                color = Color(0xFFD5CEC6)
                            )
                        }

                        if (installedSdkCount < totalSdkCount) {
                            Surface(
                                color = Color(0xFFD4883A),
                                shape = RoundedCornerShape(999.dp),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(999.dp))
                                    .clickable(onClick = onInstallAllSdks)
                            ) {
                                Text(
                                    text = "Install All SDKs",
                                    fontFamily = JetBrainsMonoFontFamily,
                                    fontSize = 9.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1A1615),
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
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
private fun FolderSummaryCard(
    folder: DocumentSummaryUiModel,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    onRename: (String) -> Unit
) {
    val dateFormat = SimpleDateFormat("MMM d, HH:mm", Locale.getDefault())
    val formattedDate = dateFormat.format(Date(folder.updatedAt))
    var showRenameDialog by remember { mutableStateOf(false) }
    var renameText by remember(folder.title) { mutableStateOf(folder.title) }

    val lang = folder.primaryLanguage

    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF231E1B).copy(alpha = 0.96f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = Color(0xFF3A322C),
                shape = RoundedCornerShape(22.dp)
            )
            .clip(RoundedCornerShape(22.dp))
            .clickable(onClick = onClick)
            .testTag("document_card_${folder.id}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    // CodeAssist Circular/Rounded Language Badge
                    Surface(
                        color = lang.badgeColor.copy(alpha = 0.18f),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, lang.badgeColor.copy(alpha = 0.55f))
                    ) {
                        Text(
                            text = lang.badgeLabel,
                            fontFamily = JetBrainsMonoFontFamily,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 12.sp,
                            color = lang.badgeColor,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Box(modifier = Modifier.size(7.dp).clip(CircleShape).background(Color(0xFFFF5F56)))
                            Box(modifier = Modifier.size(7.dp).clip(CircleShape).background(Color(0xFFFFBD2E)))
                            Box(modifier = Modifier.size(7.dp).clip(CircleShape).background(Color(0xFF27C93F)))
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "app/${lang.directoryPath.trimEnd('/')} • .${lang.nativeExtension}",
                                fontFamily = JetBrainsMonoFontFamily,
                                fontSize = 9.5.sp,
                                color = lang.badgeColor,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = folder.title,
                            style = MaterialTheme.typography.titleMedium,
                            color = Color(0xFFF5EFEA),
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Surface(
                        color = Color(0xFF283A28),
                        shape = RoundedCornerShape(999.dp),
                        border = BorderStroke(0.8.dp, Color(0xFF6A8759)),
                        modifier = Modifier
                            .clip(RoundedCornerShape(999.dp))
                            .clickable(onClick = onClick)
                            .testTag("open_run_document_${folder.id}")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "Open in Studio",
                                tint = Color(0xFF81C784),
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "Open",
                                fontFamily = JetBrainsMonoFontFamily,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFE6E1DC)
                            )
                        }
                    }

                    IconButton(
                        onClick = {
                            renameText = folder.title
                            showRenameDialog = true
                        },
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("rename_document_${folder.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Rename Folder",
                            tint = Color(0xFFA39C93),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("delete_document_${folder.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "Delete Folder",
                            tint = Color(0xFFF28B82),
                            modifier = Modifier.size(17.dp)
                        )
                    }
                }
            }

            if (showRenameDialog) {
                AlertDialog(
                    onDismissRequest = { showRenameDialog = false },
                    shape = RoundedCornerShape(22.dp),
                    containerColor = Color(0xFF241F1C),
                    title = {
                        Text(
                            text = "Rename Project Folder",
                            fontWeight = FontWeight.Bold,
                            fontFamily = JetBrainsMonoFontFamily,
                            color = Color(0xFFE6E1DC)
                        )
                    },
                    text = {
                        OutlinedTextField(
                            value = renameText,
                            onValueChange = { renameText = it },
                            label = { Text("Folder Name") },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                if (renameText.isNotBlank()) {
                                    onRename(renameText.trim())
                                }
                                showRenameDialog = false
                            }
                        ) {
                            Text("Rename", fontWeight = FontWeight.Bold, color = Color(0xFFD4883A))
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = { showRenameDialog = false }
                        ) {
                            Text("Cancel", color = Color(0xFFA39C93))
                        }
                    }
                )
            }

            // Code preview box with rounded corners
            Surface(
                color = Color(0xFF191513),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(0.8.dp, Color(0xFF302924)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = folder.previewSnippet,
                    fontFamily = JetBrainsMonoFontFamily,
                    fontSize = 11.sp,
                    lineHeight = 16.sp,
                    color = Color(0xFFD5CEC6),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp)
                )
            }

            if (folder.conceptTags.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    folder.conceptTags.forEach { tag ->
                        Surface(
                            color = Color(0xFF2E2620),
                            shape = RoundedCornerShape(999.dp),
                            border = BorderStroke(0.6.dp, Color(0xFF4A3D33))
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.School,
                                    contentDescription = null,
                                    tint = Color(0xFFFFC66D),
                                    modifier = Modifier.size(11.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = tag,
                                    fontFamily = JetBrainsMonoFontFamily,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFFE6E1DC)
                                )
                            }
                        }
                    }
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    color = Color(0xFF2C241E),
                    shape = RoundedCornerShape(999.dp),
                    border = BorderStroke(0.6.dp, Color(0xFF4A3D33))
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 3.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Code,
                            contentDescription = null,
                            tint = Color(0xFFD4883A),
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${folder.sectionCount} File${if (folder.sectionCount > 1) "s" else ""}",
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFFE6E1DC)
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Terminal,
                        contentDescription = null,
                        modifier = Modifier.size(12.dp),
                        tint = Color(0xFFA39C93)
                    )
                    Text(
                        text = "${folder.wordCount} words",
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 10.sp,
                        color = Color(0xFFA39C93)
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Storage,
                        contentDescription = null,
                        modifier = Modifier.size(12.dp),
                        tint = Color(0xFF81C784)
                    )
                    Text(
                        text = "Synced $formattedDate",
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 10.sp,
                        color = Color(0xFFA39C93)
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyDocumentsCard(onCreateDocument: () -> Unit) {
    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF231E1B).copy(alpha = 0.94f)
        ),
        border = BorderStroke(1.dp, Color(0xFF3A322C)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                imageVector = Icons.Default.FolderSpecial,
                contentDescription = null,
                tint = Color(0xFFD4883A),
                modifier = Modifier.size(42.dp)
            )
            Text(
                text = stringResource(R.string.empty_documents_title),
                style = MaterialTheme.typography.titleMedium,
                color = Color(0xFFE6E1DC),
                fontWeight = FontWeight.Bold
            )
            Text(
                text = stringResource(R.string.empty_documents_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFFA39C93)
            )
        }
    }
}
