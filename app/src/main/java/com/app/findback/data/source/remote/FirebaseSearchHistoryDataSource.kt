package com.app.findback.data.source.remote

import android.util.Log
import com.app.findback.domain.models.SearchHistory
import com.google.firebase.database.FirebaseDatabase

class FirebaseSearchHistoryDataSource {
    private val database = FirebaseDatabase.getInstance().getReference("search-history")

    //create
    fun createSearchHistory(onSuccess: (Boolean) -> Unit,searchHistory: SearchHistory) {
        if (searchHistory.id == null) return
        try {
            database.child(searchHistory.userId).child(searchHistory.id).setValue(searchHistory.toMap())
                .addOnSuccessListener {
                    onSuccess(true)
                }
                .addOnFailureListener { error ->
                    onSuccess(false)
                }
        } catch (e: Exception) {
            onSuccess(false)
            Log.d("Lỗi", "Lỗi tạo search history: ${e.message}", e)
        }
    }
    //get
    fun getSearchHistory(onData: (ArrayList<SearchHistory>) -> Unit, userId: String){
        try {
            database.child(userId)
                .get()
                .addOnSuccessListener {
                val searchHistory = it.children.mapNotNull {
                    (it.value as? Map<String, Any?>)?.let(SearchHistory::fromMap)
                }
                onData(searchHistory as ArrayList<SearchHistory>)
            }
                .addOnFailureListener { error ->
                Log.d("Lỗi", "Lỗi lấy search history: ${error.message}", error)
            }
        } catch (e: Exception) {
            Log.d("Lỗi", "Lỗi lấy search history: ${e.message}", e)
        }
    }
    //delete
    fun deleteSearchHistory(onSuccess: (Boolean) -> Unit, searchHistory: SearchHistory) {
        if (searchHistory.id == null) return
        try {
            database.child(searchHistory.userId).child(searchHistory.id).removeValue()
                .addOnSuccessListener {
                    onSuccess(true)
                }
                .addOnFailureListener { error ->
                    onSuccess(false)
                }
        } catch (e: Exception) {
            onSuccess(false)
            Log.d("Lỗi", "Lỗi xóa search history: ${e.message}", e)
        }
    }
     //update
    fun updateSearchHistory(onSuccess: (Boolean) -> Unit, searchHistory: SearchHistory) {
        if (searchHistory.id == null) return
        try {
            database.child(searchHistory.userId).child(searchHistory.id).setValue(searchHistory.toMap())
                .addOnSuccessListener {
                    onSuccess(true)
                }
                .addOnFailureListener { error ->
                    onSuccess(false)
                }
            } catch (e: Exception) {
            onSuccess(false)
            Log.d("Lỗi", "Lỗi cập nhật search history: ${e.message}", e)
        }
     }
}