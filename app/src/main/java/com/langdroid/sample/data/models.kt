package com.langdroid.sample.data

import kotlinx.serialization.Serializable

@Serializable
data class WikipediaResponse(
    val query: QueryResult
)

@Serializable
data class QueryResult(
    val pages: Map<String, PageInfo>
)

@Serializable
data class PageInfo(
    val title: String,
    val extract: String
)


data class WikipediaUiModel(
    val title: String,
    val content: String
)
