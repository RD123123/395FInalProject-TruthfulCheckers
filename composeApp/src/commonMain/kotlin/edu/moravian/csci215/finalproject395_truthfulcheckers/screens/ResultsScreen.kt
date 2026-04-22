package edu.moravian.csci215.finalproject395_truthfulcheckers.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import edu.moravian.csci215.finalproject395_truthfulcheckers.models.PlayerColor

@Composable
fun ResultsScreen(winner: PlayerColor?, onPlayAgain: () -> Unit, onHomeClick: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Game Over!",
            style = MaterialTheme.typography.displayMedium
        )
        
        Spacer(Modifier.height(16.dp))
        
        Text(
            text = if (winner == PlayerColor.RED) "Red Wins! 🏆" else "Blue Wins! 🏆",
            style = MaterialTheme.typography.headlineMedium,
            color = if (winner == PlayerColor.RED) Color.Red else Color.Blue
        )
        
        Spacer(Modifier.height(48.dp))
        
        Button(onClick = onPlayAgain, modifier = Modifier.fillMaxWidth(0.7f)) {
            Text("Play Again")
        }
        
        Spacer(Modifier.height(16.dp))
        
        OutlinedButton(onClick = onHomeClick, modifier = Modifier.fillMaxWidth(0.7f)) {
            Text("Back to Home")
        }
    }
}
