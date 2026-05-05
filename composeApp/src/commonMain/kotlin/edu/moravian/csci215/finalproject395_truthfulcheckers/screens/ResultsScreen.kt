package edu.moravian.csci215.finalproject395_truthfulcheckers.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import edu.moravian.csci215.finalproject395_truthfulcheckers.models.PlayerColor
import edu.moravian.csci215.finalproject395_truthfulcheckers.theme.getStrings

/**
 * Displays the final outcome of a Checkers match.
 * Shows the winner's name and color, or declares a tie, and provides
 * navigation options to either restart the match or return to the main menu.
 *
 * @param winnerName The display name of the player who won the match.
 * @param winnerColor The piece color of the winning player, used to colorize the victory text. Null if the match was a tie.
 * @param selectedLanguage The current language selection used to pull the correct localized strings.
 * @param onPlayAgain Callback triggered to immediately start a new match with the previous settings.
 * @param onHomeClick Callback triggered to navigate the user back to the Home screen.
 */
@Composable
fun ResultsScreen(
    winnerName: String,
    winnerColor: PlayerColor?,
    selectedLanguage: String,
    onPlayAgain: () -> Unit,
    onHomeClick: () -> Unit,
) {
    val strings = getStrings(selectedLanguage)

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = strings.gameOver,
            style = MaterialTheme.typography.displayMedium,
            color = MaterialTheme.colorScheme.primary,
        )

        Spacer(Modifier.height(16.dp))

        Text(
            text = if (winnerColor != null) "$winnerName ${strings.wins}" else strings.tie,
            style = MaterialTheme.typography.headlineMedium,
            color =
                when (winnerColor) {
                    PlayerColor.RED -> Color.Red
                    PlayerColor.BLUE -> Color.Blue
                    else -> MaterialTheme.colorScheme.onBackground
                },
        )

        Spacer(Modifier.height(48.dp))

        Button(
            onClick = onPlayAgain,
            modifier = Modifier.fillMaxWidth(0.7f).height(56.dp),
        ) {
            Text(strings.playAgain)
        }

        Spacer(Modifier.height(16.dp))

        Button(
            onClick = onHomeClick,
            modifier = Modifier.fillMaxWidth(0.7f).height(56.dp),
            colors =
                ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                ),
        ) {
            Text(strings.backHome)
        }
    }
}
