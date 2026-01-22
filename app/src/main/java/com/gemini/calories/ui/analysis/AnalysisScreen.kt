package com.gemini.calories.ui.analysis

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.PickVisualMediaRequest
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.gemini.calories.data.local.MealType
import com.gemini.calories.domain.model.FoodAnalysisResult
import java.io.ByteArrayOutputStream
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import android.content.Context

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalysisScreen(
    navController: NavController,
    viewModel: AnalysisViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val selectedUri by viewModel.selectedImageUri.collectAsState()
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    // Camera Logic
    var tempCameraUri by remember { mutableStateOf<Uri?>(null) }
    
    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success && tempCameraUri != null) {
            viewModel.onImageSelected(tempCameraUri!!)
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            viewModel.onImageSelected(uri)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Analyze Food") },
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
                .padding(padding)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Image Preview area
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    if (selectedUri != null) {
                        AsyncImage(
                            model = selectedUri,
                            contentDescription = "Selected Image",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Text(
                            text = "Select an image to analyze",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Input Buttons
            if (uiState is AnalysisUiState.Idle) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    FilledTonalButton(
                        onClick = {
                            val uri = createTempImageUri(context)
                            tempCameraUri = uri
                            cameraLauncher.launch(uri)
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.CameraAlt, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Camera")
                    }
                    FilledTonalButton(
                        onClick = { 
                            galleryLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Image, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Gallery")
                    }
                }
            }

            // Analyze Action
            if (selectedUri != null && uiState is AnalysisUiState.Idle) {
                Button(
                    onClick = {
                        val bytes = uriToBytes(context, selectedUri!!)
                        if (bytes != null) {
                            viewModel.analyzeImage(bytes)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Text("Analyze Calories")
                }
            }

            // State Handling
            when (val state = uiState) {
                is AnalysisUiState.Loading -> {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator(
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "AI is analyzing your food...",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
                is AnalysisUiState.Success -> {
                    ResultView(result = state.result, onSave = { mealType ->
                        viewModel.saveResult(mealType)
                        navController.navigateUp()
                    })
                }
                is AnalysisUiState.Error -> {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Error: ${state.message}",
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            FilledTonalButton(onClick = { viewModel.reset() }) {
                                Text("Try Again")
                            }
                        }
                    }
                }
                else -> {}
            }
        }
    }
}

@Composable
fun ResultView(result: FoodAnalysisResult, onSave: (MealType) -> Unit) {
    var selectedMealType by remember { mutableStateOf(MealType.LUNCH) }

    Card(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "Analysis Result",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            result.foods.forEach { food ->
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                ) {
                    Column {
                        Text(food.name, fontWeight = FontWeight.Medium)
                        Text(food.portion, style = MaterialTheme.typography.bodySmall)
                    }
                    Text("${food.calories} kcal", fontWeight = FontWeight.Bold)
                }
                Divider()
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Total", style = MaterialTheme.typography.titleMedium)
                Text("${result.totalCalories} kcal", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            Text("Select Meal Type:", style = MaterialTheme.typography.labelLarge)
            Row(horizontalArrangement = Arrangement.SpaceEvenly, modifier = Modifier.fillMaxWidth()) {
                MealType.entries.forEach { type ->
                    FilterChip(
                        selected = selectedMealType == type,
                        onClick = { selectedMealType = type },
                        label = { Text(type.name.lowercase().capitalize(Locale.ROOT)) }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = { onSave(selectedMealType) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Check, null)
                Spacer(Modifier.width(8.dp))
                Text("Save Record")
            }
        }
    }
}

private fun createTempImageUri(context: Context): Uri {
    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
    val imageFileName = "JPEG_" + timeStamp + "_"
    val storageDir = context.getExternalFilesDir("my_images")
    val image = File.createTempFile(imageFileName, ".jpg", storageDir)
    return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", image)
}

private fun uriToBytes(context: Context, uri: Uri): ByteArray? {
    return try {
        val inputStream = context.contentResolver.openInputStream(uri)
        val bitmap = BitmapFactory.decodeStream(inputStream)
        val outputStream = ByteArrayOutputStream()
        // Compress Image to reduce size for API
        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, outputStream)
        outputStream.toByteArray()
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}
