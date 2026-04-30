package edu.moravian.csci215.finalproject395_truthfulcheckers.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import edu.moravian.csci215.finalproject395_truthfulcheckers.theme.getStrings
import edu.moravian.csci215.finalproject395_truthfulcheckers.viewmodel.GameViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetupScreen(viewModel: GameViewModel, onStartGame: (String, String, String, String) -> Unit) {
    val state by viewModel.uiState.collectAsState()
    val strings = getStrings(state.selectedLanguage)
    
    var p1Name by remember { mutableStateOf("") }
    var p2Name by remember { mutableStateOf("") }
    var difficulty by remember { mutableStateOf("Medium") }

    BoxWithConstraints(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        val isLandscape = maxWidth > maxHeight
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(if (isLandscape) 16.dp else 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = strings.gameSetup, 
                style = MaterialTheme.typography.displaySmall, 
                color = MaterialTheme.colorScheme.primary
            )
            
            Spacer(Modifier.height(if (isLandscape) 16.dp else 32.dp))

            if (isLandscape) {
                Row(
                    modifier = Modifier.fillMaxWidth(), 
                    horizontalArrangement = Arrangement.spacedBy(24.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Column 1: Names
                    Column(modifier = Modifier.weight(1.2f)) {
                        OutlinedTextField(
                            value = p1Name,
                            onValueChange = { if (!it.contains("\n")) p1Name = it },
                            label = { Text(strings.p1Name) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                        )
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(
                            value = p2Name,
                            onValueChange = { if (!it.contains("\n")) p2Name = it },
                            label = { Text(strings.p2Name) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done)
                        )
                    }
                    
                    // Column 2: Difficulty & Button
                    Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = strings.aiDifficulty, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                        DifficultySelector(difficulty, { difficulty = it })
                        
                        Spacer(Modifier.height(24.dp))
                        
                        Button(
                            onClick = { onStartGame(p1Name, p2Name, "General", difficulty) },
                            modifier = Modifier.fillMaxWidth().height(56.dp)
                        ) {
                            Text(strings.startBattle)
                        }
                    }
                }
            } else {
                // Portrait Layout
                OutlinedTextField(
                    value = p1Name,
                    onValueChange = { if (!it.contains("\n")) p1Name = it },
                    label = { Text(strings.p1Name) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(0.9f),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                )
                Spacer(Modifier.height(16.dp))
                OutlinedTextField(
                    value = p2Name,
                    onValueChange = { if (!it.contains("\n")) p2Name = it },
                    label = { Text(strings.p2Name) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(0.9f),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done)
                )
                
                Spacer(Modifier.height(32.dp))
                Text(text = strings.aiDifficulty, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                DifficultySelector(difficulty, { difficulty = it })
                
                Spacer(Modifier.height(48.dp))
                
                Button(
                    onClick = { onStartGame(p1Name, p2Name, "General", difficulty) },
                    modifier = Modifier.fillMaxWidth(0.8f).height(56.dp)
                ) {
                    Text(strings.startBattle)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DifficultySelector(selected: String, onSelected: (String) -> Unit) {
    Row(modifier = Modifier.padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        FilterChip(selected = selected == "Easy", onClick = { onSelected("Easy") }, label = { Text("Easy") })
        FilterChip(selected = selected == "Medium", onClick = { onSelected("Medium") }, label = { Text("Medium") })
        FilterChip(selected = selected == "Hard", onClick = { onSelected("Hard") }, label = { Text("Hard") })
    }
}
