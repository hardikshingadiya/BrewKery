
package com.example.brewkery_hardik.utils

import com.example.brewkery_hardik.data.model.MenuItem

data class CartItem(
    val menuItem: MenuItem,
    val sizeId: String,
    val sizeLabel: String,
    val milkId: String,
    val milkName: String,
    val sugarLevel: String,
    val unitPrice: Double,
    var quantity: Int = 1
) {
    val totalPrice: Double
        get() = unitPrice * quantity
}

object CartManager {

    private val cartItems = mutableListOf<CartItem>()

    fun addItem(item: CartItem) {
        cartItems.add(item)
    }

    fun getItems(): List<CartItem> {
        return cartItems.toList()
    }

    fun getItemCount(): Int {
        return cartItems.sumOf { it.quantity }
    }

    fun getSubtotal(): Double {
        return cartItems.sumOf { it.totalPrice }
    }

    fun updateQuantity(index: Int, quantity: Int) {
        if (index !in cartItems.indices) return

        if (quantity <= 0) {
            cartItems.removeAt(index)
        } else {
            cartItems[index].quantity = quantity
        }
    }

    fun removeItem(index: Int) {
        if (index in cartItems.indices) {
            cartItems.removeAt(index)
        }
    }

    fun clearCart() {
        cartItems.clear()
    }

    fun isEmpty(): Boolean {
        return cartItems.isEmpty()
    }
}