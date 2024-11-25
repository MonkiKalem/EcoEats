package com.example.ecoeats

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class HomeActivity : AppCompatActivity() {

    private lateinit var storage: FirebaseStorage
    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var restaurantImage1: ImageView
    private lateinit var restaurantName1: TextView
    private lateinit var restaurantDistance1: TextView
    private lateinit var restaurantRating1: TextView
    private lateinit var restaurantImage2: ImageView
    private lateinit var restaurantName2: TextView
    private lateinit var restaurantDistance2: TextView
    private lateinit var restaurantRating2: TextView
    private lateinit var viewCartButton: MaterialButton

    private val cartItems = mutableListOf<MenuItem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        // Initialize Firestore, Storage, and Auth
        firestore = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()
        auth = FirebaseAuth.getInstance()

        // Initialize views for restaurant 1
        restaurantImage1 = findViewById(R.id.restaurantImage1)
        restaurantName1 = findViewById(R.id.restaurantName1)
        restaurantDistance1 = findViewById(R.id.restaurantDistance1)
        restaurantRating1 = findViewById(R.id.restaurantRating1)

        // Initialize views for restaurant 2
        restaurantImage2 = findViewById(R.id.restaurantImage2)
        restaurantName2 = findViewById(R.id.restaurantName2)
        restaurantDistance2 = findViewById(R.id.restaurantDistance2)
        restaurantRating2 = findViewById(R.id.restaurantRating2)

        // Initialize Cart Button
        viewCartButton = findViewById(R.id.viewCartButton)
        viewCartButton.setOnClickListener {
            val intent = Intent(this, CartActivity::class.java)
            intent.putParcelableArrayListExtra("cartItems", ArrayList(cartItems))
            startActivity(intent)
        }

        // Fetch seller data
        fetchSellerData("7TxuGsocyuCAFG7Y6BSl", restaurantImage1, restaurantName1, restaurantDistance1, restaurantRating1)
        fetchSellerData("ganNQKwHF0GSQ79qPIaj", restaurantImage2, restaurantName2, restaurantDistance2, restaurantRating2)

        // Add click listeners to restaurant views
        restaurantImage1.setOnClickListener {
            openRestaurant("7TxuGsocyuCAFG7Y6BSl")
        }

        restaurantImage2.setOnClickListener {
            openRestaurant("ganNQKwHF0GSQ79qPIaj")
        }

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    // Already on Home, do nothing
                    true
                }
                R.id.nav_orders -> {
                    val intent = Intent(this, OrdersActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.nav_profile -> {
                    val intent = Intent(this, ProfileActivity::class.java)
                    startActivity(intent)
                    true
                }
                else -> false
            }
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

    private fun updateCartButton() {
        if (cartItems.isNotEmpty()) {
            viewCartButton.text = "${cartItems.size} Items - View Cart"
            viewCartButton.visibility = View.VISIBLE
        } else {
            viewCartButton.visibility = View.GONE
        }
    }

    private fun fetchSellerData(documentId: String, restaurantImage: ImageView, restaurantName: TextView, restaurantDistance: TextView, restaurantRating: TextView) {
        firestore.collection("sellers").document(documentId).get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    val bannerImage = document.getString("bannerImage")
                    val name = document.getString("name")
                    val location = document.getGeoPoint("location")
                    val username = document.getString("username")
                    val password = document.getString("password")

                    // Log the fetched data for debugging
                    Log.d("HomeActivity", "Fetched data for documentId: $documentId")
                    Log.d("HomeActivity", "Name: $name, BannerImage: $bannerImage")

                    // Update UI with seller data
                    if (bannerImage != null) {
                        loadBannerImage(bannerImage, restaurantImage)
                    }
                    restaurantName.text = name
                    restaurantDistance.text = "1.0 km" // You can calculate the actual distance if needed
                    restaurantRating.text = "4.8 reviews" // You can update this to fetch actual reviews
                } else {
                    Toast.makeText(this, "No such document", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Error getting documents: $exception", Toast.LENGTH_SHORT).show()
                Log.e("HomeActivity", "Error getting document: ", exception)
            }
    }

    private fun loadBannerImage(imagePath: String, imageView: ImageView) {
        val storageRef = storage.reference.child(imagePath)
        storageRef.downloadUrl.addOnSuccessListener { uri ->
            Glide.with(this).load(uri).into(imageView)
        }.addOnFailureListener { exception ->
            Toast.makeText(this, "Error loading image: $exception", Toast.LENGTH_SHORT).show()
            Log.e("HomeActivity", "Error loading image: ", exception)
        }
    }

    private fun openRestaurant(documentId: String) {
        val intent = Intent(this, RestaurantActivity::class.java)
        intent.putExtra("documentId", documentId)
        startActivity(intent)
    }
}
