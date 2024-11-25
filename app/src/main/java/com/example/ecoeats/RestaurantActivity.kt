package com.example.ecoeats

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.android.material.button.MaterialButton

class RestaurantActivity : AppCompatActivity() {

    private lateinit var storage: FirebaseStorage
    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var restaurantBanner: ImageView
    private lateinit var restaurantName: TextView
    private lateinit var restaurantDistance: TextView
    private lateinit var restaurantRating: TextView
    private lateinit var menuRecyclerView: RecyclerView
    private lateinit var viewCartButton: MaterialButton
    private lateinit var backButton: ImageView

    private val cartItems = mutableListOf<MenuItem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_restaurant)

        // Initialize Firebase
        firestore = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()
        auth = FirebaseAuth.getInstance()

        // Initialize views
        restaurantBanner = findViewById(R.id.restaurantBanner)
        restaurantName = findViewById(R.id.restaurantName)
        restaurantDistance = findViewById(R.id.restaurantDistance)
        restaurantRating = findViewById(R.id.restaurantRating)
        menuRecyclerView = findViewById(R.id.menuRecyclerView)
        viewCartButton = findViewById(R.id.viewCartButton)
        backButton = findViewById(R.id.backButton)

        // Set up RecyclerView
        menuRecyclerView.layoutManager = LinearLayoutManager(this)

        // Get the documentId from the intent
        val documentId = intent.getStringExtra("documentId")

        // Fetch restaurant data
        if (documentId != null) {
            fetchRestaurantData(documentId)
        } else {
            Toast.makeText(this, "No document ID provided", Toast.LENGTH_SHORT).show()
        }

        // View Cart Button click listener
        viewCartButton.setOnClickListener {
            val intent = Intent(this, CartActivity::class.java)
            intent.putParcelableArrayListExtra("cartItems", ArrayList(cartItems))
            startActivity(intent)
        }

        backButton.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
        }

        // Check for existing cart items
        checkCartItems()
    }

    private fun checkCartItems() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val userId = currentUser.uid
            val cartRef = firestore.collection("carts").document(userId)
            cartRef.get().addOnSuccessListener { document ->
                if (document.exists()) {
                    val items = document.get("items") as? List<Map<String, Any>>
                    if (items != null && items.isNotEmpty()) {
                        for (item in items) {
                            val price = when (val priceValue = item["price"]) {
                                is Double -> priceValue
                                is Long -> priceValue.toDouble()
                                else -> 0.0
                            }
                            val quantity = when (val quantityValue = item["quantity"]) {
                                is Long -> quantityValue.toInt()
                                is Int -> quantityValue
                                else -> 1
                            }
                            cartItems.add(MenuItem(
                                name = item["name"] as String,
                                price = price,
                                image = item["image"] as String,
                                quantity = quantity
                            ))
                        }
                        updateCartButton()
                    }
                }
            }.addOnFailureListener { exception ->
                Toast.makeText(this, "Error checking cart items: $exception", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun fetchRestaurantData(documentId: String) {
        firestore.collection("sellers").document(documentId).get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    val bannerImage = document.getString("bannerImage")
                    val name = document.getString("name")
                    val distance = document.getDouble("distance")
                    val rating = document.getDouble("rating")
                    val sellerId = document.id

                    // Update UI with restaurant data
                    if (bannerImage != null) {
                        loadBannerImage(bannerImage)
                    } else {
                        Toast.makeText(this, "No banner image found", Toast.LENGTH_SHORT).show()
                    }
                    restaurantName.text = name ?: "Unknown"
                    restaurantDistance.text = "${distance ?: 0.0} km"
                    restaurantRating.text = "${rating ?: 0.0} reviews"

                    // Fetch menu items
                    fetchMenuItems(sellerId)
                } else {
                    Toast.makeText(this, "No such document", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Error getting documents: $exception", Toast.LENGTH_SHORT).show()
            }
    }

    private fun fetchMenuItems(sellerId: String) {
        firestore.collection("menus").whereEqualTo("sellerId", sellerId).get()
            .addOnSuccessListener { documents ->
                val menuItems = documents.map { document ->
                    MenuItem(
                        name = document.getString("MenuName") ?: "Unknown",
                        price = document.getDouble("price") ?: 0.0,
                        image = document.getString("MenuImage") ?: ""
                    )
                }
                menuRecyclerView.adapter = MenuAdapter(menuItems) { menuItem ->
                    addToCart(menuItem)
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Error getting menu items: $exception", Toast.LENGTH_SHORT).show()
            }
    }

    private fun addToCart(menuItem: MenuItem) {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            return
        }
        val userId = currentUser.uid
        val cartRef = firestore.collection("carts").document(userId)

        cartRef.get().addOnSuccessListener { document ->
            if (document.exists()) {
                // Cart exists, update items
                val cartItems = document.get("items") as? MutableList<Map<String, Any>> ?: mutableListOf()
                val existingItem = cartItems.find { it["name"] == menuItem.name } as? MutableMap<String, Any>
                if (existingItem != null) {
                    val updatedQuantity = (existingItem["quantity"] as? Long ?: 1) + 1
                    existingItem["quantity"] = updatedQuantity
                } else {
                    val itemMap = menuItem.toMap()
                    cartItems.add(itemMap)
                }
                cartRef.update("items", cartItems).addOnSuccessListener {
                    updateCartButton()
                }
            } else {
                // Cart does not exist, create new cart
                val cartItems = mutableListOf(menuItem.toMap())
                val cartData = mapOf(
                    "userId" to userId,
                    "items" to cartItems
                )
                cartRef.set(cartData).addOnSuccessListener {
                    updateCartButton()
                }
            }
        }.addOnFailureListener { exception ->
            Toast.makeText(this, "Error adding to cart: $exception", Toast.LENGTH_SHORT).show()
        }

        val existingItem = this.cartItems.find { it.name == menuItem.name }
        if (existingItem != null) {
            existingItem.quantity += 1
        } else {
            menuItem.quantity = 1
            this.cartItems.add(menuItem)
        }
        updateCartButton()
    }

    private fun updateCartButton() {
        if (cartItems.isNotEmpty()) {
            viewCartButton.text = "${cartItems.size} Items - View Cart"
            viewCartButton.visibility = View.VISIBLE
        } else {
            viewCartButton.visibility = View.GONE
        }
    }

    private fun loadBannerImage(imagePath: String) {
        val storageRef = storage.reference.child(imagePath)
        storageRef.downloadUrl.addOnSuccessListener { uri ->
            Glide.with(this).load(uri).into(restaurantBanner)
        }.addOnFailureListener { exception ->
            Toast.makeText(this, "Error loading image: $exception", Toast.LENGTH_SHORT).show()
        }
    }
}

private fun MenuItem.toMap(): Map<String, Any> {
    return mapOf(
        "name" to name,
        "price" to price,
        "image" to image,
        "quantity" to quantity // Add quantity field if needed
    )
}
