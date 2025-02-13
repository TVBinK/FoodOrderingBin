package com.example.foododering.Fragment

import AdapterCart
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.foododering.PayoutActivity
import com.example.foododering.databinding.FragmentCartBinding
import com.example.foododering.model.CartItems
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.DecimalFormat

class CartFragment : Fragment() {
    private lateinit var binding: FragmentCartBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var foodNames: MutableList<String>
    private lateinit var foodPrices: MutableList<String>
    private lateinit var foodDescriptions: MutableList<String>
    private lateinit var foodImages: MutableList<String>
    private lateinit var foodQuantities: MutableList<Int>
    private lateinit var cartAdapter: AdapterCart
    private lateinit var userId: String
    private lateinit var totalAmount: String
    private lateinit var subTotal: String
    private lateinit var deliveryCharge: String
    private var subTotalValue: Double = 0.0
    private val decimalFormat = DecimalFormat("#.##")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentCartBinding.inflate(layoutInflater, container, false)
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        userId = auth.currentUser?.uid ?: ""

        retrieveCartItems()

        binding.btnPlaceMyOrder.setOnClickListener {
            getOrderItemDetail()
        }

        return binding.root
    }

    private fun retrieveCartItems() {
        val foodReference: DatabaseReference =
            database.reference.child("users").child(userId).child("CartItems")
        foodNames = mutableListOf()
        foodPrices = mutableListOf()
        foodDescriptions = mutableListOf()
        foodImages = mutableListOf()
        foodQuantities = mutableListOf()

        foodReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // Clear old data before adding new
                foodNames.clear()
                foodPrices.clear()
                foodDescriptions.clear()
                foodImages.clear()
                foodQuantities.clear()

                for (foodSnapshot in snapshot.children) {
                    val cartItems = foodSnapshot.getValue(CartItems::class.java)
                    cartItems?.foodName?.let { foodNames.add(it) }
                    cartItems?.foodPrice?.let { foodPrices.add(it) }
                    cartItems?.foodDescription?.let { foodDescriptions.add(it) }
                    cartItems?.foodImage?.let { foodImages.add(it) }
                    cartItems?.foodQuantity?.let { foodQuantities.add(it) }
                }

                // Update adapter and UI
                setupAdapter()

                // Recalculate the total amounts after data changes
                subTotalValue = calculateTotalAmount(foodPrices, foodQuantities)
                subTotal = "${decimalFormat.format(subTotalValue)}$"
                binding.tvSubTotal.text = subTotal

                deliveryCharge = "1.00$"
                binding.tvCharge.text = deliveryCharge

                val totalAmountValue = subTotalValue + 1.0
                totalAmount = "${decimalFormat.format(totalAmountValue)}$"
                binding.tvTotal.text = totalAmount

                // Check if cart is empty and update UI
                if (foodNames.isEmpty()) {
                    binding.imgEmptyCart.visibility = View.VISIBLE
                    binding.imgCartTotal.visibility = View.GONE
                } else {
                    binding.imgEmptyCart.visibility = View.GONE
                    binding.imgCartTotal.visibility = View.VISIBLE
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle cancellation, if needed
            }
        })
    }

    private fun setupAdapter() {
        context?.let { ctx ->
            cartAdapter = AdapterCart(
                ctx,
                foodNames,
                foodImages,
                foodPrices,
                foodQuantities,
                foodDescriptions
            )

            binding.RecycleViewCart.layoutManager =
                LinearLayoutManager(ctx, LinearLayoutManager.VERTICAL, false)
            binding.RecycleViewCart.adapter = cartAdapter
        }
    }

    private fun getOrderItemDetail() {
        val orderIdReference: DatabaseReference =
            database.reference.child("users").child(userId).child("CartItems")
        val foodName = mutableListOf<String>()
        val foodPrice = mutableListOf<String>()
        val foodDescription = mutableListOf<String>()
        val foodImage = mutableListOf<String>()
        val foodQuantity = cartAdapter.getItemQuantities()

        orderIdReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (foodSnapshot in snapshot.children) {
                    val cartItems = foodSnapshot.getValue(CartItems::class.java)
                    cartItems?.foodName?.let { foodName.add(it) }
                    cartItems?.foodPrice?.let { foodPrice.add(it) }
                    cartItems?.foodDescription?.let { foodDescription.add(it) }
                    cartItems?.foodImage?.let { foodImage.add(it) }
                }
                orderNow(foodName, foodPrice, foodDescription, foodImage, foodQuantity)
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle cancellation
            }
        })
    }

    private fun orderNow(
        foodName: MutableList<String>,
        foodPrice: MutableList<String>,
        foodDescription: MutableList<String>,
        foodImage: MutableList<String>,
        foodQuantities: MutableList<Int>
    ) {
        if (isAdded && context != null) {
            val intent = Intent(requireContext(), PayoutActivity::class.java)
            intent.putExtra("FoodItemName", foodName as ArrayList<String>)
            intent.putExtra("FoodItemPrice", foodPrice as ArrayList<String>)
            intent.putExtra("FoodItemDescription", foodDescription as ArrayList<String>)
            intent.putExtra("FoodItemImage", foodImage as ArrayList<String>)
            intent.putExtra("FoodItemQuantities", foodQuantities as ArrayList<Int>)
            intent.putExtra("TotalAmount", totalAmount)
            startActivity(intent)
        }
    }

    private fun calculateTotalAmount(
        foodPrice: MutableList<String>,
        foodQuantity: MutableList<Int>
    ): Double {
        var totalAmount = 0.0
        for (i in 0 until foodPrice.size) {
            var price: String = foodPrice[i]

            // Parse price value to Double
            val priceDoubleValue: Double = try {
                val lastChar = price.last()
                if (lastChar == '$') {
                    price.dropLast(1).toDouble()
                } else {
                    price.toDouble()
                }
            } catch (e: NumberFormatException) {
                // If conversion fails, assign default value
                0.0
            }

            // Get quantity and calculate total
            val quantity: Int = foodQuantity[i]
            totalAmount += priceDoubleValue * quantity
        }
        return totalAmount
    }
}