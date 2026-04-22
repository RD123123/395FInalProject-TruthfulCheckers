package edu.moravian.csci215.finalproject395_truthfulcheckers.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun GameModeScreen(onModeSelected: (Boolean) -> Unit, onBack: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Select Game Mode", style = MaterialTheme.typography.headlineLarge)
        
        Spacer(Modifier.height(48.dp))
        
        Button(
            onClick = { onModeSelected(true) },
            modifier = Modifier.fillMaxWidth(0.7f).height(56.dp)
        ) {
            Text("Single Player (vs AI)")
        }
        
        Spacer(Modifier.height(16.dp))
        
        Button(
            onClick = { onModeSelected(false) },
            modifier = Modifier.fillMaxWidth(0.7f).height(56.dp)
        ) {
            Text("Local Multiplayer")
        }
        
        Spacer(Modifier.height(32.dp))
        
        TextButton(onClick = onBack) {
            Text("Back")
        }
    }
}
