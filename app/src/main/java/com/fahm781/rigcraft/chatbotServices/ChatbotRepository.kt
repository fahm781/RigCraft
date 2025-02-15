package com.fahm781.rigcraft.chatbotServices

import android.util.Log
import retrofit2.Call
import retrofit2.Callback


class ChatbotRepository {

    private val openAiApiClient = OpenAiApiClient()
    private val openAiApiInstance = openAiApiClient.getInstance()

    fun getResponse(msg: String, prompt: String, callback: (String) -> Unit) {
        val model = "gpt-3.5-turbo"
        try {
            val request = Request(
                arrayListOf(
                    Msg(
                        role = "system",
                        content = prompt
                    ),
                    Msg(
                        role = "user",
                        content = msg
                    )
                ),
                model
            )

            openAiApiInstance.getResponse(request)
                .enqueue(object : Callback<Response> {
                    override fun onResponse(call: Call<Response>, response: retrofit2.Response<Response>) {

                        val code = response.code()
                        if(code == 200){  //if the response is successful
                            response.body()?.choices?.get(0)?.message?.let {
                                callback(it.content)
                                Log.d("message", it.content)
                            }
                            }   else{
                            response.errorBody()?.let {
                                callback(it.toString())
                                Log.d("Error", it.string())
                            }
                            }
                    }
                    override fun onFailure(call: Call<Response>, t: Throwable) {
                        t.printStackTrace()
                    }
                })
        }   catch (e: Exception) {
                return e.printStackTrace()
        }
    }
}
