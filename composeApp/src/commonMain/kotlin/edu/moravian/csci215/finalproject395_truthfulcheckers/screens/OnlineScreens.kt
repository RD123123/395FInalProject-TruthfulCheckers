package edu.moravian.csci215.finalproject395_truthfulcheckers.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import edu.moravian.csci215.finalproject395_truthfulcheckers.models.TriviaCategory
import edu.moravian.csci215.finalproject395_truthfulcheckers.theme.getStrings
import edu.moravian.csci215.finalproject395_truthfulcheckers.viewmodel.GameViewModel
import edu.moravian.csci215.finalproject395_truthfulcheckers.viewmodel.OnlineGameViewModel

@Composable
fun OnlineMenuScreen(
    selectedLanguage: String,
    onCreateRoom: () -> Unit,
    onJoinRoom: () -> Unit,
) {
    val strings = getStrings(selectedLanguage)

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = strings.onlineMultiplayer,
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
        )

        Spacer(Modifier.height(32.dp))

        Button(
            onClick = onCreateRoom,
            modifier =
                Modifier
                    .fillMaxWidth(0.8f)
                    .height(56.dp),
        ) {
            Text(strings.createRoom)
        }

        Spacer(Modifier.height(16.dp))

        OutlinedButton(
            onClick = onJoinRoom,
            modifier =
                Modifier
                    .fillMaxWidth(0.8f)
                    .height(56.dp),
        ) {
            Text(strings.joinRoom)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateRoomScreen(
    gameViewModel: GameViewModel,
    onlineViewModel: OnlineGameViewModel,
    onRoomCreated: (String) -> Unit,
) {
    val gameState by gameViewModel.uiState.collectAsState()
    val onlineState by onlineViewModel.uiState.collectAsState()
    val strings = getStrings(gameState.selectedLanguage)

    var playerName by remember { mutableStateOf("") }
    var difficulty by remember { mutableStateOf("Medium") }
    var expandedCategory by remember { mutableStateOf(false) }

    LaunchedEffect(onlineState.isRoomCreated, onlineState.gameState.roomCode) {
        if (onlineState.isRoomCreated && onlineState.gameState.roomCode.isNotBlank()) {
            onRoomCreated(onlineState.gameState.roomCode)
        }
    }

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = strings.createGame,
            style = MaterialTheme.typography.headlineMedium,
        )

        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = playerName,
            onValueChange = { if (!it.contains("\n")) playerName = it },
            label = { Text(strings.yourName) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(Modifier.height(16.dp))

        Text(
            text = strings.category,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
        )

        ExposedDropdownMenuBox(
            expanded = expandedCategory,
            onExpandedChange = { expandedCategory = !expandedCategory },
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
        ) {
            OutlinedTextField(
                value = gameState.selectedCategory?.name ?: "General Knowledge",
                onValueChange = {},
                readOnly = true,
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCategory)
                },
                modifier =
                    Modifier
                        .menuAnchor()
                        .fillMaxWidth(),
            )

            ExposedDropdownMenu(
                expanded = expandedCategory,
                onDismissRequest = { expandedCategory = false },
            ) {
                gameState.categories.forEach { category: TriviaCategory ->
                    DropdownMenuItem(
                        text = { Text(category.name) },
                        onClick = {
                            gameViewModel.setCategory(category)
                            expandedCategory = false
                        },
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        Text(
            text = strings.difficulty,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
        )

        DifficultySelector(
            selected = difficulty,
            onSelected = { difficulty = it },
            strings = strings, // <-- Add this line!
        )

        Spacer(Modifier.height(24.dp))

        Button(
            onClick = {
                onlineViewModel.createRoom(
                    playerName = playerName,
                    category = gameState.selectedCategory,
                    difficulty = difficulty,
                )
            },
            enabled = playerName.isNotBlank() && !onlineState.isLoading,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(56.dp),
        ) {
            if (onlineState.isLoading) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary)
            } else {
                Text(strings.startRoom)
            }
        }

        onlineState.errorMessage?.let {
            Spacer(Modifier.height(8.dp))
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error,
            )
        }
    }
}

@Composable
fun JoinRoomScreen(
    selectedLanguage: String,
    viewModel: OnlineGameViewModel,
    onRoomJoined: (String) -> Unit,
) {
    val strings = getStrings(selectedLanguage)
    val state by viewModel.uiState.collectAsState()

    var roomCode by remember { mutableStateOf("") }
    var playerName by remember { mutableStateOf("") }

    LaunchedEffect(state.isRoomJoined, state.gameState.roomCode) {
        if (state.isRoomJoined && state.gameState.roomCode.isNotBlank()) {
            onRoomJoined(state.gameState.roomCode)
        }
    }

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = strings.joinGame,
            style = MaterialTheme.typography.headlineMedium,
        )

        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = playerName,
            onValueChange = { if (!it.contains("\n")) playerName = it },
            label = { Text(strings.yourName) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = roomCode,
            onValueChange = {
                val filtered = it.filter { char -> char.isDigit() }.take(6)
                roomCode = filtered
            },
            label = { Text(strings.roomCode) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(Modifier.height(24.dp))

        Button(
            onClick = { viewModel.joinRoom(roomCode, playerName) },
            enabled = playerName.isNotBlank() && roomCode.length == 6 && !state.isLoading,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(56.dp),
        ) {
            if (state.isLoading) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary)
            } else {
                Text(strings.joinRoom)
            }
        }

        state.errorMessage?.let {
            Spacer(Modifier.height(8.dp))
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error,
            )
        }
    }
}
