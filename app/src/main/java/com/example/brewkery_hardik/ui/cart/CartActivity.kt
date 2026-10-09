package com.example.brewkery_hardik.ui.cart

import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import com.example.brewkery_hardik.R
import com.example.brewkery_hardik.ui.order.OrderStatusActivity
import com.example.brewkery_hardik.utils.CartItem
import com.example.brewkery_hardik.utils.CartManager
import com.example.brewkery_hardik.utils.OrderManager
import java.util.Locale

class CartActivity : AppCompatActivity() {

    private lateinit var cartItemsContainer: LinearLayout
    private lateinit var emptyCartView: LinearLayout
    private lateinit var cartScrollView: View
    private lateinit var cartBottomBar: LinearLayout

    private lateinit var subtotalText: TextView
    private lateinit var taxText: TextView
    private lateinit var totalText: TextView
    private lateinit var checkoutButton: com.google.android.material.button.MaterialButton

    private val deliveryFee = 2.50
    private val taxRate = 0.08

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = android.graphics.Color.TRANSPARENT
        window.navigationBarColor = android.graphics.Color.parseColor("#FFFAF7")
        window.decorView.systemUiVisibility =
            window.decorView.systemUiVisibility or
                    View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR or
                    View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR

        setContentView(R.layout.activity_cart)

        val root = findViewById<View>(R.id.main)
        ViewCompat.setOnApplyWindowInsetsListener(root) { view, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(0, bars.top, 0, bars.bottom)
            insets
        }

        cartItemsContainer = findViewById(R.id.cartItemsContainer)
        emptyCartView = findViewById(R.id.emptyCartView)
        cartScrollView = findViewById(R.id.cartScrollView)
        cartBottomBar = findViewById(R.id.cartBottomBar)

        subtotalText = findViewById(R.id.tvCartSubtotal)
        taxText = findViewById(R.id.tvCartTax)
        totalText = findViewById(R.id.tvCartTotal)
        checkoutButton = findViewById(R.id.btnCheckout)

        findViewById<ImageView>(R.id.btnBack).setOnClickListener {
            finish()
        }

        findViewById<TextView>(R.id.btnClearCart).setOnClickListener {
            CartManager.clearCart()
            renderCart()
        }

        findViewById<Button>(R.id.btnExploreMenu).setOnClickListener {
            finish()
        }

        checkoutButton.setOnClickListener {
            if (CartManager.isEmpty()) {
                Toast.makeText(
                    this,
                    "Your cart is empty!",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            // Save the order and clear the cart
            val order = OrderManager.placeOrder() ?: return@setOnClickListener
            val intent = Intent(
                this,
                OrderStatusActivity::class.java
            ).apply {
                putExtra("ticket_id", order.ticketId)
                putExtra("item_count", order.itemCount)
            }
            startActivity(intent)
            finish()
        }
        renderCart()
    }

    override fun onResume() {
        super.onResume()
        if (::cartItemsContainer.isInitialized) renderCart()
    }

    private fun renderCart() {
        cartItemsContainer.removeAllViews()

        val items = CartManager.getItems()
        val isEmpty = items.isEmpty()

        emptyCartView.visibility = if (isEmpty) View.VISIBLE else View.GONE
        cartScrollView.visibility = if (isEmpty) View.GONE else View.VISIBLE
        cartBottomBar.visibility = if (isEmpty) View.GONE else View.VISIBLE

        items.forEachIndexed { index, item ->
            cartItemsContainer.addView(createCartItemView(item, index))
        }

        val subtotal = CartManager.getSubtotal()
        val tax = subtotal * taxRate
        val total = subtotal + tax + if (isEmpty) 0.0 else deliveryFee

        subtotalText.text = formatPrice(subtotal)
        taxText.text = formatPrice(tax)
        totalText.text = formatPrice(total)

        checkoutButton.text =
            "Place Order Now  •  ${formatPrice(total)}"
    }

    private fun createCartItemView(
        item: CartItem,
        index: Int
    ): View {
        val card = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(10), dp(10), dp(10), dp(10))
            background = getDrawable(R.drawable.bg_cart_summary)
            setBackgroundTintList(
                android.content.res.ColorStateList.valueOf(
                    android.graphics.Color.WHITE
                )
            )
        }

        val details = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f
            )
        }

        details.addView(TextView(this).apply {
            text = item.menuItem.name
            textSize = 13f
            setTextColor(android.graphics.Color.parseColor("#211713"))
            setTypeface(null, android.graphics.Typeface.BOLD)
        })

        details.addView(TextView(this).apply {
            text = "${item.sizeLabel} • ${item.milkName} • ${item.sugarLevel}"
            textSize = 10f
            setTextColor(android.graphics.Color.parseColor("#947E73"))
            maxLines = 2
            setPadding(0, dp(3), 0, dp(3))
        })

        details.addView(TextView(this).apply {
            text = formatPrice(item.totalPrice)
            textSize = 11f
            setTextColor(android.graphics.Color.parseColor("#E54428"))
            setTypeface(null, android.graphics.Typeface.BOLD)
        })

        val quantityBox = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            setPadding(dp(3), dp(3), dp(3), dp(3))
            background = getDrawable(R.drawable.bg_cart_summary)
        }

        val minus = quantityButton("−")
        val quantity = TextView(this).apply {
            text = item.quantity.toString()
            textSize = 12f
            gravity = Gravity.CENTER
            setTextColor(android.graphics.Color.parseColor("#211713"))
            setTypeface(null, android.graphics.Typeface.BOLD)
            layoutParams = LinearLayout.LayoutParams(dp(22), dp(28))
        }
        val plus = quantityButton("+")

        minus.setOnClickListener {
            CartManager.updateQuantity(index, item.quantity - 1)
            renderCart()
        }

        plus.setOnClickListener {
            CartManager.updateQuantity(index, item.quantity + 1)
            renderCart()
        }

        quantityBox.addView(minus)
        quantityBox.addView(quantity)
        quantityBox.addView(plus)

        card.addView(details)
        card.addView(quantityBox)

        card.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            bottomMargin = dp(8)
        }

        return card
    }

    private fun quantityButton(label: String): TextView {
        return TextView(this).apply {
            text = label
            textSize = 13f
            gravity = Gravity.CENTER
            setTextColor(android.graphics.Color.parseColor("#38251E"))
            setTypeface(null, android.graphics.Typeface.BOLD)
            layoutParams = LinearLayout.LayoutParams(dp(22), dp(28))
            isClickable = true
            isFocusable = true
        }
    }

    private fun formatPrice(amount: Double): String {
        return String.format(Locale.US, "$%.2f", amount)
    }

    private fun dp(value: Int): Int {
        return (value * resources.displayMetrics.density).toInt()
    }
}