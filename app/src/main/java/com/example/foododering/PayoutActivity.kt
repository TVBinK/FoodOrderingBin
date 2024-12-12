package com.example.foododering

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPayoutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Khởi tạo Firebase
        auth = FirebaseAuth.getInstance()
        databaseReference = FirebaseDatabase.getInstance().reference

        // Thiết lập dữ liệu người dùng
        setUserData()

        val intent = intent
        val totalAmount = intent.getStringExtra("TotalAmount")
        binding.editTextTotalAmount.setText(totalAmount)
        binding.btnBack2.setOnClickListener {
            finish()
        }
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
                        val address = snapshot.child("address").getValue(String::class.java) ?: "N/A"
                        val phone = snapshot.child("phone").getValue(String::class.java) ?: "N/A"

                        // Đặt dữ liệu vào các EditText
                        binding.editTextName.setText(name)
                        binding.editTextAddress.setText(address)
                        binding.editTextPhone.setText(phone)
                    } else {
                        Toast.makeText(this@PayoutActivity, "User data not found", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@PayoutActivity, "Failed to load user data: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            })
        } else {
            Toast.makeText(this, "User is not logged in", Toast.LENGTH_SHORT).show()
        }
    }
}
