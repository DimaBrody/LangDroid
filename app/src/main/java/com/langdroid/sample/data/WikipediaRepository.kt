package com.langdroid.sample.data

import java.net.URLDecoder
import java.nio.charset.StandardCharsets

class WikipediaRepository(private val wikipediaApi: WikipediaApi) {

    suspend fun getWikipediaContent(articleUrl: String): Result<WikipediaUiModel> {
        val titleUrl = articleUrl.substringAfterLast("/").substringBefore("?")
        val title = URLDecoder.decode(titleUrl, StandardCharsets.UTF_8.toString())

        return try {
            val response = wikipediaApi.getArticleContent(title)


            val pageInfo = response?.query?.pages?.values?.firstOrNull()
            val outputTitle = pageInfo?.title
            val content = pageInfo?.extract

            if (outputTitle == null || content == null)
                throw EmptyOutputException()

            val uiModel = WikipediaUiModel(outputTitle, content)

            Result.success(uiModel)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
