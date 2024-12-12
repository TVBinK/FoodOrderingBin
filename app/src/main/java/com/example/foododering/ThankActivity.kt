package com.example.foododering

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.foododering.databinding.ActivityThankBinding

class ThankActivity : AppCompatActivity() {
    private lateinit var binding: ActivityThankBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityThankBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.btnGoHome.setOnClickListener {
            //intent den home
            intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}