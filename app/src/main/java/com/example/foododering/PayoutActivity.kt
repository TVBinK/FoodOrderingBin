package com.example.foododering

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.foododering.databinding.ActivityPayoutBinding
import com.example.foododering.model.OrderDetails
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class PayoutActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var databaseReference: DatabaseReference
    private lateinit var binding: ActivityPayoutBinding
    private lateinit var name: String
    private lateinit var address: String
    private lateinit var phone: String
    private lateinit var totalAmount: String
    private lateinit var foodItemName: ArrayList<String>
    private lateinit var foodItemPrice: ArrayList<String>
    private lateinit var foodItemImage: ArrayList<String>
    private lateinit var foodItemQuantities: ArrayList<Int>
    private lateinit var userId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPayoutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Khởi tạo Firebase
        auth = FirebaseAuth.getInstance()
        databaseReference = FirebaseDatabase.getInstance().reference

        // Thiết lập dữ liệu người dùng
        setUserData()
        // Initialize them with empty lists
        foodItemName = ArrayList()
        foodItemPrice = ArrayList()
        foodItemImage = ArrayList()
        foodItemQuantities = ArrayList()

        val intent = intent
        totalAmount = intent.getStringExtra("TotalAmount") ?: ""
        binding.editTextTotalAmount.setText(totalAmount)
        binding.btnBack2.setOnClickListener {
            finish()
        }
        binding.btnPlaceMyOrder.setOnClickListener {
            name = binding.editTextName.text.toString().trim()
            address = binding.editTextAddress.text.toString().trim()
            phone = binding.editTextPhone.text.toString().trim()
            if (name.isBlank() && address.isBlank() && phone.isBlank()) {
                Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show()
            } else {
                placeOrder()
            }
        }
    }

    private fun placeOrder() {
        userId = auth.currentUser?.uid ?: ""
        val time = System.currentTimeMillis()
        val itemPlusKey =
            databaseReference.child("users").child(userId).child("OrderDertails").push().key
        val oderDetails = OrderDetails(
            userId,
            name,
            foodItemName,
            foodItemPrice,
            foodItemImage,
            foodItemQuantities,
            address,
            phone,
            totalAmount,
            false ,
            false,
            itemPlusKey,
            time
        )
        val oderReference = databaseReference.child("OrderDertails").child(itemPlusKey!!)
        oderReference.setValue(oderDetails).addOnSuccessListener {
            Toast.makeText(this, "Đặt hàng thành công", Toast.LENGTH_SHORT).show()
            //intent đến activity thank
            val intent = Intent(this, ThankActivity::class.java)
            startActivity(intent)
            removeCartItems()
            finish()
        }.addOnFailureListener {
            Toast.makeText(this, "Đặt hàng thất bại", Toast.LENGTH_SHORT).show()
        }
    }

    private fun removeCartItems() {
        val cartItemsReference = databaseReference.child("users").child(userId).child("CartItems")
        cartItemsReference.removeValue()
    }

    private fun setUserData() {
        val user = auth.currentUser
        if (user != null) {
            val userId = user.uid
            val userReference: DatabaseReference = databaseReference.child("users").child(userId)
            userReference.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val name = snapshot.child("name").getValue(String::class.java) ?: "N/A"
                        val address =
                            snapshot.child("address").getValue(String::class.java) ?: "N/A"
                        val phone = snapshot.child("phone").getValue(String::class.java) ?: "N/A"

                        // Đặt dữ liệu vào các EditText
                        binding.editTextName.setText(name)
                        binding.editTextAddress.setText(address)
                        binding.editTextPhone.setText(phone)
                    } else {
                        Toast.makeText(
                            this@PayoutActivity,
                            "User data not found",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(
                        this@PayoutActivity,
                        "Failed to load user data: ${error.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
        } else {
            Toast.makeText(this, "User is not logged in", Toast.LENGTH_SHORT).show()
        }
    }
}
