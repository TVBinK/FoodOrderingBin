package com.example.foododering

import OrderDetails
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.foododering.Adapter.ShowListOrderAdapter
import com.example.foododering.databinding.ActivityShowListOrderBinding


class ShowListOrderActivity : AppCompatActivity() {
    private lateinit var binding: ActivityShowListOrderBinding
    private lateinit var adapter: ShowListOrderAdapter

    private var foodNameList: ArrayList<String> = ArrayList()
    private var foodPriceList: ArrayList<String> = ArrayList()
    private var foodImageList: ArrayList<String> = ArrayList()
    private var foodQuantityList: ArrayList<Int> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityShowListOrderBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Thiết lập nút quay lại
        binding.btnBack.setOnClickListener {
            finish()
        }
        // Nhận danh sách đơn hàng từ Intent
        val showListOrders = intent.getSerializableExtra("listOrder") as? ArrayList<OrderDetails>
        //btn view map
        binding.btnViewMap.setOnClickListener {
            //itent to map activity
            val intent = Intent(this, MapActivity::class.java)
            startActivity(intent)
        }

        showListOrders?.let { orderDetails ->
            if (orderDetails.isNotEmpty()) {
                val showListOrder = orderDetails[0]
                foodNameList.addAll(showListOrder.foodNames ?: emptyList())
                foodPriceList.addAll(showListOrder.foodPrices ?: emptyList())
                foodImageList.addAll(showListOrder.foodImages ?: emptyList())
                foodQuantityList.addAll(showListOrder.foodQuantities ?: emptyList())
            }
        }

        // Thiết lập RecyclerView
        setUpAdapter()
    }

    private fun setUpAdapter() {
        val recyclerView = binding.RecycleView
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = ShowListOrderAdapter(this, foodNameList, foodPriceList, foodImageList, foodQuantityList)
        recyclerView.adapter = adapter
        adapter.notifyDataSetChanged()
    }
}
