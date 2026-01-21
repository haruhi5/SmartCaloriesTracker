package com.gemini.calories.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class OpenAiRequest(
    val model: String = "gpt-4-vision-preview",
    val messages: List<Message>,
    val max_tokens: Int = 1000
)

@Serializable
data class Message(
    val role: String,
    val content: List<Content>
)

@Serializable
sealed class Content {
    @Serializable
    data class Text(val type: String = "text", val text: String) : Content()

    @Serializable
    data class Image(val type: String = "image_url", val image_url: ImageUrl) : Content()
}

@Serializable
data class ImageUrl(val url: String)

@Serializable
data class OpenAiResponse(
    val choices: List<Choice>
)

@Serializable
data class Choice(
    val message: ResponseMessage
)

@Serializable
data class ResponseMessage(
    val content: String
)
