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
import edu.moravian.csci215.finalproject395_truthfulcheckers.models.TriviaCategory
import edu.moravian.csci215.finalproject395_truthfulcheckers.theme.AppStrings
import edu.moravian.csci215.finalproject395_truthfulcheckers.theme.getStrings
import edu.moravian.csci215.finalproject395_truthfulcheckers.viewmodel.GameViewModel

/**
 * The configuration screen where players set up their match parameters before playing.
 * This screen collects data (names, category, difficulty) that will be saved via DataStore.
 *
 * @param viewModel The view model managing the core game state.
 * @param onStartGame Callback containing (Player1, Player2, Category, Difficulty) to initiate the match.
 */
@Composable
fun SetupScreen(
    viewModel: GameViewModel,
    onStartGame: (String, String, String, String) -> Unit,
) {
    val state by viewModel.uiState.collectAsState()
    val strings = getStrings(state.selectedLanguage)

    var p1Name by remember { mutableStateOf("") }
    var p2Name by remember { mutableStateOf("") }
    var difficulty by remember { mutableStateOf(strings.medium) }
    var expanded by remember { mutableStateOf(false) }

    BoxWithConstraints(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        val isLandscape = maxWidth > maxHeight
        val elementWidth = if (isLandscape) 0.6f else 0.9f

        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(if (isLandscape) 16.dp else 24.dp)
                    .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = if (state.isVsAi) strings.gameSetup else strings.localMatchSetup,
                style = MaterialTheme.typography.displaySmall,
                color = MaterialTheme.colorScheme.primary,
            )

            Spacer(Modifier.height(if (isLandscape) 16.dp else 24.dp))

            PlayerNameInputs(
                isVsAi = state.isVsAi,
                p1Name = p1Name,
                p2Name = p2Name,
                onP1Change = { p1Name = it },
                onP2Change = { p2Name = it },
                elementWidth = elementWidth,
                strings = strings,
            )

            Spacer(Modifier.height(24.dp))

            CategorySelectionDropdown(
                expanded = expanded,
                selectedCategory = state.selectedCategory,
                categories = state.categories,
                onExpandedChange = { expanded = it },
                onCategorySelect = { viewModel.setCategory(it) },
                elementWidth = elementWidth,
                strings = strings,
            )

            if (state.isVsAi) {
                Spacer(Modifier.height(24.dp))
                Text(text = strings.aiDifficulty, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                DifficultySelector(
                    selected = difficulty,
                    onSelected = { difficulty = it },
                    strings = strings,
                )
            }

            Spacer(Modifier.height(32.dp))

            Button(
                onClick = { onStartGame(p1Name, p2Name, state.selectedCategory?.name ?: "General", difficulty) },
                modifier = Modifier.fillMaxWidth(if (isLandscape) 0.4f else 0.8f).height(56.dp),
            ) {
                Text(strings.startBattle)
            }
        }
    }
}

/**
 * Handles the text input fields for player names.
 */
@Composable
private fun PlayerNameInputs(
    isVsAi: Boolean,
    p1Name: String,
    p2Name: String,
    onP1Change: (String) -> Unit,
    onP2Change: (String) -> Unit,
    elementWidth: Float,
    strings: AppStrings,
) {
    OutlinedTextField(
        value = p1Name,
        onValueChange = { if (!it.contains("\n")) onP1Change(it) },
        label = { Text(if (isVsAi) strings.yourName else strings.player1Name) },
        singleLine = true,
        modifier = Modifier.fillMaxWidth(elementWidth),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
    )

    Spacer(Modifier.height(8.dp))

    OutlinedTextField(
        value = p2Name,
        onValueChange = { if (!it.contains("\n")) onP2Change(it) },
        label = { Text(if (isVsAi) strings.aiBotOptional else strings.player2Name) },
        placeholder = { if (isVsAi) Text(strings.aiBotDefault) },
        singleLine = true,
        modifier = Modifier.fillMaxWidth(elementWidth),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
    )
}

/**
 * Handles the dropdown menu for selecting the trivia category.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CategorySelectionDropdown(
    expanded: Boolean,
    selectedCategory: TriviaCategory?,
    categories: List<TriviaCategory>,
    onExpandedChange: (Boolean) -> Unit,
    onCategorySelect: (TriviaCategory) -> Unit,
    elementWidth: Float,
    strings: AppStrings,
) {
    Text(text = strings.triviaCategory, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = onExpandedChange,
        modifier = Modifier.fillMaxWidth(elementWidth).padding(top = 8.dp),
    ) {
        OutlinedTextField(
            value = selectedCategory?.name ?: strings.loading,
            onValueChange = {},
            readOnly = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor().fillMaxWidth(),
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { onExpandedChange(false) },
        ) {
            categories.forEach { category ->
                DropdownMenuItem(
                    text = { Text(category.name) },
                    onClick = {
                        onCategorySelect(category)
                        onExpandedChange(false)
                    },
                )
            }
        }
    }
}

/**
 * A row of selectable chips allowing the user to pick the AI difficulty level.
 *
 * @param selected The currently selected difficulty string.
 * @param onSelected Callback triggered when a new difficulty chip is tapped.
 * @param strings The localized string resources.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DifficultySelector(
    selected: String,
    onSelected: (String) -> Unit,
    strings: AppStrings,
) {
    Row(modifier = Modifier.padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        FilterChip(selected = selected == strings.easy, onClick = { onSelected(strings.easy) }, label = { Text(strings.easy) })
        FilterChip(selected = selected == strings.medium, onClick = { onSelected(strings.medium) }, label = { Text(strings.medium) })
        FilterChip(selected = selected == strings.hard, onClick = { onSelected(strings.hard) }, label = { Text(strings.hard) })
    }
}
