package edu.moravian.csci215.finalproject395_truthfulcheckers.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import edu.moravian.csci215.finalproject395_truthfulcheckers.theme.getStrings
import edu.moravian.csci215.finalproject395_truthfulcheckers.viewmodel.GameViewModel

@Composable
fun GameModeScreen(
    viewModel: GameViewModel, 
    onModeSelected: (Boolean) -> Unit,
    onOnlineSelected: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val strings = getStrings(state.selectedLanguage)

    BoxWithConstraints(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        val isLandscape = maxWidth > maxHeight
        
        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(strings.selectMode, style = MaterialTheme.typography.headlineLarge, color = MaterialTheme.colorScheme.primary)
            
            Spacer(Modifier.height(if (isLandscape) 24.dp else 48.dp))
            
            if (isLandscape) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    ModeButton(strings.vsAi, { onModeSelected(true) }, Modifier.weight(1f))
                    Spacer(Modifier.width(16.dp))
                    ModeButton(strings.localMulti, { onModeSelected(false) }, Modifier.weight(1f))
                }
            } else {
                ModeButton(strings.vsAi, { onModeSelected(true) }, Modifier.fillMaxWidth(0.7f))
                Spacer(Modifier.height(16.dp))
                ModeButton(strings.localMulti, { onModeSelected(false) }, Modifier.fillMaxWidth(0.7f))
            }

            Spacer(Modifier.height(24.dp))
            
            // Online Beta Button
            Button(
                onClick = onOnlineSelected,
                modifier = if (isLandscape) Modifier.fillMaxWidth(0.5f) else Modifier.fillMaxWidth(0.7f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary,
                    contentColor = MaterialTheme.colorScheme.onSecondary
                )
            ) {
                Text("Online Multiplayer (BETA coming soon)")
            }
        }
    }
}

@Composable
fun ModeButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Button(
        onClick = onClick,
        modifier = modifier.height(56.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        )
    ) {
        Text(text)
    }
}
