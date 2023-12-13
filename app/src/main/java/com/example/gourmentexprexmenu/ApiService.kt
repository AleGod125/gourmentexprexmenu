package com.example.gourmentexprexmenu

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface ApiService {
    @POST("engines/davinci/completions")
    fun generateText(
        @Header("Authorization") authorization: String,
        @Body request: OpenAIRequest
    ): Call<OpenAIResponse>
}

data class OpenAIRequest(
    val prompt: String,
    val max_tokens: Int
)

data class OpenAIResponse(
    val choices: List<OpenAIChoice>
)

data class OpenAIChoice(
    val text: String
)





