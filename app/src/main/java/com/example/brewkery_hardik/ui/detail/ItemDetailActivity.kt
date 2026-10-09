package com.example.brewkery_hardik.ui.detail

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import coil.load
import com.example.brewkery_hardik.R
import com.example.brewkery_hardik.data.model.MenuItem
import com.example.brewkery_hardik.data.model.SizeOption
import com.example.brewkery_hardik.data.model.MilkOption
import com.example.brewkery_hardik.data.repository.MenuRepository
import com.example.brewkery_hardik.utils.CartItem
import com.example.brewkery_hardik.utils.CartManager
import com.google.android.flexbox.FlexboxLayout
import kotlinx.coroutines.launch
import java.util.Locale

class ItemDetailActivity : AppCompatActivity() {

    private val repository = MenuRepository()

    private lateinit var productImage: ImageView
    private lateinit var productBadge: TextView
    private lateinit var productName: TextView
    private lateinit var productPrice: TextView
    private lateinit var productRating: TextView
    private lateinit var productDescription: TextView
    private lateinit var ingredientsContainer: FlexboxLayout
    private lateinit var sizeRadioGroup: RadioGroup
    private lateinit var milkRadioGroup: RadioGroup
    private lateinit var sugarRadioGroup: RadioGroup
    private lateinit var totalPrice: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var addToCartButton: TextView

    private var currentItem: MenuItem? = null
    private var selectedSize: SizeOption? = null
    private var selectedMilk: MilkOption? = null
    private var selectedSugar: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = Color.TRANSPARENT

        setContentView(R.layout.activity_item_detail)

        val rootView = findViewById<View>(R.id.main)

        ViewCompat.setOnApplyWindowInsetsListener(rootView) { view, insets ->
            val bars = insets.getInsets(
                WindowInsetsCompat.Type.systemBars()
            )
            view.setPadding(
                bars.left,
                bars.top,
                bars.right,
                bars.bottom
            )
            insets
        }

        bindViews()

        findViewById<TextView>(R.id.btnBack).setOnClickListener {
            finish()
        }

        loadItem()
    }

    private fun bindViews() {
        productImage = findViewById(R.id.ivDetailImage)
        productBadge = findViewById(R.id.tvDetailBadge)
        productName = findViewById(R.id.tvDetailName)
        productPrice = findViewById(R.id.tvDetailPrice)
        productRating = findViewById(R.id.tvDetailRating)
        productDescription = findViewById(R.id.tvDetailDescription)
        ingredientsContainer = findViewById(R.id.ingredientsContainer)
        sizeRadioGroup = findViewById(R.id.sizeRadioGroup)
        milkRadioGroup = findViewById(R.id.milkRadioGroup)
        sugarRadioGroup = findViewById(R.id.sugarRadioGroup)
        totalPrice = findViewById(R.id.tvTotalPrice)
        progressBar = findViewById(R.id.detailProgressBar)
        addToCartButton = findViewById(R.id.btnAddToCart)

        addToCartButton.setOnClickListener {
            val item = currentItem ?: return@setOnClickListener
            val selectedSizeId = sizeRadioGroup.checkedRadioButtonId
            val selectedMilkId = milkRadioGroup.checkedRadioButtonId
            val selectedSugarId = sugarRadioGroup.checkedRadioButtonId

            val size = item.customizations.sizes.find {
                it.id == (findViewById<RadioButton>(selectedSizeId)?.tag as? String)
            } ?: item.customizations.sizes.first()

            val milk = item.customizations.milkOptions.find {
                it.id == (findViewById<RadioButton>(selectedMilkId)?.tag as? String)
            } ?: item.customizations.milkOptions.first()

            val sugar = findViewById<RadioButton>(selectedSugarId)
                ?.tag as? String
                ?: item.customizations.sugarLevels.first()

            val price = item.basePrice + size.extraPrice + milk.extraPrice

            CartManager.addItem(
                CartItem(
                    menuItem = item,
                    sizeId = size.id,
                    sizeLabel = size.label,
                    milkId = milk.id,
                    milkName = milk.name,
                    sugarLevel = sugar,
                    unitPrice = price
                )
            )

            Toast.makeText(this, "Added to cart!", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun loadItem() {
        val itemId = intent.getIntExtra("item_id", -1)

        if (itemId == -1) {
            Toast.makeText(this, "Product ID is missing", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        lifecycleScope.launch {
            progressBar.visibility = View.VISIBLE

            repository.getItemById(itemId)
                .onSuccess { item ->
                    currentItem = item
                    displayItem(item)
                    progressBar.visibility = View.GONE
                }
                .onFailure { error ->
                    progressBar.visibility = View.GONE
                    Toast.makeText(
                        this@ItemDetailActivity,
                        error.message ?: "Unable to load product",
                        Toast.LENGTH_LONG
                    ).show()
                }
        }
    }

    private fun displayItem(item: MenuItem) {
        productName.text = item.name
        productPrice.text = formatPrice(item.basePrice)
        productRating.text = String.format(
            Locale.US,
            "★ %.2f (%d reviews)",
            item.rating,
            item.reviewCount
        )
        productDescription.text = item.description

        productImage.load(item.imageUrl) {
            crossfade(true)
        }

        if (item.badge.isBlank()) {
            productBadge.visibility = View.GONE
        } else {
            productBadge.text = item.badge
            productBadge.visibility = View.VISIBLE
        }

        ingredientsContainer.removeAllViews()
        item.ingredients.forEach { ingredient ->
            val chip = TextView(this).apply {
                text = ingredient
                setTextColor(getColor(R.color.coffee_brown))
                textSize = 11f
                setPadding(dp(9), dp(6), dp(9), dp(6))
                background = getDrawable(R.drawable.bg_product_card)
            }

            val params = FlexboxLayout.LayoutParams(
                FlexboxLayout.LayoutParams.WRAP_CONTENT,
                FlexboxLayout.LayoutParams.WRAP_CONTENT
            )
            params.setMargins(0, 0, dp(6), dp(6))
            ingredientsContainer.addView(chip, params)
        }

        setupSizeOptions(item)
        setupMilkOptions(item)
        setupSugarOptions(item)
        updateTotalPrice()
    }

    private fun setupSizeOptions(item: MenuItem) {
        sizeRadioGroup.removeAllViews()

        item.customizations.sizes.forEachIndexed { index, size ->
            val radio = RadioButton(this).apply {
                id = View.generateViewId()
                text = "${size.label}  (+${formatPrice(size.extraPrice)})"
                textSize = 13f
                tag = size
            }
            sizeRadioGroup.addView(radio)

            if (index == 0) {
                radio.isChecked = true
                selectedSize = size
            }
        }

        sizeRadioGroup.setOnCheckedChangeListener { group, checkedId ->
            selectedSize = group.findViewById<RadioButton>(checkedId)?.tag as? SizeOption
            updateTotalPrice()
        }
    }

    private fun setupMilkOptions(item: MenuItem) {
        milkRadioGroup.removeAllViews()

        item.customizations.milkOptions.forEachIndexed { index, milk ->
            val radio = RadioButton(this).apply {
                id = View.generateViewId()
                text = "${milk.name}  (+${formatPrice(milk.extraPrice)})"
                textSize = 13f
                tag = milk
            }
            milkRadioGroup.addView(radio)

            if (index == 0) {
                radio.isChecked = true
                selectedMilk = milk
            }
        }

        milkRadioGroup.setOnCheckedChangeListener { group, checkedId ->
            selectedMilk = group.findViewById<RadioButton>(checkedId)?.tag as? MilkOption
            updateTotalPrice()
        }
    }

    private fun setupSugarOptions(item: MenuItem) {
        sugarRadioGroup.removeAllViews()

        item.customizations.sugarLevels.forEachIndexed { index, sugar ->
            val radio = RadioButton(this).apply {
                id = View.generateViewId()
                text = sugar
                textSize = 13f
                tag = sugar
            }
            sugarRadioGroup.addView(radio)

            if (index == 0) {
                radio.isChecked = true
                selectedSugar = sugar
            }
        }

        sugarRadioGroup.setOnCheckedChangeListener { group, checkedId ->
            selectedSugar = group.findViewById<RadioButton>(checkedId)?.tag as? String
        }
    }

    private fun updateTotalPrice() {
        val item = currentItem ?: return

        val total = item.basePrice +
                (selectedSize?.extraPrice ?: 0.0) +
                (selectedMilk?.extraPrice ?: 0.0)

        totalPrice.text = formatPrice(total)
        productPrice.text = formatPrice(total)
    }

    private fun formatPrice(price: Double): String {
        return String.format(Locale.US, "$%.2f", price)
    }

    private fun dp(value: Int): Int {
        return (value * resources.displayMetrics.density).toInt()
    }
}