package com.example.foododering.Adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.foododering.DetailsActivity
import com.example.foododering.databinding.RecentBuyItemBinding
import com.example.foododering.model.FoodItemRecentBuy

class RecentBuyAdapter(
    private val menuList: MutableList<FoodItemRecentBuy>, // Sử dụng MutableList để quản lý danh sách
    private val context: Context
) : RecyclerView.Adapter<RecentBuyAdapter.MenuViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MenuViewHolder {
        val binding = RecentBuyItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MenuViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MenuViewHolder, position: Int) {
        holder.bind(menuList[position])
    }

    override fun getItemCount(): Int = menuList.size

    inner class MenuViewHolder(private val binding: RecentBuyItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(foodItem: FoodItemRecentBuy) {
            binding.apply {
                // Gán dữ liệu từ `FoodItem` vào View
                tvFoodName.text = foodItem.name
                tvPrice.text = foodItem.price

                // Sử dụng Glide để tải ảnh từ URL
                Glide.with(context)
                    .load(foodItem.imageUrl)
                    .into(imgViewItem)
                btnBuyAgain.setOnClickListener {
                    // Tạo Intent chuyển sang DetailsActivity
                    val intent = Intent(context, DetailsActivity::class.java)

                    // Truyền thông tin món ăn qua Intent
                    intent.putExtra("foodName", foodItem.name)
                    intent.putExtra("foodImage", foodItem.imageUrl)
                    intent.putExtra("foodPrice", foodItem.price)
                    intent.putExtra("foodDescription", foodItem.description)

                    // Bắt đầu activity
                    context.startActivity(intent)
                    }
                }
            }
        }
}

