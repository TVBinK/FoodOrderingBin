package com.example.foododering

import OrderDetails
import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.foododering.databinding.ActivityPayoutBinding
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class PayoutActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var databaseReference: DatabaseReference
    private lateinit var binding: ActivityPayoutBinding
    private lateinit var userId: String

    private var name: String = ""
    private var address: String = ""
    private var phone: String = ""
    private var totalAmount: String = ""
    private var orderNumber: Int = 1

    private lateinit var foodItemName: List<String>
    private lateinit var foodItemPrice: List<String>
    private lateinit var foodItemImage: List<String>
    private lateinit var foodItemQuantities: List<Int>
    private lateinit var foodDescription: List<String>

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var latitude: Double = 0.0
    private var longitude: Double = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPayoutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Firebase Initialization
        auth = FirebaseAuth.getInstance()
        databaseReference = FirebaseDatabase.getInstance().reference
        userId = auth.currentUser?.uid.orEmpty()

        // Initialize Location Services
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        // tắt nút khi chưa bật vị trí
        binding.btnPlaceMyOrder.isEnabled = false

        // Kiểm tra quyền và trạng thái GPS
        checkLocationPermissionAndGPS()

        // Initialize user data
        setUserData()

        // Retrieve data from Intent
        totalAmount = intent.getStringExtra("TotalAmount").orEmpty()
        foodItemName = intent.getStringArrayListExtra("FoodItemName") ?: emptyList()
        foodItemPrice = intent.getStringArrayListExtra("FoodItemPrice") ?: emptyList()
        foodItemImage = intent.getStringArrayListExtra("FoodItemImage") ?: emptyList()
        foodItemQuantities = intent.getIntegerArrayListExtra("FoodItemQuantities") ?: emptyList()
        foodDescription = intent.getStringArrayListExtra("FoodItemDescription") ?: emptyList()

        binding.editTextTotalAmount.setText(totalAmount)

        // Set button listeners
        binding.btnBack2.setOnClickListener { finish() }

        binding.btnPlaceMyOrder.setOnClickListener {
            name = binding.editTextName.text.toString().trim()
            address = binding.editTextAddress.text.toString().trim()
            phone = binding.editTextPhone.text.toString().trim()

            if (validateInput()) {
                fetchOrderNumberAndPlaceOrder()
            }
        }
    }

    private fun validateInput(): Boolean {
        return if (name.isBlank() || address.isBlank() || phone.isBlank()) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show()
            false
        } else {
            true
        }
    }

    private fun getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
            return
        }

        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                latitude = location.latitude
                longitude = location.longitude
                binding.btnPlaceMyOrder.isEnabled = true // Bật nút khi có vị trí
            }
        }.addOnFailureListener {
            Toast.makeText(this, "Lỗi khi lấy vị trí: ${it.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun checkLocationPermissionAndGPS() {
        if (!isLocationEnabled()) {
            requestLocationSettings()
            return
        }

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
            return
        }

        // Nếu đã bật vị trí và có quyền, lấy vị trí hiện tại
        getCurrentLocation()
    }

    private fun isLocationEnabled(): Boolean {
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }

    private fun requestLocationSettings() {
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000).build()
        val locationSettingsRequest = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)
            .setAlwaysShow(true) // Hiển thị hộp thoại yêu cầu bật GPS
            .build()

        val settingsClient: SettingsClient = LocationServices.getSettingsClient(this)
        val task = settingsClient.checkLocationSettings(locationSettingsRequest)

        task.addOnSuccessListener {
            // GPS đã bật, lấy vị trí
            getCurrentLocation()

        }

        task.addOnFailureListener { exception ->
            if (exception is ResolvableApiException) {
                try {
                    // Hiển thị hộp thoại yêu cầu bật GPS
                    exception.startResolutionForResult(this, LOCATION_PERMISSION_REQUEST_CODE)
                } catch (sendEx: IntentSender.SendIntentException) {
                    Toast.makeText(this, "Không thể bật GPS", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "GPS chưa được bật", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun fetchOrderNumberAndPlaceOrder() {
        val numberReference = databaseReference.child("users").child(userId).child("OrderNumber")
        numberReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                orderNumber = snapshot.getValue(Int::class.java) ?: 1
                placeOrder()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(
                    this@PayoutActivity,
                    "Lỗi khi lấy số thứ tự đơn hàng: ${error.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    private fun placeOrder() {
        val time = System.currentTimeMillis()
        val itemPushKey = databaseReference.child("users").child(userId).child("OrderDetails").push().key

        val orderDetails = OrderDetails(
            userUid = userId,
            userName = name,
            foodNames = foodItemName,
            foodImages = foodItemImage,
            foodPrices = foodItemPrice,
            foodQuantities = foodItemQuantities,
            foodDescription = foodDescription,
            address = address,
            totalPrice = totalAmount,
            phoneNumber = phone,
            orderAccepted = "Waiting",
            paymentReceived = false,
            itemPushKey = itemPushKey,
            currentTime = time,
            orderNumber = orderNumber,
            latitude = latitude,      
            longitude = longitude
        )

        val userOrderReference = databaseReference.child("users").child(userId).child("OrderDetails").child(itemPushKey!!)
        userOrderReference.setValue(orderDetails).addOnSuccessListener {
            Toast.makeText(this, "Đặt hàng thành công", Toast.LENGTH_SHORT).show()
            updateOrderNumber()
            navigateToThankActivity()
            removeCartItems()
        }.addOnFailureListener {
            Toast.makeText(this, "Đặt hàng thất bại", Toast.LENGTH_SHORT).show()
        }

        val globalOrderReference = databaseReference.child("OrderDetails").child(itemPushKey!!)
        globalOrderReference.setValue(orderDetails).addOnFailureListener {
            Toast.makeText(this, "Không thể lưu đơn hàng vào nhánh chung", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateOrderNumber() {
        val numberReference = databaseReference.child("users").child(userId).child("OrderNumber")
        numberReference.setValue(orderNumber + 1).addOnFailureListener {
            Toast.makeText(
                this,
                "Không thể cập nhật số thứ tự đơn hàng: ${it.message}",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun removeCartItems() {
        val cartItemsReference = databaseReference.child("users").child(userId).child("CartItems")
        cartItemsReference.removeValue()
    }

    private fun navigateToThankActivity() {
        val intent = Intent(this, ThankActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun setUserData() {
        val userReference = databaseReference.child("users").child(userId)
        userReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    name = snapshot.child("name").getValue(String::class.java).orEmpty()
                    address = snapshot.child("address").getValue(String::class.java).orEmpty()
                    phone = snapshot.child("phone").getValue(String::class.java).orEmpty()

                    binding.editTextName.setText(name)
                    binding.editTextAddress.setText(address)
                    binding.editTextPhone.setText(phone)
                } else {
                    Toast.makeText(this@PayoutActivity, "Không tìm thấy dữ liệu người dùng", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@PayoutActivity, "Lỗi khi tải dữ liệu người dùng: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                checkLocationPermissionAndGPS()
            } else {
                Toast.makeText(this, "Quyền truy cập vị trí bị từ chối", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                getCurrentLocation()
            } else {
                Toast.makeText(this, "Bạn cần bật GPS để đặt hàng", Toast.LENGTH_SHORT).show()
            }
        }
    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1001
    }
}
