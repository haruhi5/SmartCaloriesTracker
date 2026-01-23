# 设计系统 (Design System)

Smart Calories Tracker 严格遵循 Modern Android Development (MAD) 指南，全面采用 Jetpack Compose 构建 UI。设计灵感来源于 openScale，强调简洁、直观的数据展示。

## 1. 主题与配色 (Theming)

### 1.1 Material Design 3

- **风格**: **Material 3 + Material You**。
- **动态取色 (Dynamic Color)**:
    - 必须启用 `DynamicColors.applyToActivitiesIfAvailable(this)`。
    - UI 颜色直接映射系统壁纸色调，确保与原生系统视觉一致。
- **深色模式**: 完整支持 Light/Dark/System 三种模式切换。
- **主要控件**: 使用 M3 标准组件 (`Scaffold`, `TopAppBar`, `Card`, `NavigationBar`, `FloatingActionButton` 等)。

### 1.2 颜色语义

```kotlin
// 卡路里状态颜色
val CalorieColors = object {
    val underTarget = Color(0xFF4CAF50)    // 绿色 - 摄入低于目标
    val nearTarget = Color(0xFFFFC107)     // 黄色 - 接近目标
    val overTarget = Color(0xFFF44336)     // 红色 - 超过目标
}

// 营养素颜色
val NutrientColors = object {
    val protein = Color(0xFF2196F3)        // 蓝色 - 蛋白质
    val carbs = Color(0xFFFF9800)          // 橙色 - 碳水化合物
    val fat = Color(0xFFE91E63)            // 粉色 - 脂肪
}
```

## 2. 页面结构 (Navigation)

### 2.1 左侧抽屉导航 (Left Drawer)

整体导航采用 **左侧抽屉 (ModalNavigationDrawer)**，适配手机竖屏使用场景：

```
┌───────────────┬───────────────────────────────┐
│  ☰  Smart Calories│                           │
│─────────────────│          [内容区域]           │
│  ▸ Dashboard   │                           │
│  ▸ Analyze     │                           │
│  ▸ Profile     │                           │
│  ▸ Settings    │                           │
└───────────────┴───────────────────────────────┘
```

- 使用 `ModalNavigationDrawer` + `ModalDrawerSheet` + `NavigationDrawerItem` 实现。
- 当前选中项高亮，保持与路由状态同步。
- 导航入口：顶部应用栏左侧展示「汉堡按钮」，点击或边缘滑动打开抽屉。

### 2.2 页面清单

| 页面         | 路由            | 描述                                      |
|:------------|:----------------|:------------------------------------------|
| Dashboard   | `dashboard`     | 今日摄入概览、卡路里环、日/周趋势与今日记录列表         |
| Analyze     | `analysis`      | 相机拍照或相册选择，AI 分析并展示可编辑的识别结果       |
| Profile     | `profile`       | 用户资料、TDEE 目标设置、导出饮食记录               |
| Settings    | `settings`      | AI 引擎选择、API Key、主题模式等应用设置            |

## 3. 首页设计 (Dashboard)

### 3.1 布局结构

顶部使用 **小型应用栏 (SmallTopAppBar)**，与内容区域共用同一背景颜色，避免「悬浮卡片」视觉，并保持紧贴状态栏：

```
┌─────────────────────────────────────┐
│  ☰  Smart Calories Tracker         │ ← SmallTopAppBar
├─────────────────────────────────────┤
│  ┌─────────────────────────────┐    │
│  │     今日摄入                 │    │ ← 主卡片
│  │     ████████░░░  1,450      │    │   圆形进度条
│  │         / 2,000 kcal        │    │
│  │                             │    │
│  │  蛋白质: 85g  碳水: 180g  脂肪: 45g │ ← 宏量营养素
│  └─────────────────────────────┘    │
│                                     │
│  今日记录                            │ ← Section Header
│  ┌─────────────────────────────┐    │
│  │ 🍳 早餐 - 煎蛋三明治    350 kcal │    │
│  ├─────────────────────────────┤    │
│  │ 🍜 午餐 - 牛肉面        650 kcal │    │
│  ├─────────────────────────────┤    │
│  │ 🍎 加餐 - 苹果          95 kcal │    │
│  └─────────────────────────────┘    │
│                                     │
│                          [📷 添加]  │ ← FAB
└─────────────────────────────────────┘
```

### 3.2 卡路里进度环

```kotlin
@Composable
fun CalorieProgressRing(
    consumed: Int,
    target: Int,
    modifier: Modifier = Modifier
) {
    val progress = (consumed.toFloat() / target).coerceIn(0f, 1.5f)
    val color = when {
        progress < 0.8f -> CalorieColors.underTarget
        progress < 1.0f -> CalorieColors.nearTarget
        else -> CalorieColors.overTarget
    }
    
    Box(
        modifier = modifier.size(200.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            // 背景环
            drawArc(
                color = color.copy(alpha = 0.2f),
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(width = 24.dp.toPx(), cap = StrokeCap.Round)
            )
            // 进度环
            drawArc(
                color = color,
                startAngle = -90f,
                sweepAngle = 360f * progress.coerceAtMost(1f),
                useCenter = false,
                style = Stroke(width = 24.dp.toPx(), cap = StrokeCap.Round)
            )
        }
        
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "$consumed",
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "/ $target kcal",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
```

## 4. 统计图表 (Charts)

### 4.1 周趋势折线图

参考 openScale 的图表设计，使用 Vico 或 MPAndroidChart 库实现：

```kotlin
@Composable
fun WeeklyCalorieChart(
    dailyStats: List<DailyStats>,
    targetCalories: Int,
    modifier: Modifier = Modifier
) {
    val chartEntryModel = entryModelOf(
        dailyStats.mapIndexed { index, stats ->
            entryOf(index.toFloat(), stats.totalCalories.toFloat())
        }
    )
    
    Chart(
        chart = lineChart(
            lines = listOf(
                lineSpec(
                    lineColor = MaterialTheme.colorScheme.primary,
                    lineBackgroundShader = verticalGradient(
                        arrayOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                            Color.Transparent
                        )
                    )
                )
            )
        ),
        model = chartEntryModel,
        modifier = modifier.height(200.dp),
        startAxis = rememberStartAxis(),
        bottomAxis = rememberBottomAxis(
            valueFormatter = { value, _ ->
                dailyStats.getOrNull(value.toInt())?.date
                    ?.format(DateTimeFormatter.ofPattern("E"))
                    ?: ""
            }
        )
    )
}
```

### 4.2 营养素分布饼图

```kotlin
@Composable
fun MacronutrientPieChart(
    protein: Float,
    carbs: Float,
    fat: Float,
    modifier: Modifier = Modifier
) {
    val total = protein * 4 + carbs * 4 + fat * 9  // 转换为卡路里
    
    val slices = listOf(
        PieSlice(protein * 4 / total, NutrientColors.protein, "蛋白质"),
        PieSlice(carbs * 4 / total, NutrientColors.carbs, "碳水"),
        PieSlice(fat * 9 / total, NutrientColors.fat, "脂肪")
    )
    
    // 绘制饼图...
}
```

## 5. 食物记录卡片

```kotlin
@Composable
fun FoodEntryCard(
    entry: FoodEntry,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 餐食图标
            Icon(
                imageVector = when (entry.mealType) {
                    MealType.BREAKFAST -> Icons.Default.FreeBreakfast
                    MealType.LUNCH -> Icons.Default.LunchDining
                    MealType.DINNER -> Icons.Default.DinnerDining
                    MealType.SNACK -> Icons.Default.Cookie
                },
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(40.dp)
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = entry.foodName,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = entry.portion,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Text(
                text = "${entry.calories} kcal",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
```

## 6. AI 分析界面

### 6.1 拍照界面

```
┌─────────────────────────────────────┐
│  ←  分析食物                         │
├─────────────────────────────────────┤
│                                     │
│         ┌─────────────────┐         │
│         │                 │         │
│         │   [相机预览]     │         │
│         │                 │         │
│         └─────────────────┘         │
│                                     │
│  将食物放置在画面中央                   │
│                                     │
│     [🖼 相册]      [📷 拍照]          │
│                                     │
└─────────────────────────────────────┘
```

### 6.2 分析结果界面

```
┌─────────────────────────────────────┐
│  ←  分析结果                    ✓   │
├─────────────────────────────────────┤
│  ┌───────────────────────────────┐  │
│  │       [食物图片预览]           │  │
│  └───────────────────────────────┘  │
│                                     │
│  识别到 3 种食物                     │
│                                     │
│  ┌───────────────────────────────┐  │
│  │ 🍚 米饭 (1碗)           200 kcal│  │
│  │    蛋白质: 4g  碳水: 45g  脂肪: 0g│  │
│  ├───────────────────────────────┤  │
│  │ 🥢 红烧肉 (100g)        380 kcal│  │
│  │    蛋白质: 25g  碳水: 5g  脂肪: 28g│ │
│  ├───────────────────────────────┤  │
│  │ 🥬 炒青菜 (1份)          45 kcal│  │
│  │    蛋白质: 2g  碳水: 6g  脂肪: 2g│  │
│  └───────────────────────────────┘  │
│                                     │
│  ────────────────────────────────   │
│  总计                      625 kcal │
│                                     │
│  选择餐食类型:                       │
│  [早餐] [午餐✓] [晚餐] [加餐]         │
│                                     │
│         [保存记录]                   │
└─────────────────────────────────────┘
```

## 7. 设置页面

### 7.1 AI 分析引擎选择

设置页顶部展示一个「AI 分析引擎」卡片，用于在不同分析后端之间切换：

```
┌─────────────────────────────────────┐
│ AI Analysis Engine                 │
│ ─────────────────────────────────  │
│ [● GPT-4 Vision]                  │
│     最高质量的图像理解，需要 OpenAI API Key│
│ [○ Gemini (Cloud)]                │
│     使用云端 Gemini，密钥在应用内统一配置  │
│ [○ Gemini (On-device)]            │
│     在支持设备上本地运行，更注重隐私       │
│                                     │
│  (当选择 GPT-4 Vision 时显示)        │
│  OpenAI API Key:  [••••••••••] [Save] │
└─────────────────────────────────────┘
```

交互规则：

- 仅允许三者 **单选**，当前选中的引擎以高亮卡片或选中态标识。
- 下方动态说明当前引擎的优劣与适用场景（质量 / 隐私 / 依赖网络等）。
- 只有在选择 **GPT-4 Vision** 时出现 `OpenAI API Key` 输入框与保存按钮。
- Gemini Cloud / On-device 不需要在此页面输入密钥，只展示说明文案。

Compose 实现示例：

```kotlin
@Composable
fun AiEngineSettings(
    apiType: String,
    onApiTypeChange: (String) -> Unit,
    apiKey: String,
    onApiKeyChange: (String) -> Unit,
    onSaveKey: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "AI Analysis Engine",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        AiEngineOptionCard(
            selected = apiType == "gpt",
            title = "GPT-4 Vision",
            subtitle = "Best quality vision model, requires OpenAI API key.",
            onClick = { onApiTypeChange("gpt") }
        )
        AiEngineOptionCard(
            selected = apiType == "gemini",
            title = "Gemini (Cloud)",
            subtitle = "Uses cloud Gemini configuration bundled with the app.",
            onClick = { onApiTypeChange("gemini") }
        )
        AiEngineOptionCard(
            selected = apiType == "gemini_ondevice",
            title = "Gemini (On-device)",
            subtitle = "Runs locally when supported, no API key required.",
            onClick = { onApiTypeChange("gemini_ondevice") }
        )

        if (apiType == "gpt") {
            OutlinedTextField(
                value = apiKey,
                onValueChange = onApiKeyChange,
                label = { Text("OpenAI API Key") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Button(
                onClick = onSaveKey,
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("Save Key")
            }
        }
    }
}
```

```kotlin
@Composable
fun AiEngineOptionCard(
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
        Column(modifier = Modifier.padding(16.dp)) {
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
```

## 8. 交互动效

### 8.1 添加食物动画

```kotlin
// FAB 点击后的扩展动画
val fabExpanded by remember { mutableStateOf(false) }

AnimatedVisibility(
    visible = fabExpanded,
    enter = fadeIn() + scaleIn(),
    exit = fadeOut() + scaleOut()
) {
    Column {
        SmallFloatingActionButton(onClick = { /* 打开相册 */ }) {
            Icon(Icons.Default.PhotoLibrary, "相册")
        }
        Spacer(modifier = Modifier.height(8.dp))
        SmallFloatingActionButton(onClick = { /* 打开相机 */ }) {
            Icon(Icons.Default.CameraAlt, "拍照")
        }
    }
}
```

### 8.2 卡路里更新动画

```kotlin
val animatedCalories by animateIntAsState(
    targetValue = todayCalories,
    animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing)
)
```
