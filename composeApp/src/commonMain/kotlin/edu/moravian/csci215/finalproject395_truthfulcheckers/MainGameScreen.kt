package edu.moravian.csci215.finalproject395_truthfulcheckers.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun MainGameScreen(onTriggerQuestion: () -> Unit, onGameEnd: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Main Game Board", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(16.dp))
        Button(onClick = onTriggerQuestion) { Text("Simulate Checker Move") }
        Spacer(Modifier.height(8.dp))
        Button(onClick = onGameEnd) { Text("Simulate Game Over") }
    }
}