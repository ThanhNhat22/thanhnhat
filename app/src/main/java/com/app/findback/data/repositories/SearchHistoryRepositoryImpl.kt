package com.app.findback.data.repositories

import com.app.findback.data.source.remote.FirebaseSearchHistoryDataSource
import com.app.findback.domain.models.SearchHistory
import com.app.findback.domain.repository.SearchHistoryRepository

class SearchHistoryRepositoryImpl : SearchHistoryRepository {
    private val dataSource = FirebaseSearchHistoryDataSource()
    override fun getSearchHistory(onData: (ArrayList<SearchHistory>) -> Unit, userId: String) {
        dataSource.getSearchHistory(onData, userId)
    }

    override fun createSearchHistory(
        onSuccess: (Boolean) -> Unit,
        searchHistory: SearchHistory
    ) {
        dataSource.createSearchHistory(onSuccess, searchHistory)
    }

    override fun deleteSearchHistory(
        onSuccess: (Boolean) -> Unit,
        searchHistory: SearchHistory
    ) {
        dataSource.deleteSearchHistory(onSuccess,searchHistory)
    }

    override fun updateSearchHistory(
        onSuccess: (Boolean) -> Unit,
        searchHistory: SearchHistory
    ) {
        dataSource.updateSearchHistory(onSuccess,searchHistory)
    }
}