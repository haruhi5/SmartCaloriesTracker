package com.gemini.calories.ui.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.gemini.calories.data.local.ActivityLevel
import com.gemini.calories.data.local.FitnessGoal
import com.gemini.calories.data.local.Gender
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavController,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val user by viewModel.currentUser.collectAsState(initial = null)
    val exportUri by viewModel.exportUri.collectAsState()

    // Form State
    // We update state when user loads
    var name by remember(user) { mutableStateOf(user?.name ?: "") }
    var height by remember(user) { mutableStateOf(user?.height?.toString() ?: "170") }
    var weight by remember(user) { mutableStateOf(user?.weight?.toString() ?: "70") }
    var age by remember(user) { mutableStateOf(user?.age?.toString() ?: "25") }
    var gender by remember(user) { mutableStateOf(user?.gender ?: Gender.MALE) }
    var activityLevel by remember(user) { mutableStateOf(user?.activityLevel ?: ActivityLevel.MODERATE) }
    var goal by remember(user) { mutableStateOf(user?.goal ?: FitnessGoal.MAINTAIN) }

    // Effect to share file when exported
    LaunchedEffect(exportUri) {
        exportUri?.let { viewModel.shareExportedFile(it) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.exportData() }) {
                        Icon(Icons.Default.Share, "Export CSV")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Name") },
                modifier = Modifier.fillMaxWidth()
            )

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = height,
                    onValueChange = { height = it },
                    label = { Text("Height (cm)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = weight,
                    onValueChange = { weight = it },
                    label = { Text("Weight (kg)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
            }
            
            OutlinedTextField(
                value = age,
                onValueChange = { age = it },
                label = { Text("Age") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            // Gender Selection
            Text("Gender", style = MaterialTheme.typography.titleMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Gender.entries.forEach { g ->
                    FilterChip(
                        selected = gender == g,
                        onClick = { gender = g },
                        label = { Text(g.name.lowercase().capitalize(Locale.ROOT)) }
                    )
                }
            }

            // Activity Level
            Text("Activity Level", style = MaterialTheme.typography.titleMedium)
            // Use a Dropdown or simple Column of RadioButtons. For simplicity: Chips wrapping?
            // Activity levels names are long. Let's use a simpler selector or just vertical RadioButtons.
            Column {
                ActivityLevel.entries.forEach { level ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = activityLevel == level,
                            onClick = { activityLevel = level }
                        )
                        Text(
                            text = level.name.replace("_", " ").lowercase().capitalize(Locale.ROOT),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            // Goal
            Text("Goal", style = MaterialTheme.typography.titleMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FitnessGoal.entries.forEach { g ->
                    FilterChip(
                        selected = goal == g,
                        onClick = { goal = g },
                        label = { Text(g.name.replace("_", " ").lowercase().capitalize(Locale.ROOT)) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = {
                    viewModel.saveProfile(
                        name,
                        height.toFloatOrNull() ?: 0f,
                        weight.toFloatOrNull() ?: 0f,
                        age.toIntOrNull() ?: 0,
                        gender,
                        activityLevel,
                        goal
                    )
                    navController.navigateUp()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save Profile")
            }
        }
    }
}
