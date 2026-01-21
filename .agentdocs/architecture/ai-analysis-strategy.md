# AI 分析策略 (AI Analysis Strategy)

## 概述

Smart Calories Tracker 采用**双模式 AI 分析架构**，支持云端 GPT-4 Vision API 和本地 Gemini 模型两种方式进行食物图像识别和卡路里估算。用户可根据网络环境和隐私需求自由切换。

## 核心策略: 统一分析接口

### 原理

为了兼容多种 AI 模型，我们定义了统一的分析接口 `FoodAnalyzer`，不同模型实现该接口即可无缝切换。

### 接口设计

```kotlin
interface FoodAnalyzer {
    /**
     * 分析食物图片并返回识别结果
     * @param imageData 图片的 Base64 编码或文件路径
     * @return 食物分析结果列表
     */
    suspend fun analyze(imageData: ByteArray): Result<FoodAnalysisResult>
}

data class FoodAnalysisResult(
    val foods: List<FoodItem>,
    val totalCalories: Int,
    val confidence: Float
)

data class FoodItem(
    val name: String,           // 食物名称
    val calories: Int,          // 卡路里
    val protein: Float,         // 蛋白质 (g)
    val carbs: Float,           // 碳水化合物 (g)
    val fat: Float,             // 脂肪 (g)
    val portion: String,        // 份量描述
    val confidence: Float       // 识别置信度
)
```

## 1. GPT-4 Vision API 实现

### 1.1 API 调用流程

```
┌─────────────┐     ┌─────────────┐     ┌─────────────┐     ┌─────────────┐
│  用户拍照    │ ──→ │  图片压缩    │ ──→ │  API 调用   │ ──→ │  结果解析    │
│  或上传图片  │     │  Base64编码  │     │  GPT-4V    │     │  返回结果   │
└─────────────┘     └─────────────┘     └─────────────┘     └─────────────┘
```

### 1.2 Prompt 设计

```text
你是一个专业的营养分析师。请分析这张食物图片，识别其中的所有食物，并估算每种食物的营养信息。

请以 JSON 格式返回结果：
{
  "foods": [
    {
      "name": "食物名称",
      "calories": 卡路里数值,
      "protein": 蛋白质克数,
      "carbs": 碳水化合物克数,
      "fat": 脂肪克数,
      "portion": "份量描述（如：一碗、100g）",
      "confidence": 0.0-1.0的置信度
    }
  ],
  "totalCalories": 总卡路里,
  "notes": "任何需要说明的备注"
}
```

### 1.3 代码结构

```kotlin
class GptFoodAnalyzer(
    private val apiKey: String,
    private val httpClient: OkHttpClient
) : FoodAnalyzer {
    
    override suspend fun analyze(imageData: ByteArray): Result<FoodAnalysisResult> {
        return withContext(Dispatchers.IO) {
            try {
                val base64Image = Base64.encodeToString(imageData, Base64.NO_WRAP)
                val response = callGptVisionApi(base64Image)
                Result.success(parseResponse(response))
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    
    private suspend fun callGptVisionApi(base64Image: String): String {
        // 构建请求体
        val requestBody = buildJsonObject {
            put("model", "gpt-4-vision-preview")
            put("max_tokens", 1000)
            putJsonArray("messages") {
                addJsonObject {
                    put("role", "user")
                    putJsonArray("content") {
                        addJsonObject {
                            put("type", "text")
                            put("text", FOOD_ANALYSIS_PROMPT)
                        }
                        addJsonObject {
                            put("type", "image_url")
                            putJsonObject("image_url") {
                                put("url", "data:image/jpeg;base64,$base64Image")
                            }
                        }
                    }
                }
            }
        }
        // 发送请求...
    }
}
```

### 1.4 错误处理

| 错误类型           | 处理策略                  |
|:---------------|:----------------------|
| API Key 无效     | 提示用户检查 API Key 设置     |
| 网络超时           | 重试机制 (最多3次)           |
| 图片过大           | 自动压缩并重试               |
| 无法识别食物         | 返回空结果并提示用户手动输入        |
| Rate Limit     | 显示友好提示，建议稍后重试         |

## 2. 本地 Gemini 模型实现

### 2.1 优势

* **离线可用**: 无需网络连接即可进行分析。
* **隐私保护**: 图片不离开设备，完全本地处理。
* **零成本**: 不产生 API 调用费用。

### 2.2 集成方案

使用 Google AI Edge SDK 集成 Gemini Nano：

```kotlin
class LocalGeminiAnalyzer(
    private val context: Context
) : FoodAnalyzer {
    
    private val generativeModel by lazy {
        GenerativeModel(
            modelName = "gemini-nano",
            context = context
        )
    }
    
    override suspend fun analyze(imageData: ByteArray): Result<FoodAnalysisResult> {
        return withContext(Dispatchers.Default) {
            try {
                val bitmap = BitmapFactory.decodeByteArray(imageData, 0, imageData.size)
                val response = generativeModel.generateContent(
                    content {
                        image(bitmap)
                        text(FOOD_ANALYSIS_PROMPT)
                    }
                )
                Result.success(parseResponse(response.text ?: ""))
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
}
```

### 2.3 模型下载与管理

```kotlin
class ModelManager(private val context: Context) {
    
    suspend fun ensureModelReady(): Boolean {
        val downloadManager = GenerativeModel.getDownloadManager(context)
        return when (downloadManager.getModelState("gemini-nano")) {
            ModelState.DOWNLOADED -> true
            ModelState.DOWNLOADING -> {
                // 等待下载完成
                downloadManager.awaitDownload("gemini-nano")
                true
            }
            else -> {
                // 开始下载
                downloadManager.downloadModel("gemini-nano")
                false
            }
        }
    }
}
```

## 3. 分析器工厂模式

```kotlin
class FoodAnalyzerFactory @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val context: Context,
    private val httpClient: OkHttpClient
) {
    fun create(): FoodAnalyzer {
        return when (settingsRepository.getApiType()) {
            ApiType.GPT -> GptFoodAnalyzer(
                apiKey = settingsRepository.getApiKey(),
                httpClient = httpClient
            )
            ApiType.LOCAL_GEMINI -> LocalGeminiAnalyzer(context)
        }
    }
}
```

## 4. 图片预处理

### 4.1 压缩策略

```kotlin
object ImageProcessor {
    
    private const val MAX_WIDTH = 1024
    private const val MAX_HEIGHT = 1024
    private const val QUALITY = 85
    
    fun processForAnalysis(bitmap: Bitmap): ByteArray {
        // 1. 调整尺寸
        val scaledBitmap = scaleBitmap(bitmap, MAX_WIDTH, MAX_HEIGHT)
        
        // 2. 压缩为 JPEG
        val outputStream = ByteArrayOutputStream()
        scaledBitmap.compress(Bitmap.CompressFormat.JPEG, QUALITY, outputStream)
        
        return outputStream.toByteArray()
    }
    
    private fun scaleBitmap(bitmap: Bitmap, maxWidth: Int, maxHeight: Int): Bitmap {
        val ratio = minOf(
            maxWidth.toFloat() / bitmap.width,
            maxHeight.toFloat() / bitmap.height
        )
        if (ratio >= 1f) return bitmap
        
        return Bitmap.createScaledBitmap(
            bitmap,
            (bitmap.width * ratio).toInt(),
            (bitmap.height * ratio).toInt(),
            true
        )
    }
}
```

## 5. 兼容性与限制

| 模式             | 最低 Android 版本 | 设备要求                |
|:---------------|:--------------|:--------------------|
| GPT-4 Vision   | Android 8.0   | 需要网络连接              |
| Gemini Nano    | Android 14    | 需要支持 AI Core 的 Pixel 设备 |

## 6. 后续优化方向

1. **结果缓存**: 对相似图片进行哈希缓存，避免重复分析。
2. **批量分析**: 支持一次拍摄多张图片批量分析。
3. **用户反馈学习**: 收集用户对识别结果的修正，优化 Prompt。
4. **备选模型**: 支持 Claude Vision、通义千问等其他模型。
