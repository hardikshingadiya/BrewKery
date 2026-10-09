package com.example.brewkery_hardik.ui.order

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import com.example.brewkery_hardik.R
import com.example.brewkery_hardik.utils.Order
import com.example.brewkery_hardik.utils.OrderManager
import java.util.Locale

class OrderHistoryActivity : AppCompatActivity() {

    private lateinit var ordersContainer: LinearLayout
    private lateinit var emptyOrdersView: LinearLayout
    private lateinit var ordersScrollView: ScrollView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = Color.TRANSPARENT
        window.navigationBarColor = Color.parseColor("#FFFAF7")

        window.decorView.systemUiVisibility =
            window.decorView.systemUiVisibility or
                    View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR or
                    View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR

        setContentView(R.layout.activity_order_history)

        val root = findViewById<View>(R.id.main)

        ViewCompat.setOnApplyWindowInsetsListener(root) { view, insets ->
            val bars = insets.getInsets(
                WindowInsetsCompat.Type.systemBars()
            )
            view.setPadding(0, bars.top, 0, bars.bottom)
            insets
        }

        ordersContainer = findViewById(R.id.ordersContainer)
        emptyOrdersView = findViewById(R.id.emptyOrdersView)
        ordersScrollView = findViewById(R.id.ordersScrollView)

        findViewById<ImageView>(R.id.btnBack).setOnClickListener {
            finish()
        }

        renderOrders()
    }

    override fun onResume() {
        super.onResume()

        if (::ordersContainer.isInitialized) {
            renderOrders()
        }
    }

    private fun renderOrders() {
        ordersContainer.removeAllViews()

        val orders = OrderManager.getOrders()

        emptyOrdersView.visibility =
            if (orders.isEmpty()) View.VISIBLE else View.GONE

        ordersScrollView.visibility =
            if (orders.isEmpty()) View.GONE else View.VISIBLE

        orders.forEach { order ->
            ordersContainer.addView(createOrderCard(order))
        }
    }

    private fun createOrderCard(order: Order): View {
        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(15), dp(14), dp(15), dp(14))
            background = getDrawable(R.drawable.bg_cart_summary)
            isClickable = true
            isFocusable = true
        }

        val header = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        val ticket = TextView(this).apply {
            text = order.ticketId
            textSize = 14f
            setTextColor(Color.parseColor("#211713"))
            setTypeface(null, Typeface.BOLD)
            layoutParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            )
        }

        val status = TextView(this).apply {
            text = order.status
            textSize = 10f
            setTextColor(Color.parseColor("#A7470B"))
            setPadding(dp(10), dp(6), dp(10), dp(6))
            background = getDrawable(R.drawable.bg_preparing_badge)
        }

        header.addView(ticket)
        header.addView(status)
        card.addView(header)

        val itemNames = order.items.joinToString(", ") {
            "${it.menuItem.name} × ${it.quantity}"
        }

        card.addView(TextView(this).apply {
            text = itemNames
            textSize = 12f
            setTextColor(Color.parseColor("#947E73"))
            maxLines = 2
            setPadding(0, dp(10), 0, dp(10))
        })

        val footer = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        footer.addView(TextView(this).apply {
            text = "${order.itemCount} items"
            textSize = 11f
            setTextColor(Color.parseColor("#947E73"))
            layoutParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            )
        })

        footer.addView(TextView(this).apply {
            text = String.format(
                Locale.US,
                "$%.2f",
                order.totalAmount
            )
            textSize = 14f
            setTextColor(Color.parseColor("#E54428"))
            setTypeface(null, Typeface.BOLD)
        })

        card.addView(footer)

        card.addView(TextView(this).apply {
            text = "View Order  →"
            textSize = 11f
            setTextColor(Color.parseColor("#C65D32"))
            setTypeface(null, Typeface.BOLD)
            setPadding(0, dp(12), 0, 0)
        })

        card.setOnClickListener {
            val intent = Intent(
                this,
                OrderStatusActivity::class.java
            ).apply {
                putExtra("ticket_id", order.ticketId)
                putExtra("item_count", order.itemCount)
            }

            startActivity(intent)
        }

        card.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            bottomMargin = dp(12)
        }

        return card
    }

    private fun dp(value: Int): Int {
        return (value * resources.displayMetrics.density).toInt()
    }
}