package com.gemini.calories.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
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
    val geminiKey by viewModel.geminiApiKey.collectAsState()
    val apiType by viewModel.apiType.collectAsState()
    var tempKey by remember(apiKey) { mutableStateOf(apiKey) }
    var tempGeminiKey by remember(geminiKey) { mutableStateOf(geminiKey) }
    var openAiKeyVisible by remember { mutableStateOf(false) }
    var geminiKeyVisible by remember { mutableStateOf(false) }
    var openAiSaved by remember { mutableStateOf(false) }
    var geminiSaved by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
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

                AiEngineOptionCard(
                    selected = apiType == "gpt",
                    title = "GPT-4 Vision",
                    subtitle = "Best quality vision model, requires OpenAI API key.",
                    onClick = { viewModel.setApiType("gpt") }
                )
                AiEngineOptionCard(
                    selected = apiType == "gemini",
                    title = "Gemini (Cloud)",
                    subtitle = "Uses cloud Gemini configuration bundled with the app.",
                    onClick = { viewModel.setApiType("gemini") }
                )
                if (apiType == "gpt") {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    OutlinedTextField(
                        value = tempKey,
                        onValueChange = {
                            tempKey = it
                            openAiSaved = false
                        },
                        label = { Text("OpenAI API Key") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        visualTransformation = if (openAiKeyVisible) {
                            VisualTransformation.None
                        } else {
                            PasswordVisualTransformation()
                        },
                        trailingIcon = {
                            IconButton(onClick = { openAiKeyVisible = !openAiKeyVisible }) {
                                Icon(
                                    imageVector = if (openAiKeyVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                    contentDescription = if (openAiKeyVisible) "Hide API key" else "Show API key"
                                )
                            }
                        }
                    )
                    Button(
                        onClick = {
                            viewModel.saveApiKey(tempKey)
                            openAiSaved = true
                        },
                        modifier = Modifier.align(androidx.compose.ui.Alignment.End)
                    ) {
                        Text("Save Key")
                    }
                    if (openAiSaved) {
                        Text(
                            text = "OpenAI key saved",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.align(androidx.compose.ui.Alignment.End)
                        )
                    }
                } else if (apiType == "gemini") {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    OutlinedTextField(
                        value = tempGeminiKey,
                        onValueChange = {
                            tempGeminiKey = it
                            geminiSaved = false
                        },
                        label = { Text("Gemini API Key") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        visualTransformation = if (geminiKeyVisible) {
                            VisualTransformation.None
                        } else {
                            PasswordVisualTransformation()
                        },
                        trailingIcon = {
                            IconButton(onClick = { geminiKeyVisible = !geminiKeyVisible }) {
                                Icon(
                                    imageVector = if (geminiKeyVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                    contentDescription = if (geminiKeyVisible) "Hide API key" else "Show API key"
                                )
                            }
                        }
                    )
                    Button(
                        onClick = {
                            viewModel.saveGeminiApiKey(tempGeminiKey)
                            geminiSaved = true
                        },
                        modifier = Modifier.align(androidx.compose.ui.Alignment.End)
                    ) {
                        Text("Save Key")
                    }
                    if (geminiSaved) {
                        Text(
                            text = "Gemini key saved",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.align(androidx.compose.ui.Alignment.End)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AiEngineOptionCard(
    selected: Boolean,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.elevatedCardColors(
            containerColor = if (selected)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(title, style = MaterialTheme.typography.titleSmall)
            Spacer(Modifier.height(4.dp))
            Text(
                subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
