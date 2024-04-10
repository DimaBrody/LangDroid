package com.langdroid.sample.data

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

class WikipediaApi {

    private val client = HttpClient(Android) {
        install(ContentNegotiation) {
            json(Json {
                prettyPrint = true
                isLenient = true
                ignoreUnknownKeys = true
            })
        }
    }

    suspend fun getArticleContent(pageTitle: String): WikipediaResponse? =
        withContext(Dispatchers.IO) {
            try {
                client.get("https://en.wikipedia.org/w/api.php") {
                    parameter("format", "json")
                    parameter("action", "query")
                    parameter("prop", "extracts")
                    parameter("exlimit", "max")
                    parameter("explaintext", null)
                    parameter("titles", pageTitle)
                    parameter("redirects", "")
                }.body()
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
}
