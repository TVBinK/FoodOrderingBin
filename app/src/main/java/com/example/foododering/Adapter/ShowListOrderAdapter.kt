package com.example.foododering.Adapter

import android.content.Context
import android.net.Uri

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.foododering.databinding.ListOrderItemBinding


class ShowListOrderAdapter(
    private val context: Context,
    private var foodNameList: ArrayList<String>,
    private var foodPriceList: ArrayList<String>,
    private var foodImageList: ArrayList<String>,
    private var foodNameQuantityList: ArrayList<Int>
) : RecyclerView.Adapter<ShowListOrderAdapter.MenuViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MenuViewHolder {
        val binding =
            ListOrderItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MenuViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MenuViewHolder, position: Int) {
       holder.bind(position)
    }

    override fun getItemCount(): Int = foodNameList.size

    inner class MenuViewHolder(private val binding: ListOrderItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(position: Int) {
            binding.apply {
                // Gán dữ liệu từ `FoodItem` vào View
                tvFoodName.text = foodNameList[position]
                tvPrice.text = foodPriceList[position]
                FoodQuantity.text = foodNameQuantityList[position].toString()
                // Sử dụng thư viện Glide để tải và hiển thị ảnh từ URL
                val uriString = foodImageList[position]
                val uri = Uri.parse(uriString)
                Glide.with(context)
                    .load(uri)
                    .into(imgViewItem)
            }
        }
    }
}

