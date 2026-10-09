package com.example.brewkery_hardik.data.repository
import com.example.brewkery_hardik.data.model.MenuItem
import com.example.brewkery_hardik.data.model.MenuResponse
import com.example.brewkery_hardik.data.remote.RetrofitClient

class MenuRepository {
    private val apiService = RetrofitClient.apiService

    suspend fun getMenu(): Result<MenuResponse> {
        return try {
            val response = apiService.getMenu()
            if (response.isSuccessful) {
                response.body()?.let {
                    Result.success(it)
                } ?: Result.failure(
                    Exception("Menu response is empty")
                )
            } else {
                Result.failure(
                    Exception("Failed to load menu: ${response.code()}")
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getItemById(id: Int): Result<MenuItem> {
        return try {
            val response = apiService.getItemById(id)
            if (response.isSuccessful) {
                response.body()?.let {
                    Result.success(it)
                } ?: Result.failure(
                    Exception("Item response is empty")
                )
            } else {
                Result.failure(
                    Exception("Failed to load item: ${response.code()}")
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}