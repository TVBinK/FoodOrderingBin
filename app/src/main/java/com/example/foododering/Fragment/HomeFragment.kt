package com.example.foododering.Fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.denzcoskun.imageslider.constants.ScaleTypes
import com.denzcoskun.imageslider.models.SlideModel
import com.example.foododering.Adapter.AdapterHome
import com.example.foododering.DetailsActivity
import com.example.foododering.R
import com.example.foododering.databinding.FragmentHomeBinding
import com.example.foododering.model.AllMenu
import com.example.foododering.model.PopularMenu
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class HomeFragment : Fragment() {
    private lateinit var binding: FragmentHomeBinding
    private lateinit var database: FirebaseDatabase
    private lateinit var databaseReference: DatabaseReference
    private var menuItems: ArrayList<PopularMenu> = ArrayList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        //Thiết lập binding với layout fragment_home
        binding = FragmentHomeBinding.inflate(layoutInflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Khoi tao Firebase Reference
        databaseReference = FirebaseDatabase.getInstance().reference

        setupImageSlider() // Cai dat slider hinh anh
        setupViewMoreButton() // Cai dat nut xem them
        retrieveMenuItems() // Lay du lieu mon an tu Firebase
    }

    private fun retrieveMenuItems() {
        database = FirebaseDatabase.getInstance() // Khoi tao Firebase
        val foodRef: DatabaseReference = database.reference.child("home")// Lấy node home trong database
        //lấy dữ liệu từ node home
        foodRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                menuItems.clear()// // Xóa hết các mục cũ trong danh sách trước khi cập nhật lại
                for (foodSnapshot in snapshot.children) {
                    val PopularMenu = foodSnapshot.getValue(PopularMenu::class.java)
                    PopularMenu?.let {
                        menuItems.add(it) // Thêm món ăn vào danh sách
                    }
                }
                setupRecyclerView()
                binding.progressBar.visibility = View.GONE
            }

            override fun onCancelled(error: DatabaseError) {
                binding.progressBar.visibility = View.GONE
            }
        })
    }


    private fun setupImageSlider() {
        val imageList = ArrayList<SlideModel>()

        imageList.add(SlideModel(R.drawable.banner2, scaleType = ScaleTypes.FIT))
        imageList.add(SlideModel(R.drawable.banner1, scaleType = ScaleTypes.FIT))
        imageList.add(SlideModel(R.drawable.banner3, scaleType = ScaleTypes.FIT))

        val imageSlider = binding.imageSlider
        imageSlider.setImageList(imageList, ScaleTypes.FIT)
    }


    private fun setupViewMoreButton() {
        binding.tviViewMore.setOnClickListener {
            val bottomSheetDialog = MenuBottomSheetFragment()
            bottomSheetDialog.show(parentFragmentManager, "MenuBottomSheet")
        }
    }


    private fun setupRecyclerView() {
        val adapter = AdapterHome(requireActivity(), menuItems, databaseReference, object : AdapterHome.OnItemClickListener {
            override fun onItemClick(item: PopularMenu) {
                // Chuyển đến DetailsActivity với dữ liệu
                val intent = Intent(requireActivity(), DetailsActivity::class.java)
                intent.putExtra("foodName", item.foodName)
                intent.putExtra("foodImage", item.foodImage)
                intent.putExtra("foodPrice", item.foodPrice)
                intent.putExtra("foodDescription", item.foodDescription)
                startActivity(intent)
            }
        })

        binding.PopularFoodHome.layoutManager = LinearLayoutManager(
            requireActivity(),
            LinearLayoutManager.HORIZONTAL,
            false
        )
        binding.PopularFoodHome.adapter = adapter
    }


}