package com.example.foododering

import android.content.Intent
import android.graphics.Paint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.example.foododering.databinding.ActivitySignBinding
import com.example.foododering.model.UserModel
import com.google.android.gms.tasks.Task
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.database

class SignActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySignBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var name: String
    private lateinit var database: DatabaseReference
    private lateinit var email: String
    private lateinit var password: String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignBinding.inflate(layoutInflater)
        setContentView(binding.root)
        //khởi tạo firebase
        auth = Firebase.auth
        //khởi tạo database
        database = Firebase.database.reference

        binding.tvHaveacc.paintFlags = Paint.UNDERLINE_TEXT_FLAG
        binding.tvHaveacc.setOnClickListener {
            intent = Intent(this@SignActivity, LoginActivity::class.java)
            startActivity(intent)
        }
        binding.btnCreate.setOnClickListener {
            //lấy dữ liệu từ các edittext
            name = binding.edtName.text.toString().trim()
            email = binding.edtEmail.text.toString().trim()
            password = binding.edtPassword.text.toString().trim()
            //kiểm tra dữ liệu
            if(name.isBlank() || email.isBlank() || password.isBlank()){
                Toast.makeText(this@SignActivity,"Please fill all details",Toast.LENGTH_SHORT).show()
            }
            else{
                createAccount(email,password)
            }
        }
    }

    private fun createAccount(email: String, password: String) {
        //tạo tài khoản
            auth.createUserWithEmailAndPassword(email,password).addOnCompleteListener { Task ->
                if(Task.isSuccessful){
                    Toast.makeText(this@SignActivity,"Account created successfully",Toast.LENGTH_SHORT).show()
                    saveUserdata()
                    intent = Intent(this@SignActivity, LoginActivity::class.java)
                    startActivity(intent)
                    finish()
                }
                else{
                    Toast.makeText(this@SignActivity,"Failed to create account",Toast.LENGTH_SHORT).show()
                    // Lỗi
                    // Log.d("account","Failed to create account",Task.exception)
                }
            }
    }

    private fun saveUserdata() {
        //lấy dữ liệu từ các edittext
        name = binding.edtName.text.toString().trim()
        email = binding.edtEmail.text.toString().trim()
        password = binding.edtPassword.text.toString().trim()
        val user = UserModel(name,email,password)
        //chèn dữ liệu vào database
        database.child("users").child(auth.uid!!).setValue(user)
    }


}