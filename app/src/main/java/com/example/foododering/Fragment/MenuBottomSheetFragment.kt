package com.example.foododering.Fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.foododering.Adapter.AdapterHome
import com.example.foododering.Adapter.AdapterMenu
import com.example.foododering.databinding.FragmentMenuBottomSheetBinding
import com.example.foododering.model.AllMenu
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.database.*

class MenuBottomSheetFragment : BottomSheetDialogFragment() {
    private lateinit var binding: FragmentMenuBottomSheetBinding
    private lateinit var databaseReference: DatabaseReference
    private val menuItems: ArrayList<AllMenu> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Initialize database reference here to avoid multiple initializations
        databaseReference = FirebaseDatabase.getInstance().reference.child("menu")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMenuBottomSheetBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        retrieveMenuItems()
        binding.btnBack.setOnClickListener {
            dismiss()
        }
    }

    private fun retrieveMenuItems() {
        // Fetch data from the database
        databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                menuItems.clear()
                for (foodSnapshot in snapshot.children) {
                    val menuItem = foodSnapshot.getValue(AllMenu::class.java)
                    menuItem?.let {
                        menuItems.add(it)
                    }
                }
                setupRecyclerView()
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle the error gracefully
                showToast("Failed to retrieve data: ${error.message}")
            }
        })
    }

    private fun setupRecyclerView() {
        if (menuItems.isNotEmpty()) {
            val adapter = AdapterMenu(menuItems, databaseReference, requireActivity())
            binding.menuRecyclerView.layoutManager = LinearLayoutManager(requireActivity())
            binding.menuRecyclerView.adapter = adapter
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }
}
