package edu.moravian.csci215.finalproject395_truthfulcheckers.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import edu.moravian.csci215.finalproject395_truthfulcheckers.theme.getStrings
import edu.moravian.csci215.finalproject395_truthfulcheckers.viewmodel.GameViewModel

/**
 * The initial landing screen of the Truthful Checkers application.
 * Provides navigation to start a game, view instructions, or configure settings.
 *
 * @param viewModel The shared view model managing the application state and language.
 * @param onStartClick Callback triggered when the user chooses to play a game.
 * @param onInstructionsClick Callback triggered when the user wants to read the rules.
 * @param onSettingsClick Callback triggered to navigate to the app settings menu.
 */
@Composable
fun HomeScreen(
    viewModel: GameViewModel,
    onStartClick: () -> Unit,
    onInstructionsClick: () -> Unit,
    onSettingsClick: () -> Unit,
) {
    val state by viewModel.uiState.collectAsState()
    val strings = getStrings(state.selectedLanguage)

    BoxWithConstraints(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        val isLandscape = maxWidth > maxHeight

        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = strings.appName,
                style = if (isLandscape) MaterialTheme.typography.displaySmall else MaterialTheme.typography.displayMedium,
                color = MaterialTheme.colorScheme.primary,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            )

            Spacer(Modifier.height(if (isLandscape) 24.dp else 48.dp))

            // Adjust the button layout based on the device orientation
            if (isLandscape) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    HomeButton(strings.playGame, onStartClick, Modifier.weight(1f))
                    Spacer(Modifier.width(16.dp))
                    HomeButton(strings.howToPlay, onInstructionsClick, Modifier.weight(1f))
                }
            } else {
                HomeButton(strings.playGame, onStartClick, Modifier.fillMaxWidth(0.7f))
                Spacer(Modifier.height(16.dp))
                HomeButton(strings.howToPlay, onInstructionsClick, Modifier.fillMaxWidth(0.7f))
            }

            Spacer(Modifier.height(if (isLandscape) 16.dp else 32.dp))

            IconButton(onClick = onSettingsClick) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = strings.settings,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(if (isLandscape) 32.dp else 48.dp),
                )
            }
        }
    }
}

/**
 * A reusable, styled button component specifically designed for the Home Screen navigation options.
 *
 * @param text The localized string to display on the button.
 * @param onClick The action to execute when the button is pressed.
 * @param modifier The layout modifier to apply to the button.
 */
@Composable
fun HomeButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(56.dp),
        colors =
            ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            ),
    ) {
        Text(text)
    }
}
