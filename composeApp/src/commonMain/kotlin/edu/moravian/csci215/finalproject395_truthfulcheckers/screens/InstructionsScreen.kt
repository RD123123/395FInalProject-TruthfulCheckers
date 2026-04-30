package edu.moravian.csci215.finalproject395_truthfulcheckers.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import edu.moravian.csci215.finalproject395_truthfulcheckers.theme.getStrings
import edu.moravian.csci215.finalproject395_truthfulcheckers.viewmodel.GameViewModel

@Composable
fun InstructionsScreen(viewModel: GameViewModel) {
    val state by viewModel.uiState.collectAsState()
    val strings = getStrings(state.selectedLanguage)

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isLandscape = maxWidth > maxHeight
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = strings.howToPlay, 
                style = MaterialTheme.typography.displaySmall, 
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(16.dp))
            
            if (isLandscape) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.weight(1f)) {
                        InstructionSection(strings.goalTitle, strings.goalDesc)
                        InstructionSection(strings.movingTitle, strings.movingDesc)
                    }
                    Spacer(Modifier.width(24.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        InstructionSection(strings.triviaTitle, strings.triviaDesc)
                        InstructionSection(strings.emotionsTitle, strings.emotionsDesc)
                        InstructionSection(strings.customTitle, strings.customDesc)
                    }
                }
            } else {
                InstructionSection(strings.goalTitle, strings.goalDesc)
                InstructionSection(strings.movingTitle, strings.movingDesc)
                InstructionSection(strings.triviaTitle, strings.triviaDesc)
                InstructionSection(strings.emotionsTitle, strings.emotionsDesc)
                InstructionSection(strings.customTitle, strings.customDesc)
            }
            
            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
fun InstructionSection(title: String, body: String) {
    Column(modifier = Modifier.padding(vertical = 12.dp)) {
        Text(title, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
        Text(body, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onBackground)
    }
}
