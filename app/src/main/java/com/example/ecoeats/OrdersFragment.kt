package com.example.ecoeats

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.ecoeats.databinding.FragmentOrdersBinding

class OrdersFragment : Fragment() {

    private var _binding: FragmentOrdersBinding? = null
    private val binding get() = _binding!!
    private val orderAdapter by lazy {
        OrderAdapter { order ->
            val intent = Intent(requireContext(), OrderDetailsActivity::class.java).apply {
                putExtra("order", order)
            }
            startActivity(intent)
        }
    }

    private var orders: List<Order> = emptyList()

    companion object {
        private const val ARG_ORDERS = "orders"

        fun newInstance(orders: List<Order>): OrdersFragment {
            val fragment = OrdersFragment()
            val args = Bundle().apply {
                putParcelableArrayList(ARG_ORDERS, ArrayList(orders))
            }
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            orders = it.getParcelableArrayList(ARG_ORDERS) ?: emptyList()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentOrdersBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.ordersRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = orderAdapter
        }

        orderAdapter.submitList(orders)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
