package com.example.brewkery_hardik.ui.home

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.brewkery_hardik.R
import com.example.brewkery_hardik.data.model.Category
import com.example.brewkery_hardik.data.repository.MenuRepository
import com.example.brewkery_hardik.ui.cart.CartActivity
import com.example.brewkery_hardik.ui.detail.ItemDetailActivity
import com.example.brewkery_hardik.ui.order.OrderHistoryActivity
import com.example.brewkery_hardik.utils.CartManager
import com.example.brewkery_hardik.viewmodel.MenuViewModel
import com.example.brewkery_hardik.viewmodel.MenuViewModelFactory
import kotlinx.coroutines.launch
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var viewModel: MenuViewModel
    private lateinit var menuAdapter: MenuAdapter

    private lateinit var menuRecyclerView: RecyclerView
    private lateinit var progressBar: View
    private lateinit var errorContainer: View
    private lateinit var tvErrorMessage: TextView
    private lateinit var tvEmptyState: TextView
    private lateinit var tvItemCount: TextView
    private lateinit var tvDeliveryTime: TextView
    private lateinit var tvDeliveryFee: TextView
    private lateinit var categoryContainer: LinearLayout
    private lateinit var etSearch: EditText
    private lateinit var btnClearSearch: View

    private var selectedCategoryId: String? = null

    private var categories: List<Category> = emptyList()
    private var renderedCategoryIds: List<String>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = Color.TRANSPARENT
        WindowInsetsControllerCompat(window, window.decorView)
            .isAppearanceLightStatusBars = true

        setContentView(R.layout.activity_main)

        val rootView = findViewById<View>(R.id.main)
        ViewCompat.setOnApplyWindowInsetsListener(rootView) { view, insets ->
            val systemBars = insets.getInsets(
                WindowInsetsCompat.Type.systemBars()
            )
            view.setPadding(
                systemBars.left,
                systemBars.top,
                systemBars.right,
                0
            )
            insets
        }

        initializeViews()
        setupViewModel()
        setupRecyclerView()
        setupSearch()
        setupButtons()
        observeViewModel()

        viewModel.loadMenu()
    }

    private fun initializeViews() {
        menuRecyclerView = findViewById(R.id.menuRecyclerView)
        progressBar = findViewById(R.id.progressBar)
        errorContainer = findViewById(R.id.errorContainer)
        tvErrorMessage = findViewById(R.id.tvErrorMessage)
        tvEmptyState = findViewById(R.id.tvEmptyState)
        tvItemCount = findViewById(R.id.tvItemCount)
        tvDeliveryTime = findViewById(R.id.tvDeliveryTime)
        tvDeliveryFee = findViewById(R.id.tvDeliveryFee)
        categoryContainer = findViewById(R.id.categoryContainer)
        etSearch = findViewById(R.id.etSearch)
        btnClearSearch = findViewById(R.id.btnClearSearch)
    }

    private fun setupViewModel() {
        val repository = MenuRepository()
        val factory = MenuViewModelFactory(repository)

        viewModel = ViewModelProvider(
            this,
            factory
        )[MenuViewModel::class.java]
    }

    private fun setupRecyclerView() {
        menuAdapter = MenuAdapter { item ->
            val intent = Intent(this, ItemDetailActivity::class.java)
            intent.putExtra("item_id", item.id)
            startActivity(intent)
        }

        menuRecyclerView.layoutManager = LinearLayoutManager(this)
        menuRecyclerView.adapter = menuAdapter

        // The RecyclerView is inside the existing ScrollView.
        menuRecyclerView.isNestedScrollingEnabled = false
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->

                    progressBar.visibility =
                        if (state.isLoading) View.VISIBLE else View.GONE

                    errorContainer.visibility =
                        if (state.errorMessage != null) {
                            View.VISIBLE
                        } else {
                            View.GONE
                        }

                    if (state.errorMessage != null) {
                        tvErrorMessage.text = state.errorMessage
                    }

                    // Update categories only when the API data changes.
                    if (categories != state.categories) {
                        categories = state.categories
                        createCategoryChips(categories)
                    }

                    // Store information comes from the API.
                    state.metadata?.let { metadata ->
                        tvDeliveryTime.text =
                            "Delivery in ${metadata.estimatedDeliveryTime}"

                        tvDeliveryFee.text = String.format(
                            Locale.US,
                            "$%.2f flat fee",
                            metadata.deliveryFee
                        )
                    }

                    // Update product cards.
                    menuAdapter.submitItems(state.items)

                    val hasError = state.errorMessage != null
                    val isEmpty = state.items.isEmpty() &&
                            !state.isLoading && !hasError

                    menuRecyclerView.visibility =
                        if (state.items.isNotEmpty() &&
                            !state.isLoading && !hasError
                        ) {
                            View.VISIBLE
                        } else {
                            View.GONE
                        }

                    tvEmptyState.visibility =
                        if (isEmpty) View.VISIBLE else View.GONE

                    if (isEmpty) {
                        tvEmptyState.text = "No items found."
                    }

                    tvItemCount.text = when {
                        state.isLoading -> "Loading menu..."
                        hasError -> "Menu unavailable"
                        else -> "${state.items.size} items"
                    }
                }
            }
        }
    }

    private fun setupSearch() {
        etSearch.addTextChangedListener(object : TextWatcher {

            override fun beforeTextChanged(
                s: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) = Unit

            override fun onTextChanged(
                s: CharSequence?,
                start: Int,
                before: Int,
                count: Int
            ) {
                val query = s?.toString().orEmpty()

                btnClearSearch.visibility =
                    if (query.isNotBlank()) View.VISIBLE else View.GONE

                viewModel.searchItems(query)
            }

            override fun afterTextChanged(s: Editable?) = Unit
        })

        btnClearSearch.setOnClickListener {
            etSearch.text.clear()
        }
    }

    private fun createCategoryChips(categories: List<Category>) {
        val newIds = categories.map { it.id }

        if (renderedCategoryIds == newIds) return

        renderedCategoryIds = newIds
        categoryContainer.removeAllViews()

        addCategoryChip(
            label = "All Items",
            categoryId = null,
            icon = null
        )

        categories.forEach { category ->
            addCategoryChip(
                label = category.name,
                categoryId = category.id,
                icon = category.icon
            )
        }
    }

    private fun addCategoryChip(
        label: String,
        categoryId: String?,
        icon: String?
    ) {
        val chip = TextView(this).apply {
            text = if (icon.isNullOrBlank()) label else "$icon $label"
            tag = categoryId

            gravity = Gravity.CENTER
            textSize = 13f

            setPadding(
                dpToPx(14),
                0,
                dpToPx(14),
                0
            )

            isClickable = true
            isFocusable = true

            setOnClickListener {
                selectedCategoryId = categoryId
                viewModel.selectCategory(categoryId)
                updateCategoryChipStyles()
            }
        }

        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            dpToPx(38)
        )

        if (categoryContainer.childCount > 0) {
            params.marginStart = dpToPx(8)
        }

        categoryContainer.addView(chip, params)
        updateCategoryChipStyles()
    }


    private fun updateCategoryChipStyles() {
        for (index in 0 until categoryContainer.childCount) {
            val chip = categoryContainer.getChildAt(index) as TextView
            val chipCategoryId = chip.tag as? String

            val isSelected = chipCategoryId == selectedCategoryId

            chip.background = ContextCompat.getDrawable(
                this,
                if (isSelected) {
                    R.drawable.bg_category_selected
                } else {
                    R.drawable.bg_category
                }
            )

            chip.setTextColor(
                ContextCompat.getColor(
                    this,
                    if (isSelected) {
                        R.color.white
                    } else {
                        R.color.text_secondary
                    }
                )
            )
        }
    }


    private fun setupButtons() {
        findViewById<View>(R.id.btnRetry).setOnClickListener {
            viewModel.loadMenu()
        }

        findViewById<View>(R.id.btnCart).setOnClickListener {
            startActivity(
                Intent(this, CartActivity::class.java)
            )
        }

        findViewById<View>(R.id.btnStoreInfo).setOnClickListener {
            Toast.makeText(
                this,
                "Delivery information is shown above.",
                Toast.LENGTH_SHORT
            ).show()
        }

        findViewById<ImageView>(R.id.btnOrderHistory).setOnClickListener {
            startActivity(
                Intent(this, OrderHistoryActivity::class.java)
            )
        }
    }

    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }

    override fun onResume() {
        super.onResume()
        updateCartCount()
    }

    private fun updateCartCount() {
        findViewById<TextView>(R.id.tvCartBadge).text = CartManager.getItemCount().toString()
    }
}