package com.example.foododering.Fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.foododering.Adapter.RecentBuyAdapter
import com.example.foododering.databinding.FragmentHistoryBinding
import com.example.foododering.model.FoodItemRecentBuy
import com.example.foododering.model.OrderDetails
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class HistoryFragment : Fragment() {
    private lateinit var binding: FragmentHistoryBinding
    private lateinit var recentBuyAdapter: RecentBuyAdapter
    private lateinit var database: FirebaseDatabase
    private lateinit var auth: FirebaseAuth
    private val listOfFoodItems: MutableList<FoodItemRecentBuy> = mutableListOf()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Liên kết layout với Fragment
        binding = FragmentHistoryBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Khởi tạo Firebase
        database = FirebaseDatabase.getInstance()
        auth = FirebaseAuth.getInstance()

        // Thiết lập RecyclerView
        setupRecyclerView()

        // Tải lịch sử đơn hàng từ Firebase
        retrieveOrderHistory()

    }

    private fun setupRecyclerView() {
        binding.RecyclerViewRecent.layoutManager = LinearLayoutManager(requireContext())
        recentBuyAdapter = RecentBuyAdapter(listOfFoodItems, requireContext())
        binding.RecyclerViewRecent.adapter = recentBuyAdapter
    }

    private fun retrieveOrderHistory() {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            Toast.makeText(requireContext(), "Vui lòng đăng nhập để xem lịch sử đơn hàng.", Toast.LENGTH_SHORT).show()
            return
        }

        val ordersRef = database.reference.child("users").child(userId).child("OrderDetails")
        ordersRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                listOfFoodItems.clear() // Đảm bảo danh sách không bị trùng lặp dữ liệu

                val foodItemsSet = mutableSetOf<String>()  // Dùng Set để lưu tên món ăn đã xuất hiện

                for (orderSnapshot in snapshot.children) {
                    val orderDetails = orderSnapshot.getValue(OrderDetails::class.java)
                    orderDetails?.let { order ->
                        order.foodNames?.forEachIndexed { index, foodName ->
                            // Nếu món ăn chưa có trong Set, thêm nó vào
                            if (!foodItemsSet.contains(foodName)) {
                                val foodImage = order.foodImages?.getOrNull(index) ?: ""
                                val foodPrice = order.foodPrices?.getOrNull(index) ?: ""

                                // Thêm tên món ăn vào Set để tránh trùng lặp
                                foodItemsSet.add(foodName)

                                // Tạo đối tượng FoodItemRecentBuy và thêm vào danh sách
                                listOfFoodItems.add(
                                    FoodItemRecentBuy(
                                        name = foodName,
                                        imageUrl = foodImage,
                                        price = foodPrice,
                                        itemPushKey = order.itemPushKey ?: "",
                                        description = order.foodDescription?.getOrNull(index) ?: ""
                                    )
                                )
                            }
                        }
                    }
                }

                // Cập nhật RecyclerView sau khi danh sách được lọc
                recentBuyAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(
                    requireContext(),
                    "Không thể tải lịch sử đơn hàng: ${error.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

}
