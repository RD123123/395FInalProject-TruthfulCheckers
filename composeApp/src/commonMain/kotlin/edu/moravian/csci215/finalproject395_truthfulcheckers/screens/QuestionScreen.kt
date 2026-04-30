package edu.moravian.csci215.finalproject395_truthfulcheckers.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import edu.moravian.csci215.finalproject395_truthfulcheckers.theme.getStrings
import edu.moravian.csci215.finalproject395_truthfulcheckers.viewmodel.GameViewModel

@Composable
fun QuestionScreen(viewModel: GameViewModel, onAnswerSubmitted: () -> Unit) {
    val state by viewModel.uiState.collectAsState()
    val question = state.currentQuestion
    val strings = getStrings(state.selectedLanguage)

    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(strings.triviaGate, style = MaterialTheme.typography.displaySmall)
        Spacer(Modifier.height(8.dp))
        Text(strings.triviaDesc, style = MaterialTheme.typography.bodyMedium, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        
        Spacer(Modifier.height(48.dp))
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Text(
                text = question?.question ?: "No Question Available",
                modifier = Modifier.padding(24.dp),
                style = MaterialTheme.typography.headlineSmall,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }

        Spacer(Modifier.height(48.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            Button(
                onClick = { 
                    viewModel.onAnswerQuestion(true)
                    onAnswerSubmitted()
                },
                modifier = Modifier.weight(1f).height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50), contentColor = Color.White)
            ) {
                Text(strings.trueText)
            }
            Spacer(Modifier.width(16.dp))
            Button(
                onClick = { 
                    viewModel.onAnswerQuestion(false)
                    onAnswerSubmitted()
                },
                modifier = Modifier.weight(1f).height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF44336), contentColor = Color.White)
            ) {
                Text(strings.falseText)
            }
        }
    }
}
