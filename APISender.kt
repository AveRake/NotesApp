package com.lab1.notesapp

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import okhttp3.*
import okhttp3.logging.HttpLoggingInterceptor
import java.io.IOException
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit

class APISender {
    companion object {
        val mapper = jacksonObjectMapper()
    }

    private val client: OkHttpClient
    private val domain = "http://10.0.2.2:4000"

    init {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .build()
    }

    fun get(url: String): CompletableFuture<Response> {
        val future = CompletableFuture<Response>()

        val request = Request.Builder()
            .url("$domain$url")
            .header("Accept", "application/json")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                future.completeExceptionally(e)
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    future.complete(response)
                } else {
                    future.completeExceptionally(IOException("HTTP ${response.code}"))
                }
            }
        })

        return future
    }

    fun post(url: String, params: Map<String, String>): CompletableFuture<String> {
        val future = CompletableFuture<String>()

        val formBody = FormBody.Builder().apply {
            params.forEach { (key, value) ->
                add(key, value)
            }
        }.build()

        val request = Request.Builder()
            .url("$domain$url")
            .post(formBody)
            .header("Accept", "application/json")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                future.completeExceptionally(e)
            }

            override fun onResponse(call: Call, response: Response) {
                try {
                    val body = response.body?.string() ?: ""
                    if (response.isSuccessful) {
                        future.complete(body)
                    } else {
                        future.completeExceptionally(IOException("HTTP ${response.code}: $body"))
                    }
                } catch (e: Exception) {
                    future.completeExceptionally(e)
                } finally {
                    response.close()
                }
            }
        })

        return future
    }
}