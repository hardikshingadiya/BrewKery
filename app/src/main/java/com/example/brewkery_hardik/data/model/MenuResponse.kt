package com.example.brewkery_hardik.data.model
import com.google.gson.annotations.SerializedName

data class MenuResponse(
    val meta: StoreMetadata,
    val categories: List<Category>,
    val items: List<MenuItem>
)

data class StoreMetadata(
    val app: String,
    val version: String,
    val tagline: String,
    val currency: String,

    @SerializedName("currency_symbol")
    val currencySymbol: String,

    @SerializedName("delivery_fee")
    val deliveryFee: Double,

    @SerializedName("tax_rate_percent")
    val taxRatePercent: Double,

    @SerializedName("estimated_delivery_time")
    val estimatedDeliveryTime: String
)

data class Category(
    val id: String,
    val name: String,
    val icon: String,

    @SerializedName("item_count")
    val itemCount: Int
)
