package edu.moravian.csci215.finalproject395_truthfulcheckers.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetupScreen(onStartGame: (String, String, String, String) -> Unit) {
    var p1Name by remember { mutableStateOf("Player 1") }
    var p2Name by remember { mutableStateOf("Player 2") }
    var category by remember { mutableStateOf("General") }
    var difficulty by remember { mutableStateOf("Medium") }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Game Setup", style = MaterialTheme.typography.headlineLarge)
        
        Spacer(Modifier.height(24.dp))
        
        TextField(value = p1Name, onValueChange = { p1Name = it }, label = { Text("Player 1 Name") })
        Spacer(Modifier.height(8.dp))
        TextField(value = p2Name, onValueChange = { p2Name = it }, label = { Text("Player 2 Name") })
        
        Spacer(Modifier.height(16.dp))
        
        Text("Trivia Settings", style = MaterialTheme.typography.titleMedium)
        // Simple selection for demo
        Row {
            FilterChip(selected = difficulty == "Easy", onClick = { difficulty = "Easy" }, label = { Text("Easy") })
            Spacer(Modifier.width(8.dp))
            FilterChip(selected = difficulty == "Medium", onClick = { difficulty = "Medium" }, label = { Text("Medium") })
            Spacer(Modifier.width(8.dp))
            FilterChip(selected = difficulty == "Hard", onClick = { difficulty = "Hard" }, label = { Text("Hard") })
        }

        Spacer(Modifier.height(32.dp))
        
        Button(
            onClick = { onStartGame(p1Name, p2Name, category, difficulty) },
            modifier = Modifier.fillMaxWidth(0.7f)
        ) {
            Text("Start Battle")
        }
    }
}
