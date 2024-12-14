package com.example.foododering

import OrderDetails
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.foododering.databinding.ActivityPayoutBinding

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class PayoutActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var databaseReference: DatabaseReference
    private lateinit var binding: ActivityPayoutBinding
    private lateinit var userId: String

    private var name: String = ""
    private var address: String = ""
    private var phone: String = ""
    private var totalAmount: String = ""
    private var orderNumber: Int = 1

    private lateinit var foodItemName: List<String>
    private lateinit var foodItemPrice: List<String>
    private lateinit var foodItemImage: List<String>
    private lateinit var foodItemQuantities: List<Int>
    private lateinit var foodDescription: List<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPayoutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Firebase Initialization
        auth = FirebaseAuth.getInstance()
        databaseReference = FirebaseDatabase.getInstance().reference
        userId = auth.currentUser?.uid.orEmpty()

        // Initialize user data
        setUserData()

        // Retrieve data from Intent
        totalAmount = intent.getStringExtra("TotalAmount").orEmpty()
        foodItemName = intent.getStringArrayListExtra("FoodItemName") ?: emptyList()
        foodItemPrice = intent.getStringArrayListExtra("FoodItemPrice") ?: emptyList()
        foodItemImage = intent.getStringArrayListExtra("FoodItemImage") ?: emptyList()
        foodItemQuantities = intent.getIntegerArrayListExtra("FoodItemQuantities") ?: emptyList()
        foodDescription = intent.getStringArrayListExtra("FoodItemDescription") ?: emptyList()

        binding.editTextTotalAmount.setText(totalAmount)

        // Set button listeners
        binding.btnBack2.setOnClickListener { finish() }

        binding.btnPlaceMyOrder.setOnClickListener {
            name = binding.editTextName.text.toString().trim()
            address = binding.editTextAddress.text.toString().trim()
            phone = binding.editTextPhone.text.toString().trim()

            if (validateInput()) {
                fetchOrderNumberAndPlaceOrder()
            }
        }
    }

    private fun validateInput(): Boolean {
        return if (name.isBlank() || address.isBlank() || phone.isBlank()) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show()
            false
        } else {
            true
        }
    }

    private fun fetchOrderNumberAndPlaceOrder() {
        val numberReference = databaseReference.child("users").child(userId).child("OrderNumber")
        numberReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                orderNumber = snapshot.getValue(Int::class.java) ?: 1
                placeOrder()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(
                    this@PayoutActivity,
                    "Lỗi khi lấy số thứ tự đơn hàng: ${error.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    private fun placeOrder() {
        val time = System.currentTimeMillis()
        val itemPushKey = databaseReference.child("users").child(userId).child("OrderDetails").push().key

        val orderDetails = OrderDetails(
            userUid = userId,
            userName = name,
            foodNames = foodItemName,
            foodImages = foodItemImage,
            foodPrices = foodItemPrice,
            foodQuantities = foodItemQuantities,
            foodDescription = foodDescription,
            address = address,
            totalPrice = totalAmount,
            phoneNumber = phone,
            orderAccepted = "Waiting",
            paymentReceived = false,
            itemPushKey = itemPushKey,
            currentTime = time,
            orderNumber = orderNumber
        )

        val userOrderReference = databaseReference.child("users").child(userId).child("OrderDetails").child(itemPushKey!!)
        userOrderReference.setValue(orderDetails).addOnSuccessListener {
            Toast.makeText(this, "Đặt hàng thành công", Toast.LENGTH_SHORT).show()
            updateOrderNumber()
            navigateToThankActivity()
            removeCartItems()
        }.addOnFailureListener {
            Toast.makeText(this, "Đặt hàng thất bại", Toast.LENGTH_SHORT).show()
        }

        val globalOrderReference = databaseReference.child("OrderDetails").child(itemPushKey!!)
        globalOrderReference.setValue(orderDetails).addOnFailureListener {
            Toast.makeText(this, "Không thể lưu đơn hàng vào nhánh chung", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateOrderNumber() {
        val numberReference = databaseReference.child("users").child(userId).child("OrderNumber")
        numberReference.setValue(orderNumber + 1).addOnFailureListener {
            Toast.makeText(
                this,
                "Không thể cập nhật số thứ tự đơn hàng: ${it.message}",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun removeCartItems() {
        val cartItemsReference = databaseReference.child("users").child(userId).child("CartItems")
        cartItemsReference.removeValue()
    }

    private fun navigateToThankActivity() {
        val intent = Intent(this, ThankActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun setUserData() {
        val userReference = databaseReference.child("users").child(userId)
        userReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    name = snapshot.child("name").getValue(String::class.java).orEmpty()
                    address = snapshot.child("address").getValue(String::class.java).orEmpty()
                    phone = snapshot.child("phone").getValue(String::class.java).orEmpty()

                    binding.editTextName.setText(name)
                    binding.editTextAddress.setText(address)
                    binding.editTextPhone.setText(phone)
                } else {
                    Toast.makeText(this@PayoutActivity, "Không tìm thấy dữ liệu người dùng", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@PayoutActivity, "Lỗi khi tải dữ liệu người dùng: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
