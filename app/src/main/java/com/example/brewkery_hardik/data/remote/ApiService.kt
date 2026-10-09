package com.example.brewkery_hardik.data.remote
import com.example.brewkery_hardik.data.model.MenuItem
import com.example.brewkery_hardik.data.model.MenuResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface ApiService {

    @GET("data.json")
    suspend fun getMenu(): Response<MenuResponse>

    @GET("api/items/{id}.json")
    suspend fun getItemById(
        @Path("id") id: Int
    ): Response<MenuItem>
}