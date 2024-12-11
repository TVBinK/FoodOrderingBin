package com.example.foododering.model

data class CartItems(
    val foodName: String? = null,
    val foodPrice: String? = null,
    val foodImage: String? = null,
    val foodDescription: String? = null,
    var foodQuantity: Int? = null
)
