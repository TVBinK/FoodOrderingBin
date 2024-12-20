package com.example.foododering.Fragment

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.foododering.EditProfileActivity
import com.example.foododering.LoginActivity
import com.example.foododering.R
import com.example.foododering.SplashScreen
import com.example.foododering.databinding.FragmentProfileBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.database

class ProfileFragment : Fragment() {
    private lateinit var binding: FragmentProfileBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentProfileBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Khởi tạo FirebaseAuth
        auth = FirebaseAuth.getInstance()

        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            // Lấy email từ Firebase Authentication
            binding.tvEmail.text = currentUser.email

            // Lấy tên người dùng từ Firebase Realtime Database
            val database = Firebase.database.reference
            val userId = currentUser.uid
            database.child("users").child(userId).get().addOnSuccessListener { snapshot ->
                val name = snapshot.child("name").value.toString()
                binding.tvName.text = name
            }.addOnFailureListener {
                binding.tvName.text = "Không thể tải tên"
            }
        }


        // Đăng xuất
        binding.btnLogout.setOnClickListener {
            // Sign out the user from Firebase Authentication
            auth.signOut()
            // Create an intent to launch the LoginActivity
            val intent = Intent(requireActivity(), LoginActivity::class.java)
            startActivity(intent)
            // Finish the current activity
            requireActivity().finish()
        }

        // Chỉnh sửa thông tin
        binding.btnCI.setOnClickListener {
            val intent = Intent(requireActivity(), EditProfileActivity::class.java)
            startActivity(intent)
        }
    }
}
