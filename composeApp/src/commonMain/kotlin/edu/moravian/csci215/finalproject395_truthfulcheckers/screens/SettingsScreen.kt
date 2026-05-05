package edu.moravian.csci215.finalproject395_truthfulcheckers.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.unit.dp
import edu.moravian.csci215.finalproject395_truthfulcheckers.audio.SoundManager
import edu.moravian.csci215.finalproject395_truthfulcheckers.theme.AppStrings
import edu.moravian.csci215.finalproject395_truthfulcheckers.theme.getStrings
import edu.moravian.csci215.finalproject395_truthfulcheckers.viewmodel.GameViewModel
import org.koin.compose.koinInject

/**
 * The configuration screen where players can adjust the visual and audio settings
 * of the application, including language, theme, and turn timers.
 *
 * @param viewModel The shared view model managing the core game state and settings.
 */
@Composable
fun SettingsScreen(viewModel: GameViewModel) {
    val state by viewModel.uiState.collectAsState()
    val strings = getStrings(state.selectedLanguage)
    val soundManager: SoundManager = koinInject()

    val languages = listOf("English", "Spanish", "French")
    val themes = listOf("Warm Tan", "Dark Espresso", "Classic Blue")
    val timerOptions = listOf("Off", "10 seconds", "20 seconds", "30 seconds", "60 seconds")
    val boardStyles = listOf("Classic", "Modern", "High Contrast")

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isLandscape = maxWidth > maxHeight

        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(horizontal = 24.dp, vertical = 12.dp)
                    .verticalScroll(rememberScrollState()),
        ) {
            Text(
                text = strings.settings,
                style = if (isLandscape) MaterialTheme.typography.headlineSmall else MaterialTheme.typography.displaySmall,
                color = MaterialTheme.colorScheme.primary,
            )

            Spacer(Modifier.height(if (isLandscape) 8.dp else 24.dp))

            if (isLandscape) {
                LandscapeSettingsLayout(viewModel, soundManager, strings, languages, themes, timerOptions, boardStyles)
            } else {
                PortraitSettingsLayout(viewModel, soundManager, strings, languages, themes, timerOptions, boardStyles)
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

/**
 * Helper composable that arranges the settings options in a two-column layout for landscape orientation.
 */
@Composable
private fun LandscapeSettingsLayout(
    viewModel: GameViewModel,
    soundManager: SoundManager,
    strings: AppStrings,
    languages: List<String>,
    themes: List<String>,
    timerOptions: List<String>,
    boardStyles: List<String>,
) {
    val state by viewModel.uiState.collectAsState()

    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(32.dp)) {
        Column(modifier = Modifier.weight(1f)) {
            SectionLabel(strings.appearance)
            SettingDropdown(
                label = strings.theme,
                options = themes,
                selected = state.selectedTheme,
                onOptionSelected = { viewModel.setTheme(it) },
            )
            Spacer(Modifier.height(8.dp))
            SettingDropdown(label = strings.boardStyle, options = boardStyles, selected = state.selectedBoardStyle, onOptionSelected = {
                viewModel.setBoardStyle(it)
            })
            Spacer(Modifier.height(8.dp))
            SettingDropdown(label = strings.language, options = languages, selected = state.selectedLanguage, onOptionSelected = {
                viewModel.setLanguage(it)
            })
        }

        Column(modifier = Modifier.weight(1f)) {
            SectionLabel(strings.gameSettings)
            SettingDropdown(label = strings.turnTimer, options = timerOptions, selected = state.turnTimerSetting, onOptionSelected = {
                viewModel.setTurnTimer(it)
            })

            Spacer(Modifier.height(16.dp))
            SectionLabel(strings.audio)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                SettingSwitchCompact(
                    label = strings.soundEffectsShort,
                    checked = soundManager.isSoundEnabled,
                    onCheckedChange = { soundManager.toggleSound(it) },
                    modifier = Modifier.weight(1f),
                )
                SettingSwitchCompact(
                    label = strings.backgroundMusicShort,
                    checked = soundManager.isMusicEnabled,
                    onCheckedChange = { soundManager.toggleMusic(it) },
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

/**
 * Helper composable that arranges the settings options in a single-column layout for portrait orientation.
 */
@Composable
private fun PortraitSettingsLayout(
    viewModel: GameViewModel,
    soundManager: SoundManager,
    strings: AppStrings,
    languages: List<String>,
    themes: List<String>,
    timerOptions: List<String>,
    boardStyles: List<String>,
) {
    val state by viewModel.uiState.collectAsState()

    SectionLabel(strings.appearance)
    SettingDropdown(label = strings.theme, options = themes, selected = state.selectedTheme, onOptionSelected = { viewModel.setTheme(it) })
    Spacer(Modifier.height(12.dp))
    SettingDropdown(label = strings.boardStyle, options = boardStyles, selected = state.selectedBoardStyle, onOptionSelected = {
        viewModel.setBoardStyle(it)
    })

    Spacer(Modifier.height(24.dp))

    SectionLabel(strings.gameSettings)
    SettingDropdown(label = strings.language, options = languages, selected = state.selectedLanguage, onOptionSelected = {
        viewModel.setLanguage(it)
    })
    Spacer(Modifier.height(12.dp))
    SettingDropdown(label = strings.turnTimer, options = timerOptions, selected = state.turnTimerSetting, onOptionSelected = {
        viewModel.setTurnTimer(it)
    })

    Spacer(Modifier.height(24.dp))

    SectionLabel(strings.audio)
    SettingSwitch(label = strings.soundEffects, checked = soundManager.isSoundEnabled, onCheckedChange = { soundManager.toggleSound(it) })
    SettingSwitch(
        label = strings.backgroundMusic,
        checked = soundManager.isMusicEnabled,
        onCheckedChange = { soundManager.toggleMusic(it) },
    )
}

/**
 * Renders a visually distinct header to group related settings.
 *
 * @param text The title of the settings category.
 */
@Composable
fun SectionLabel(text: String) {
    Column {
        Text(text = text, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
    }
}

/**
 * A standard toggle switch for enabling or disabling a setting.
 *
 * @param label The text describing the setting.
 * @param checked The current on/off state.
 * @param onCheckedChange Callback triggered when the switch is flipped.
 */
@Composable
fun SettingSwitch(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium)
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

/**
 * A smaller version of the toggle switch, intended for side-by-side placement.
 */
@Composable
fun SettingSwitchCompact(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(text = label, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
        Switch(checked = checked, onCheckedChange = onCheckedChange, modifier = Modifier.scale(0.8f))
    }
}

/**
 * A dropdown menu component for selecting a single value from a list of options.
 *
 * @param label The descriptive text above the dropdown.
 * @param options The list of strings the user can select from.
 * @param selected The currently chosen option.
 * @param onOptionSelected Callback triggered when a new option is tapped.
 */
@Composable
fun SettingDropdown(
    label: String,
    options: List<String>,
    selected: String,
    onOptionSelected: (String) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.height(4.dp))
        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(4.dp))
                    .clip(RoundedCornerShape(4.dp))
                    .clickable { expanded = !expanded }
                    .padding(horizontal = 16.dp),
            contentAlignment = Alignment.CenterStart,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(text = selected, style = MaterialTheme.typography.bodyLarge)
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.fillMaxWidth(0.8f),
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = {
                            onOptionSelected(option)
                            expanded = false
                        },
                    )
                }
            }
        }
    }
}
