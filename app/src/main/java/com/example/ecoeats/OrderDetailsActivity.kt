package com.example.ecoeats

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.ecoeats.databinding.ActivityOrderDetailsBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class OrderDetailsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOrderDetailsBinding
    private lateinit var order: Order

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOrderDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        order = intent.getParcelableExtra("order")!!

        binding.orderItemsRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@OrderDetailsActivity)
            adapter = OrderItemAdapter(order.items)
        }

        binding.finishOrderButton.setOnClickListener {
            finishOrder()
        }
    }

    private fun finishOrder() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val orderRef = FirebaseFirestore.getInstance().collection("orders").document(userId)

        orderRef.update("status", "History")
            .addOnSuccessListener {
                Toast.makeText(this, "Order marked as History", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to update order: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
