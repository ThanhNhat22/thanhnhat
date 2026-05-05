package com.app.findback.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.app.findback.data.repositories.SearchHistoryRepositoryImpl
import com.app.findback.domain.models.SearchHistory
import com.app.findback.domain.repository.SearchHistoryRepository

class SearchHistoryViewModel : ViewModel() {
    private val searchHistoryRepository: SearchHistoryRepository = SearchHistoryRepositoryImpl()
    private val _searchHistory = MutableLiveData<List<SearchHistory>>(emptyList())
    val searchHistory: LiveData<List<SearchHistory>> = _searchHistory

    fun getSearchHistory(userId: String) {
        searchHistoryRepository.getSearchHistory(onData = { searchHistory ->
            _searchHistory.postValue(searchHistory)
        }, userId)
    }
    fun createSearchHistory(searchHistory: SearchHistory, onSuccess: (Boolean) -> Unit) {
        searchHistoryRepository.createSearchHistory(onSuccess, searchHistory)
    }
    fun deleteSearchHistory(searchHistory: SearchHistory, onSuccess: (Boolean) -> Unit) {
        searchHistoryRepository.deleteSearchHistory(onSuccess, searchHistory)
    }
    fun updateSearchHistory(searchHistory: SearchHistory, onSuccess: (Boolean) -> Unit) {
        searchHistoryRepository.updateSearchHistory(onSuccess, searchHistory)
    }
}