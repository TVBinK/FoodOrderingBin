package com.example.foododering.Adapter

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.foododering.databinding.MenuItemBinding
import com.example.foododering.databinding.PopularItemBinding

import com.example.foododering.model.PopularMenu
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class AdapterHome(
    private val context: Context,
    private val menuList: ArrayList<PopularMenu>,
    private val databaseReference: DatabaseReference,
    private val listener: OnItemClickListener
) : RecyclerView.Adapter<AdapterHome.MenuViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AdapterHome.MenuViewHolder {
        val binding = PopularItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MenuViewHolder(binding)
    }

    interface OnItemClickListener {
        fun onItemClick(item: PopularMenu)
    }

    override fun onBindViewHolder(holder: MenuViewHolder, position: Int) {
        holder.bind(position)
    }

    override fun getItemCount(): Int = menuList.size

    inner class MenuViewHolder(private val binding: PopularItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(position: Int) {
            binding.apply {
                val menuItem = menuList[position]
                val uriString = menuItem.foodImage
                val uri = Uri.parse(uriString)
                Glide.with(context).load(uri).into(imgViewPopularItem)
                tvFoodName.text = menuItem.foodName
                tvPrice.text = menuItem.foodPrice
                itemView.setOnClickListener {
                    listener.onItemClick(menuItem)
                }
            }
        }
    }

}