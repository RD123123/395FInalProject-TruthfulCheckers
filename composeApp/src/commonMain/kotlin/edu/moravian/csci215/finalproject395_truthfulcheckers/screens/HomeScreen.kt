package edu.moravian.csci215.finalproject395_truthfulcheckers.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun HomeScreen(
    onStartClick: () -> Unit,
    onInstructionsClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Truthful Checkers",
            style = MaterialTheme.typography.displayMedium,
            color = MaterialTheme.colorScheme.primary
        )
        
        Spacer(Modifier.height(48.dp))
        
        Button(
            onClick = onStartClick,
            modifier = Modifier.fillMaxWidth(0.7f).height(56.dp)
        ) {
            Text("Play Game")
        }
        
        Spacer(Modifier.height(16.dp))
        
        OutlinedButton(
            onClick = onInstructionsClick,
            modifier = Modifier.fillMaxWidth(0.7f).height(56.dp)
        ) {
            Text("How to Play")
        }
        
        Spacer(Modifier.height(32.dp))
        
        IconButton(onClick = onSettingsClick) {
            Icon(Icons.Default.Settings, contentDescription = "Settings")
        }
    }
}
