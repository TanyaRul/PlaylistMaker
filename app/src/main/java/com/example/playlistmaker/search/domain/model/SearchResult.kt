package com.example.playlistmaker.search.domain.model

import java.io.Serializable

data class SearchResult<out A, out B>(
    val resultList: A,
    val error: B
) : Serializable
