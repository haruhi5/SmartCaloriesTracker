package com.gemini.calories.data.manager

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.gemini.calories.data.local.FoodEntryDao
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CsvExportManager @Inject constructor(
    private val foodEntryDao: FoodEntryDao,
    @ApplicationContext private val context: Context
) {

    suspend fun exportToFile(userId: Long = 1L): Result<Uri> {
        return withContext(Dispatchers.IO) {
            try {
                val entries = foodEntryDao.getAllEntriesForExport(userId)
                val csvContent = buildCsvContent(entries)

                val fileName = "calories_export_${System.currentTimeMillis()}.csv"
                val file = File(context.getExternalFilesDir(null), fileName)
                file.writeText(csvContent)

                val uri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    file
                )
                Result.success(uri)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    private fun buildCsvContent(entries: List<com.gemini.calories.data.local.FoodEntryEntity>): String {
        val header = "Date,Meal Type,Food Name,Calories,Protein(g),Carbs(g),Fat(g),Portion\n"
        val rows = entries.joinToString("\n") { entry ->
            "${entry.date}," +
            "${entry.mealType}," +
            "\"${entry.foodName.replace("\"", "\"\"")}\"," +
            "${entry.calories}," +
            "${entry.protein}," +
            "${entry.carbs}," +
            "${entry.fat}," +
            "\"${entry.portion.replace("\"", "\"\"")}\""
        }
        return header + rows
    }

    fun shareFile(uri: Uri) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/csv"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(Intent.createChooser(intent, "Export Data"))
    }
}
