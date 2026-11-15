package com.example.ui.screens

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
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
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
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.FolderSpecial
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SettingsBrightness
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
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
import com.example.learning.CodingLesson
import com.example.learning.ConceptMasteryState
import com.example.learning.ErrorPlainEnglishTranslator
import com.example.learning.LearningTrack
import com.example.learning.LessonProgressState
import com.example.learning.StudentCurriculumCatalog
import com.example.model.CodeAssistProjectTemplate
import com.example.model.DocumentSummaryUiModel
import com.example.model.HomeScreenTab
import com.example.model.LanguageType
import com.example.model.PluginCategory
import com.example.model.ToolchainPluginItem
import com.example.ui.components.CodeAssistCreateProjectWizardDialog
import com.example.ui.components.CodeAssistHomeModeToggleBar
import com.example.ui.components.CodeAssistProjectCard
import com.example.ui.components.CodeAssistThemeQuickPickerDialog
import com.example.ui.components.ModernFloatingMeshBackground
import com.example.ui.components.TrichromeBrandHeader
import com.example.ui.components.TrichromeStudioLogo
import com.example.ui.theme.AdaptiveThemeSpec
import com.example.ui.theme.AppThemeId
import com.example.ui.theme.CodeAssistAppTheme
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.LocalAdaptiveThemeSpec
import com.example.ui.theme.ScreenRatioTier
import com.example.ui.theme.ThemeMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocumentListScreen(
    documents: List<DocumentSummaryUiModel>,
    searchQuery: String,
    shortcutLetter: Char,
    themeMode: ThemeMode,
    toolchainPlugins: List<ToolchainPluginItem> = emptyList(),
    conceptMastery: List<ConceptMasteryState> = emptyList(),
    lessonProgressById: Map<String, LessonProgressState> = emptyMap(),
    homeScreenTab: HomeScreenTab = HomeScreenTab.PROJECTS,
    isGuidedBeginnerMode: Boolean = true,
    currentCodeAssistAppTheme: CodeAssistAppTheme = CodeAssistAppTheme.CODEASSIST_DARK,
    currentEditorScheme: AppThemeId = AppThemeId.DARCULA_ESPRESSO,
    onSelectHomeTab: (HomeScreenTab) -> Unit = {},
    onToggleGuidedBeginnerMode: () -> Unit = {},
    onCreateProjectFromWizard: (String, String, Int, CodeAssistProjectTemplate) -> Unit = { _, _, _, _ -> },
    onSelectCodeAssistAppTheme: (CodeAssistAppTheme) -> Unit = {},
    onSelectEditorScheme: (AppThemeId) -> Unit = {},
    onSearchQueryChange: (String) -> Unit,
    onOpenDocument: (Long) -> Unit,
    onCreateDocument: () -> Unit,
    onCreateDocumentWithTemplate: (String, LanguageType) -> Unit = { _, _ -> onCreateDocument() },
    onStartLesson: (CodingLesson) -> Unit = {},
    onOpenQuiz: (LearningTrack?) -> Unit = {},
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
    val adaptive = LocalAdaptiveThemeSpec.current

    var selectedLanguageFilter by remember { mutableStateOf<LanguageType?>(null) }
    var showCreateWizardDialog by remember { mutableStateOf(false) }
    var showThemeQuickPicker by remember { mutableStateOf(false) }

    val filteredList = remember(documents, selectedLanguageFilter) {
        documents.filter { doc ->
            selectedLanguageFilter == null || doc.primaryLanguage == selectedLanguageFilter
        }
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
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Column(modifier = Modifier.fillMaxWidth()) {
                TopAppBar(
                    title = {
                        TrichromeBrandHeader(
                            appName = stringResource(R.string.app_name),
                            showSubtitle = adaptive.showBrandSubtitle,
                            compact = adaptive.useCompactTopActions
                        )
                    },
                    actions = {
                        IconButton(
                            onClick = { showThemeQuickPicker = true },
                            modifier = Modifier.testTag("home_theme_picker_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Palette,
                                contentDescription = "CodeAssist Themes & Schemes",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }

                        IconButton(
                            onClick = onOpenVaultModal,
                            modifier = Modifier.testTag("home_vault_recovery_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Hidden Vault 'xyz'",
                                tint = MaterialTheme.colorScheme.tertiary
                            )
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
                                contentDescription = "Switch Brightness (${themeMode.label})",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        IconButton(
                            onClick = onOpenSettings,
                            modifier = Modifier.testTag("home_main_settings_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Studio Settings",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer
                    )
                )

                // Top segmented toggle bar: Projects | Learn Mode + Guided ON/Pro Mode pill
                CodeAssistHomeModeToggleBar(
                    activeTab = homeScreenTab,
                    isGuidedBeginnerMode = isGuidedBeginnerMode,
                    onSelectTab = onSelectHomeTab,
                    onToggleGuidedMode = onToggleGuidedBeginnerMode
                )
            }
        },
        floatingActionButton = {
            if (homeScreenTab == HomeScreenTab.PROJECTS) {
                ExtendedFloatingActionButton(
                    onClick = { showCreateWizardDialog = true },
                    shape = RoundedCornerShape(16.dp),
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "New Project"
                        )
                    },
                    text = {
                        Text(
                            text = "New Project",
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            softWrap = false
                        )
                    },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier
                        .navigationBarsPadding()
                        .testTag("create_document_fab")
                )
            }
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (adaptive.useNavigationRail) {
                NavigationRail(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(adaptive.railWidth.coerceAtLeast(68.dp))
                ) {
                    Spacer(modifier = Modifier.height(8.dp))
                    NavigationRailItem(
                        selected = homeScreenTab == HomeScreenTab.PROJECTS,
                        onClick = { onSelectHomeTab(HomeScreenTab.PROJECTS) },
                        icon = { Icon(Icons.Default.FolderSpecial, contentDescription = "Projects") },
                        label = { Text("Projects", fontSize = 10.sp, maxLines = 1) }
                    )
                    NavigationRailItem(
                        selected = homeScreenTab == HomeScreenTab.LEARN_MODE,
                        onClick = { onSelectHomeTab(HomeScreenTab.LEARN_MODE) },
                        icon = { Icon(Icons.Default.School, contentDescription = "Learn Mode") },
                        label = { Text("Learn", fontSize = 10.sp, maxLines = 1) }
                    )
                    NavigationRailItem(
                        selected = false,
                        onClick = { showThemeQuickPicker = true },
                        icon = { Icon(Icons.Default.Palette, contentDescription = "Themes") },
                        label = { Text("Themes", fontSize = 10.sp, maxLines = 1) }
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    NavigationRailItem(
                        selected = false,
                        onClick = onOpenSettings,
                        icon = { Icon(Icons.Default.Settings, contentDescription = "Studio Settings") },
                        label = { Text("Setup", fontSize = 10.sp, maxLines = 1) }
                    )
                }
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                contentAlignment = Alignment.TopCenter
            ) {
                ModernFloatingMeshBackground(modifier = Modifier.fillMaxSize())

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = adaptive.maxContentWidth)
                        .animateContentSize(
                            animationSpec = spring(
                                dampingRatio = 0.82f,
                                stiffness = Spring.StiffnessMediumLow
                            )
                        ),
                    contentPadding = PaddingValues(
                        start = adaptive.screenPadding,
                        end = adaptive.screenPadding,
                        top = 12.dp,
                        bottom = 112.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(adaptive.sectionSpacing)
                ) {
                    if (homeScreenTab == HomeScreenTab.PROJECTS) {
                        // Compact CodeAssist Workspace Header Banner
                        item {
                            CompactCodeAssistProjectsSummaryCard(
                                folderCount = documents.size,
                                totalSections = totalSections,
                                installedSdkCount = installedSdkCount,
                                totalSdkCount = totalSdkCount,
                                activeAppTheme = currentCodeAssistAppTheme,
                                activeEditorScheme = currentEditorScheme,
                                shortcutLetter = shortcutLetter,
                                onOpenThemePicker = { showThemeQuickPicker = true },
                                onOpenNewProjectWizard = { showCreateWizardDialog = true },
                                onInstallAllSdks = onInstallAllSdks
                            )
                        }

                        // Material 3 Project Search Bar
                        item {
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = onSearchQueryChange,
                                placeholder = {
                                    Text(
                                        text = stringResource(R.string.search_documents_hint),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Search,
                                        contentDescription = "Search Projects",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                },
                                trailingIcon = {
                                    if (searchQuery.isNotEmpty()) {
                                        IconButton(onClick = { onSearchQueryChange("") }) {
                                            Icon(
                                                imageVector = Icons.Default.Clear,
                                                contentDescription = "Clear Search",
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                },
                                singleLine = true,
                                shape = RoundedCornerShape(14.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("search_documents_input")
                            )
                        }

                        // Compact Language Filter Strip
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                FilterChip(
                                    selected = selectedLanguageFilter == null,
                                    onClick = { selectedLanguageFilter = null },
                                    label = { Text("All (${documents.size})", maxLines = 1, softWrap = false) }
                                )

                                LanguageType.entries.forEach { lang ->
                                    val count = documents.count { it.primaryLanguage == lang }
                                    val isSelected = selectedLanguageFilter == lang
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = {
                                            selectedLanguageFilter = if (isSelected) null else lang
                                        },
                                        leadingIcon = {
                                            Box(
                                                modifier = Modifier
                                                    .size(8.dp)
                                                    .clip(CircleShape)
                                                    .background(lang.badgeColor)
                                            )
                                        },
                                        label = {
                                            Text(
                                                text = if (count > 0) "${lang.displayName} ($count)" else lang.displayName,
                                                maxLines = 1,
                                                softWrap = false
                                            )
                                        }
                                    )
                                }
                            }
                        }

                        // Responsive CodeAssist Project Cards (1-3 columns)
                        if (filteredList.isEmpty()) {
                            item {
                                EmptyDocumentsCard(
                                    onCreateWizard = { showCreateWizardDialog = true },
                                    onSwitchToLearn = { onSelectHomeTab(HomeScreenTab.LEARN_MODE) }
                                )
                            }
                        } else {
                            val gridRows = filteredList.chunked(adaptive.gridColumns.coerceAtLeast(1))
                            items(
                                items = gridRows,
                                key = { row -> row.joinToString("_") { it.id.toString() } }
                            ) { rowItems ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(adaptive.sectionSpacing)
                                ) {
                                    rowItems.forEach { folder ->
                                        CodeAssistProjectCard(
                                            project = folder,
                                            onOpen = { onOpenDocument(folder.id) },
                                            onRename = { newTitle -> onRenameDocument(folder.id, newTitle) },
                                            onDelete = { onDeleteDocument(folder.id) },
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                    repeat(adaptive.gridColumns - rowItems.size) {
                                        Spacer(modifier = Modifier.weight(1f))
                                    }
                                }
                            }
                        }
                    } else {
                        // LEARN MODE TAB
                        item {
                            HeroWorkspaceBanner(
                                adaptive = adaptive,
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

                        item {
                            GuidedModeControlBanner(
                                isGuidedBeginnerMode = isGuidedBeginnerMode,
                                onToggleGuidedMode = onToggleGuidedBeginnerMode,
                                onOpenQuiz = { onOpenQuiz(null) }
                            )
                        }

                        item {
                            StudentLearnHubCard(
                                adaptive = adaptive,
                                conceptMastery = conceptMastery,
                                lessonProgressById = lessonProgressById,
                                onStartLesson = onStartLesson,
                                onOpenQuiz = onOpenQuiz
                            )
                        }

                        item {
                            StudentWordDictionaryPreviewCard()
                        }
                    }
                }
            }
        }
    }

    if (showCreateWizardDialog) {
        CodeAssistCreateProjectWizardDialog(
            onCreateProject = { appName, packageName, minSdk, template ->
                onCreateProjectFromWizard(appName, packageName, minSdk, template)
            },
            onDismiss = { showCreateWizardDialog = false }
        )
    }

    if (showThemeQuickPicker) {
        CodeAssistThemeQuickPickerDialog(
            currentAppTheme = currentCodeAssistAppTheme,
            currentEditorScheme = currentEditorScheme,
            onSelectAppTheme = onSelectCodeAssistAppTheme,
            onSelectEditorScheme = onSelectEditorScheme,
            onDismiss = { showThemeQuickPicker = false }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CompactCodeAssistProjectsSummaryCard(
    folderCount: Int,
    totalSections: Int,
    installedSdkCount: Int,
    totalSdkCount: Int,
    activeAppTheme: CodeAssistAppTheme,
    activeEditorScheme: AppThemeId,
    shortcutLetter: Char,
    onOpenThemePicker: () -> Unit,
    onOpenNewProjectWizard: () -> Unit,
    onInstallAllSdks: () -> Unit
) {
    ElevatedCard(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(0.8.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(18.dp))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Your CodeAssist Projects",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "$folderCount projects • $totalSections files • $installedSdkCount/$totalSdkCount SDKs • Ctrl+$shortcutLetter",
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 10.5.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                OutlinedButton(
                    onClick = onOpenNewProjectWizard,
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("open_template_wizard_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Wizard", fontSize = 12.sp, maxLines = 1, softWrap = false)
                }
            }

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shape = RoundedCornerShape(999.dp),
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .clickable(onClick = onOpenThemePicker)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Palette,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = "${activeAppTheme.displayName} • ${activeEditorScheme.displayName}",
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            softWrap = false,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }

                if (installedSdkCount < totalSdkCount) {
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(999.dp),
                        modifier = Modifier
                            .clip(RoundedCornerShape(999.dp))
                            .clickable(onClick = onInstallAllSdks)
                    ) {
                        Text(
                            text = "Enable All $totalSdkCount Compilers",
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun GuidedModeControlBanner(
    isGuidedBeginnerMode: Boolean,
    onToggleGuidedMode: () -> Unit,
    onOpenQuiz: () -> Unit
) {
    ElevatedCard(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = if (isGuidedBeginnerMode) {
                MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f)
            } else {
                MaterialTheme.colorScheme.surfaceContainerLow
            }
        ),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                0.8.dp,
                if (isGuidedBeginnerMode) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.outlineVariant,
                RoundedCornerShape(18.dp)
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.AutoStories,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.size(28.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (isGuidedBeginnerMode) {
                        "Guided Beginner Mode: Active"
                    } else {
                        "Pro IDE Mode: Distraction-Free"
                    },
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Real-life stories, plain-English error helpers & coding word dictionary stay ready while your code canvas stays spacious.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Switch(
                    checked = isGuidedBeginnerMode,
                    onCheckedChange = { onToggleGuidedMode() }
                )
                OutlinedButton(
                    onClick = onOpenQuiz,
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp)
                ) {
                    Icon(Icons.Default.Quiz, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Quiz", fontSize = 11.sp)
                }
            }
        }
    }
}

@Composable
private fun StudentWordDictionaryPreviewCard() {
    val entries = remember { ErrorPlainEnglishTranslator.globalGlossary.take(6) }
    ElevatedCard(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        modifier = Modifier
            .fillMaxWidth()
            .border(0.8.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(18.dp))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Beginner Coding Word Dictionary",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Everyday English meanings for tricky programming & Android keywords:",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            entries.forEach { term ->
                Surface(
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = term.term,
                                fontFamily = JetBrainsMonoFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "• ${term.simpleMeaning}",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Text(
                            text = "Real life: ${term.everydayExample}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun HeroWorkspaceBanner(
    adaptive: AdaptiveThemeSpec,
    shortcutLetter: Char,
    folderCount: Int,
    totalSections: Int,
    learnedConceptsCount: Int,
    installedSdkCount: Int,
    totalSdkCount: Int,
    onOpenSdkManager: () -> Unit,
    onInstallAllSdks: () -> Unit
) {
    ElevatedCard(
        shape = RoundedCornerShape(adaptive.cardCornerRadius),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = adaptive.cardElevation + 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(adaptive.cardCornerRadius))
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Image(
                painter = painterResource(id = R.drawable.img_editor_hero_1794022926039),
                contentDescription = "KtScribe CodeAssist Studio",
                contentScale = ContentScale.Crop,
                modifier = Modifier.matchParentSize()
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.surfaceContainerLowest.copy(alpha = 0.96f),
                                MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.90f),
                                MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.80f)
                            )
                        )
                    )
                    .padding(adaptive.cardPadding),
                contentAlignment = Alignment.BottomStart
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    FlowRow(
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        TrichromeStudioLogo(sizeDp = 24.dp)

                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = RoundedCornerShape(999.dp)
                        ) {
                            Text(
                                text = "● KT COMPILER + VM",
                                fontFamily = JetBrainsMonoFontFamily,
                                fontSize = 9.5.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                fontWeight = FontWeight.ExtraBold,
                                maxLines = 1,
                                softWrap = false,
                                modifier = Modifier.padding(horizontal = 9.dp, vertical = 3.dp)
                            )
                        }

                        Surface(
                            color = MaterialTheme.colorScheme.tertiaryContainer,
                            shape = RoundedCornerShape(999.dp),
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
                                    tint = MaterialTheme.colorScheme.tertiary,
                                    modifier = Modifier.size(11.dp)
                                )
                                Text(
                                    text = "$installedSdkCount/$totalSdkCount Engines Active",
                                    fontFamily = JetBrainsMonoFontFamily,
                                    fontSize = 9.5.sp,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                        }
                    }

                    Text(
                        text = "Interactive Lessons • Real-Life Stories • Compose & XML Preview",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Terminal,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "$folderCount Projects • $totalSections Files • $learnedConceptsCount Concepts • Ctrl+$shortcutLetter",
                                fontFamily = JetBrainsMonoFontFamily,
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        if (installedSdkCount < totalSdkCount) {
                            Surface(
                                color = MaterialTheme.colorScheme.primary,
                                shape = RoundedCornerShape(999.dp),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(999.dp))
                                    .clickable(onClick = onInstallAllSdks)
                            ) {
                                Text(
                                    text = "Enable All",
                                    fontFamily = JetBrainsMonoFontFamily,
                                    fontSize = 9.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    softWrap = false,
                                    color = MaterialTheme.colorScheme.onPrimary,
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
private fun EmptyDocumentsCard(
    onCreateWizard: () -> Unit,
    onSwitchToLearn: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                imageVector = Icons.Default.FolderSpecial,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(42.dp)
            )
            Text(
                text = stringResource(R.string.empty_documents_title),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = stringResource(R.string.empty_documents_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(onClick = onCreateWizard) {
                    Text("Create Project")
                }
                OutlinedButton(onClick = onSwitchToLearn) {
                    Text("Browse Lessons")
                }
            }
        }
    }
}

@Composable
private fun StudentLearnHubCard(
    adaptive: AdaptiveThemeSpec,
    conceptMastery: List<ConceptMasteryState>,
    lessonProgressById: Map<String, LessonProgressState>,
    onStartLesson: (CodingLesson) -> Unit,
    onOpenQuiz: (LearningTrack?) -> Unit
) {
    var selectedTrack by remember { mutableStateOf<LearningTrack?>(null) }
    val lessons = remember(selectedTrack) {
        if (selectedTrack == null) StudentCurriculumCatalog.lessons
        else StudentCurriculumCatalog.lessons.filter { it.track == selectedTrack }
    }
    val totalXp = remember(conceptMastery) { conceptMastery.sumOf { it.xp } }
    val completedCount = remember(lessonProgressById) { lessonProgressById.values.count { it.isCompleted } }
    val totalLessonCount = StudentCurriculumCatalog.lessons.size

    ElevatedCard(
        shape = RoundedCornerShape(adaptive.cardCornerRadius),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = adaptive.cardElevation),
        modifier = Modifier
            .fillMaxWidth()
            .border(0.8.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(adaptive.cardCornerRadius))
            .testTag("student_learn_hub_card")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(adaptive.cardPadding),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Student Learn Hub & Interactive Lessons",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "$completedCount/$totalLessonCount Lessons Completed • $totalXp XP Earned",
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 10.5.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shape = RoundedCornerShape(999.dp),
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .clickable { onOpenQuiz(selectedTrack) }
                        .testTag("home_open_quiz_button")
                ) {
                    Text(
                        text = "Quiz Me",
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        maxLines = 1,
                        softWrap = false,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }

            // Track filter row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val allSelected = selectedTrack == null
                Surface(
                    color = if (allSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHigh,
                    shape = RoundedCornerShape(999.dp),
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .clickable { selectedTrack = null }
                ) {
                    Text(
                        text = "All Tracks",
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (allSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        softWrap = false,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }

                LearningTrack.entries.forEach { track ->
                    val active = selectedTrack == track
                    Surface(
                        color = if (active) track.accentColor.copy(alpha = 0.24f) else MaterialTheme.colorScheme.surfaceContainerHigh,
                        shape = RoundedCornerShape(999.dp),
                        border = if (active) BorderStroke(0.8.dp, track.accentColor) else null,
                        modifier = Modifier
                            .clip(RoundedCornerShape(999.dp))
                            .clickable { selectedTrack = if (active) null else track }
                    ) {
                        Text(
                            text = track.shortBadge,
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (active) track.accentColor else MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            softWrap = false,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }
                }
            }

            // Lesson cards list
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                lessons.forEach { lesson ->
                    val done = lessonProgressById[lesson.id]?.isCompleted == true
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceContainer,
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(
                            width = 0.85.dp,
                            color = if (done) MaterialTheme.colorScheme.tertiary else lesson.track.accentColor.copy(alpha = 0.45f)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .clickable { onStartLesson(lesson) }
                            .testTag("start_lesson_${lesson.id}")
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${lesson.language.badgeLabel} • ${lesson.difficulty}",
                                    fontFamily = JetBrainsMonoFontFamily,
                                    fontSize = 9.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = lesson.track.accentColor
                                )
                                Text(
                                    text = if (done) "✓ Done" else "+${lesson.xpReward} XP",
                                    fontFamily = JetBrainsMonoFontFamily,
                                    fontSize = 9.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (done) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Text(
                                text = lesson.title,
                                fontFamily = JetBrainsMonoFontFamily,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = lesson.realLifeAnalogy.story,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }
}
