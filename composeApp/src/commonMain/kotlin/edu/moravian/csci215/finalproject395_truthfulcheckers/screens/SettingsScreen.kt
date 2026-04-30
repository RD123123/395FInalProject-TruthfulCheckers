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
import androidx.compose.ui.unit.sp
import edu.moravian.csci215.finalproject395_truthfulcheckers.audio.SoundManager
import edu.moravian.csci215.finalproject395_truthfulcheckers.theme.getStrings
import edu.moravian.csci215.finalproject395_truthfulcheckers.viewmodel.GameViewModel

@Composable
fun SettingsScreen(viewModel: GameViewModel) {
    val state by viewModel.uiState.collectAsState()
    val strings = getStrings(state.selectedLanguage)

    val languages = listOf("English", "Spanish", "French")
    val themes = listOf("Warm Tan", "Dark Espresso", "Classic Blue")
    val timerOptions = listOf("Off", "10 seconds", "20 seconds", "30 seconds", "60 seconds")
    val boardStyles = listOf("Classic", "Modern", "High Contrast")

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isLandscape = maxWidth > maxHeight
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(horizontal = 24.dp, vertical = 12.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = strings.settings, 
                style = if (isLandscape) MaterialTheme.typography.headlineSmall else MaterialTheme.typography.displaySmall, 
                color = MaterialTheme.colorScheme.primary
            )
            
            Spacer(Modifier.height(if (isLandscape) 8.dp else 24.dp))

            if (isLandscape) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(32.dp)) {
                    // Column 1: Appearance & Language
                    Column(modifier = Modifier.weight(1f)) {
                        SectionLabel(strings.appearance)
                        SettingDropdown(
                            label = strings.theme,
                            options = themes,
                            selected = state.selectedTheme,
                            onOptionSelected = { viewModel.setTheme(it) }
                        )
                        Spacer(Modifier.height(8.dp))
                        SettingDropdown(
                            label = strings.boardStyle,
                            options = boardStyles,
                            selected = state.selectedBoardStyle,
                            onOptionSelected = { viewModel.setBoardStyle(it) }
                        )
                        Spacer(Modifier.height(8.dp))
                        SettingDropdown(
                            label = strings.language,
                            options = languages,
                            selected = state.selectedLanguage,
                            onOptionSelected = { viewModel.setLanguage(it) }
                        )
                    }
                    
                    // Column 2: Game Settings & Audio
                    Column(modifier = Modifier.weight(1f)) {
                        SectionLabel(strings.gameSettings)
                        SettingDropdown(
                            label = strings.turnTimer,
                            options = timerOptions,
                            selected = state.turnTimerSetting,
                            onOptionSelected = { viewModel.setTurnTimer(it) }
                        )
                        
                        Spacer(Modifier.height(16.dp))
                        SectionLabel(strings.audio)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            SettingSwitchCompact(
                                label = strings.soundEffectsShort,
                                checked = SoundManager.isSoundEnabled,
                                onCheckedChange = { SoundManager.isSoundEnabled = it },
                                modifier = Modifier.weight(1f)
                            )
                            SettingSwitchCompact(
                                label = strings.backgroundMusicShort,
                                checked = SoundManager.isMusicEnabled,
                                onCheckedChange = { SoundManager.isMusicEnabled = it },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            } else {
                // Portrait Layout
                SectionLabel(strings.appearance)
                SettingDropdown(label = strings.theme, options = themes, selected = state.selectedTheme, onOptionSelected = { viewModel.setTheme(it) })
                Spacer(Modifier.height(12.dp))
                SettingDropdown(label = strings.boardStyle, options = boardStyles, selected = state.selectedBoardStyle, onOptionSelected = { viewModel.setBoardStyle(it) })

                Spacer(Modifier.height(24.dp))

                SectionLabel(strings.gameSettings)
                SettingDropdown(label = strings.language, options = languages, selected = state.selectedLanguage, onOptionSelected = { viewModel.setLanguage(it) })
                Spacer(Modifier.height(12.dp))
                SettingDropdown(label = strings.turnTimer, options = timerOptions, selected = state.turnTimerSetting, onOptionSelected = { viewModel.setTurnTimer(it) })

                Spacer(Modifier.height(24.dp))

                SectionLabel(strings.audio)
                SettingSwitch(label = strings.soundEffects, checked = SoundManager.isSoundEnabled, onCheckedChange = { SoundManager.isSoundEnabled = it })
                SettingSwitch(label = strings.backgroundMusic, checked = SoundManager.isMusicEnabled, onCheckedChange = { SoundManager.isMusicEnabled = it })
            }
            
            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
fun SectionLabel(text: String) {
    Column {
        Text(text = text, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
        Divider(modifier = Modifier.padding(vertical = 4.dp), color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
    }
}

@Composable
fun SettingSwitch(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium)
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
fun SettingSwitchCompact(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(text = label, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
        Switch(checked = checked, onCheckedChange = onCheckedChange, modifier = Modifier.scale(0.8f))
    }
}

@Composable
fun SettingDropdown(label: String, options: List<String>, selected: String, onOptionSelected: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(4.dp))
                .clip(RoundedCornerShape(4.dp))
                .clickable { expanded = !expanded }
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = selected, style = MaterialTheme.typography.bodyLarge)
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.fillMaxWidth(0.8f)
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = {
                            onOptionSelected(option)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}
