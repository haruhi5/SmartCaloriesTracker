package com.gemini.calories.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val apiKey by viewModel.apiKey.collectAsState()
    val apiType by viewModel.apiType.collectAsState()
    var tempKey by remember(apiKey) { mutableStateOf(apiKey) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            
            Text("AI Analysis Engine", style = MaterialTheme.typography.titleMedium)
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = apiType == "gpt",
                    onClick = { viewModel.setApiType("gpt") },
                    label = { Text("GPT-4 Vision") }
                )
                FilterChip(
                    selected = apiType == "gemini",
                    onClick = { viewModel.setApiType("gemini") },
                    label = { Text("Gemini (Local)") }
                )
            }
            
            if (apiType == "gpt") {
                OutlinedTextField(
                    value = tempKey,
                    onValueChange = { tempKey = it },
                    label = { Text("OpenAI API Key") },
                    modifier = Modifier.fillMaxWidth()
                )
                Button(
                    onClick = { viewModel.saveApiKey(tempKey) },
                    modifier = Modifier.align(androidx.compose.ui.Alignment.End)
                ) {
                    Text("Save Key")
                }
            } else {
                Text(
                    text = "Local Gemini uses on-device model. No API key required.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
