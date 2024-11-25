package com.example.ecoeats

import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.textview.MaterialTextView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class CartActivity : AppCompatActivity() {

    private lateinit var cartRecyclerView: RecyclerView
    private lateinit var checkoutButton: MaterialButton
    private lateinit var totalPriceTextView: MaterialTextView
    private lateinit var backButton: ImageView

    private lateinit var firestore: FirebaseFirestore
    private lateinit var cartItems: MutableList<MenuItem>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cart)

        // Initialize Firestore
        firestore = FirebaseFirestore.getInstance()

        // Initialize views
        cartRecyclerView = findViewById(R.id.cartRecyclerView)
        checkoutButton = findViewById(R.id.checkoutButton)
        totalPriceTextView = findViewById(R.id.totalPriceTextView)
        backButton = findViewById(R.id.backButton)

        // Set up RecyclerView
        cartRecyclerView.layoutManager = LinearLayoutManager(this)

        // Load cart items from Firestore
        loadCartItems()

        // Checkout button click listener
        checkoutButton.setOnClickListener {
            // Handle checkout logic here
            checkout()
        }

        // Back button click listener
        backButton.setOnClickListener {
            finish() // Closes CartActivity and returns to the previous activity
        }
    }

    private fun loadCartItems() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val cartRef = firestore.collection("carts").document(userId)

        cartRef.get().addOnSuccessListener { document ->
            if (document.exists()) {
                val cartItems = document.get("items") as? List<Map<String, Any>> ?: emptyList()
                val items = cartItems.map { item ->
                    MenuItem(
                        name = item["name"] as? String ?: "Unknown",
                        price = (item["price"] as? Double) ?: 0.0,
                        image = item["image"] as? String ?: "",
                        quantity = (item["quantity"] as? Long)?.toInt() ?: 1 // Use Long and convert to Int
                    )

                }.toMutableList()
                this.cartItems = items
                cartRecyclerView.adapter = CartAdapter(items) { menuItem ->
                    removeFromCart(menuItem)
                }

                // Calculate total price
                val totalPrice = items.sumOf { it.price * it.quantity }
                totalPriceTextView.text = "Total: $totalPrice IDR"
            } else {
                Toast.makeText(this, "No items in cart", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener { exception ->
            Toast.makeText(this, "Error loading cart: $exception", Toast.LENGTH_SHORT).show()
        }
    }

    private fun removeFromCart(menuItem: MenuItem) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val cartRef = firestore.collection("carts").document(userId)

        cartRef.get().addOnSuccessListener { document ->
            if (document.exists()) {
                val items = document.get("items") as? MutableList<Map<String, Any>>
                if (items != null) {
                    val itemToRemove = items.find { it["name"] == menuItem.name }
                    if (itemToRemove != null) {
                        items.remove(itemToRemove)
                        cartRef.update("items", items).addOnSuccessListener {
                            menuItem.quantity = 1 // Reset quantity to 1
                            cartItems.remove(menuItem)
                            (cartRecyclerView.adapter as CartAdapter).notifyDataSetChanged()
                            Toast.makeText(this, "${menuItem.name} removed from cart", Toast.LENGTH_SHORT).show()

                            // Update total price
                            updateTotalPrice()
                        }
                    }
                }
            }
        }.addOnFailureListener { exception ->
            Toast.makeText(this, "Error removing from cart: $exception", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateTotalPrice() {
        val totalPrice = cartItems.sumOf { it.price * it.quantity }
        totalPriceTextView.text = "Total: $totalPrice IDR"
    }

    private fun checkout() {
        // Implement checkout logic
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val orderRef = firestore.collection("orders").document(userId)

        // Save cart items as a new order in Firestore
        val order = mapOf(
            "items" to cartItems.map { item ->
                mapOf(
                    "name" to item.name,
                    "price" to item.price,
                    "image" to item.image,
                    "quantity" to item.quantity
                )
            },
            "totalPrice" to cartItems.sumOf { it.price * it.quantity },
            "status" to "Ongoing"
        )

        orderRef.set(order).addOnSuccessListener {
            // Clear the cart
            clearCart()
            Toast.makeText(this, "Checkout successful!", Toast.LENGTH_SHORT).show()
            // Navigate to order activity
            // startActivity(Intent(this, OrderActivity::class.java))
        }.addOnFailureListener { exception ->
            Toast.makeText(this, "Error during checkout: $exception", Toast.LENGTH_SHORT).show()
        }
    }

    private fun clearCart() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val cartRef = firestore.collection("carts").document(userId)

        cartRef.update("items", emptyList<Map<String, Any>>()).addOnSuccessListener {
            cartItems.clear()
            (cartRecyclerView.adapter as CartAdapter).notifyDataSetChanged()
            updateTotalPrice()
        }.addOnFailureListener { exception ->
            Toast.makeText(this, "Error clearing cart: $exception", Toast.LENGTH_SHORT).show()
        }
    }
}
