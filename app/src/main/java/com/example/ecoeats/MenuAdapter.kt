package com.example.ecoeats

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.storage.FirebaseStorage

class MenuAdapter(private val menuItems: List<MenuItem>, private val addToCartCallback: (MenuItem) -> Unit) : RecyclerView.Adapter<MenuAdapter.MenuViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MenuViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.menu_item, parent, false)
        return MenuViewHolder(view)
    }

    override fun onBindViewHolder(holder: MenuViewHolder, position: Int) {
        val menuItem = menuItems[position]
        holder.menuItemName.text = menuItem.name
        holder.menuItemPrice.text = "${menuItem.price} IDR"

        // Load image from Firebase Storage
        val storageRef = FirebaseStorage.getInstance().reference.child(menuItem.image)
        storageRef.downloadUrl.addOnSuccessListener { uri ->
            Glide.with(holder.itemView.context).load(uri).into(holder.menuItemImage)
        }

        // Set add to cart button click listener
        holder.addToCartButton.setOnClickListener {
            addToCartCallback(menuItem)
        }
    }

    override fun getItemCount() = menuItems.size

    class MenuViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val menuItemImage: ImageView = itemView.findViewById(R.id.menuItemImage)
        val menuItemName: TextView = itemView.findViewById(R.id.menuItemName)
        val menuItemPrice: TextView = itemView.findViewById(R.id.menuItemPrice)
        val addToCartButton: ImageView = itemView.findViewById(R.id.addToCartButton)
    }
}
