package com.example.brewkery_hardik.ui.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.brewkery_hardik.R
import com.example.brewkery_hardik.data.model.MenuItem
import java.util.Locale

class MenuAdapter(
    private val onCustomizeClick: (MenuItem) -> Unit
) : RecyclerView.Adapter<MenuAdapter.MenuViewHolder>() {

    private val items = mutableListOf<MenuItem>()

    // Holds references to the views in item_menu.xml
    class MenuViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {

        val productImage: ImageView =
            itemView.findViewById(R.id.ivProductImage)

        val productBadge: TextView =
            itemView.findViewById(R.id.tvProductBadge)

        val productName: TextView =
            itemView.findViewById(R.id.tvProductName)

        val productRating: TextView =
            itemView.findViewById(R.id.tvProductRating)

        val productPrice: TextView =
            itemView.findViewById(R.id.tvProductPrice)

        val customizeButton: TextView =
            itemView.findViewById(R.id.btnCustomize)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MenuViewHolder {

        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_menu, parent, false)

        return MenuViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: MenuViewHolder,
        position: Int
    ) {
        val item = items[position]

        // Display product information from the API
        holder.productName.text = item.name

        holder.productRating.text = String.format(
            Locale.US,
            "★ %.2f (%d)",
            item.rating,
            item.reviewCount
        )

        holder.productPrice.text = String.format(
            Locale.US,
            "$%.2f",
            item.basePrice
        )

        // Show the badge only when one is available
        if (item.badge.isBlank()) {
            holder.productBadge.visibility = View.GONE
        } else {
            holder.productBadge.visibility = View.VISIBLE
            holder.productBadge.text = item.badge
        }

        // Load the product image from its URL using Coil
        holder.productImage.load(item.imageUrl) {
            crossfade(true)
        }

        // Pass the selected product to the detail-screen handler
        holder.customizeButton.setOnClickListener {
            onCustomizeClick(item)
        }
    }

    override fun getItemCount(): Int = items.size

    // Update the list when API data arrives
    fun submitItems(newItems: List<MenuItem>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }
}