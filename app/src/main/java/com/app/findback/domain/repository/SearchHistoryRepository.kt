package com.app.findback.domain.repository

import com.app.findback.domain.models.SearchHistory

interface SearchHistoryRepository {
    fun getSearchHistory(onData: (ArrayList<SearchHistory>) -> Unit, userId: String)
    fun createSearchHistory(onSuccess: (Boolean) -> Unit, searchHistory: SearchHistory)
    fun deleteSearchHistory(onSuccess: (Boolean) -> Unit, searchHistory: SearchHistory)
    fun updateSearchHistory(onSuccess: (Boolean) -> Unit, searchHistory: SearchHistory)
}