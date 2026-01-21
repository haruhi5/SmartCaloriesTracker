# GEMINI Development Guidelines

## 代码质量与开发原则

### 质量要求

- **语言规范**: 严格遵循 Kotlin 官方编码规范。
- **Android 规范**:
    - 遵循 Modern Android Development (MAD) 指南。
    - **UI 框架**: 全面使用 Jetpack Compose (+ Material3/Material You)，支持动态取色。
    - **架构**: MVVM (ViewModel + StateFlow + Repository) + Clean Architecture。
    - **依赖注入**: 使用 **Hilt** (Hilt-Android, Hilt-Navigation-Compose)。
    - **兼容性**: MinSDK 为 26 (Android 8.0)，TargetSDK 为 34 (Android 14+)。
- **功能特性规范**:
    - **AI 核心逻辑**:
        - 优先使用本地 Gemini Nano 模型（如可用）保护隐私。
        - 云端分析 (GPT-4 Vision) 需经过用户确认并使用加密存储的 API Key。
        - **图片处理**: 上传前必须进行压缩处理，减少流量消耗。
    - **数据管理**:
        - 使用 Room 数据库存储所有结构化数据，严禁直接操作文件系统存储核心数据。
        - 敏感数据 (API Keys) 使用 `EncryptedSharedPreferences`。
    - **UI 呈现**:
        - 仪表盘需清晰展示今日卡路里目标进度。
        - 拍照/分析流程需包含加载状态与错误处理。
- **代码结构**:
    - 单个文件原则上不超过 1000 行。
    - 业务逻辑必须下沉至 `Domain` 层或 `Repository` 层。
    - 避免在 UI 层 (Composable) 直接调用数据源。
- **注释**:
    - 使用中文编写清晰的 KDoc 与行内注释。
    - 核心算法逻辑（如 TDEE 计算、AI 结果解析）必须添加详细注释说明。

### 测试与验证

- **编译检查**:
    - 变更代码后，必须执行 `./gradlew :app:assembleDebug` 确保编译通过。
    - 变更代码后, 执行相应的syntax check 例如 bash -n 检查相应代码
- **Lint 检查**:
    - 运行 `./gradlew lint` 检查潜在的代码质量问题。
- **功能验证**:
    - 重点测试 AI 识别流程的异常情况（网络断开、Key 无效、无法识别图片）。
    - 必须在真机（或模拟器相机）上测试拍照与图库选择功能。

#### 项目本地校验流程

1.  命令行执行 `./gradlew :app:assembleDebug`。
2.  确认无编译错误，且 `libs.versions.toml` 中无过时警告。

## 文档与记忆

文档与记忆采用 Markdown 格式，存放于 `.agentdocs/` 及其子目录下。
索引文档：`.agentdocs/index.md`

### 文档分类

- `prd/` - 产品与需求
    - `prd/requirements.md` - 核心功能需求
- `architecture/` - 架构与技术细节
    - `architecture/ai-analysis-strategy.md` - AI 分析与集成策略
    - `architecture/data-management-strategy.md` - 数据库与数据流策略
- `ui/` - 界面规范
    - `ui/design-system.md` - 设计系统与组件规范
- `workflow/` - 任务流文档 (按标准格式命名)

### 全局重要记忆

- **项目名称**: Smart Calories Tracker
- **设备支持**:
    - 核心目标: Android 手机 (支持相机)。
    - AI 要求: 本地模型功能仅限支持 Gemni Nano 的设备 (Pixel 8+ 等)。
- **SDK 版本**:
    - **MinSDK**: 26 (Android 8.0)。
    - **CompileSDK/TargetSDK**: 34 (Android 14)。
- **关键技术决策**:
    - **DI**: Hilt (Android 标准推荐)。
    - **DB**: Room (本地存储)。
    - **Net**: Retrofit + OkHttp (GPT API)。
    - **Async**: Coroutines + Flow。

## 任务处理指南

- **需求澄清**: 当需求不明确时（例如具体的 UI 细节、API 参数），先提问澄清。
- **方案分析**: 对于 AI 相关功能，需考虑 Token 消耗与隐私合规。
- **分阶段实施**:
    1.  基础架构搭建 (Hilt, Room, Compose)。
    2.  AI 分析模块实现 (Camera -> API -> Result)。
    3.  数据记录与统计模块。
    4.  用户系统与设置。
- **风险记录**: 涉及 API Key 存储与图片隐私时，需在文档中记录安全策略。

### 任务回顾

- 在任务完成呈现最终消息前，必须进行以下任务回顾：
    - 检查是否产生新的可复用组件，并更新架构文档。
    - 检查 `.agentdocs/` 下的文档是否需要更新。
    - 确认 `libs.versions.toml` 中的依赖版本是否为最新稳定版。
