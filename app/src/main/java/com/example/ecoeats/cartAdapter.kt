package com.example.ecoeats

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.button.MaterialButton
import com.google.firebase.storage.FirebaseStorage

class CartAdapter(
    private val cartItems: List<MenuItem>,
    private val onDeleteClick: (MenuItem) -> Unit
) : RecyclerView.Adapter<CartAdapter.CartViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.cart_item, parent, false)
        return CartViewHolder(view)
    }

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        val cartItem = cartItems[position]
        holder.cartItemName.text = cartItem.name
        holder.cartItemPrice.text = "${cartItem.price} IDR"
        holder.cartItemQuantity.text = "Qty: ${cartItem.quantity}"

        // Load image from Firebase Storage
        val storageRef = FirebaseStorage.getInstance().reference.child(cartItem.image)
        storageRef.downloadUrl.addOnSuccessListener { uri ->
            Glide.with(holder.itemView.context).load(uri).into(holder.cartItemImage)
        }

        holder.deleteButton.setOnClickListener {
            onDeleteClick(cartItem)
        }
    }

    override fun getItemCount() = cartItems.size

    class CartViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cartItemImage: ImageView = itemView.findViewById(R.id.cartItemImage)
        val cartItemName: TextView = itemView.findViewById(R.id.cartItemName)
        val cartItemPrice: TextView = itemView.findViewById(R.id.cartItemPrice)
        val cartItemQuantity: TextView = itemView.findViewById(R.id.cartItemQuantity)
        val deleteButton: MaterialButton = itemView.findViewById(R.id.deleteButton)
    }
}
