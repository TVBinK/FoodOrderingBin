package com.example.foododering

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.foododering.Fragment.ProfileFragment
import com.example.foododering.databinding.ActivityEditProfileBinding
import com.example.foododering.databinding.ActivityLoginBinding
import com.example.foododering.model.CartItems
import com.example.foododering.model.UserModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class EditProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEditProfileBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)
        //init firebase
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference
        //binding
        binding = ActivityEditProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener {
            finish()
        }
        setUserData()
        binding.btnSaveInfo.setOnClickListener{
            updateUserData()
            finish()
        }
    }

    private fun updateUserData() {
        val name = binding.editTextName.text.toString().trim()
        val phone = binding.editTextPhone.text.toString().trim()
        val address = binding.editTextAddress.text.toString().trim()
        val email = binding.editTextEmail.text.toString().trim()
        val password = binding.editTextPassword.text.toString().trim()

        // Kiểm tra xem thông tin có trống không
        if (name.isEmpty() || phone.isEmpty() || address.isEmpty() || email.isEmpty() || password.isEmpty()) {
            // Bạn có thể hiển thị thông báo lỗi ở đây
            return
        }

        val userId = auth.currentUser?.uid
        if (userId != null) {
            // Sao lưu CartItems trước khi cập nhật thông tin người dùng
            val cartReference = database.child("users").child(userId).child("CartItems")
            cartReference.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(cartSnapshot: DataSnapshot) {
                    // Lấy CartItems cũ và lưu trữ
                    val cartItems = mutableListOf<CartItems>()
                    for (cartItemSnapshot in cartSnapshot.children) {
                        val cartItem = cartItemSnapshot.getValue(CartItems::class.java)
                        cartItem?.let { cartItems.add(it) }
                    }

                    // Cập nhật thông tin người dùng
                    val userReference = database.child("users").child(userId)
                    val user = UserModel(name, email, phone, address, password)
                    userReference.setValue(user).addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            // Sau khi cập nhật thông tin người dùng, lưu lại CartItems
                            saveCartItems(userId, cartItems)
                        } else {
                            // Xử lý lỗi nếu cập nhật thông tin người dùng thất bại
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // Xử lý lỗi nếu không thể đọc CartItems
                }
            })
        } else {
            // Người dùng chưa đăng nhập, điều hướng đến màn hình đăng nhập
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun saveCartItems(userId: String, cartItems: List<CartItems>) {
        // Lưu lại CartItems vào UID người dùng
        val cartReference = database.child("users").child(userId).child("CartItems")
        for (cartItem in cartItems) {
            val cartItemId = cartReference.push().key
            cartReference.child(cartItemId ?: "").setValue(cartItem)
        }
    }


    private fun setUserData() {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            val userReference = database.child("users").child(userId)
            userReference.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val userProfile = snapshot.getValue(UserModel::class.java)
                        if (userProfile != null) {
                            binding.editTextName.setText(userProfile.name)
                            binding.editTextPhone.setText(userProfile.phone)
                            binding.editTextAddress.setText(userProfile.address)
                            binding.editTextEmail.setText(userProfile.email)
                            binding.editTextPassword.setText(userProfile.password)
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })
        }
    }
}