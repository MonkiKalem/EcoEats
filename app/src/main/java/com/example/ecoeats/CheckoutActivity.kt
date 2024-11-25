package com.example.ecoeats

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textview.MaterialTextView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class CheckoutActivity : AppCompatActivity() {

    private lateinit var firestore: FirebaseFirestore
    private lateinit var choosePaymentButton: MaterialButton
    private lateinit var subTotalTextView: MaterialTextView
    private lateinit var taxTextView: MaterialTextView
    private lateinit var grandTotalTextView: MaterialTextView
    private lateinit var restaurantNameTextView: MaterialTextView
    private lateinit var distanceTextView: MaterialTextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_checkout)

        firestore = FirebaseFirestore.getInstance()
        choosePaymentButton = findViewById(R.id.choosePaymentButton)
        subTotalTextView = findViewById(R.id.subTotalTextView)
        taxTextView = findViewById(R.id.taxTextView)
        grandTotalTextView = findViewById(R.id.grandTotalTextView)
        restaurantNameTextView = findViewById(R.id.restaurantNameTextView)
        distanceTextView = findViewById(R.id.distanceTextView)

        choosePaymentButton.setOnClickListener {
            completePayment()
        }

        // TODO: Populate the views with actual data
    }

    private fun completePayment() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        val userId = currentUser?.uid ?: return // Ensure we have a valid user ID

        val restaurantName = restaurantNameTextView.text.toString()
        val distance = distanceTextView.text.toString()
        val subTotal = subTotalTextView.text.toString().removePrefix("Rp ").toDoubleOrNull() ?: 0.0
        val tax = taxTextView.text.toString().removePrefix("Rp ").toDoubleOrNull() ?: 0.0
        val grandTotal = grandTotalTextView.text.toString().removePrefix("Rp ").toDoubleOrNull() ?: 0.0

        val cartRef = firestore.collection("carts").document(userId)
        cartRef.get().addOnSuccessListener { document ->
            if (document.exists()) {
                val cartItems = document.get("items") as? List<Map<String, Any>> ?: emptyList()

                val orderData = mapOf(
                    "restaurant" to restaurantName,
                    "distance" to distance,
                    "items" to cartItems,
                    "subTotal" to subTotal,
                    "tax" to tax,
                    "grandTotal" to grandTotal,
                    "status" to "Ongoing",
                    "timestamp" to FieldValue.serverTimestamp()
                )

                firestore.collection("orders").document(userId)
                    .update("ongoing", FieldValue.arrayUnion(orderData))
                    .addOnSuccessListener {
                        val intent = Intent(this, PaymentSuccessActivity::class.java)
                        startActivity(intent)
                        // Optionally clear the cart after successful order placement
                        cartRef.update("items", emptyList<Map<String, Any>>())
                    }
                    .addOnFailureListener { e ->
                        // Handle error
                    }
            }
        }.addOnFailureListener { e ->
            // Handle error
        }
    }
}
