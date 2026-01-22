package com.gemini.calories.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
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
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "AI Analysis Engine",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = apiType == "gpt",
                    onClick = { viewModel.setApiType("gpt") },
                    label = { Text("GPT-4 Vision") }
                )
                FilterChip(
                    selected = apiType == "gemini",
                    onClick = { viewModel.setApiType("gemini") },
                    label = { Text("Gemini (Cloud)") }
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = apiType == "gemini_ondevice",
                    onClick = { viewModel.setApiType("gemini_ondevice") },
                    label = { Text("Gemini (On-device)") }
                )
            }
            
            if (apiType == "gpt") {
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                OutlinedTextField(
                    value = tempKey,
                    onValueChange = { tempKey = it },
                    label = { Text("OpenAI API Key") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Button(
                    onClick = { viewModel.saveApiKey(tempKey) },
                    modifier = Modifier.align(androidx.compose.ui.Alignment.End)
                ) {
                    Text("Save Key")
                }
            } else if (apiType == "gemini_ondevice") {
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                Text(
                    text = "On-device Gemini runs locally when supported (Pixel/Android 14+). No API key required.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                Text(
                    text = "Cloud Gemini uses your bundled API configuration. No key required here.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
                }
            }
        }
    }
}
