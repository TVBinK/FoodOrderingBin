package com.example.foododering

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.foododering.databinding.ActivityDetailsBinding
import com.example.foododering.model.CartItems
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.MutableData
import com.google.firebase.database.Transaction


class DetailsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDetailsBinding
    private var foodName: String? = null
    private var foodPrice: String? = null
    private var foodImage: String? = null
    private var foodDescription: String? = null
    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.btnBackDetails.setOnClickListener {
            finish()
        }
        //khoi tao firebase
        auth = FirebaseAuth.getInstance()
        //lay du lieu tu intent
        foodName = intent.getStringExtra("foodName")
        foodPrice = intent.getStringExtra("foodPrice")
        foodImage = intent.getStringExtra("foodImage")
        foodDescription = intent.getStringExtra("foodDescription")
        with(binding) {
            tvFoodNameDetail.text = foodName
            Glide.with(this@DetailsActivity).load(Uri.parse(foodImage)).into(imageViewfoodImage)
            tvDesciption.text = foodDescription
            tvPrice.text = foodPrice
        }
        binding.btnAddCart.setOnClickListener {
            addtoupdateCart()
        }
    }

    private fun addtoupdateCart() {
        val userId = auth.currentUser?.uid ?: run {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            return
        }

        val cartRef = FirebaseDatabase.getInstance().reference
            .child("users").child(userId).child("CartItems")

        cartRef.child(foodName ?: return).runTransaction(object : Transaction.Handler {
            override fun doTransaction(currentData: MutableData): Transaction.Result {
                val currentItem = currentData.getValue(CartItems::class.java)
                if (currentItem != null) {
                    // Tăng số lượng
                    currentItem.foodQuantity = currentItem.foodQuantity?.plus(1)
                    currentData.value = currentItem
                } else {
                    // Thêm mới
                    val newCartItem = CartItems(
                        foodName = foodName ?: "",
                        foodPrice = foodPrice ?: "",
                        foodImage = foodImage ?: "",
                        foodDescription = foodDescription ?: "",
                        foodQuantity = 1
                    )
                    currentData.value = newCartItem
                }
                return Transaction.success(currentData)
            }

            override fun onComplete(
                error: DatabaseError?,
                committed: Boolean,
                currentData: DataSnapshot?
            ) {
                if (error != null) {
                    Toast.makeText(
                        this@DetailsActivity,
                        "Failed: ${error.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    // Log toàn bộ dữ liệu của cart item
                    val cartItem = currentData?.getValue(CartItems::class.java)
                    Log.d("xemPriceinchua", "Cart Item Added: $cartItem")
                    Toast.makeText(this@DetailsActivity, "Cart updated", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

}