package com.example.ecoeats

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class OrdersActivity : AppCompatActivity() {

    private lateinit var viewPager: ViewPager2
    private lateinit var tabLayout: TabLayout
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_orders)

        viewPager = findViewById(R.id.viewPager)
        tabLayout = findViewById(R.id.tabLayout)
        firestore = FirebaseFirestore.getInstance()

        val adapter = OrdersPagerAdapter(this)
        viewPager.adapter = adapter

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "History"
                1 -> "Ongoing"
                else -> null
            }
        }.attach()

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    val intent = Intent(this, HomeActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.nav_orders -> {
                    // Already on Orders, do nothing
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

        fetchOrders()
    }

    private fun fetchOrders() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        Log.d("OrdersActivity", "User ID: $userId")
        val ordersRef = firestore.collection("orders").document(userId)

        ordersRef.get().addOnSuccessListener { document ->
            if (document.exists()) {
                Log.d("OrdersActivity", "Document data: ${document.data}")
                val totalPrice = (document.get("totalPrice") as? Double) ?: 0.0
                val status = document.getString("status") ?: ""
                val items = (document.get("items") as? List<Map<String, Any>>)?.map { item ->
                    OrderItem(
                        name = item["name"] as? String ?: "",
                        price = (item["price"] as? Double) ?: 0.0,
                        image = item["image"] as? String ?: "",
                        quantity = (item["quantity"] as? Long)?.toInt() ?: 1
                    )
                } ?: emptyList()

                val orderObj = Order(
                    items = items,
                    status = status,
                    totalPrice = totalPrice
                )

                val ongoingOrders = mutableListOf<Order>()
                val historyOrders = mutableListOf<Order>()

                if (status == "Ongoing") {
                    ongoingOrders.add(orderObj)
                } else {
                    historyOrders.add(orderObj)
                }

                Log.d("OrdersActivity", "Ongoing Orders: $ongoingOrders")
                Log.d("OrdersActivity", "History Orders: $historyOrders")

                // Update ViewPager adapter with fetched orders
                (viewPager.adapter as? OrdersPagerAdapter)?.setOrders(ongoingOrders, historyOrders)
            } else {
                Log.d("OrdersActivity", "No such document")
            }
        }.addOnFailureListener { exception ->
            Log.e("OrdersActivity", "Error fetching orders: $exception")
        }
    }
}
