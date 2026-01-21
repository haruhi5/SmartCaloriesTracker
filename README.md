# Smart Calories Tracker

A modern Android application for tracking calories using AI-powered food recognition.

## 🚀 Features

*   **AI Food Analysis**: Recognize food from photos using **GPT-4 Vision** or local **Gemini Nano**.
*   **Calorie Dashboard**: Track daily intake, macros (Protein, Carbs, Fat), and visualize progress.
*   **Smart Goals**: TDEE-based calorie targets for Weight Maintenance, Muscle Gain, or Fat Loss.
*   **Data Privacy**: All data stored locally using Room Database.
*   **Export**: Export your dietary logs to CSV.

## 🛠 Tech Stack

*   **Language**: Kotlin
*   **UI**: Jetpack Compose (Material 3)
*   **Architecture**: MVVM + Clean Architecture
*   **DI**: Hilt
*   **Database**: Room
*   **Network**: Retrofit + OkHttp
*   **AI Integration**: OpenAI API / Google AI Client SDK

## 📦 How to Build

1.  **Open in Android Studio**:
    *   Open Android Studio.
    *   Select "Open" and choose the `SmartCalroiesTracker` folder.
    *   Allow Gradle to sync (this will generate the necessary Gradle wrapper files).

2.  **Configuration**:
    *   The app uses **Hilt**, so a compilation step is required to generate dependency injection code.
    *   For GPT-4 Vision features, go to **Settings** in the app and enter your OpenAI API Key.

3.  **Run**:
    *   Connect a device or start an emulator.
    *   Run the `app` configuration.

## 📝 License

This project is created by Gemini Agent.
