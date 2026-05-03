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
    var expanded by remember { mutableStateOf(false) }

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
                text = if (state.isVsAi) strings.gameSetup else "Local Match Setup", 
                style = MaterialTheme.typography.displaySmall, 
                color = MaterialTheme.colorScheme.primary
            )
            
            Spacer(Modifier.height(if (isLandscape) 16.dp else 24.dp))

            // Player 1 Name
            OutlinedTextField(
                value = p1Name,
                onValueChange = { if (!it.contains("\n")) p1Name = it },
                label = { Text(if (state.isVsAi) "Your Name" else "Player 1 Name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(if (isLandscape) 0.6f else 0.9f),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
            )
            
            Spacer(Modifier.height(8.dp))
            
            // Player 2 / AI Name
            OutlinedTextField(
                value = p2Name,
                onValueChange = { if (!it.contains("\n")) p2Name = it },
                label = { Text(if (state.isVsAi) "AI Bot Name (Optional)" else "Player 2 Name") },
                placeholder = { if (state.isVsAi) Text("AI Bot") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(if (isLandscape) 0.6f else 0.9f),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done)
            )

            Spacer(Modifier.height(24.dp))

            // Category Selection
            Text(text = "Trivia Category", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded },
                modifier = Modifier.fillMaxWidth(if (isLandscape) 0.6f else 0.9f).padding(top = 8.dp)
            ) {
                OutlinedTextField(
                    value = state.selectedCategory?.name ?: "Loading...",
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    state.categories.forEach { category ->
                        DropdownMenuItem(
                            text = { Text(category.name) },
                            onClick = {
                                viewModel.setCategory(category)
                                expanded = false
                            }
                        )
                    }
                }
            }

            // Hide Difficulty for Local Multiplayer
            if (state.isVsAi) {
                Spacer(Modifier.height(24.dp))
                Text(text = strings.aiDifficulty, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                DifficultySelector(difficulty, { difficulty = it })
            }
            
            Spacer(Modifier.height(32.dp))
            
            Button(
                onClick = { onStartGame(p1Name, p2Name, state.selectedCategory?.name ?: "General", difficulty) },
                modifier = Modifier.fillMaxWidth(if (isLandscape) 0.4f else 0.8f).height(56.dp)
            ) {
                Text(strings.startBattle)
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
