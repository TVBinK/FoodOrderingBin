package com.example.foododering.Adapter

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.foododering.DetailsActivity
import com.example.foododering.databinding.MenuItemBinding
import com.example.foododering.model.AllMenu
import com.google.firebase.database.DatabaseReference

class AdapterMenu(
    private val menuList : ArrayList<AllMenu>,
    private val databaseReference: DatabaseReference,
    private val requireContext: Context
) : RecyclerView.Adapter<AdapterMenu.MenuViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AdapterMenu.MenuViewHolder {
        val binding = MenuItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MenuViewHolder(binding) 
    }

    override fun onBindViewHolder(holder: MenuViewHolder, position: Int) {
        holder.bind(position)
    }

    override fun getItemCount(): Int = menuList.size

    inner class MenuViewHolder(private val binding: MenuItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
            init {
                binding.root.setOnClickListener{
                    val position = adapterPosition
                    if(position!=RecyclerView.NO_POSITION) {
                        openDetailsActivity(position)
                    }

                }
            }
        fun bind(position: Int) {
            binding.apply {
                val menuItem = menuList[position]
                val uriString = menuItem.foodImage
                val uri = Uri.parse(uriString)
                Glide.with(requireContext).load(uri).into(imgViewItem)
                tvFoodName.text = menuItem.foodName
                tvPrice.text = menuItem.foodPrice

            }
        }

    }

    private fun openDetailsActivity(position: Int) {
            val menuItem = menuList[position]
            // Create an intent to open the DetailsActivity
            val intent = Intent(requireContext, DetailsActivity::class.java).apply {
                putExtra("foodName", menuItem.foodName)
                putExtra("foodImage", menuItem.foodImage)
                putExtra("foodDescription", menuItem.foodDescription)
                putExtra("foodPrice", menuItem.foodPrice)
            }
        requireContext.startActivity(intent)
    }
}