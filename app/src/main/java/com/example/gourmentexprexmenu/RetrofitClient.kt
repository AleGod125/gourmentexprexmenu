package com.example.gourmentexprexmenu

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object OpenAIRetrofitClient {

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://api.openai.com/v1/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val openAIApiService = retrofit.create(ApiService::class.java)
}

