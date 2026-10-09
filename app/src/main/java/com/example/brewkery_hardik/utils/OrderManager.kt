package com.example.brewkery_hardik.utils
import java.util.UUID

data class Order(
    val ticketId: String,
    val items: List<CartItem>,
    val subtotal: Double,
    val tax: Double,
    val deliveryFee: Double,
    val totalAmount: Double,
    val status: String = "PREPARING",
    val estimatedWait: String = "20 – 30 minutes"
) {
    val itemCount: Int
        get() = items.sumOf { it.quantity }
}

object OrderManager {

    private val orders = mutableListOf<Order>()

    fun placeOrder(): Order? {
        val cartItems = CartManager.getItems()

        if (cartItems.isEmpty()) return null

        // Make independent copies of cart items
        val orderItems = cartItems.map { it.copy() }

        val subtotal = orderItems.sumOf { it.totalPrice }
        val tax = subtotal * 0.08
        val deliveryFee = 2.50

        val order = Order(
            ticketId = "#BK-${UUID.randomUUID()
                .toString().take(5).uppercase()}",
            items = orderItems,
            subtotal = subtotal,
            tax = tax,
            deliveryFee = deliveryFee,
            totalAmount = subtotal + tax + deliveryFee
        )

        orders.add(0, order)
        CartManager.clearCart()

        return order
    }

    fun getOrders(): List<Order> {
        return orders.toList()
    }

    fun getOrderByTicketId(ticketId: String): Order? {
        return orders.find { it.ticketId == ticketId }
    }

    fun getLatestOrder(): Order? {
        return orders.firstOrNull()
    }

    fun isEmpty(): Boolean {
        return orders.isEmpty()
    }

    fun clearOrders() {
        orders.clear()
    }
}