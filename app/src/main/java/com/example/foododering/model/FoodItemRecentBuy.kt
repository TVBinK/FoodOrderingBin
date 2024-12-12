package com.example.foododering.model

data class FoodItemRecentBuy(
    val name: String,      // Tên món ăn
    val imageUrl: String,  // URL hình ảnh
    val price: String,     // Giá món ăn
    val itemPushKey: String, // Khóa để tham chiếu đơn hàng
    val description: String
)


