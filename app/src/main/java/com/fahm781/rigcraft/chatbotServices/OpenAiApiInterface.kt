package com.fahm781.rigcraft.chatbotServices

import com.fahm781.rigcraft.BuildConfig
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface OpenAiApiInterface {
    @POST("chat/completions")
    fun getResponse(
        @Body request: Request,
        @Header("Content-Type") token: String = "application/json",
        @Header("Authorization") authorization: String = "Bearer ${BuildConfig.OPEN_AI_API_KEY}"
    ): Call<Response>
    }



