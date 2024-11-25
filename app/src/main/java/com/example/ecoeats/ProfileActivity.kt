package com.example.ecoeats

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ProfileActivity : AppCompatActivity() {

    private lateinit var tvUsername: TextView
    private lateinit var tvEmail: TextView
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        // Initialize views and Firestore instance
        tvUsername = findViewById(R.id.tvUsername)
        tvEmail = findViewById(R.id.tvEmail)
        firestore = FirebaseFirestore.getInstance()

        // Fetch and display user information
        val currentUser = FirebaseAuth.getInstance().currentUser
        currentUser?.let { user ->
            fetchUserData(user.uid)
        }

        // Handle logout
        findViewById<TextView>(R.id.tvLogout).setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show()
            navigateToLogin()
        }

        // Setup bottom navigation
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    val intent = Intent(this, HomeActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.nav_orders -> {
                    val intent = Intent(this, OrdersActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.nav_profile -> {
                    // Already on Profile, do nothing
                    true
                }
                else -> false
            }
        }
    }

    private fun fetchUserData(userId: String) {
        // Fetch username and email from Firestore based on user's UID
        val userRef = firestore.collection("users").document(userId)

        userRef.get().addOnSuccessListener { document ->
            if (document.exists()) {
                val username = document.getString("username")
                val email = document.getString("email")

                tvUsername.text = username ?: "Username not found"
                tvEmail.text = email ?: "Email not found"
            } else {
                Log.d("ProfileActivity", "No such document")
                tvUsername.text = "Username not found"
                tvEmail.text = "Email not found"
            }
        }.addOnFailureListener { exception ->
            Log.e("ProfileActivity", "Error fetching user data: $exception")
            tvUsername.text = "Error fetching username"
            tvEmail.text = "Error fetching email"
        }
    }

    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
